/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Oct 4, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.wizard;

import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.DesignObject;
import puakma.vortex.wizard.java.JavaObjectWizard;

public class NewScheduledActionWizard extends JavaObjectWizard
{
  public NewScheduledActionWizard()
  {
    this(null);
  }
  
  public NewScheduledActionWizard(Application application)
  {
    super(application, DesignObject.TYPE_SCHEDULEDACTION, TYPE_CLASS);
  }

}
