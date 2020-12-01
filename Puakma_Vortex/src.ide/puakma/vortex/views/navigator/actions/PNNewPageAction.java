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

import java.util.Iterator;

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
import puakma.vortex.wizard.NewPageWizard;

public class PNNewPageAction extends PNBaseAction
{

	public PNNewPageAction(PuakmaResourceView view)
	{
		super("", view);
		setText("New Page");
		setToolTipText("Creates New Page");
		setImageDescriptor(VortexPlugin.getImageDescriptor("text.gif"));
	}

	public void run()
	{
		Application application = ContextMenuHandler.getApplicationFromSelection(getView().getSelection());
		NewPageWizard w = new NewPageWizard();
		w.init(application);
		WizardDialog dlg = new WizardDialog(getShell(), w);
		dlg.open();
	}

	public boolean handleKeyEvent(KeyEvent event)
	{
		return false;
	}

	public boolean qualifyForSelection()
	{
		IStructuredSelection selection = getView().getSelection();
		Application application = ContextMenuHandler.getApplicationFromSelection(selection);
		if(application == null)
			return false;

		Iterator it = selection.iterator();
		while(it.hasNext()) {
			Object o = it.next();
			if(o instanceof ATVParentNode && ((ATVParentNode)o).getNodeType() == ApplicationTreeViewController.NODE_PAGES)
				return true;
			else if(o instanceof ATVApplicationNode)
				return true;
			else if(o instanceof DesignObject) {
				DesignObject obj = (DesignObject) o;
				int type = obj.getDesignType();
				return type == DesignObject.TYPE_PAGE;
			}
		}

		return false;
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection)
	{
	}

}
