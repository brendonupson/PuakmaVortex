/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jul 15, 2005
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

public interface ApplicationObjectEvent extends EventConstant
{
  public static final int EV_REMOVE = 20;
  public static final int EV_UPDATE = 21;

  public int getEventType();

  /**
   * Returns true if the application object has been also renamed in the update
   * event.
   *
   * @return true if app object has been renamed, false otherwise
   */
  public boolean isRename();
  
  /**
   * Gets the old name before renaming the application object.
   *
   * @return old name of the application object
   */
  public String getOldName();
  
  public ApplicationObject getObject();
}
