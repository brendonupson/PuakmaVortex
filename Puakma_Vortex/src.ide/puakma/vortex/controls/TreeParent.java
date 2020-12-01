/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Dec 10, 2004
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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents all parent nodes in the tree. Note that this is just bean for
 * storing structure, and thus it doesn't update the viewer.
 * 
 * @author Martin Novak
 */
public class TreeParent extends TreeObject
{
  /**
   * List of all child elements
   */
  public List children = new ArrayList();

  public TreeParent(String name, TreeParent parent)
  {
    super(name, parent);
  }

  /**
   * Adds the child {@link TreeObject}. Note that this is not public API, and cannot be used outside
   */
  protected void addChild(TreeObject child)
  {
    children.add(child);
    child.setParent(this);
  }

  public void removeChild(TreeObject child)
  {
    children.remove(child);
    child.setParent(null);
  }

  /**
   * Lists all children {@link TreeObject}s
   */
  public TreeObject[] getChildren()
  {
    return (TreeObject[]) children.toArray(new TreeObject[children.size()]);
  }

  /**
   * @return true if this tree item some sub items
   */
  public boolean hasChildren()
  {
    return children.size() > 0;
  }
}
