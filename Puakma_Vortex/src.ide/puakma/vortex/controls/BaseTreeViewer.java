/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jan 5, 2005
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

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;


/**
 * @author Martin Novak
 */
public abstract class BaseTreeViewer extends TreeViewer
{
  /**
   * Default double click action which opens all tree parent items on double click.
   */
  DefaultDoubleClickAction doubleClickAction;

  public BaseTreeViewer(Composite parent)
  {
    super(parent);
    
    enableDefaultDoubleClick(true);
  }

  /**
   * Enables default tree behaviour which opens parent tree item on double click.
   *
   * @param enable if true, then enables behaviour else disables it.
   */
  public void enableDefaultDoubleClick(boolean enable)
  {
    if(enable == true && doubleClickAction == null) {
      doubleClickAction = new DefaultDoubleClickAction(this);
      addDoubleClickListener(doubleClickAction);
    }
    else if(enable == false && doubleClickAction != null) {
      removeDoubleClickListener(doubleClickAction);
      doubleClickAction = null;
    }
  }
}
