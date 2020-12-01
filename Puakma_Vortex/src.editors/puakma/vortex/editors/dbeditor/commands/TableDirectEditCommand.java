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

import puakma.coreide.objects2.Table;

public class TableDirectEditCommand extends Command
{

  private String name;
  private Table table;
  private String oldName;

  public TableDirectEditCommand(Table table)
  {
    super();
    
    this.table = table;
  }

  public void execute()
  {
    redo();
  }

  public void redo()
  {
    table.setName(name);
    
    // TODO: handle resizing somehow...
  }

  public void undo()
  {
    table.setName(oldName);
  }

  public void setOldName(String oldName)
  {
    this.oldName = oldName;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  
}
