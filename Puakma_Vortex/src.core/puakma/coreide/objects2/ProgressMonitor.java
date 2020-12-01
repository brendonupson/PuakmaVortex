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
package puakma.coreide.objects2;

/**
 * This is a basic progress monitor for vortex core library. It uses kinda similar semantics as
 * {@link org.eclipse.core.runtime.IProgressMonitor}.
 * 
 * @author Martin Novak
 */
public interface ProgressMonitor
{
  /**
   * This sets up the amount of work that needs to be done.
   * 
   * @param work is the amount of work to be done
   */
  public void setup(int work);
  
  /**
   * This is called when some work has been done.
   * 
   * @param work number of work what has been done
   */
  public void worked(int work);
  
  /**
   * This is called when the whole process is done.
   */
  public void done();
}
