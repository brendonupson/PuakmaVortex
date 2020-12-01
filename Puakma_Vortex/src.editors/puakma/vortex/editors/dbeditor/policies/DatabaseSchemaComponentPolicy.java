/*
 * Author: Martin Novak
 * Date:   Feb 11, 2006
 */
package puakma.vortex.editors.dbeditor.policies;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

public class DatabaseSchemaComponentPolicy extends ComponentEditPolicy
{
  /**
   * Overridden to prevent the host from being deleted.
   * @see org.eclipse.gef.editpolicies.ComponentEditPolicy#createDeleteCommand(GroupRequest)
   */
  protected Command createDeleteCommand(GroupRequest request)
  {
    return UnexecutableCommand.INSTANCE;
  }
}
