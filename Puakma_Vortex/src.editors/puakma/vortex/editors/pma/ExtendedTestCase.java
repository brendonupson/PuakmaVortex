package puakma.vortex.editors.pma;

import junit.framework.TestCase;

public class ExtendedTestCase extends TestCase
{
  protected void assertArrayContainsValue(int[] a, int expected)
  {
    for(int i = 0; i < a.length; ++i)
      if(expected == a[i])
        return;
    
    String msg = "Array doesn't contain value " + expected;
    fail(msg);
  }
  
  protected void assertArrayContainsValue(Object[] a, Object expected)
  {
    for(int i = 0; i < a.length; ++i)
      if(expected.equals(a[i]))
        return;
    
    String msg = "Array doesn't contain value " + expected;
    fail(msg);
  }
}
