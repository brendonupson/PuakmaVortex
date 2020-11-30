/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Oct 6, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide;

import puakma.coreide.designer.AppDesigner;
import puakma.coreide.designer.DatabaseDesigner;

/**
 * This interface is used for setting designer objects to the server, application, etc...
 *
 * @author Martin Novak
 */
interface DesignerFactory
{
  /**
   * Sets up soap designer connection preferences.
   * 
   * @param prefs is the wanted connection preferences
   */
  public void setupConnectionPreferences(ConnectionPrefs prefs);
  
  /**
   * Creates a new instance of the application designer.
   * @param prefs is the connection preferences to load designer for
   * @return AppDesigner object
   */
  public AppDesigner newAppDesigner(ConnectionPrefs prefs);
  
  /**
   * Creates a new instance of application designer class
   * 
   * @param baseSOAPDesignerPath is the base path to the SOAPDesigner application
   * @param userName user name
   * @param pwd password
   * @return AppDesigner object
   */
  public AppDesigner newAppDesigner(String baseSOAPDesignerPath, String userName, String pwd);
  
  /**
   * Creates a new DatabaseDesigner object filled with connection preferences from function setup.
   * @return DatabaseDesigner object
   */
  public DatabaseDesigner newDbDesigner();
  
  /**
   * Creates a new instance of DatabaseDesigner class.
   * @param prefs is the connection preferences to initialize designer
   * @return DatabaseDesigner object
   */
  public DatabaseDesigner newDbDesigner(ConnectionPrefs prefs);
  
  /**
   * Creates a new instance of database designer class
   * 
   * @param baseSOAPDesignerPath is the base path to the SOAPDesigner application
   * @param user user name
   * @param pwd password
   * @return DatabaseDesigner object
   */
  public DatabaseDesigner newDbDesigner(String baseSOAPDesignerPath, String user, String pwd);
}
