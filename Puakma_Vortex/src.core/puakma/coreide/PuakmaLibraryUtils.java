/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jul 12, 2005
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
import puakma.coreide.objects2.DesignObject;

public class PuakmaLibraryUtils
{
  /**
   * This function checks if the design type passed is valid design type for java
   * objects like action, java class, widget...
   *
   * @param designType is the checked design type
   * @return true if the design type is valid, false otherwise
   */
  public static boolean isValidJavaObjectType(int designType)
  {
    return designType == DesignObject.TYPE_ACTION || designType == DesignObject.TYPE_LIBRARY
        || designType == DesignObject.TYPE_SCHEDULEDACTION || designType == DesignObject.TYPE_WIDGET;
  }
  
  public static PuakmaCoreException handleException(Throwable th)
  {
    if(th instanceof SOAPFaultException) {
        SOAPFaultException ex = (SOAPFaultException) th;
        // HACK!!!
        // TODO: add user name here
        if(ex.getMessage().startsWith("401"))
          return new VortexAuthentificationException(ex);//"Invalid user credentials", "");//this.userName);
    }
    else if(th instanceof PuakmaCoreException)
      return (PuakmaCoreException) th;
    
    return new PuakmaCoreException(th);
  }
  
  public static VortexDatabaseException handleDbException(Throwable th)
  {
    if(th instanceof VortexDatabaseException) {
      return (VortexDatabaseException) th;
    }

    return new VortexDatabaseException(th);
  }
  
  public static VortexDatabaseException handleDbException(String message, Throwable th)
  {
    if(th instanceof VortexDatabaseException)
      return (VortexDatabaseException) th;
    
    return new VortexDatabaseException(message, th);
  }

  public static PuakmaCoreException handleException(String string, Throwable e)
  {
    return handleException(e);
  }
}
