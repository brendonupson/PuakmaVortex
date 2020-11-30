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

import puakma.coreide.objects2.Table;
import puakma.coreide.objects2.TableColumn;
import puakma.vortex.WorkbenchUtils;

public class ATVDatabaseTable extends ATVBaseNode
{
  private Table table;

  public ATVDatabaseTable(ATVBaseNode parent, TreeViewer viewer, Table table)
  {
    super(parent, viewer);
    
    this.table = table;
  }

  public Object[] getChildren()
  {
    TreeViewer viewer = getViewer();
    TableColumn[] cols = table.listColumns();
    Object[] ret = new Object[cols.length];
    for(int i = 0; i < cols.length; ++i) {
      ATVDatabaseColumn col = new ATVDatabaseColumn(this, viewer, cols[i]);
      ret[i] = col;
    }
    return ret;
  }

  public Image getIcon()
  {
    return WorkbenchUtils.getImageFromCache(table);
  }

  public Object getModel()
  {
    return table;
  }

  public String getText()
  {
    return table.getName();
  }
}
