/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    May 17, 2005
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

/**
 * This interface represents permission for the role on the server. Note that this class is
 * data class, and thus doesn't fires global application events for change.
 *
 * @author Martin Novak
 */
public interface Permission extends ApplicationObject
{
  /**
   * This function makes working copy for this permission object
   *
   * @return shadow copy of the Permission object
   */
  public Permission makeWorkingCopy();
  
  /**
   * Gets Role in which is this Permission living
   *
   * @return parent Role object
   */
  public Role getRole();
}
