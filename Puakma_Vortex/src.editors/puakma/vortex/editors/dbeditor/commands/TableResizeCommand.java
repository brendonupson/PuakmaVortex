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
package puakma.vortex.editors.dbeditor.commands;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ChangeBoundsRequest;

import puakma.coreide.objects2.Table;
import puakma.vortex.editors.dbeditor.parts.DatabaseSchemaPart;

/**
 * Commands for resizing tables. This needs to be remade!!!
 * 
 * TODO: reimplement this...
 *
 * @author Martin Novak &lt;mn@puakma.net&gt;
 */
public class TableResizeCommand extends Command
{
  private Rectangle oldBounds;

  private Rectangle newBounds;

  private ChangeBoundsRequest request;

  private Table table;

  public TableResizeCommand(EditPart child, Table table, ChangeBoundsRequest request,
      Rectangle bounds)
  {
    this.table = table;
    this.request = request;
    this.newBounds = bounds.getCopy();
  }

  public boolean canExecute()
  {
    if(request == null)
      return true;
    
    Object type = request.getType();
    // make sure the Request is of a type we support:
    return RequestConstants.REQ_MOVE.equals(type) || RequestConstants.REQ_MOVE_CHILDREN.equals(type)
        || RequestConstants.REQ_RESIZE.equals(type) || RequestConstants.REQ_RESIZE_CHILDREN.equals(type);
  }
  
  public void execute()
  {
    oldBounds = (Rectangle) table.getData(DatabaseSchemaPart.KEY_BOUNDS);
    redo();
  }
  
  public void redo()
  {
    table.setData(DatabaseSchemaPart.KEY_BOUNDS, newBounds);
  }
  
  public void undo()
  {
    table.setData(DatabaseSchemaPart.KEY_BOUNDS, oldBounds);
  }
}
