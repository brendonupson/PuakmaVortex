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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;

import puakma.coreide.objects2.Application;

/**
 * @author Martin Novak
 */
public class ServerTreeViewer extends BaseTreeViewer
{
  ServerTreeViewController controler;

  public ServerTreeViewer(Composite parent)
  {
    super(parent);

    controler = new ServerTreeViewController();
    setContentProvider(controler);
    setLabelProvider(new ServerTreeLabelProvider());
    setSorter(new ServerTreeNameSorter());
  }

  /**
   * Gets the description of the currently selected item. The description can have
   * only item which represents application.
   * 
   * @return String with description, never null
   */
  public String getCurrentDescription()
  {
    STVApplicationNode node = getSelectedAppNode();
    Application app = node.getApplication();
    if(app != null && app.getDescription() != null)
      return app.getDescription();

    return "";
  }
  
  /**
   * Returns selected application node.
   *
   * @return selcted application node or null if no app node is selected
   */
  private STVApplicationNode getSelectedAppNode()
  {
    IStructuredSelection selection = (IStructuredSelection) getSelection();
    Object elem = selection.getFirstElement();
    if(elem instanceof STVApplicationNode)
      return (STVApplicationNode) elem;
    
    return null;
  }

  /**
   * Returns the name of the currently selected application.
   *
   * @return name of the application or null if nothing selected
   */
  public String getCurrentApplicationName()
  {
    STVApplicationNode node = getSelectedAppNode();
    if(node != null)
      return node.getName();
    return null;
  }

  /**
   * Returns group name of the currently selected application.
   *
   * @return String or null if there is no application selected or application doesn't
   *         belong to any group
   */
  public String getCurrentGroupName()
  {
    STVApplicationNode node = getSelectedAppNode();
    if(node != null)
      return node.getApplication().getGroup();

    return null;
  }
  
  /**
   * Returns currently selected application
   * @return selected application or null if nothing is selected
   */
  public Application getCurrentApplication()
  {
    STVApplicationNode node = getSelectedAppNode();
    if(node != null)
      return node.getApplication();
    
    return null;
  }

  /**
   * Clears the content of the tree
   */
  public void clear()
  {
    controler.clear();
  }
}
