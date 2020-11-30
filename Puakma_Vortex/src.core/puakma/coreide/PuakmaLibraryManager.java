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
package puakma.coreide;

import java.io.IOException;

import puakma.coreide.objects2.ILogger;
import puakma.utils.MimeType;
import puakma.utils.MimeTypesResolver;


/**
 * Manages communication with outside world, and environment. You should use this
 * class also to configure puakma communication library.
 *
 * @author Martin Novak
 */
public class PuakmaLibraryManager
{
  /**
   * Some external logger to log the errors, warnings, etc...
   */
  private static ILogger logger;
  
  private static MimeTypesResolver mimesResolver;

  /**
   * Determines whether the library runs in the debug mode
   */
  public static boolean DEBUG_MODE = false;

  /**
   * Logs exception.
   *
   * @param e is the logged exception.
   */
  public static void log(Throwable e)
  {
    if(logger != null)
      logger.log(e.getLocalizedMessage(), e, ILogger.ERROR_ERROR);
  }

  /**
   * Logs some message.
   *
   * @param message is the message to log
   */
  public static void log(String message)
  {
    if(logger != null)
      logger.log(message, null, ILogger.ERROR_ERROR);
  }
  
  public static void log(String message, int level)
  {
    if(logger != null)
      logger.log(message, null, level);
  }

  /**
   * Logs error, and notifies user about error.
   *
   * @param message is the string to display to the user
   * @param e is the exception to display
   */
  public static void fireGlobalError(String message, Exception e)
  {
    if(logger != null)
      logger.log(message, e, ILogger.ERROR_FATAL);
  }

  /**
   * Setups puakma communication library.
   * 
   * @param logger is the logger object
   * @param debugMode 
   */
  public static void configure(ILogger logger, boolean debugMode)
  {
    PuakmaLibraryManager.logger = logger;
    DEBUG_MODE  = debugMode;
    
    // initialize mime types
    mimesResolver = new MimeTypesResolver();
    try {
      mimesResolver.init();
    }
    catch(IOException e) {
      log(e);
    }
    
    // install shutdown hook
//    Runtime.getRuntime().addShutdownHook(new Thread() {
//      public void run()
//      {
//        ServerManager.shutdown();
//      }
//    });
  }
  
  /**
   * Determines the most prefered extension from content type.
   *
   * @param contentType is the content type to guess
   * @return String with the most prefered extension. If the content type is unknown,
   * returns null.
   */
  public static String determineExtension(String contentType)
  {
    MimeType mt = mimesResolver.getMimeTypeFromMime(contentType);

    if(mt == null) {
      return null;
    }
    else {
      String ext = mt.getFirstExtension();
      return ext;
    }
  }
}
