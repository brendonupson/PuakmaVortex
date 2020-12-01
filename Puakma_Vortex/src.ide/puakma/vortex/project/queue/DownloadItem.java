/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Mar 31, 2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.project.queue;

import java.io.IOException;

import puakma.coreide.PuakmaCoreException;
import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.ServerDataStatus;
import puakma.utils.FirstWaitForSecond;

public class DownloadItem
{
  public static final int TYPE_DO_NOTHING = -1;
  public static final int TYPE_DOWNLOAD = 0;
  public static final int TYPE_UPLOAD   = 1;
  /**
   * This determines that the item has been removed from server object or wherever, so we
   * need to remove the file from disk.
   */
  public static final int TYPE_REMOVED_FROM_SERVER  = 2;
  /**
   * This is removal from disk, so we will need to remove it from server as well
   */
  public static final int TYPE_REMOVED_FROM_DISK = 3;
  
  private FirstWaitForSecond synchronizer = new FirstWaitForSecond();
  private DesignObject dob;
  /**
   * The type of the item.
   * @see TYPE_DOWNLOAD
   * @see TYPE_UPLOAD
   * @see TYPE_REMOVAL
   * @see TYPE_REMOVE_FROM_DISK
   */
  private int type;
  private boolean isSource;
  private byte[] data;
  private boolean shouldWriteToWorkspace = true;
  private Object parameter;
  private ServerDataStatus dataStatus;
  private boolean hasBeenUploaded;

  
  /**
   * Ctor.
   * 
   * @param dob is the {@link DesignObject} instance
   * @param isSource if is true then we care about source
   * @param type the type of the download/workspace operation
   */
  public DownloadItem(DesignObject dob, boolean isSource, int type)
  {
    if(type < -1 || type > 3)
      throw new IllegalArgumentException("Invalid download type");
    
    this.dob = dob;
    this.type = type;
    this.isSource = isSource;
  }

  /**
   * @return synchronization object which passes some signal when we finish
   *         downloading/uploading in different thread
   */
  public FirstWaitForSecond getSynchronizer()
  {
    return synchronizer;
  }
  
  public DesignObject getDesignObject()
  {
    return dob;
  }
  
  public boolean isSource()
  {
    return isSource;
  }
  
  public boolean isDownload()
  {
    return type == TYPE_DOWNLOAD;
  }
  
  public boolean isUpload()
  {
    return type == TYPE_UPLOAD;
  }
  
  public boolean isForDeleteFromDisk()
  {
    return type == TYPE_REMOVED_FROM_SERVER;
  }
  
  public boolean isForDeleteFromServer()
  {
    return type == TYPE_REMOVED_FROM_DISK;
  }
  
  /**
   * Returns the type of this download.
   */
  public int getType()
  {
    return this.type;
  }
  
  public synchronized void setData(byte[] data)
  {
    this.data = data;
  }
  
  /**
   * Gets the data for reading/writing. Note that if this should be always filled with
   * incoming thread.
   * 
   * @return array of byte as a data for download/upload
   */
  public synchronized byte[] getData()
  {
    return data;
  }

  /**
   * Checks if the data should be written to workspace or somebody processes them.
   * 
   * @return true if this item is intended to be saved to workspace
   */
  public synchronized boolean isForWorkspace()
  {
    return shouldWriteToWorkspace;
  }
  
  /**
   * If you set it to true, and this is download then download thread saves the data to
   * workspace.
   * 
   * @param isForWorkspace is whether we want to save stuff to workspace or not
   */
  public synchronized void setForWorkspace(boolean isForWorkspace)
  {
    this.shouldWriteToWorkspace = isForWorkspace;
  }

  public synchronized Object getParameter()
  {
    return parameter;
  }
  
  public synchronized void setParameter(Object parameter)
  {
    this.parameter = parameter;
  }
  
  public synchronized ServerDataStatus getServerStatus() throws PuakmaCoreException, IOException
  {
    if(dataStatus == null)
      dataStatus = getDesignObject().getServerStatus();
    return dataStatus;
  }

  public void setDownloadType(int type)
  {
    this.type = type;
  }

  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("Queue item for ");
    switch(type) {
      case TYPE_DOWNLOAD:
        sb.append("download");
      break;
      case TYPE_UPLOAD:
        sb.append("upload");
      break;
      case TYPE_DO_NOTHING:
        sb.append("no operation");
      break;
      case TYPE_REMOVED_FROM_DISK:
        sb.append("removed from disk");
      break;
      case TYPE_REMOVED_FROM_SERVER:
        sb.append("removed from server");
      break;
      default:
        sb.append("unknown type (" + type + ")");
    }
    
    if(isSource)
      sb.append(" source");
    else
      sb.append(" data");
    
    if(dob != null)
      sb.append(dob.getName());
    else
      sb.append(" unknown design object");
    
    return sb.toString();
  }

  /**
   * Returns true if this dowload item has been really uploaded. Note that this is set
   * only when the type is upload type at the beginning.
   */
  public boolean hasBeenUploaded()
  {
    return hasBeenUploaded;
  }
  
  void setHasBeenUploaded(boolean hasBeenUploaded)
  {
    this.hasBeenUploaded = hasBeenUploaded;
  }
}
