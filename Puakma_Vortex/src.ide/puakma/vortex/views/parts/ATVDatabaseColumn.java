/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Mar 2, 2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.views.parts;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import puakma.coreide.objects2.TableColumn;
import puakma.vortex.WorkbenchUtils;

public class ATVDatabaseColumn extends ATVBaseNode
{
  private TableColumn column;

  public ATVDatabaseColumn(ATVBaseNode parent, TreeViewer viewer, TableColumn column)
  {
    super(parent, viewer);
    this.column = column;
  }

  public Image getIcon()
  {
    return WorkbenchUtils.getImageFromCache(column);
  }

  public Object getModel()
  {
    return column;
  }

  public String getText()
  {
    return column.getName();
  }
}
