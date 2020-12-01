/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Nov 25, 2005
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
 * This interface is useful when we want to read some connection preferences, but not write.
 *
 * @author Martin Novak
 */
public interface ConnectionPrefsReader
{
  /**
   * Default path to SOAPDesigner application.
   * The current value is <code>"/system/SOAPDesigner.pma"</code>.
   */
  public static final String DEFAULT_PATH = "/system/SOAPDesigner.pma";
  
  /**
   * @return name of the connection preference
   */
  public String getName();

  public String getHost();
  
  public String getUser();
  
  public int getPort();
  
  public String getPwd();
  
  public String getDesignerPath();
  
  public boolean isUsingSsl();
  
  public String getApplication();
  
  public String getGroup();
  
  public boolean getSavePwd();
}
