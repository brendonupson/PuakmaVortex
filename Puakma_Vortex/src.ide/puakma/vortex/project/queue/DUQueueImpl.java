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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;

import puakma.coreide.objects2.DesignObject;
import puakma.utils.FirstWaitForSecond;
import puakma.vortex.project.PuakmaProject2Impl;

public class DUQueueImpl extends UIQueue implements DUQueue
{
  /**
   * This mutex checks all external entrances to this class.
   */
  private Object externalEntryMutex = new Object();
    
  /**
   * Ctor which assigns vortex project to this queue.
   *
   * @param project is the Vortex project assigned with this queue
   */
  public DUQueueImpl(PuakmaProject2Impl project)
  {
    super(project);
  }
  
  public boolean download(DesignObject dob, boolean isSource, boolean blocking)
  {
    synchronized(externalEntryMutex) {
      clearFilter(dob);
      
      DownloadItem item = addToQueue(dob, isSource, true);
      
      if(blocking) {
        FirstWaitForSecond sync = item.getSynchronizer();
        sync.waitForSecond();
      }
      
      return true;
    }
  }

  public boolean upload(DesignObject dob, boolean isSource, boolean blocking) throws CoreException, IOException
  {
    synchronized(externalEntryMutex) {
      DownloadItem item = this.createDownloadItem(dob, isSource, DownloadItem.TYPE_UPLOAD);
      
      List<DownloadItem> l = new ArrayList<DownloadItem>();
      processBatch(l, blocking);
      
      return item.hasBeenUploaded();
    }
  }
  
  public void processBatch(final List<DownloadItem> items, boolean blocking)
  {
    if(items.size() == 0)
      return;
    
    final List<DownloadItem> downloads = new ArrayList<DownloadItem>(items);
    
    DownloadScheduleJob j = new DownloadScheduleJob(this, downloads, true);
    if(blocking) {
      j.run(null);
    }
    else {
      j.schedule();
    }
  }
  
  public boolean uploadInTheSameThread(DesignObject obj, boolean isSource) throws CoreException, IOException
  {
    synchronized(externalEntryMutex) {
      DownloadItem item = this.createDownloadItem(obj, isSource, DownloadItem.TYPE_UPLOAD);
      List<DownloadItem> items = new ArrayList<DownloadItem>();
      items.add(item);
      processBatch(items, true);
      return item.hasBeenUploaded();
    }
  }

  private DownloadItem addToQueue(DesignObject dob, boolean isSource, boolean isDownload)
  {
    int type = isDownload ? DownloadItem.TYPE_DOWNLOAD : DownloadItem.TYPE_UPLOAD;
    DownloadItem item = createDownloadItem(dob, isSource, type);
    addDownloadItem(item);
    return item;
  }
  
  private DownloadItem createDownloadItem(DesignObject dob, boolean isSource, int type)
  {
    DownloadItem item = new DownloadItem(dob, isSource, type);
    return item;
  }
}
