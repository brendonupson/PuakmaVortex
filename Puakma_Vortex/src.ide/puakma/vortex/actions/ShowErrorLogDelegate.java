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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PartInitException;

import puakma.vortex.VortexPlugin;


/**
 * @author Martin Novak
 */
public class ShowErrorLogDelegate extends BaseWorkbenchDelegate
{
  public void run(IAction action)
  {
    try {
      window.getActivePage().showView("org.eclipse.pde.runtime.LogView");
    }
    catch(PartInitException e) {
      VortexPlugin.log(e);
      MessageDialog.openError(window.getShell(),"Cannot create view",
          "Cannot create Error Log View.\nReason:\n" + e.getLocalizedMessage());
    }
  }
}
