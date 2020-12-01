/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Dec 11, 2004
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.views.navigator;

import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.Server;
import puakma.vortex.controls.TreeParent;

class PuakmaViewAppRootNode extends TreeParent
{
  private Application connection;
  
  public PuakmaViewAppRootNode(Application connection, TreeParent parent)
  {
    super("", parent);
    this.connection = connection;
    setName(getTitle());
  }

  /**
   * Creates the title which appears on the item.
   *
   * @return the title written on the item
   */
  private String getTitle()
  {
    StringBuffer sb = new StringBuffer();
    Server sc = connection.getServer();

    sb.append(sc.getUserName());
    sb.append('@');
    sb.append(sc.getHost());
    sb.append('/');
    if(connection.getGroup() != null) {
      sb.append(connection.getGroup());
      sb.append('/');
    }
    sb.append(connection.getName());
    
    return sb.toString();
  }
}

/**
 * Base class for application node. Types of node:
 *
 * <ul>
 * <li>application root node
 * <li>database connections root node
 * <li>security root node
 * <li>keywords root node
 * <li>pages root node
 * </ul>
 * 
 * @author Martin Novak
 */
public class PuakmaViewRootNode extends TreeParent
{
  public static final int NODE_DB_ROOT = 0;
  public static final int NODE_SECURITY_ROOT = 1;
  public static final int NODE_KEYWORDS_ROOT = 2;
  public static final int NODE_PAGES_ROOT = 3;
  public static final int NODE_LIBRARIES_ROOT = 4;
  public static final int NODE_CLASSES_ROOT = 5;
  public static final int NODE_ACTIONS_ROOT = 6;
  public static final int NODE_SCHEDUL_ROOT = 7;
  public static final int NODE_SOAP_ROOT = 8;
  
  private static final String[] map = {
      "Database Connections",
      "Security Settings",
      "Keywords",
      "Pages",
      "Libraries",
      "Sources",
      "Actions",
      "Scheduled Actions",
      "SOAP Widgets",
  };

  public PuakmaViewRootNode(PuakmaViewAppRootNode parent, int type)
  {
    super("", parent);
    setName(map[type]);
  }
}
