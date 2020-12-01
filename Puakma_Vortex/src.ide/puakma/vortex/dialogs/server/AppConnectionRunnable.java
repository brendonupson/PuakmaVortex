/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    18/07/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.dialogs.server;

import org.eclipse.core.runtime.IProgressMonitor;

import puakma.coreide.ConnectionPrefs;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.Server;
import puakma.vortex.project.ProjectManager;
import puakma.vortex.swt.DialogBuilder2;

public class AppConnectionRunnable implements AppSelectionDialogRunnable
{
  private static final String TITLE = "Open Tornado Application";
  
  private Application application;

  public String getDescription()
  {
    return "Open Tornado application";
  }

  public String getTitle()
  {
    return TITLE;
  }

  public String getWindowTitle()
  {
    return TITLE;
  }

  public void run() throws Exception
  {
    
  }

  public String getOkButtonText()
  {
    return "Open";
  }

  public void run(IProgressMonitor monitor) throws Exception
  {
    String name = application.getName();
    String group = application.getGroup();
    Server server = application.getServer();
    
    ConnectionPrefs prefs = server.getConnectionPrefs();
    prefs.setGroup(group);
    prefs.setApplication(name);

    ProjectManager.connectToApplication(prefs, monitor);    
  }

  public void setSelectedApplication(Application application)
  {
    this.application = application;
  }
  
  

  public void appendCustomControls(DialogBuilder2 builder)
  {
  }

  public String validateCustomControls()
  {
    return null;
  }

  public void setController(AppSelectionDialogController controller)
  {
    
  }

  public void gatherData()
  {
    
  }
}
