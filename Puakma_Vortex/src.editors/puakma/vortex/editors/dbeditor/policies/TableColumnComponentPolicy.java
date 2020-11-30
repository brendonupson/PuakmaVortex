/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 11, 2006
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

import java.util.Iterator;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

import puakma.coreide.objects2.Table;
import puakma.coreide.objects2.TableColumn;
import puakma.vortex.editors.dbeditor.commands.TableColumnDeleteCommand;
import puakma.vortex.editors.dbeditor.parts.TablePart;

public class TableColumnComponentPolicy extends ComponentEditPolicy
{
  protected Command createDeleteCommand(GroupRequest deleteRequest)
  {
    Object parent = getHost().getParent().getModel();
    Object child = getHost().getModel();
    if(parent instanceof Table && child instanceof TableColumn) {
      Table thisTable = (Table) parent;
      
      // SELECT ONLY COLUMNS FROM TABLES WHICH ARE NOT IN THE LIST OF PARTS TO BE DELETED
      Iterator it = deleteRequest.getEditParts().iterator();
      while(it.hasNext()) {
        Object o = it.next();
        if(o instanceof TablePart) {
          Table t = ((TablePart)o).getTable();
          if(thisTable == t)
            return null;
        }
      }
      
      return new TableColumnDeleteCommand((TableColumn) child);
    }
    
    return super.createDeleteCommand(deleteRequest);
  }
}
