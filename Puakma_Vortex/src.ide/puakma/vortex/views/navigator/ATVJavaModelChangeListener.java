/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Nov 4, 2005
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.swt.widgets.Display;

import puakma.coreide.objects2.DesignObject;
import puakma.vortex.project.ProjectUtils;

public class ATVJavaModelChangeListener implements IElementChangedListener
{
  private ApplicationTreeViewer viewer;

  public ATVJavaModelChangeListener(ApplicationTreeViewer viewer)
  {
    this.viewer = viewer;
  }

  public void elementChanged(final ElementChangedEvent event)
  {
    // NOW GO THRU ALL THE ELEMENTS AND FIND DESIGN OBJECTS WHICH ARE
    // ENVOLVED TO UPDATE THEM
    IJavaElementDelta delta = event.getDelta();
    final List<IAdaptable> changedList = new ArrayList<IAdaptable>();
    addChildren(changedList, delta);
    doUpdateTree(changedList);
  }
  
  private void doUpdateTree(final List<IAdaptable> changedList)
  {
    ApplicationTreeViewController provider = (ApplicationTreeViewController) viewer.getContentProvider();
    final ATVApplicationNode[] nodes = provider.listApplicationNodes();
    final IFolder[] srcFolders = listSourceFolders(nodes);
    Display.getDefault().asyncExec(new Runnable() {
      public void run()
      {
        if(viewer.getTree().isDisposed() == false) {
          viewer.getTree().setRedraw(false);
          
          // UPDATE SOURCE FOLDERS
          viewer.update(srcFolders, null);

          // UPDATE ALL THE ACTIONS, ETC...
          for(int i = 0; i < nodes.length; ++i) {
            ATVApplicationNode node = nodes[i];
            Object[] nodesList = new Object[] { node.actionsNode,
                node.scheduledNode, node.widgetsNode, node.sharedNode };
            for(int j = 0; j < nodesList.length; ++j)
              viewer.refresh(nodesList[j], true);
             viewer.update(nodes[i], null);
          }

          // AND NOW UPDATE THE SOURCE TREE
          Object[] o = changedList.toArray();
//          for(int i = 0; i < o.length; ++i)
//            viewer.refresh(o[i], true);
           viewer.update(o, null);
//           for(int i = 0; i < o.length; ++i) {
//             if(o[i] instanceof IJavaElement)
//               System.out.println("REFRESH: " + ((IJavaElement)o[i]).getElementName());
//           }
          
          viewer.getTree().setRedraw(true);
        }
      }
    });
  }

  private IFolder[] listSourceFolders(ATVApplicationNode[] nodes)
  {
    IFolder[] folders = new IFolder[nodes.length];
    for(int i = 0; i < nodes.length; ++i) {
      folders[i] = nodes[i].srcNode;
    }
    return folders;
  }

  private void addChildren(List<IAdaptable> changedList, IJavaElementDelta delta)
  {
    // System.out.println("CHANGE ELEMENT: " +
    // delta.getElement().getElementName());
    IJavaElement el = delta.getElement();
    if(el instanceof IPackageFragment) {
      //System.out.println("FRAGMENT: " + delta.getElement().getElementName());
      changedList.add(el);
      IJavaElementDelta[] d = delta.getAffectedChildren();
      for(int i = 0; i < d.length; ++i) {
        addChildren(changedList, d[i]);
      }
    }
    if(el instanceof IJavaModel || el instanceof IJavaProject
        || el instanceof IPackageFragmentRoot) {
      IJavaElementDelta[] d = delta.getAffectedChildren();
      for(int i = 0; i < d.length; ++i) {
        addChildren(changedList, d[i]);
      }
    }
    else if(el instanceof ICompilationUnit) {
      ICompilationUnit cu = (ICompilationUnit) el;
      //System.out.println("COMPILATION UNIT: " + delta.getElement().getElementName());
      changedList.add(cu);
//      try {
//        System.out.println("CU SEVERITY: " + JdtUtils.getCompilationUnitSeverity(cu));
//      }catch(Exception ex) {ex.printStackTrace();}

      try {
        IResource res = cu.getCorrespondingResource();
        if(res instanceof IFile) {
          DesignObject dob = ProjectUtils.getDesignObject((IFile) res);
          if(dob != null) {
            changedList.add(ApplicationTreeViewController.getWrapper(dob));
          }
        }
      }
      catch(Exception e) {
      }
    }
  }
}
