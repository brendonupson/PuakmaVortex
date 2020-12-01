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
package puakma.coreide;

import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.Server;
import puakma.coreide.objects2.ServerEvent;

class ServerEventImpl implements ServerEvent
{
  private int eventType;
  private ServerImpl server;
  private ApplicationImpl application;
  
  public ServerEventImpl(int eventType, ServerImpl server)
  {
    this.eventType = eventType;
    this.server = server;
  }
  
  public Server getServer()
  {
    return server;
  }

  public Application getApplication()
  {
    return application;
  }

  public int getEventType()
  {
    return eventType;
  }

  public void setApplication(ApplicationImpl application)
  {
    this.application = application;
  }
}
