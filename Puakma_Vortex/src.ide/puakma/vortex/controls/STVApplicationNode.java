/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jan 20, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.controls;

import puakma.coreide.objects2.Application;


/**
 * @author Martin Novak
 */
public class STVApplicationNode extends TreeObject
{
  private Application application;

  STVApplicationNode(Application application, TreeParent parent)
  {
    super(application.getName(), parent);
    this.application = application;
  }
  
  public Application getApplication()
  {
    return application;
  }
}
