/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    1.11.2004
 
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

/**
 * @author Martin Novak
 */
public class PuakmaCoreException extends Exception
{
  private static final long serialVersionUID = 4881236844321148933L;

  public PuakmaCoreException()
  {
    super();
  }

  public PuakmaCoreException(String message)
  {
    super(message);
  }

  public PuakmaCoreException(String message, Throwable cause)
  {
    super(message, cause);
  }

  public PuakmaCoreException(Throwable cause)
  {
    super(cause);
  }
}
