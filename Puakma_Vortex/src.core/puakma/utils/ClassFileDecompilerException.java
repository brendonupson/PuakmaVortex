/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Some lovely spring day 2005
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

import puakma.coreide.PuakmaCoreException;

/**
 * This exception is thrown when JavaDecompiler class doesn't recognize the
 * class name - it means parse error.
 *
 * @author Martin Novak
 */
public class ClassFileDecompilerException extends PuakmaCoreException
{
  private static final long serialVersionUID = -8032681658881426235L;

  public ClassFileDecompilerException()
  {
    super();
  }

  public ClassFileDecompilerException(String message)
  {
    super(message);
  }

  public ClassFileDecompilerException(Throwable cause)
  {
    super(cause);
  }

  public ClassFileDecompilerException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
