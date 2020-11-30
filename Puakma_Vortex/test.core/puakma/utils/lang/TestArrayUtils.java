/*
 * Author: Martin Novak
 * Date:   Apr 1, 2005
 */
package puakma.utils.lang;

import junit.framework.TestCase;

public class TestArrayUtils extends TestCase
{
  public void testFindSequence()
  {
    byte[] arr = new byte[10];
    byte[] seq = new byte[3];
    int offset = 3;
    
    for(int i = 0; i < arr.length; ++i) {
      arr[i] = (byte) i;
    }
    for(int i = offset; i < offset + seq.length; ++i) {
      seq[i - offset] = (byte) i;
    }
    
    assertEquals(3, ArrayUtils.findSequence(arr, seq));
  }
  
  public void testFindSequence1()
  {
    byte[] array = new byte[] { 'a', 'b', 'c', '.', '\r', '\n' };
    byte[] pattern = new byte[] { '.', '\r', '\n' };
    
    int i = ArrayUtils.findSequence(array, pattern);
    assertEquals(3, i);
    
    pattern = new byte[] { '.', '\r', 'a'};
    i = ArrayUtils.findSequence(array, pattern);
    assertEquals(-1, i);
    
    pattern = new byte[] { 'z', 'y', 'x'};
    i = ArrayUtils.findSequence(array, pattern);
    assertEquals(-1, i);
  }
  
  public void testRemove()
  {
    String[] arr = new String[] { "Ahoj", "Prdel", "Zadek" };
    String[] x;
    x = (String[])ArrayUtils.remove(arr, "Ahoj");
    assertEquals(2, x.length);
    assertEquals("Prdel", x[0]);
    assertEquals("Zadek", x[1]);
    
    assertEquals(3, ArrayUtils.remove(arr, "Ahoj1").length);
    assertEquals(2, ArrayUtils.remove(arr, "Prdel").length);
    
    x = (String[])ArrayUtils.remove(arr, "Zadek");
    assertEquals(2, x.length);
    assertEquals("Ahoj", x[0]);
    assertEquals("Prdel", x[1]);
  }
  
  public void testRemove2()
  {
    String[] arr = new String[] { "Ahoj", "Prdel", "Zadek" };
    String[] x;
    x = (String[])ArrayUtils.remove(arr, 0);
    assertEquals(2, x.length);
    assertEquals("Prdel", x[0]);
    assertEquals("Zadek", x[1]);
    
    try {
      assertEquals(2, ArrayUtils.remove(arr, 4).length);
      assertTrue(false);
    }
    catch(IndexOutOfBoundsException ex) {  }
    assertEquals(2, ArrayUtils.remove(arr, 1).length);
    
    x = (String[])ArrayUtils.remove(arr, 2);
    assertEquals(2, x.length);
    assertEquals("Ahoj", x[0]);
    assertEquals("Prdel", x[1]);
  }
  
  public void testFilterNulls()
  {
    String[] a = { null, "Xx", "aa", null };
    String[] z = (String[]) ArrayUtils.filterNulls(a);
    assertEquals(2, z.length);
    
    a = new String[] { null, null };
    z = (String[]) ArrayUtils.filterNulls(a);
    assertEquals(0, z.length);
    
    a = new String[] { "Xx", "aa" };
    z = (String[]) ArrayUtils.filterNulls(a);
    assertEquals(2, z.length);
  }
  
  public void testCreateArrayFromSubIndex()
  {
    String[][] a = { { "Ahoj", "a"} , {"Ahoj", "b"}, {"Ahoj", "c"} };
    String[] x = (String[]) ArrayUtils.createArrayFromSubIndex(a, 1);
    assertEquals(3, x.length);
    assertEquals("a", x[0]);
    assertEquals("b", x[1]);
    assertEquals("c", x[2]);
    
  }
  
  public void testRemoveDuplicates() throws Exception
  {
    String[] a = {
      "A", "A", "B", "A",  
    };
    String[] x = (String[]) ArrayUtils.removeDuplicates(a);
    assertEquals(2, x.length);
    assertEquals(x[0], "B");
    assertEquals(x[1], "A");
  }
  
  public void testParseIntArray()
  {
    int[] r = ArrayUtils.parseIntArray("1,2", ',');
    assertEquals(2, r.length);
    assertEquals(1, r[0]);
    assertEquals(2, r[1]);
    
    r = ArrayUtils.parseIntArray("1,x,2", ',');
    assertEquals(2, r.length);
    assertEquals(1, r[0]);
    assertEquals(2, r[1]);
    
    r = ArrayUtils.parseIntArray("1,x2", ',');
    assertEquals(1, r.length);
    assertEquals(1, r[0]);
    
    r = ArrayUtils.parseIntArray("1,2x", ',');
    assertEquals(1, r.length);
    assertEquals(1, r[0]);
    
    r = ArrayUtils.parseIntArray("1,,2", ',');
    assertEquals(2, r.length);
    assertEquals(1, r[0]);
    assertEquals(2, r[1]);
  }
}
