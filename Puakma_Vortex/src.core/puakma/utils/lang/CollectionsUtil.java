/*
 * Author: Martin Novak
 * Date:   Mar 14, 2005
 */
package puakma.utils.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * This class contains some helper functions for manipulation with collections, and arrays.
 *
 * @author Martin Novak
 */
public class CollectionsUtil
{
  /**
   * Creates new unsynchronized list from array.
   *
   * @param array is the array which will be transfered to List object
   * @return new List object with the content of the array
   */
  public static <T> List<T> toList(T[] array)
  {
    List<T> l = new ArrayList<T>();
    
    for(T o : array)
      l.add(o);
    
    return l;
  }
  
  /**
   * Creates new synchronized list from array.
   *
   * @param array is the array which will be transfered to List object
   * @return new List object with the content of the array
   */
  public static <T> List<T> toSynchronizedList(T[] array)
  {
    List<T> l = new Vector<T>();
    
    for(T o : array)
      l.add(o);
    
    return l;
  }
  
  /**
   * Adds all items from array to list. It doesn't matter if the item is already in array
   * or not.
   *
   * @param list is the destination list.
   * @param array is the array to add
   */
  public static <T> void addArrayToList(List<T> list, T[] array)
  {
    for(T o : array)
      list.add(o);
  }
  
  /**
   * Adds all items from array to list. It matters if the item is already in array
   * or not - if yes, no action is performed. The checking is performed
   * by <code>List.contains()</code> function.
   *
   * @param list is the destination list.
   * @param array is the array to add
   */
  public static <T> void addArrayToListUniq(List<T> list, T[] array)
  {
    for(T o : array) {
      if(list.contains(o) == false)
        list.add(o);
    }
  }

  /**
   * Adds all items from source array to destination List which are not in the list.
   *
   * @param destList is the list into which are items added
   * @param array is the source array
   */
  public static void addArrayToListUniq(List<Integer> destList, int[] array)
  {
    for(int x : array) {
      Integer integer = new Integer(x);

      if(destList.contains(integer) == false)
        destList.add(integer);
    }
  }

  /**
   * Creates a new integer array. Copies all the items from List l to int array. All the items
   * in the list has to be of type Integer.
   *
   * @param list is the List containing all items to be added to array
   * @return array of int from list
   */
  public static int[] toIntArray(List<Integer> list)
  {
    int[] ret = new int[list.size()];
    int i = 0;

    for(int x : list) {
      ret[i] = x;
      i++;
    }

    return ret;
  }
  
  /**
   * Creates a new long array. Copies all the items from List l to int array. All the items
   * in the list has to be of type Long.
   *
   * @param list is the List containing all items to be added to array
   * @return array of longs from list
   */
  public static long[] toLongArray(List<Long> list)
  {
    long[] ret = new long[list.size()];
    int i = 0;

    for(long l : list) {
      ret[i] = l;
      i++;
    }

    return ret;
  }
  
  /**
   * Shifts items in the list.
   *
   * @param list is the list in which will be items shifted.
   * @param dest is the destination index
   * @param src is the source index.
   */
  public static <T> void shiftObjects(List<T> list, int dest, int src)
  {
    if(dest == src)
      return;

    T tmp = list.remove(src);
    list.add(dest, tmp);
  }
  
  /**
   * Substracts second list from the first, and returns result in a new list. Basicly creates
   * set operation first - second.
   *
   * @param first is the list from which we substract
   * @param second is the list which we substract from the first one
   * @param comparator is the custom comparator for those two objects
   * @return List object with the result
   */
  public static <T1, T2> List<T1> firstMinusSecond(List<T1> first, List<T2> second, EqualsComparator<T1, T2> comparator)
  {
    List<T1> ret = new ArrayList<T1>();
    
    main: for(T1 fo : first) {
      for(T2 so : second)
        if(comparator.equals(fo, so))
          continue main;
      
      ret.add(fo);
    }
    
    return ret;
  }
  
  /**
   * Substracts second list from the first, and returns result in a new list. Basicly creates
   * set operation first - second.
   *
   * @param first is the list from which we substract
   * @param second is the list which we substract from the first one
   * @param keyProvider provides keys for second list
   * @return List object with the result
   */
  public static <K, V, T> List<V> firstMinusSecond(Map<K, V> first, List<T> second,
                                                KeyProvider<T, K> keyProvider)
  {
    Map<K, V> ret = new HashMap<K, V>(first);
    
    for(T o : second) {
      K key = keyProvider.getKeyFor(o);
      ret.remove(key);
    }
    
    return new ArrayList<V>(ret.values());
  }

  /**
   * Checks if all items in the list are assignable to class type.
   */
  public static <T> boolean areAllItemsOfType(List<T> l, Class<?> clazz)
  {
    for(T o : l) {
      Class<?> c = o.getClass();
      if(clazz.isAssignableFrom(c) == false)
        return false;
    }
    
    return true;
  }

  /**
   * Checks if some of the items in the list are assignable to the class of some type.
   */
  public static <T> boolean areSomeItemsOfType(List<T> l, Class<?> clazz)
  {
    for(T o : l) {
      Class<?> c = o.getClass();
      if(clazz.isAssignableFrom(c))
        return true;
    }
    return false;
  }

  /**
   * Removes item from {@link Map} object. Note that this is linear remove which
   * doesn't use key. So whenever you have key for your Map object, use it!
   * Assumption is that every value is only ONCE in the Map.
   * 
   * @return true if the object has been removed, false otherwise
   */
  public static <K, V, P extends V> boolean removeValue(Map<K, V> m, P obj)
  {
    for(K key : m.keySet()) {
      V val = m.get(key);
      if(val.equals(obj)) {
        m.remove(key);
        return true;
      }
    }
    
    return false;
  }
}
