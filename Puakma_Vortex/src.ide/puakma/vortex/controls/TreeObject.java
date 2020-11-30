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

/**
 * Root object for all tree elements.
 *
 * @author Martin Novak
 */
public class TreeObject
{
  /**
   * Name of the node - this will be displayed in the tree.
   */
  private String name;
  
  /**
   * Parent node.
   */
  private TreeParent parent;
  
  private Object data;
  
  public TreeObject(String name, TreeParent parent)
  {
    this.name = name;
    this.parent = parent;
    if(parent != null)
      parent.addChild(this);
  }

  /**
   * Sets a new parent to the object.
   * 
   * @param parent is the new parent object
   */
  public void setParent(TreeParent parent)
  {
    this.parent = parent;
  }
  
  public TreeParent getParent()
  {
    return parent;
  }

  public void setName(String name)
  {
    this.name = name;
  }
  
  public String getName()
  {
    return name;
  }
  

  public String toString()
  {
    return getName();
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
}
