/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jul 13, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide.objects2;

public interface ApplicationEvent
{
  public Application getApplication();
  
  public String getOldGroupName();
  
  public String getOldAppName();
  
  /**
   * Checks if the application has been renamed.
   * 
   * @return true if the application has been renamed
   */
  public boolean isRenamed();
}
