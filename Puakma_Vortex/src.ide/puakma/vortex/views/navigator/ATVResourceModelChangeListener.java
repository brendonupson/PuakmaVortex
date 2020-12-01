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

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import puakma.utils.lang.UniqueArrayList;
import puakma.vortex.VortexPlugin;
import puakma.vortex.project.ProjectUtils;
import puakma.vortex.project.PuakmaProject2;

public class ATVResourceModelChangeListener implements IResourceChangeListener, IResourceDeltaVisitor
{
  private ApplicationTreeViewer viewer;

  public ATVResourceModelChangeListener(ApplicationTreeViewer viewer)
  {
    this.viewer = viewer;
  }
  
  public void resourceChanged(IResourceChangeEvent event)
  {
    switch(event.getType()) {
      case IResourceChangeEvent.POST_CHANGE:
        IResourceDelta delta = event.getDelta();
        
        IResourceDelta[] deltas = delta.getAffectedChildren();
        
        for(int i = 0; i < deltas.length; ++i) {
          // AT FIRST CHECK IF THIS IS IN OUR PUAKMA PROJECT
          if(ProjectUtils.getProject(deltas[i].getResource()) != null) {
            // GET THE SOURCE DELTA
            IResourceDelta srcDelta = deltas[i].findMember(new Path(PuakmaProject2.DIR_SRC));
            
            if(srcDelta != null)
            // OK, SO THIS IS FROM OUR SRC DIRECTORY, SO WE CAN DIVE IN
            try {
              srcDelta.accept(this);
            }
            catch(CoreException ex) {
              VortexPlugin.log(ex);
            }
          }
        }
      break;
    }
  }

  public boolean visit(IResourceDelta delta) throws CoreException
  {
    final List fragments = new UniqueArrayList();
    final List cus = new UniqueArrayList();

    // SO NOW WE ARE INSIDE PROJECT, SO WE WANT TO CHECK SOURCE FOLDER
    listAllCompilationUnits(fragments, cus, delta);
    
    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        updateViewer(fragments, cus);
      }
    });
    
    return false;
  }

  /**
   * Lists all the fragments, and compilation units which we have to refresh.
   *
   * @param fragments
   * @param cus
   * @param delta
   */
  private void listAllCompilationUnits(List fragments, List cus, IResourceDelta delta)
  {
    IResource res = delta.getResource();
    if(res instanceof IFolder) {
      // CHECK IF WE HAVEN'T REMOVED THE FOLDER, IF YES, ADD PARENT TO REFRESH
      if(delta.getKind() == IResourceDelta.REMOVED) {
        IFolder folder = (IFolder) res;
        IResource parentResource = folder.getParent();
        if(parentResource instanceof IFolder) {
          IFolder parent = (IFolder) parentResource;
          IJavaElement el = JavaCore.create(parent);
          if(el instanceof IPackageFragment && ((IPackageFragment) el).isDefaultPackage() == false) {
            fragments.add(el);
          }
        }
      }
      else {
        IResourceDelta[] children = delta.getAffectedChildren();
        
        // SO RECURSIVELY DIVE
        for(int i = 0; i < children.length; ++i)
          listAllCompilationUnits(fragments, cus, children[i]);
      }
    }
    else if(res instanceof IFile) {
      IFile file = (IFile) res;
      
      // IF RESOURCE HAS BEEN REMOVED, REFRESH PARENT
      if(delta.getKind() == IResourceDelta.REMOVED) {
        IFolder parent = (IFolder) file.getParent();
        IJavaElement fragment = JavaCore.create(parent);
        fragments.add(fragment);
        // AND CHECK OUT ALSO IF ALL PARENTS NEED REFRESH
        parentRefresh(fragment, fragments);
      }
      // CHECK IF MARKERS HAVE CHANGED
      else if((delta.getFlags() & IResourceDelta.MARKERS) == IResourceDelta.MARKERS) {
        // NOW WE SHOULD GET COMPILATION UNIT TO REFRESH, AND ALSO PACKAGE FRAGMENT TO REFRESH
        ICompilationUnit cu = JavaCore.createCompilationUnitFrom(file);
        
        parentRefresh(cu.getParent(), fragments);
      }
      // OR IGNORE IT
    }
    else {
      VortexPlugin.log("UNKNOWN resource delta: " + delta);
    }
  }

  private void parentRefresh(IJavaElement fragment, List fragments)
  {
    if(fragment instanceof IPackageFragment) {
      fragments.add(fragment);
      parentRefresh(fragment.getParent(), fragments);
    }
  }
  
  /**
   * Updates viewer with all changes in java model
   *
   * @param fragments is the list of fragments to update
   * @param cus is the list of compilation units to update
   */
  private void updateViewer(List fragments, List cus)
  {
    Control c = viewer.getControl();
    ApplicationTreeViewController controller = (ApplicationTreeViewController) viewer.getContentProvider();
    
    c.setRedraw(false);
    
    controller.refreshTornadoNodes();
    
    refreshJavaElements(fragments);
    refreshJavaElements(cus);
    
    c.setRedraw(true);
  }

  /**
   * Refreshes all java elements in the class browser.
   * @param javaElements is the list of java elements to refresh
   */
  private void refreshJavaElements(List javaElements)
  {
    Iterator it = javaElements.iterator();
    while(it.hasNext()) {
      IJavaElement el = (IJavaElement) it.next();
      viewer.refresh(el, true);
    }
  }
}
