/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jan 10, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.utils;


/**
 * Synchronization object where one class is waiting for another one to pass.
 * The another object passes the point without problem. If it passes the border
 * then the second can continue at the point even if it wasn't wainting there.
 * 
 * <p>Global invariant is: in &lt;= out + 1
 *
 * @author Martin Novak
 */
public class FirstWaitForSecond
{
  private Object condition = new Object();
  private int in = 0;
  private int out = 0;
  
  /**
   * Non-blocking enter. Notifies or sets that the second one can proceed the exit
   * method.
   */
  public void notifyFirst()
  {
    synchronized(condition) {
      out++;
      condition.notifyAll();
    }
  }
  
  /**
   * If the condition of in &lt;= out is not satisfied then blocks thread.
   */
  public void waitForSecond()
  {
    synchronized(condition) {
      in++;
      while(checkCond() == false) {
        try {
            condition.wait();
        } catch(InterruptedException e) {}
      }
    }
  }

  private boolean checkCond()
  {
    if(in <= out) {
      return true;
    }
    return false;
  }
}
