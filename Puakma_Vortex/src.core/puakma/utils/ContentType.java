/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Aug 15, 2005
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

public interface ContentType
{
  public String getContentType();
  
  public String getMainType();
  
  public String getSubType();
  
  public String getEncoding();
  
  public String[] listExtensions();
}
