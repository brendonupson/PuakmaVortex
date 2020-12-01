/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Mar 22, 2006
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.IModificationDate;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;

import puakma.coreide.PuakmaCoreException;
import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.JavaObject;
import puakma.coreide.objects2.ServerDataStatus;
import puakma.vortex.VortexPlugin;
import puakma.vortex.project.ProjectUtils;

class HistoryItem implements IStreamContentAccessor, ITypedElement, IModificationDate, IEncodedStreamContentAccessor
{
  /**
   * This constant means that we don't want to upload nor download from the server
   */
  public static final int ACTION_DONT_CARE = 0;
  /**
   * This constant usually means that we want to use the actual version from the server.
   */
  public static final int ACTION_USE_SERVER = 1;
  /**
   * This constant usually means that we want to upload local copy to the server.
   */
  public static final int ACTION_USE_LOCAL = 2;
  /**
   * Prefix for the temporary file used by history
   */
  private static final String TMP_FILE_PREFIX = "vortex_" + System.getProperty("user") + "_";
  /**
   * Suffix for the temporary file used by history
   */
  private static final String TMP_FILE_SUFFIX = "";
  // TODO: create a new action MERGE
  
  /**
   * This indicates if the design object is the current one or the one on the server side.
   */
  private boolean isLocal;
  /**
   * Is the temporary file into which is history item stored
   */
  private File tmpFile;
  /**
   * If true then the compare item is prepared for the show!
   */
  private boolean prepared = false;
  /**
   * User defined object
   */
  private Object parameter;
  private DesignObject dob;
  private long revision;
  private ServerDataStatus status;
  private int answer = ACTION_USE_LOCAL;
  private boolean isSource;
  
  public HistoryItem(DesignObject dob, long revision, boolean isLocal)
  {
    this.isLocal = isLocal;
    this.dob = dob;
    this.revision = revision;
    this.isSource = ProjectUtils.isSourceMoreImportant(dob);
  }
  
  /**
   * Sets user defined parameter for this item.
   * @param parameter is the user object
   */
  public void setParameter(Object parameter)
  {
    this.parameter = parameter;
  }
  
  /**
   * @return user defined parameter
   */
  public Object getParameter()
  {
    return parameter;
  }
  
  /**
   * This function can help to avoid unnecessary calls to the server for the status of the data.
   * @param status is the {@link ServerDataStatus} object
   */
  public void setCurrentServerDataStatus(ServerDataStatus status)
  {
    this.status = status;
  }
  
  /**
   * Prepares content for this history item.
   * 
   * @param monitor is the progress of the work
   * @throws PuakmaCoreException 
   * @throws IOException 
   */
  public void prepareContent(IProgressMonitor monitor) throws PuakmaCoreException, IOException
  {
    int totalWork = 1;
    monitor.beginTask("Preparing " + (isLocal ? "local" : "remote") + "content for design object", totalWork);
      
    if(prepared)
      return;
    
    if(isLocal) {
      // TODO: for future we have to download the current item here if there is
      // nothing here. current version always means the server side if there is
      // nothing local
      prepared = true;
    }
    else {
      prepareHistoryItem();
    }
  }

  /**
   * Downloads hoistory item to the temp directory.
   *
   * @throws IOException 
   * @throws PuakmaCoreException 
   */
  private void prepareHistoryItem() throws IOException, PuakmaCoreException
  {
    if(prepared == false) {
      tmpFile = File.createTempFile(TMP_FILE_PREFIX, TMP_FILE_SUFFIX, null);
    
      long revision = DesignObject.REV_CURRENT;
      
      FileOutputStream os = null;
      try {
        os = new FileOutputStream(tmpFile);
        
        dob.download(os, isSource, revision);
        prepared = true;
      }
      finally {
        if(os != null) {
          try { os.close(); } catch(IOException ex) {  }
        }
      }
    }
  }

  public InputStream getContents() throws CoreException
  {
    if(isLocal) {
      IFile file = ProjectUtils.getIFile(dob, isSource);
      InputStream is = file.getContents();
      return new BufferedInputStream(is);
    }
    else {
      FileInputStream fis;
      try {
        prepareHistoryItem();
        fis = new FileInputStream(tmpFile);
      }
      catch(IOException e) {
        throw new CoreException(VortexPlugin.createStatus(e));
      }
      catch(PuakmaCoreException e) {
        throw new CoreException(VortexPlugin.createStatus(e));
      }
      return new BufferedInputStream(fis);
    }
  }

  public Image getImage()
  {
    return null;
  }

  public long getModificationDate()
  {
    if(isLocal) {
      IFile ifile = ProjectUtils.getIFile(dob, isSource);
      long time;
      if(ifile.exists()) {
        File file = ifile.getLocation().toFile();
        time = file.lastModified();
      }
      else {
        ServerDataStatus localStatus = dob.getKnownServerStatus();
        time = localStatus.getUpdateTime();
      }
      return time;
    }
    else {
      if(status == null) {
        try {
          status = dob.getServerStatus();
        }
        catch(Exception e) {
          VortexPlugin.log(e);
          return 0;
        }
      }
      return status.getUpdateTime();
    }
  }
  
  public String getName()
  {
    return dob.getName();
  }

  public String getType()
  {
    if(dob instanceof JavaObject) {
      return "JAVA";
    }
    
    String contentType = dob.getContentType();
    if("text/properties".equals(contentType))
      return "PROPERTIES";
    else if("image/bmp".equals(contentType))
      return "BMP";
    else if("image/jpg".equals(contentType))
      return "JPG";
    else if("image/jpeg".equals(contentType))
      return "JPEG";
    else if("image/gif".equals(contentType))
      return "GIF";
    else if("image/tiff".equals(contentType))
      return "TIFF";
    else if("image/png".equals(contentType))
      return "PNG";
    else if("image/ico".equals(contentType))
      return "ICO";
    else if("application/zip".equals(contentType))
      return "ZIP";
    else if("image/jar".equals(contentType))
      return "JAR";
    
    return ITypedElement.TEXT_TYPE;
  }

  public String getCharset() throws CoreException
  {
    return "utf-8";
  }

  protected void finalize()
  {
    dispose();
  }
  
  public void dispose()
  {
    if(tmpFile != null && tmpFile.exists()) {
      tmpFile.delete();
      tmpFile = null;
    }
  }

  /**
   * @return true if the content of this compare item is prepared.
   */
  public boolean isPrepared()
  {
    return prepared;
  }

  /**
   * Returns the modification time for the current design object file on filesystem.
   * @return time of the design object or 0 if design object is not downloaded
   */
  public long getLocalTime()
  {
    IFile file = ProjectUtils.getIFile(dob, isSource);
    if(file != null && file.exists()) {
      File f = file.getLocation().toFile();
      return f.lastModified();
    }
    return 0;
  }
  
  /**
   * @return true if the history item is located on local disk. false if the item is
   *         located on server
   */
  public boolean isLocal()
  {
    return isLocal;
  }
  
  public DesignObject getDesignObject()
  {
    return dob;
  }
  
  /**
   * If the {@link HistoryItem} object is local then this returns the current
   * user name, otherwise it gets the latest server revision author name.
   * 
   * @return String with authors name
   */
  public String getAuthorName()
  {
    if(isLocal()) {
      return dob.getApplication().getServer().getUserName();
    }
    else {
      if(status == null) {
        try {
          status = dob.getServerStatus();
        }
        catch(Exception e) {
          VortexPlugin.log(e);
          return e.getLocalizedMessage();
        }
      }
      
      return status.getAuthor();
    }
  }

  /**
   * Returns what to do with this design object.
   * 
   * @return {@link #ACTION_DONT_CARE} or {@link #ACTION_USE_SERVER} when user
   *         chooses to use server's copy
   */
  public int getUserAnswer()
  {
    return answer;
  }
  
  public void setUserAnswer(int answer)
  {
    this.answer = answer;
  }

  public void setSource(boolean isSource)
  {
    this.isSource = isSource;
  }
  
  public boolean isSource()
  {
    return isSource;
  }
}
