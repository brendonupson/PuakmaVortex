/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jun 12, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.database;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import puakma.utils.io.IniReader;
import puakma.vortex.VortexPlugin;

/**
 * @author Martin Novak
 */
public class JdbcSupport
{
  public static final String DRIVER_MYSQL = "com.mysql.jdbc.Driver";
  
  public static final String[] DRIVER_LIST = {
      DRIVER_MYSQL,
  };
  
  private static Map drivers = new HashMap();
  
  /**
   * This class contains description of one jdbc driver
   * 
   * @author Martin Novak
   */
  public static class DriverDescription {
    private String className;
    private String description;
    private String initialString;
    private String id;
    public String getClassName()
    {
      return className;
    }
    public String getDescription()
    {
      return description;
    }
    public String getInitialString()
    {
      return initialString;
    }
  }

  /**
   * Lists all the drivers available
   *
   * @return array with all the drivers information
   */
  public static DriverDescription[] listDrivers()
  {
    return (DriverDescription[]) drivers.values().toArray(new DriverDescription[drivers.size()]);
  }
  
  public static void initialize()
  {
    InputStream is = JdbcSupport.class.getResourceAsStream("drivers.properties");
    IniReader reader = new IniReader();
    try {
      reader.load(is);
      IniReader.Section[] sections = reader.listSections();
      IniReader.Section section;
      for(int i = 0; i < sections.length; ++i) {
        section = sections[i];
        int index = -1;
        if((index = section.getName().indexOf('.')) == -1) {
          DriverDescription desc = new DriverDescription();
          desc.id = section.getName();
          desc.className = section.get("className");
        }
        else {
          
        }
      }
    }
    catch(IOException e) {
      VortexPlugin.log(e);
    }
  }
}
