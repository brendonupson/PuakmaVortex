/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Dec 21, 2004
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

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ViewerSorter;


/**
 * @author Martin Novak
 */
public abstract class BaseTreeViewController extends ViewerSorter
                                             implements IStructuredContentProvider,
                                                        ITreeContentProvider
{
  /**
   * Root of all nodes. Can be either ApplicationRoot or ServerRoot or AllServersRoot
   */
  protected TreeParent root;
  
  public Object[] getElements(Object inputElement)
  {
    if(root == null)
      initialize();
    return getChildren(root);
  }
  
  /**
   * @return root object in the tree
   */
  public TreeParent getRoot()
  {
    return root;
  }
  
  /**
   * Returns all child elements.
   *
   * @param parentElement
   * @return the array of child elements or null if the request was targeted
   * for bad object (without children)
   */
  public Object[] getChildren(Object parentElement)
  {
    if(parentElement instanceof TreeParent) {
      TreeParent parent = (TreeParent)parentElement;
      return parent.children.toArray();
    }
    return null;
  }

  /**
   * Returns the parent node of element.
   *
   * @param element is the element which is requested for parent
   * @return parent object or null if element doesn't have any parent element
   */
  public Object getParent(Object element)
  {
    if(element instanceof TreeObject) {
      TreeObject obj = (TreeObject) element;
      return obj.getParent();
    }
    return null;
  }

  /**
   * Returns true if the element can be expanded.
   * 
   * @param element is the tree object which is asked to check expand availability
   * @return true if the element can be expanded
   */
  public boolean hasChildren(Object element)
  {
    if(element instanceof TreeParent) {
      TreeParent parent = (TreeParent) element;
      return (parent.children.size() > 0) ? true : false;
    }
    return false;
  }

  /**
   * Abstract function which should initialize the content of the viewer
   */
  protected abstract void initialize();
}
