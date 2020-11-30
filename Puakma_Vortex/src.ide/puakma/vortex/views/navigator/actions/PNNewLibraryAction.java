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

import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.DesignObject;
import puakma.vortex.VortexPlugin;
import puakma.vortex.views.navigator.ATVApplicationNode;
import puakma.vortex.views.navigator.ATVParentNode;
import puakma.vortex.views.navigator.ApplicationTreeViewController;
import puakma.vortex.views.navigator.ContextMenuHandler;
import puakma.vortex.views.navigator.PuakmaResourceView;
import puakma.vortex.wizard.NewLibraryWizard;

public class PNNewLibraryAction extends PNBaseAction
{
	public PNNewLibraryAction(PuakmaResourceView view)
	{
		super("", view);

		setText("New Jar Library");
		setToolTipText("Uploads New Jar Library");
		setImageDescriptor(VortexPlugin.getImageDescriptor("jar_file.gif"));
	}

	public void run()
	{
		Application application = ContextMenuHandler.getApplicationFromSelection(getView().getSelection());
		NewLibraryWizard wizard = new NewLibraryWizard(application);
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
		if(selection.size() != 1)
			return false;
		Application application = ContextMenuHandler.getApplicationFromSelection(selection);
		if(application == null)
			return false;

		Object o = selection.getFirstElement();
		if(o instanceof ATVParentNode && ((ATVParentNode)o).getNodeType() == ApplicationTreeViewController.NODE_SHARED_CODE)
			return true;
		else if(o instanceof ATVApplicationNode)
			return true;
		else if(o instanceof DesignObject) {
			DesignObject obj = (DesignObject) o;
			int type = obj.getDesignType();
			return type == DesignObject.TYPE_JAR_LIBRARY || type == DesignObject.TYPE_LIBRARY;
		}

		return false;
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection)
	{
	}
}
