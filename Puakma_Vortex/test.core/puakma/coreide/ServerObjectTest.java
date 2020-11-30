/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 3, 2006
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import puakma.coreide.objects2.ServerObject;
import puakma.utils.NameValuePair;

public class ServerObjectTest extends BaseTest
{
  static final String KEY = "#key#";
  static final String DATA = "#data#";
  static final String DATA1 = "#data1#";
  
  NameValuePair pair;
  Object oldData;
  
  public void testDataChange() throws Exception
  {
    ServerObject o = ObjectsFactory.createDbConnection("hola");
    o.addListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt)
      {
        if(ServerObject.PROP_DATA.equals(evt.getPropertyName())) {
          NameValuePair p = (NameValuePair) evt.getNewValue();
          if(p.getName().equals(KEY)) {
            oldData = evt.getOldValue();
            pair = p;
          }
        }
      }
    });
    // ADD NEW DATA
    o.setData(KEY, DATA);
    assertEquals(DATA, pair.getValue());
    assertNull(oldData);
    // CHANGE DATA
    o.setData(KEY, DATA1);
    assertEquals(DATA1, pair.getValue());
    assertEquals(DATA, oldData);
    // REMOVE DATA
    o.setData(KEY, null);
    assertNull(pair.getValue());
    assertEquals(DATA1, oldData);
  }
}
