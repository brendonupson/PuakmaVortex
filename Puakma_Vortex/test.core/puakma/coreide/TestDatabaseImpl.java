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
import java.sql.Statement;

import junit.framework.TestCase;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.Database;
import puakma.coreide.objects2.DatabaseConnection;
import puakma.coreide.objects2.SQLQuery;

public class TestDatabaseImpl extends TestCase
{
  Application app;
  String fileName = "serverPrefs.conf";
  int APP_ID = 7;
  String errMsg = null;
  boolean isDone = false;
  DatabaseConnection dbCon;
  
  protected void setUp() throws Exception
  {
    super.setUp();
    
    TestUtils.setup();
    Connection c = TestUtils.getClearConnection(TestUtils.DB_MYSQL);
    Statement s = c.createStatement();
    s.executeUpdate("CREATE TABLE TESTTABLE (id int, name varchar(255))");
    s.executeUpdate("INSERT INTO TESTTABLE (id,name) VALUES ('1', 'Martin')");
    app = TestUtils.openDbTestApplication();
    app.refresh();
    
    dbCon = app.getDatabaseConnection("testDbCon");
  }

  protected void tearDown() throws Exception
  {
    super.tearDown();
  }

  /*
   * Test method for 'puakma.coreide.DatabaseImpl.executeQuery(String)'
   */
  public void testExecuteQuery() throws Exception
  {
    Database db = dbCon.getDatabase();
    SQLQuery query = db.executeQuery("SELECT * FROM TESTTABLE");
    
    assertTrue(query.next());
    assertEquals(1, query.longValue(0));
    assertEquals("Martin", query.value(1));
    
    assertEquals("Martin", query.valueFromRowHandle(0, 1));
  }
}
