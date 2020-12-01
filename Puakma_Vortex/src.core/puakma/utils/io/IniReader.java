/*
 * Author: Martin Novak
 * Date:   Jun 12, 2005
 */
package puakma.utils.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Martin Novak
 */
public class IniReader
{
  public class Section {
    private Properties props = new Properties();
    private String name;
    
    public String getName()
    {
      return name;
    }

    public String get(String property)
    {
      return props.getProperty(property);
    }
  }
  
  private Map<String, Section> sections = new HashMap<String, Section>();
  private Section currentSection;

  public void load(InputStream is) throws IOException
  {
    LineNumberReader reader = new LineNumberReader(new InputStreamReader(is));
    String tmp = null;
    StringBuffer sb = new StringBuffer();

    while((tmp = reader.readLine()) != null ) {
      tmp = tmp.trim();
      if(tmp.startsWith("[") && tmp.endsWith("]")) {
        processSection(sb);
        newSection(tmp);
      }
      else {
        sb.append(tmp);
        sb.append('\n');
      }
    }
    
    if(sb != null)
      processSection(sb);
  }
  
  /**
   * Lists all sections in the ini file
   *
   * @return array with all the sections in the file
   */
  public Section[] listSections()
  {
    return sections.values().toArray(new Section[sections.size()]);
  }

  private void processSection(StringBuffer sb) throws IOException
  {
    Properties p = new Properties();
    p.load(new ByteArrayInputStream(sb.toString().getBytes("ISO-8859-1")));
    if(currentSection == null) {
      currentSection = new Section();
      currentSection.name = "";
    }
    
    currentSection.props = p;
    sections.put(currentSection.name, currentSection);
    sb.setLength(0);
  }

  private void newSection(String buf)
  {
    currentSection = new Section();
    currentSection.name = new String(buf.substring(1, buf.length() - 1));
  }

  /**
   * Returns the section defined by the name
   *
   * @param name is the name of the section
   * @return Section by name or null if section doesn't exist
   */
  public Section getSection(String name)
  {
    return sections.get(name);
  }
}
