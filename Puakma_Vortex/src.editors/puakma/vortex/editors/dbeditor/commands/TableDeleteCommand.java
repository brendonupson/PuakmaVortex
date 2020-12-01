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

import puakma.coreide.objects2.Database;
import puakma.coreide.objects2.Table;
import puakma.vortex.VortexPlugin;

/**
 * This command allows to remove table from database.
 *
 * @author Martin Novak
 */
public class TableDeleteCommand extends Command
{
  private Table table;
  private boolean executed;

  public TableDeleteCommand(Table table)
  {
    this.table = table;
    
    setLabel("delete table");
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
      Database database = table.getDatabase();
      database.removeObject(table);
    }
    catch(Exception e) {
      executed = false;
      VortexPlugin.log(e);
    }
  }

  public void undo()
  {
    try {
      Database database = table.getDatabase();
      database.addObject(table);
    }
    catch(Exception e) {
      VortexPlugin.log(e);
    }
  }
}
