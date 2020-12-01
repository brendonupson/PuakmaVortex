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
import java.util.List;

import org.eclipse.core.runtime.CoreException;

import puakma.coreide.objects2.DesignObject;

public interface DUQueue
{
  /**
   * This function downloads {@link DesignObject} data/source.
   * 
   * @param dob
   * @param isSource
   * @param blocking if true then the call should be synchronous with the queue, so it
   *          shouldn't wait until queue finishes downloading
   */
  public boolean download(DesignObject dob, boolean isSource, boolean blocking);

  public boolean upload(DesignObject dob, boolean isSource, boolean blocking) throws CoreException,
                                                                             IOException;

  /**
   * This function processes batch download of the design objects. Note that this function
   * blocks accepting all other uploads/downloads unless this function is finished.
   * 
   * @param items are information about objects we want to download
   * @param blocking if true then this function exits after all items are downloaded
   */
  public void processBatch(List<DownloadItem> items, boolean blocking);

  /**
   * Uploads design object source/data in the same thread as this function is being
   * called. If the file status is not consistent with the server, then this asks user
   * what to do, and possibly cancels upload, and returns false.
   * 
   * @param obj is the {@link DesignObject} to upload
   * @param isSource if true then we should upload source code otherwise data
   * @return true if the file has been uploaded, false otherwise
   * @throws IOException
   * @throws CoreException
   */
  public boolean uploadInTheSameThread(DesignObject obj, boolean isSource) throws CoreException,
                                                                          IOException;
}
