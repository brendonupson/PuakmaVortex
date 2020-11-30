/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jul 15, 2005
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

import puakma.coreide.objects2.ApplicationObject;
import puakma.coreide.objects2.ApplicationObjectEvent;

public class ApplicationObjectEventImpl implements ApplicationObjectEvent
{
  protected int eventType;
  protected boolean renamed;
  protected String oldName;
  protected ApplicationObject object;
  
  public ApplicationObjectEventImpl(int eventType, ApplicationObject object)
  {
    this.eventType = eventType;
    this.object = object;
  }
  
  public ApplicationObjectEventImpl(int eventType, ApplicationObject object, String oldName)
  {
    this.eventType = eventType;
    this.object = object;
    this.oldName = oldName;
    if(oldName != null)
      renamed = true;
  }

  public int getEventType()
  {
    return eventType;
  }

  public boolean isRename()
  {
    return renamed;
  }

  public String getOldName()
  {
    return oldName;
  }

  public ApplicationObject getObject()
  {
    return object;
  }
}
