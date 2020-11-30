/*
 * Author: Martin Novak
 * Date:   Oct 9, 2005
 */
package puakma.coreide;

import junit.framework.TestCase;

public class TestApplicationImpl1 extends TestCase
{
  private static final String TEMPLATE1 = "Template1";
  private static final String INHERITFROM1 = "Inh1";
  private static final String DESCRIPTION1 = "Description1";
  private static final String GROUP1 = "Group1";
  private static final String NAME1 = "Name1";
  final static int OBJ_ORIG_ID = 110;
  final static int OBJ_ID = 111;
  private static final long APP_ID = 11;
  static int runSaveDO = 0;
  static int runSaveParam = 0;
  private static class InternalAppDesigner extends AppDesignerTestStub
  {
    public synchronized long saveApplication(long appId, String group, String name, String inheritFrom, String templateName, String description)
    {
      return appId;
    }
  }
  public void testCopy() throws PuakmaCoreException
  {
    ServerImpl server = new ServerImpl();
    server.setDesignerFactory(new DesignerFactoryImpl(InternalAppDesigner.class));
    ApplicationImpl app = new ApplicationImpl(server);
    
    app.open();
    
    app.setId(APP_ID);
    app.setName("Name");
    app.setGroup("Group");
    app.setDescription("Description");
    app.setInheritFrom("InheritFrom");
    app.setTemplateName("TemplateName");
    app.setValid();
    
    ApplicationImpl wc = (ApplicationImpl)app.makeWorkingCopy();

    // TEST WORKING COPY
    assertEquals(APP_ID, wc.getId());
    assertEquals("Name", wc.getName());
    assertEquals("Group", wc.getGroup());
    assertEquals("Description", wc.getDescription());
    assertEquals("InheritFrom", wc.getInheritFrom());
    assertEquals("TemplateName", wc.getTemplateName());
    assertEquals(server, wc.getServer());
    assertEquals(app, wc.original);
    
    wc.setName(NAME1);
    wc.setGroup(GROUP1);
    wc.setDescription(DESCRIPTION1);
    wc.setInheritFrom(INHERITFROM1);
    wc.setTemplateName(TEMPLATE1);
    
    wc.commit();
    
    assertEquals(false, app.isNew());
    assertEquals(false, wc.isNew());
    assertEquals(APP_ID, app.getId());
    assertEquals(NAME1, app.getName());
    assertEquals(GROUP1, app.getGroup());
    assertEquals(DESCRIPTION1, app.getDescription());
    assertEquals(INHERITFROM1, app.getInheritFrom());
    assertEquals(TEMPLATE1, app.getTemplateName());
    assertEquals(server, app.getServer());
  }
}
