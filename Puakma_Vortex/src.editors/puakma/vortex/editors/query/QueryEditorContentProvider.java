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

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import puakma.coreide.objects2.SQLQuery;

public class QueryEditorContentProvider implements IStructuredContentProvider
{
  private SQLQuery queryInput;

  public Object[] getElements(Object inputElement)
  {
    int rows = queryInput.countRows();
    Integer[] I = new Integer[rows];
    for(int i = 0; i < rows; ++i) {
      I[i] = new Integer(i);
    }
    return I;
  }

  public void dispose()
  {
    queryInput = null;
  }

  public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
  {
    queryInput = (SQLQuery) newInput;
  }
}
