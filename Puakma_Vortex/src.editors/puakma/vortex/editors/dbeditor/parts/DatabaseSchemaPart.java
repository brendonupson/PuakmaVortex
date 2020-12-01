/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jan 17, 2006
 * 
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.dbeditor.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import puakma.coreide.objects2.Database;
import puakma.coreide.objects2.Table;
import puakma.coreide.objects2.TableColumn;
import puakma.utils.lang.CollectionsUtil;
import puakma.vortex.editors.dbeditor.commands.ReshufleAllTablesCommand;
import puakma.vortex.editors.dbeditor.figures.TableFigure;
import puakma.vortex.editors.dbeditor.layout.GraphXYLayoutManager;
import puakma.vortex.editors.dbeditor.policies.DatabaseSchemaComponentPolicy;
import puakma.vortex.editors.dbeditor.policies.DatabaseSchemaContainerEditPolicy;
import puakma.vortex.editors.dbeditor.policies.DatabaseSchemaLayoutPolicy;

public class DatabaseSchemaPart extends AbstractGraphicalEditPart implements
PropertyChangeListener
{
	/**
	 * This is the key for database objects which returns {@link Rectangle}
	 * object, and there should be the position of the table. It might also return
	 * null which means that the position has not been set.
	 */
	public static final String KEY_BOUNDS = "bounds";

	protected IFigure createFigure()
	{
		Figure f = new FreeformLayer();
		f.setBorder(new MarginBorder(3));
		GraphXYLayoutManager l = new GraphXYLayoutManager(this);
		f.setLayoutManager(l);
		return f;
	}

	protected void createEditPolicies()
	{
		// installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, null);
		// CO TOTO ZNAMENA???????
		installEditPolicy(EditPolicy.CONTAINER_ROLE, new DatabaseSchemaContainerEditPolicy());

		// DISALLOW DELETE
		if(getParent() instanceof RootEditPart) {
			installEditPolicy(EditPolicy.COMPONENT_ROLE, new DatabaseSchemaComponentPolicy());
		}
		// ALLOW CREATE, MOVE ON CHILDREN
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new DatabaseSchemaLayoutPolicy());
	}

	protected List<Table> getModelChildren()
	{
		Database db = (Database) getModel();
		List<Table> l = new ArrayList<Table>();
		CollectionsUtil.addArrayToList(l, db.listTables());
		return l;
	}

	public Database getDatabase()
	{
		return (Database) getModel();
	}

	public void activate()
	{
		if(isActive() == false) {
			getDatabase().addListener(this);
		}

		super.activate();
	}

	public void deactivate()
	{
		if(isActive()) {
			getDatabase().removeListener(this);
		}

		super.deactivate();
	}

	public void propertyChange(PropertyChangeEvent evt)
	{
		String property = evt.getPropertyName();
		// REFRESH CHILDREN WHEN NEW IS ADDED OR REMOVED
		if(Database.PROP_ADD_TABLE.equals(property)) {
			refreshChildren();
		}
		else if(Database.PROP_REMOVE_TABLE.equals(property)) {
			refreshChildren();
		}
	}

	public boolean setTableModelBounds()
	{
		List<TablePart> tableParts = getChildren();

		for(TablePart tablePart : tableParts) {
			TableFigure tableFigure = (TableFigure) tablePart.getFigure();

			// if we don't find a node for one of the children then we should
			// continue
			if(tableFigure == null)
				continue;

			Rectangle bounds = tableFigure.getBounds().getCopy();
			Table table = tablePart.getTable();
			table.setData(KEY_BOUNDS, bounds);
		}

		return true;
	}

	public boolean setTableFigureBounds()
	{
		List<TablePart> tableParts = getChildren();

		for(TablePart tablePart : tableParts) {
			Table table = tablePart.getTable();

			// now check whether we can find an entry in the tableToNodesMap
			Rectangle bounds = (Rectangle) table.getData(DatabaseSchemaPart.KEY_BOUNDS);
			if(bounds == null) {
				// TODO handle this better - position the table somehere!!!! and maybe
				// do something about size???
				bounds = new Rectangle();
				return false;
			}

			TableFigure tableFigure = (TableFigure) tablePart.getFigure();
			if(tableFigure == null) {
				return false;
			}
			else {
				// pass the constraint information to the xy layout
				// setting the width and height so that the preferred size will be
				// applied
				GraphXYLayoutManager xyLayout = (GraphXYLayoutManager) getFigure().getLayoutManager();
				xyLayout.setConstraint(tableFigure, new Rectangle(bounds.x, bounds.y, -1, -1));
			}
		}
		return true;
	}

	public void setModel(Object model)
	{
		super.setModel(model);
	}

	public void reshuffleAllTables()
	{
		boolean haveAtLeastOneProperty = false;
		Table[] tables = getDatabase().listTables();

		for(int i = 0; i < tables.length; ++i) {
			Rectangle bounds = (Rectangle) tables[i].getData(KEY_BOUNDS);
			if(bounds != null)
				haveAtLeastOneProperty = true;
		}

		if(haveAtLeastOneProperty == false) {
			ReshufleAllTablesCommand cmd = new ReshufleAllTablesCommand(this);
			cmd.execute();
		}
	}

	public boolean isLayoutManualDesired()
	{
		return false;
	}

	public boolean isLayoutManualAllowed()
	{
		return false;
	}

	public Object getAdapter(Class key)
	{
		if(key == Database.class)
			return getDatabase();
		else
			return super.getAdapter(key);
	}
}
