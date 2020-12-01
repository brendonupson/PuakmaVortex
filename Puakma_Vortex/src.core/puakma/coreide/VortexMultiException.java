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
package puakma.coreide;

import java.util.ArrayList;
import java.util.List;

/**
 * This exception is useful when we want to pass more exceptions at once.
 * 
 * @author Martin Novak
 */
public class VortexMultiException extends PuakmaCoreException
{
  private static final long serialVersionUID = -3506887776483586089L;

  private List<Exception> exceptions = new ArrayList<Exception>();

  public void addException(Exception ex)
  {
    exceptions.add(ex);
  }

  /**
   * Lists all passed exceptions.
   * 
   * @return array with all exceptions or empty array if there is no exception
   */
  public Exception[] listExceptions()
  {
    return exceptions.toArray(new Exception[exceptions.size()]);
  }

  /**
   * Returns true if this exception doesn't contain any root exception.
   */
  public boolean isEmpty()
  {
    return exceptions.size() == 0;
  }

  /**
   * Returns how many exception contains this exception.
   */
  public int countExceptions()
  {
    return exceptions.size();
  }
}
