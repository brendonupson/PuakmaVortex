/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jan 6, 2006
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
 * This class represents event performed on database.
 *
 * @author Martin Novak
 * @deprecated
 */
public class DatabaseObjectEvent
{
  public static final int EV_ADD = 1;
  public static final int EV_CHANGE = 2;
  public static final int EV_REMOVE = 3;
  
  private DatabaseObject dbo;
  
  public DatabaseObjectEvent(DatabaseObject dbo, int type)
  {
    this.dbo = dbo;
  }
  
  public DatabaseObject getObject()
  {
    return dbo;
  }
}
