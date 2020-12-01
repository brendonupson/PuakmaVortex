/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 15, 2006
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
import org.eclipse.gef.requests.CreateRequest;

import puakma.coreide.objects2.Table;
import puakma.coreide.objects2.TableColumn;
import puakma.vortex.editors.dbeditor.commands.TableColumnCreateCommand;
import puakma.vortex.editors.dbeditor.commands.TableColumnMoveCommand;
import puakma.vortex.editors.dbeditor.parts.TablePart;
import puakma.vortex.gef.ToolbarLayoutEditPolicy;

public class TableLayoutEditoPolicy extends ToolbarLayoutEditPolicy
{
  protected Command createAddCommand(EditPart child, EditPart after)
  {
    return null;
  }

  protected Command createMoveChildCommand(EditPart child, EditPart after)
  {
    if(after != null) {
      TableColumn childModel = (TableColumn) child.getModel();
      TableColumn afterModel = (TableColumn) after.getModel();

      Table parentTable = (Table) getHost().getModel();
      int oldIndex = getHost().getChildren().indexOf(child);
      int newIndex = getHost().getChildren().indexOf(after);

      TableColumnMoveCommand command = new TableColumnMoveCommand(childModel, parentTable, oldIndex, newIndex);
      return command;
    }
    return null;
  }

  protected Command getCreateCommand(CreateRequest request)
  {
    Object newObj = request.getNewObject();
    if(newObj instanceof TableColumn == false)
      return null;
    
    EditPart afterPart = getInsertionReference(request);
    int insertionIndex;
    if(afterPart != null) {
      TableColumn col = (TableColumn) afterPart.getModel();
      insertionIndex = col.getPosition();
      //insertionIndex = getHost().getChildren().indexOf(col);
    }
    else
      insertionIndex = getHost().getChildren().size();

    TablePart dbPart = (TablePart) getHost();
    TableColumn table = (TableColumn) request.getNewObject();
    TableColumnCreateCommand cmd = new TableColumnCreateCommand(dbPart, request, table, insertionIndex);
    return cmd;
  }

  protected Command getDeleteDependantCommand(Request request)
  {
    return null;
  }
}
