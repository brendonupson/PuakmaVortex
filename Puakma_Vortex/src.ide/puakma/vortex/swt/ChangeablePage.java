/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    16/05/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.swt;

import org.eclipse.swt.widgets.Composite;

public interface ChangeablePage extends PropertiesPageLifecycle
{
  /**
   * This function creates a composite for the properties page which can accept some events.
   * 
   * @param parent is the parent composite of the page
   * @return {@link Composite} object which was created for the tab.
   */
  public Composite createComposite(Composite parent);
}
