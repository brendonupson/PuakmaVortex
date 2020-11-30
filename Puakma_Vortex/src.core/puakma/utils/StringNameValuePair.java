/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Oct 26, 2005
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

public class StringNameValuePair
{
  private String name;
  private String value;

  public StringNameValuePair()
  {
    
  }

  public StringNameValuePair(String name)
  {
    this.name = name;
  }

  public StringNameValuePair(String name, String value)
  {
    this.name = name;
    this.value = value;
  }

  public String getName()
  {
    return this.name;
  }

  public String getValue()
  {
    return value;
  }

  public void setValue(String value)
  {
    this.value = value;
  }
}
