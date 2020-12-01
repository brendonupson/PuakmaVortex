/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 14, 2006
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

import puakma.coreide.objects2.TableColumn;

public class TableColumnDirectEditCommand extends Command
{
  private String name;
  private String oldName;
  private TableColumn column;

  public TableColumnDirectEditCommand(TableColumn column)
  {
    super();
    
    this.column = column;
  }
  
  public void execute()
  {
    redo();
  }

  public void redo()
  {
    column.setName(name);
  }

  public void undo()
  {
    column.setName(oldName);
  }

  public void setOldName(String name)
  {
    this.oldName = name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

}
