/*
 * Author: Martin Novak
 * Date:   Feb 15, 2005
 */
package puakma.coreide;

import java.io.File;

import junit.framework.TestCase;
import puakma.coreide.listeners.ILogger;
import puakma.coreide.objects2.Server;

/**
 * @author Martin Novak
 */
public class ServerConnectionTest extends TestCase
{
  ConnectionPrefsImpl prefs;
  String fileName = "serverPrefs.conf";
  int APP_ID = 7;
  String errMsg = null;
  boolean isDone = false;
  Server connection;
  
  protected void setUp() throws Exception
  {
    super.setUp();

    PuakmaLibraryManager.configure(null, new ILogger() {
      public void log(String messsage, Throwable e, int level)
      {
        System.out.println(messsage);
        if(e != null)
          e.printStackTrace();
      }
    });
    
    prefs = new ConnectionPrefsImpl();
    prefs.setName("test");
    prefs.setHost("localhost");
    prefs.setPort(8080);
    prefs.setDesignerPath(ConnectionPrefsReader.DEFAULT_PATH);
    prefs.setUser("SysAdmin");
    prefs.setPwd(";");
    prefs.setUsingSsl(false);
  }

  protected void tearDown() throws Exception
  {
    super.tearDown();
  }

  public void testExportPmx() throws PuakmaCoreException
  {
    connection = ServerManager.createServerConnection(prefs);
    connection.exportPmx("system", "SOAPDesigner", new File("export.pmx"), true);
    
    assertSame(null, errMsg);
    assertEquals(isDone, true);
  }
}
