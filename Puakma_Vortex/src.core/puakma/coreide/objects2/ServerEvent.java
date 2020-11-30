/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jul 20, 2005
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

public interface ServerEvent extends EventConstant
{
  /**
   * This event is fired when some application is connected.
   */
  public static final int EV_APPLICATION_CONNECT = 40;
  
  /**
   * This event is fired when we disconnect from some application.
   */
  public static final int EV_APPLICATION_DISCONNECT = 41;

  /**
   * Fired when application is removed. Note this can occured after
   * the application is disconnected and removed.
   */
  public static final int EV_APPLICATION_REMOVE = 42;

  /**
   * This is fired when some application is added
   */
  public static final int EV_APPLICATION_ADDED = 43;

  public Server getServer();
  
  /**
   * This returns Application object to which is source fired for.
   *
   * @return <code>Application</code> object
   */
  public Application getApplication();
}
