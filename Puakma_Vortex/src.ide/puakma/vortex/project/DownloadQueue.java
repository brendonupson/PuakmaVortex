/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jul 13, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.project;

import java.util.LinkedList;
import java.util.List;

public class DownloadQueue
{
  private Object lock = new Object();
  private List l = new LinkedList();
  private boolean running = true;

  public Object remove() {
    synchronized(lock) {
      while(true) {
        if(l.size() > 0) {
          return l.remove(0);
        }
        else if(running) {
          try { lock.wait(); } catch(InterruptedException e) { }
        }
        else
          return null;
      }
    }
  }
  
  public void add(Object o) {
    synchronized(lock) {
      l.add(o);
      lock.notifyAll();
    }
  }
  
  public void run() {
    synchronized(lock) {
      running = true;
      lock.notifyAll();
    }
  }
  
  public void quit() {
    synchronized(lock) {
      running = false;
      lock.notifyAll();
    }
  }
}
