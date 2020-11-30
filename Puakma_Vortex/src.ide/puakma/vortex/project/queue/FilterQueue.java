/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Apr 8, 2006
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.JavaObject;
import puakma.utils.lang.ArrayUtils;
import puakma.vortex.project.PuakmaProject2Impl;

abstract class FilterQueue extends BaseDownloadQueue
{
  public FilterQueue(PuakmaProject2Impl impl)
  {
    super(impl);
  }

  /**
   * This is a list of all java design objects which classes are not allowed to
   * be uploaded because user selected something like that he doesn't want to
   * use local version.
   */
  private List<DesignObject> filters = new ArrayList<DesignObject>();
  
  /**
   * This function setup filter.
   *
   * @param item is the {@link DiffDialog} responses
   */
  protected void setupFilter(HistoryItem item)
  {
    // WE FILTER ONLY JAVA OBJECTS
    if(item.getDesignObject() instanceof JavaObject == false)
      return;
    if(item.isSource() == false)
      return;
    
    synchronized(filters) {
      if(item.getUserAnswer() == HistoryItem.ACTION_DONT_CARE) {
        filters.add(item.getDesignObject());
      }
      else {
        Iterator it = filters.iterator();
        while(it.hasNext()) {
          JavaObject jo = (JavaObject) it.next();
          if(item.getDesignObject() == jo) {
            it.remove();
          }
        }
      }
    }
  }
  
  /**
   * Clears filters from all {@link DesignObject}s. This is called in some
   * certain situations like when asking to download the file from the server -
   * the downloaded file is being known now, so it doesn't need to be filtered.
   * 
   * @param items are the {@link DownloadItem}s to clear filter from
   */
  protected void clearFilters(DownloadItem[] items)
  {
    synchronized(filters) {
      for(DownloadItem item : items) {
        DesignObject dob = item.getDesignObject();
        if(dob instanceof JavaObject && item.isSource())
          clearFilter(dob);
      }
    }
  }
  
  /**
   * Removes {@link DesignObject} from filters.
   * 
   * @param dob is the {@link DesignObject} we want to remove from filters
   */
  protected void clearFilter(DesignObject dob)
  {
    synchronized(filters) {
      Iterator it = filters.iterator();
      while(it.hasNext()) {
        if(dob == it.next())
          it.remove();
      }
    }
  }
  
  /**
   * This function checks design object against filter. Currently we filter only
   * {@link JavaObject}s, and only uploading .class files in which .java file
   * has been set to not to upload to the server. This means that if user
   * cancels the overwrite dialog or selects that he doesn't want to upload the
   * file, so in this case we won't upload .class file.
   * 
   * @param obj is the {@link DesignObject} to filter
   * @return true if the design object was catched in the filter, so we
   *         shouldn't proceed with upload
   */
  protected boolean isFiltered(DownloadItem download)
  {
    DesignObject obj = download.getDesignObject();
    boolean isSource = download.isSource();
    // WE FILTER ONLY JAVA OBJECTS
    if(obj instanceof JavaObject == false)
      return false;
    if(isSource == true)
      return false;
    
    synchronized(filters) {
      for(DesignObject dob : filters)
        if(dob == obj)
          return true;
      
      return false;
    }
  }
  
  /**
   * Filters items that are supposed to be filtered. Those are for example class files which
   * shouldn't overwrite newer server's code if user doesn't want to.
   */
  void filterDownloads(List<DownloadItem> downloads)
  {
    Iterator<DownloadItem> it = downloads.iterator();
    while(it.hasNext()) {
      DownloadItem download = it.next();
      if(download.isUpload() && isFiltered(download))
        it.remove();
    }
  }
}
