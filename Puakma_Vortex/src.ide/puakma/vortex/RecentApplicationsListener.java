/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Nov 30, 2005
 * 
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import puakma.coreide.ConfigurationManager;
import puakma.coreide.ConnectionPrefs;
import puakma.coreide.ConnectionPrefsImpl;
import puakma.coreide.ServerManager;
import puakma.coreide.objects2.Application;

public class RecentApplicationsListener implements PropertyChangeListener
{
  static public final String RECENT = "recent";
  private static final int RECENT_MAX_SIZE = 10;

  public void connectToApplication(Application application)
  {
    ConfigurationManager manager = VortexPlugin.getDefault().getRecentAppsManager();
    synchronized(manager) {
      // TODO: optimize manager's performance [-;
      int count = manager.countConnections();
      if(count == RECENT_MAX_SIZE) {
        pushAllDown(manager);
        addNewApplicationAtPosition(application, manager, RECENT_MAX_SIZE);
      }
      else {
        addNewApplicationAtPosition(application, manager, count + 1);
      }
    }
  }
  
  /**
   * Adds a new application at the
   * @param application
   * @param index
   */
  private void addNewApplicationAtPosition(Application application, ConfigurationManager manager, int index)
  {
    ConnectionPrefs pref = (ConnectionPrefs) application.getConnectionPrefs();
    ConnectionPrefs[] prefs = manager.listConnectionPrefs();
    for(int i = 0; i < prefs.length; ++i) {
      if(ConnectionPrefsImpl.compareByUrl(prefs[i], pref))
        return;
    }

    pref.setName(RECENT + index);
    try {
      manager.save(pref);
    }
    catch(Exception e) {
      VortexPlugin.log(e);
    }
  }

  /**
   * Pushes down all the recent connections.
   * @param manager is the recent connections manager
   */
  private void pushAllDown(ConfigurationManager manager)
  {
    for(int i = 1; i < RECENT_MAX_SIZE; ++i) {
      ConnectionPrefs pref = manager.getConnectionPref(RECENT + i);
      if(pref != null)
        pref.setName(RECENT + (i - 1));
      try {
        manager.save(pref);
      }
      catch(Exception e) {
        VortexPlugin.log(e);
      }
    }
    try {
      manager.removeConnection(RECENT + RECENT_MAX_SIZE);
    }
    catch(Exception e) {
      VortexPlugin.log(e);
    }
  }

  public void propertyChange(PropertyChangeEvent evt)
  {
    String prop = evt.getPropertyName();
    if(ServerManager.PROP_APP_CONNECTED.equals(prop)) {
      connectToApplication((Application) evt.getNewValue());
    }
  }

}
