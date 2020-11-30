/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Apr 4, 2006
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

import puakma.coreide.objects2.DesignObject;

/**
 * This class contains information about single file in the refresh event.
 * 
 * @author Martin Novak
 */
public class RefreshEvent
{
  public static final int OP_ADD = 0;
  public static final int OP_REMOVE = 1;
  public static final int OP_CHANGE = 2;

  private long oldCrc32Data;
  private long oldCrc32Source;
  /**
   * Note that design object is being already updated, so if you want to get it back, you
   * should do it manually in the ide.
   */
  private DesignObject dob;
  private long oldTime;
  private String oldAuthor;
  private long oldSourceSize;
  private long oldDataSize;
  private String oldName;
  private String oldClassName;
  private String oldPackageName;
  private int type;

  RefreshEvent(int type, DesignObject dob)
  {
    this.type = type;
    this.dob = dob;
  }

  public DesignObject getDob()
  {
    return dob;
  }

  public long getOldTime()
  {
    return oldTime;
  }

  void setOldTime(long oldTime)
  {
    this.oldTime = oldTime;
  }

  public long getOldCrc32Data()
  {
    return oldCrc32Data;
  }

  void setOldCrc32Data(long oldCrc32Data)
  {
    this.oldCrc32Data = oldCrc32Data;
  }

  public long getOldCrc32Source()
  {
    return oldCrc32Source;
  }

  void setOldCrc32Source(long oldCrc32Source)
  {
    this.oldCrc32Source = oldCrc32Source;
  }

  public long getOldDataSize()
  {
    return oldDataSize;
  }

  void setOldDataSize(long oldDataSize)
  {
    this.oldDataSize = oldDataSize;
  }

  public String getOldName()
  {
    return oldName;
  }

  void setOldName(String oldName)
  {
    this.oldName = oldName;
  }

  public long getOldSourceSize()
  {
    return oldSourceSize;
  }

  void setOldSourceSize(long oldSourceSize)
  {
    this.oldSourceSize = oldSourceSize;
  }

  public String getOldAuthor()
  {
    return oldAuthor;
  }

  void setOldAuthor(String previousAuthor)
  {
    this.oldAuthor = previousAuthor;
  }

  public int getType()
  {
    return type;
  }

  public String getOldClassName()
  {
    return oldClassName;
  }

  void setOldClassName(String oldClassName)
  {
    this.oldClassName = oldClassName;
  }

  public String getOldPackageName()
  {
    return oldPackageName;
  }

  void setOldPackageName(String oldPackageName)
  {
    this.oldPackageName = oldPackageName;
  }  
}
