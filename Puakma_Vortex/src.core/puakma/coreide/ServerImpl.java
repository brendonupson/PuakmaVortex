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
package puakma.coreide;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import puakma.SOAP.SOAPFaultException;
import puakma.SOAP.SoapProxy;
import puakma.coreide.designer.ApplicationStructureBean;
import puakma.coreide.designer.DatabaseDesigner;
import puakma.coreide.designer.DownloadDesigner;
import puakma.coreide.designer.ServerDesigner;
import puakma.coreide.designer.ServerStructureBean;
import puakma.coreide.designer.XMLServerParser;
import puakma.coreide.designer.XmlLogHandler;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.DatabaseConnection;
import puakma.coreide.objects2.Server;
import puakma.coreide.objects2.ServerEvent;
import puakma.coreide.objects2.ServerListener;
import puakma.coreide.objects2.TornadoDatabaseConstraints;
import puakma.utils.XmlUtils;
import puakma.utils.lang.ListenersList;
import puakma.utils.lang.StringUtil;
import puakma.vortex.VortexPlugin;

/**
 * @author Martin Novak
 */
class ServerImpl implements Server
{
  public static final String SERVER_DESIGNER_EXEC_PATH = "ServerDesigner?WidgetExecute";
  
  public static final String SERVER_DOWNLOADER_EXEC_PATH = "DownloadDesigner?WidgetExecute";
  
  public static final String APP_DESIGNER_EXEC_PATH = "AppDesigner?WidgetExecute";
  
  public static final String DATABASE_DESIGNER_EXEC_PATH = "DatabaseDesigner?WidgetExecute";
  
  /**
   * This is a default size of the log history being kept by the Server class.
   */
  public static final int DEFAULT_MAX_LOG_SIZE = 200;

  private String host;
  private int port;
  private boolean ssl;
  private String userName;
  private String pwd;
  private String x500UserName;
  /**
   * Path to designer application ending with '/'. It can be eg. "/system/SOAPDesigner.pma/"
   */
  private String path;
  private String fullPath;
  private boolean connected = false;
  
  /**
   * If true then the server is fully initialized
   */
  private boolean initialized = false;
  
  /**
   * Server designer to manage connections with the real server.
   */
  private ServerDesigner designer;
  private DownloadDesigner downloadDesigner;
  private DatabaseDesigner databaseClient;
  private List<ServerListener> oldListeners = new ArrayList<ServerListener>();
  private ApplicationImpl[] beans;
  
  private ListenersList listeners = new ListenersList();
  
  private long lastLogItemId = -1;
  private List<ConsoleLogItem> logItems = new ArrayList<ConsoleLogItem>();
  private int logItemsSize = DEFAULT_MAX_LOG_SIZE;
  
  /**
   * Server doesn't need to be globaly registered, so if it is not, no connection
   * events are fired, etc...
   */
  private boolean registered;
  
  /**
   * We can get soap designer from this object
   */
  private DesignerFactory designerFactory = new DesignerFactoryImpl();

  private boolean savePassword;

  /**
   * Lock for environment properties manipulating
   */
  private Object environmentLock = new Object();
  
  /**
   * Server environment variables.
   */
  private Map<String, String> environment = new HashMap<String, String>();

  private TornadoDatabaseConstraintsImpl tornadoConstraints = new TornadoDatabaseConstraintsImpl();

  /**
   * Initializes server from the connection preferences
   *
   * @param prefs is the connection preferences to init server from
   */
  synchronized void init(ConnectionPrefsReader prefs)
  {
    if(connected == true)
      throw new IllegalStateException("Cannot initialize server connection when we are already connected");
    
    this.host = prefs.getHost();
    this.port = prefs.getPort();
    this.userName = prefs.getUser();
    this.path = prefs.getDesignerPath();
    if(this.path.startsWith("/") == false)
      this.path = "/" + this.path;
    if(this.path.endsWith("/") == false)
      this.path = this.path + "/";
    this.ssl = prefs.isUsingSsl();
    
    if(this.ssl)
      this.fullPath = "https://";
    else
      this.fullPath = "http://";
    
    this.fullPath += this.host;
    if(this.port != 0 && ((this.ssl == false && this.port != 80) || (this.ssl && this.port != 443)))
      this.fullPath += ":" + port;
    this.fullPath += this.path;
    if(fullPath.endsWith("/") == false)
      fullPath += "/";

    this.pwd = prefs.getPwd();
    
    this.beans = new ApplicationImpl[0];
    
    // SAVE SAVE PASSWORD STATE
    this.savePassword = prefs.getSavePwd();
    
    // INITIALIZE DESIGNERS
    initConnections();

    // NOW INITIALIZE TORNADO CONSTRAINTS IF NEEDED
    try {
      synchronized(tornadoConstraints) {
        if(tornadoConstraints.isLoaded() == false)
          tornadoConstraints.reload(getServerDesigner());
      }
    }
    catch(Exception ex) {
      // TODO: DO WE REALLY WANT SOME EXCEPTION AT THIS MOMENT???
      throw new RuntimeException(ex);
    }
    
    this.initialized = true;
  }
  
  public void setPassword(String pwd)
  {
    if(this.pwd.equals(pwd) == false) {
      this.pwd = pwd;
      
      initConnections();
    }
  }
  
  /**
   * Reinitializes designers. This should be called only when initializing connection or setting new
   * password.
   */
  private void initConnections()
  {
    // SETUP ALL SOAP CLIENTS
    designer = (ServerDesigner) SoapProxy.createSoapClient(ServerDesigner.class,
                                                           fullPath + SERVER_DESIGNER_EXEC_PATH,
                                                           userName, pwd);
    downloadDesigner = (DownloadDesigner) SoapProxy.createSoapClient(DownloadDesigner.class,
                                                                     fullPath + SERVER_DOWNLOADER_EXEC_PATH,
                                                                     userName, pwd);
    databaseClient = (DatabaseDesigner) SoapProxy.createSoapClient(DatabaseDesigner.class,
                                                                   fullPath + DATABASE_DESIGNER_EXEC_PATH,
                                                                   userName, pwd);
  }
  
  /**
   * This function sets the new designer factory
   * @param factory
   */
  void setDesignerFactory(DesignerFactory factory)
  {
    this.designerFactory = factory;
  }
  
  DesignerFactory getDesignerFactory()
  {
    return this.designerFactory;
  }

  /**
   * Checks if the application is connected. If not, it tries to connect it. If connecting
   * to the server fails, exception is thrown. Also fails when server connection is not
   * initialized.
   *
   * @throws PuakmaCoreException
   */
  private synchronized void checkConnected() throws PuakmaCoreException, IOException
  {
    checkInitialized();

    if(connected == false) {
      refresh();
    }
  }

  /**
   * This function checks if the server connection is initialized. If not, throws exception.
   * 
   * @throws PuakmaCoreException is thrown when server is not initialized
   */
  private void checkInitialized() throws PuakmaCoreException
  {
    if(initialized == false)
      throw new PuakmaCoreException("Already connected");
  }

  public synchronized void refresh() throws PuakmaCoreException, IOException
  {
    checkInitialized();
    
    // if we are not connected, we should do that
    if(connected == false) {
      try {
        designer.initiateConnection();
      }
      catch(Exception e) {
        throw PuakmaLibraryUtils.handleException(e);
      }
      
      connected = true;
    }
    
    ServerStructureBean sBean;
    try {
      sBean = getServerInfo(designer);
    }
    catch(Exception e) {
      throw PuakmaLibraryUtils.handleException(e);
    }
    
    List<ApplicationImpl> appsList = new ArrayList<ApplicationImpl>();
    for(ApplicationStructureBean bean : sBean.apps) {
      
      // IN SOME CASES BEAN CAN BE INVALID
      if(isInvalidApplicationBean(bean))
        continue;
      
      ApplicationImpl app = new ApplicationImpl(this);
      app.setName(bean.appName);
      app.setGroup(bean.appGroup);
      app.setTemplateName(bean.templateName);
      app.setInheritFrom(bean.inheritFrom);
      app.setDescription(bean.description);
      app.setId(bean.appId);

      app.setDirty(false);
      appsList.add(app);
    }

    ApplicationImpl[] apps = new ApplicationImpl[appsList.size()];
    apps = appsList.toArray(apps);
    fireRefresh(apps);
    
    // ALSO REFRESH ENVIRONMENT VARIABLES
    try {
      synchronized(environmentLock) {
        String[][] envs;
        envs = designer.listEnvironmentProperties();
        setupEnvironment(envs);
      }
    }
    catch(SOAPFaultException e) {
      throw PuakmaLibraryUtils.handleException(e);
    }
  }

  /**
   * Filters out invalid applications like with empty name, etc...
   */
  private boolean isInvalidApplicationBean(ApplicationStructureBean bean)
  {
    if(bean.appName == null || bean.appName.length() == 0)
      return true;
    
    return false;
  }

  private static ServerStructureBean getServerInfo(ServerDesigner designer) throws IOException, SOAPFaultException, ParserConfigurationException, SAXException
  {
    String str = designer.getServerInfo();
    
    // log the xml to the file
    //
    if(VortexPlugin.DEBUG_MODE) {
      Calendar c = Calendar.getInstance();
      FileOutputStream fos = null;
      try {
        String fileName = "server_" + c.get(Calendar.MONTH) + '_' + c.get(Calendar.DAY_OF_MONTH)
                          + '_' + c.get(Calendar.HOUR_OF_DAY) + '_' + c.get(Calendar.MINUTE) + ".log";
        fos = new FileOutputStream(new File(fileName));
        byte[] bytes = str.getBytes("UTF-8");
        fos.write(bytes);
      }
      catch(Exception e) {
        //VortexPlugin.log(e);
    	  e.printStackTrace();
      }
      finally {
        if(fos != null)
          try { fos.close(); }
          catch(IOException e2) { }
      }
    }
    
    SAXParserFactory saxFactory = SAXParserFactory.newInstance();
    SAXParser parser;
    
      parser = saxFactory.newSAXParser();
      XMLServerParser handler = new XMLServerParser();

      InputSource is = new InputSource(new StringReader(str));
      parser.parse(is, handler);

      return handler.getServer();
    
  }

  /**
   * This function fires refresh events in the whole server.
   */
  private void fireRefresh(ApplicationImpl[] apps)
  {
    int count = beans.length;
    List<Application> addedApps = new ArrayList<Application>();
    for(int i = 0; i < apps.length; ++i) {
      Application app = apps[i];
      boolean found = false;
      // find application by id:
      for(int j = 0; j < count; ++j) {
        if(app.getId() == beans[j].getId()) {
          beans[j].updateFromBean(app);
          found = true;
          break;
        }
      }
      
      if(found == false)
        addedApps.add(app);
    }
    
    // search for applications which has been removed
    boolean[] removed = new boolean[beans.length];
    Arrays.fill(removed, true);
    
    for(int i = 0; i < beans.length; ++i) {
      for(int j = 0; j < apps.length; ++j) {
        if(beans[i].getId() == apps[j].getId()) {
          removed[i] = false;
          break;
        }
      }
    }
    
    // handle application removing
    count = 0; // count rest applications
    for(int i = 0; i < beans.length; ++i) {
      if(removed[i] == true) {
      }
      else
        count++;
    }
    
    // copy array to the new location
    apps = new ApplicationImpl[count];
    for(int i = 0, j = 0; i < beans.length; ++i) {
      if(removed[i] == false) {
        apps[j] = beans[i];
        j++;
      }
    }
    beans = apps;

    // and at the end add new applications
    ApplicationImpl[] newApps = new ApplicationImpl[beans.length + addedApps.size()];
    System.arraycopy(beans, 0, newApps, 0, beans.length);
    System.arraycopy(addedApps.toArray(), 0, newApps, beans.length, addedApps.size());
    beans = newApps;
  }

  /**
   * Copies array with all the server listeners.
   * 
   * @return array with all listeners
   */
  private ServerListener[] listListeners()
  {
    return oldListeners.toArray(new ServerListener[oldListeners.size()]);
  }

  public void close()
  {
    connected = false;
    
    // CLEAR INFORMATION ABOUT THE LOG
    lastLogItemId = -1;
    logItems = new ArrayList<ConsoleLogItem>();
    logItemsSize = DEFAULT_MAX_LOG_SIZE;
    
    listeners.fireEvent(this, PROP_CLOSE, null, null);
    
    if(registered) {
      setRegistered(false);
    }
  }

  public String getUserName()
  {
    return userName;
  }
  
  public String getX500UserName()
  {
    return x500UserName;
  }

  public String getHost()
  {
    return host;
  }

  public int getPort()
  {
    return port;
  }

  public boolean usingSsl()
  {
    return ssl;
  }

  public String getPathToDesigner()
  {
    return path;
  }

  public String getFullPathToDesigner()
  {
    return fullPath;
  }

  public synchronized Application[] listApplications()
  {
    Application[] apps = new Application[beans.length];
    System.arraycopy(beans, 0, apps, 0, beans.length);
    return apps;
  }

  public synchronized Application getApplication(String appGroup, String appName) throws PuakmaCoreException, IOException
  {
    checkConnected();
    if(appGroup == null)
      appGroup = "";

    for(int i = 0; i < beans.length; ++i) {
      if(StringUtil.compareStrings(appGroup, beans[i].getGroup()) && StringUtil.compareStrings(appName, beans[i].getName()))
        return beans[i];
    }

    return null;
  }

  public synchronized Application getApplicationBean(long id) throws PuakmaCoreException, IOException
  {
    checkConnected();

    for(int i = 0; i < beans.length; ++i) {
      if(id == beans[i].getId())
        return beans[i];
    }

    return null;
  }

  public void addListener(ServerListener listener)
  {
    Application[] apps;
    synchronized(listeners) {
      if(oldListeners.contains(listener))
        return;

      oldListeners.add(listener);
      apps = listApplications();
    }

    for(int i = 0; i < apps.length; ++i) {
      try {
        listener.addApplication(this, apps[i]);
      }
      catch(Exception e) {
        PuakmaLibraryManager.log(e);
      }
    }
  }

  public void removeListener(ServerListener listener)
  {
    synchronized(listeners) {
      oldListeners.remove(listener);
      try {
        listener.closed(this);
      }
      catch(Exception e) {
        PuakmaLibraryManager.log(e);
      }
    }
  }

  public void importPmx(String group, String appName, File file) throws PuakmaCoreException, IOException
  {
    checkConnected();

    if(file.canRead() == false)
      throw new PuakmaCoreException("Cannot read from the file" + file.toString());
    FileInputStream fis = null;
    
    try {
      ApplicationImpl app = null;
      byte[] bytes = new byte[(int) file.length()];
      fis = new FileInputStream(file);
      fis.read(bytes);
      long id = downloadDesigner.uploadPmx(group, appName, bytes);

      synchronized(this) {
        ApplicationImpl[] local = new ApplicationImpl[beans.length + 1];
        System.arraycopy(beans, 0, local, 0, beans.length);
        app = local[local.length - 1] = new ApplicationImpl(this);
        app.setId(id);
        app.setName(appName);
        app.setGroup(group);
        beans = local;
      }

      fireAdd(app);
    }
    catch(Exception e) {
      throw new PuakmaCoreException(e.getLocalizedMessage(), e);
    }
    finally {
        try { if(fis != null) fis.close(); } catch(IOException e) {  }
    }
  }

  /**
   * Fires add application event at all server listeners
   *
   * @param app is the application which has been added.
   */
  private void fireAdd(ApplicationImpl app)
  {
    ServerListener[] ls = listListeners();
    for(int i = 0; i < ls.length; ++i) {
      ServerEventImpl event = new ServerEventImpl(ServerEvent.EV_APPLICATION_ADDED, this);
      event.setApplication(app);
      try {
        ls[i].serverEvent(event);
      }
      catch(Exception e) {
        PuakmaLibraryManager.log(e);
      }
    }
  }
  
  public void fireRemove(ApplicationImpl app)
  {
    ServerListener[] ls = listListeners();
    for(int i = 0; i < ls.length; ++i) {
      ServerEventImpl event = new ServerEventImpl(ServerEvent.EV_APPLICATION_REMOVE, this);
      event.setApplication(app);
      try {
        ls[i].serverEvent(event);
      }
      catch(Exception e) {
        PuakmaLibraryManager.log(e);
      }
    }
  }

  public void exportPmx(String group, String appName, File file, boolean exportSource) throws PuakmaCoreException, IOException
  {
    checkConnected();

    try {
      file.createNewFile();
    }
    catch(IOException e) {
      String str = "Cannot create the export file " + file.toString();
      throw new PuakmaCoreException(str, e);
    }

    if(file.canWrite() == false) {
      String str = "Cannot write to the file" + file.toString();
      throw new PuakmaCoreException(str);
    }
    
    Application app = getApplication(group, appName);
    if(app == null) {
      throw new PuakmaCoreException("Application to import doesn't exists");
    }

    FileOutputStream fos = null;
    try {
      byte[] bytes = downloadDesigner.downloadPmx(app.getId(), exportSource);
      fos = new FileOutputStream(file);
      fos.write(bytes);
    }
    catch(Exception e) {
      PuakmaLibraryManager.log(e);
      throw new PuakmaCoreException(e.getLocalizedMessage(), e);
    }
    finally {
      try { if(fos != null) fos.close(); } catch(Exception ex) {  }
    }
  }

  public String getHashString()
  {
    return getFullPathToDesigner();
  }

  /**
   * Returns password to management soap widgets.
   *
   * @return String with password for access to SOAPDesigner application
   */
  public String getPassword()
  {
    return pwd;
  }
  
  synchronized void fireApplicationDisconnect(Application application)
  {
    // AT FIRST RUN THRU ALL APPLICATIONS, AND CHECK WHICH ONES ARE STILL OPEN
    Application[] apps;
    apps = listApplications();
    boolean allClosed = true;
    for(int i = 0; i < apps.length; ++i) {
      if(apps[i].isOpen())
        allClosed = false;
    }
    // IF ALL APPLICATIONS HAS BEEN CLOSED, CLOSE ALSO THE SERVER
    if(allClosed) {
      close();
    }
    
    ServerListener[] ls = listListeners();
    for(int i = 0; i < ls.length; ++i) {
      ServerEventImpl event = new ServerEventImpl(ServerEvent.EV_APPLICATION_DISCONNECT, this);
      event.setApplication((ApplicationImpl) application);
      try {
        ls[i].serverEvent(event);
      }
      catch(Exception e) {
        PuakmaLibraryManager.log(e);
      }
    }
  }

  void fireApplicationConnect(ApplicationImpl application)
  {
    ServerListener[] ls = listListeners();
    for(int i = 0; i < ls.length; ++i) {
      ServerEventImpl event = new ServerEventImpl(ServerEvent.EV_APPLICATION_CONNECT, this);
      event.setApplication(application);
      try {
        ls[i].serverEvent(event);
      }
      catch(Exception e) {
        PuakmaLibraryManager.log(e);
      }
    }
    
    if(isRegistered())
      ServerManager.fireApplicationConnected(application);
  }

  /**
   * This function notifies about removing the application. The current
   * implementation calls refresh() here for getting list of the applications
   * on the server. Also fires the appropriate events.
   *
   * @param impl
   */
  void notifyRemove(ApplicationImpl impl)
  {
    try {
      refresh();
    }
    catch(Exception e) {
      PuakmaLibraryManager.log(e);
    }
    
    fireRemove(impl);
  }
  
  /**
   * Checks if the server connection is registered in the global list of servers.
   * This property is inherited to all of the applications, etc...
   *
   * @return true if the server is globaly registered in the <code>ServerManager</code>
   */
  boolean isRegistered()
  {
    return registered;
  }
  
  void setRegistered(boolean registered)
  {
    synchronized(this) {
      this.registered = registered;
      if(registered == true)
        ServerManager.registerServer(this);
      else
        ServerManager.unregisterServer(this);
    }
  }
  
  /**
   * Gets the ServerDesigner, so we can be able to make some functions
   * on the server even outside ServerImpl object.
   *
   * @return ServerDesigner object
   */
  ServerDesigner getServerDesigner()
  {
    return designer;
  }

  public String[] pingDatabase(DatabaseConnection dbo) throws PuakmaCoreException
  {
    try {
      return designer.pingDatabaseServer(dbo.getDriverClass(), dbo.getUserName(),
                            dbo.getPassword(), dbo.getDatabaseUrl(), dbo.getDatabaseName(),
                            dbo.getDatabaseUrlOptions());
    }
    catch(Exception e) {
      throw PuakmaLibraryUtils.handleException(e);
    }
  }

  public String[] ping() throws PuakmaCoreException, IOException
  {
    try {
      String[] ret = designer.ping();
      if(ret != null && ret.length == 2)
        return ret;
      else
        throw new PuakmaCoreException("Invalid response from server");
    }
    catch(SOAPFaultException e) {
      throw PuakmaLibraryUtils.handleException(e);
    }
  }

  /**
   * Generates ConnectionPrefsReader object from the this server.
   *
   * @return <code>ConnectionPrefsReader</code> object. Note that group, and application fields are
   *         not filed
   */
  public ConnectionPrefs getConnectionPrefs()
  {
    ConnectionPrefs prefs = new ConnectionPrefsImpl();
    Random r = new Random();
    r.setSeed(Calendar.getInstance().getTimeInMillis());
    int randomInt = r.nextInt();
    
    prefs.setName(Integer.toHexString(randomInt));
    prefs.setDesignerPath(getPathToDesigner());
    prefs.setHost(getHost());
    prefs.setPort(getPort());
    prefs.setPwd(getPassword());
    prefs.setUser(getUserName());
    prefs.setUsingSsl(this.ssl);
    prefs.setSavePwd(this.savePassword);
    
    return prefs;
  }

  public DatabaseDesigner getDatabaseClient()
  {
    return databaseClient;
  }
  
  public void addListener(PropertyChangeListener listener)
  {
    listeners.addListener(listener);
  }
  
  public void removeListener(PropertyChangeListener listener)
  {
    listeners.removeListener(listener);
  }

  public int getMaxLogSize()
  {
    synchronized(logItems) {
      return this.logItemsSize;
    }
  }
  
  public void setMaxLogSize(int size)
  {
    synchronized(logItems) {
      this.logItemsSize = size;
    }
  }

  public ConsoleLogItem[] getKnownHistory(int lastItems)
  {
    synchronized(logItems) {
      ConsoleLogItem[] items = new ConsoleLogItem[logItems.size()];
      items = logItems.toArray(items);
      return items;
    }
  }
  
  public void refreshLog() throws IOException, PuakmaCoreException
  {
    try {
      ConsoleLogItem[] items;
      
      synchronized(logItems) {
        String xml;
        if(lastLogItemId == -1)
          xml = designer.getLastLogItems(10, -1);
        else
          xml = designer.getLastLogItems(-1, lastLogItemId);
        
        XmlLogHandler handler = new XmlLogHandler();
        XmlUtils.parseXml(handler, xml);
        items = handler.getLogItems();
        
        if(items.length == 0)
          return;
      
        // REMOVE SOME ITEMS
        int restItemsCount = logItemsSize - items.length;
        for(int i = 0; i < restItemsCount; ++i)
          logItems.remove(0);
        
        // AND ADD SOME TOO
        for(int i = 0; i < items.length; ++i) {
          ConsoleLogItem item = items[i];
          long id = item.getId();
          if(id > lastLogItemId)
            lastLogItemId = id;
          
          logItems.add(item);
        }
      }
      
      listeners.fireEvent(this, PROP_LOG_ITEM_ADDED, null, items);
    }
    catch(SOAPFaultException e) {
      throw new PuakmaCoreException(e);
    }
    catch(SAXException e) {
      throw new PuakmaCoreException(e);
    }
    catch(ParserConfigurationException e) {
      throw new PuakmaCoreException(e);
    }
  }

  public String executeCommand(String command) throws IOException, PuakmaCoreException
  {
    try {
      return designer.executeCommand(command);
    }
    catch(SOAPFaultException e) {
      throw new PuakmaCoreException(e);
    }
  }

  public String getEnvironmentProperty(String name)
  {
    synchronized(environmentLock) {
      return environment.get(name);
    }
  }

  public String[] listEnvironment()
  {
    synchronized(environmentLock) {
      String[] ret = new String[environment.size()];
      return environment.keySet().toArray(ret);
    }
  }

  void setupEnvironment(String[][] envs)
  {
    Map<String, String> m = new HashMap<String, String>();
    for(int i = 0; i < envs.length; ++i) {
      String name = envs[i][0];
      String value = envs[i][1];
      if(value == null)
        value = StringUtil.EMPTY_STRING;
      m.put(name, value);
    }
    // TODO: create here some property change stuff???
    environment = m;
  }

  public TornadoDatabaseConstraints getTornadoDatabaseConstraints()
  {
    return tornadoConstraints;
  }
}
