/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 9, 2005
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



/**
 * This interface is used for listening change of selection of the preferences or
 * some other combo box in some special composite controls.
 *
 * @author Martin Novak
 */
public interface ConnectionsPageListener
{
  /**
   * This is fired after user selects another connection.
   *
   * @param connectionName is the newly selected connection name
   */
  public void changeSelection(String connectionName);
  
  /**
   * This method is fired when user presses refresh button. There is not specified
   * which event should occur or some other details. Just general method.
   */
  public void refreshPressed();
}
