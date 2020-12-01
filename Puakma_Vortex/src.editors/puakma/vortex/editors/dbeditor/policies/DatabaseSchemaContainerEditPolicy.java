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
package puakma.vortex.editors.dbeditor.policies;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ContainerEditPolicy;
import org.eclipse.gef.requests.CreateRequest;

/**
 * TODO: CO tato class dela?
 *
 * @author Martin Novak &lt;mn@puakma.net&gt;
 */
public class DatabaseSchemaContainerEditPolicy extends ContainerEditPolicy
{

  protected Command getCreateCommand(CreateRequest request)
  {
//    Object newObj = request.getNewObject();
//    if(newObj instanceof Table == false)
//      return null;
//
//    DatabaseSchemaPart dbPart = (DatabaseSchemaPart) getHost();
//    Table table = (Table) request.getNewObject();
//    TableCreateCommand cmd = new TableCreateCommand(dbPart, request, table);
//    return cmd;
    return null;
  }

  public EditPart getTargetEditPart(Request request)
  {
    if(REQ_CREATE.equals(request.getType()))
      return getHost();
    if(REQ_ADD.equals(request.getType()))
      return getHost();
    if(REQ_MOVE.equals(request.getType()))
      return getHost();
    if(REQ_SELECTION.equals(request.getType()))
      return getHost();
    return super.getTargetEditPart(request);
  }
}
