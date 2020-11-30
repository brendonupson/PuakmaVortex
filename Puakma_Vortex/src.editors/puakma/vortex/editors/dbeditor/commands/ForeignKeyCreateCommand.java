/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    20/05/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
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

/**
 * This command creates a new foreign key.
 * 
 * @author Martin Novak
 */
public class ForeignKeyCreateCommand extends Command
{
  private TableColumn source;

  /**
   * This is the table which we are connecting to
   */
  private Table targetTable;

  /**
   * This is the table which was cannected before, so we can undo this action.
   */
  private Table oldTargetTable;

  public void setSource(TableColumn source)
  {
    this.source = source;
  }

  public void setTarget(Table targetTable)
  {
    this.targetTable = targetTable;
  }

  public Table getTargetTable()
  {
    return targetTable;
  }

  public TableColumn getSource()
  {
    return source;
  }

  public boolean canExecute()
  {
    // WE SHOULDN'T REFERENCE TO THE SAME TABLE AS BEFORE
    //Table oldRefTable = source.getRefTable();
    //if(targetTable == oldRefTable)
    //  return false;
    
    // WE SHOULDN'T REFERENCE TO THE PARENT TABLE WHEN SOURCE IS ALSO
    // PRIMARY KEY
    if(targetTable == source.getTable() && source.isPk())
      return false;
    
    return true;
  }

  public void execute()
  {
    redo();
  }

  public void redo()
  {
    if(oldTargetTable == null)
      oldTargetTable = source.getRefTable();
    source.setRefTable(targetTable);
  }

  public void undo()
  {
    source.setRefTable(oldTargetTable);
  }
}
