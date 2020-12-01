/*
 * Author: Martin Novak
 * Date:   Aug 10, 2005
 */
package puakma.coreide;

import junit.framework.TestCase;
import puakma.coreide.designer.ApplicationStructureBean;
import puakma.coreide.designer.ApplicationStructureBean.DObject;
import puakma.coreide.objects2.DesignObject;

public class TestApplicationImplFunctions extends TestCase
{
  private DObject getValidDBean()
  {
    ApplicationStructureBean.DObject bean = new ApplicationStructureBean.DObject();
    bean.id = 1;
    bean.name = "BEAN";
    bean.designType = DesignObject.TYPE_ACTION;
    return bean;
  }
  
  public void testIsValidDObjectBean()
  {
    ApplicationStructureBean.DObject dbean = getValidDBean();

    // VALID GOOD OBJECT
    assertTrue(ApplicationImpl.isValidDObjectBean(dbean));
    // ID NOT VALID
    dbean.id = -1;
    assertFalse(ApplicationImpl.isValidDObjectBean(dbean));
    
    dbean = getValidDBean();
    dbean.name = null;
    assertFalse(ApplicationImpl.isValidDObjectBean(dbean));
    
    dbean = getValidDBean();
    dbean.name = "";
    assertFalse(ApplicationImpl.isValidDObjectBean(dbean));
    
    dbean = getValidDBean();
    dbean.designType = -101;
    assertFalse(ApplicationImpl.isValidDObjectBean(dbean));
  }
}
