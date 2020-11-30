/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Nov 6, 2004
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.controls;

import java.util.Iterator;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.Server;


/**
 * @author Martin Novak
 */
class ServerTreeViewController extends BaseTreeViewController
                               implements IStructuredContentProvider, ITreeContentProvider
{
  /**
   * Viewer control.
   */
  private ServerTreeViewer viewer;
  
  private Server server;

  protected void initialize()
  {
    root = new TreeParent("", null);
  }

  public void dispose()
  {
  }

  public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
  {
    this.viewer = (ServerTreeViewer) viewer;
    if(newInput != null) {
      root = new TreeParent("", null);
      Application[] apps = (Application[]) newInput;
      for(int i = 0; i < apps.length; ++i) {
        addApplication(apps[i]);
      }
    }
  }
  
  private void addApplication(Application application)
  {
    TreeParent tp = getParentNode(application, true);
    STVApplicationNode appNode = new STVApplicationNode(application, tp);
    viewer.refresh(tp, true);
  }
  
  /**
   * Gets the parent node for the application. If the parent node doesn't exist, creates
   * a new one.
   *
   * @param application is the application for which is the node returned/created.
   * @param createParent if true then creates parent node if it doesn't exist
   * @return TreeParent object with the appropriate parent node 
   */
  private TreeParent getParentNode(Application application, boolean createParent)
  {
    if(application.getGroup() == null || application.getGroup().length() == 0) {
      return root;
    }
    else {
      TreeObject[] objs = root.getChildren();
      for(int i = 0; i < objs.length; ++i) {
        Object o = objs[i];
        if(o instanceof TreeParent) {
          TreeParent tp = (TreeParent) o;
          if(application.getGroup().equalsIgnoreCase(tp.getName()))
            return tp;
        }
      }
      
      if(createParent) {
        TreeParent tp = new TreeParent(application.getGroup(), root);
        return tp;
      }
    }

    return null;
  }
  
  private void removeApplication(Application application)
  {
    TreeParent tp = getParentNode(application, false);
    if(tp == null)
      tp = root;
    Iterator it = tp.children.iterator();
    while(it.hasNext()) {
      Object o = it.next();
      if(o instanceof STVApplicationNode) {
        STVApplicationNode appNode = (STVApplicationNode) o;
        Application localApp = appNode.getApplication();
        if(localApp.equals(application)) {
          it.remove();
          if(tp.children.size() == 0) {
            TreeParent parent = tp.getParent();
            parent.removeChild(tp);
            viewer.refresh(parent, true);
          }
          else {
            viewer.refresh(tp, true);
          }
          // no return - for the complete cleanup
        }
      }
    }
  }

  /**
   * Clears the content of the tree
   */
  public void clear()
  {
    if(root != null) {
      root.children.clear();
      viewer.refresh();
    }
  }
} // END OF CLASS SERVERTREEVIEWCONTROLLER
