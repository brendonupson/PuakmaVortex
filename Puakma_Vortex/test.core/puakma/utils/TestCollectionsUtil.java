/*
 * Author: Martin Novak
 * Date:   Mar 17, 2005
 */
package puakma.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import puakma.utils.lang.CollectionsUtil;
import puakma.utils.lang.KeyProvider;

/**
 * @author Martin Novak
 */
public class TestCollectionsUtil extends TestCase
{
  public void testShiftObjects()
  {
    List l = new ArrayList();
    String a[] = {
        "Martinek", "Pepa", "Brendon", "Mamka"  
    };
    for(int i = 0; i < a.length; ++i)
      l.add(a[i]);
    
    CollectionsUtil.shiftObjects(l, 0, 1);
    assertEquals(a[0], l.get(1));
    assertEquals(a[1], l.get(0));
    
    CollectionsUtil.shiftObjects(l, 1, 0);
    assertEquals(a[0], l.get(0));
    assertEquals(a[1], l.get(1));
    
    CollectionsUtil.shiftObjects(l, 2, 3);
    assertEquals(a[2], l.get(3));
    assertEquals(a[3], l.get(2));

    CollectionsUtil.shiftObjects(l, 3, 2);
    assertEquals(a[2], l.get(2));
    assertEquals(a[3], l.get(3));
    
    CollectionsUtil.shiftObjects(l, 1, 2);
    assertEquals(a[2], l.get(1));
    assertEquals(a[1], l.get(2));
    
    CollectionsUtil.shiftObjects(l, 2, 1);
    assertEquals(a[1], l.get(1));
    assertEquals(a[2], l.get(2));
  }
  
  public void testRemoveValue()
  {
    Map m = new HashMap();
    m.put("1", "gagarin");
    m.put("2", "gagarin1");
    m.put("3", "ghaga");
    
    assertTrue(CollectionsUtil.removeValue(m, "gagarin"));
    assertTrue(CollectionsUtil.removeValue(m, "gagarin1"));
    assertTrue(CollectionsUtil.removeValue(m, "ghaga"));
    assertFalse(CollectionsUtil.removeValue(m, "gagarin2"));
    assertEquals(0, m.size());
  }
  
  public void testFirstMinusSecond()
  {
    Map m = new HashMap();
    m.put(new Long(1), "1");
    m.put(new Long(2), "2");
    m.put(new Long(3), "3");
    List l = new ArrayList();
    l.add("2");
    List ret = CollectionsUtil.firstMinusSecond(m, l, new KeyProvider() {
      public Object getKeyFor(Object value)
      {
        return Long.valueOf((String) value);
      }
    });
    assertEquals(2, ret.size());
    assertTrue(ret.contains("1"));
    assertTrue(ret.contains("3"));
  }
}
