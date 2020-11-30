/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Nov 8, 2005
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

/**
 * The base interface for all the local designers. Implements some general functions.
 *
 * @author Martin Novak
 */
public interface DesignerBase
{
  /**
   * Sets up the soap client
   * @param url is the SOAPDesigner.pma application url. For example:
   *            https://server:8443/group/SOAPDesigner.pma
   * @param userName is the name of the user to work with the widgets
   * @param pwd is the user password
   */
  public void setup(String url, String userName, String pwd);
}
