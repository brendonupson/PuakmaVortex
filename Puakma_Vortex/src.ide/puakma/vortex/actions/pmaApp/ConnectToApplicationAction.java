/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Dec 10, 2004
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.actions.pmaApp;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

import puakma.vortex.VortexPlugin;
import puakma.vortex.actions.BaseWorkbenchAction;
import puakma.vortex.dialogs.server.AppConnectionRunnable;
import puakma.vortex.dialogs.server.AppSelectionDialog;

/**
 * This action show connection dialog to the applicatoin.
 *
 * @author Martin Novak
 */
public class ConnectToApplicationAction extends BaseWorkbenchAction
{
	public ConnectToApplicationAction()
	{
		setText("Open Application");
		setToolTipText("Opens Tornado Application");
		setImageDescriptor(VortexPlugin.getImageDescriptor("openApplication.gif"));
	}

	public void run(final IWorkbenchWindow window)
	{
		Shell shell = window.getShell();
		//ConnectToServerWizard wizard = new ConnectToServerWizard();
		//AppSelectionWizard wizard = new AppSelectionWizard(new AppConnectionRunnable());
		//WizardDialog dlg = new WizardDialog(shell, wizard);
		AppSelectionDialog dlg = new AppSelectionDialog(shell, new AppConnectionRunnable());
		dlg.open();
	}
}
