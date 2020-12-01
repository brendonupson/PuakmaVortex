/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jan 20, 2006
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
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.Clickable;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.requests.DirectEditRequest;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;

import puakma.coreide.FkConnectionImpl;
import puakma.coreide.objects2.Database;
import puakma.coreide.objects2.FkConnection;
import puakma.coreide.objects2.ServerObject;
import puakma.coreide.objects2.Table;
import puakma.coreide.objects2.TableColumn;
import puakma.utils.NameValuePair;
import puakma.utils.lang.CollectionsUtil;
import puakma.vortex.dialogs.database.NewColumnDialog;
import puakma.vortex.editors.dbeditor.directedit.DbSchemaDirectEditManager;
import puakma.vortex.editors.dbeditor.figures.EditableFigure;
import puakma.vortex.editors.dbeditor.figures.LeftRightCenterAnchor;
import puakma.vortex.editors.dbeditor.figures.TableFigure;
import puakma.vortex.editors.dbeditor.policies.ForeignKeyPolicy;
import puakma.vortex.editors.dbeditor.policies.TableComponentPolicy;
import puakma.vortex.editors.dbeditor.policies.TableDirectEditPolicy;
import puakma.vortex.editors.dbeditor.policies.TableLayoutEditoPolicy;

/**
 * Part for table.
 *
 * @author Martin Novak &lt;mn@puakma.net&gt;
 */
public class TablePart extends AbstractGraphicalEditPart implements PropertyChangeListener,
FkEditPart, NodeEditPart, ActionListener,
IAdaptable
{
	private DbSchemaDirectEditManager manager;

	protected IFigure createFigure()
	{
		Table table = (Table) getModel();
		TableFigure figure = new TableFigure(table.getName());
		figure.addBottomButtonsListener(this);
		return figure;
	}

	protected void addTargetConnection(ConnectionEditPart connection, int index)
	{
		super.addTargetConnection(connection, index);
	}

	protected void createEditPolicies()
	{
		//installEditPolicy(EditPolicy.CONTAINER_ROLE, new TableContainerEditPolicy());
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new TableComponentPolicy());
		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new TableDirectEditPolicy());
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new TableLayoutEditoPolicy());
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new ForeignKeyPolicy());
	}

	protected List<TableColumn> getModelChildren()
	{
		Table t = (Table) getModel();
		List<TableColumn> l = new ArrayList<TableColumn>();
		CollectionsUtil.addArrayToList(l, t.listColumns());
		return l;
	}

	protected void registerVisuals()
	{
		super.registerVisuals();

		//    Rectangle bounds = new Rectangle(getLocation(), getSize());
		//    ((GraphicalEditPart) getParent()).setLayoutConstraint(this, getFigure(), bounds);
	}

	public void activate()
	{
		if(isActive() == false)
			getTable().addListener(this);

		super.activate();
	}

	public void deactivate()
	{
		if(isActive())
			getTable().removeListener(this);

		super.deactivate();
	}

	public Table getTable()
	{
		return (Table) getModel();
	}

	public void propertyChange(final PropertyChangeEvent evt)
	{
		Display.getDefault().asyncExec(new Runnable() {
			public void run()
			{
				String property = evt.getPropertyName();
				if(ServerObject.PROP_DATA.equals(property)) {
					NameValuePair pair = (NameValuePair) evt.getNewValue();
					if(DatabaseSchemaPart.KEY_BOUNDS.equals(pair.getName())) {
						TableFigure tableFigure = (TableFigure) getFigure();
						Rectangle constraint = (Rectangle) pair.getValue();
						Rectangle oldConstraint = tableFigure.getBounds();
						if(constraint.x == oldConstraint.x && constraint.y == oldConstraint.y)
							return;
						DatabaseSchemaPart parent = (DatabaseSchemaPart) getParent();
						parent.setLayoutConstraint(TablePart.this, tableFigure, constraint);
					}
				}
				if(ServerObject.PROP_NAME.equals(property)) {
					getTableFigure().setName((String) evt.getNewValue());
					refreshVisuals();
				}
				else if(Table.PROP_COLUMN_ADD.equals(property) || Table.PROP_COLUMN_MOVE.equals(property)
						|| Table.PROP_COLUMN_REMOVE.equals(property)) {
					refreshChildren();
				}
				else if(property == TableFigure.EVT_PLUS_BTN_PRESSED) {
					addTableColumnPressed();
				}
				else if(property == TableFigure.EVT_DROP_DOWN_MENU) {
					showPopupMenu();
				}
				else if(property == Table.PROP_TARGET_CONNECTION_CHANGE) {
					refreshSourceConnections();
					refreshTargetConnections();
					refresh();
				}
			}
		}); 
	}

	/**
	 * This will try to make an dialog asking user about the new column in the
	 * table.
	 */
	private void addTableColumnPressed()
	{
		NewColumnDialog dlg = new NewColumnDialog(getTable(), getViewer().getControl().getShell());
		dlg.open();
	}

	/**
	 * Shows the popup menu for the table.
	 */
	private void showPopupMenu()
	{
		EditPartViewer viewer = getViewer();
		Control c = viewer.getControl();
		viewer.select(this);
		MenuManager menu = viewer.getContextMenu();
		Menu m = menu.createContextMenu(c);
		TableFigure figure = (TableFigure) getFigure();
		Clickable btFig = figure.getEditButton();

		Rectangle r = btFig.getBounds().getCopy();
		btFig.translateToAbsolute(r);

		org.eclipse.swt.graphics.Point p = new org.eclipse.swt.graphics.Point(r.x, r.y + btFig.getBounds().height);

		p = c.getDisplay().map(c, null, p);
		m.setLocation(p);
		m.setVisible(true);
	}

	public TableFigure getTableFigure()
	{
		return (TableFigure) getFigure();
	}

	protected void refreshVisuals()
	{
		TableFigure figure = (TableFigure) getFigure();
		Table table = (Table) getModel();
		figure.setName(table.getName());

		super.refreshVisuals();
	}

	public IFigure getContentPane()
	{
		return ((TableFigure)getFigure()).getContentPane();
	}

	public void setSelected(int value)
	{
		super.setSelected(value);

		TableFigure tableFigure = (TableFigure) getFigure();
		EditableFigure figure = tableFigure.getLabelFigure();

		if(value != EditPart.SELECTED_NONE) {
			tableFigure.setSelected(true);
			//figure.setSelected(true);
		}
		else {
			tableFigure.setSelected(false);
			//figure.setSelected(false);
		}
		tableFigure.repaint();
		figure.repaint();
	}

	/**
	 * In TableEdit part we check for direct editing. We basically try to detect
	 * direct hit testing on some label, and if there is something like that, we
	 * start processing direct edit on the table.
	 */
	public void performRequest(Request req)
	{
		if(req.getType() == RequestConstants.REQ_DIRECT_EDIT) {
			if(req instanceof DirectEditRequest) {
				Point location = ((DirectEditRequest) req).getLocation().getCopy();
				if(directEditHitTest(location) == false)
					return;
			}

			performDirectEdit();
			return;
		}

		super.performRequest(req);
	}

	private void performDirectEdit()
	{
		if(manager == null) {
			final EditableFigure figure = ((TableFigure) getFigure()).getLabelFigure();
			ICellEditorValidator validator = new ICellEditorValidator() {
				public String isValid(Object value)
				{
					String name = (String) value;
					if(name.length() == 0)
						return "Table name cannot be empty";
					if(name.indexOf(' ') != -1)
						return "Table name contains invalid characters";
					Table table = getTable();
					if(table.getName().equalsIgnoreCase(name))
						return "Table names are the same";
					Database db = table.getDatabase();
					if(db.getTable(name) != null)
						return "Table " + name + " already exists in database. Please choose unique name";
					// TODO: add restrictions for table name size here
					return null;
				}
			};

			manager = new DbSchemaDirectEditManager(this, TextCellEditor.class, figure, validator);
		}
		String textToEdit = ((Table) getModel()).getName();
		manager.setTextToEdit(textToEdit);
		manager.show();
	}

	private boolean directEditHitTest(Point location)
	{
		TableFigure figure = (TableFigure) getFigure();
		EditableFigure nameLabel = figure.getLabelFigure();
		nameLabel.translateToRelative(location);
		if(nameLabel.containsPoint(location))
			return true;
		return false;
	}

	protected List<FkConnection> getModelTargetConnections()
	{
		Table table = getTable();
		return table.getTargetConnections();
	}

	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection)
	{
		return new LeftRightCenterAnchor(getFigure());
	}

	public ConnectionAnchor getTargetConnectionAnchor(Request request)
	{
		return new LeftRightCenterAnchor(getFigure());
	}

	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection)
	{
		return new LeftRightCenterAnchor(getFigure());
	}

	public ConnectionAnchor getSourceConnectionAnchor(Request request)
	{
		return new LeftRightCenterAnchor(getFigure());
	}

	public void addTargetConnectionPart(ConnectionEditPart part)
	{
		if(targetConnections == null)
			targetConnections = new ArrayList();
		if(targetConnections.contains(part) == false)
			addTargetConnection(part, targetConnections.size());
	}

	public List<FkConnection> getTargetConnections()
	{
		return super.getTargetConnections();
	}

	public void actionPerformed(ActionEvent event)
	{

	}

	public Object getAdapter(Class key)
	{
		if(key == Table.class)
			return getTable();

		return super.getAdapter(key);
	}
}
