/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Mar 22, 2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.swt;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import puakma.vortex.VortexPlugin;

public class ResizableDialog extends Dialog
{
	/**
	 * Context id for help
	 */
	private String contextId;
	/**
	 * Dialog identifier for saving position and size.
	 */
	private String dialogId;

	/**
	 * Constructor for resizable dialog.
	 * 
	 * @param parent is the parent shell
	 * @param id is identifier for saving size, and position
	 */
	public ResizableDialog(Shell parent, String id)
	{
		super(parent);

		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		this.dialogId = id;
	}

	public void setHelpContextId(String contextId)
	{
		this.contextId = contextId;
	}

	/*
	 * @see org.eclipse.jface.window.Window#configureShell(Shell)
	 */
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);
		if(contextId != null)
			PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell, contextId);
	}

	protected IDialogSettings getDialogBoundsSettings()
	{
		IDialogSettings settings = VortexPlugin.getDefault().getDialogSettings();
		if(settings.getSection(dialogId) == null)
			settings.addNewSection(dialogId);
		return settings.getSection(dialogId);
	}
}
