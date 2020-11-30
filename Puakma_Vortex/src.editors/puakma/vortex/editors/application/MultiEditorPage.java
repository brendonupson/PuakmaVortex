/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    31/07/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.application;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Interface for defining editor pages in the multi page editor part.
 * 
 * @author Martin Novak
 */
public interface MultiEditorPage
{
  /**
   * Checks the status of the page. If the editor is not saved, it should return
   * true, false otherwise.
   */
  public boolean isDirty();

  /**
   * Saves the editor. It should report progress in the monitor.
   */
  public void doSave(IProgressMonitor monitor);

  /**
   * This should remove all references, free resources, etc...
   */
  public void disposePage();
}
