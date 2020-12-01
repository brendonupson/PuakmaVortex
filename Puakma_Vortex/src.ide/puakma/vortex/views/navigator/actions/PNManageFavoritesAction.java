/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Nov 26, 2005
 * 
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.views.navigator.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import puakma.vortex.dialogs.ConnectionManagerDialog;

public class PNManageFavoritesAction extends Action implements IAction
{
  public PNManageFavoritesAction()
  {
    super();
    setText("Manage Favorites");
  }

  public void run()
  {
    Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
    ConnectionManagerDialog dlg = new ConnectionManagerDialog(shell, ConnectionManagerDialog.TYPE_FAVORITES);
    dlg.open();
  }
}
