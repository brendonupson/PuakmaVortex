/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Dec 1, 2005
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

import puakma.SOAP.SOAPFaultException;

public class VortexAuthentificationException extends PuakmaCoreException
{
  private static final long serialVersionUID = 2043882412337405609L;
  
  private static final String INVALID_USER_CREDENTIALS = "Invalid user name or password";

  private String userName;

  public VortexAuthentificationException(String userName)
  {
    super(INVALID_USER_CREDENTIALS);
   
    this.userName = userName;
  }
  
  public VortexAuthentificationException(SOAPFaultException ex)
  {
    super(INVALID_USER_CREDENTIALS);
    
    //this.userName = ex.getUserName();
    // TODO: get user name from the exception
    this.userName = "";
  }
  
  public String getUserName()
  {
    return userName;
  }
}
