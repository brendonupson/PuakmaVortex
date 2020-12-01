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
import java.util.List;

import puakma.utils.lang.CollectionsUtil;
import puakma.vortex.project.PuakmaProject2Impl;

/**
 * This class provides some basic functinos implementation for the support of
 * the download queue.
 * 
 * @author Martin Novak
 */
abstract class BaseDownloadQueue
{
  /**
   * The default value for the number of maximal concurrent jobs for
   * application.
   */
  public static final int MAX_CONC_JOBS = 3;

  /**
   * Maximum of the concurent jobs for one application
   */
  private int maxRunningJobs = MAX_CONC_JOBS;

  private List<DownloadItem> filesToDownloadUpload = new ArrayList<DownloadItem>();

  private List<DownloadItem> filesToWriteToWorkspace = new ArrayList<DownloadItem>();

  private int runningJobs = 0;

  /**
   * Number of executed operations
   */
  private int operationsInFlight = 0;

  /**
   * If true then workspace writer job is running
   */
  private boolean workspaceWriterRunning = false;
  
  /**
   * This is the Vortex project assigned to this queue
   */
  private PuakmaProject2Impl vortexProject;
  
  public BaseDownloadQueue(PuakmaProject2Impl impl)
  {
    this.vortexProject = impl;
  }

  /**
   * We ensure in this function that we have at least one thread running in the
   * thread pool. Also ensure that there is nothing waiting for picking up in
   * the queue unless we have full thread pool - maximum of running threads
   * &gt;= number of threads.
   */
  private void checkThreadPoolRunning()
  {
    if(this.operationsInFlight >= this.maxRunningJobs)
      return;
      
    int count = filesToDownloadUpload.size();
    // AT FIRST COMPUTE NUMBER OF NEEDED JOBS
    int jobsNeeded = count / 3 + runningJobs;
    if(jobsNeeded <= 0)
      jobsNeeded = 1;
    if(jobsNeeded > maxRunningJobs)
      jobsNeeded = maxRunningJobs;

    for(int i = 0; i < jobsNeeded; ++i)
      addNewJob();
  }

  /**
   * Adds a new job to the thread pool, and run it.
   */
  private void addNewJob()
  {
    // TODO: add there something more sophisticated
    int number = runningJobs;
    DUJob job = new DUJob(number, this);
    runningJobs++;
    job.schedule();
  }

  /**
   * This function adds {@link DownloadItem} among the list of items to
   * download. If the workspace wiriter job is not running, we start one such a
   * job.
   * 
   * @param item is the data to write to the disk
   */
  public void writeToWorkspace(DownloadItem item)
  {
    synchronized(this) {
      filesToWriteToWorkspace.add(item);
      if(workspaceWriterRunning == false) {
        WorkspaceWriterJob job = new WorkspaceWriterJob(this);
        job.schedule();
        workspaceWriterRunning = true;
      }
    }
  }

  protected void addDownloadItem(DownloadItem download)
  {
    synchronized(this) {
      filesToDownloadUpload.add(download);
      operationsInFlight += 1;
      
      checkThreadPoolRunning();
    }
  }

  protected void addDownloadItems(DownloadItem[] downloads)
  {
    synchronized(this) {
      CollectionsUtil.addArrayToList(filesToDownloadUpload, downloads);
      operationsInFlight += downloads.length;
      checkThreadPoolRunning();
    }
  }

  /**
   * This function picks up one files which should be written to the workspace.
   * This file should be picked up only by workspace writer job. If there is no
   * file to pickup, we assume that the workspace writing job quits, and returns
   * null.
   * 
   * @return {@link DownloadItem} object to write to disk or null if workspace
   *         writer job should quit because there is nothing to write to disk.
   */
  public DownloadItem pickFileToWriteToWorkspace()
  {
    synchronized(this) {
      if(filesToWriteToWorkspace.size() == 0) {
        workspaceWriterRunning = false;
        this.notifyAll();
        return null;
      }

      DownloadItem item = filesToWriteToWorkspace.remove(0);
      return item;
    }
  }
  
  public DownloadItem pickFileToDownloadUpload()
  {
    synchronized(this) {
      if(filesToDownloadUpload.size() == 0) {
        runningJobs--;
        this.notifyAll();
        return null;
      }

      DownloadItem item = filesToDownloadUpload.remove(0);
      return item;
    }
  }

  /**
   * Waits for all running operations to finish.
   */
  protected void waitForOperationsInFlight()
  {
    synchronized(this) {
      while(operationsInFlight > 0)
        try {
          this.wait();
        }
        catch(InterruptedException e) {
        }
    }
  }

  /**
   * This notifies download queue that some operation has been finished. Note
   * that this is meant to be either download operation or workspace operation
   * if the download item should be written to workspace as well.
   */
  void operationInFlightFinished()
  {
    synchronized(this) {
      operationsInFlight--;
      this.notifyAll();
    }
  }
  
  public PuakmaProject2Impl getVortexProject()
  {
    return vortexProject;
  }
}
