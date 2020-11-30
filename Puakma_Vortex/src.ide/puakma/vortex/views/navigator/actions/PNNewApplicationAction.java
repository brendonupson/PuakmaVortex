/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Aug 10, 2005
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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.ui.IWorkbenchPart;

import puakma.vortex.VortexPlugin;
import puakma.vortex.views.navigator.PuakmaResourceView;
import puakma.vortex.wizard.NewApplicationWizard;

public class PNNewApplicationAction extends PNBaseAction
{

	public PNNewApplicationAction(PuakmaResourceView view)
	{
		super("", view);

		setText("New Application");
		setImageDescriptor(VortexPlugin.getImageDescriptor("newprj.gif"));
		setToolTipText("Create New Application");
	}

	public void run()
	{
		NewApplicationWizard wizard = new NewApplicationWizard();
		WizardDialog dlg = new WizardDialog(getShell(), wizard);
		dlg.open();
	}

	public boolean handleKeyEvent(KeyEvent event)
	{
		return false;
	}

	public boolean qualifyForSelection()
	{
		IStructuredSelection selection = getView().getSelection();
		if(selection.size() == 0)
			return true;
		return false;
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection)
	{
	}
}
