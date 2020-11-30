/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Apr 9, 2006
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

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import puakma.coreide.PuakmaCoreException;
import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.ServerDataStatus;
import puakma.utils.lang.ArrayUtils;
import puakma.vortex.VortexPlugin;
import puakma.vortex.project.PuakmaProject2Impl;

abstract class UIQueue extends FilterQueue
{
  public UIQueue(PuakmaProject2Impl impl)
  {
    super(impl);
  }

  /**
   * This function waits for user's decision what to do with the file we want to upload.
   * @param obj is the {@link DesignObject} we want to ask about
   * @param isSource
   */
  protected void waitForUserDecisionToUpload(final HistoryItem[] his)
  {
    Display.getDefault().syncExec(new Runnable() {
      public void run()
      {
        displayFilesToOverwriteInUI(his);
      }
    });
  }
  
  /**
   * This function displays the dialog for overwriting the files which are not in sync with
   * the others.
   */
  private boolean displayFilesToOverwriteInUI(HistoryItem[] items)
  {
    Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
    DiffDialog dlg = new DiffDialog(shell, items);
    return dlg.open() == Window.OK;
  }
  
  private DownloadItem[] listFilesToOverwrite(DownloadItem[] downloads)
  {
    DownloadItem[] ret = new DownloadItem[downloads.length];
    
    for(int i = 0; i < downloads.length; ++i) {
      if(downloads[i].isDownload() == false)
        continue;
      
      try {
        if(doWeOverwriteFile(downloads[i]))
          ret[i] = downloads[i];
      }
      catch(Exception ex) {
        VortexPlugin.log(ex);
      }
    }
    
    ret = (DownloadItem[]) ArrayUtils.filterNulls(ret);
    return ret;
  }
  
  private DownloadItem[] listFilesToOverwrite(List<DownloadItem> downloads)
  {
    DownloadItem[] ret = new DownloadItem[downloads.size()];
    
    for(int i = 0; i < downloads.size(); ++i) {
      if(downloads.get(i).isDownload() == false)
        continue;
      
      try {
        if(doWeOverwriteFile(downloads.get(i)))
          ret[i] = downloads.get(i);
      }
      catch(Exception ex) {
        VortexPlugin.log(ex);
      }
    }
    
    ret = (DownloadItem[]) ArrayUtils.filterNulls(ret);
    return ret;
  }
  
  /**
   * This function checks if the design object has been modified since the last time.
   *
   * @param download informaion about download
   * @throws PuakmaCoreException
   * @throws IOException
   */
  private static boolean doWeOverwriteFile(DownloadItem download) throws PuakmaCoreException, IOException
  {
    DesignObject obj = download.getDesignObject();
    ServerDataStatus serverStatus = download.getServerStatus();
    ServerDataStatus knownStatus = obj.getKnownServerStatus();
    return serverStatus.equals(knownStatus) == false;
  }
  
  protected void checkOverwritingFiles(DownloadItem[] downloads)
  {
    DownloadItem[] filesToOverwrite = listFilesToOverwrite(downloads);
    
    if(filesToOverwrite.length > 0) {
      HistoryItem[] his = prepareHistoryItems(filesToOverwrite);
      waitForUserDecisionToUpload(his);
      for(int i = 0; i < his.length; ++i) {
        setupFilter(his[i]);
      }
    }
  }
  
  void checkOverwritingFiles(List<DownloadItem> downloads)
  {
    DownloadItem[] filesToOverwrite = listFilesToOverwrite(downloads);
    
    if(filesToOverwrite.length > 0) {
      HistoryItem[] his = prepareHistoryItems(filesToOverwrite);
      waitForUserDecisionToUpload(his);
      for(int i = 0; i < his.length; ++i) {
        setupFilter(his[i]);
      }
    }
  }
  
  /**
   * Prepares list of {@link HistoryItem}s from the download items.
   *
   * @param filesToOverwrite is the list of {@link DownloadItem}s
   * @return array with prepared {@link HistoryItem} objects
   */
  private HistoryItem[] prepareHistoryItems(DownloadItem[] filesToOverwrite)
  {
    HistoryItem[] his = new HistoryItem[filesToOverwrite.length];
    for(int i = 0; i < filesToOverwrite.length; ++i) {
      DownloadItem down = filesToOverwrite[i];
      boolean isLocal = true;
      his[i] = new HistoryItem(down.getDesignObject(), DesignObject.REV_CURRENT, isLocal);
      his[i].setSource(down.isSource());
      down.setParameter(his[i]);
      i++;
    }
    return his;
  }
}
