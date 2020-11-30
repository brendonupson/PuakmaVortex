/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 23, 2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.controls;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TableItem;

import puakma.coreide.objects2.Table;
import puakma.coreide.objects2.TableColumn;
import puakma.utils.lang.ArrayUtils;
import puakma.utils.lang.PropertyManipulator;
import puakma.vortex.VortexPlugin;
import puakma.vortex.swt.DialogBuilder2;
import puakma.vortex.swt.SWTUtil;

public class ColumnsTableProvider extends LabelProvider implements IStructuredContentProvider,
                                                       ITableLabelProvider, PropertyChangeListener,
                                                       ICellModifier
{
  public static final int COL_NAME      = 0;
  public static final int COL_TYPE      = 1;
  public static final int COL_TYPE_SIZE = 2;
  public static final int COL_DECIMALS  = 3;
  public static final int COL_PK        = 4;
  public static final int COL_UNIQUE    = 5;
  public static final int COL_AUTOINC   = 6;
  public static final int COL_DESC      = 7;
  public static final String[] COL_PROPS = {
    TableColumn.PROP_NAME, TableColumn.PROP_TYPE, TableColumn.PROP_TYPESIZE,
    TableColumn.PROP_DECIMAL_DIGITS, TableColumn.PROP_PK, TableColumn.PROP_UNIQUE,
    TableColumn.PROP_AUTO_INC, //TableColumn.PROP_DESCRIPTION,
  };
  
  private static final String COLS_NAME[] = { "Name", "Type", "Size", "Decs", "PK", "Uni",
                                              "AI", };// "Description" };
  private static final String COLS_TOOLTIP[] = { "Column Name", "Column Data Type", "Column Size",
                                                 "Column Decimals", "Primary Key", "Unique",
                                                 "Auto Increment Values",};// "Column Description" };
  private static final int COLS_CHAR_WIDTHS[] = { 25, 12, 7, 7, 5, 5, 5, };// 30 };
  private static final boolean COLS_ALLOW_MOVE[] = { false, true, true, true, true, true, true, };//true };
  
  private Table table;
  private TableViewer viewer;
  
  public Object[] getElements(Object inputElement)
  {
    if(table != null)
      return table.listColumns();
    else
      return new Object[0];
  }

  public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
  {
    if(newInput instanceof Table == false && newInput != null)
      throw new IllegalArgumentException("This provider accepts only Table as an input");
    
    this.viewer = (TableViewer) viewer;
    
    unhookTable();
    table = (Table) newInput;
    hookTable();
  }
  
  private void hookTable()
  {
    if(table != null) {
      table.addListener(this);
      
      TableColumn[] cols = table.listColumns();
      for(int i = 0; i < cols.length; ++i) {
        cols[i].addListener(this);
      }
    }
  }

  private void unhookTable()
  {
    if(table != null) {
      table.removeListener(this);
      
      TableColumn[] cols = table.listColumns();
      for(int i = 0; i < cols.length; ++i) {
        cols[i].removeListener(this);
      }
      table = null;
    }
  }

  public void dispose()
  {
    // FOR SURE CHECK PROPERTY CHANGE LISTENER
    unhookTable();
    
    super.dispose();
  }

  public Image getColumnImage(Object element, int columnIndex)
  {
    String key = null;
    if(element instanceof TableColumn) {
      TableColumn col = (TableColumn) element;
      if(columnIndex == COL_NAME) {
        if(col.isPk() && col.isFk())
          key = "column_pkfk.png";
        else if(col.isPk())
          key = "column_pk.png";
        else if(col.isFk())
          key = "column_fk.png";
        else
          key = "column.png";
      }
      else if(columnIndex == COL_PK) {
        if(col.isPk())
          return DialogBuilder2.getCheckboxButtonImage(true);
        else
          return DialogBuilder2.getCheckboxButtonImage(false);
      }
      else if(columnIndex == COL_AUTOINC) {
        if(col.isAutoInc())
          return DialogBuilder2.getCheckboxButtonImage(true);
        else
          return DialogBuilder2.getCheckboxButtonImage(false);
      }
      else if(columnIndex == COL_UNIQUE) {
        if(col.isUnique())
          return DialogBuilder2.getCheckboxButtonImage(true);
        else
          return DialogBuilder2.getCheckboxButtonImage(false);
      }
    }
    
    if(key != null)
      return VortexPlugin.getDefault().getImage(key);
    else
      return null;
  }

  public String getColumnText(Object element, int columnIndex)
  {
    TableColumn column = (TableColumn) element;
    switch(columnIndex) {
      case COL_NAME:
        return column.getName();
      case COL_TYPE:
        return column.getType();
      case COL_TYPE_SIZE: {
        int value = column.getTypeSize();
        if(value < 0)
          return "";
        return Integer.toString(value);
      }
      case COL_DECIMALS: {
        int value = column.getFloatDecimals();
        if(value < 1)
          return "";
        return Integer.toString(value);
      }
      case COL_PK:
      case COL_UNIQUE:
      case COL_AUTOINC:
        return "";
      case COL_DESC:
        return column.getDescription();
    }
    
    throw new IllegalStateException("Invalid column index among table column indexes: " + columnIndex);
  }

  /**
   * Setup columns for {@link org.eclipse.swt.widgets.Table} object.
   * 
   * @param table is the {@link org.eclipse.swt.widgets.Table} object in which
   *          we should setup columns
   */
  public static void setupColumns(org.eclipse.swt.widgets.Table table)
  {
    int count = COLS_NAME.length;
    for(int i = 0; i < count; ++i) {
      org.eclipse.swt.widgets.TableColumn col = new org.eclipse.swt.widgets.TableColumn(table, SWT.LEFT);
      col.setText(COLS_NAME[i]);
      col.setWidth(SWTUtil.computeWidthOfChars(table, COLS_CHAR_WIDTHS[i]));
      col.setResizable(true);
      col.setMoveable(COLS_ALLOW_MOVE[i]);
      col.setToolTipText(COLS_TOOLTIP[i]);
    }  
  }

  /**
   * On any property change refresh also the viewer.
   */
  public void propertyChange(PropertyChangeEvent evt)
  {
    String prop = evt.getPropertyName();
    if(Table.PROP_COLUMN_ADD.equals(prop)) {
      TableColumn col = (TableColumn) evt.getNewValue();
      viewer.add(col);
      col.addListener(this);
    }
    else if(Table.PROP_COLUMN_MOVE.equals(prop)) {
      viewer.refresh();
    }
    else if(Table.PROP_COLUMN_REMOVE.equals(prop)) {
      TableColumn col = (TableColumn) evt.getOldValue();
      viewer.remove(col);
      col.removeListener(this);
    }
    else {
      viewer.refresh(evt.getSource(), true);
    }
  }
  
  public boolean canModify(Object element, String property)
  {
    return ArrayUtils.indexOf(COL_PROPS, property) != -1;
  }

  public Object getValue(Object element, String property)
  {
    TableColumn t = (TableColumn) element;
    // FILTER OUT ZEROES AND -1s AT COLUMN SIZES
    if(TableColumn.PROP_TYPESIZE.equals(property)) {
      int value = t.getTypeSize();
      if(value < 0)
        value = 0;
      // TODO: convert this stupidity to TextNumericCellEditor
      return Integer.toString(value);
    }
    else if(TableColumn.PROP_DECIMAL_DIGITS.equals(property)) {
      int value = t.getFloatDecimals();
      if(value < 0)
        value = 0;
      return Integer.toString(value);
    }
    PropertyManipulator m = new PropertyManipulator(TableColumn.class, property);
    Object ret = m.getPropertyFromObject(t);
    return ret;
  }

  public void modify(Object element, String property, Object value)
  {
    TableItem item = (TableItem) element;
    TableColumn t = (TableColumn) item.getData();
    PropertyManipulator m = new PropertyManipulator(TableColumn.class, property);
    // IF PROPERTY DOESN'T MODIFY, DO NOTHING...
    if(value.equals(m.getPropertyFromObject(t)))
      return;
    // CHECK NAME PROPERTY VALUE
    if(Table.PROP_NAME.equals(property)) {
      Table table = t.getTable();
      String name = (String) value;
      TableColumn c = table.getColumn(name);
      if(c != null)
        return;
    }
    
    m.setPropertyOnObject(t, value);
  }
  
  public static CellEditor[] listCellEditors(org.eclipse.swt.widgets.Table table)
  {
    CellEditor[] mod = {
        null, //new TextCellEditor(table), // NAME
        new TextCellEditor(table), // TYPE
        SWTUtil.createNumericTextCellEditor(table), // TYPE_SIZE
        SWTUtil.createNumericTextCellEditor(table), // DECIMALS
        new CheckboxCellEditor(table), new CheckboxCellEditor(table),
        new CheckboxCellEditor(table), // PK, UNIQUE, AUTOINC
        null // DESC
    };
    
    return mod;
  }
}
