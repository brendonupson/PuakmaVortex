/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jan 6, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;


/**
 * @author Martin Novak
 */
public abstract class BaseWorkbenchDelegate implements IWorkbenchWindowActionDelegate
{
  protected IWorkbenchWindow window;

  public void dispose()
  {
  }

  public void init(IWorkbenchWindow window)
  {
    this.window = window;
  }

  public void selectionChanged(IAction action, ISelection selection)
  {
  }
}
