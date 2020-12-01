/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    May 5, 2005
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
 * This is base class for all of the objects which are living in the application like
 * design objects, keywords, user roles, etc...
 *
 * @author Martin Novak
 */
public interface ApplicationObject extends ServerObject
{
  /**
   * Returns assigned application.
   *
   * @return Application object or null if the object is not living in the application
   */
  public Application getApplication();
}
