/*
 * Author: Martin Novak
 * Date:   Jun 30, 2005
 */
package puakma.utils.lang;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Martin Novak
 */
public class ClassUtil
{
  /**
   * Map with all classes we support for mapping from primitive types to object wrappers
   */
  public static final Map<Class<?>, Class<?>> classes = new HashMap<Class<?>, Class<?>>();
  
  static {
    classes.put(Byte.TYPE, Byte.class);
    classes.put(Character.TYPE, Character.class);
    classes.put(Short.TYPE, Short.class);
    classes.put(Integer.TYPE, Integer.class);
    classes.put(Long.TYPE, Long.class);
    
    classes.put(Float.TYPE, Float.class);
    classes.put(Double.TYPE, Double.class);
  }
  
  /**
   * Returns class name from the fully qualified class name. Note that this function doesn't
   * work with resource style name like package/className.class - you have to use function
   * getResourceName.
   *
   * @param fqName is the fully qualified class name
   * @return String with the class name
   */
  public static String getClassName(String fqName)
  {
    int index = fqName.lastIndexOf('.');
    if(index == -1)
      return fqName;
    String ret = fqName.substring(index + 1);
    return ret;
  }
  
  /**
   * Gets the package name for the fully qualified class name. If the package
   * name is none, returns empty string "".
   */
  public static String getPackageName(String fqName)
  {
    int index = fqName.lastIndexOf('.');
    if(index == -1)
      return "";
    String ret = fqName.substring(0,index);
    return ret;
  }

  /**
   * This function returns the base class name for the class name. It's eg for inner class
   * <code>package.CLAZZ$1$INNER</code> : <code>CLAZZ</code>.
   *
   * @param fqClassName is the fully qualified class name
   * @return base class name for all inner classes
   */
  public static String getBaseClass(String fqClassName)
  {
    String clz = getClassName(fqClassName);
    int index = clz.indexOf('$');
    if(index == -1)
      return clz;
    clz = clz.substring(0, index);
    return clz;
  }
  
  public static Object[] getObjectArrayFromObjects(Object[] a, Field f)
  {
    int size = a.length;
    Class<?> clazz = f.getType();
    if(clazz.isPrimitive())
      clazz = classes.get(clazz);
    
    Object[] ret = (Object[]) Array.newInstance(clazz, size);
    for(int i = 0; i < size; ++i) {
      try {
        ret[i] = f.get(a[i]);
      }
      catch(Exception e) {
        throw new RuntimeException(e);
      }
    }
    
    return ret;
  }

  public static long[] getLongArrayFromObjects(Object[] a, Field f)
  {
    int size = a.length;
    Class<?> clazz = f.getDeclaringClass();
    if(clazz.isPrimitive())
      clazz = classes.get(clazz);
    
    long[] ret = new long[size];
    for(int i = 0; i < size; ++i) {
      try {
        ret[i] = ((Long) f.get(a[i])).longValue();
      }
      catch(Exception e) {
        throw new RuntimeException(e);
      }
    }
    
    return ret;
  }
  
  public static boolean[] getBoolArrayFromObjects(Object[] a, Field f)
  {
    int size = a.length;
    Class<?> clazz = f.getDeclaringClass();
    if(clazz.isPrimitive())
      clazz = classes.get(clazz);
    
    boolean[] ret = new boolean[size];
    for(int i = 0; i < size; ++i) {
      try {
        ret[i] = ((Boolean) f.get(a[i])).booleanValue();
      }
      catch(Exception e) {
        throw new RuntimeException(e);
      }
    }
    
    return ret;
  }
  
  public static int[] getIntArrayFromObjects(Object[] a, Field f)
  {
    int size = a.length;
    Class<?> clazz = f.getDeclaringClass();
    if(clazz.isPrimitive())
      clazz = classes.get(clazz);
    
    int[] ret = new int[size];
    for(int i = 0; i < size; ++i) {
      try {
        ret[i] = ((Integer) f.get(a[i])).intValue();
      }
      catch(Exception e) {
        throw new RuntimeException(e);
      }
    }
    
    return ret;
  }
  
  /**
   * Checks if the package name is valid or not. Note that this also accepts empty package
   * name as a valid package name.
   * 
   * @param packageName is the package name to be checked
   * @return true if the package name is valid, false otherwise
   */
  public static boolean isValidPackageName(String packageName)
  {
    char c;
    int len = packageName.length();
    // WE ALLOW EMPTY PACKAGES
    if(len == 0)
      return true;
    
    c = packageName.charAt(0);
    if(Character.isJavaIdentifierStart(c) == false)
      return false;
    
    for(int i = 1; i < len; ++i) {
      c = packageName.charAt(i);
      if(Character.isJavaIdentifierPart(c) == false && c != '.')
        return false;
    }
    
    if(c == '.')
      return false;
    
    return true;
  }
}
