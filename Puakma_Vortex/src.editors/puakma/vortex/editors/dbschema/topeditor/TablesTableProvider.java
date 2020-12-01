/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 21, 2006
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

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import puakma.coreide.objects2.Database;
import puakma.coreide.objects2.Table;
import puakma.utils.lang.ArrayUtils;
import puakma.utils.lang.PropertyManipulator;
import puakma.vortex.VortexPlugin;
import puakma.vortex.swt.SWTUtil;

/**
 * Content provider for database tables for {@link TableViewer} viewer.
 * 
 * @author Martin Novak
 */
public class TablesTableProvider extends LabelProvider implements IStructuredContentProvider,
                                                      ITableLabelProvider, PropertyChangeListener,
                                                      ICellModifier
{
  public static final int COL_NAME = 0;
  public static final int COL_DESC = 1;
  public static final String[] COLS = { Table.PROP_NAME, };// Table.PROP_DESCRIPTION, };
  private static final String C_NAMES[] = { "Name", };// "Description" };
  private static final String C_TOOLTIPS[] = { "Table Name", }; //"Table Description" };
  private static final int C_CHARWIDTHS[] = { 30, };//40 };
  private static final boolean C_ALLOWMOVE[] = { false, };//true };

  private Database database;
  private TableViewer viewer;

  public Object[] getElements(Object inputElement)
  {
    if(database != null)
      return database.listTables();
    else
      return new Object[0];
  }

  public void dispose()
  {
    unhookDatabase();
    
    super.dispose();
  }

  public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
  {
    if(newInput != null && newInput instanceof Database == false)
      throw new IllegalArgumentException("This provider accepts only database schema controller as an input");

    this.viewer = (TableViewer) viewer;
    unhookDatabase();
    database = (Database) newInput;
    hookDatabase();
  }

  private void hookDatabase()
  {
    if(database != null) {
      database.addListener(this);
      
      Table[] tables = database.listTables();
      for(int i = 0; i < tables.length; ++i) {
        tables[i].addListener(this);
      }
    }
  }

  /**
   * Unhooks database listeners
   */
  private void unhookDatabase()
  {
    if(database != null) {
      database.removeListener(this);
      Table[] tables = database.listTables();
      for(int i = 0; i < tables.length; ++i) {
        tables[i].removeListener(this);
      }
      database = null;
    }
  }

  public Image getColumnImage(Object element, int columnIndex)
  {
    if(columnIndex == COL_NAME)
      return VortexPlugin.getDefault().getImage("table.png");
    return null;
  }

  public String getColumnText(Object element, int columnIndex)
  {
    Table table = (Table) element;
    if(columnIndex == COL_NAME)
      return table.getName();
    else if(columnIndex == COL_DESC)
      return table.getDescription();

    return "##ERROR##";
  }

  /**
   * Setups columns for table to be able to view all table attributes.
   * @param t is the table where we want to setup columns
   */
  public static void setupColumns(org.eclipse.swt.widgets.Table t)
  {
    for(int i = 0; i < COLS.length; ++i) {
      TableColumn col = new TableColumn(t, SWT.LEFT);
      col.setText(C_NAMES[i]);
      col.setWidth(SWTUtil.computeWidthOfChars(t, C_CHARWIDTHS[i]));
      col.setResizable(true);
      col.setMoveable(C_ALLOWMOVE[i]);
      col.setToolTipText(C_TOOLTIPS[i]);
    }
  }
  
  public static CellEditor[] listCellEditors(org.eclipse.swt.widgets.Table table)
  {
//    TextCellEditor textEditor = new TextCellEditor(table);
//    CellEditor[] mod = {
//        textEditor, textEditor,
//    };
    return new CellEditor[] { null, null };
  }

  public void propertyChange(PropertyChangeEvent evt)
  {
    String prop = evt.getPropertyName();
    // REFRESH TABLE NAME/DESCRIPTION
    if(Table.PROP_NAME.equals(prop) || Table.PROP_DESCRIPTION.equals(prop)) {
      viewer.refresh(evt.getSource(), true);
    }
    // HANDLES ADD/MOVE/REMOVE COLUMNS
    else if(Database.PROP_ADD_TABLE.equals(prop)) {
      Table t = (Table) evt.getNewValue();
      t.addListener(this);
      viewer.add(t);
    }
    else if(Database.PROP_REMOVE_TABLE.equals(prop)) {
      Table t = (Table) evt.getOldValue();
      t.removeListener(this);
      viewer.remove(t);
    }
    else if(Table.PROP_COLUMN_MOVE.equals(prop)) {
      viewer.refresh();
    }
  }

  public boolean canModify(Object element, String property)
  {
    int index = ArrayUtils.indexOf(COLS, property);
    // ENABLE ONLY NAME TO BE MODIFIED
    return index == 0;
  }

  public Object getValue(Object element, String property)
  {
    Table t = (Table) element;
    PropertyManipulator m = new PropertyManipulator(Table.class, property);
    return m.getPropertyFromObject(t);
  }

  public void modify(Object element, String property, Object value)
  {
    TableItem item = (TableItem) element;
    Table t = (Table) item.getData();
    PropertyManipulator m = new PropertyManipulator(Table.class, property);
    // IF PROPERTY DOESN'T MODIFY, DO NOTHING...
    if(value.equals(m.getPropertyFromObject(t)))
      return;
    // CHECK PROPERTY VALUE
    if(Table.PROP_NAME.equals(property)) {
      Database db = t.getDatabase();
      String name = (String) value;
      Table dbTable = db.getTable(name);
      if(dbTable != null)
        return;
    }
    
    m.setPropertyOnObject(t, value);
  }
}
