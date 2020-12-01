/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    10/05/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide;

import puakma.coreide.database.SQLCommandDescriptor;

public class VortexMultiDatabaseException extends VortexDatabaseException
{
  private static final long serialVersionUID = 5375327116846049068L;
  
  private SQLCommandDescriptor[] descs;
  private int errors;
  
  public void setupSQLCommandDescriptors(SQLCommandDescriptor[] descs)
  {
    this.descs = descs;
    for(int i = 0; i < descs.length; ++i) {
      if(descs[i].exceptionStackTrace != null)
        errors++;
    }
  }
  
  public SQLCommandDescriptor[] listSQLCommandDescriptors()
  {
    return descs;
  }
  
  public SQLCommandDescriptor[] listErrors()
  {
    SQLCommandDescriptor[] ret = new SQLCommandDescriptor[errors];
    int j = 0;
    for(int i = 0; i < descs.length; ++i) {
      if(descs[i].exceptionStackTrace != null) {
        ret[j] = descs[i];
        j++;
      }
    }
    
    return ret;
  }
}
