/*
 * Author: Martin Novak
 * Date:   Jun 12, 2005
 */
package puakma.utils.io;

import junit.framework.TestCase;

/**
 * @author Martin Novak
 */
public class TestIniReader extends TestCase
{
  private IniReader reader;

  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(TestIniReader.class);
  }

  protected void setUp() throws Exception
  {
    reader = new IniReader();
    reader.load((getClass().getResourceAsStream("test.ini")));
    super.setUp();
  }

  public void testListSections()
  {
    IniReader.Section[] sections = reader.listSections();
    assertEquals(3, sections.length);
  }
  
  public void testGetSection()
  {
    IniReader.Section section = reader.getSection("section1");
    assertNotNull(section);
    assertEquals("section1", section.getName());
    
    assertEquals("12", section.get("ahoj"));
    assertNull(section.get("gagarin"));
  }
}
