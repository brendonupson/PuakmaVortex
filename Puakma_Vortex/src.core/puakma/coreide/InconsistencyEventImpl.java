/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    30/06/2006
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

import puakma.coreide.designer.ApplicationStructureBean;
import puakma.coreide.event.InconsistencyEvent;
import puakma.coreide.objects2.DatabaseObject;
import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.Table;
import puakma.coreide.objects2.TableColumn;

public class InconsistencyEventImpl implements InconsistencyEvent
{
  protected DatabaseObject[] databaseObjects;
  protected ApplicationStructureBean.DObject[] beanDObjects;
  protected DesignObject[] designObjects;
  protected Object[] resolutions;
  protected boolean[] removed;
  /**
   * Array with all indexes which dos/dbos has been marked for resolution
   */
  protected boolean[] resolved;
  private String type;
  private boolean valid;
  
  public InconsistencyEventImpl(DatabaseObject[] dbos)
  {
    resolutions = new Object[dbos.length];
    resolved = new boolean[dbos.length];
    removed = new boolean[dbos.length];
    databaseObjects = new DatabaseObject[dbos.length];
    System.arraycopy(dbos, 0, databaseObjects, 0, dbos.length);
  }
  
  public InconsistencyEventImpl(DesignObject[] dobs, ApplicationStructureBean.DObject[] beanDObjects)
  {
    resolutions = new Object[dobs.length];
    resolved = new boolean[dobs.length];
    removed = new boolean[dobs.length];
    designObjects = new DesignObject[dobs.length];
    this.beanDObjects = beanDObjects;
    System.arraycopy(dobs, 0, databaseObjects, 0, dobs.length);
  }

  public void setType(String type)
  {
    this.type = type;
  }
  
  public String getType()
  {
    return type;
  }
  
  public DatabaseObject[] getDatabaseObjects()
  {
    return databaseObjects;
  }

  public DesignObject[] getDesignObjects()
  {
    return designObjects;
  }

  public String getInconsistencyType()
  {
    return type;
  }
  
  public void setValid(boolean valid)
  {
    this.valid = valid;
  }

  public boolean isValid()
  {
    return valid;
  }

  public void setResolution(DesignObject dob, Object resolution)
  {
    int index = getDesignObjectIndex(dob);
    if(index == -1)
      throw new IllegalArgumentException("Cannot find the design object " + dob.getName());
    
    this.resolutions[index] = resolution;
    this.resolved[index] = true;
  }

  public void setResolution(DatabaseObject dbo, Object resolution)
  {
    int index = getDatabaseObjectIndex(dbo);
    if(index == -1)
      throw new IllegalArgumentException("Cannot find the database object " + dbo);
    
    this.resolutions[index] = resolution;
    this.resolved[index] = true;
  }

  public void setRemoved(DesignObject dob, boolean removed)
  {
    int index = getDesignObjectIndex(dob);
    if(index == -1)
      throw new IllegalArgumentException("Cannot find the design object " + dob.getName());
    
    this.removed[index] = removed;
    this.resolved[index] = true;
  }

  private int getDesignObjectIndex(DesignObject dob)
  {
    if(designObjects == null)
      return -1;
    
    for(int i = 0; i < designObjects.length; ++i) {
      if(designObjects.equals(dob))
        return i;
    }
    
    return -1;
  }

  public void setRemoved(DatabaseObject dbo, boolean removed)
  {
    int index = getDatabaseObjectIndex(dbo);
    if(index == -1)
      throw new IllegalArgumentException("Cannot find the database object " + dbo);
    
    this.removed[index] = removed;
    this.resolved[index] = true;
  }

  private int getDatabaseObjectIndex(DatabaseObject dbo)
  {
    if(databaseObjects == null)
      return -1;
    
    for(int i = 0; i < databaseObjects.length; ++i) {
      if(databaseObjects[i].equals(dbo))
        return i;
    }
    
    return -1;
  }

  public String getText()
  {
    if(type == INCONSISTENCY_DUPLICATE) {
      String name = getCurrentObjectName();
      if(designObjects != null) {
        return "Design object " + name + " has duplicate instances in the system database";
      }
      else
        return "Database object " + name + " has duplicate instances in the system database";
    }
    else throw new IllegalStateException("Invalid inconsistency type");
  }
  
  /**
   * Returns the current object name.
   */
  private String getCurrentObjectName()
  {
    if(designObjects != null) {
      return designObjects[0].getName();
    }
    else {
      if(databaseObjects[0] instanceof Table)
        return ((Table) databaseObjects[0]).getName();
      else
        return ((TableColumn) databaseObjects[0]).getName();
    }
  }

  public String getTextFor(Object object)
  {
    throw new IllegalStateException("Not implemented yet");
  }
}
