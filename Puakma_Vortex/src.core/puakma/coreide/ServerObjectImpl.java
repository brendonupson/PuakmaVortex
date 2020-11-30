/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    May 17, 2005
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

import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import puakma.coreide.objects2.Server;
import puakma.coreide.objects2.ServerObject;
import puakma.utils.NameValuePair;
import puakma.utils.lang.ListenersList;
import puakma.utils.lang.StringUtil;

/**
 * @author Martin Novak
 */
abstract class ServerObjectImpl implements ServerObject
{
  /**
   * Identifier of the object. It's always (-1) if object doesn't exists.
   */
  protected long id = -1;
  protected String name = StringUtil.EMPTY_STRING;
  protected String description = StringUtil.EMPTY_STRING;

  /**
   * It's valid only and only if the object is working copy.
   */
  protected boolean dirty;
  protected ServerImpl server;
  
  /**
   * If this is working copy, then this value holds original.
   */
  protected ServerObjectImpl original;
  
  /**
   * List of listeners for this object
   */
  private ListenersList propListeners = new ListenersList();
  
  public static final int STATUS_NEW = 0;
  public static final int STATUS_VALID = 1;
  public static final int STATUS_REMOVED = 2;

  /**
   * This identifies if the object is still valid - it means if it was/wasn't removed from
   * the server.
   */
  protected int status = STATUS_NEW;
  
  /**
   * This field contains all the data assigned to the object. Key is String object, and value
   * can be whatever...
   */
  private Map<String, Object> dataTable = new HashMap<String, Object>(0);

  public ServerObjectImpl(ServerImpl server)
  {
    setServer(server);
  }
  
  protected void setServer(ServerImpl server)
  {
    this.server = server;
  }

  public boolean isDirty()
  {
    return dirty;
  }
  
  public boolean isNew()
  {
    return status == STATUS_NEW;
  }
  
  public boolean isValid()
  {
    if(isWorkingCopy())
      return original.isValid();

    return status == STATUS_VALID;
  }
  
  public boolean isRemoved()
  {
    if(isWorkingCopy())
      return original.isRemoved();
    
    return status == STATUS_REMOVED;
  }
  
  protected void setValid()
  {
    status = STATUS_VALID;
  }
  
  protected void setNew()
  {
    status = STATUS_NEW;
    id = -1;
  }
  
  protected void setRemoved()
  {
    status = STATUS_REMOVED;
  }
  
  public boolean isWorkingCopy()
  {
    return original != null;
  }
  
  void close()
  {
    fireEvent(PROP_CLOSE, null, null);
  }

  public void setName(String name)
  {
    if((name == null) || name.length() == 0)
      throw new IllegalArgumentException("Name of the object cannot be null or zero length");

    if(name.equals(this.name) == false) {
      String oldValue = this.name;
      this.name = name;
      setDirty(true);
      fireEvent(PROP_NAME, oldValue, name);
    }
  }

  /**
   * Sets dirty status
   *
   * @param dirty
   */
  void setDirty(boolean dirty)
  {
    this.dirty = dirty;
  }

  public String getName()
  {
    return name;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    if(description == null)
      throw new IllegalArgumentException("Invalid argument - description is null");
    
    if(description.equals(this.description) == false) {
      String oldDescription = description;
      this.description = description;
      setDirty(true);
      fireEvent(PROP_DESCRIPTION, oldDescription, description);
    }
  }

  public long getId()
  {
    return id;
  }
  
  /**
   * Sets identifier of this object. Note that this function is hidden from user, and can
   * be set up only by the library.
   *
   * @param id is new identifier of the object
   */
  public void setId(long id)
  {
    this.id = id;
    // no set dirty because id is not official public parameter
  }
  
  public Server getServer()
  {
    return server;
  }
  
  /**
   * This function sets up the working copy parameter.
   *
   * @param copy is the working copy to set up
   */
  protected void makeCopy(ServerObjectImpl copy)
  {
    copy.status = status;
    copy.description = description;
    copy.name = name;
    copy.server = server;
    copy.id = id;
  }
  
  /**
   * This function setups object in parameter as working copy of this object.
   *
   * @param workingCopy is the working copy object.
   */
  protected void setupAsWorkingCopy(ServerObjectImpl workingCopy)
  {
    workingCopy.original = this;
  }
  
  /**
   * Copies datas back from working copy.
   *
   * @param workingCopy
   */
  protected void copyFromWorkingCopy(ServerObjectImpl workingCopy)
  {
    this.setName(workingCopy.name);
    this.setDescription(workingCopy.description);
    this.setId(workingCopy.id);
  }

  /**
   * Refreshes server object from another object taken from the server
   *
   * @param impl
   */
  protected synchronized void refreshFrom(ServerObjectImpl impl)
  {
    setId(impl.getId());
    setName(impl.getName());
    setDescription(impl.getDescription());
  }
  
  public Object getData(String key)
  {
    synchronized(dataTable) {
      return dataTable.get(key);
    }
  }

  public void setData(String key, Object data)
  {
    assert key != null : "Key for data inserted to the object cannot be null";
    
    NameValuePair pair = new NameValuePair(key, data);
    Object oldValue;
    
    synchronized(dataTable) {
      oldValue = dataTable.get(key);
      if(data == null)
        dataTable.remove(key);
      else
        dataTable.put(key, data);
    }
    
    fireEvent(PROP_DATA, oldValue, pair);
  }
  
  public void addListener(PropertyChangeListener listener)
  {
    propListeners.addListener(listener);
  }

  public void removeListener(PropertyChangeListener listener)
  {
    propListeners.removeListener(listener);
  }
  
  /**
   * This function fires an event to all listeners. Note that those listeners are
   * PropertyChangeListeners, and there are still deprecated old ApplicationListeners
   * 
   * @param propName
   * @param oldValue
   * @param newValue
   */
  protected void fireEvent(String propName, Object oldValue, Object newValue)
  {
    propListeners.fireEvent(this, propName, oldValue, newValue);
  }
  
  protected void fireEvent(String propName, int oldValue, int newValue)
  {
    fireEvent(propName, new Integer(oldValue), new Integer(newValue));
  }
  
  protected void fireEvent(String propName, boolean oldValue, boolean newValue)
  {
    fireEvent(propName, Boolean.valueOf(oldValue), Boolean.valueOf(newValue));
  }
}
