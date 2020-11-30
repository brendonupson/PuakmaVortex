/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Nov 26, 2005
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

import java.io.IOException;

import org.eclipse.jface.action.Action;

import puakma.coreide.ConfigurationManager;
import puakma.vortex.VortexPlugin;

public class PNClearRecentAppsAction extends Action
{
  public PNClearRecentAppsAction()
  {
    super();
    
    setText("Clear Menu");
  }

  public void run()
  {
    ConfigurationManager manager = VortexPlugin.getDefault().getRecentAppsManager();
    try {
      manager.removeAll();
    }
    catch(IOException e) {
      VortexPlugin.log(e);
    }
  }
}
