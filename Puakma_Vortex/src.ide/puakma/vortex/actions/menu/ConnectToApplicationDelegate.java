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
package puakma.vortex.actions.menu;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import puakma.vortex.actions.pmaApp.ConnectToApplicationAction;


/**
 * @author Martin Novak
 */
public class ConnectToApplicationDelegate implements IWorkbenchWindowActionDelegate
{
  IWorkbenchWindow window;
  
  public void dispose()
  {
  }

  public void init(IWorkbenchWindow window)
  {
    this.window = window;
  }

  public void run(IAction action)
  {
    ConnectToApplicationAction actionA = new ConnectToApplicationAction();
    actionA.run(window);
  }

  public void selectionChanged(IAction action, ISelection selection)
  {
  }
}
