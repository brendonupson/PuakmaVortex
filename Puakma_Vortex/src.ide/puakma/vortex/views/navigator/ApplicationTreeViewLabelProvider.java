/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jan 5, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.views.navigator;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IViewerLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import puakma.coreide.database.SQLUtil;
import puakma.coreide.objects2.ApplicationObject;
import puakma.coreide.objects2.JavaObject;
import puakma.coreide.objects2.TableColumn;
import puakma.vortex.VortexPlugin;
import puakma.vortex.WorkbenchUtils;
import puakma.vortex.controls.TreeObject;
import puakma.vortex.preferences.PreferenceConstants;
import puakma.vortex.project.ProjectUtils;
import puakma.vortex.views.parts.ATVBaseNode;

class ApplicationTreeViewLabelProvider extends LabelProvider implements ILabelProvider, IViewerLabelProvider
{
  private PmaPackagesViewLabelProvider javaProvider;
  private Viewer viewer;
//  private Font fontCopy;
    
  public ApplicationTreeViewLabelProvider(Viewer viewer)
  {
    super();
    
    this.viewer = viewer;
    
//    final Font font = getViewer().getControl().getFont();
////    GC gc = new GC(Display.getDefault());
//    FontData[] fd = font.getFontData();
//    fontCopy = new Font(Display.getDefault(), fd[0].getName(), fd[0].getHeight(), fd[0].getStyle() | SWT.BOLD);
//    getViewer().getControl().addDisposeListener(new DisposeListener() {
//      public void widgetDisposed(DisposeEvent e)
//      {
//        fontCopy.dispose();
//      }
//    });
    
    javaProvider = new PmaPackagesViewLabelProvider();
  }
  
  public String getText(Object obj)
  {
    if(obj instanceof ATVBaseNode) {
      ATVBaseNode node = (ATVBaseNode) obj;
      return node.getText();
    }
    else if(obj instanceof TreeObject) {
      TreeObject o = (TreeObject) obj;
      return o.getName();
    }
    else if(obj instanceof IFolder) {
      IFolder folder = (IFolder) obj;
      
      // try to check if the folder is top level source, if yes, then return string "Classes"
      if(isSourceFolder(folder))
        return "Class Browser";
    }
    else if(obj instanceof ICompilationUnit) {
      ICompilationUnit cu = (ICompilationUnit) obj;
      return cu.getElementName();
    }
    else if(obj instanceof IJavaElement) {
      if(obj instanceof IMethod) {
        IMethod m = (IMethod) obj;
        return m.getElementName() + "()";
      }
      else if(obj instanceof IPackageFragment) {
        return getPackageText((IPackageFragment) obj);
      }
      else {
        IJavaElement m = (IJavaElement) obj;
        return m.getElementName();
      }
    }
    
    ApplicationObject ao = (ApplicationObject) AdapterUtils.getObject(obj, ApplicationObject.class);
    if(ao != null) {
      // FORMAT TABLE COLUMNS SOMEHOW DIFFERENTLY
      if(ao instanceof TableColumn) {
        TableColumn col = (TableColumn) ao;
        StringBuffer sb = new StringBuffer();
        sb.append(col.getName());
        sb.append(" ");
        sb.append(col.getType());
        if(col.getTypeSize() != -1) {
          sb.append(" (").append(col.getTypeSize());
          if(SQLUtil.isFloatType(col.getType()) && col.getFloatDecimals() != -1)
            sb.append(", ").append(col.getFloatDecimals());
          sb.append(")");
        }
        return sb.toString();
      }
      
      return ao.getName();
    }

    return super.getText(obj);
  }

  /**
   * Gets the package fragment text. This is done according to the state of the viewer whether it
   * shows flat or hierarchical packages. So it might be: String or java.lang.String depending
   * on the context.
   * 
   * @param fragment is the package fragment we want the text from
   * @return String with the name of package
   */
  private String getPackageText(IPackageFragment fragment)
  {
    IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
    boolean useFlatPackages = store.getBoolean(PreferenceConstants.PREF_NAVIGATOR_USE_FLAT_PACKAGES);
    if(useFlatPackages) {
      if(fragment.isDefaultPackage())
        return "(default package)";
      return fragment.getElementName();
    }
    else {
      String fullName = fragment.getElementName();
      int dotIndex = fullName.lastIndexOf('.');
      if(dotIndex == -1)
        return fullName;
      else
        return fullName.substring(dotIndex + 1);
    }
  }

  /**
   * Returns true if the folder is top level source folder.
   * @param folder is the folder to check
   * @return true if the folder is top level source folder, otherwise false
   */
  private boolean isSourceFolder(IFolder folder)
  {
    IPath fullPath = folder.getFullPath();
    fullPath = fullPath.removeFirstSegments(1);
    int segments = fullPath.segmentCount();
    if(segments == 1 && "src".equals(fullPath.segment(0)))
        return true;

    return false;
  }

  public Image getImage(Object obj)
  {
    String imgKey = null;
    
    if(obj instanceof ATVBaseNode) {
      ATVBaseNode node = (ATVBaseNode) obj;
      return node.getIcon();
    }

    ApplicationObject ao = (ApplicationObject) AdapterUtils.getObject(obj, ApplicationObject.class);
    if(ao != null) {
      if(ao instanceof JavaObject) {
//        IFile file = ProjectUtils.getIFile((DesignObject) ao, false);
        ICompilationUnit cu = ProjectUtils.getCompilationUnit((JavaObject) ao);
        return javaProvider.getImage(cu);
      }
      else
        return WorkbenchUtils.getImageFromCache(ao);
    }
    else if(obj instanceof ATVApplicationNode)
      imgKey = "puakma.gif";
    else if(obj instanceof ATVDbConnectionNode) {
      return WorkbenchUtils.getImageFromCache(((ATVDbConnectionNode)obj).getDatabaseObject());
    }
    else if(obj instanceof ATVParentNode) {
      ATVParentNode pn = (ATVParentNode) obj;
      if(pn.getNodeType() == ApplicationTreeViewController.NODE_DATABASES)
        imgKey = "databases.gif";
      else if(pn.getNodeType() == ApplicationTreeViewController.NODE_SHARED_CODE) {
        imgKey = "library.gif";
      }
      else
        imgKey = "folder.gif";
    }
    else if(obj instanceof IFolder) {
      IFolder folder = (IFolder) obj;
      IPath path = folder.getFullPath().removeFirstSegments(1);
      if(path.segmentCount() == 1)
        imgKey = "packagefolder.gif";
      else
        imgKey = "package.gif";
    }
    else
      return javaProvider.getImage(obj);

    if(imgKey != null) {
      Image img = VortexPlugin.getImage(imgKey);
      return img;
    }

    return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
  }
  
  public Viewer getViewer()
  {
    return viewer;
  }

  public void updateLabel(ViewerLabel label, Object element)
  {
    label.setText(getText(element));
//    label.setFont(fontCopy);
  }
}
