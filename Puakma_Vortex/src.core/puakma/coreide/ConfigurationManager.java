/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Sep 29, 2005
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

import java.io.File;
import java.io.IOException;

/**
 * This class is used as a manager for connection configurations. It can be used to handle
 * both connection to server settings, and also application connection settings. So you can
 * handle favorites, old connection, and server connection list with this class.
 *
 * @author Martin Novak
 */
public interface ConfigurationManager
{
  /**
   * This function sets the file to which we will save/load preferences
   *
   * @param file is the file to load/save preferences
   */
  public void setFile(File file);
  
  public void load() throws IOException;

  public void save(ConnectionPrefs prefs) throws IOException;

  public ConnectionPrefs[] listConnectionPrefs();

  /**
   * Gets the connection with the specified name
   *
   * @param name is the name of the connection we want
   * @return ConnectionPrefs object or null if there is no such connection with
   *         that name
   */
  public ConnectionPrefs getConnectionPref(String name);

  /**
   * Creates a new connection preference. If there is some preference with such
   * a name, throws PuakmaCoreException.
   * 
   * @param name is the name of the connection we want
   * @return ConnectionPrefs object
   * @throws PuakmaCoreException if name already exists
   * @throws IOException 
   */
  public ConnectionPrefs createConnectionPrefs(String name) throws PuakmaCoreException, IOException;

  /**
   * @return number of available connection preferences
   */
  public int countConnections();

  /**
   * Removes connection
   * @param name is the name of the connection to remove
   * @throws PuakmaCoreException 
   * @throws IOException 
   */
  public void removeConnection(String name) throws PuakmaCoreException, IOException;

  /**
   * Removes all connections.
   * @throws IOException 
   */
  public void removeAll() throws IOException;
}
