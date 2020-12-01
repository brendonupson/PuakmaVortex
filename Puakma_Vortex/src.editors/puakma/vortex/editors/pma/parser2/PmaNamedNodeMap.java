package puakma.vortex.editors.pma.parser2;

import java.util.Iterator;

import org.eclipse.wst.xml.core.internal.contentmodel.CMNamedNodeMap;
import org.eclipse.wst.xml.core.internal.contentmodel.CMNode;

/**
 * An implementation of the CMNamedNodeMap interface. This class is intented to
 * be used as a container of attribute declarations. If someone wants to use
 * this class for other purpose, he must pay attention to the fact that this
 * class is tolerant of the key name case. That is, this class does not
 * distinguish "name", "NAME", and "Name" as a key name.
 */
public class PmaNamedNodeMap implements CMNamedNodeMap
{
  private java.util.Hashtable items = null;

  public PmaNamedNodeMap()
  {
    super();
    items = new java.util.Hashtable();
  }

  /**
   * getLength method
   * 
   * @return int
   */
  public int getLength()
  {
    return items.size();
  }

  /**
   * getNamedItem method
   * 
   * @return CMNode <code>null</code> for unknown keys.
   * @param name java.lang.String
   */
  public CMNode getNamedItem(String name)
  {
    String cookedName = makeCanonicalForm(name);
    if(!items.containsKey(cookedName))
      return null;
    return (CMNode) items.get(cookedName);
  }

  /**
   * item method
   * 
   * @return CMNode
   * @param index int
   */
  public CMNode item(int index)
  {
    Iterator iter = iterator();
    while(iter.hasNext()) {
      CMNode node = (CMNode) iter.next();
      if(--index < 0)
        return node;
    }
    return null;
  }

  /**
   * @return java.util.Iterator
   */
  public Iterator iterator()
  {
    return items.values().iterator();
  }

  /**
   * @return java.lang.String
   * @param rawForm java.lang.String
   */
  private String makeCanonicalForm(String rawForm)
  {
    return rawForm.toUpperCase();
  }

  /**
   * @param key java.lang.String
   * @param item java.lang.String
   */
  public void putNamedItem(String name, CMNode item)
  {
    String cookedName = makeCanonicalForm(name);
    if(items.containsKey(cookedName))
      return; // already registered.
    items.put(cookedName, item);
  }
}
