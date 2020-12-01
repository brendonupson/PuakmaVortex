/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    May 5, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide.objects2;

import java.beans.PropertyChangeListener;
import java.io.IOException;

import puakma.coreide.PuakmaCoreException;

/**
 * Represents the generic server object.
 *
 * @author Martin Novak
 */
public interface ServerObject
{
  /**
   * This property is for setData() function. Event listeners get oldValue, and in new value they get
   * NameValuePair object
   */
  public static final String PROP_DATA = "data";
  
  public static final String PROP_NAME = "name";
  
  public static final String PROP_DESCRIPTION = "description";
  
  public static final String PROP_CLOSE = "close";
  
  /**
   * This should commit all the changes to the server. Note that this should commit only
   * working copy. Non working copy should throw exception <code>PuakmaCoreException</code>.
   */
  public void commit() throws PuakmaCoreException, IOException;

  /**
   * Removes the object from the application. Note that only original object can
   * remove the object, no one from working copies.
   */
  public void remove() throws PuakmaCoreException, IOException;
  
  /**
   * This checks if there were some changes since the last commitement. Note
   * that if the object is not working copy, it's never dirty. So only working
   * copies can be dirty.
   * 
   * @return true if the object is in the same state as on the server.
   */
  public boolean isDirty();
  
  /**
   * Checks if the object is new or already existing.
   *
   * @return true if the object is new
   */
  public boolean isNew();
  
  /**
   * Checks if the object is valid - if it's still on the server.
   *
   * @return true if object is still valid, false if not.
   */
  public boolean isValid();
  
  /**
   * This function checks if the objet has been removed from the server or is still
   * on the server.
   *
   * @return true if the object has been removed
   */
  public boolean isRemoved();
  
  /**
   * Sets name of this object. This function is again valid only if the object is working copy.
   *
   * @param name is the new name of the object.
   */
  public void setName(String name);
  
  /**
   * Gets the name of the object.
   *
   * @return string with the object name
   */
  public String getName();
  
  public String getDescription();
  
  public void setDescription(String description);
  
  /**
   * Gets identifier of the object.
   *
   * @return identifier of the server object or -1 if the object is not available
   * on the server
   */
  public long getId();
  
  /**
   * Returns Server object to which this object belongs.
   *
   * @return Server object or null if the object doesn't belong to the server.
   */
  public Server getServer();
  
  /**
   * This returns status of the object - whether it is working copy or not.
   *
   * @return true if the object is working copy, false otherwise
   */
  public boolean isWorkingCopy();
  
  public boolean isOpen();
  
  /**
   * Returns status of openess/closeness of object
   *
   * @return true if the object is close, false if the object is open
   */
  public boolean isClosed();
  
  public void setData(String key, Object data);
  
  public Object getData(String key);
  
  /**
   * Adds a property change listener. Note that now it can fire events for only certain events
   * specially in Application interface, and database related stuff...
   * 
   * @param listener is the listener to be added
   */
  public void addListener(PropertyChangeListener listener);
  
  /**
   * Removes listener for watching server object's properties change.
   *
   * @param listener is the listener which is to be removed
   */
  public void removeListener(PropertyChangeListener listener);
}
