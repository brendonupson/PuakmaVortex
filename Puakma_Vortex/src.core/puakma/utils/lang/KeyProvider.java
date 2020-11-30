/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    05/07/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.utils.lang;

/**
 * This class is used for retriving keys from value for some object. It's used
 * when manipulating with collections.
 * 
 * @author Martin Novak
 */
public interface KeyProvider<K, V>
{
  /**
   * This should return key usable in collections for some value.
   */
  public V getKeyFor(K value);
}
