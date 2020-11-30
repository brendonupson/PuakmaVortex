/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    23.10.2004  
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

import puakma.utils.lang.StringUtil;

/**
 * Connection preferences is class which stores information about connection
 * preferences to server.
 * 
 * <p>How does it store data. It store datas to IStorePreference from PuakmaLibraryManager
 * class. Connection has it's own id which is assigned by ServerManager sclass.
 * ServerManager also maintains list of available ids. This class stores all preferences
 * under key conPref.id.SOME_PROPERTY_NAME.
 * 
 * @author Martin Novak
 */
public class ConnectionPrefsImpl implements ConnectionPrefs
{
  private String name = "";
  private String host = "";
  private int port = 80;
  private String designerPath = "";
  private boolean ssl = false;
  private String userName = "";
  private String pwd = "";
  private String group = "";
  private String appName = "";
  private boolean savePwd;
  private boolean modified;
  private ConfigurationManagerImpl configManager;

  /**
   * Disable creating object. You can obtain this object only from ServerManager
   * class. But btw. the constrctor sets some values, so user could connect
   * at the first try.
   */
  public ConnectionPrefsImpl()
  {
    name = "default";
    host = "localhost";
    designerPath = DEFAULT_PATH;
    userName = "SysAdmin";
    savePwd = true;
    modified = false;
  }
  
  /**
   * Creates a copy of the existing connection preferences.
   * @param prefs is the connection to copy settings from
   */
  public ConnectionPrefsImpl(ConnectionPrefsReader prefs)
  {
    this();
    
    copyFrom(prefs);
  }
  
  public ConnectionPrefsImpl(ConfigurationManagerImpl configManager)
  {
    this();
    
    this.configManager = configManager;
  }

  public void copyFrom(ConnectionPrefsReader prefs)
  {
    setName(prefs.getName());
    setApplication(prefs.getApplication());
    setGroup(prefs.getGroup());
    
    setHost(prefs.getHost());
    setPort(prefs.getPort());
    setUsingSsl(prefs.isUsingSsl());
    setDesignerPath(prefs.getDesignerPath());
    
    setUser(prefs.getUser());
    setPwd(prefs.getPwd());
    setSavePwd(prefs.getSavePwd());
  }

  /**
   * This code generates String object in the shape: http[s]://user@host[:port]
   *
   * @return String with server name and informations.
   */
  public synchronized String getServerString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("http");
    if(ssl)
      sb.append('s');
    sb.append("://");
    sb.append(userName);
    sb.append('@');
    sb.append(host);
    if(! ((ssl && port == 443) || (ssl == false && port == 80) || port == 0) ) {
      sb.append(':');
      sb.append(port);
    }
    
    return sb.toString();
  }
  
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append(name);
    sb.append(" - ");
    sb.append(userName);
    sb.append('@');
    sb.append(host);
    return sb.toString();
  }

  public String getHost()
  {
    return host;
  }

  public void setHost(String host)
  {
    this.host = StringUtil.safeString(host);
  }

  public String getUser()
  {
    return userName;
  }

  public void setUser(String user)
  {
    this.userName = StringUtil.safeString(user);
  }

  public int getPort()
  {
    return port;
  }

  public void setPort(int port)
  {
    this.port = port;
  }

  public String getPwd()
  {
    return pwd;
  }

  public void setPwd(String pwd)
  {
    this.pwd = StringUtil.safeString(pwd);
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = StringUtil.safeString(name);
  }

  public String getDesignerPath()
  {
    return designerPath;
  }

  public void setDesignerPath(String path)
  {
    designerPath = StringUtil.safeString(path);
  }

  public boolean isUsingSsl()
  {
    return ssl;
  }

  public void setUsingSsl(boolean useSsl)
  {
    ssl = useSsl;
  }

  /**
   * This function checks validity of the connection settings. Corrects some mistakes,
   * and if some mistakes are icorrectible, returns false.
   * 
   * <p>Note that now it always return true
   * 
   * @return true if values are somehow correct
   */
  public boolean quickCheckValidity()
  {
    if(port == -1 || port == 0) {
      if(ssl)
        port = 443;
      else
        port = 80;
    }
    return true;
  }

  public boolean equals(Object obj)
  {
    if(obj instanceof ConnectionPrefs) {
      ConnectionPrefs prefs = (ConnectionPrefs) obj;
      return prefs.getName().equals(getName());
    }
    return getName().equals(obj);
  }

  public void setApplication(String application)
  {
    this.appName = application;
  }

  public void setGroup(String group)
  {
    this.group = group;
  }

  public String getApplication()
  {
    return appName;
  }

  public String getGroup()
  {
    return group;
  }

  public void setSavePwd(boolean savePwd)
  {
    this.savePwd = savePwd;
  }

  public boolean getSavePwd()
  {
    return savePwd;
  }

  /**
   * This function creates a user-readable path to application. THe url has the for:
   * <code>http[s]://host[:port][/group]/application</code>.
   * @param prefs is the url we want to use
   * 
   * @return String with application url
   */
  public static String getFullApplicationUrl(ConnectionPrefsReader prefs)
  {
    StringBuffer sb = new StringBuffer();
    
    if(prefs.isUsingSsl())
      sb.append("https://");
    else
      sb.append("http://");
    sb.append(prefs.getHost());
    if(((prefs.getPort() == 80 && prefs.isUsingSsl() == false) || (prefs.getPort() == 443 && prefs.isUsingSsl())) == false) {
      sb.append(":");
      sb.append(prefs.getPort());
    }
    if(prefs.getGroup() != null && prefs.getGroup().length() > 0) {
      sb.append("/");
      sb.append(prefs.getGroup());
    }
    sb.append("/");
    sb.append(prefs.getApplication());
    
    return sb.toString();
  }
  
  /**
   * This function compares two ConnectionPrefs by content, not the name.
   * 
   * @param pref1 is the first compared connection
   * @param pref2 is the second compared connection
   * @return true if those two connections are the same
   */
  public static boolean compareByUrl(ConnectionPrefsReader pref1, ConnectionPrefsReader pref2)
  {
    String normalizedPath1 = pref1.getDesignerPath();
    if(normalizedPath1.endsWith("/") == false)
      normalizedPath1 += "/";
    String normalizedPath2 = pref2.getDesignerPath();
    if(normalizedPath2.endsWith("/") == false)
      normalizedPath2 += "/";
    if(StringUtil.compareStrings(pref1.getUser(), pref2.getUser()) &&
       StringUtil.compareStringsIgnoreCase(pref1.getHost(), pref2.getHost()) &&
       StringUtil.compareStringsIgnoreCase(normalizedPath1, normalizedPath2) &&
       pref1.getPort() == pref2.getPort() &&
       pref1.isUsingSsl() == pref2.isUsingSsl() &&
       StringUtil.compareStringsIgnoreCase(pref1.getApplication(), pref2.getApplication()) &&
       StringUtil.compareStringsIgnoreCase(pref1.getGroup(), pref2.getGroup()))
    {
      return true;
    }
    
    return false;
  }

  public boolean isModified()
  {
    return modified;
  }

  public void setModified(boolean modified)
  {
    this.modified = modified;
  }

  public ConfigurationManager getConfigurationManager()
  {
    return configManager;
  }
}
