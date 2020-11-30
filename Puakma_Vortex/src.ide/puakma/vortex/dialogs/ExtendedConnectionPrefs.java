/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    25.12.2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.dialogs;

import puakma.coreide.ConnectionPrefsReader;

/**
 * This is the extended connection preference interface. Helps with easier manipulation with
 * the connection data in dialogs.
 *
 * @author Martin Novak
 */
public interface ExtendedConnectionPrefs extends ConnectionPrefsReader
{
  /**
   * Fills bean with connection preferences
   * @param reader is the connection preference to get data from. This parameter might be null to show
   * that no connection preference should be displayed
   */
  public void setup(ConnectionPrefsReader reader);

  /**
   * Sets the change status of the dialog
   * @param dirty false if the dialog has not been changed, and is in initial state - eg after save
   * of data
   */
  public void setDirty(boolean dirty);
  
  /**
   * @return true if some value in the dialog has changed
   */
  public boolean isDirty();
  
  /**
   * @return previous name which had the configuration as initial
   */
  public String getPreviousName();
  
  /**
   * @return true if the name of the configuration has changed after initial setup
   */
  public boolean hasNameChanged();
}
