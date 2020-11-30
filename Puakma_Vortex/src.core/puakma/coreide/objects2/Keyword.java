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

import puakma.coreide.PuakmaCoreException;

/**
 * This class represents single keyword in the application.
 *
 * @author Martin Novak
 */
public interface Keyword extends ApplicationObject
{
  /**
   * This adds data to keyword. And also commits automatically data to the server.
   *
   * @param value is the value of the new datas at the end
   * @return index of added data
   */
  public int addData(String value);
  
  public int addData(String value, int index);
  
  /**
   * Replaces the string to the position of index in the keyword.
   *
   * @param value is the new value of data
   * @param index is the index where the string hsould be put
   * @throws PuakmaCoreException
   */
  public void setData(String value, int index);

  public int removeData(int index);
  
  /**
   * Lists all the permissions assigned to the Role.
   *
   * @return array with all assigned <code>Permission</code>s.
   */
  public String[] listData();
  
  public String getData(int index);
  
  /**
   * Creates semi soft/semi hard copy of this object. Soft copy means that all references
   * on the other objects are kept, and hard means that all the values are independent.
   * This this working copy should be used to update stuff to the server, and then
   * automatically updates the real object shared for everyone.
   *
   * @return shadow copy of the Keyword object
   */
  public Keyword makeWorkingCopy();

  /**
   * Commits data change to the server. This function can be called only on working copy.
   *
   * @throws PuakmaCoreException if something is wrong in communication with server
   */
  public void commitData() throws PuakmaCoreException;

  /**
   * This function shifts datas from srcIndex to destIndex.
   *
   * @param srcIndex is the index from which we are moving data
   * @param destIndex is the destination index
   */
  public void shiftValue(int srcIndex, int destIndex);
}
