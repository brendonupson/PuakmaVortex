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

import puakma.coreide.objects2.Table;
import puakma.vortex.editors.dbeditor.commands.TableDirectEditCommand;
import puakma.vortex.editors.dbeditor.figures.TableFigure;

/**
 * This class handles direct editing of the table name.
 *
 * @author Martin Novak
 */
public class TableDirectEditPolicy extends DirectEditPolicy
{
  protected Command getDirectEditCommand(DirectEditRequest request)
  {
    Table table = (Table) getHost().getModel();
    TableDirectEditCommand command = new TableDirectEditCommand(table);
    
    command.setOldName(((Table) getHost().getModel()).getName());
    command.setName((String) request.getCellEditor().getValue());
    
    // TODO: what is this doing - I got this from gef example...
//    CellEditor cellEditor = request.getCellEditor();
//    command.setName((String) cellEditor.getValue());
    
    return command;
  }

  protected void showCurrentEditValue(DirectEditRequest request)
  {
    String value = (String)request.getCellEditor().getValue();
    TableFigure tableFigure = (TableFigure) getHostFigure();
    tableFigure.getLabelFigure().setText(value);
    //hack to prevent async layout from placing the cell editor twice.
//  getHostFigure().getUpdateManager().performUpdate();
  }

//  protected void revertOldEditValue(DirectEditRequest request)
//  {
//    super.revertOldEditValue(request);
//  }
//
//  protected void storeOldEditValue(DirectEditRequest request)
//  {
//    super.storeOldEditValue(request);
//    
//    CellEditor cellEditor = request.getCellEditor();
//    oldValue = (String) cellEditor.getValue();
//  }
}
