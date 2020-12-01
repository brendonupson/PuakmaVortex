/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Mar 15, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.views.navigator.actions;

import java.util.Iterator;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;

import puakma.coreide.PuakmaCoreException;
import puakma.coreide.objects2.Application;
import puakma.vortex.VortexPlugin;
import puakma.vortex.actions.BaseWorkbenchAction;
import puakma.vortex.views.navigator.ATVApplicationNode;
import puakma.vortex.views.navigator.PuakmaResourceView;

/**
 * This action disconnects all of the currently selected applications.
 *
 * @author Martin Novak
 */
public class DisconnectApplication extends BaseWorkbenchAction
{
  private PuakmaResourceView view;

  public DisconnectApplication(PuakmaResourceView view)
  {
    this.view = view;
    setText("Close");
  }

  public void run(IWorkbenchWindow window)
  {
    IStructuredSelection selection = view.getSelection();
    Iterator it = selection.iterator();
    while(it.hasNext()) {
      Object o = it.next();
      if(o instanceof ATVApplicationNode) {
        Application con = ((ATVApplicationNode)o).application;
        try {
          con.close();
        }
        catch(PuakmaCoreException e) {
          VortexPlugin.log(e);
        }
      }
    }
  }
}
