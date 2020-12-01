/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    10/06/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.project;


public interface JavaProjectConfiguration
{
  public static final String PROP_JAVA_VERSION = "javaVersion";
  
  public boolean useLatestServerCompatibleJdk();
  
  public void setUseLatestServerCompatibleJdk(boolean usingLatestServerJdk);
  
  public void setJavaVersion(String environmentName);
  
  public String getJavaVersion();
}
