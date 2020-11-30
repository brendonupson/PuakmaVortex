/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 23, 2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.utils.lang;

import junit.framework.TestCase;

public class TestPropertyManipulator extends TestCase
{
  public void testWrite() throws Exception
  {
    PropertyManipulator m = new PropertyManipulator(AAA.class, "x");
    AAA x = new XXX();
    m.setPropertyOnObject(x, new Integer(11));
    assertEquals(new Integer(11), m.getPropertyFromObject(x));
    
    
  }
}

abstract class AAA {
  public abstract void setX(int x);
  public abstract int getX();
}

class XXX extends AAA {
  private int x;

  public void setX(int x)
  {
    this.x = x;
  }
  
  public int getX()
  {
    return this.x;
  }
}
