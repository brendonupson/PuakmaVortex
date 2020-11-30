/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:      
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

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;

public class DefaultDoubleClickAction implements IDoubleClickListener
{
  private TreeViewer viewer;

  DefaultDoubleClickAction(TreeViewer viewer)
  {
    this.viewer = viewer;
  }

  public void doubleClick(DoubleClickEvent event)
  {
    ISelection selection = viewer.getSelection();
    Object obj = ((IStructuredSelection) selection).getFirstElement();

    if(obj instanceof TreeParent) {
      if(viewer.getExpandedState(obj) == true)
        viewer.collapseToLevel(obj,1);
      else
        viewer.expandToLevel(obj, 1);
    }
  }
}
