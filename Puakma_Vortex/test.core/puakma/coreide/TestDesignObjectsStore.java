/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    13/09/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import puakma.coreide.objects2.DesignObject;

public class TestDesignObjectsStore extends TestCase
{
  public void testNameGenerating()
  {
    Map m = new HashMap();
    String[] items = {
      "AhojClazz", "012345678901234567890123456789",  
    };
    for(int i = 0; i < items.length; ++i) {
      m.put(items[i], items[i]);
    }
    TornadoDatabaseConstraintsImpl dc = new TornadoDatabaseConstraintsImpl();
    
    JavaObjectImpl jo = new JavaObjectImpl(null, DesignObject.TYPE_LIBRARY);
    jo.setClassName("AhojClazz");
    DesignObjectsStore.setupNonConflictJavaObjectName(jo, m, dc);
    assertEquals("AhojClazz0", jo.getName());
    
    jo = new JavaObjectImpl(null, DesignObject.TYPE_LIBRARY);
    jo.setClassName("012345678901234567890123456789");
    DesignObjectsStore.setupNonConflictJavaObjectName(jo, m, dc);
    assertEquals("012345678901234567890123456780", jo.getName());
  }
}
