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
package puakma.vortex.swt;

import java.util.ArrayList;
import java.util.List;

public class TreeObject
{
  private List children = new ArrayList();
  private Object data;
  private TreeObject parent;
  private String name;
  
  public TreeObject(String name, TreeObject parent)
  {
    this.name = name;
    this.parent = parent;
    
    if(parent != null)
      parent.addChild(this);
  }

  private void addChild(TreeObject object)
  {
    children.add(object);
  }
  
  /**
   * Sets the custom data to the tree object.
   */
  public void setData(Object data)
  {
    this.data = data;
  }

  /**
   * Gets the custom data from the tree object.
   */
  public Object getData()
  {
    return data;
  }
  
  /**
   * Sets the new name to the tree object.
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * Gets the name from the tree object.
   */
  public String getName()
  {
    return name;
  }
  
  public TreeObject getParent()
  {
    return parent;
  }

  public String toString()
  {
    return getName();
  }
  
  public void removeChild(TreeObject child)
  {
    children.remove(child);
    child.parent = null;
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
