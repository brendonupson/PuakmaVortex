/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jan 17, 2006
 * 
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.dbeditor;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

import puakma.coreide.FkConnectionImpl;
import puakma.coreide.objects2.Database;
import puakma.coreide.objects2.Table;
import puakma.coreide.objects2.TableColumn;
import puakma.vortex.editors.dbeditor.parts.ConnectionPart;
import puakma.vortex.editors.dbeditor.parts.DatabaseSchemaPart;
import puakma.vortex.editors.dbeditor.parts.TableColumnPart;
import puakma.vortex.editors.dbeditor.parts.TablePart;

/**
 * This factory class creates parts for model objects.
 * 
 * @author Martin Novak
 */
class PartFactory implements EditPartFactory
{
  public EditPart createEditPart(EditPart context, Object model)
  {
    EditPart part = null;
    
    if(model instanceof Table) {
      part = new TablePart();
    }
    else if(model instanceof TableColumn) {
      part = new TableColumnPart();
    }
    else if(model instanceof FkConnectionImpl) {
      part = new ConnectionPart();
    }
    else if(model instanceof Database) {
      part = new DatabaseSchemaPart();
    }
    else
      throw new IllegalArgumentException("Invalid model object passed to schema");
    
    part.setModel(model);
    return part;
  }
}
