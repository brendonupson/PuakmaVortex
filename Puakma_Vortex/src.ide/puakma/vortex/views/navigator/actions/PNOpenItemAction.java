/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jan 4, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.views.navigator.actions;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import puakma.coreide.PuakmaCoreException;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.ApplicationObject;
import puakma.coreide.objects2.DatabaseConnection;
import puakma.coreide.objects2.DesignObject;
import puakma.vortex.VortexPlugin;
import puakma.vortex.WorkbenchUtils;
import puakma.vortex.preferences.PreferenceConstants;
import puakma.vortex.views.navigator.ATVApplicationNode;
import puakma.vortex.views.navigator.ATVDbConnectionNode;
import puakma.vortex.views.navigator.ATVParentNode;
import puakma.vortex.views.navigator.AdapterUtils;
import puakma.vortex.views.navigator.PuakmaResourceView;

/**
 * TODO: IMPLEMENT THIS CLASS
 * @author Martin Novak
 */
public class PNOpenItemAction extends BasePmaViewAction implements IDoubleClickListener
{
  public PNOpenItemAction(PuakmaResourceView view)
  {
    super(view, "Open item");
    setToolTipText("Opens item in editor");
    setSharedImage(ISharedImages.IMG_OBJS_INFO_TSK);
  }
  
  public void run()
  {

  }

  public void doubleClick(DoubleClickEvent event)
  {
    try {
      IStructuredSelection selection = (IStructuredSelection) event.getSelection();
      Iterator it = selection.iterator();
      while(it.hasNext()) {
        Object o = it.next();
        openSelection(o);
      }
    }
    catch(Exception e) {
      VortexPlugin.log(e);
      // TODO: add notification for the user
    }
  }

  /**
   * Opens design object in the Vortex Tornado Navigator. So if there is some
   * settings for the Navigator view like what kind of editor it should open, it
   * is handled here.
   * 
   * @param o
   * @throws JavaModelException
   * @throws PartInitException
   */
  private void openSelection(final Object o) throws JavaModelException, PartInitException
  {
    DesignObject obj = (DesignObject) AdapterUtils.getObject(o, DesignObject.class);
    if(obj != null) {
      WorkbenchUtils.openDesignObject(obj);
      return;
    }
    
    final DatabaseConnection dc = (DatabaseConnection) AdapterUtils.getObject(o, DatabaseConnection.class);
    if(dc != null) {
      if(dc.getDatabase().isOpen() == false) {
        Job j = new Job("Database refresh") {
          protected IStatus run(IProgressMonitor monitor) {
            try {
              dc.getDatabase().refresh();
              Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                  databaseDefaultAction(o, dc);
                }
              });
            }
            catch(PuakmaCoreException e) {
              VortexPlugin.log(e);
            }
            return Status.OK_STATUS;
          }
        };
        j.schedule();
      }
      else
        databaseDefaultAction(o, dc);
      
      return;
    }
    
    ApplicationObject ao = (ApplicationObject) AdapterUtils.getObject(o, ApplicationObject.class);
    if(ao != null) {
      WorkbenchUtils.openApplicationObject(ao);
      return;
    }
    
    ICompilationUnit cu = (ICompilationUnit) AdapterUtils.getObject(o, ICompilationUnit.class);
    if(cu != null) {
      JavaUI.openInEditor(cu);
      return;
    }
    
    IPackageFragment fragment = (IPackageFragment) AdapterUtils.getObject(o, IPackageFragment.class);
    if(fragment != null) {
      if(view.getViewer().getExpandedState(fragment) == true)
        view.getViewer().collapseToLevel(fragment, 1);
      else
        view.getViewer().expandToLevel(fragment, 1);
      return;
    }
    
    if(o instanceof ATVApplicationNode) {
      ATVApplicationNode node = (ATVApplicationNode) o;
      Application application = node.application;
      WorkbenchUtils.openApplicationEditor(application);
    }
    else if(o instanceof ATVDbConnectionNode) {
      ATVDbConnectionNode dbon = (ATVDbConnectionNode) o;
      WorkbenchUtils.openDatabaseObject(dbon.getDatabaseObject());
    }
    else if(o instanceof ATVParentNode) {
      // add special code for keywords
      ATVParentNode node = (ATVParentNode) o;
      ATVApplicationNode appNode = node.getAppNode();
      boolean state = view.getViewer().getExpandedState(o);
      view.getViewer().setExpandedState(o, ! state);
    }
    else if(o instanceof IJavaElement) {
      JavaUI.openInEditor((IJavaElement) o);
    }
    else if(o instanceof IFile) {
      IWorkbench workbench = PlatformUI.getWorkbench();
      IWorkbenchPage p = workbench.getActiveWorkbenchWindow().getActivePage();
      IDE.openEditor(p, (IFile)o);
    }
    else if(o instanceof IFolder) {
      view.getViewer().setExpandedState(o, !view.getViewer().getExpandedState(o));
    }
  }

  private void databaseDefaultAction(Object o, final DatabaseConnection dc)
  {
    IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
    String action = store.getString(PreferenceConstants.PREF_DB_DOUBLECLICK_DEFAULT_ACTION);
    if(PreferenceConstants.DB_DBLCLK_OPEN_QUERY.equals(action))
      WorkbenchUtils.openDatabaseQueryEditor(dc);
    else if(PreferenceConstants.DB_DBLCLK_OPEN_DB_SETTINGS.equals(action))
      WorkbenchUtils.openDatabaseSettings(dc);
    else if(PreferenceConstants.DB_DBLCLK_UNFOLD.equals(action)) {
      TreeViewer viewer = view.getViewer();
      viewer.setExpandedState(o, ! viewer.getExpandedState(o));
    }
    else {
      WorkbenchUtils.openDatabaseSchemaEditor(dc);
    }
  }

//  /**
//   * Opens design object
//   * @param obj
//   * @throws PartInitException
//   */
//  public static void openDesignNode(ApplicationObject obj) throws PartInitException
//  {
//    EditorUtils.openDesignObject(obj);
//  }

//  public static void openDesignObject(DesignObject dobj) throws PartInitException
//  {
//    if(dobj instanceof JavaObject) {
//      if(dobj.getDesignSize(true) == 0) {
////        MessageDialog.openError(shell,"Cannot open source editor", "No source associated with the object");
////        return;
//      }
//      else {
//        PuakmaProject2 prj = ProjectManager.getProject(dobj.getApplication());
//        boolean isSource = true;
//        IFile file = ProjectUtils.getIFile(dobj, isSource);
//        IWorkbenchPage p = JavaPlugin.getActivePage();
//        if(p != null) {
//          IDE.openEditor(p, file);
//          return;
////        JavaUI.openInEditor((IJavaElement) file);
//        }
//      }
//    }
//    
//    openDesignEditor(dobj);
//  }

//  /**
//   * @param dobj
//   * @throws PartInitException
//   */
//  private static void openDesignEditor(DesignObject dobj) throws PartInitException
//  {
//    IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
//    IWorkbenchPage page = activeWorkbenchWindow.getActivePage();
//
//    IEditorInput finput = new PuakmaEditorInput(dobj);
//    page.openEditor(finput,PuakmaEditor.EDITOR_ID,true);
//  }

  public boolean qualifySelection()
  {
    IStructuredSelection selection = (IStructuredSelection) view.getViewer().getSelection();
    Object firstElement = selection.getFirstElement();
    if(firstElement instanceof ApplicationObject) {
      return true;
    }
    else if(firstElement instanceof ATVApplicationNode) {
      return true;
    }
//    else if(firstElement instanceof ATVParentNode) {
//      ATVParentNode pNode = firstElement;
//      ATVApplicationNode parent = pNode.getParent();
//      if(pNode == parent.)
//        pNode.
//    }
    return false;
  }
}
