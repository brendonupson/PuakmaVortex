/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    01/07/2006
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import puakma.coreide.designer.AppDesigner;
import puakma.coreide.designer.ApplicationStructureBean;
import puakma.coreide.designer.ApplicationStructureBean.DObject;
import puakma.coreide.event.InconsistenciesList;
import puakma.coreide.event.InconsistencyEvent;
import puakma.coreide.objects2.DatabaseObject;
import puakma.coreide.objects2.DesignObject;
import puakma.utils.lang.ArrayUtils;

public class InconsistenciesListImpl implements InconsistenciesList
{
  private List<InconsistencyEventImpl> events = new ArrayList<InconsistencyEventImpl>();
  private ApplicationStructureBean bean;
  private ApplicationImpl application;
  
  public InconsistenciesListImpl(ApplicationImpl application, ApplicationStructureBean bean)
  {
    this.application = application;
    this.bean = bean;
    
    findInconsistenciesInDesignObjects(bean);
  }
  
  private void findInconsistenciesInDesignObjects(ApplicationStructureBean bean)
  {
    Map<String, DObject[]> names = new HashMap<String, DObject[]>();
    Map<String, DObject[]> infected = new HashMap<String, DObject[]>();
    for(ApplicationStructureBean.DObject dob : bean.designObjects) {
      String name = dob.name;
      ApplicationStructureBean.DObject[] objs = names.get(name);
      if(objs != null) {
        objs = (DObject[]) ArrayUtils.append(objs, dob);
        infected.put(name, objs);
      }
      else {
        objs = new ApplicationStructureBean.DObject[1];
      }
      
      names.put(name, objs);
    }
    
    // NOW CREATE EVENTS FOR RESOLUTION
    List<DesignObjectImpl> l = new ArrayList<DesignObjectImpl>();
    for(DObject[] objs : names.values()) {
      l.clear();
      
      for(int i = 0; i < objs.length; ++i) {
        DesignObjectImpl obj = ObjectsFactory.setupDesignObjectFromBean(objs[i], null);
        l.add(obj);
      }
      InconsistencyEventImpl event = new InconsistencyEventImpl((DatabaseObject[]) l.toArray(new DesignObject[l.size()]));
      events.add(event);
    }
  }

  public InconsistencyEvent[] listEvents()
  {
    return events.toArray(new InconsistencyEvent[events.size()]);
  }

  public void resolveAll() throws VortexMultiException
  {
    VortexMultiException ex = new VortexMultiException();
    
    Iterator<InconsistencyEventImpl> it = events.iterator();
    while(it.hasNext()) {
      InconsistencyEventImpl event = it.next();
      try {
        resolveEvent(event);
      }
      catch(PuakmaCoreException e) {
        ex.addException(e);
      }
    }
    
    if(ex.countExceptions() > 0) {
      // OK, SO IF SOMETHING WENT WRONG, WE HAVE TO RELOAD APPLICATION FROM THE SERVER
      try {
        this.bean = application.loadAppStructureFromServer();
      }
      catch(Exception e) {
        PuakmaLibraryManager.log(e);
      }
      
      throw ex;
    }
  }

  private void resolveEvent(InconsistencyEventImpl event) throws PuakmaCoreException
  {
    if(event.getInconsistencyType().equals(InconsistencyEvent.INCONSISTENCY_DUPLICATE)) {
      resolveDuplicateNameInconsistency(event);
    }
    else
      throw new IllegalStateException("Invalid inconsistency state set");
  }

  private void resolveDuplicateNameInconsistency(InconsistencyEventImpl event) throws PuakmaCoreException
  {
    verifyInconsistency(event);
    
    if(event.designObjects != null)
      resolveDuplicateNameInDesignObjects(event);
    else
      resolveDuplicateNameInDatabaseObjects(event);
  }

  public void verifyInconsistency(InconsistencyEventImpl event) throws PuakmaCoreException
  {
    int unresolved = 0;
    StringBuffer sb = new StringBuffer();
    
    // AT FIRST CHECK IF THERE IS VALID RESOLUTION FOR AT LEAST ALL ITEMS MINUS ONE 
    for(int i = 0; i < event.removed.length; ++i) {
      if(event.removed[i] == false && event.resolutions[i] == null) {
        if(event.designObjects != null) {
          sb.append("Design object ");
          sb.append(event.designObjects[i].getName());
        }
        else {
          sb.append("Database object ");
          sb.append(event.databaseObjects[i]);
        }
        sb.append(" is still undecided.\n");
        unresolved++;
      }
      else if(event.resolutions[i] != null && event.resolutions[i] instanceof String == false) {
        throw new RuntimeException("Invalid resolution type - must be java.lang.String");
      }
    }
    
    if(unresolved > 1)
      throw new PuakmaCoreException(sb.toString());
  }

  private void resolveDuplicateNameInDesignObjects(InconsistencyEventImpl event) throws PuakmaCoreException
  {
    AppDesigner designer = application.getAppDesigner();
    for(int i = 0; i < event.designObjects.length; ++i) {
      DesignObject dob = event.designObjects[i];
      try {
        if(event.removed[i]) {
          designer.removeDesignObject(dob.getId());
          bean.designObjects.remove(event.beanDObjects[i]);
        }
        else {
          designer.updateDesignObjectName(dob.getId(), (String) event.resolutions[i]);
        }
      }
      catch(Exception e) {
        throw new PuakmaCoreException(e);
      }
    }
  }

  private void resolveDuplicateNameInDatabaseObjects(InconsistencyEventImpl event)
  {
    
  }

  /**
   * Counts all inconsistencies in the application.
   */
  public int countInconsistencies()
  {
    return events.size();
  }

  public InconsistencyEvent getEvent(int index)
  {
    return events.get(index);
  }
}
