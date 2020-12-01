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
package puakma.utils;

import junit.framework.TestCase;

public class TestGraphicsUtils extends TestCase
{
  public void testPointsInterfere() throws Exception
  {
    int[] b1 = { 0, 2, 2, 4 };
    int[] b2 = { 1, 3, 3, 5 };
    assertTrue(GraphicsUtils.pointsInterfere(b1, b2));
    assertTrue(GraphicsUtils.pointsInterfere(b2, b1));
    
    b1 = new int[] { 5, 5, 6, 6 };
    assertFalse(GraphicsUtils.pointsInterfere(b1, b2));
    assertFalse(GraphicsUtils.pointsInterfere(b2, b1));
    
    b1 = new int[] { 0, 20, 1, 21 };
    b2 = new int[] { 0 , 3, 1,  4 };
    assertFalse(GraphicsUtils.pointsInterfere(b1, b2));
    assertFalse(GraphicsUtils.pointsInterfere(b2, b1));
    
    b1 = new int[] { 1, 1, 20, 20 };
    b2 = new int[] { 20 , 20, 23, 23 };
    assertTrue(GraphicsUtils.pointsInterfere(b1, b2));
    assertTrue(GraphicsUtils.pointsInterfere(b2, b1));
  }
}
