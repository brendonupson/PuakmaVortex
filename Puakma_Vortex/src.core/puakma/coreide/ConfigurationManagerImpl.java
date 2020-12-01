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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This class manages all the configurations. This means that it loads
 * the configuration from the file, and also it can save it to the file.
 *
 * @author Martin Novak
 */
public class ConfigurationManagerImpl implements ConfigurationManager
{
  /**
   * The path to the defualt configuration file relatively to the home directory
   */
  public static final String CONFIG_DIR = ".puakma/.connections";
  
  static final String PREF_HOST = ".host";
  static final String PREF_PORT = ".port";
  static final String PREF_PATH = ".path";
  static final String PREF_SSL = ".ssl";
  static final String PREF_USERNAME = ".userName";
  static final String PREF_PWD = ".pwd";
  static final String PREF_SAVE_PWD = ".savePwd";
  static final String PREF_APPLICATION = ".app";
  static final String PREF_GROUP = ".group";
  
  private File file;

  /**
   * List containing all the loaded preferences.
   */
  private List<ConnectionPrefs> preferences = new ArrayList<ConnectionPrefs>();

  /**
   * Default condstructor loads configuration from the ~/.puakma/.connections
   */
  public ConfigurationManagerImpl()
  {
    String homeDir = System.getProperty("user.home");
    this.file = new File(homeDir);
    file = new File(file, CONFIG_DIR);
  }
  
  private void checkFileConsistency()
  {
    if(file.exists() == false) {
      File dir = file.getParentFile();
      if(dir.exists() == false) {
        dir.mkdirs();
      }
    }
  }
  
  public synchronized void load() throws IOException
  {
    Properties props = new Properties();
    FileInputStream is = null;
    Map<String, ConnectionPrefsImpl> m = new HashMap<String, ConnectionPrefsImpl>();
    try {
      is = new FileInputStream(this.file);
      props.load(is);
      Iterator<Object> it = props.keySet().iterator();
      while(it.hasNext()) {
        String name = (String) it.next();
        name = getConfigName(name);
        if(name == null)
          continue;
        
        if(m.containsKey(name) == false) {
          ConnectionPrefsImpl prefs = new ConnectionPrefsImpl(this);
          prefs.setName(name);
          prefs.setHost(props.getProperty(name + PREF_HOST));
          prefs.setPort(getInt(props, name + PREF_PORT));
          prefs.setDesignerPath(props.getProperty(name + PREF_PATH));
          prefs.setUsingSsl(getBool(props,name + PREF_SSL));
          prefs.setUser(props.getProperty(name + PREF_USERNAME));
          prefs.setPwd(props.getProperty(name + PREF_PWD));
          prefs.setSavePwd(getBool(props, name + PREF_SAVE_PWD));
          prefs.setApplication(props.getProperty(name + PREF_APPLICATION));
          prefs.setGroup(props.getProperty(name + PREF_GROUP));
          if(prefs.quickCheckValidity()) {
            m.put(name, prefs);
            preferences.add(prefs);
          }
        }
      }
    }
    catch(IOException e) {
      // DO NOTHING...
    }
    finally {
      if(is != null) try { is.close(); } catch(Exception e) {}
    }
  }

  private boolean getBool(Properties props, String key)
  {
    String value = props.getProperty(key);
    if("true".equalsIgnoreCase(value))
      return true;
    return false;
  }

  private int getInt(Properties props, String key)
  {
    String value = props.getProperty(key);
    try {
      int ret = Integer.parseInt(value);
      return ret;
    }
    catch(NumberFormatException e) {
      return -1;
    }
  }

  private String getConfigName(String name)
  {
    int index = name.indexOf('.');
    if(index != -1) {
      name = name.substring(0, index);
      return name;
    }
    else
      return null;
  }

  public void save(ConnectionPrefs prefs) throws IOException
  {
    Properties props = new Properties();
    FileInputStream is = null;
    FileOutputStream os = null;
    try {
      is = new FileInputStream(file);
      props.load(is);
    }
    catch(IOException ex) {
      // DO NOTHING... [-;
    }
    finally {
      if(is != null) try { is.close(); } catch(IOException e) {  }
    }
    
    String name = prefs.getName();
    props.setProperty(name + PREF_HOST, prefs.getHost());
    props.setProperty(name + PREF_PORT, Integer.toString(prefs.getPort()));
    props.setProperty(name + PREF_PATH, prefs.getDesignerPath());
    props.setProperty(name + PREF_SSL, prefs.isUsingSsl() ? "true" : "false");
    props.setProperty(name + PREF_USERNAME, prefs.getUser());
    if(prefs.getSavePwd())
      props.setProperty(name + PREF_PWD, prefs.getPwd());
    props.setProperty(name + PREF_APPLICATION, prefs.getApplication());
    props.setProperty(name + PREF_GROUP, prefs.getGroup());
    props.setProperty(name + PREF_SAVE_PWD, Boolean.toString(prefs.getSavePwd()));
    
    try {
      checkFileConsistency();
      os = new FileOutputStream(file);
      props.store(os, null);
      
      ConnectionPrefs p = new ConnectionPrefsImpl();
      p.setName(name);
      int index = preferences.indexOf(p);
      if(index >= 0)
        preferences.remove(index);
      preferences.add(prefs);
    }
    finally {
      if(os != null) try { os.close(); } catch(IOException e) {  }
    }
  }

  public ConnectionPrefs[] listConnectionPrefs()
  {
    synchronized(this) {
      ConnectionPrefs[] prefs = new ConnectionPrefs[preferences.size()];
      return preferences.toArray(prefs);
    }
  }

  public ConnectionPrefs getConnectionPref(String name)
  {
    synchronized(this) {
      Iterator<ConnectionPrefs> it = preferences.iterator();
      while(it.hasNext()) {
        ConnectionPrefs pref = it.next();
        if(pref.getName().equals(name))
          return pref;
      }

      return null;
    }
  }

  public ConnectionPrefs createConnectionPrefs(String name) throws PuakmaCoreException, IOException
  {
    ConnectionPrefsImpl pref = (ConnectionPrefsImpl) getConnectionPref(name);
    if(pref != null)
      throw new PuakmaCoreException("Connection with name " + name + " already exists");
    
    synchronized(this) {
      pref = new ConnectionPrefsImpl();
      pref.setName(name);
      save(pref);
      return pref;
    }
  }

  public int countConnections()
  {
    return preferences.size();
  }

  public void removeConnection(String name) throws PuakmaCoreException, IOException
  {
    Properties props = new Properties();
    FileInputStream is = null;
    FileOutputStream os = null;
    try {
      is = new FileInputStream(file);
      props.load(is);
    }
    catch(IOException ex) {
      // DO NOTHING
    }
    finally {
      if(is != null) try { is.close(); } catch(IOException e) {  }
    }
    
    props.remove(name + PREF_HOST);
    props.remove(name + PREF_PORT);
    props.remove(name + PREF_PATH);
    props.remove(name + PREF_SSL);
    props.remove(name + PREF_USERNAME);
    props.remove(name + PREF_PWD);
    props.remove(name + PREF_APPLICATION);
    props.remove(name + PREF_GROUP);
    props.remove(name + PREF_SAVE_PWD);
    
    try {
      os = new FileOutputStream(file);
      props.store(os, null);
      
      ConnectionPrefs prefs = getConnectionPref(name);
      if(prefs != null) {
        int index = preferences.indexOf(prefs);
        if(index >= 0)
          preferences.remove(index);
      }
    }
    finally {
      if(os != null) try { os.close(); } catch(IOException e) {  }
    }
  }

  public void setFile(File file)
  {
    assert file != null : "File cannot be null";

    this.file = file;
  }

  public void removeAll() throws IOException
  {
    Properties p = new Properties();
    FileOutputStream os = null;
    try {
      os = new FileOutputStream(file);
      p.store(os, getOutputFileHeader());
      preferences.clear();
    }
    finally {
      try{ if(os != null) os.close(); } catch(IOException ex) {  }
    }
  }

  /**
   * Creates a header for the output properties file
   * @return String with the header to put to the output configuration file
   */
  private String getOutputFileHeader()
  {
    return "Connection configuration";
  }
}
