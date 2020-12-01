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
import puakma.coreide.objects2.JavaObject;
import puakma.utils.lang.ClassUtil;

/**
 * Implements design object containing java code. This needs class, and package
 * name handling.
 * 
 * @author Martin Novak
 */
class JavaObjectImpl extends DesignObjectImpl implements JavaObject
{
  /**
   * The default content type for java objects
   */
  private static final String JAVA_OBJECT_CONTENT_TYPE = "application/java";
  
  /**
   * Name of the server object package
   */
  private String packageName = "";
  
  /**
   * Name of the server object class
   */
  private String className = "";

  public JavaObjectImpl(ApplicationImpl application, int designType)
  {
    super(application, designType);
    
    assert PuakmaLibraryUtils.isValidJavaObjectType(designType) : "Invalid design type for java object";
    setContentType(JAVA_OBJECT_CONTENT_TYPE);
  }

  public void setClassName(String className)
  {
    if(className == null)
      throw new IllegalArgumentException("Cannot set class name to null");
    if(className.length() == 0)
      throw new IllegalArgumentException("Cannot set empty class name");
    
    if(className.equals(this.className) == false) {
      String oldClassName = this.className;
      this.className = className;
      setDirty(true);
      fireEvent(PROP_CLASSNAME, oldClassName, className);
    }
  }

  public String getClassName()
  {
    return className;
  }

  public void setPackage(String packageName)
  {
    if(packageName == null)
      throw new IllegalArgumentException("Cannot set package to null");
    
    if(packageName.equals(this.packageName) == false) {
      String oldPackageName = this.packageName;
      this.packageName = packageName;
      setDirty(true);
      fireEvent(PROP_PACKAGE, oldPackageName, packageName);
    }
  }

  public String getPackage()
  {
    return packageName;
  }
  
  public void setFullyQualifiedName(String packageName, String className)
  {
    if(packageName == null)
      throw new IllegalArgumentException("Package cannot be null");
    if(className == null)
      throw new IllegalArgumentException("Cannot set class name to null");
    if(className.length() == 0)
      throw new IllegalArgumentException("Cannot set empty class name");
    
    if(className.equals(this.className) == false || packageName.equals(this.packageName) == false) {
      String oldClassName = this.className;
      String oldPackage = this.packageName;
      this.className = className;
      this.packageName = packageName;
      setDirty(true);
      fireEvent(PROP_PACKAGE, oldPackage, packageName);
      fireEvent(PROP_CLASSNAME, oldClassName, className);
    }
  }

  public void setFullyQualifiedName(String name) throws PuakmaCoreException
  {
    if(name == null)
      throw new IllegalArgumentException("Cannot parse null fq name");
    
    String className = ClassUtil.getClassName(name);
    String packageName = ClassUtil.getPackageName(name);

    setFullyQualifiedName(packageName, className);
  }

  public String getFullyQualifiedName()
  {
    return (packageName == null ? "" : packageName + '.') + className;
  }
  
  protected void makeCopy(JavaObjectImpl copy, boolean wantWorkingCopy)
  {
    super.makeCopy(copy, wantWorkingCopy);

    copy.setPackage(getPackage());
    copy.setClassName(getClassName());
  }

  public DesignObject makeWorkingCopy()
  {
    JavaObjectImpl copy = new JavaObjectImpl((ApplicationImpl)getApplication(), getDesignType());
    makeCopy(copy, true);
    setupAsWorkingCopy(copy);
    return copy;
  }
  
  public DesignObject copy()
  {
    JavaObjectImpl copy = new JavaObjectImpl(null, getDesignType());
    
    makeCopy(copy, false);
    
    copy.setNew();
    copy.application = null;
    return copy;
  }

  public String toString()
  {
    return getFullyQualifiedName() + " [" + getId() + "]";
  }
}
