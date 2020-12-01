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


public class JavaProjectConfigurationImpl implements JavaProjectConfiguration
{
  private String javaVersion;
  private boolean usingLatestServerJdk;

  public String getJavaVersion()
  {
    return javaVersion;
  }

  public void setJavaVersion(String javaVersion)
  {
    this.javaVersion = javaVersion;
  }

  public void setUseLatestServerCompatibleJdk(boolean usingLatestServerJdk)
  {
    this.usingLatestServerJdk = usingLatestServerJdk;
  }

  public boolean useLatestServerCompatibleJdk()
  {
    return usingLatestServerJdk;
  }
}
