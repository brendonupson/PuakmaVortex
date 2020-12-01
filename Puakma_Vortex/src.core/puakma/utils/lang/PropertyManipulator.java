/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 23, 2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.utils.lang;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * This class is usefull for manipulating with properties.
 * 
 * @author Martin Novak
 */
public class PropertyManipulator
{
  private Method writeMethod;
  private Method readMethod;
  private String propertyName;
  /**
   * There is saved the type of the property
   */
  private Class<?> clazz;

  public PropertyManipulator(Class<?> clazz, String propertyName)
  {
    this.propertyName = propertyName;
    try {
      PropertyDescriptor desc = new PropertyDescriptor(propertyName, clazz);
      this.clazz = desc.getPropertyType();
      writeMethod = desc.getWriteMethod();
      readMethod = desc.getReadMethod();
    }
    catch(IntrospectionException e) {
      throw new IllegalArgumentException(e.getLocalizedMessage());
    }
  }

  public void setPropertyOnObject(Object object, Object value)
  {
    try {
      if((clazz == Integer.class || clazz == Integer.TYPE) && value instanceof Integer == false) {
        String str = value.toString();
        value = new Integer(Integer.parseInt(str));
      }
      
      writeMethod.invoke(object, new Object[] { value });
    }
    catch(RuntimeException ex) {
      throw ex;
    }
    catch(Exception ex) {
      throw new RuntimeException(ex);
    }
  }
  
  /**
   * Returns true if the property can be changed because of no formatting errors.
   * NOT NEEDED????
   */
  boolean isAssignable(Object object, Object value)
  {
    if((clazz == Integer.class || clazz == Integer.TYPE) && value instanceof Integer == false) {
      
    }
    return true;
  }

  public Object getPropertyFromObject(Object object)
  {
    try {
      return readMethod.invoke(object, new Object[0]);
    }
    catch(Exception e) {
      throw new RuntimeException(e);
    }
  }

  public String toString()
  {
    return propertyName + "(" + clazz.getName() + ") [" + readMethod.getName() + ", "
        + writeMethod.getName() + "]";
  }
}
