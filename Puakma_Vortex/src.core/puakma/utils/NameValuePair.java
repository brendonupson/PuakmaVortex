/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 11, 2005
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


/**
 * @author Martin Novak
 */
public class NameValuePair
{
  private String name;
  private Object value;
  
  public NameValuePair()
  {
    
  }
  
  public NameValuePair(String name, Object value)
  {
    this.name = name;
    this.value = value;
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public Object getValue()
  {
    return value;
  }
}
