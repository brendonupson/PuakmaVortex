/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jun 30, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide;

import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.ResourceObject;

/**
 * @author Martin Novak
 */
class ResourceObjectImpl extends DesignObjectImpl implements ResourceObject
{
  public ResourceObjectImpl(ApplicationImpl application, int designType)
  {
    super(application, designType);
    
    assert designType == TYPE_RESOURCE || designType == TYPE_PAGE || designType == TYPE_DOCUMENTATION
      || designType == TYPE_LIBRARY || designType == TYPE_JAR_LIBRARY || designType == TYPE_CONFIGURATION
      : "Invalid design type for resource";
    
    if(designType == TYPE_LIBRARY)
      setContentType("application/java-vm");
    else if(designType == TYPE_PAGE)
      setContentType("text/html");
    else if(designType == TYPE_JAR_LIBRARY)
      setContentType("application/java-archive");
  }

  public DesignObject makeWorkingCopy()
  {
    ResourceObjectImpl impl = new ResourceObjectImpl((ApplicationImpl) getApplication(), getDesignType());
    
    super.makeCopy(impl, true);
    setupAsWorkingCopy(impl);
    
    return impl;
  }
  
  public DesignObject copy()
  {
    ResourceObjectImpl impl = new ResourceObjectImpl(null, getDesignType());
    
    super.makeCopy(impl, false);
    impl.setNew();
    impl.application = null;
    
    return impl;
  }
}
