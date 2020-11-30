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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;

import puakma.coreide.objects2.DesignObject;
import puakma.vortex.VortexPlugin;
import puakma.vortex.preferences.PreferenceConstants;
import puakma.vortex.project.ProjectUtils;

/**
 * This job runs in Eclipse thread pool, and wrotes stuff to workspace.
 *
 * @author Martin Novak
 */
public class WorkspaceWriterJob extends WorkspaceJob
{
  private BaseDownloadQueue queue;

  public WorkspaceWriterJob(BaseDownloadQueue queue)
  {
    super("Writing downloaded files to workspace");
    this.queue = queue;
  }

  public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException
  {
    DownloadItem item;
    
    while((item = queue.pickFileToWriteToWorkspace()) != null) {
      processWorkspaceOperation(item);
    }
    
    return Status.OK_STATUS;
  }

  private void processWorkspaceOperation(DownloadItem item)
  {
    try {
      if(item.isDownload())
        processDownloadFile(item);
      else if(item.isForDeleteFromDisk())
        processDeleteFile(item);
    }
    catch(CoreException ex) {
      VortexPlugin.log(ex);
    }
    finally {
      // NOTIFY ALL OTHERS THAT WE HAVE FINISHED THE THING
      item.getSynchronizer().notifyFirst();
      queue.operationInFlightFinished();
    }
  }

  /**
   * Deletes file from disk.
   *
   * @param item is the description of file/design object.
   */
  private void processDeleteFile(DownloadItem item) throws CoreException
  {
    IFile file = ProjectUtils.getIFile(item.getDesignObject(), item.isSource());
    if(file.exists()) {
      //item.getDesignObject().setData(PuakmaProject2Impl.KEY_REMOVE_REFRESH, PuakmaProject2Impl.OP_REFRESH);
      file.delete(true, true, null);
      
      IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
      boolean doLog = store.getBoolean(PreferenceConstants.PREF_DEBUG_PRINT);
      if(doLog)
        VortexPlugin.info("DELETING FILE FROM DISK: " + item.getDesignObject().getName());
    }
  }

  /**
   * Writes downloaded file to disk.
   * 
   * @param item is the description of the file to download
   */
  private void processDownloadFile(DownloadItem item) throws CoreException
  {
    IFile file = ProjectUtils.getIFile(item.getDesignObject(), item.isSource());
    IFolder parent = (IFolder) file.getParent();
    if(parent.exists() == false) {
      ProjectUtils.createFolder(parent);
    }
    
    IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
    boolean doLog = store.getBoolean(PreferenceConstants.PREF_DEBUG_PRINT);
    
    ByteArrayInputStream is = new ByteArrayInputStream(item.getData());
    if(file.exists()) {
      if(doLog)
        VortexPlugin.info("WRITING FILE OBJECT: " + item.getDesignObject().getName());
      
      file.setContents(is, true, true, null);
    }
    else {
      if(doLog)
        VortexPlugin.info("CREATING FILE OBJECT: " + item.getDesignObject().getName());
      
      file.create(is, true, null);
    }
    
    // TODO: inspect LibraryDeltaVisitor class for functionality duplication
    //if(item.isSource() == false && item.getDesignObject().getDesignType() == DesignObject.TYPE_JAR_LIBRARY)
    //  queue.getVortexProject().addLibrary(item.getDesignObject());
  }
}
