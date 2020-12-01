/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Mar 21, 2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.project.queue;

import java.util.Date;

import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;

import puakma.vortex.swt.SWTUtil;

public class DiffController extends LabelProvider implements IStructuredContentProvider, ITableLabelProvider, ICellModifier
{
  private static final String PROP_NAME = "name";
//  private static final String PROP_USE_LEFT = "useLeft";
//  private static final String PROP_USE_RIGHT = "useRight";
//  private static final String PROP_USE_DONT_CARE = "useDontCare";
  private static final String PROP_CHANGED_BY = "changedBy";
  private static final String PROP_CHANGED = "changed";
  /**
   * List of columns(properties) displayed by the table.
   */
  private static final String[] PROPERTIES = { PROP_NAME, PROP_CHANGED_BY, PROP_CHANGED };
  private static final int INDEX_NAME = 0;
  private static final int INDEX_AUTHOR = 1;
  private static final int INDEX_TIME = 2;
  
  private static final String COLS_NAME[] = { "Name", "Modified By", "Modified Time" };
  private static final String COLS_TOOLTIP[] = { "Design Object Name", "Modified By User", "Modification Time" };
  private static final int COLS_CHAR_WIDTHS[] = { 30, 40, 25 };
  
  private DiffNode[] diffs;
  
  public static String[] getProperties()
  {
    return PROPERTIES;
  }

  public static CellEditor[] getEditors()
  {
    return null;
  }

  public Object[] getElements(Object inputElement)
  {
    return diffs;
  }

  public void dispose()
  {
    diffs = null;
  }

  public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
  {
    diffs = (DiffNode[]) newInput;
  }

  public Image getColumnImage(Object element, int columnIndex)
  {
    return  null;
  }

  public String getColumnText(Object element, int columnIndex)
  {
    DiffNode node = (DiffNode) element;
    HistoryItem compareItem = (HistoryItem) node.getRight();
    switch(columnIndex) {
      case INDEX_NAME:
        return compareItem.getDesignObject().getName();
      case INDEX_AUTHOR:
        return compareItem.getAuthorName();
      case INDEX_TIME:
        // TODO: use time/date formatter here
        return new Date(compareItem.getModificationDate()).toString();
    }
    return "#ERROR#";
  }


  public boolean canModify(Object element, String property)
  {
    return false;
  }

  public Object getValue(Object element, String property)
  {
    return null;
  }

  public void modify(Object element, String property, Object value)
  {
    
  }
  
  /**
   * Setup columns for {@link org.eclipse.swt.widgets.Table} object.
   * 
   * @param table is the {@link org.eclipse.swt.widgets.Table} object in which we should setup columns
   */
  public static void setupColumns(Table table)
  {
    int count = COLS_NAME.length;
    for(int i = 0; i < count; ++i) {
      org.eclipse.swt.widgets.TableColumn col = new org.eclipse.swt.widgets.TableColumn(table, SWT.LEFT);
      col.setText(COLS_NAME[i]);
      col.setWidth(SWTUtil.computeWidthOfChars(table, COLS_CHAR_WIDTHS[i]));
      col.setResizable(true);
      col.setMoveable(true);
      col.setToolTipText(COLS_TOOLTIP[i]);
    }
  }
}
