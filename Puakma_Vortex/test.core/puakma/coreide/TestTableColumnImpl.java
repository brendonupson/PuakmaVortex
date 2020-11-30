/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 2, 2006
 * 
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide;

import junit.framework.TestCase;

public class TestTableColumnImpl extends TestCase
{
  public void testParseColumnType() throws Exception
  {
    TableColumnImpl impl = new TableColumnImpl(null);
    impl.parseAndSetType("INTEGER");
    assertEquals("INTEGER", impl.getType());
    
    impl.parseAndSetType("VARCHAR(255)");
    assertEquals("VARCHAR", impl.getType());
    assertEquals(255, impl.getTypeSize());
    
    impl.parseAndSetType("VARCHAR ( 255 )");
    assertEquals("VARCHAR", impl.getType());
    assertEquals(255, impl.getTypeSize());
    
    impl.parseAndSetType("FLOAT(23, 7)");
    assertEquals("FLOAT", impl.getType());
    assertEquals(23, impl.getTypeSize());
    assertEquals(7, impl.getFloatDecimals());
    
    impl.parseAndSetType("FLOAT(23,7)");
    assertEquals("FLOAT", impl.getType());
    assertEquals(23, impl.getTypeSize());
    assertEquals(7, impl.getFloatDecimals());
  }
}
