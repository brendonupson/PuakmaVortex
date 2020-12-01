/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    01/07/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide.event;

import puakma.coreide.PuakmaCoreException;

public class InconsistencyException extends PuakmaCoreException
{
  private static final long serialVersionUID = -5916009177430547232L;
  private InconsistencyEvent event;
  
  public InconsistencyException(InconsistencyEvent event, String message)
  {
    super(message);
    
    this.event = event;
  }
  
  public InconsistencyEvent getEvent()
  {
    return event;
  }
}
