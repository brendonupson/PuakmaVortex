/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Nov 1, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.views.navigator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;

import puakma.vortex.VortexPlugin;
import puakma.vortex.preferences.PreferenceConstants;

public class LayoutActionGroup extends MultiActionGroup
{
	public LayoutActionGroup(PuakmaResourceView view)
	{
		super(new IAction[] { new LayoutAction(view, true), new LayoutAction(view, false), }, 0);

		IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
		boolean useFlatPackages = store.getBoolean(PreferenceConstants.PREF_NAVIGATOR_USE_FLAT_PACKAGES);
		updateCurrentSelection(useFlatPackages ? 0 : 1);
	}

	public void fillActionBars(IActionBars actionBars)
	{
		super.fillActionBars(actionBars);
		contributeToViewMenu(actionBars.getMenuManager());
	}

	public void contributeToViewMenu(IMenuManager viewMenu)
	{
		viewMenu.add(new Separator());

		// Create layout sub menu
		IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
		boolean useFlatPackages = store.getBoolean(PreferenceConstants.PREF_NAVIGATOR_USE_FLAT_PACKAGES);
		updateCurrentSelection(useFlatPackages ? 0 : 1);

		IMenuManager layoutSubMenu = new MenuManager("Class Browsing Layout");
		final String layoutGroupName = "layout"; //$NON-NLS-1$
		Separator marker = new Separator(layoutGroupName);

		viewMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		viewMenu.add(marker);
		viewMenu.appendToGroup(layoutGroupName, layoutSubMenu);
		viewMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS + "-end"));
		addActions(layoutSubMenu);
	}
}

class LayoutAction extends Action
{
	private PuakmaResourceView view;

	private boolean flat;

	public LayoutAction(PuakmaResourceView view, boolean flat)
	{
		super();

		this.view = view;
		this.flat = flat;

		if(flat) {
			setText("Flat");
			setImageDescriptor(VortexPlugin.getImageDescriptor("flatLayout.gif"));
		}
		else {
			setText("Hierarchical");
			setImageDescriptor(VortexPlugin.getImageDescriptor("hierarchicalLayout.gif"));
		}
	}

	public void run()
	{
		view.setFlatLayout(flat);
	}
}
