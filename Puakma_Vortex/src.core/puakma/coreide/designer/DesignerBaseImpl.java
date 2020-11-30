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

public class DesignerBaseImpl implements DesignerBase
{
  /**
   * SOAP client used for talking to the server.
   */
  protected BaseSoapObject client;
  
  public DesignerBaseImpl()
  {
  }
  
  public DesignerBaseImpl(String url, String userName, String pwd)
  {
    setup(url, userName, pwd);
  }

  public void setup(String url, String userName, String pwd)
  {
    client = new BaseSoapObject(url, userName, pwd);
  }
}
