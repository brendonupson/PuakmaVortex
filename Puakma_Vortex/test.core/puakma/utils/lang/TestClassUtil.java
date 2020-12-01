/*
 * Author: Martin Novak
 * Date:   Jun 30, 2005
 */
package puakma.utils.lang;

import junit.framework.TestCase;

/**
 * @author Martin Novak
 */
public class TestClassUtil extends TestCase
{
  public void testGetClassName()
  {
    String className = "package.subpackage.class";
    assertEquals("class", ClassUtil.getClassName(className));
    className = "class";
    assertEquals("class", ClassUtil.getClassName(className));
  }

  public void testGetPackageName()
  {
    String className = "package.subpackage.class";
    assertEquals("package.subpackage", ClassUtil.getPackageName(className));
    className = "class";
    assertEquals("", ClassUtil.getPackageName(className));
  }

  public void testGetBaseClass()
  {
    String className = "package.subpackage.class$1$12";
    assertEquals("class", ClassUtil.getBaseClass(className));
    className = "class$1";
    assertEquals("class", ClassUtil.getBaseClass(className));    
  }
  
  public void testIsValidPackageName() throws Exception
  {
    String s = "Package";
    assertTrue(ClassUtil.isValidPackageName(s));
    
    s = ".Package";
    assertFalse(ClassUtil.isValidPackageName(s));
    s = "test.aaa";
    assertTrue(ClassUtil.isValidPackageName(s));
    s = "test.aaa.";
    assertFalse(ClassUtil.isValidPackageName(s));
  }
}
