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

/**
 * This action is supposed to change the type of the column.
 * 
 * @author Martin Novak
 */
public class ChangeTypeAction extends Action
{
  private String typeName;

  private CommandStack stack;

  private TableColumn column;

  public ChangeTypeAction(String typeName, CommandStack stack, TableColumn column)
  {
    super(typeName);

    this.typeName = typeName;
    this.stack = stack;
    this.column = column;

    setText(typeName);
  }

  public void run()
  {
    String[] propNames = new String[] { TableColumn.PROP_TYPE };
    Object[] values = new Object[] { typeName };
    PropertiesChangeCommand cmd = new PropertiesChangeCommand(TableColumn.class,
                                                              propNames, column, values);
    stack.execute(cmd);
  }
}
