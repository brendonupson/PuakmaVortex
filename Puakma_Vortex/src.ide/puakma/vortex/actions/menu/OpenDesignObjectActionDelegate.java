/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Aug 11, 2005
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

import puakma.vortex.dialogs.DesignObjectLoooookupDialog;

public class OpenDesignObjectActionDelegate implements IWorkbenchWindowActionDelegate
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
    DesignObjectLoooookupDialog dlg = new DesignObjectLoooookupDialog(window.getShell());
    dlg.open();
  }

  public void selectionChanged(IAction action, ISelection selection)
  {
  }

}
