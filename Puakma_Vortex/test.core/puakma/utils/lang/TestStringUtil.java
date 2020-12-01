/*
 * Author: Martin Novak
 * Date:   Apr 27, 2005
 */
package puakma.utils.lang;

import junit.framework.TestCase;

public class TestStringUtil extends TestCase
{
  public void testMerge()
  {
    String[] a = { "Martin", "Patrick", "Pepa", "Walis" };
    String res = StringUtil.merge(a,",");
    assertEquals("Martin,Patrick,Pepa,Walis",res);
    
    String[] b = {};
    res = StringUtil.merge(b,",");
    assertEquals("",res);
    
    String[] c = { "Martin" };
    res = StringUtil.merge(c,",");
    assertEquals("Martin",res);
  }
  
  public void testCompare()
  {
    String s1, s2;
    
    s1 = null; s2 = null;
    assertTrue(StringUtil.compareStrings(s1, s2));
    
    s1 = ""; s2 ="";
    assertTrue(StringUtil.compareStrings(s1, s2));
    
    s1 = ""; s2 = null;
    assertTrue(StringUtil.compareStrings(s1, s2));
    
    s1 = null; s2 ="";
    assertTrue(StringUtil.compareStrings(s1, s2));
    
    s1 = "aa"; s2 ="aa";
    assertTrue(StringUtil.compareStrings(s1, s2));
    
    s1 = ""; s2 ="aa";
    assertFalse(StringUtil.compareStrings(s1, s2));
    
    s1 = null; s2 ="aa";
    assertFalse(StringUtil.compareStrings(s1, s2));
    
    s1 = "aa"; s2 ="";
    assertFalse(StringUtil.compareStrings(s1, s2));
    
    s1 = "aa"; s2 =null;
    assertFalse(StringUtil.compareStrings(s1, s2));
    
    s1 = "bb"; s2 ="aa";
    assertFalse(StringUtil.compareStrings(s1, s2));
  }
  
  public void testToHexString()
  {
    byte[] buffer = new byte[] {
        0x00, 0x12, 0x1A, 0x1F
    };

    String str = StringUtil.bytesToHexString(buffer);
    assertEquals("00121A1F", str);
    
    str = StringUtil.bytesToHexString(new byte[0]);
    assertEquals("", str);
    
    try {
      str = StringUtil.bytesToHexString(null);
      assert false : "HAS TO THROW NullPointerException";
    }
    catch(NullPointerException ex) {
      // IGNORE
    }
    
    // TEST THAT WE CAN PASS THROUHG ALL THE BYTES
    buffer = new byte[256];
    for(int i = 0 ; i < buffer.length; ++i) {
      buffer[i] = (byte) i;
    }
    StringUtil.bytesToHexString(buffer);
  }
  
  public void testStringToBytes()
  {
    String hex = "00121A1F";
    byte[] res = {
        0x00, 0x12, 0x1A, 0x1F
    };
    
    byte[] bytes = StringUtil.hexStringToBytes(hex);
    assertTrue(bytesEqual(res, bytes));
  }

  private boolean bytesEqual(byte[] res, byte[] bytes)
  {
    for(int i = 0; i < res.length; ++i) {
      if(res[i] != bytes[i])
        return false;
    }
    return true;
  }
  
  public void testWildcardMatcher()
  {
    String t = "file.txt", p = "*fi?e";
    assertTrue(StringUtil.matchWildcardIgnoreCase(t, p));
    
    t = "file.txt"; p = "fi?e";
    assertTrue(StringUtil.matchWildcardIgnoreCase(t, p));
    
    t = "file.txt"; p = "i?e";
    assertFalse(StringUtil.matchWildcardIgnoreCase(t, p));
  }
  
  public void testSafeString()
  {
    String s;
    
    s = StringUtil.safeString(null);
    assertEquals(s,"");
    
    s = StringUtil.safeString("");
    assertEquals(s,"");
    
    s = StringUtil.safeString("AHOJ");
    assertEquals(s,"AHOJ");
  }

  public void testIndexOfSecondIgnoreCase()
  {
    //TODO Implement indexOfSecondIgnoreCase().
  }

  public void testTrim()
  {
    String s;
    
    s = StringUtil.trim(" \t\nAHOJ AHOJ\t ");
    assertEquals("AHOJ AHOJ", s);
    
    s = StringUtil.trim("AHOJ AHOJ");
    assertEquals("AHOJ AHOJ", s);
  }

  public void testArraySearch()
  {
    String[] arr = {
        "Ahoj", "ahoj", "zzz"
    };
    assertEquals(-1, StringUtil.arrayContainsString(arr,"zzzz"));
    assertEquals(0, StringUtil.arrayContainsString(arr,"Ahoj"));
    assertEquals(1, StringUtil.arrayContainsString(arr,"ahoj"));
  }
  
  public void testWildcardMatch()
  {
    String a = "befeleme";
    assertTrue(StringUtil.matchWildcardIgnoreCase(a, "*fe"));
    assertTrue(StringUtil.matchWildcardIgnoreCase(a, "be"));
    assertTrue(StringUtil.matchWildcardIgnoreCase(a, "*le"));
    assertTrue(StringUtil.matchWildcardIgnoreCase(a, "b*f*l"));
    
    assertFalse(StringUtil.matchWildcardIgnoreCase(a, "fe"));
  }

  
  public void testSearchInSortedArrayIgnoreCase()
  {
    String[] a = {
        "ahoj", "Ahoj", "nazdarek"
    };
    String key = "aHoJ";
    assertEquals(1, StringUtil.findInSortedArrayIgnoreCase(a, key));
    
    key = "gaga";
    assertEquals(-1, StringUtil.findInSortedArrayIgnoreCase(a, key));
    
    key = "Nazdar";
    assertEquals(-1, StringUtil.findInSortedArrayIgnoreCase(a, key));
  }
  
  public void testFindLineRange()
  {
    String txt = "Herr\nGrupen\nFuhrer";
    int line = 0;
    StringUtil.Range range = new StringUtil.Range();
    assertTrue(StringUtil.findLineRange(txt, line, range));
    assertEquals(0, range.start);
    assertEquals(4, range.end);
    
    line = 1;
    assertTrue(StringUtil.findLineRange(txt, line, range));
    assertEquals(5, range.start);
    assertEquals(11, range.end);
    
    line = 2;
    assertTrue(StringUtil.findLineRange(txt, line, range));
    assertEquals(12, range.start);
    assertEquals(18, range.end);
    
    txt = "\n\n";
    line = 0;
    assertTrue(StringUtil.findLineRange(txt, line, range));
    assertEquals(0, range.start);
    assertEquals(0, range.end);
    
    line = 1;
    assertTrue(StringUtil.findLineRange(txt, line, range));
    assertEquals(1, range.start);
    assertEquals(1, range.end);
    
    line = 2;
    assertTrue(StringUtil.findLineRange(txt, line, range));
    assertEquals(2, range.start);
    assertEquals(2, range.end);
  }
  
  public void testGetLine()
  {
    String s = "A\nB\nC";
    
    assertEquals("A", StringUtil.getLine(s, 0));
    assertEquals("B", StringUtil.getLine(s, 1));
    assertEquals("C", StringUtil.getLine(s, 2));
  }
}
