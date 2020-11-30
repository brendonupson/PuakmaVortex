/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Oct 9, 2005
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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.ICompilationUnit;

import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.JavaObject;
import puakma.coreide.objects2.ResourceObject;
import puakma.vortex.project.ProjectUtils;

public class DesignObjectTreeWrap implements IAdaptable
{
  private DesignObject object;
  
  public DesignObjectTreeWrap(DesignObject object)
  {
    this.object = object;
  }
  
  public Object getAdapter(Class adapter)
  {
    if(adapter.isAssignableFrom(DesignObject.class))
      return object;
    else if(object instanceof JavaObject && adapter.isAssignableFrom(JavaObject.class))
      return object;
    else if(object instanceof ResourceObject && adapter.isAssignableFrom(ResourceObject.class))
      return object;
    else if(adapter.isAssignableFrom(ICompilationUnit.class) && object instanceof JavaObject) {
      return ProjectUtils.getCompilationUnit((JavaObject) object);
    }
    return null;
  }
  
  public String toString()
  {
    return object.toString();
  }
}
