/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    13/09/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import puakma.coreide.designer.ApplicationStructureBean.DObject;
import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.JavaObject;
import puakma.coreide.objects2.TornadoDatabaseConstraints;
import puakma.utils.lang.CollectionsUtil;
import puakma.utils.lang.KeyProvider;

class DesignObjectsStore implements PropertyChangeListener, Iterable<DesignObjectImpl>
{
  private final ApplicationImpl application;

  private Map<Long, DesignObjectImpl> objectsId = new HashMap<Long, DesignObjectImpl>();

  private HashMap<String, DesignObjectImpl> objectsName = new HashMap<String, DesignObjectImpl>();

  DesignObjectsStore(ApplicationImpl impl)
  {
    application = impl;
  }

  public Iterator<DesignObjectImpl> iterator()
  {
    return objectsId.values().iterator();
  }

  public Object[] toArray(Object[] objs)
  {
    return objectsId.values().toArray(objs);
  }

  public int size()
  {
    return objectsId.size();
  }

  public void clear()
  {
    objectsId.clear();
    objectsName.clear();
  }

  public void add(DesignObjectImpl obj)
  {
    assert obj != null : "Cannot add null design object";
    obj.addListener(this);
    objectsId.put(new Long(obj.getId()), obj);
    objectsName.put(obj.getName(), obj);
    // objects.add(obj);
  }

  public void remove(DesignObjectImpl obj)
  {
    assert obj != null : "Cannot remove null design object";
    obj.removeListener(this);
    objectsId.remove(new Long(obj.getId()));
    if(objectsName.remove(obj.getName()) == null)
      CollectionsUtil.removeValue(objectsName, obj);
  }

  public void propertyChange(PropertyChangeEvent evt)
  {
    String prop = evt.getPropertyName();

    synchronized(application.dobjects) {
      if(DesignObject.PROP_NAME.equals(prop)) {
        String oldName = (String) evt.getOldValue();
        String newName = (String) evt.getNewValue();
        objectsName.remove(oldName);
        objectsName.put(newName, (DesignObjectImpl) evt.getSource());
      }
    }
  }

  /**
   * Subtracts designobject beans from design objects, so it returns {@link List} of
   * {@link DesignObject}s which are not on the server anymore.
   */
  List<DesignObjectImpl> substractBeans(List<DObject> beans)
  {
    List<DesignObjectImpl> ret = CollectionsUtil.firstMinusSecond(objectsId, beans,
                                   new KeyProvider<DObject, Long>() {
                                      public Long getKeyFor(DObject value) {
                                        return new Long(value.id);
                                      }
                                   });
    return ret;
  }

  /**
   * This function creates a new name for the java object, so it doesn't conflict with the
   * other java objects in the application. Note that there is an assumption that object
   * name is zero length before object enters the function.
   * 
   * <p>
   * So at first it tries to create class name.
   */
  static void setupNonConflictJavaObjectName(JavaObject object, DesignObjectsStore store,
                                             TornadoDatabaseConstraints cons)
  {
    setupNonConflictJavaObjectName(object, store.objectsName, cons);
  }

  static void setupNonConflictJavaObjectName(JavaObject object, Map<String, DesignObjectImpl> m,
                                             TornadoDatabaseConstraints cons)
  {
    String n = object.getClassName();
    int maxLen = cons.getMaxDObj_NameLen();
    if(n.length() > maxLen)
      n = n.substring(0, maxLen);
    if(m.get(n) != null) {
      int index = 0;
      while(true) {
        StringBuffer sb = new StringBuffer(n);
        sb.append(Integer.toString(index));
        if(sb.length() > maxLen) {
          n = n.substring(0, maxLen - 1);
          // index++;
          continue;
        }

        if(m.get(sb.toString()) == null) {
          object.setName(sb.toString());
          return;
        }

        index++;
      }
    }
    else
      object.setName(n);
  }
}
