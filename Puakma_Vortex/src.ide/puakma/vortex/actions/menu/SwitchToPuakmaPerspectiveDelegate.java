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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.WorkbenchException;

import puakma.vortex.VortexPlugin;
import puakma.vortex.actions.BaseWorkbenchDelegate;
import puakma.vortex.rcp.PuakmaDeveloperPerspective;


/**
 * This delegate switches to the puakma resource navigator. This action delegate however
 * is not in the menu right now. So keep it for future as an example how to switch
 * to the appropriate perspective.
 *
 * @author Martin Novak
 */
public class SwitchToPuakmaPerspectiveDelegate extends BaseWorkbenchDelegate
{
	public void run(IAction action)
	{
		try {
			window.getWorkbench().showPerspective(PuakmaDeveloperPerspective.PERSPECTIVE_ID, window);
		}
		catch(WorkbenchException e) {
			VortexPlugin.log(e);
			MessageDialog.openError(window.getShell(),"Cannot open perspective",
					"Cannot open Puakma Developer Perspective.\nReason:\n" + e.getLocalizedMessage());
		}
	}

}
