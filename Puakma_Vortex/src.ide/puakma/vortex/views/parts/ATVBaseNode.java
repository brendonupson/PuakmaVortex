/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Mar 2, 2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.views.parts;

import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import puakma.utils.lang.StringUtil;

public class ATVBaseNode
{
  private TreeViewer viewer;
  private ATVBaseNode parent;
  
  public ATVBaseNode(ATVBaseNode parent, TreeViewer viewer)
  {
    this.viewer = viewer;
    this.parent = parent;
  }
  
  /**
   * Lists all children from this node.
   * @return {@link List} with all children objects
   */
  public Object[] getChildren()
  {
    return null;
  }
  
  /**
   * @return {@link Image} object for the current node
   */
  public Image getIcon()
  {
    return null;
  }
  
  /**
   * This function should get model object.
   * @return model object associated with this node
   */
  public Object getModel()
  {
    return null;
  }
  
  public void refresh()
  {
    viewer.refresh(this);
  }
  
  public void refresh(boolean updateLabels)
  {
    viewer.refresh(updateLabels);
  }
  
  public void remove()
  {
    viewer.remove(this);
  }
  
  public String getText()
  {
    return StringUtil.EMPTY_STRING;
  }
  
  public TreeViewer getViewer()
  {
    return viewer;
  }

  public ATVBaseNode getParent()
  {
    return parent;
  }
}
