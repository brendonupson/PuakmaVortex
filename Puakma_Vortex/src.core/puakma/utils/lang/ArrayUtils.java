/*
 * Author: Martin Novak
 * Date:   Mar 18, 2005
 */
package puakma.utils.lang;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 * In this class are some utilities for manipulation with arrays.
 * @author Martin Novak
 */
public class ArrayUtils
{
  /**
   * Resize the array to the new size. Returns the new array back.
   * @param clz is the type of the new array has to be some object type, not
   *          primitive type or interface. Can be also one dimensional array -
   *          the return type will be used as array the type of the clz item
   *          type.
   * @param array is the array from which we copy the objects
   * @param newSize is the new size of the array
   * @return array of the type specified by clz parameter
   */
  public static Object[] resize(Class<?> clz, Object[] array, int newSize)
  {
    if(newSize < 0)
      throw new IllegalArgumentException("New array size has to be bigger then 0");

    if(array != null && array.length == newSize)
      return array;

    if(clz.isArray())
      clz = clz.getComponentType();
    if(clz.isPrimitive())
      throw new IllegalArgumentException("Primitive type is not allowed here.");
    else if(clz.isInterface())
      throw new IllegalArgumentException("Interface type is not allowed here.");
    else if(clz.isArray())
      throw new IllegalArgumentException(
          "Multidimensional array type is not allowed here.");

    Object[] ret = (Object[]) Array.newInstance(clz, newSize);
    // copy the old values
    if(array != null)
      System.arraycopy(array, 0, ret, 0, Math.min(array.length, newSize));

    return ret;
  }

  /**
   * Traverses through the whole array, and tries to find if there are some
   * duplicated strings.
   * <p>
   * Complexity is: <code>O(n * log(n))</code>. From which n * log(n) is sort
   * array complexity - other parts have O(n).
   * @param array is the array which will be inspected
   * @return true if there are some duplicated values in the array
   */
  public static boolean hasDuplicatedValues(int[] array)
  {
    int[] a = new int[array.length];
    System.arraycopy(array, 0, a, 0, array.length);
    Arrays.sort(a);
    for(int i = 0; i < a.length - 1; ++i) {
      if(a[i] == a[i + 1])
        return true;
    }

    return false;
  }

  /**
   * Traverses through the whole array, and tries to find if there are some
   * duplicated values except exception.
   * <p>
   * Complexity is: <code>O(n * log(n))</code>. From which n * log(n) is sort
   * array complexity - other parts have O(n).
   * @param array is the array which will be inspected
   * @param except is the exception from the duplicated values
   * @return true if there are some duplicated values in the array
   */
  public static boolean hasDuplicatedValuesExcept(int[] array, int except)
  {
    int[] a = new int[array.length];
    System.arraycopy(array, 0, a, 0, array.length);
    Arrays.sort(a);
    for(int i = 0; i < a.length - 1; ++i) {
      if(a[i] == except)
        continue;

      if(a[i] == a[i + 1])
        return true;
    }

    return false;
  }

  /**
   * Traverses through the whole array, and tries to find if there are some
   * duplicated values except exception.
   * <p>
   * Complexity is: <code>O(n * log(n))</code>. From which n * log(n) is sort
   * array complexity - other parts have O(n).
   * @param array is the array which will be inspected
   * @param except is the exception from the duplicated values
   * @return true if there are some duplicated values in the array
   */
  public static boolean hasDuplicatedValuesExcept(long[] array, int except)
  {
    long[] a = new long[array.length];
    System.arraycopy(array, 0, a, 0, array.length);
    Arrays.sort(a);
    for(int i = 0; i < a.length - 1; ++i) {
      if(a[i] == except)
        continue;

      if(a[i] == a[i + 1])
        return true;
    }

    return false;
  }
  
  /**
   * Converts Integer array to native type int array.
   *
   * @param array is the Integer objects array to convert
   * @return int array with the same value as parameter array
   */
  public static int[] toNativeArray(Integer[] array)
  {
    int[] ret = new int[array.length];

    for(int i = 0; i < array.length; ++i) {
      ret[i] = array[i].intValue();
    }

    return ret;
  }

  /**
   * Converts Long array to native type long array.
   *
   * @param array is the Long objects array to convert
   * @return long array with the same value as parameter array
   */
  public static long[] toNativeArray(Long[] array)
  {
    long[] ret = new long[array.length];

    for(int i = 0; i < array.length; ++i) {
      ret[i] = array[i].longValue();
    }

    return ret;
  }

  public static int findSequence(byte[] buffer, byte[] searchedSeq)
  {
    if(0 >= buffer.length) {
      return (searchedSeq.length == 0 ? buffer.length : -1);
    }
    if(searchedSeq.length == 0) {
      return 0;
    }

    byte first = searchedSeq[0];
    int i = 0;
    int max = buffer.length - searchedSeq.length;

    startSearchForFirstChar: while(true) {
      /* Look for first character. */
      while(i <= max && buffer[i] != first) {
        i++;
      }
      if(i > max) {
        return -1;
      }

      /* Found first character, now look at the rest of v2 */
      int j = i + 1;
      int end = j + searchedSeq.length - 1;
      int k = 1;
      while(j < end) {
        if(buffer[j++] != searchedSeq[k++]) {
          i++;
          /* Look for str's first char again. */
          continue startSearchForFirstChar;
        }
      }
      return i; /* Found whole string. */
    }
  }

  /**
   * This function sums all the items in array, and return the result.
   *
   * @param array is the array to sum
   * @return sum of all array elements
   */
  public static long computeSum(int[] array)
  {
    long ret = 0;
    for(int i = 0; i < array.length; ++i) {
      ret += array[i];
    }
    return ret;
  }

  /**
   * Creates an array from two in the parameters. The return type is the type
   * of the first array in parameter.
   *
   * @param a1 is the first array. The function also get the type of return array
   *           from here
   * @param a2 is the second array in result
   * @return array containing at the beginning the content of the first array, then
   *         the second array
   */
  public static Object[] mergeArrays(Object[] a1, Object[] a2)
  {
    Object[] ret = (Object[]) Array.newInstance(a1.getClass().getComponentType(), a1.length + a2.length);
    System.arraycopy(a1, 0, ret, 0, a1.length);
    System.arraycopy(a2, 0, ret, a1.length, a2.length);
    return ret;
  }

  /**
   * Creates an array from two in the parameters. From the first array is taken
   * a1len objects, and from the second array is taken a2len objects. The return
   * type is the type of the first array in parameter.
   * 
   * @param a1 is the first array. The function also get the type of return
   *          array from here
   * @param a2 is the second array in result
   * @return array containing at the beginning the content of the first array,
   *         then the second array
   */
  public static Object[] mergeArrays(Object[] a1, Object[] a2, int a1len, int a2len)
  {
    Object[] ret = (Object[]) Array.newInstance(a1.getClass().getComponentType(), a1len + a2len);
    System.arraycopy(a1, 0, ret, 0, a1len);
    System.arraycopy(a2, a1len, ret, a1.length, a2len);
    return ret;
  }
  
  /**
   * Creates an array from two in the parameters. From the first array is taken
   * a1len objects, and from the second array is taken a2len objects. The return
   * type is the type of the first array in parameter.
   * 
   * @param a1 is the first array. The function also get the type of return
   *          array from here
   * @param a2 is the second array in result
   * @return array containing at the beginning the content of the first array,
   *         then the second array
   */
  public static byte[] mergeArrays(byte[] a1, byte[] a2, int a1len, int a2len)
  {
    byte[] ret = new byte[a1len + a2len];
    System.arraycopy(a1, 0, ret, 0, a1len);
    System.arraycopy(a2, a1len, ret, a1.length, a2len);
    return ret;
  }

  /**
   * Creates an array from the three ones in the parameters. The return type is
   * the type of the first array in parameter.
   *
   * @param a1 is the first array. The function also get the type of return array
   *           from here
   * @param a2 is the second array in result
   * @param a3 is the third array in result
   * @return array containing at the beginning the content of the first array, then
   *         the second array, and the third
   */
  public static Object[] mergeArrays(Object[] a1, Object[] a2, Object[] a3)
  {
    int newLen = a1.length + a2.length + a3.length;
    Object[] ret = (Object[]) Array.newInstance(a1.getClass().getComponentType(), newLen);
    System.arraycopy(a1, 0, ret, 0, a1.length);
    System.arraycopy(a2, 0, ret, a1.length, a2.length);
    System.arraycopy(a3, 0, ret, a1.length + a2.length, a3.length);
    return ret;
  }

  /**
   * This function appends some object o to array a
   * @param a is the array at the result's beginning
   * @param o is the object to append at the end
   * @return new array where a is at the beginning and o is the last element. Note that
   * the type of result array is the same as input array
   */
  public static Object[] append(Object[] a, Object o)
  {
    Object[] a1 = (Object[]) Array.newInstance(a.getClass().getComponentType(), a.length + 1);
    System.arraycopy(a, 0, a1, 0, a.length);
    a1[a.length] = o;
    return a1;
  }

  public static int indexOf(Object[] types, Object type)
  {
    for(int i = 0; i < types.length; ++i) {
      if(types[i].equals(type))
        return i;
    }
    
    return -1;
  }

  /**
   * Finds string in the array of strings ignoring case.
   *
   * @param array is the array of Strings to search in
   * @param str is the String to find
   * @return index of string or -1 if there is no such string
   */
  public static final int indexOfIgnoreCase(String[] array, String str)
  {
    for(int i = 0; i < array.length; ++i) {
      if(array[i].equalsIgnoreCase(str))
        return i;
    }
    
    return -1;
  }
  
  public static Object[] remove(Object[] a, Object o)
  {
    for(int i = 0; i < a.length; ++i) {
      if(a[i].equals(o)) {
        a = remove(a, i);
      }
    }
    return a;
  }

  public static Object[] remove(Object[] a, int index)
  {
    if(index >= a.length || index < 0)
      throw new IndexOutOfBoundsException();
    
    Object[] r = (Object[]) Array.newInstance(a.getClass().getComponentType(), a.length-1);
    System.arraycopy(a, 0, r, 0, index);
    if(index < a.length - 1)
      System.arraycopy(a, index + 1, r, index, a.length - index - 1);
    return r;
  }

  /**
   * Creates a new array, and copies the content of the array in parameter to the returned array.
   * 
   * @param a is the array to copy
   * @return array with the exactly same content as a
   */
  public static Object copyArray(Object[] a)
  {
    Object[] ret = (Object[]) Array.newInstance(a.getClass().getComponentType(), a.length);
    System.arraycopy(a, 0, ret, 0, a.length);
    return ret;
  }

  /**
   * This function creates a new array, and puts to this new array all non null values from the array
   * 'a'. Note that if 'a' doesn't have any null item, 'a' is returned.
   * 
   * @param a is the array we want to filter
   * @return array with non null items
   */
  public static Object[] filterNulls(Object[] a)
  {
    int size = 0;
    for(int i = 0; i < a.length; ++i) {
      if(a[i] == null)
        size++;
    }
    
    if(size == 0)
      return a;
    
    Object[] ret = (Object[]) Array.newInstance(a.getClass().getComponentType(), a.length - size);
    int j = 0;
    for(int i = 0; i < a.length; ++i) {
      if(a[i] != null) {
        ret[j] = a[i];
        j++;
      }
    }
    return ret;
  }

  /**
   * Creates a new array with the size of array a, and with the items to be at
   * the index in subarray of a. Note that the new array will have the type of
   * the subarray items.
   */
  public static Object[] createArrayFromSubIndex(Object[][] a, int index)
  {
    Class<?> type = a.getClass().getComponentType().getComponentType();
    Object[] ret = (Object[]) Array.newInstance(type, a.length);
    for(int i = 0; i < ret.length; ++i)
      ret[i] = a[i][index];
    return ret;
  }

  public static Object[] removeDuplicates(Object[] a)
  {
    List<Object> l = new ArrayList<Object>();
    OUTER_LOOP: for(int i = 0; i < a.length; ++i) {
      for(int j = i + 1; j < a.length; ++j) {
        if(a[i].equals(a[j]))
          continue OUTER_LOOP;
      }
      
      l.add(a[i]);
    }
    
    Class<?> type = a.getClass().getComponentType();
    Object[] ret = (Object[]) Array.newInstance(type, l.size());
    return l.toArray(ret);
  }

  //  
  // public static int findSequence(byte[] buffer, byte[] searchedSeq)
  // {
  // if(buffer.length < searchedSeq.length)
  // return (searchedSeq.length == 0 ? 0 : -1);
  //
  // int i = 0;
  // startSearch: while(true) {
  // while(i < buffer.length && buffer[i] != searchedSeq[0])
  // i++;
  // if(i > buffer.length)
  // return -1;
  //      
  // int j = i + 1;
  // int k = 1;
  // int end = j + searchedSeq.length - 1;
  // while(j < end) {
  // if(k == searchedSeq.length || j == buffer.length)
  // return -1;
  //
  // if(buffer[j++] != searchedSeq[k++]) {
  // i++;
  // continue startSearch;
  // }
  // }
  //
  // return i;// - searchedSeq.length;
  // }
  // }
  
  /**
   * Parses integer values combined by some separator
   */
  public static int[] parseIntArray(String value, char separator)
  {
    String sep = new String(new char[] { separator });
    StringTokenizer tokenizer = new StringTokenizer(value, sep);
    int tokens = tokenizer.countTokens();
    int[] ret = new int[tokens];
    int i = 0;
    while(tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken();
      try {
        ret[i] = Integer.parseInt(token);
        i++;
      }
      catch(Exception ex) {
        // IGNORE 
      }
    }
    if(i < ret.length) {
      int[] ret1 = new int[i];
      System.arraycopy(ret, 0, ret1, 0, i);
      return ret1;
    }
    
    return ret;
  }
}
