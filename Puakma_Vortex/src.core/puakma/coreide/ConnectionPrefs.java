/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Sep 29, 2005
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

/**
 * Stores information about saving connection configuration from and to file.
 * 
 * @author Martin Novak
 */
public interface ConnectionPrefs extends ConnectionPrefsReader
{
  /**
   * Copies all data from the reader object
   * 
   * @param reader is the readable connection preference
   */
  public void copyFrom(ConnectionPrefsReader reader);

  public void setHost(String host);

  public void setUser(String user);

  public void setPort(int port);

  public void setPwd(String pwd);

  public void setName(String name);

  public void setDesignerPath(String path);

  public void setUsingSsl(boolean useSsl);

  public void setApplication(String application);

  public void setGroup(String group);

  public void setSavePwd(boolean savePwd);

  /**
   * Helper method which tells that the content has been modified during for
   * example some authentication question.
   */
  public void setModified(boolean modified);

  /**
   * @return true if the modify flag has been set, false otherwise
   */
  public boolean isModified();

  /**
   * Returns the configuration manager, so passwords and some other fields can
   * be easily saved.
   */
  public ConfigurationManager getConfigurationManager();
}
