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

/**
 * This interface introduces some basic event constants common to all the events
 * in the whole library like disconnect, etc... Note that events from this class
 * has reserved numbers in the range 1-19, so subclasses can fire events with
 * numbers greater than 20.
 *
 * @author Martin Novak
 */
public interface EventConstant
{
  public static final int EV_DISCONNECT = 1;
  
  public int getEventType();
}
