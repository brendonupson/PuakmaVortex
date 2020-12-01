/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Sep 20, 2005
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
 * This is adapter for ApplicationListener interface. Made just for lazy developers [-;
 * There is no other purpose of this class.
 *
 * @author Martin Novak
 */
public class ApplicationAdapter implements ApplicationListener
{

  public void update(ApplicationEvent event)
  {
  }

  public void disconnect(Application application)
  {
  }

  public void objectChange(ObjectChangeEvent event)
  {
  }

}
