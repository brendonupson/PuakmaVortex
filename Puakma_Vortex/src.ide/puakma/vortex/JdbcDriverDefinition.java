/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Aug 8, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex;

public class JdbcDriverDefinition
{
  private String name;
  private String driverClass;
  private String sampleUrl;
  
  public JdbcDriverDefinition(String name, String driverClass, String sampleUrl)
  {
    this.name = name;
    this.driverClass = driverClass;
    this.sampleUrl = sampleUrl;
  }
  
  public String getDriverClass()
  {
    return driverClass;
  }
  public String getName()
  {
    return name;
  }
  public String getSampleUrl()
  {
    return sampleUrl;
  }

}
