/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    01/05/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide.objects2;

import puakma.coreide.ConnectionPrefs;
import puakma.coreide.VortexAuthentificationException;

/**
 * Interface which should ask user about troubles with password. Generally it
 * should ask user for a new password.
 * 
 * @author Martin Novak
 */
public interface AuthenticationDialog
{
  /**
   * Opens a new dialog, and asks user for a password
   * 
   * @param ex is the authentication exception thrown
   * @param prefs is the preferences which can be updated when user changes
   *          password/user name
   * @return true if user clicks ok, false otherwise
   */
  boolean open(VortexAuthentificationException ex, ConnectionPrefs prefs);

}
