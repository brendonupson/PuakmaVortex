/*
 * Author: Martin Novak
 * Date:   Jan 24, 2005
 */
package puakma.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import puakma.utils.io.LineReader;

import junit.framework.TestCase;

/**
 * @author Martin Novak
 */
public class TestFileListReader extends TestCase
{
  public void testFileStreamLoad() throws IOException
  {
    LineReader reader = new LineReader();
    InputStream is = getClass().getClassLoader().getResourceAsStream(
                                            "puakma/test/imageFiles.config");

    // parsing
    reader.parse(is);
    
    // contain test
    assertTrue(reader.contains("image/png"));
    
    // iterator test
    boolean have = false;
    Iterator it = reader.iterator();
    while(it.hasNext()) {
      String s = (String) it.next();
      if(s.equals("image/png")) {
        have = true;
        break;
      }
    }
    assertTrue(have);
    
    // test if the number of items in the reader is correct
    assertTrue(reader.size() == 16);
  }

  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(TestFileListReader.class);
  }
}