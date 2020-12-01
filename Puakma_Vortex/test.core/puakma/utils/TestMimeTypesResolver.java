/*
 * Author: Martin Novak
 * Date:   Jan 11, 2005
 */
package puakma.utils;

import junit.framework.TestCase;


/**
 * @author Martin Novak
 */
public class TestMimeTypesResolver extends TestCase
{
  MimeTypesResolver resolver;

  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(TestMimeTypesResolver.class);
  }

  /*
   * @see TestCase#setUp()
   */
  protected void setUp() throws Exception
  {
    super.setUp();
    resolver = new MimeTypesResolver();
    resolver.init();
  }

  public void testGetMimeTypeFromExt()
  {
    MimeType mt;
    mt = resolver.getMimeTypeFromExt("snd");
    assertNotNull(mt);
    assertEquals("audio",mt.getMainType());
    assertEquals("basic",mt.getSubType());
    
    mt = resolver.getMimeTypeFromExt("tex");
    assertNotNull(mt);
    assertEquals("application",mt.getMainType());
    assertEquals("x-tex",mt.getSubType());
  }
  
  public void testImageType()
  {
    assertTrue("image/jpeg is image type", MimeTypesResolver.isImageType("image/jpeg"));
    assertFalse("application/xxxjpeg is not image type", MimeTypesResolver
                                                             .isImageType("application/xxxjpeg"));
  }

  public void testGetMimeType()
  {
    //TODO Implement getMimeType().
  }

  public void testGetMimeTypeFromMime()
  {
    //TODO Implement getMimeTypeFromMime().
  }

}
