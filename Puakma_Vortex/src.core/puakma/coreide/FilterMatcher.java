/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Sep 19, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide;

/**
 * This interface is useful for looping thru some collection, and selecting what
 * do we want to include in result and what we don't want.
 *
 * @author Martin Novak
 */
public interface FilterMatcher<T>
{
  /**
   * This function should return true if the object matches the internal user
   * criteria.
   *
   * @param obj is the object from collection or something like that...
   * @return true if object matches, false otherwise
   */
  public boolean matches(T obj);
}
