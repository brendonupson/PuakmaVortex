/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jul 9, 2005
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import puakma.coreide.objects2.DesignObject;

/**
 * Container for all {@link RefreshEvent} events.
 * 
 * @author Martin Novak
 */
public class RefreshEventInfoImpl
{
  private List<RefreshEvent> events = new ArrayList<RefreshEvent>();

  public RefreshEvent[] listEvents()
  {
    return events.toArray(new RefreshEvent[events.size()]);
  }

  void addChange(DesignObject dob, String oldName, String oldAuthor, long oldCrcData,
                 long oldCrcSource, long oldDataSize, long oldSourceSize, long oldTime,
                 String oldClass, String oldPackage)
  {
    RefreshEvent event = new RefreshEvent(RefreshEvent.OP_CHANGE, dob);

    event.setOldAuthor(oldAuthor);
    event.setOldCrc32Data(oldCrcData);
    event.setOldCrc32Source(oldCrcSource);
    event.setOldDataSize(oldDataSize);
    event.setOldSourceSize(oldSourceSize);
    event.setOldName(oldName);
    event.setOldTime(oldTime);
    event.setOldClassName(oldClass);
    event.setOldPackageName(oldPackage);

    events.add(event);
  }

  void addAdd(DesignObject dob)
  {
    RefreshEvent event = new RefreshEvent(RefreshEvent.OP_ADD, dob);
    events.add(event);
  }

  void addRemoved(DesignObject dob)
  {
    RefreshEvent event = new RefreshEvent(RefreshEvent.OP_REMOVE, dob);
    events.add(event);
  }

  public RefreshEvent[] getRemovedObjects()
  {
    return listEventsOfType(RefreshEvent.OP_REMOVE);
  }

  private RefreshEvent[] listEventsOfType(int type)
  {
    List<RefreshEvent> l = new ArrayList<RefreshEvent>();
    Iterator<RefreshEvent> it = events.iterator();
    while(it.hasNext()) {
      RefreshEvent ev = it.next();
      if(ev.getType() == type)
        l.add(ev);
    }
    return l.toArray(new RefreshEvent[l.size()]);
  }

  public RefreshEvent[] getNewObjects()
  {
    return listEventsOfType(RefreshEvent.OP_ADD);
  }

  public RefreshEvent[] getChangedObjects()
  {
    return listEventsOfType(RefreshEvent.OP_CHANGE);
  }
}
