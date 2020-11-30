/*
 * Author: Martin Novak
 * Date:   Nov 6, 2005
 */
package puakma.utils.lang;

import java.util.ArrayList;
import java.util.Collection;

public class UniqueArrayList<T> extends ArrayList<T>
{
  private static final long serialVersionUID = 1286570129495374085L;

  public UniqueArrayList()
  {
    super();
  }

  public UniqueArrayList(Collection<? extends T> c)
  {
    super();
    
    addAll(c);
  }

  public UniqueArrayList(int initialCapacity)
  {
    super(initialCapacity);
  }

  public void add(int index, T element)
  {
    if(super.contains(element) == false)
      super.add(index, element);
  }

  public boolean add(T o)
  {
    if(super.contains(o) == false)
      return super.add(o);
    return false;
  }

  public boolean addAll(Collection<? extends T> c)
  {
    for(T o : c)
      add(o);

    return true;
  }

  public boolean addAll(int index, Collection<? extends T> c)
  {
    for(T o : c) {
      if(contains(o)) {
        add(index, o);
        index++;
      }
    }
    return true;
  }
}
