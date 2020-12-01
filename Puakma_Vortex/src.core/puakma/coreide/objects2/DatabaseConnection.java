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
package puakma.coreide.objects2;


/**
 * This interface encapsulates database connection settings.
 *
 * @author Martin Novak
 */
public interface DatabaseConnection extends ApplicationObject
{
  public static final String PROP_DATABASE_NAME = "databaseName";
  public static final String PROP_DATABASE_URL = "databaseUrl";
  public static final String PROP_OPTIONS = "options";
  public static final String PROP_USER_NAME = "userName";
  public static final String PROP_PASSWORD = "password";
  
  public String getDatabaseName();
  
  public void setDatabaseName(String name);
  
  public String getDatabaseUrl();
  
  public void setDatabaseUrl(String url);
  
  public String getDatabaseUrlOptions();
  
  public void setDatabaseUrlOptions(String options);
  
  public String getUserName();
  
  public void setUserName(String userName);
  
  public String getPassword();
  
  public void setPassword(String password);
  
  public String getDriverClass();
  
  /**
   * Sets driver class name. Eg <code>com.mysql.jdbc.Driver</code>
   *
   * @param driver is the class name of the driver
   */
  public void setDriverClass(String driver);
  
  public DatabaseConnection makeWorkingCopy();
  
  /**
   * Removes database connection.
   *
   * @return Database object which communicates with the database, and also with database
   *         related tables
   */
  public Database getDatabase();
}
