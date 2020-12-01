/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Nov 21, 2005
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

import puakma.coreide.database.DatabaseQueryResultBean.ServerException;

/**
 * This is the root class for all database errors in vortex.
 *
 * @author Martin Novak
 */
public class VortexDatabaseException extends PuakmaCoreException
{
  private static final long serialVersionUID = -1814479043037817991L;
  private String[] serverStackTrace;
  private String sqlState;
  private int errorCode;
  private String exceptionClassName;

  public VortexDatabaseException()
  {
    super();
  }
  
  public VortexDatabaseException(ServerException exception)
  {
    super(exception.message);
    this.exceptionClassName = exception.className;
    this.errorCode = exception.errorCode;
    this.sqlState = exception.sqlState;
    this.serverStackTrace = exception.stackTrace;
  }

  public VortexDatabaseException(String message)
  {
    super(message);
  }

  public VortexDatabaseException(String message, Throwable cause)
  {
    super(message, cause);
  }

  public VortexDatabaseException(Throwable cause)
  {
    super(cause);
  }

  public int getErrorCode()
  {
    return errorCode;
  }

  /**
   * Gets the class name of the server side exception. Although this would usually be SQLException,
   * it might be actually some other exception class.
   * 
   * @return String with server side exception class
   */
  public String getExceptionClassName()
  {
    return exceptionClassName;
  }

  /**
   * Gets stack trace generated on the server by some exception.
   * 
   * @return array filled with stack trace from server
   */
  public String[] getServerStackTrace()
  {
    return serverStackTrace;
  }

  public String getSqlState()
  {
    return sqlState;
  }
}
