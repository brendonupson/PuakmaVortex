/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    22/05/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.dbschema.actions;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.action.Action;

import puakma.coreide.objects2.TableColumn;
import puakma.vortex.editors.dbeditor.commands.PropertiesChangeCommand;

class DBSEPrimaryKeyAction extends Action
{
  private TableColumn column;
  private CommandStack stack;

  public DBSEPrimaryKeyAction(TableColumn c, CommandStack stack)
  {
    super("Primary Key");
    
    this.column = c;
    this.stack = stack;
    
    setChecked(column.isPk());
  }

  public void run()
  {
    Object newVal = Boolean.valueOf(!column.isPk());
    String propNames = TableColumn.PROP_PK;
    PropertiesChangeCommand cmd = new PropertiesChangeCommand(TableColumn.class,
                                                              propNames, column, newVal);
    if(column.isPk())
      cmd.setLabel("Clear Column Primary Key Attribute");
    else
      cmd.setLabel("Set Column as Primary Key");
    stack.execute(cmd);
  }
}
