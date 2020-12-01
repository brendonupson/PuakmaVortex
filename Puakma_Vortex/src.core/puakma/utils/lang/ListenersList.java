/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 1, 2006
 * 
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.utils.lang;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import puakma.coreide.PuakmaLibraryManager;

/**
 * This class is simplier version of PropertyChangeSupport. Also one noticeable feature is that if some
 * event handler fails, other handlers still proceeds.
 *
 * @author Martin Novak
 */
public class ListenersList
{
  private PropertyChangeListener[] listeners = new PropertyChangeListener[0];
  
  public synchronized void addListener(PropertyChangeListener listener)
  {
    listeners = (PropertyChangeListener[]) ArrayUtils.append(listeners, listener);
  }
  
  public synchronized void removeListener(PropertyChangeListener listener)
  {
    listeners = (PropertyChangeListener[]) ArrayUtils.remove(listeners, listener);
  }
  
  public void fireEvent(Object source, String propertyName, Object oldValue, Object newValue)
  {
    PropertyChangeListener[] ls;
    synchronized(this) {
      ls = (PropertyChangeListener[]) ArrayUtils.copyArray(listeners);
    }
    
    for(int i = 0; i < ls.length; ++i) {
      try {
        PropertyChangeEvent evt = new PropertyChangeEvent(source, propertyName, oldValue, newValue);
        ls[i].propertyChange(evt);
      }
      catch(Exception ex) {
        PuakmaLibraryManager.log(ex);
      }
    }
  }

  /**
   * Removes all listener from class.
   */
  public synchronized void clear()
  {
    listeners = new PropertyChangeListener[0];
  }
}
