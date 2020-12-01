/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Mar 2, 2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import puakma.coreide.objects2.ProgressMonitor;

/**
 * This class is a bridge between vortex core library monitor, and eclipse {@link IProgressMonitor}.
 * 
 * @author Martin Novak
 */
public class VortexToEclipseProgressMonitorBridge implements ProgressMonitor
{
  private IProgressMonitor monitor;
  private String title;

  public VortexToEclipseProgressMonitorBridge(IProgressMonitor monitor, String workTitle)
  {
    this.title = workTitle;
    if(monitor == null)
      monitor = new NullProgressMonitor();
    this.monitor = monitor;
  }

  public void setup(int work)
  {
    monitor.beginTask(title, work);
  }

  public void worked(int work)
  {
    monitor.worked(work);
  }

  public void done()
  {
    monitor.done();
  }

}
