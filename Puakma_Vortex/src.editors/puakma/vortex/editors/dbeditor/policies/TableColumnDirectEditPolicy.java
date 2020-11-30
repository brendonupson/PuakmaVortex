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
package puakma.vortex.editors.dbeditor.policies;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.DirectEditPolicy;
import org.eclipse.gef.requests.DirectEditRequest;

import puakma.coreide.objects2.TableColumn;
import puakma.vortex.editors.dbeditor.commands.TableColumnDirectEditCommand;
import puakma.vortex.editors.dbeditor.figures.EditableFigure;

public class TableColumnDirectEditPolicy extends DirectEditPolicy
{

  protected Command getDirectEditCommand(DirectEditRequest request)
  {
    TableColumn column = (TableColumn) getHost().getModel();
    TableColumnDirectEditCommand command = new TableColumnDirectEditCommand(column);
    
    command.setOldName(((TableColumn) getHost().getModel()).getName());
    command.setName((String) request.getCellEditor().getValue());
    
    return command;
  }

  protected void showCurrentEditValue(DirectEditRequest request)
  {
    String value = (String)request.getCellEditor().getValue();
    EditableFigure columnFigure = (EditableFigure) getHostFigure();
    columnFigure.setText(value);
    //hack to prevent async layout from placing the cell editor twice.
//  getHostFigure().getUpdateManager().performUpdate();
  }
}
