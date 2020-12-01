/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    20/07/2006
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

public class TestClassFileDecompiler extends TestCase
{
  public void testCheckClassName()
  {
    String name = "Ahoj";
    assertNull(ClassFileDecompiler.checkClassName(name));
    
    name = "#Ahoj";
    assertNotNull(ClassFileDecompiler.checkClassName(name));
    
    name = "1Ahoj";
    assertNotNull(ClassFileDecompiler.checkClassName(name));
    
    name = "A#Ahoj";
    assertNotNull(ClassFileDecompiler.checkClassName(name));
    
    name = "A1Ahoj";
    assertNull(ClassFileDecompiler.checkClassName(name));
  }
}
