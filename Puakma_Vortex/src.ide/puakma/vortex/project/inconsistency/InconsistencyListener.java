/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    02/07/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.project.inconsistency;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import puakma.coreide.ServerManager;
import puakma.coreide.event.InconsistenciesList;

public class InconsistencyListener implements PropertyChangeListener
{
  public void propertyChange(final PropertyChangeEvent evt)
  {
    String prop = evt.getPropertyName();
    if(ServerManager.PROP_RESOLVE_INCONSISTENCIES.equals(prop)) {
      Display.getDefault().syncExec(new Runnable() {
      
        public void run()
        {
          Shell shell = Display.getDefault().getActiveShell();
          InconsistenciesList incons = (InconsistenciesList) evt.getNewValue();
          InconsDialog dlg = new InconsDialog(shell, incons);
          dlg.open();
        }
      });
    }
  }

}
