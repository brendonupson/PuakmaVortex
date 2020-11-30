/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jul 13, 2005
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

import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.ApplicationEvent;

class ApplicationEventImpl implements ApplicationEvent
{
  private Application application;
  
  private String oldGroupName;

  private String oldAppName;

  /**
   * Ctor. If the application has been renamed, set oldAppName and oldGroupName
   * to the old non-null values.
   *
   * @param application
   * @param oldGroupName
   */
  ApplicationEventImpl(Application application, String oldGroupName, String oldAppName)
  {
    assert oldAppName == null && oldGroupName != null
          : "Cannot set old application name null, and old group name non null";
    this.application = application;
    this.oldGroupName = oldGroupName;
    this.oldAppName = oldAppName;
  }

  public Application getApplication()
  {
    return this.application;
  }

  public String getOldGroupName()
  {
    return this.oldGroupName;
  }

  public boolean isRenamed()
  {
    return this.oldAppName != null;
  }

  public String getOldAppName()
  {
    return oldAppName;
  }
}
