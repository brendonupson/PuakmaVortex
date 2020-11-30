/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jul 1, 2005
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
 * @author Martin Novak
 */
public interface ApplicationListener
{
  /**
   * This handler is called when application is updated.
   *
   * @param event is the information about the application which is updated
   */
  public void update(ApplicationEvent event);
  
  /**
   * This handler is called when application is disconnected.
   *
   * @param application is the disconnected application
   */
  public void disconnect(Application application);
  
  /**
   * This event is fired when some object is changed in the application. All the information
   * about the change can be found in the <code>event</code> parameter.
   *
   * @param event <code>ObjectChangeEvent</code> object containing all the necessary information
   */
  public void objectChange(ObjectChangeEvent event);
}
