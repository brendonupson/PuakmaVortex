/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    May 22, 2005
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

import puakma.SOAP.SoapProxy;
import puakma.coreide.designer.AppDesigner;
import puakma.coreide.designer.DatabaseDesigner;
import puakma.coreide.objects2.Database;
import puakma.coreide.objects2.DatabaseConnection;
import puakma.utils.lang.StringUtil;

/**
 * This class implements database connection management in the ide.
 *
 * @author Martin Novak
 */
class DatabaseConnectionImpl extends ApplicationObjectImpl implements DatabaseConnection
{
  /**
   * Name of the database, NOT THE CONNECTION!!!
   */
  private String databaseName = "";
  /**
   * Prefix of the database url
   */
  private String databaseUrl = "";
  /**
   * Those are options passed to the database
   */
  private String urlOptions = "";
  /**
   * Database user name
   */
  private String userName = "";
  /**
   * User's password to database
   */
  private String password = "";
  /**
   * Server database driver class name
   */
  private String driverClass = "";
  /**
   * Is the database designer client.
   */
  private DatabaseDesigner dbClient;
  /**
   * The lazy loaded database object. This is laoded when the database object is requested
   * for the first time.
   */
  private DatabaseImpl database;

  public DatabaseConnectionImpl(ApplicationImpl application)
  {
    super(application);
  }
  
  protected void setServer(ServerImpl server)
  {
    super.setServer(server);

    if(server != null) {
      // SETUP THE DATABASE CLIENT
      dbClient = (DatabaseDesigner) SoapProxy.createSoapClient(DatabaseDesigner.class,
                                                    server.getFullPathToDesigner() + ServerImpl.DATABASE_DESIGNER_EXEC_PATH,
                                                    server.getUserName(), server.getPassword());
    }
  }



  public String getDatabaseName()
  {
    return databaseName;
  }

  public void setDatabaseName(String name)
  {
    if(name == null)
      throw new IllegalArgumentException("Database name cannot be null");
    if(name.length() == 0)
      throw new IllegalArgumentException("Database name cannot be empty");
    
    if(StringUtil.compareStrings(name, this.databaseName) == false) {
      String oldName = this.databaseName;
      this.databaseName = name;
      setDirty(true);
      fireEvent(PROP_DATABASE_NAME, oldName, name);
    }
  }

  public String getDatabaseUrl()
  {
    return databaseUrl;
  }

  public void setDatabaseUrl(String url)
  {
    if(url == null)
      throw new IllegalArgumentException("Database url cannot be null");
    
    if(StringUtil.compareStrings(url, this.databaseUrl) == false) {
      String oldUrl = this.databaseUrl;
      this.databaseUrl = url;
      setDirty(true);
      fireEvent(PROP_DATABASE_URL, oldUrl, url);
    }
  }

  public String getDatabaseUrlOptions()
  {
    return urlOptions;
  }

  public void setDatabaseUrlOptions(String options)
  {
    if(options == null)
      throw new IllegalArgumentException("Database options cannot be null");
    
    if(StringUtil.compareStrings(options, this.urlOptions) == false) {
      String oldOptions = this.urlOptions;
      this.urlOptions = options;
      setDirty(true);
      fireEvent(PROP_OPTIONS, oldOptions, options);
    }
  }

  public String getUserName()
  {
    return userName;
  }

  public void setUserName(String userName)
  {
    if(userName == null)
      throw new IllegalArgumentException("User name cannot be null");
    
    if(StringUtil.compareStrings(userName, this.userName) == false) {
      String oldName = this.userName;
      this.userName = userName;
      setDirty(true);
      fireEvent(PROP_USER_NAME, oldName, userName);
    }
  }

  public String getPassword()
  {
    return password;
  }

  public void setPassword(String password)
  {
    if(password == null)
      throw new IllegalArgumentException("Database options cannot be null");
    
    if(StringUtil.compareStrings(password, this.password) == false) {
      String oldPwd = this.password;
      this.password = password;
      setDirty(true);
      fireEvent(PROP_PASSWORD, oldPwd, password);
    }
  }

  public String getDriverClass()
  {
    return driverClass;
  }

  public void setDriverClass(String driver)
  {
    if(StringUtil.compareStrings(driver, this.driverClass) == false) {
      this.driverClass = driver;
      setDirty(true);
    }
  }

  public void close()
  {
    setApplication(null);
    // TODO: make new status closed???
    setRemoved();
    
    if(database != null) {
      database.close();
      database = null;
    }
  }

  public void commit() throws PuakmaCoreException
  {
    if(isRemoved())
      throw new PuakmaCoreException("Cannot commit removed object");
    if(isNew() == false && isWorkingCopy() == false)
      throw new PuakmaCoreException("Cannot commit server object, it has to be working copy");
    
    synchronized(this) {
      try {
        AppDesigner designer = application.getAppDesigner();
        
        long newId = designer.saveDatabaseConnection(getId(), application.getId(), getName(),
                                  getDescription(), getDatabaseName(), getDatabaseUrl(),
                                  getDatabaseUrlOptions(), getUserName(), getPassword(), getDriverClass());
        if(isNew())
          id = newId;
        if(isNew() == false)
          ((DatabaseConnectionImpl)original).copyFromWorkingCopy(this);
        setValid();
      }
      catch(Exception e) {
        throw PuakmaLibraryUtils.handleException("Cannot save database connection to server", e);
      }
    }
  }

  public void remove() throws PuakmaCoreException
  {
    synchronized(this) {
      if(isNew())
        throw new PuakmaCoreException("Cannot remove new nonexisting object");
      if(isWorkingCopy())
        throw new PuakmaCoreException("Cannot remove working copy");

      try {
        if(isRemoved() == false) {
          AppDesigner designer = application.getAppDesigner();
          designer.removeDatabaseConnection(getId());
          setRemoved();
        }
        application.notifyRemove(this);
      }
      catch(Exception e) {
        throw PuakmaLibraryUtils.handleException("Cannot remove database connection", e);
      }
    }
  }
  

  public DatabaseConnection makeWorkingCopy()
  {
    if(isWorkingCopy())
      throw new IllegalStateException("Cannot create working copy from working copy");
    
    DatabaseConnectionImpl workingCopy = new DatabaseConnectionImpl(application);
    super.makeCopy(workingCopy);
    setupAsWorkingCopy(workingCopy);

    workingCopy.databaseName = this.databaseName;
    workingCopy.databaseUrl = this.databaseUrl;
    workingCopy.driverClass = this.driverClass;
    workingCopy.urlOptions = this.urlOptions;
    workingCopy.userName = this.userName;
    workingCopy.password = this.password;
    return workingCopy;
  }

  
  protected void copyFromWorkingCopy(DatabaseConnectionImpl workingCopy)
  {
    super.copyFromWorkingCopy(workingCopy);
    
    this.databaseName = workingCopy.databaseName;
    this.databaseUrl = workingCopy.databaseUrl;
    this.driverClass = workingCopy.driverClass;
    this.urlOptions = workingCopy.urlOptions;
    this.userName = workingCopy.userName;
    this.password = workingCopy.password;
  }

  /**
   * Refreshes implementation from 
   */
  synchronized void refreshFrom(DatabaseConnectionImpl dbi)
  {
    assert getId() == dbi.getId() : "Identifier has to be the same in the both objects when refreshing";

    super.refreshFrom(dbi);
    
    setDatabaseName(dbi.getDatabaseName());
    setDatabaseUrl(dbi.getDatabaseUrl());
    setDatabaseUrlOptions(dbi.getDatabaseUrlOptions());
    setDriverClass(dbi.getDriverClass());
    setPassword(dbi.getPassword());
    setUserName(dbi.getUserName());
  }

  public Database getDatabase()
  {
    synchronized(this) {
      if(isWorkingCopy())
        return ((DatabaseConnection)original).getDatabase();
      
      if(database == null) {
        database = new DatabaseImpl(this);
        database.setNew(isNew());
      }
      return database;
    }
  }
  
  protected void setValid()
  {
    super.setValid();
    
    synchronized(this) {
      if(database != null) {
        database.setNew(false);
      }
    }
  }

  public DatabaseDesigner getDatabaseClient()
  {
    return dbClient;
  }

  /**
   * This function refreshes the database with the server-side stuff...
   * @throws PuakmaCoreException 
   */
  public void doRefreshDatabase() throws PuakmaCoreException
  {
    synchronized(this) {
      if(database != null) {
        database.refresh();
      }
    }
  }
}
