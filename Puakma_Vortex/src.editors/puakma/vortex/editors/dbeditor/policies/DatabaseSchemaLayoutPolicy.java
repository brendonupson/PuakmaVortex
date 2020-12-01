/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 9, 2006
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

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;

import puakma.coreide.objects2.Table;
import puakma.vortex.editors.dbeditor.commands.TableCreateCommand;
import puakma.vortex.editors.dbeditor.commands.TableResizeCommand;
import puakma.vortex.editors.dbeditor.parts.DatabaseSchemaPart;
import puakma.vortex.editors.dbeditor.parts.TablePart;

public class DatabaseSchemaLayoutPolicy extends XYLayoutEditPolicy
{
  protected Command createAddCommand(EditPart child, Object constraint)
  {
    return null;
  }

  protected Command createChangeConstraintCommand(EditPart child, Object constraint)
  {
    return null;
  }
  
  protected Command createChangeConstraintCommand(ChangeBoundsRequest request, EditPart child, Object constraint)
  {
    if(child instanceof TablePart && constraint instanceof Rectangle) {
      TablePart tablePart = (TablePart) child;
      return new TableResizeCommand(tablePart, tablePart.getTable(), request, (Rectangle) constraint);
    }
    return super.createChangeConstraintCommand(request, child, constraint); 
  }

  protected Command getCreateCommand(CreateRequest request)
  {
    Object newObj = request.getNewObject();
    if(newObj instanceof Table == false)
      return null;

    Table table = (Table) newObj;
    DatabaseSchemaPart dbPart = (DatabaseSchemaPart) getHost();
    TableCreateCommand cmd = new TableCreateCommand(dbPart, request, table);
    return cmd;
  }

  protected Command getDeleteDependantCommand(Request request)
  {
    throw new IllegalStateException("Not implemented yet");
  }
  
//  public EditPart getTargetEditPart(Request request)
//  {
//    return null;
//  }
}
