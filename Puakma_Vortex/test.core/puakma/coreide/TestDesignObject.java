/*
 * Author: Martin Novak
 * Date:   Oct 9, 2005
 */
package puakma.coreide;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import junit.framework.TestCase;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.ApplicationEvent;
import puakma.coreide.objects2.ApplicationListener;
import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.ObjectChangeEvent;

public class TestDesignObject extends TestCase
{
  final static int OBJ_ORIG_ID = 110;
  final static int OBJ_ID = 111;
  static int runSaveDO = 0;
  static int runSaveParam = 0;
  private boolean removeListenerNotified;
  
  private static class InternalAppDesigner extends AppDesignerTestStub
  {
    public synchronized long updateDesignObject(long id, long appId, String name,
        int designType, String contentType, String comment, String options, String inheritFrom) {
      runSaveDO++;
      return OBJ_ID;
    }
  
    public synchronized long[] setParams(long objId, String[] paramNames, String[] paramValues)
    {
      assert objId == OBJ_ID;
      runSaveParam++;
      return new long[0];
    }

    public synchronized void removeDesignObject(long id)
    {
      if(id != OBJ_ORIG_ID)
        throw new IllegalArgumentException("Removing invalid design object");
    }
  }
  
  PropertyChangeListener removeListener = new PropertyChangeListener() {
    public void propertyChange(PropertyChangeEvent evt)
    {
      removeListenerNotified = true;
    }
  };
  
  protected void setUp() throws Exception
  {
    removeListenerNotified = false;
  }

  public void testCopy() throws PuakmaCoreException
  {
    ServerImpl server = new ServerImpl();
    server.setDesignerFactory(new DesignerFactoryImpl(InternalAppDesigner.class));
    ApplicationImpl app = new ApplicationImpl(server);
    ResourceObjectImpl ro = new ResourceObjectImpl(null, DesignObject.TYPE_RESOURCE);
    ro.setName("Ahoj");
    ro.setValid();
    ro.id = OBJ_ORIG_ID;
    app.addObject(ro, true);
    DesignObject copy = ro.copy();
    try {
      app.addObject(copy);
      assertTrue("Cannot add object with the same name", false);
    } catch(PuakmaCoreException ex) {
      
    }
    copy.setName("Ahoj1");
    app.addObject(copy);
    // CHECK IF WE UPDATED TO THE SERVER JUST ONCE
    assertEquals(1, runSaveDO);
    assertEquals(1, runSaveParam);
    
    // OK, SO NOW TEST REMOVING DESIGN OBJECT - IF WE FIRE SOME EVENT OR NOT...
    app.addListener(removeListener);
    ro.remove();
    assertTrue(removeListenerNotified);
    app.removeListener(removeListener);
  }
}
