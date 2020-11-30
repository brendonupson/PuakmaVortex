/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    May 11, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide.objects2;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import puakma.coreide.ConnectionPrefs;
import puakma.coreide.ConsoleLogItem;
import puakma.coreide.PuakmaCoreException;

/**
 * This class encapsulates connection to the server. If you want to create new connection
 * to application, or do some global application changes, do them through this class.
 *
 * @author Martin Novak
 */
public interface Server
{
  /**
   * This property is fired when an array of log items is added.
   */
  public static final String PROP_LOG_ITEM_ADDED = "logItem";
  
  /**
   * This is fired when we close the connection to the server.
   */
  public static final String PROP_CLOSE = "close";
  
  public void refresh() throws PuakmaCoreException, IOException;
  
  public void close() throws PuakmaCoreException;
  
  /**
   * Returns user name of the application
   *
   * @return string with user name
   */
  public String getUserName();
  
  /**
   * Gets X500 user name of logged user.
   * 
   * @return x500 user name for the user.
   */
  public String getX500UserName();
  
  public String getHost();
  
  public int getPort();
  
  public boolean usingSsl();
  
  public String getPathToDesigner();

  /**
   * Gets the full path to the designer application. So if you want to use some design widget,
   * you have to append the widget name.
   * 
   * @return full url to the SOAPDesigner application
   */
  public String getFullPathToDesigner();
  
  public Application[] listApplications();
  
  public Application getApplication(String appGroup, String appName)
                                    throws PuakmaCoreException, IOException;
  
  public Application getApplicationBean(long id) throws PuakmaCoreException, IOException;
  
  public void addListener(ServerListener listener);
  
  public void removeListener(ServerListener listener);
  
  public void addListener(PropertyChangeListener listener);
  
  public void removeListener(PropertyChangeListener listener);
  
  public void importPmx(String group, String appName, File file) throws PuakmaCoreException, IOException;
  
  /**
   * This function exports pmx file from the server
   *
   * @param group is the application group
   * @param appName is the application name
   * @param file is the file to which we should save the application
   * @param exportSource if true, the source code will be also exported
   * @throws PuakmaCoreException is thrown when something goes wrong
   * @throws IOException 
   */
  public void exportPmx(String group, String appName, File file, boolean exportSource)
                        throws PuakmaCoreException, IOException;

  /**
   * This generates hash string which uniquely identifies server among the rest of the servers
   *
   * @return String with unique identifier about the server connection.
   */
  public String getHashString();
  
  /**
   * This function pings database connection not dependingly if the database
   * connection exists or not (so it can be pinged even if it's working copy.
   * 
   * @param dbo is the database connection object 
   * @return array of String documenting the ping operation
   * @throws PuakmaCoreException
   */
  public String[] pingDatabase(DatabaseConnection dbo) throws PuakmaCoreException;

  /**
   * This function pings the server.
   *
   * @return array with items: server's version string, soap designer's version string
   * @throws PuakmaCoreException 
   * @throws IOException if there is some IO error connecting to the server. Note that this
   * is the only place where IOException is being thrown
   */
  public String[] ping() throws PuakmaCoreException, IOException;

  /**
   * This sets a new password to the server. This might actually happen if the user sets invalid
   * password, and then changes it.
   * 
   * @param password is the new password for the server
   */
  public void setPassword(String password);
  
  /**
   * Sets the maximal number of items which are kept by the implementing class.
   * 
   * @param size is the new history size
   */
  public void setMaxLogSize(int size);
  
  public int getMaxLogSize();
  
  public ConsoleLogItem[] getKnownHistory(int lastItems);
  
  public void refreshLog() throws IOException, PuakmaCoreException;
  
  /**
   * Executes command on the server, and returns response.
   * 
   * @param command is the Puakma's server command
   * @return String with response from the server
   * @throws IOException 
   * @throws PuakmaCoreException 
   */
  public String executeCommand(String command) throws IOException, PuakmaCoreException;
  
  /**
   * Lists all names of available environment properties.
   */
  public String[] listEnvironment();
  
  /**
   * Gets the environment property from the server. Note that we expect that 
   * @param name of the property
   */
  public String getEnvironmentProperty(String name);
  
  public TornadoDatabaseConstraints getTornadoDatabaseConstraints();

  /**
   * Returns a cop of the current connection preferences.
   */
  public ConnectionPrefs getConnectionPrefs();
}
