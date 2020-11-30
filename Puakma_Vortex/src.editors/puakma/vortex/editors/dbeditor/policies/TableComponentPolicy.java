/*
 * Author: Martin Novak
 * Date:   Feb 11, 2006
 */
package puakma.vortex.editors.dbeditor.policies;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

import puakma.coreide.objects2.Table;
import puakma.vortex.editors.dbeditor.commands.TableDeleteCommand;

public class TableComponentPolicy extends ComponentEditPolicy
{
  protected Command createDeleteCommand(GroupRequest deleteRequest)
  {
    Object child = getHost().getModel();
    if(child instanceof Table) {
      return new TableDeleteCommand((Table) child);
    }
    return super.createDeleteCommand(deleteRequest);
  }
}
