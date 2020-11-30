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
package puakma.vortex.editors.dbeditor.policies;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;

import puakma.coreide.objects2.Table;
import puakma.coreide.objects2.TableColumn;
import puakma.vortex.editors.dbeditor.commands.ForeignKeyCreateCommand;

public class ForeignKeyPolicy extends GraphicalNodeEditPolicy
{
  protected Command getConnectionCompleteCommand(CreateConnectionRequest request)
  {
    Object o = getHost().getModel();
    ForeignKeyCreateCommand command = (ForeignKeyCreateCommand) request.getStartCommand();
    if(o instanceof Table) {
      command.setTarget((Table) o);
      return command;
    }
    else
      return null;
  }

  protected Command getConnectionCreateCommand(CreateConnectionRequest request)
  {
    Object o = getHost().getModel();
    if(o instanceof TableColumn) {
      TableColumn column = (TableColumn) o;
      ForeignKeyCreateCommand command = new ForeignKeyCreateCommand();
      command.setSource(column);
      request.setStartCommand(command);
      return command;
    }
    else
      return null;
  }

  protected Command getReconnectSourceCommand(ReconnectRequest request)
  {
    throw new IllegalStateException("Not implemented yet");
  }

  protected Command getReconnectTargetCommand(ReconnectRequest request)
  {
    throw new IllegalStateException("Not implemented yet");
  }

}
