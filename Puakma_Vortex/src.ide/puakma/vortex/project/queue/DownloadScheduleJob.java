/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    23/11/2006
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

import java.io.InputStream;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import puakma.vortex.VortexPlugin;
import puakma.vortex.project.ProjectUtils;

public class DownloadScheduleJob extends Job
{
  private DUQueueImpl queue;
  private List<DownloadItem> operations;
  private boolean blocking;

  public DownloadScheduleJob(DUQueueImpl queue, List<DownloadItem> operations, boolean blocking)
  {
    super("Tornado Server File Synchronization Job");
    
    this.queue = queue;
    this.operations = operations;
    this.blocking = blocking;
  }

  protected IStatus run(IProgressMonitor monitor)
  {
    if(monitor == null)
      monitor = new NullProgressMonitor();
    
    monitor.beginTask("Download/Upload Files To Tornado Server", operations.size());
    
    monitor.setTaskName("Wainting for other Tornado server synchronization job");
    queue.waitForOperationsInFlight();
    
    // AT FIRST WE SHOULD FILTER THE QUEUE
    queue.filterDownloads(operations);
    
    queue.checkOverwritingFiles(operations);
    
    for(DownloadItem download : operations) {
      // WE SHOULD CONVERT DOWNLOAD TYPE ACCORDING TO THE HISTORY
      int type = getDownloadType(download);
      
      try {
        switch(type) {
          case DownloadItem.TYPE_DOWNLOAD:
            download.setDownloadType(DownloadItem.TYPE_DOWNLOAD);
            download.setForWorkspace(true);
            // TODO: is this correct to clear the filters right now???
            queue.clearFilter(download.getDesignObject());
            
            queue.addDownloadItem(download);
          break;
          case DownloadItem.TYPE_UPLOAD:
            download.setDownloadType(DownloadItem.TYPE_UPLOAD);
            // TODO: move this code to the thread
            loadDownloadItemFromDisk(download);
            queue.addDownloadItem(download);
          break;
        }
      }
      catch(Exception ex) {
        VortexPlugin.log(ex);
      }
      
      // DO SOME CLEANUP
      download.setParameter(null);
    }
    
    if(blocking)
      queue.waitForOperationsInFlight();
    
    return Status.OK_STATUS;
  }

  /**
   * Convert upload items to download if the user selected that he wants to have server's copy
   */
  private int getDownloadType(DownloadItem download)
  {
    int ret = download.getType();
    HistoryItem item = (HistoryItem) download.getParameter();
    
    if(item != null) {
      int hist = item.getUserAnswer();
      if(hist == HistoryItem.ACTION_USE_SERVER)
        ret = DownloadItem.TYPE_DOWNLOAD;
      else if(hist == HistoryItem.ACTION_USE_LOCAL)
        ret = DownloadItem.TYPE_UPLOAD;
      else if(hist == HistoryItem.ACTION_DONT_CARE)
        ret = DownloadItem.TYPE_DO_NOTHING;
      else
        throw new IllegalStateException("Internal error - HistoryItem doesn't have valid user input");
    }
    
    return ret;
  }
  
  /**
   * Loads file from disk to the DownloadItem to be available for uploading.
   * 
   * @param download is the {@link DownloadItem} refering to the file to load
   * @throws CoreException 
   */
  void loadDownloadItemFromDisk(DownloadItem download) throws CoreException
  {
    InputStream is = null;
    
    try {
      byte[] data;
      // LOAD THE FILE CONTENT TO MEMORY
      IFile file = ProjectUtils.findIFile(download.getDesignObject(), download.isSource());
      is = file.getContents();
      data = new byte[(int) file.getLocation().toFile().length()];
      is.read(data);
      
      download.setData(data);
    }
    catch(Exception e) {
      throw new CoreException(VortexPlugin.createStatus(e));
    }
    finally {
      if(is != null) try { is.close(); } catch(Exception ex) {  }
    }
  }
}
