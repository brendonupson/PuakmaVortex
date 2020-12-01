/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jul 14, 2005
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

public interface Parameters
{
  public static final String PARAM_OPEN_ACTION   = "OpenAction";
  public static final String PARAM_OPEN_ACTION_1 = "OpenAction1";
  public static final String PARAM_SAVE_ACTION   = "SaveAction";
  public static final String PARAM_SAVE_ACTION_1 = "SaveAction1";
  public static final String PARAM_DEFAULT_CHARSET = "DefaultCharset";
  public static final String PARAM_DEFAULT_OPEN = "DefaultOpen";
  public static final String PARAM_LOGIN_PAGE = "LoginPage";
  public static final String PARAM_PAGE_MODE = "PageMode";
  public static final String PARAM_PARENT_PAGE = "ParentPage";
  
  public void addParameter(String name, String value);
  
  /**
   * Resets all values of the parameter with the specified name to the single value.
   *
   * @param name is the name of the parameter
   * @param value is the single value of the parameter
   */
  public void setParameter(String name, String value);
  
  public void setParameters(String name, String[] values);
  
  public void removeParameter(String name);
  
  public void removeParameterValue(String name, String value);
  
  /**
   * Gets the first value of the parameter.
   * 
   * @param name is the name of the parameter
   * @return string with the value of the parameter or null if the parameter is not set
   */
  public String getParameterValue(String name);
  
  public String[] getParameterValues(String name);
  
  public String[] listParameters();
    
  /**
   * Commits all the parameters to the server
   *
   * @throws PuakmaCoreException
   */
  public void commitParams() throws PuakmaCoreException;
  
  /**
   * Checks if the parameter is parameter name reserved for use in pages.
   *
   * @param name is the name of parameter
   * @return true if param is reserved for pages
   */
  public boolean isReservedPageProperty(String name);
  
  /**
   * Checks if the parameter is parameter reserved for application's use
   * 
   * @param name of parameter
   * @return true if param is reserved for application
   */
  public boolean isReservedAppProperty(String name);
}
