/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    May 5, 2005
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.zip.CRC32;

import puakma.coreide.PuakmaCoreException;

/**
 * This interface represents any design object presented on the server.
 *
 * @author Martin Novak
 */
public interface DesignObject extends ApplicationObject, Parameters
{
  String PROP_OPTIONS = "options";
  
  /*------------------------------------------------------------------------------
   * Design types constants
   */
  /**
   * This is virtual type - only for internal refrence. Always has to be converted to
   * <code>TYPE_LIBRARY</code> constant.
   */
  int TYPE_JAR_LIBRARY = -2;
  int TYPE_ERROR = 0;
  int TYPE_PAGE = 1;
  int TYPE_RESOURCE = 2;
  int TYPE_ACTION = 3;
  /**
   * Java classes. Except actions, scheduled actions, and widgets.
   */
  int TYPE_LIBRARY = 4;
  /**
   * @deprecated - not used on the server anymore
   */
  int TYPE_TASK = 5;
  int TYPE_SCHEDULEDACTION = 6;
  /**
   * SOAP Widget
   */
  int TYPE_WIDGET = 7;
  /**
   * Documentation design object.
   */
  int TYPE_DOCUMENTATION = 8;
  /**
   * Internal configuration files
   */
  int TYPE_CONFIGURATION = 100;
  
  /**
   * The current server-side revision - this means the current working copy from the <b>DESIGNBUCKET</b>
   * table.
   */
  public static final long REV_CURRENT = -1;
  /**
   * The latest revision from the repository
   */
  public static final long REV_LATEST = -2;
  
  /**
   * Uploads file to the server. Note that only one file of the same type can be uploaded
   * to the server at the same time.
   *
   * @param file is the file to upload
   * @param isSource specifies if the design object data are source or data
   * @throws PuakmaCoreException when something goes wrong with upload
   */
  public void upload(File file, boolean isSource) throws PuakmaCoreException, IOException;
  
  /**
   * This is the same function as <code>upload(InputStream, boolean)</code> with
   * the difference that we can specify if application's cache should be flushed.
   *
   * @param file is the file to upload
   * @param isSource specifies if the design object data are source or data
   * @param flushCache specifies if we should execute "tell http cache flush"
   * @throws PuakmaCoreException when something goes wrong with upload
   */
  public void upload(File file, boolean isSource, boolean flushCache) throws PuakmaCoreException, IOException;
  
  /**
   * Uploads InputStream object content to the server. Note that only one file
   * of the same type can be uploaded to the server at the same time.
   *
   * @param is is the InputStream object creating content of the design object
   * @param isSource specifies if the design object data are source or data
   * @throws PuakmaCoreException when something goes wrong with upload
   */
  public void upload(InputStream is, boolean isSource) throws PuakmaCoreException, IOException;
  
  /**
   * This is the same function as <code>upload(InputStream, boolean)</code> with
   * the difference that we can specify if application's cache should be flushed.
   *
   * @param is is the InputStream object creating content of the design object
   * @param isSource specifies if the design object data are source or data
   * @param flushCache specifies if we should execute "tell http cache flush"
   * @throws PuakmaCoreException when something goes wrong with upload
   */
  public void upload(InputStream is, boolean isSource, boolean flushCache) throws PuakmaCoreException, IOException;
  
  /**
   * Downloads the file from the server.
   *
   * @param file is the file into which we should download datas
   * @param isSource specifies if the design object data are source or data
   * @throws PuakmaCoreException
   */
  public void download(File file, boolean isSource) throws PuakmaCoreException, IOException;
  
  /**
   * Downloads the file from the server to OutputStream object.
   *
   * @param os is the user defined OutputStream into which we should copy data
   * @param isSource specifies if the design object data are source or data
   * @throws PuakmaCoreException
   */
  public void download(OutputStream os, boolean isSource) throws PuakmaCoreException, IOException;
  
  /**
   * Downloads the file revision from the server to the OutputStream object. Note that this API doesn't
   * update the design object to the one from the revision.
   * 
   * @param os is the user defined OutputStream into which we should copy the data.
   * @param isSource if true then we want to download source, otherwise design data
   * @param revision is the revision number
   * @throws PuakmaCoreException
   * @see {@link #REV_CURRENT}
   * @see {@link #REV_LATEST}
   */
  public void download(OutputStream os, boolean isSource, long revision) throws PuakmaCoreException, IOException;
  
  /**
   * Gets the default content type of the design object. Note that design object's
   * content type does not necessary mean that this content type will be tell client,
   * because action can specify it's own content type.
   *
   * @return String with the default content type
   */
  public String getContentType();
  
  /**
   * Sets new content type for this DesignObject.
   *
   * @param contentType is the new content type
   */
  public void setContentType(String contentType);
  
  public void setInheritFrom(String inheritFrom);
  
  public String getInheritFrom();
  
  /**
   * Gets the design type of the design object it can be one of:
   * <ul>
   * <li>DesignObject.TYPE_ACTION</li>
   * <li>DesignObject.TYPE_PAGE</li>
   * <li>DesignObject.TYPE_RESOURCE</li>
   * <li>DesignObject.TYPE_LIBRARY</li>
   * <li>DesignObject.TYPE_JAR_LIBRARY - this design type is converted
   *     to TYPE_LIBRARY when interacting with the server</li>
   * <li>DesignObject.TYPE_SCHEDULEDACTION</li>
   * <li>DesignObject.TYPE_DOCUMENTATION</li>
   * <li>DesignObject.TYPE_WIDGET</li>
   * </ul>
   *
   * @return int with the valid design type of the design object
   */
  public int getDesignType();
  
  public void setDesignType(int designType);
  
  public void setOptions(String options);
  
  public String getOptions();
  
  /**
   * Get the url on which we can see action/page or whatever in the web browser.
   *
   * @return String with the url or null if the object is new
   */
  public String getUrl();

  public DesignObject makeWorkingCopy();
  
  /**
   * This function creates a copy of the java object, but this copy is not
   * attached to the existing application anymore. So the copy will have
   * properties: isNew() == true, application == null and appId == -1.
   *
   * @return copy of this object
   */
  public DesignObject copy();
  
  /**
   * Gets design data/source size on the server.
   *
   * @param isSource if true then query for design data otherwise design source
   * @return positive int representing size of design data/source in bytes
   */
  public int getDesignSize(boolean isSource);
  
  /**
   * Gets the person identifier who updated the design object as the last one.
   * 
   * @return String with X405 identifier of person
   */
  public String getUpdatedByUser();
  
  /**
   * Gets the time of the last update of the design object. The time returned is
   * the local time, however on the server there is stored time in GMT.
   *
   * @return Date object with the time of last update of design object
   */
  public Date getLastUpdateTime();
  
  /**
   * Gets the current server-side crc32.
   * 
   * @param isSource if true then source crc32 is returned, otherwise crc32 value of data
   * @return crc32 valu, as from {@link CRC32} object.
   */
  public long getCrc32(boolean isSource);
  
  /**
   * This function returns the current state of the server data for the design
   * object. Note that this doesn't update the current knowledge about the
   * server status.
   * 
   * @return {@link ServerDataStatus} object with information
   */
  public ServerDataStatus getServerStatus() throws PuakmaCoreException, IOException;
  
  /**
   * Gets the currently known server data status for this design object object.
   * 
   * @return {@link ServerDataStatus} object with information about length and
   *         crc32 of source and data.
   */
  public ServerDataStatus getKnownServerStatus();
}
