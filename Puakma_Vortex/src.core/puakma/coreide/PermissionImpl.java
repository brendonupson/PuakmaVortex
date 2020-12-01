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

import puakma.coreide.designer.AppDesigner;
import puakma.coreide.objects2.Permission;
import puakma.coreide.objects2.Role;

/**
 * @author Martin Novak
 */
class PermissionImpl extends ApplicationObjectImpl implements Permission
{
  /**
   * Associated role for this permission
   */
  private RoleImpl role;

  public PermissionImpl(RoleImpl role)
  {
    super((role == null) ? null : (ApplicationImpl) role.getApplication());
    
    this.role = role;
  }

  public void commit() throws PuakmaCoreException
  {
    if(isNew() == false && isWorkingCopy() == false)
      throw new PuakmaCoreException("Cannot commit server object, it has to be working copy");
    if(isRemoved())
      throw new PuakmaCoreException("Invalid object");

    synchronized(this) {
      if(isDirty() == false)
        return;

      AppDesigner designer = application.getAppDesigner();
      try {
        long permId = designer.savePermission(getId(), role.getId(), getName(), getDescription());
        if(id == -1)
          id = permId;
        
        if(original != null)
          original.copyFromWorkingCopy(this);
        
        setDirty(false);
      }
      catch(Exception e) {
        throw PuakmaLibraryUtils.handleException("Cannot save permission " + getName(), e);
      }
    }
  }

  public void remove() throws PuakmaCoreException
  {
    synchronized(this) {
      if(isWorkingCopy())
        throw new PuakmaCoreException("Cannot remove working copy");
      if(isRemoved())
        throw new PuakmaCoreException("Object is not on the server");

      AppDesigner designer;
      if(application != null)
        designer = application.getAppDesigner();
      else if(original != null)
        designer = ((ApplicationImpl) ((PermissionImpl) original).getApplication()).getAppDesigner();
      else
        throw new IllegalStateException("Invalid state of the model");
      
      try {
        designer.removePermission(getId());
        
        setRemoved();
        role.notifyRemove(this);
      }
      catch(Exception e) {
        throw PuakmaLibraryUtils.handleException("Cannot remove role " + getName(), e);
      }
    }
  }
  
  public String toString()
  {
    return name + "(" + id + ")";
  }
  
  public Permission makeWorkingCopy()
  {
    if(isWorkingCopy())
      return ((PermissionImpl)original).makeWorkingCopy();
    PermissionImpl perm = new PermissionImpl(null);

    super.makeCopy(perm);
    setupAsWorkingCopy(perm);
    perm.role = role;

    return perm;
  }

  public Role getRole()
  {
    return role;
  }

  public void close()
  {
    setApplication(null);
    setRemoved();
    role = null;
  }
}
