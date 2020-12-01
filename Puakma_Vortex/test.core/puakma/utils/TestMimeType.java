/*
 * Author: Martin Novak
 * Date:   Jan 11, 2005
 */
package puakma.utils;

import junit.framework.TestCase;
import puakma.coreide.ParseException;


/**
 * @author Martin Novak
 */
public class TestMimeType extends TestCase
{

  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(TestMimeType.class);
  }

  public void testParse() throws Exception
  {
    MimeType mt = new MimeType();

    mt.parse("text/html");
    assertEquals(mt.getMainType(), "text");
    assertEquals(mt.getSubType(), "html");

    // test 2
    mt.parse("text/html;charset=cp1250");
    assertEquals(mt.getMainType(), "text");
    assertEquals(mt.getSubType(), "html");
    assertEquals(mt.getCharset(),"cp1250");

    // test 3
    try {
      mt.parse("/html;charset=cp1250");
      throw new Exception("Invalid test result");
    }
    catch(ParseException e)
    {}
    
    // test 4
    try {
      mt.parse("text/;charset=cp1250");
      throw new Exception("Invalid test result");
    }
    catch(ParseException e)
    {}

    
    // test 5
    try {
      mt.parse(";charset=cp1250");
      throw new Exception("Invalid test result");
    }
    catch(ParseException e)
    {}
  }

}
