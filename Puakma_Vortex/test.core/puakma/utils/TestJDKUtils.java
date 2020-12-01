/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    10/06/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.utils;

import junit.framework.TestCase;
import puakma.utils.lang.JDKUtils;

public class TestJDKUtils extends TestCase
{
  String t1 = "1.5.0_06";
  
  public void testGuessJdk()
  {
    int res = JDKUtils.guessJdk(t1);
    assertEquals(JDKUtils.JDK_VERSION_1_5_0, res);
  }

  public void testMaxVersion()
  {
    int res = JDKUtils.guessJdk(t1);
    assertEquals(1, JDKUtils.maxVersion(res));
  }

  public void testMedVersion()
  {
    int res = JDKUtils.guessJdk(t1);
    assertEquals(5, JDKUtils.medVersion(res));
  }

  public void testMinVersion()
  {
    int res = JDKUtils.guessJdk(t1);
    assertEquals(0, JDKUtils.minVersion(res));
  }

}
