/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Dec 9, 2005
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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.ILogger;

public class TestUtils
{
  public static final int DB_MYSQL = 0;
  private static final String DBAPP_NAME = "dbTestApp";
  private static final String GROUP_NAME = "test";
  
  static class DBInfo {
    String url;
    String driver;
    String user;
    String pwd;
    public DBInfo(String _url, String _driver, String _u, String _p) {
      url = _url; driver = _driver; user = _u; pwd = _p;
    }
  }
  
  private static DBInfo[] dbs = {
      new DBInfo("jdbc:mysql://localhost/junit", "com.mysql.jdbc.Driver", "root", ""),
  };
  private static boolean initialized;
  
  /**
   * Gets the connection according to the database we want to test
   * @param type is the database we want to connect to
   * @return Connection object
   * @throws Exception
   */
  public static Connection getConnection(int type) throws Exception
  {
    setup();
    Class.forName(dbs[type].driver).newInstance();
    Connection con = DriverManager.getConnection(dbs[type].url, dbs[type].user, dbs[type].pwd);
    return con;
  }

  public static void setup()
  {
    if(initialized)
      return;
    
    initialized = true;
    PuakmaLibraryManager.configure(new ILogger() {
      public void log(String messsage, Throwable e, int level)
      {
        System.out.println(messsage);
        if(e != null)
          e.printStackTrace();
      }
    }, true);
  }

  public static void setupDatabase(Connection connection) throws Exception
  {
    String[] tables = listTables(connection);
    Statement st = connection.createStatement();
    for(int i = 0; i < tables.length; ++i)
      st.execute("DROP TABLE " + tables[i]);
  }

  public static String[] listTables(Connection connection) throws Exception
  {
    List<String> l = new ArrayList<String>();
    DatabaseMetaData m = connection.getMetaData();
    ResultSet rs = m.getTables(null, null, "%", new String[] {"TABLE"});
    while(rs.next())
      l.add(rs.getString("TABLE_NAME"));
    return l.toArray(new String[l.size()]);
  }

  public static Application openDbTestApplication() throws Exception
  {
    ServerImpl s = new ServerImpl();
    s.init(getConnectionPrefs());
    Application app = s.getApplication(GROUP_NAME, DBAPP_NAME);
    app.open();
    return app;
  }

  public static ConnectionPrefsImpl getConnectionPrefs()
  {
    ConnectionPrefsImpl prefs = new ConnectionPrefsImpl();
    prefs.setDesignerPath(ConnectionPrefsReader.DEFAULT_PATH);
    prefs.setHost("localhost");
    prefs.setPort(8080);
    prefs.setUsingSsl(false);
    prefs.setUser("SysAdmin");
    prefs.setPwd("gagarin;");
    return prefs;
  }

  public static Connection getClearConnection(int type) throws Exception
  {
    Connection c = getConnection(type);
    setupDatabase(c);
    return c;
  }
  
  /**
   * Creates a connection to the puakma system database.
   * @return Connection object.
   * @throws Exception
   */
  public static Connection createPmaSystemConnection() throws Exception
  {
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/puakma", "puakma", "gagarin");
    return connection;
  }

  public static long getTestDbConnectionId(Connection sysCon) throws Exception
  {
    long appId = getDbTestAppId(sysCon);
    PreparedStatement pst = sysCon.prepareStatement("SELECT DBConnectionID FROM DBCONNECTION WHERE AppID=? AND DBConnectionName='testDbCon'");
    pst.setLong(1, appId);
    ResultSet rs = pst.executeQuery();
    if(rs.next() == false)
      throw new IllegalStateException("NO DB CONNECTION!!!");
      
    return rs.getLong(1);
  }

  private static long getDbTestAppId(Connection sysCon) throws Exception
  {
    PreparedStatement pst = sysCon.prepareStatement("SELECT AppID FROM APPLICATION WHERE AppName='dbTestApp' AND AppGroup='test'");
    ResultSet rs = pst.executeQuery();
    if(rs.next() == false)
      throw new IllegalStateException("NO DBTEST APPLICATION!!!");
    
    return rs.getLong(1);
  }
}
