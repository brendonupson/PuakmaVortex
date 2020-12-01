/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    18/07/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.dialogs.server;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import puakma.coreide.objects2.Application;
import puakma.vortex.swt.TreeObject;

/**
 * Content provider which provides the list of applications on the server. The
 * applications are represented as {@link TreeObject} and groups as
 * {@link TreeObject} without custom data. Input is array of {@link Application} objects.
 * 
 * @author Martin Novak
 */
public class AppsContentProvider implements IContentProvider, IStructuredContentProvider,
                                            ITreeContentProvider
{
  private TreeObject root;

  public void dispose()
  {
  }

  public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
  {
    if(newInput != null) {
      // BUILD THE TREE FOR THE MODEL
      Application[] apps = (Application[]) newInput;
      root = new TreeObject("", null);
      Map<String, TreeObject> m = new HashMap<String, TreeObject>();
      for(int i = 0; i < apps.length; ++i) {
        Application app = apps[i];
        TreeObject to;
        
        String group = app.getGroup();
        if(group.length() == 0) {
          to = new TreeObject(app.getName(), root);
        }
        else {
          TreeObject p = m.get(group);
          // IF THERE IS NO PARENT OBJECT, CREATE A NEW ONE
          if(p == null) {
            p = new TreeObject(group, root);
            m.put(group, p);
          }
          
          // CREATE ITEM
          to = new TreeObject(app.getName(), p);
        }
        
        to.setData(app);
      }
    }
    else
      root = new TreeObject("", null);
  }

  public Object[] getElements(Object inputElement)
  {
    return root.getChildren();
  }

  public Object[] getChildren(Object parentElement)
  {
    return ((TreeObject) parentElement).getChildren();
  }

  public Object getParent(Object element)
  {
    return ((TreeObject) element).getParent();
  }

  public boolean hasChildren(Object element)
  {
    return ((TreeObject) element).hasChildren();
  }
}
