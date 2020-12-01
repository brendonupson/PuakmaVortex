/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    30/06/2006
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

import puakma.coreide.objects2.DatabaseObject;
import puakma.coreide.objects2.DesignObject;

public interface InconsistencyEvent
{
  public static final String INCONSISTENCY_DUPLICATE = "duplicate";

  public DesignObject[] getDesignObjects();

  public DatabaseObject[] getDatabaseObjects();

  /**
   * Returns the type of inconsistency.
   */
  public String getInconsistencyType();

  /**
   * Sets the resolution for the inconsistency. It's up to the inconsistency
   * type to set the type of the object.
   */
  public void setResolution(DesignObject dob, Object resolution);

  /**
   * Sets the resolution for the inconsistency. It's up to the inconsistency
   * type to set the type of the object.
   */
  public void setResolution(DatabaseObject dbo, Object resolution);
  
  public void setRemoved(DesignObject dob, boolean removed);
  
  public void setRemoved(DatabaseObject dob, boolean removed);

  /**
   * Returns true if the inconsistency is still inconsistent.
   */
  public boolean isValid();

  /**
   * Gets the short description of error.
   */
  public String getText();

  /**
   * Gets the short description of the error in the design object, and the status.
   */
  public String getTextFor(Object object);
}
