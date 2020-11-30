/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 11, 2006
 * 
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.dbeditor.tree;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.editparts.AbstractTreeEditPart;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;

import puakma.coreide.objects2.Database;
import puakma.coreide.objects2.Table;
import puakma.utils.lang.CollectionsUtil;

public class TreeDatabaseSchemaPart extends AbstractTreeEditPart implements PropertyChangeListener
{
	public TreeDatabaseSchemaPart()
	{
	}

	public TreeDatabaseSchemaPart(Database database)
	{
		super(database);
	}

	public Database getDatabase()
	{
		return (Database) getModel();
	}

	public void propertyChange(PropertyChangeEvent evt)
	{
		String property = evt.getPropertyName();
		// REFRESH CHILDREN WHEN NEW IS ADDED OR REMOVED
		if(Database.PROP_ADD_TABLE.equals(evt.getPropertyName())) {
			refreshChildren();
		}
		else if(Database.PROP_REMOVE_TABLE.equals(property)) {
			refreshChildren();
		}
	}

	public void activate()
	{
		if(isActive() == false) {
			super.activate();

			getDatabase().addListener(this);
		}
	}

	public void deactivate()
	{
		if(isActive() == false) {
			getDatabase().removeListener(this);

			super.deactivate();
		}
	}

	protected void createEditPolicies()
	{
		// DISABLE REMOVING THIS PART...
		if(getParent() instanceof RootEditPart) {
			installEditPolicy(EditPolicy.COMPONENT_ROLE, new RootComponentEditPolicy());
		}
	}

	protected List<Table> getModelChildren()
	{
		Table[] tables = getDatabase().listTables();
		List<Table> l = new ArrayList<Table>();
		CollectionsUtil.addArrayToList(l, tables);
		return l;
	}

	public Object getAdapter(Class key)
	{
		if(key == Database.class)
			return getDatabase();

		return super.getAdapter(key);
	}
}
