/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    May 17, 2005
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import puakma.coreide.designer.AppDesigner;
import puakma.coreide.objects2.Permission;
import puakma.coreide.objects2.Role;

/**
 * @author Martin Novak
 */
class RoleImpl extends ApplicationObjectImpl implements Role
{
  private List<PermissionImpl> permissions = new ArrayList<PermissionImpl>();

  /**
   * @param application
   */
  public RoleImpl(ApplicationImpl application)
  {
    super(application);
  }

  public Permission addPermission(String name, String description) throws PuakmaCoreException
  {
    synchronized(permissions) {
      // find the permission in the list of permissions
      PermissionImpl perm = (PermissionImpl) getPermission(name);
      if(perm != null)
        throw new PuakmaCoreException("Duplicate permission " + name);
      
      perm = new PermissionImpl(this);

      perm.setName(name);
      perm.setDescription(description);
      perm.setApplication((ApplicationImpl) getApplication());
      perm.commit();
      
      permissions.add(perm);
      return perm;
    }
  }
  
  /**
   * Imports existing permission. Doesn't import anything to server like addPermission function.
   *
   * @param id is identifier of the existing permission
   * @param name is the name of the permission
   * @param description is the description of the permission
   * @return new Permission object
   */
  public PermissionImpl importPermission(long id, String name, String description)
  {
    synchronized(permissions) {
      PermissionImpl perm = new PermissionImpl(this);
      
      perm.setId(id);
      perm.setName(name);
      perm.setDescription(description);
      
      permissions.add(perm);
      return perm;
    }
  }

  public void removePermission(Permission permission) throws PuakmaCoreException, IOException
  {
    permission.remove();
  }

  public Permission[] listPermissions()
  {
    Permission[] ret;
    synchronized(permissions) {
      ret = permissions.toArray(new Permission[permissions.size()]);
    }
    return ret;
  }

  public String getHashString()
  {
    return null;
  }

  public void commit() throws PuakmaCoreException
  {
    if(isRemoved())
      throw new PuakmaCoreException("Cannot commit removed object");
    if(isNew() == false && isWorkingCopy() == false)
      throw new PuakmaCoreException("Cannot commit server object, it has to be working copy");

    synchronized(this) {
      if(isDirty() == false)
        return;

      AppDesigner designer = null;
      try {
        designer = application.getAppDesigner();
        long roleId = designer.saveRole(application.getId(), getId(), getName(), getDescription());
        if(id == -1)
          id = roleId;

        if(isNew() == false)
          original.copyFromWorkingCopy(this);

        setValid();
        setDirty(false);
      }
      catch(Exception e) {
        throw PuakmaLibraryUtils.handleException("Cannot save role on server", e);
      }
    }
  }

  public Permission getPermission(String name)
  {
    synchronized(permissions) {
      Iterator<PermissionImpl> it = permissions.iterator();
      while(it.hasNext()) {
        Permission perm = it.next();
        //TODO: je to case sensitive???
        if(name.equals(perm.getName())) {
          return perm;
        }
      }

      return null;
    }
  }

  public void remove() throws PuakmaCoreException
  {
    synchronized(this) {
      if(isWorkingCopy())
        throw new PuakmaCoreException("Cannot remove working copy");
      if(isNew())
        throw new PuakmaCoreException("Cannot remove non-existing role");

      try {
        if(isRemoved() == false) {
          AppDesigner designer = application.getAppDesigner();
          designer.removeRole(id);
        }

        setRemoved();
        application.notifyRemove(this);
      }
      catch(Exception e) {
        throw PuakmaLibraryUtils.handleException("Cannot remove role " + getName(), e);
      }
    }
  }
  
  public String toString()
  {
    String ret;
    
    ret = "ROLE " + name + "(" + id + ") [ ";
    synchronized(permissions) {
      Iterator<PermissionImpl> it = permissions.iterator();
      while(it.hasNext()) {
        ret += it.next().toString();
        if(it.hasNext())
          ret += ", ";
      }
    }
    ret += " ]";
    
    return ret;
  }

  public Role makeWorkingCopy()
  {
    if(isWorkingCopy())
      return ((RoleImpl)original).makeWorkingCopy();
    RoleImpl role = new RoleImpl(null);

    super.makeCopy(role);
    setupAsWorkingCopy(role);
    role.permissions = permissions;

    return role;
  }

  /**
   * Finishes removal of the permission.
   *
   * @param perm is the permission which is removed
   */
  void notifyRemove(PermissionImpl perm)
  {
    synchronized(permissions) {
      permissions.remove(perm);
    }
  }

  public void close()
  {
    setApplication(null);
    setRemoved();
    
    Iterator<PermissionImpl> it = permissions.iterator();
    while(it.hasNext()) {
      PermissionImpl perm = it.next();
      perm.close();
    }
  }
  
  /**
   * Refreshes role from another one. Note that this has to be externally
   * synchronized, and then called update event. Sample:
   * 
   * <pre>
   * synchronized(role) {
   *   role.refreshFrom(anotherRole);
   *   if(role.isDirty())
   *     role.fireUpdateEvent();
   * }
   * </pre>
   *
   * @param role is the role from which we want to have refreshed role
   */
  void refreshFrom(RoleImpl role)
  {
    assert getId() == role.getId() : "Identifier has to be the same in the both objects when refreshing";
    
    super.refreshFrom(role);
    
    synchronized(permissions) {
      permissions.clear();
      Iterator<PermissionImpl> it = role.permissions.iterator();
      while(it.hasNext()) {
        Permission perm = it.next();
        importPermission(perm.getId(), perm.getName(), perm.getDescription());
      }
    }
  }
}
