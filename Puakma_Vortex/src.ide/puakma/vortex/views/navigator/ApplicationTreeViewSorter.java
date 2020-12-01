/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    May 6, 2005
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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import puakma.coreide.objects2.TableColumn;


/**
 * Customized tree sorter which doesn't sort the second level nodes like Pages, Classes, etc...
 *
 * @author Martin Novak
 */
public class ApplicationTreeViewSorter extends ViewerSorter
{
  public int compare(Viewer viewer, Object e1, Object e2)
  {
    if(e1 instanceof ATVParentNode || e2 instanceof ATVParentNode)
      return -1;
    if(e1 instanceof IPackageFragment && e2 instanceof ICompilationUnit)
      return -1;
    if(e1 instanceof ICompilationUnit && e2 instanceof IPackageFragment)
      return 1;
    
    if(e1 instanceof TableColumn && e2 instanceof TableColumn) {
      TableColumn c1 = (TableColumn) e1, c2 = (TableColumn) e2;
      return c2.getPosition() - c1.getPosition();
    }

    return super.compare(viewer, e1, e2);
  }
}
