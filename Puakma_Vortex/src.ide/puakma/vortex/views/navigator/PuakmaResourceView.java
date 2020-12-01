/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Some nice day in 2005      
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

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;

import puakma.coreide.ConfigurationManager;
import puakma.coreide.ConnectionPrefs;
import puakma.vortex.RecentApplicationsListener;
import puakma.vortex.VortexPlugin;
import puakma.vortex.WorkbenchUtils;
import puakma.vortex.actions.pmaApp.ConnectToApplicationAction;
import puakma.vortex.preferences.PreferenceConstants;
import puakma.vortex.views.navigator.actions.PNBaseAction;
import puakma.vortex.views.navigator.actions.PNClearRecentAppsAction;
import puakma.vortex.views.navigator.actions.PNCopyCutAction;
import puakma.vortex.views.navigator.actions.PNFavoriteApplicationAction;
import puakma.vortex.views.navigator.actions.PNManageFavoritesAction;
import puakma.vortex.views.navigator.actions.PNNewApplicationAction;
import puakma.vortex.views.navigator.actions.PNOpenItemAction;
import puakma.vortex.views.navigator.actions.PNPasteAction;
import puakma.vortex.views.navigator.actions.PNRemoveAction;
import puakma.vortex.views.navigator.actions.PNRenameAction;

public class PuakmaResourceView extends ViewPart
{
	/**
	 * This is really dump, empty action... [-;
	 *
	 * @author Martin Novak
	 */
	public class DumbAction extends Action
	{

	}

	public static final String VIEW_ID = "puakma.vortex.views.PuakmaResourceView";

	private PNPasteAction pasteAction;

	public final PNBaseAction[] globalActions = {
			new PNCopyCutAction(this, true),
			new PNCopyCutAction(this, false),
			(pasteAction = new PNPasteAction(this)),
			new PNRemoveAction(this),
			new PNRenameAction(this),
	};

	public static final String [] GLOBAL_ACTIONS = {
		//    ActionFactory.UNDO.getId(),
		//    ActionFactory.REDO.getId(),
		ActionFactory.CUT.getId(),
		ActionFactory.COPY.getId(),
		ActionFactory.PASTE.getId(),
		//    ActionFactory.PRINT.getId(),
		ActionFactory.DELETE.getId(),
		//    ActionFactory.FIND.getId(),
		//    ActionFactory.SELECT_ALL.getId(),
		//    IDEActionFactory.BOOKMARK.getId()
		ActionFactory.RENAME.getId(),
	};

	private ApplicationTreeViewer viewer;

	private PNOpenItemAction openAction;

	private ConnectToApplicationAction connectToAppAction;

	private Text queryEdit;

	private LayoutActionGroup layoutAction;

	private PNNewApplicationAction newAppAction;

	/**
	 * This is a callback that will allow us to create the viewer and initialize it.
	 *
	 * @param parent is the parent control
	 */
	public void createPartControl(Composite parent)
	{
		GridLayout gl = new GridLayout(2,false);
		gl.marginHeight = gl.marginWidth = 0;
		parent.setLayout(gl);

		Label l = new Label(parent, SWT.NULL);
		l.setText("Filter:");
		queryEdit = new Text(parent,SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
		queryEdit.setLayoutData(gd);
		queryEdit.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e)
			{
				String filter = queryEdit.getText();
				viewer.filterText(filter);
			}
		});

		viewer = new ApplicationTreeViewer(parent);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		viewer.getControl().setLayoutData(gd);

		makeActions();
		hookContextMenu();
		contributeToActionBars();

		viewer.enableDefaultDoubleClick(false);
		getSite().setSelectionProvider(viewer);

		//    internalDblClckListener = new InternalDoubleClickListener();
		//    viewer.addDoubleClickListener(internalDblClckListener);
	}

	private void hookContextMenu()
	{
		MenuManager menuMgr = new MenuManager("PuakmaResourceViewPopup");
		menuMgr.setRemoveAllWhenShown(true);
		
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager)
			{
				ContextMenuHandler.handlerMenu(manager, PuakmaResourceView.this);
			}
		});
		
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
	}

	private void contributeToActionBars()
	{
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());

		for(int i = 0; i < globalActions.length; ++i) {
			PNBaseAction action = globalActions[i];
			action.init();
			bars.setGlobalActionHandler(action.getId(), action);
		}

		viewer.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event)
			{
				for(int i = 0; i < globalActions.length; ++i) {
					PNBaseAction action = globalActions[i];
					if(action.handleKeyEvent(event))
						return;
				}
			}
		});
	}

	public void dispose()
	{
		super.dispose();

		// REMOVE ALL THE LISTENERS
		for(int i = 0; i < globalActions.length; ++i)
			globalActions[i].dispose();
	}

	/**
	 * Fills the view toolbar pull down menu.
	 * @param manager is the manager for the local pull down menu
	 */
	private void fillLocalPullDown(IMenuManager manager)
	{
		newAppAction = new PNNewApplicationAction(this);
		connectToAppAction = new ConnectToApplicationAction();
		layoutAction = new LayoutActionGroup(this);

		// ADD SOME DUMP ACTION TO HAVE MENU VISIBLE
		manager.add(newAppAction);
		manager.add(connectToAppAction);
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager)
			{
				displayPullDownMenu(manager);
			}
		});
	}

	/**
	 * Fills the local pull down menu. Because the menu is being filled
	 */
	private void displayPullDownMenu(IMenuManager manager)
	{
		manager.removeAll();
		manager.add(newAppAction);
		manager.add(connectToAppAction);
		// FAVORITES GROUP
		addFavoritesApplicationToMenu(manager);
		// NOW ADD RECENT APPLICATIONS
		addRecentApplicationsMenu(manager);
		// FLAT/HIERARCHY LAYOUT MENU
		layoutAction.contributeToViewMenu(manager);
	}

	/**
	 * Adds favorite applications management to the pull down menu
	 * @param menuManager is the menu to add stuff to
	 */
	private void addFavoritesApplicationToMenu(IMenuManager menuManager)
	{
		menuManager.add(new Separator());
		ConnectionPrefs[] prefs = VortexPlugin.getDefault().getFavoritesAppsManager().listConnectionPrefs();
		Arrays.sort(prefs, new Comparator<ConnectionPrefs>() {
			public int compare(ConnectionPrefs p1, ConnectionPrefs p2) {
				String s1 = p1.getName(); //ConnectionPrefsImpl.getFullApplicationUrl(p1);
				String s2 = p2.getName(); //ConnectionPrefsImpl.getFullApplicationUrl(p2);
				return s1.compareToIgnoreCase(s2);
			}
		});
		for(int i = 0; i < prefs.length; ++i) {
			PNFavoriteApplicationAction act = new PNFavoriteApplicationAction(prefs[i],
					PNFavoriteApplicationAction.SHOW_NAME);
			menuManager.add(act);
		}
		menuManager.add(new Separator());
		menuManager.add(new PNManageFavoritesAction());
	}

	/**
	 * This function adds a recent applications submenu
	 * @param menuManager is the menu into which we should add submenu
	 */
	private void addRecentApplicationsMenu(IMenuManager menuManager)
	{
		menuManager.add(new Separator());
		IMenuManager subMenu = new MenuManager("Recent Applications");
		menuManager.add(subMenu);

		ConfigurationManager manager = VortexPlugin.getDefault().getRecentAppsManager();
		ConnectionPrefs[] recentApps = manager.listConnectionPrefs();
		if(recentApps.length == 0) {
			IAction action = new DumbAction();
			action.setText("Empty");
			action.setEnabled(false);
			subMenu.add(action);
		}
		else {
			Arrays.sort(recentApps, new Comparator<ConnectionPrefs>() {
				public int compare(ConnectionPrefs p1, ConnectionPrefs p2) {
					String n1 = p1.getName().substring(RecentApplicationsListener.RECENT.length());
					String n2 = p2.getName().substring(RecentApplicationsListener.RECENT.length());
					// TODO: add some chekcing here
					try {
						int i1 = Integer.parseInt(n1);
						int i2 = Integer.parseInt(n2);
						return i2 - i1;
					}
					catch(Exception e) {
						return 0;
					}
				}
			});
			for(int i = 0; i < recentApps.length; ++i) {
				subMenu.add(new PNFavoriteApplicationAction(recentApps[i], PNFavoriteApplicationAction.SHOW_PATH));
			}
		}

		subMenu.add(new Separator());
		subMenu.add(new PNClearRecentAppsAction());
	}

	/**
	 * This function sets up using flat layout in the tornado navigator instance. Also saves
	 * the state to the settings.
	 *
	 * @param useFlat if true we will use flat layout in the tornado navigator view.
	 */
	public void setFlatLayout(boolean useFlat)
	{
		IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
		store.setValue(PreferenceConstants.PREF_NAVIGATOR_USE_FLAT_PACKAGES, useFlat);

		viewer.setFlat(useFlat);

		viewer.getControl().setRedraw(false);
		viewer.refresh();
		viewer.getControl().setRedraw(true);
	}

	public PNPasteAction getPasteAction()
	{
		return pasteAction;
	}

	/**
	 * Fills the toolbar on the top of the view
	 *
	 * @param manager
	 */
	private void fillLocalToolBar(IToolBarManager manager)
	{
		manager.add(newAppAction);
		manager.add(connectToAppAction);
		manager.add(new Separator());
		if(VortexPlugin.DEBUG_MODE) {
			manager.add(new Action("Open Database Editor") {
				public void run()
				{
					WorkbenchUtils.openDatabaseEditor();
				}
			});
		}
	}

	private void makeActions()
	{
		openAction = new PNOpenItemAction(this);
		connectToAppAction = new ConnectToApplicationAction();

		viewer.addDoubleClickListener(openAction);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus()
	{
		viewer.getControl().setFocus();
	}

	/**
	 * Returns the currently selected items in the tree.
	 *
	 * @return the currently selected items in the tree
	 */
	public IStructuredSelection getSelection()
	{
		return (IStructuredSelection) viewer.getSelection();
	}

	/**
	 * @return ApplicationTreeViewController content provider
	 */
	public ApplicationTreeViewController getContentProvider()
	{
		return (ApplicationTreeViewController) viewer.getContentProvider();
	}

	public ApplicationTreeViewer getViewer()
	{
		return viewer;
	}

	public Object getAdapter(Class adapter)
	{
		if(adapter.equals(ISelectionProvider.class))
			return viewer;

		return super.getAdapter(adapter);
	}
}
