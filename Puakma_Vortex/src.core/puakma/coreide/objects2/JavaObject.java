/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jun 30, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide.objects2;

import puakma.coreide.PuakmaCoreException;

/**
 * This object represents the java source object on the server.
 *
 * @author Martin Novak
 */
public interface JavaObject extends DesignObject
{
  public static final String PROP_CLASSNAME = "className";
  
  public static final String PROP_PACKAGE = "package";
  
  public void setClassName(String className);
  
  public String getClassName();
  
  public void setPackage(String packageName);
  
  public String getPackage();
  
  /**
   * Sets fully qualified class name. Note that the class name has to be well formated,
   * otherwise you get exception - PuakmaCoreException
   *
   * @param name is the new FQ name for this object - something like: package.subpackage.className
   * @throws PuakmaCoreException if the class name is not well formated
   */
  public void setFullyQualifiedName(String name) throws PuakmaCoreException;
  
  public String getFullyQualifiedName();

  /**
   * 
   * @param packageName
   * @param className
   */
  public void setFullyQualifiedName(String packageName, String className);
}
