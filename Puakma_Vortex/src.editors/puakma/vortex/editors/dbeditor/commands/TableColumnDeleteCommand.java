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
package puakma.vortex.editors.dbeditor.commands;

import org.eclipse.gef.commands.Command;

import puakma.coreide.objects2.Table;
import puakma.coreide.objects2.TableColumn;
import puakma.vortex.VortexPlugin;

/**
 * This command allows to remove column from table.
 *
 * @author Martin Novak
 */
public class TableColumnDeleteCommand extends Command
{
  private TableColumn column;
  private Table table;
  private boolean executed;

  public TableColumnDeleteCommand(TableColumn column)
  {
    if(column == null)
      throw new IllegalArgumentException("Invalid arguments for removing table column");
    
    this.column = column;
    this.table = column.getTable();
    
    setLabel("Drop Table Column");
  }

  public boolean canUndo()
  {
    return executed;
  }

  public void execute()
  {
    redo();
  }

  public void redo()
  {
    try {
      table.removeColumn(column);
    }
    catch(Exception e) {
      executed = false;
      VortexPlugin.log(e);
    }
  }

  public void undo()
  {
    try {
      table.addColumn(column);
    }
    catch(Exception e) {
      VortexPlugin.log(e);
    }
  }
}
