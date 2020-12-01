/*
 * Author: Martin Novak
 * Date:   Jun 15, 2005
 */
package puakma.utils.lang;

/**
 * This interface defines the only method which is used by clients for comparing equality
 * of two objects.
 *
 * @author Martin Novak
 */
public interface EqualsComparator<T1, T2>
{
  /**
   * Compares objects o1 and o2. See java.lang.Object#equals function for description of
   * what equals means.
   *
   * @param o1 is the first object
   * @param o2 is the second object
   * @return <code>true</code> if o1 and o2 objects are equal
   * @see java.lang.Object#equals
   */
  public boolean equals(T1 o1, T2 o2);
}
