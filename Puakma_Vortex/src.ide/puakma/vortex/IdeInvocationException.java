/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Aug 10, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex;

/**
 * This exception is thrown when some subprocess gets fucked...
 *
 * @author Martin Novak
 */
public class IdeInvocationException extends IdeException
{
  private static final long serialVersionUID = 4927616323038487605L;

  public IdeInvocationException(Throwable cause)
  {
    super(cause);
  }
}
