/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 20, 2006
 * 
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.dbschema.topeditor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.ui.parts.AbstractEditPartViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;

import puakma.vortex.controls.ColumnsTableProvider;
import puakma.vortex.editors.dbeditor.DbEditorController;
import puakma.vortex.editors.dbeditor.parts.DatabaseSchemaPart;
import puakma.vortex.swt.DialogBuilder2;
import puakma.vortex.swt.PropertiesPage;
import puakma.vortex.swt.TabbedPropertiesController;

public class TopEditor extends AbstractEditPartViewer implements PropertyChangeListener
{
	private TableViewer tableViewer;
	private TableViewer colsViewer;
	private TabbedPropertiesController tabedPropsController;
	private DbEditorController controller;
	private GraphicalViewer viewer;
	/**
	 * If true we are processing selection change from some of the swt controls...
	 */
	private boolean selectFromViewer;
	/**
	 * If true we are processing direct user selection from lists
	 */
	private boolean selectFromLists;

	public TopEditor(Composite parent, DbEditorController controller)
	{
		this.controller = controller;

		Composite c = new Composite(parent, SWT.NULL);
		setControl(c);
		GridLayout gl = new GridLayout();
		gl.marginHeight = gl.marginWidth = 0;
		c.setLayout(gl);

		SashForm form = new SashForm(c, SWT.HORIZONTAL | SWT.SMOOTH);
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		DialogBuilder2 builder = new DialogBuilder2(form);

		builder.createComposite();
		Table tables = builder.createTable();
		builder.closeComposite();

		builder.createComposite();
		Table columns = builder.createTable();
		builder.closeComposite();

		// NOW SETUP PROPERTIES PAGES THERE
		//Composite tabbedPane = builder.createComposite();
		//tabedPropsController = new TabbedPropertiesController(tabbedPane);
		//tabedPropsController.setDirtyListener(this);
		//tabedPropsController.assignModelClassWithPages(puakma.coreide.objects2.Table.class, createTablePropsPages());
		//tabedPropsController.assignModelClassWithPages(puakma.coreide.objects2.TableColumn.class, createColumnsPropsPages());
		//builder.closeComposite();

		builder.finishBuilder();

		if(form.getChildren().length == 3)
			form.setWeights(new int[] {20, 46, 34});
		else
			form.setWeights(new int[] {40, 60});

		// NOW SETUP CONTROLLERS FOR TABLES
		TablesTableProvider tablesProvider = new TablesTableProvider();
		TablesTableProvider.setupColumns(tables);
		tableViewer = builder.setupTableViewer(tables, tablesProvider, tablesProvider, TablesTableProvider.COLS,
				tablesProvider, TablesTableProvider.listCellEditors(tables));

		ColumnsTableProvider columnsProvider = new ColumnsTableProvider();
		ColumnsTableProvider.setupColumns(columns);
		colsViewer = builder.setupTableViewer(columns, columnsProvider, columnsProvider, ColumnsTableProvider.COL_PROPS,
				columnsProvider, ColumnsTableProvider.listCellEditors(columns));

		// SETUP LISTENERS
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event)
			{
				tablesSelectionChanged();
			}
		});
		colsViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event)
			{
				columnsSelectionChanged();
			}
		});

		// AND SUPPLY CONTENT
		tableViewer.setInput(controller.getDatabase());
		//tableViewer.setSelection(StructuredSelection.EMPTY);

		// SELECT THE FIRST TABLE
		//tables.select(0);

		//
		setEditPartFactory(new TopEditorPartFactory());
		setRootEditPart(new TopEditorRootEditPart());
		controller.getSelectionSynchronizer().addViewer(this);
		controller.getEditDomain().addViewer(this);

		setContents(controller.getDatabase());
	}

	/**
	 * Sets up graphical viewer, so we can select its user interface elements
	 * 
	 * @param viewer is the {@link GraphicalViewer} instance
	 */
	public void setupGraphicalViewer(GraphicalViewer viewer)
	{
		this.viewer = viewer;
	}

	/**
	 * Creates properties pages for changing column properties
	 * @return array of {@link PropertiesPage} objects
	 */
	private PropertiesPage[] createColumnsPropsPages()
	{
		PropertiesPage[] pages = {
				new ColumnPageBasicPropertiesPage(controller),
				new ColumnPageIndexPropertiesPage(controller),
				new ColumnPageDescriptionPropertiesPage(controller),
		};
		return pages;
	}

	/**
	 * Creates properties pages for changing table properties
	 * @return array of {@link PropertiesPage} objects
	 */
	private PropertiesPage[] createTablePropsPages()
	{
		PropertiesPage[] pages = {
				new TablePageBasicPropertiesController(controller),
		};
		return pages;    
	}

	protected void columnsSelectionChanged()
	{
		if(selectFromViewer)
			return;
		selectFromLists = true;

		IStructuredSelection selection = (IStructuredSelection) colsViewer.getSelection();

		// AT FIRST TRY TO SELECT UI IN VIEWER
		selectInViewer(selection);

		if(selection.size() != 1) {
			if(tabedPropsController != null)
				tabedPropsController.setInput(null);
			selectFromLists = false;
			return;
		}

		puakma.coreide.objects2.TableColumn column = (puakma.coreide.objects2.TableColumn) selection.getFirstElement();
		if(tabedPropsController != null)
			tabedPropsController.setInput(column);

		selectFromLists = false;


		//    SelectionChangedEvent ev = new SelectionChangedEvent(this, selection);
		//    try {
		//      changingSelection = true;
		//      selSynch.selectionChanged(ev);
		//    }
		//    finally {
		//      changingSelection = false;
		//    }
	}

	protected void tablesSelectionChanged()
	{
		if(selectFromViewer)
			return;
		selectFromLists = true;

		IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();

		if(selection.size() != 1) {
			colsViewer.setInput(null);
			if(tabedPropsController != null)
				tabedPropsController.setInput(null);
		}
		else {
			puakma.coreide.objects2.Table table = (puakma.coreide.objects2.Table) selection.getFirstElement();
			colsViewer.setInput(table);
			colsViewer.setSelection(StructuredSelection.EMPTY);

			// SHOW PROPERTIES ONLY IF WE WANT TO [-;
			if(tabedPropsController != null)
				tabedPropsController.setInput(table);
		}
		// AT FIRST TRY TO SELECT UI IN VIEWER
		selectInViewer(selection);

		selectFromLists = false;
	}

	/**
	 * This function selects all objects in the graphical editor according the
	 * selection parameter.
	 * 
	 * @param selection is the list of objects to select
	 */
	private void selectInViewer(IStructuredSelection selection)
	{
		// PREVENT SOME NPE AT INITIA
		if(viewer == null)
			return;

		Map m = viewer.getEditPartRegistry();
		List<Object> l = new ArrayList<Object>();
		Iterator it = selection.iterator();
		while(it.hasNext()) {
			Object ep = m.get(it.next());
			l.add(ep);
		}
		selection = new StructuredSelection(l);

		SelectionChangedEvent event = new SelectionChangedEvent(this, selection);
		controller.getSelectionSynchronizer().selectionChanged(event);
		//    viewer.setSelection(selection);
	}

	public Control createControl(Composite parent)
	{
		return null;
	}

	public EditPart findObjectAtExcluding(Point location, Collection exclusionSet, Conditional conditional)
	{
		return null;
	}

	protected void fireSelectionChanged()
	{
		super.fireSelectionChanged();
		showSelection();
	}

	/**
	 * This function shows the current selection in the viewers.
	 */
	private void showSelection()
	{

	}

	public void selectionChanged(SelectionChangedEvent event)
	{
		if(selectFromLists)
			return;
		selectFromViewer = true;

		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		if(selection.isEmpty() || selection.getFirstElement() instanceof DatabaseSchemaPart) {
			deselectEverything();
			return;
		}

		// ANYWAY FOR ALL CIRCUMSTAINCES WE SHOULD SAVE PROPERTIES...
		//tabedPropsController.savePages();

		Object[] a = selection.toArray();

		// NOW CHECK IF ALL TABLES ARE SELECTED
		boolean singleTable = true;
		boolean singleCol = true;
		puakma.coreide.objects2.TableColumn curCol = null;
		puakma.coreide.objects2.Table curTab = null;
		boolean singleTableAmongColumns = true;
		puakma.coreide.objects2.Table columnTable = null;

		// CHECK WHO HAS BEEN SELECTED
		for(int i = 0; i < a.length; ++i) {
			// CONVERT EDITPARTS TO MODEL ITEMS
			a[i] = ((EditPart)a[i]).getModel();
			if(a[i] instanceof puakma.coreide.objects2.Table) {
				if(singleTable == true) {
					singleTable = false;
					curTab = (puakma.coreide.objects2.Table) a[i];
				}
				else
					curTab = null;
			}
			else if(a[i] instanceof puakma.coreide.objects2.TableColumn) {
				if(columnTable == null)
					columnTable = ((puakma.coreide.objects2.TableColumn)a[i]).getTable();
				else if(columnTable != ((puakma.coreide.objects2.TableColumn)a[i]).getTable())
					singleTableAmongColumns = false;

				if(singleCol == true) {
					singleCol = false;
					curCol = (puakma.coreide.objects2.TableColumn) a[i];
				}
				else {
					curCol = null;
				}
			}
		}

		if(singleTable && singleCol == false && singleTableAmongColumns) {
			// IF ONLY COLS HAS BEEN SELECTED
			puakma.coreide.objects2.Table t = ((puakma.coreide.objects2.TableColumn)a[0]).getTable();
			tableViewer.setSelection(new StructuredSelection(new Object[] { t }), true);
			colsViewer.setInput(t);
			colsViewer.setSelection(new StructuredSelection(a), true);
			if(tabedPropsController != null)
				tabedPropsController.setInput(curCol);
		}
		else if(singleCol && singleTable == false) {
			// ONLY TABLES HAS BEEN SELECTED
			StructuredSelection modelSelection = new StructuredSelection(a);
			tableViewer.setSelection(modelSelection, true);
			if(selection.size() == 1) {
				colsViewer.setInput(curTab);
				colsViewer.setSelection(modelSelection, true);
			}
			else {
				colsViewer.setInput(null);
				colsViewer.setSelection(StructuredSelection.EMPTY, true);
			}
			if(tabedPropsController != null)
				tabedPropsController.setInput(curTab);
		}
		else {
			// MIXED SELECTION, SO SELECT ONLY TABLES
			List<puakma.coreide.objects2.Table> l = new ArrayList<puakma.coreide.objects2.Table>();
			for(int i = 0; i < a.length; ++i) {
				if(a[i] instanceof puakma.coreide.objects2.Table)
					l.add((puakma.coreide.objects2.Table) a[i]);
			}
			tableViewer.setSelection(new StructuredSelection(l), true);
			// IF THERE IS SINGLE TABLE IN THE SELECTION, FILL THE COLS TABLE, OR CLEAR THE COLS TABLE
			if(l.size() == 1) {
				colsViewer.setInput(l.get(0));
			}
			else {
				colsViewer.setInput(null);
			}
			colsViewer.setSelection(StructuredSelection.EMPTY);

			if(tabedPropsController != null)
				tabedPropsController.setInput(null);
		}

		selectFromViewer = false;
	}

	/**
	 * This function deselects both tables and columns viewers
	 */
	private void deselectEverything()
	{
		colsViewer.setSelection(StructuredSelection.EMPTY, true);
		colsViewer.setInput(null);
		tableViewer.setSelection(StructuredSelection.EMPTY, true);
		if(tabedPropsController != null)
			tabedPropsController.setInput(null);
	}

	public void propertyChange(PropertyChangeEvent evt)
	{
		String prop = evt.getPropertyName();
		if(evt.getSource() == tabedPropsController && TabbedPropertiesController.PROP_DIRTY.equals(prop)) {
			this.controller.dirtyChanged();
		}
	}

	/**
	 * Gets the actual dirty status of property editors. If the property editors
	 * has changed some value by the user, then we should return true, false
	 * otherwise.
	 */
	public boolean isDirty()
	{
		if(tabedPropsController != null)
			return tabedPropsController.isDirty();

		return false;
	}
}
