/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jan 9, 2005
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


/**
 * @author Martin Novak
 */
public interface ILogger
{
  public static final int ERROR_OK = 0;
  
  public static final int ERROR_INFO = 1;
  
  public static final int ERROR_WARNING = 2;
  
  public static final int ERROR_ERROR = 3;
  
  /**
   * Fatal errors are really fatal, and so should be logged to user interface.
   */
  public static final int ERROR_FATAL = 4;

  /**
   * Logs some text to external log system or user interface.
   *
   * @param messsage short problem description
   * @param e possible exception - if any
   * @param level sets the severity of the message
   */
  public void log(String messsage, Throwable e, int level);
}
