/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Aug 15, 2005
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


/**
 * This class is about project lifecycle listening. You should add listener after
 * you catch signal that the application has been connected. At this time eclipse
 * project should already exist.
 *
 * @author Martin Novak
 */
public interface ProjectLifecycleListener
{
  /**
   * This is fired after project is created
   */
  public static final int POST_CREATE = 1;
  
  /*
   * This event is fired after the project downloads all the java files.
   */
  public static final int POST_JAVA_START = 2;
  
  /**
   * Fired before project closes (efectively this means after project disconnects
   * from the application)
   */
  public static final int CLOSE = 3;

  /**
   * Fired after called refresh operation
   */
  public static final int POST_REFRESH = 4;
  
  /**
   * This broadcasts all crc32 changes in the whole application. Note that in parameter param
   * in {@link #projectEvent(PuakmaProject2, int, Object)} is passed array of {@link Crc32Change}
   * objects with change information.
   */
  public static final int CRC32_CHANGED_OBJECTS = 5;
  
  /**
   * This is fired when some event occurs.
   * 
   * @param project is the puakma project occured
   * @param eventType is the event type which occured
   * @param param is the parameter passed to the listener.
   */
  public void projectEvent(PuakmaProject2 project, int eventType, Object param);
}
