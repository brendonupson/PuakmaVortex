/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jan 27, 2006
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
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.CreateRequest;

import puakma.coreide.objects2.Database;
import puakma.coreide.objects2.Table;
import puakma.vortex.VortexPlugin;
import puakma.vortex.editors.dbeditor.parts.DatabaseSchemaPart;

public class TableCreateCommand extends Command
{
  private CreateRequest request;
  private DatabaseSchemaPart dbPart;
  private Table table;

  public TableCreateCommand(DatabaseSchemaPart dbPart, CreateRequest request, Table table)
  {
    this.dbPart = dbPart;
    this.request = request;
    this.table = table;
  }

  public void execute()
  {
    Database database = dbPart.getDatabase();
    String name = getNextTableName(database);
    table.setName(name);
    Dimension size = request.getSize();
    if(size == null)
      size = new Dimension(-1, -1);
    
    Rectangle rect = new Rectangle(request.getLocation(), size);
    table.setData(DatabaseSchemaPart.KEY_BOUNDS, rect);

    redo();
  }

  public void redo()
  {
    try {
      Database database = dbPart.getDatabase();
      database.addObject(table);
    }
    catch(Exception e) {
      VortexPlugin.log(e);
    }
  }

  public void undo()
  {
    try {
      Database database = dbPart.getDatabase();
      database.removeObject(table);
    }
    catch(Exception e) {
      VortexPlugin.log(e);
    }
  }

  /**
   * This function returns the next available table name
   * 
   * @param database is the database we want to check
   * @return String with the next free table name available
   */
  private static String getNextTableName(Database database)
  {
    String baseS = "TABLE";
    String candidate;
    // BASICALLY INFINITE LOOP FOR SEARCHING FOR THE CORRECT NAME [-;
    for(int i = 1;; i++) {
      candidate = baseS + i;
      if(database.getTable(candidate) == null)
        return candidate;
    }
  }
}
