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
import java.util.Collections;
import java.util.List;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.requests.DirectEditRequest;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.graphics.Image;

import puakma.coreide.FkConnectionImpl;
import puakma.coreide.database.SQLUtil;
import puakma.coreide.objects2.FkConnection;
import puakma.coreide.objects2.Table;
import puakma.coreide.objects2.TableColumn;
import puakma.vortex.VortexPlugin;
import puakma.vortex.editors.dbeditor.directedit.DbSchemaDirectEditManager;
import puakma.vortex.editors.dbeditor.figures.EditableFigure;
import puakma.vortex.editors.dbeditor.figures.LeftRightCenterAnchor;
import puakma.vortex.editors.dbeditor.policies.ForeignKeyPolicy;
import puakma.vortex.editors.dbeditor.policies.TableColumnComponentPolicy;
import puakma.vortex.editors.dbeditor.policies.TableColumnDirectEditPolicy;

public class TableColumnPart extends AbstractGraphicalEditPart implements PropertyChangeListener,
FkEditPart, NodeEditPart
{
	private DbSchemaDirectEditManager manager;

	protected IFigure createFigure()
	{
		EditableFigure control = new EditableFigure("");
		setFigure(control);

		control.setSnapToParent(true);
		refreshFigure();

		return control;
	}

	protected void createEditPolicies()
	{
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new TableColumnComponentPolicy());
		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new TableColumnDirectEditPolicy());
		installEditPolicy(EditPolicy.LAYOUT_ROLE, null);
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new ForeignKeyPolicy());
	}

	public EditableFigure getEditableFigure()
	{
		return (EditableFigure) getFigure();
	}

	public TableColumn getColumn()
	{
		return (TableColumn) getModel();
	}

	public void setSelected(int value)
	{
		super.setSelected(value);

		EditableFigure figure = getEditableFigure();
		if(value != EditPart.SELECTED_NONE)
			figure.setSelected(true);
		else
			figure.setSelected(false);
		figure.repaint();
	}

	/**
	 * In TableColumnPart we check for direct editing. We basically try to detect direct hit testing on
	 * some label, and if there is something like that, we start processing direct edit
	 * on the table column.
	 */
	public void performRequest(Request req)
	{
		if(req.getType() == RequestConstants.REQ_DIRECT_EDIT) {
			if(req instanceof DirectEditRequest) {
				Point location = ((DirectEditRequest) req).getLocation().getCopy();
				if(req instanceof DirectEditRequest && directEditHitTest(location) == false)
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
			final EditableFigure figure = (EditableFigure) getFigure();
			ICellEditorValidator validator = new ICellEditorValidator() {
				public String isValid(Object value)
				{
					String name = (String) value;
					if(name.length() == 0)
						return "Column name cannot be empty";
					if(name.indexOf(' ') != -1)
						return "Column name contains invalid characters";
					TableColumn column = getColumn();
					if(column.getName().equalsIgnoreCase(name))
						return "Column names are the same";
					Table table = column.getTable();
					if(table.getColumn(name) != null)
						return "Column " + name + " already exists in table " + table.getName() + ". Please choose unique name";
					return null;
				}
			};

			manager = new DbSchemaDirectEditManager(this, TextCellEditor.class, figure, validator);
		}

		String textToEdit = ((TableColumn) getModel()).getName();
		manager.setTextToEdit(textToEdit);
		manager.show();
	}

	private boolean directEditHitTest(Point location)
	{
		EditableFigure nameLabel = (EditableFigure) getFigure();
		nameLabel.translateToRelative(location);
		if(nameLabel.containsPoint(location))
			return true;
		return false;
	}

	public void propertyChange(PropertyChangeEvent evt)
	{
		String prop = evt.getPropertyName();
		if(TableColumn.PROP_NAME == prop) {
			getEditableFigure().setText((String) evt.getNewValue());
			refreshVisuals();
		}
		else if(TableColumn.PROP_REFERENCED_TABLE == prop) {
			refreshSourceConnections();
			refreshIcon();
		}
		else if(TableColumn.PROP_PK == prop) {
			refreshIcon();
		}
		else if(TableColumn.PROP_UNIQUE == prop) {
			refreshIcon();
		}
		else if(TableColumn.PROP_TYPE == prop) {
			refreshText();
		}
		else if(TableColumn.PROP_DECIMAL_DIGITS == prop) {
			refreshText();
		}
		else if(TableColumn.PROP_TYPESIZE == prop) {
			refreshText();
		}
	}

	protected void refreshVisuals()
	{
		refreshFigure();
		super.refreshVisuals();
	}

	/**
	 * This refreshes the whole figure
	 */
	 private void refreshFigure()
	 {
		 refreshIcon();
		 refreshText();
	 }

	 /**
	  * Refreshes icons text
	  */
	 private void refreshText()
	 {
		 TableColumn column = (TableColumn) getModel();
		 String label;
		 if(SQLUtil.isIntegerType(column.getType())) {
			 label = column.getName() + " : " + column.getType();
		 }
		 else {
			 label = column.toString();
		 }

		 EditableFigure figure = (EditableFigure) getFigure();
		 figure.setText(label);
	 }

	 /**
	  * Sets the proper icon to column figure.
	  */
	 private void refreshIcon()
	 {
		 EditableFigure figure = (EditableFigure) getFigure();
		 TableColumn col = getColumn();
		 String key;
		 if(col.isPk() && col.getRefTable() != null)
			 key = "column_pkfk.png";
		 else if(col.isPk())
			 key = "column_pk.png";
		 else if(col.getRefTable() != null)
			 key = "column_fk.png";
		 else
			 key = "column.png";

		 Image image = VortexPlugin.getDefault().getImage(key);
		 figure.setIcon(image);
		 //    figure.repaint();
	 }

	 public void addTargetConnectionPart(ConnectionEditPart part)
	 {
		 if(targetConnections == null)
			 targetConnections = new ArrayList<Object>();
		 if(targetConnections.contains(part) == false)
			 addTargetConnection(part, targetConnections.size());
	 }

	 protected List<FkConnection> getModelSourceConnections()
	 {
		 TableColumn col = getColumn();
		 if(col.isFk() == false)
			 return Collections.emptyList();

		 FkConnection sourceConnection = col.getRefConnection();
		 List<FkConnection> l = new ArrayList<FkConnection>();
		 l.add(sourceConnection);
		 return l;
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

	 public void activate()
	 {
		 if(isActive() == false)
			 getColumn().addListener(this);

		 super.activate();
	 }

	 public void deactivate()
	 {
		 if(isActive())
			 getColumn().removeListener(this);

		 super.deactivate();
	 }

	 public Object getAdapter(Class key)
	 {
		 if(key == TableColumn.class)
			 return getColumn();

		 return super.getAdapter(key);
	 }

	 public List<FkConnection> getSourceConnections()
	 {
		 return super.getSourceConnections();
	 }
}
