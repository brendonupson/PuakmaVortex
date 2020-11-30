/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Nov 21, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.query;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import puakma.coreide.VortexDatabaseException;
import puakma.coreide.objects2.SQLQuery;
import puakma.vortex.VortexPlugin;

public class QueryEditorLabelProvider extends LabelProvider implements ITableLabelProvider
{
  private SQLQuery query;

  public QueryEditorLabelProvider()
  {
    super();
  }
  
  public void changeInput(SQLQuery query)
  {
    this.query = query;
  }

  public void dispose()
  {
    super.dispose();
    query = null;
  }

  public Image getColumnImage(Object element, int columnIndex)
  {
    return null;
  }

  public String getColumnText(Object element, int columnIndex)
  {
    try {
      Integer I = (Integer) element;
//      if(query.isValueFromRowHandleNull(I.intValue(), columnIndex))
//        return "";
      return query.valueFromRowHandle(I.intValue(), columnIndex);
    }
    catch(VortexDatabaseException e) {
      VortexPlugin.log(e);
      return "";
    }
  }
}
