/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    12-abr-2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide;

import java.util.Date;

/**
 * This class is a bean which represents a single event in the console.
 * 
 * @author Martin Novak
 */
public class ConsoleLogItem
{
  public static final int TYPE_INFO = 0;

  public static final int TYPE_ERROR = 1;
  
  public static final int tYPE_WARNING = 2;

  private long id;

  private int type;

  private String message;

  private String itemSource;

  private String userName;

  private String serverName;

  private long date;

  public long getId()
  {
    return id;
  }
  
  public void setId(long id)
  {
    this.id = id;
  }

  public int getType()
  {
    return type;
  }
  
  public void setType(int type)
  {
    this.type = type;
  }

  public String getMessage()
  {
    return message;
  }
  
  public void setMessage(String msg)
  {
    this.message = msg;
  }

  public String getItemSource()
  {
    return itemSource;
  }
  
  public void setItemSource(String itemSource)
  {
    this.itemSource = itemSource;
  }
  
  public String getServerName()
  {
    return serverName;
  }
  
  public void setServerName(String name)
  {
    this.serverName = name;
  }

  public String getUserName()
  {
    return userName;
  }
  
  public void setUserName(String userName)
  {
    this.userName = userName;
  }

  public Date getDate()
  {
    Date d = new Date(date);
    return d;
  }
  
  public void setDate(long date)
  {
    this.date = date;
  }
}
