/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Sep 25, 2004
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

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.AuthenticationDialog;
import puakma.coreide.objects2.Server;
import puakma.utils.lang.ListenersList;

/**
 * This class manages all connections to all servers.
 * 
 * @author Martin Novak
 */
public class ServerManager
{
  public static final String PROP_SERVER_CONNECTED = "serverConnected";
  public static final String PROP_APP_CONNECTED = "appConnected";
  public static final String PROP_SERVER_CLOSE = "serverClose";
  public static final String PROP_APP_CLOSE = "appClose";
  public static final String PROP_RESOLVE_INCONSISTENCIES = "resolveInconsistencies";
  
  /**
   * Used to save maximal preference id
   */
  public static final String PREF_MAX_ID = "connPrefs.maxId";
  
  /**
   * Used to save the list of all connection ids
   */
  public static final String PREF_CONN_IDS = "connPrefs.allIds";
  
  /**
   * List of all connected servers. Items are <code>ServerConnection</code> objects.
   * <p>Key is ServerConnection object, but it can be also compared to the string
   * in the shape: http[s]://user@host:port
   */
  private static Hashtable<String, ServerImpl> serverConnections = new Hashtable<String, ServerImpl>();

  /**
   * The default path of the soap designer appplication
   */
  public static final String DEFAULT_SERVER_PATH = "/system/SOAPDesigner.pma";
  
  /**
   * Properties with mime types
   */
  private static Properties mimeProperties;
  
  /**
   * Used for locking static instance.
   */
  private static Object lock = new Object();

  private static List<Application> applicationConnections = new ArrayList<Application>();
  /**
   * Event listeners.
   */
  private static ListenersList listeners = new ListenersList();
  /**
   * If user sets up this authentication dialog, we can ask him to enter new
   * authentication data if needed.
   * TODO: the authentication should be made differently - after each failure
   */
  private static AuthenticationDialog authenticationDialog;

  static {
    loadAllConnectionPrefs();
  }
  
  /**
   * Loads all ConectoinPrefs object from PreferenceStore from PuakmaLibraryManager. 
   */
  private static void loadAllConnectionPrefs()
  {
    // now load all mime types
    mimeProperties = new Properties();
    try {
      ClassLoader cl = ServerManager.class.getClassLoader();
      InputStream fis = cl.getResourceAsStream("puakma/utils/mimetypes.config"); 
      mimeProperties.load(fis);
    }
    catch(IOException e) {
      PuakmaLibraryManager.log(e);
    }
  }

  /**
   * Creates a new server connection if there is no one presented in the list
   * of currently connected servers.
   * 
   * @param pref is <code>ConnectionPrefsReader</code> object with all params of the connection
   * @return ServerConnection object
   */
  public static Server createServerConnection(ConnectionPrefsReader pref)
  {
    ServerImpl server;
    synchronized(ServerManager.class) {
      if((server = serverConnections.get(getServerIdentificationString(pref))) == null) {
        server = new ServerImpl();
        server.init(pref);
        server.setRegistered(true);
      }
      else
        server.setPassword(pref.getPwd());
    }
    return server;
  }
  
  public static void unregisterServer(Server server) throws PuakmaCoreException
  {
    synchronized(ServerManager.class) {
      String id = getServerIdentificationString(server.getUserName(), server.getHost(),
                                                server.usingSsl(), server.getPort());
      ServerImpl server1 = serverConnections.get(id);
      if(server1 != null) {
        Application[] apps = server.listApplications();
        for(int i = 0; i < apps.length; ++i) {
          if(apps[i].isOpen()) {
            throw new PuakmaCoreException("Cannot unregister server with at least one open application");
          }
        }
      }
    }
  }

  /**
   * Returns unique identification of the server string.
   *
   * @param pref is the ConnectionPrefsReader object identifying the server
   * @return String which uniquely identifies the server
   */
  static String getServerIdentificationString(ConnectionPrefsReader pref)
  {
    return getServerIdentificationString(pref.getUser(), pref.getHost(), pref.isUsingSsl(), pref.getPort());
  }
  
  /**
   * Returns unique identification of the server string.
   *
   * @param host is the host of puakma server
   * @param ssl true if connection is using ssl
   * @param port is the http(s) port to connect on
   * @return String which uniquely identifies the server
   */
  static String getServerIdentificationString(String user, String host, boolean ssl, int port)
  {
    StringBuffer sb = new StringBuffer();
    if(ssl)
      sb.append("https://");
    else
      sb.append("http://");
    sb.append(user);
    sb.append("@");
    sb.append(host);
    if(((ssl && port == 443) || (ssl == false && port == 80)) == false) {
      sb.append(":");
      sb.append(port);
    }
    return sb.toString();
  }

  /**
   * This function registers server from <code>ServerManager</code>
   *
   * @param impl is the server to register
   */
  static void registerServer(ServerImpl impl)
  {
    synchronized(lock) {
      String ident = ServerManager.getServerIdentificationString(impl.getUserName(), impl.getHost(),
                                                                 impl.usingSsl(), impl.getPort());
      serverConnections.put(ident, impl);
      listeners.fireEvent(ServerManager.class, PROP_SERVER_CONNECTED, null, impl);
    }
  }
  
  /**
   * This function unregisters server from <code>ServerManager</code>
   *
   * @param impl is the server to register
   */
  static void unregisterServer(ServerImpl impl)
  {
    synchronized(lock) {
      String ident = ServerManager.getServerIdentificationString(impl.getUserName(), impl.getHost(),
                                                                 impl.usingSsl(), impl.getPort());
      serverConnections.remove(ident);
      listeners.fireEvent(ServerManager.class, PROP_SERVER_CLOSE, impl, null);
    }
  }
  
  /**
   * Notifies all listeners that we connected to some application
   *
   * @param application is the newly connected application connection
   */
  static void fireApplicationConnected(Application application)
  {
    assert application != null : "Passing null Application to fire connect event";

    synchronized(lock) {
      applicationConnections.add(application);
      listeners.fireEvent(ServerManager.class, PROP_APP_CONNECTED, null, application);
    }
  }
  
  /**
   * Notifies all listeners that we disconnected some application.
   *
   * @param application is the application connection which we want to disconnect
   */
  static void fireApplicationDisconnected(Application application)
  {
    assert application != null : "Passing null Application to fire disconnect event";

    synchronized(lock) {
      applicationConnections.remove(application);
      listeners.fireEvent(ServerManager.class, PROP_APP_CLOSE, application, null);
    }
  }

  /**
   * Lists all the connected applications.
   *
   * @return array Application objects
   */
  public static Application[] listConnectedApplications()
  {
    synchronized(lock) {
      return applicationConnections 
          .toArray(new Application[applicationConnections.size()]);
    }
  }

  /**
   * @return number of connected applications
   */
  public static int countApplications()
  {
    synchronized(lock) {
      return applicationConnections.size();
    }
  }
  
  public static void addListener(PropertyChangeListener listener)
  {
    listeners.addListener(listener);
  }
  
  public static void removeListener(PropertyChangeListener listener)
  {
    listeners.removeListener(listener);
  }

  /**
   * Enumerates all connected servers.
   */
  public static Server[] listConnectedServers()
  {
    synchronized(lock) {
      Collection<ServerImpl> c = serverConnections.values();
      return c.toArray(new Server[c.size()]);
    }
  }

  public static void setAuthenticatorDialog(AuthenticationDialog dlg)
  {
    authenticationDialog = dlg;
  }

  /**
   * Fires inconsistencies resolution request, so client can resolve all inconsistencies.
   */
  static void fireResolveInconsistencies(InconsistenciesListImpl inconsList)
  {
    listeners.fireEvent(ServerManager.class, PROP_RESOLVE_INCONSISTENCIES, null, inconsList);
  }
}
