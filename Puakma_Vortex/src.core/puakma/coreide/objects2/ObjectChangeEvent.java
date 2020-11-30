/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jul 7, 2005
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
 * This is event fired in the event <code>ApplicationListener#objectChange()</code>
 * 
 * @author Martin Novak
 */
public class ObjectChangeEvent
{
  public static final int EV_ADD_APP_OBJECT = 1;
  public static final int EV_CHANGE = 2;
  public static final int EV_REMOVE = 3;

  private int event;
  private ApplicationObject object;
  private String oldPackage;
  private String oldClass;
  private String oldName;
  private boolean isRefresh;
  
  public ObjectChangeEvent(int event, ApplicationObject appObject, boolean isRefresh)
  {
    assert event > 0 && event <= 4 : "Invalid event type";

    this.event = event;
    this.object = appObject;
    this.isRefresh = isRefresh;
  }
  
  public ObjectChangeEvent(ApplicationObject appObject, String oldName, boolean isRefresh)
  {
    this.event = ObjectChangeEvent.EV_CHANGE;
    this.object = appObject;
    this.oldName = oldName;
    this.isRefresh = isRefresh;
  }
  
  public ObjectChangeEvent(JavaObject appObject, String oldPackage, String oldClass, boolean isRefresh)
  {
    this.event = ObjectChangeEvent.EV_CHANGE;
    this.object = appObject;
    this.oldPackage = oldPackage;
    this.oldClass = oldClass;
    this.isRefresh = isRefresh;
  }
  
  public int getEventType()
  {
    return event;
  }
  
  public ApplicationObject getObject()
  {
    return object;
  }
  
  public String getOldClass()
  {
    return oldClass;
  }

  public String getOldName()
  {
    return oldName;
  }

  public String getOldPackage()
  {
    return oldPackage;
  }
  
  public boolean isRefresh()
  {
    return isRefresh;
  }

  /**
   * Checks if the event renamed the application object
   * @return true if the application object has been renamed
   */
  public boolean isRenamed()
  {
    return oldName != null;
  }
  
  public boolean classChanged()
  {
    return oldClass != null;
  }

  public void setOldName(String oldName)
  {
    this.oldName = oldName;
  }

  public void setOldClass(String oldClass)
  {
    this.oldClass = oldClass;
  }

  public void setOldPackage(String oldPackage)
  {
    this.oldPackage = oldPackage;
  }
}
