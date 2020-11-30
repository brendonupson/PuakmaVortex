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

import org.eclipse.gef.editparts.AbstractTreeEditPart;
import org.eclipse.swt.graphics.Image;

import puakma.coreide.objects2.Table;
import puakma.coreide.objects2.TableColumn;
import puakma.utils.lang.CollectionsUtil;
import puakma.vortex.VortexPlugin;

public class TreeTablePart extends AbstractTreeEditPart implements PropertyChangeListener
{
	public TreeTablePart()
	{
		super();
	}

	public TreeTablePart(Table table)
	{
		super(table);
	}

	public void activate()
	{
		if(isActive() == false) {
			super.activate();

			((Table) getModel()).addListener(this);
		}
	}

	public void deactivate()
	{
		if(isActive()) {
			((Table) getModel()).removeListener(this);

			super.deactivate();
		}
	}

	public List<TableColumn> getModelChildren()
	{
		List<TableColumn> l = new ArrayList<TableColumn>();
		TableColumn[] columns = getTable().listColumns();
		CollectionsUtil.addArrayToList(l, columns);
		return l;
	}

	protected Image getImage()
	{
		return VortexPlugin.getDefault().getImage("table.png");
	}

	public void propertyChange(PropertyChangeEvent evt)
	{
		String prop = evt.getPropertyName();
		if(Table.PROP_COLUMN_ADD.equals(prop) || Table.PROP_COLUMN_MOVE.equals(prop)
				|| Table.PROP_COLUMN_REMOVE.equals(prop)) {
			refreshChildren();
		}
		else if(Table.PROP_NAME.equals(prop)) {
			refreshVisuals();
		}
	}

	protected String getText()
	{
		return ((Table) getModel()).getName();
	}

	public Object getAdapter(Class key)
	{
		if(key == Table.class)
			return getTable();

		return super.getAdapter(key);
	}

	private Table getTable()
	{
		return (Table) getModel();
	}
}
