/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jan 31, 2005
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

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

import puakma.vortex.dialogs.server.AppSelectionDialog;
import puakma.vortex.dialogs.server.ExportApplicationRunnable;


/**
 * @author Martin Novak
 */
public class DownloadPmxCommand extends BaseWorkbenchCommand
{
  public void run(IWorkbenchWindow window)
  {
    //ExportApplicationWizard wizard = new ExportApplicationWizard();
    //WizardDialog dlg = new WizardDialog(window.getShell(), wizard);
    Shell shell = window.getShell();
    AppSelectionDialog dlg = new AppSelectionDialog(shell, new ExportApplicationRunnable());
    dlg.open();
  }
}
