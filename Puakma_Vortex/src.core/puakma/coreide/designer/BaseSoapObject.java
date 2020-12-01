/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Sep 28, 2004
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide.designer;

import puakma.SOAP.SOAPClient;

/**
 * This class is used as wrapper for some additional functionality for SOAPClient
 * like more accurate error handling, native types parameter handling, etc...
 *
 * @author Martin Novak
 */
public class BaseSoapObject extends SOAPClient
{
  private String userName;
  
  public BaseSoapObject(String serviceUrl, String userName, String pwd)
  {
    super();
    setURL(serviceUrl);
    setUserNamePassword(userName, pwd);
    this.userName = userName;
  }
  
  public synchronized void setMethod(String methodName)
  {
    super.setMethod(methodName);
    clearParameters();
  }
  
  public void addParameter(short i)
  {
    super.addParameter(new Short(i));
  }
  
  public void addParameter(char c)
  {
    super.addParameter(new Character(c));
  }
  
//  public Object execute() throws SOAPFaultException, IOException
//  {
//    try {
//      return super.execute();
//    }
//    catch(Exception e){
//      if(e instanceof SOAPFaultException) {
//        SOAPFaultException ex = (SOAPFaultException) e;
//        // HACK!!!
//        if(ex.getMessage().startsWith("401"))
//          throw new SoapAuthorizationFailed("Invalid user credentials", this.userName);
//      }
//
//      // Remove DesignWidgetException class name from the beginning of the message
//      // because DesignWidgetException is always thrown when there is some fault in my server side
//      // code
//      String msg = e.getMessage();
//      if(msg != null && msg.startsWith(DesignWidgetException.class.getName()))
//        msg = msg.substring(msg.indexOf(DesignWidgetException.class.getName()));
//
//      // THROW WRAPPER
//      throw new SoapCallException(msg);
//    }
//  }
  
  public String getUserName()
  {
    return userName;
  }
}
