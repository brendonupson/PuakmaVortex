/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 10, 2006
 * 
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.dbeditor.commands;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.CreateRequest;

import puakma.coreide.objects2.Table;
import puakma.coreide.objects2.TableColumn;
import puakma.vortex.VortexPlugin;
import puakma.vortex.editors.dbeditor.parts.TablePart;

public class TableColumnCreateCommand extends Command
{
  private TablePart tablePart;
  private CreateRequest request;
  private TableColumn column;
  /**
   * If true then we have had need to resize table for creating a new column.
   */
  boolean needResize = false;
  private Dimension newSize;
  private Dimension oldSize;
  private int insertionIndex;
  
  /**
   * Note that requst might be null. 
   */
  public TableColumnCreateCommand(TablePart tablePart, CreateRequest request, TableColumn column, int insertionIndex)
  {
    this.tablePart = tablePart;
    this.request = request;
    this.column = column;
    this.insertionIndex = insertionIndex;
    
    // CHECK CORRECT TABLE NAME
    Table table = tablePart.getTable();
    if(table.getColumn(column.getName()) != null) {
      String prefix = column.getName() == null ? "Column" : column.getName();
      String name = getNextTableColumnName(table, prefix);
      column.setName(name);
    }
      column.setType("VARCHAR");
      column.setTypeSize(30);
  }

  public void execute()
  {
    redo();
  }

  public void redo()
  {
    try {
      Table table = tablePart.getTable();
      if(insertionIndex == -1)
        table.addColumn(column);
      else
        table.addColumn(insertionIndex, column);
      
//      TableFigure tableFigure = tablePart.getTableFigure();
//      IFigure contentPane = tableFigure.getContentPane();
      
      // COLUMN ADDED, SEE THE MISSING SIZE, IF THE MISSING SIZE IS LARGER THAN CONTENT PANE SIZE,
      // RESIZE THE TABLE!
//      List l = contentPane.getChildren();
      // NOTE THAT IT HAS TO BE BIGGER THAN ONE BECAUSE WE JUST ADDED NEW COLUMN. IF THIS HAS SIZE
      // OF ZERO - SOMETHING IS WRONG - BUG???
//      IFigure lastFigure = (IFigure) l.get(l.size() - 1);
//      Dimension prefSize = lastFigure.getPreferredSize();
//      int newH = l.size() * prefSize.height;
//      Dimension d = DbSchemaController.guessSize(table);
//      Dimension oldContentClientArea = lastFigure.getPreferredSize();//ClientArea();
//      if(d.height > oldContentClientArea.height) {
//        Dimension size = tableFigure.getSize();
//        this.oldSize = size.getCopy();
////        int heightDifference = newH - oldContentClientArea.height;
////        size.height += heightDifference;
//        size.height = d.height + l.size() + 2;
//        size.width = d.width;
//        tableFigure.setSize(size);
//        this.needResize = true;
//        this.newSize = size;
//        DbSchemaController.getControllerFor(table).setSize(table, newSize);
//      }
    }
    catch(Exception e) {
      VortexPlugin.log(e);
    }
  }

  public void undo()
  {
    try {
      Table table = tablePart.getTable();
      
      table.removeColumn(column);
      
//      if(needResize) {
//        TableFigure tableFigure = tablePart.getTableFigure();
//        tableFigure.setSize(oldSize);
//        DbSchemaController.getControllerFor(table).setSize(table, oldSize);
//      }
    }
    catch(Exception e) {
      VortexPlugin.log(e);
    }
  }

  /**
   * This function returns the next available table name
   *
   * @param table is the database we want to check
   * @return String with the next free table name available
   */
  private static String getNextTableColumnName(Table table, String prefix)
  {
    String candidate;
    // BASICALLY INFINITE LOOP FOR SEARCHING FOR THE CORRECT NAME [-;
    for(int i = 1; ; i++) {
      candidate = prefix + i;
      if(table.getColumn(candidate) == null)
        return candidate;
    }
  }
}
