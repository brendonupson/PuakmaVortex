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
package puakma.vortex.views.navigator;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;

import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.ApplicationObject;
import puakma.coreide.objects2.DatabaseObject;
import puakma.coreide.objects2.DesignObject;
import puakma.vortex.VortexPlugin;
import puakma.vortex.actions.pmaApp.ConnectToApplicationAction;
import puakma.vortex.project.ProjectUtils;
import puakma.vortex.project.PuakmaProject2;
import puakma.vortex.views.navigator.actions.DisconnectApplication;
import puakma.vortex.views.navigator.actions.PNAddToFavoritesAction;
import puakma.vortex.views.navigator.actions.PNBaseAction;
import puakma.vortex.views.navigator.actions.PNCopyCutAction;
import puakma.vortex.views.navigator.actions.PNNewApplicationAction;
import puakma.vortex.views.navigator.actions.PNNewDbConnectionAction;
import puakma.vortex.views.navigator.actions.PNNewJavaObjectAction;
import puakma.vortex.views.navigator.actions.PNNewLibraryAction;
import puakma.vortex.views.navigator.actions.PNNewPageAction;
import puakma.vortex.views.navigator.actions.PNNewResourceAction;
import puakma.vortex.views.navigator.actions.PNOpenDbSchemaEditor;
import puakma.vortex.views.navigator.actions.PNPasteAction;
import puakma.vortex.views.navigator.actions.PNQueryEditorAction;
import puakma.vortex.views.navigator.actions.PNRefreshAction;
import puakma.vortex.views.navigator.actions.PNRemoveAction;
import puakma.vortex.views.navigator.actions.PNRenameAction;
import puakma.vortex.views.navigator.actions.PNScheduleActionAction;
import puakma.vortex.wizard.NewResourceWizard;
import puakma.vortex.wizard.java.JavaObjectWizard;

public class ContextMenuHandler
{
	private static final String NEW = "NEW";
	private static final String OPEN = "OPEN";
	private static final String EDIT = "EDIT";
	private static final String EXTENDED = "EXTENDED";
	private static final String PROPERTIES = "PROPERTIES";
	private static final String CUSTOM = "CUSTOM";
	private static final String INFO = "INFO";

	/**
	 * This class is used for displaying text information in the context menu.
	 *
	 * @author Martin Novak
	 */
	private static class InfoAction extends Action {
		public InfoAction(String text) {
			super(text);
			setEnabled(false);
		}
	}

	/**
	 * Adds all necessary menu items to the context menu in the puakma resource view.
	 *
	 * @param manager is the menu manager for the context menu
	 * @param view is the associated puakma resource view
	 */
	public static void handlerMenu(IMenuManager manager, PuakmaResourceView view)
	{
		manager.add(new Separator(OPEN));
		manager.add(new Separator(NEW));
		manager.add(new Separator(EDIT));
		manager.add(new Separator(EXTENDED));
		manager.add(new Separator(CUSTOM));
		manager.add(new Separator(PROPERTIES));
		manager.add(new Separator(INFO));

		IStructuredSelection selection = (IStructuredSelection) view.getViewer().getSelection();

		// so now handle all available menu actions
		addOpenMenu(manager, view, selection);
		addNewMenu(manager, view, selection);
		addEditMenu(manager, view, selection);
		addExtendedMenu(manager, view, selection);
		addCustomMenu(manager, view, selection);
		addPropertiesMenu(manager, view, selection);
		addInfoMenu(manager, view, selection);
	}

	/**
	 * Adds open menu item if necessary. Adds open menu only if there is selected more design
	 * object items, puakma application, and nothing else.
	 *
	 * @param manager is the menu manager
	 * @param view is the puakma resource view
	 * @param selection is the selection of the puakma resource view
	 */
	private static void addOpenMenu(IMenuManager manager, PuakmaResourceView view,
			IStructuredSelection selection)
	{
		PNBaseAction[] actions = new PNBaseAction[] {
				new PNNewJavaObjectAction(DesignObject.TYPE_LIBRARY, JavaObjectWizard.TYPE_INTERFACE, view),
				new PNNewJavaObjectAction(DesignObject.TYPE_LIBRARY, JavaObjectWizard.TYPE_ENUM, view),
				new PNNewJavaObjectAction(DesignObject.TYPE_LIBRARY, JavaObjectWizard.TYPE_CLASS, view),
				new PNNewJavaObjectAction(DesignObject.TYPE_ACTION, JavaObjectWizard.TYPE_CLASS, view),
				new PNNewJavaObjectAction(DesignObject.TYPE_SCHEDULEDACTION, JavaObjectWizard.TYPE_CLASS, view),
				new PNNewJavaObjectAction(DesignObject.TYPE_WIDGET, JavaObjectWizard.TYPE_CLASS, view),
				new PNNewPageAction(view),
				new PNNewResourceAction(view, NewResourceWizard.TYPE_NEW),
				new PNNewResourceAction(view, NewResourceWizard.TYPE_UPLOAD),
				new PNNewLibraryAction(view),
				new PNNewDbConnectionAction(view),
				new PNQueryEditorAction(view),
				new PNOpenDbSchemaEditor(view),
		};

		addToMenu(actions, view, manager, NEW);
		//    manager.appendToGroup(OPEN, new OpenItemAction(view));
	}

	/**
	 * Adds all new items. The items to add are only design objects, and database objects.
	 * All selected items has to be only in one application. If there is selected only one
	 * item, then it allows you to create new item only from that type. If there are selected
	 * more items, then it allows you to create all new design objects, and also database
	 * objects. If there is nothing selected then allow user to create new application,
	 * and also to connect to the existing applications.
	 *
	 * @param manager is the menu manager
	 * @param view is the puakma resource view
	 * @param selection is the selection of the puakma resource view
	 */
	private static void addNewMenu(IMenuManager manager, PuakmaResourceView view, IStructuredSelection selection)
	{
		if(selection.size() == 0) {
			manager.appendToGroup(NEW, new ConnectToApplicationAction());
			manager.appendToGroup(NEW, new PNNewApplicationAction(view));
		}
	}

	private static void addExtendedMenu(IMenuManager manager, PuakmaResourceView view,
			IStructuredSelection selection)
	{
		PNBaseAction[] actions = new PNBaseAction[] {
				new PNAddToFavoritesAction(view),
		};

		addToMenu(actions, view, manager, EXTENDED);
	}

	/**
	 * Adds delete item to the menu. In the future there should be also something like
	 * copy/paste items.
	 *
	 * @param manager is the menu manager
	 * @param view is the puakma resource view
	 * @param selection is the selection of the puakma resource view
	 */
	private static void addEditMenu(IMenuManager manager, PuakmaResourceView view,
			IStructuredSelection selection)
	{
		PNBaseAction[] actions = new PNBaseAction[] {
				new PNCopyCutAction(view, false),
				new PNCopyCutAction(view, true),
				new PNPasteAction(view),
				new PNRemoveAction(view),
				new PNRefreshAction(view),
				new PNRenameAction(view),
		};

		addToMenu(actions, view, manager, EDIT);
	}

	/**
	 * This function adds all action items to the menu at the place.
	 *
	 * @param actions are the actions to add
	 * @param view is the PuakmaResourceView for which selection all the stuff applies
	 * @param manager is the menu manager
	 * @param menuGroup is the place where to add the menu
	 */
	private static void addToMenu(PNBaseAction[] actions, PuakmaResourceView view,
			IMenuManager manager, String menuGroup)
	{
		for(int i = 0; i < actions.length; ++i) {
			if(actions[i].qualifyForSelection())
				manager.appendToGroup(menuGroup, actions[i]);
		}
	}

	private static void addCustomMenu(IMenuManager manager, PuakmaResourceView view, IStructuredSelection selection)
	{
		if(selectionAreonlyApplications(selection)) {
			manager.appendToGroup(CUSTOM, new DisconnectApplication(view));
		}
	}

	/**
	 * @param manager is the menu manager
	 * @param view is the puakma resource view
	 * @param selection is the selection of the puakma resource view
	 */
	private static void addPropertiesMenu(IMenuManager manager, PuakmaResourceView view,
			IStructuredSelection selection)
	{
		//    if(selectionAreonlyApplications(selection)) {
		//      manager.appendToGroup(PROPERTIES, new PNPropertiesAction(view));
		//    }
		//    else
		addToMenu(
				new PNBaseAction[] { new PNScheduleActionAction(view) },
				view, manager, PROPERTIES);
	}

	/**
	 * Creates items which shows design object properties. To show them, there must be selected
	 * only one node in the puakma resource view.
	 *
	 * @param manager is the menu manager
	 * @param view is the puakma resource view
	 * @param selection is the selection of the puakma resource view
	 */
	private static void addInfoMenu(IMenuManager manager, PuakmaResourceView view,
			IStructuredSelection selection)
	{
		if(selection.size() == 1) {
			Object o = selection.getFirstElement();
			DesignObject obj = (DesignObject) AdapterUtils.getObject(o, DesignObject.class);
			if(obj == null) {
				IFile file = (IFile) AdapterUtils.getObject(o, IFile.class);
				if(file == null) {
					ICompilationUnit cu = (ICompilationUnit) AdapterUtils.getObject(o, ICompilationUnit.class);
					if(cu != null) {
						try {
							IPath path = cu.getCorrespondingResource().getFullPath();
							if(path != null) {
								obj = ProjectUtils.getDesignObject(path);
							}
						}
						catch(JavaModelException e) {
							VortexPlugin.log(e);
						}
					}
				}
				else {
					obj = ProjectUtils.getDesignObject(file);
				}
			}

			if(obj != null) {
				manager.add(new Separator());

				manager.add(new InfoAction("Updated by: " + obj.getUpdatedByUser()));
				manager.add(new InfoAction("Updated on: " + obj.getLastUpdateTime()));
				if(obj.getDesignSize(false) > 0)
					manager.add(new InfoAction("Size of design data: " + obj.getDesignSize(false) + " bytes"));
				if(obj.getDesignSize(true) > 0)
					manager.add(new InfoAction("Size of design sources: " + obj.getDesignSize(true) + " bytes"));
			}
			else {
				DatabaseObject dbo = (DatabaseObject) AdapterUtils.getObject(o, DatabaseObject.class);
				if(dbo != null)
					addDatabaseObjectInformation(manager, dbo);
			}
		}
	}

	/**
	 * Adds information about database object to the menu.
	 * 
	 * @param manager is the menu manager
	 * @param dbo is the database object
	 */
	private static void addDatabaseObjectInformation(IMenuManager manager, DatabaseObject dbo)
	{
		//    if(dbo.existsInJdbc()) {
		//      manager.add(new InfoAction(""));
		//    }
	}

	/**
	 * Checks if all of the selected items are the main application nodes.
	 *
	 * @param selection is the selection which will be checked
	 * @return true if there are selected only application nodes
	 */
	public static boolean selectionAreonlyApplications(IStructuredSelection selection)
	{
		if(selection.size() == 0)
			return false;

		Iterator it = selection.iterator();
		while(it.hasNext()) {
			Object o = it.next();
			if(o instanceof ATVApplicationNode == false)
				return false;
		}
		return true;
	}

	/**
	 * This function should find somehow Application object for the current selection
	 * in the puakma navigator tree. We count only with the first item.
	 *
	 * @param selection is the selection in the tree
	 * @return Application object if the first item belongs to some application,
	 *         null otherwise
	 */
	public static Application getApplicationFromSelection(IStructuredSelection selection)
	{
		Object o = selection.getFirstElement();
		ApplicationObject ao = (ApplicationObject) AdapterUtils.getObject(o, ApplicationObject.class);
		if(ao != null)
			return ao.getApplication();

		IJavaElement je = (IJavaElement) AdapterUtils.getObject(o, IJavaElement.class);
		if(je != null) {
			IResource res = je.getResource();
			PuakmaProject2 project = ProjectUtils.getProject(res);
			if(project != null)
				return project.getApplication();
			else
				return null;
		}

		IResource res = (IResource) AdapterUtils.getObject(o, IResource.class);
		if(res != null) {
			PuakmaProject2 project = ProjectUtils.getProject(res);
			if(project != null)
				return project.getApplication();
			else
				return null;
		}

		Application app = (Application) AdapterUtils.getObject(o, Application.class);
		if(app != null)
			return app;

		return null;
	}
}
