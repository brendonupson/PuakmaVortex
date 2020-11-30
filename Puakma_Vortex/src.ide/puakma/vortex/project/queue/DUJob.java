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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import puakma.coreide.objects2.DesignObject;
import puakma.vortex.VortexPlugin;

/**
 * This is download/upload job. This job runs in seperate thread in the
 * application downloading thread pool. It gets the work from the
 * {@link DUQueue} instance. If there is nothing to download, quits the
 * execution. This download/upload job is recreated when needed in the future.
 * 
 * @author Martin Novak
 */
public class DUJob extends Job
{
  private BaseDownloadQueue queue;

  public DUJob(int number, BaseDownloadQueue queue)
  {
    super("Download job [" + number + "]");

    this.queue = queue;
  }

  protected IStatus run(IProgressMonitor monitor)
  {
    DownloadItem item = null;
    while((item = queue.pickFileToDownloadUpload()) != null) {
      processDownload(item);
    }
    return Status.OK_STATUS;
  }

  private void processDownload(DownloadItem item)
  {
    try {
      DesignObject o = item.getDesignObject();
      
      if(item.isDownload()) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        o.download(os, item.isSource());
        byte[] bytes = os.toByteArray();
        item.setData(bytes);
        
        // WRITE TO WORKSPACE ONLY, AND ONLY IF THE ITEM IS SUPPOSED TO DO SO
        if(item.isForWorkspace())
          queue.writeToWorkspace(item);
      }
      else if(item.isUpload()) {
        // WE WRITE TO SERVER FROM WORKSPACE, SO WE SHOULD ALREADY HAVE BYTES
        // IN item
        byte[] bytes = item.getData();
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        o.upload(is, item.isSource());
        VortexPlugin.info("UPLOADED " + (item.isSource() ? "SOURCE" : "DATA")
                          + " of design object " + item.getDesignObject());
        is.close();
      }
      else {
        VortexPlugin.log("Unknown download item type: " + item);
      }
    }
    catch(Exception e) {
      VortexPlugin.log(e);
      // TODO: we should maybe tell the primary thread that something got fucked
    }
    finally {
      // NOTIFY EVERYBODY THAT THIS OPERATION IS AT THE END IF THE DOWNLOAD IS
      // NOT FOR WORKSPACE
      if(item.isDownload() == false || (item.isForWorkspace() == false && item.isDownload())) {
        item.getSynchronizer().notifyFirst();
        queue.operationInFlightFinished();
      }
    }
  }
}
