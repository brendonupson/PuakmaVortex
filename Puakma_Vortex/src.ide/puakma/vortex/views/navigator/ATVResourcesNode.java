/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Aug 9, 2005
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

import puakma.vortex.controls.TreeParent;

public class ATVResourcesNode extends ATVParentNode
{
  public ATVParentNode webNode;
  public ATVParentNode imagesNode;
  public ATVParentNode xmlNode;
  public ATVParentNode docsNode;
  public ATVParentNode othersNode;
  
  public ATVResourcesNode(String name, int nodeTypeId, TreeParent parent)
  {
    super(name, nodeTypeId, parent);
  }
}
