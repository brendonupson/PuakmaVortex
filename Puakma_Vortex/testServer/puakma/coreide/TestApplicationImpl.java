/*
 * Author: Martin Novak
 * Date:   Jul 1, 2005
 */
package puakma.coreide;

import junit.framework.TestCase;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.JavaObject;
import puakma.coreide.objects2.Server;

/**
 * @author Martin Novak
 */
public class TestApplicationImpl extends TestCase
{
  public static final String PUAKMA_TEST = "puakma.test";
  public static final String DUMBCLASS = "DumbClass";
  
  private ConnectionPrefs pref;
  private Application app;
  private Server server;
  
  protected void setUp() throws Exception
  {
    if(pref == null) {
      pref = new ConnectionPrefsImpl();
      pref.setHost("localhost");
      pref.setPort(8080);
      pref.setUser("SysAdmin");
      pref.setPwd(";");
      pref.setUsingSsl(false);
      server = ServerManager.createServerConnection(pref);
      app = server.getApplication("test", "test1");
      app.open();
    }
  }

  public void testConnection() throws Exception
  {
    app.refresh();
    
    // NOW TEST ALL THE DESIGN OBJECTS
    DesignObject[] objs = app.listDesignObjects();
    
    DesignObject velocity = app.getDesignObject("VelocityLibrary");
    assert velocity != null : "Library VelocityLibrary doesn't exist";
    
    DesignObject veloCopy = velocity.makeWorkingCopy();
    veloCopy.setDescription("AHOJ1");
    
    // NOW TRY TO FIND SOME DESIGN OBJECTS
    JavaObject jo = app.getJavaObject(PUAKMA_TEST, DUMBCLASS);
    assertNotNull(jo);
    
    jo = app.getJavaObject(PUAKMA_TEST, "DumbClass$B$C");
    assertNotNull(jo);
    
    app.getJavaObject("", DUMBCLASS);
    assertNotNull(jo);
    
    app.getJavaObject(null, DUMBCLASS);
    assertNotNull(jo);
    
    app.close();
    
    server.close();
  }
}
