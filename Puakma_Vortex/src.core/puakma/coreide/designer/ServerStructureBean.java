/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Sep 15, 2005
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

import java.util.ArrayList;
import java.util.List;

public class ServerStructureBean
{
  public String x500Name;
  public String serverString;
  public List<ApplicationStructureBean> apps = new ArrayList<ApplicationStructureBean>();
}
