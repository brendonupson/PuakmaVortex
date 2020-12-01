/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 15, 2006
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

import org.eclipse.gef.commands.Command;

import puakma.coreide.objects2.Table;
import puakma.coreide.objects2.TableColumn;
import puakma.vortex.VortexPlugin;

public class TableColumnMoveCommand extends Command
{
  private Table table;

  private TableColumn column;

  private int oldIndex;

  private int newIndex;

  public TableColumnMoveCommand(TableColumn column, Table table, int oldIndex,
                                int newIndex)
  {
    this.table = table;
    this.column = column;
    this.oldIndex = oldIndex;
    this.newIndex = newIndex;
    if(newIndex > oldIndex)
      --this.newIndex;
  }

  /**
   * We can execute only when moving inside the same table, and also if the old
   * position is not the same as the newly proposed position.
   */
  public boolean canExecute()
  {
    return newIndex != oldIndex && table == column.getTable();
  }

  public void execute()
  {
    redo();
  }

  public void redo()
  {
    try {
      table.moveColumn(column, newIndex);
    }
    catch(Exception e) {
      VortexPlugin.log(e);
    }
  }

  public void undo()
  {
    try {
      table.moveColumn(column, oldIndex);
    }
    catch(Exception e) {
      VortexPlugin.log(e);
    }
  }
}
