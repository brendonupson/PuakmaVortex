/*
 * Author: Martin Novak
 * Date:   Aug 11, 2005
 */
package puakma.coreide;

import junit.framework.TestCase;
import puakma.coreide.designer.AppDesignerImpl;
import puakma.coreide.designer.ServerDesigner;
import puakma.coreide.listeners.ILogger;
import puakma.utils.lang.Console;

public class DatabasePingTest extends TestCase
{
  ConnectionPrefsImpl prefs;
  
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
    prefs.setHost("localhost");
    prefs.setPort(8080);
    prefs.setDesignerPath(ConnectionPrefsImpl.DEFAULT_PATH);
    prefs.setUser("SysAdmin");
    prefs.setPwd(";");
    prefs.setUsingSsl(false);
  }

  public void testDatabasePing() throws Exception
  {
    ServerDesigner designer = new ServerDesigner(prefs.getServerString() + "/system/SOAPDesigner.pma/ServerDesigner?WidgetExecute", prefs.getUser(), prefs.getPwd());
//    designer.saveDatabaseConnection(-1, 15, "xxx", "desc", "dbanme", "jdbc://mysql", "urlopts", "user", "pwd", "com.mysql.jdbc.Driver");
    String[] res = designer.pingDatabaseServer("com.mysql.jdbc.Driver", "root", "",
                                     "jdbc:mysql://localhost/", "puakmab", "");
    Console.println(res[0]);
    Console.println(res[1]);
  }
}
