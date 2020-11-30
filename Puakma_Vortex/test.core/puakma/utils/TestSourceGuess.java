/*
 * Author: Martin Novak
 * Date:   Aug 10, 2005
 */
package puakma.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import junit.framework.TestCase;
import puakma.utils.lang.StringUtil;

public class TestSourceGuess extends TestCase
{
  class TestData {
    public TestData(String expPackage, String expClass)
    {
      this.expClass = expClass; this.expPackage = expPackage;
    }
    String source; String expClass; String expPackage;
  }
  TestData[] res = {
      new TestData("", "SaveIncidentReports"),
      new TestData("", "OpenMntIndustry"),
      new TestData("actions", "GlobalSaveSecurity"),
      new TestData("actions", "GlobalSecurity"),
  };

  protected void setUp() throws Exception
  {
    super.setUp();
    String packageN = TestSourceGuess.class.getPackage().getName();
    for(int i = 0; i < res.length; ++i) {
      String r = packageN.replace('.','/') + "/" + "source" + i + ".xjava";
      InputStream is = TestSourceGuess.class.getClassLoader().getResourceAsStream(r);
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      StringBuffer sb = new StringBuffer();
      while(true) {
        String line = br.readLine();
        if(line == null) {
          res[i].source = sb.toString();
          break;
        }
        
        sb.append(line);
        sb.append("\n");
      }
    }
  }

  /*
   * Test method for 'puakma.utils.SourceGuess.guessContent(String)'
   */
  public void testGuessContent() throws ClassFileDecompilerException
  {
    for(int i = 0; i < res.length; ++i) {
      SourceGuess g = new SourceGuess();
      g.guessContent(res[i].source);
      
      assertEquals(res[i].expClass, g.getClassName());
      String gP = g.getPackageName();
      String eP = res[i].expPackage;
      assertTrue(StringUtil.compareStrings(eP, gP));
    }
  }

}
