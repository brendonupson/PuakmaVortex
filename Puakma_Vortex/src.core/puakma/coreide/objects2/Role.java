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
package puakma.coreide.objects2;

import java.io.IOException;

import puakma.coreide.PuakmaCoreException;

/**
 * This class represents single role in the application.
 *
 * @author Martin Novak
 */
public interface Role extends ApplicationObject
{
  /**
   * This adds permission to application. And also commits automatically permission
   * to the server.
   *
   * @param name is the name of the new permission
   * @param description is the description
   * @return Permission which is already on the server
   * @throws PuakmaCoreException when permission with that name already exists or there is
   * some problem with commiting permission
   */
  public Permission addPermission(String name, String description) throws PuakmaCoreException;

  public void removePermission(Permission permission) throws PuakmaCoreException, IOException;

  /**
   * Lists all the permissions assigned to the Role.
   *
   * @return array with all assigned <code>Permission</code>s.
   */
  public Permission[] listPermissions();
  
  public Permission getPermission(String name);
  
  /**
   * Creates semi soft/semi hard copy of this object. Soft copy means that all references
   * on the other objects are kept, and hard means that all the values are independent.
   * This this working copy should be used to update stuff to the server, and then
   * automatically updates the real object shared for everyone.
   *
   * @return shadow copy of the Role object
   */
  public Role makeWorkingCopy();
}
