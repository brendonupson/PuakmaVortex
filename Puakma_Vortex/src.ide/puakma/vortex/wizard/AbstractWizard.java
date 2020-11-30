/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Oct 3, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.wizard;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Common class for the Vortex wizards.
 * 
 * @author Martin Novak
 */
public abstract class AbstractWizard extends Wizard implements INewWizard
{
  private IStructuredSelection selection;

  private IWorkbench workbench;
  
  public void init(IWorkbench workbench, IStructuredSelection selection)
  {
    this.workbench = workbench;
    this.selection = selection;
  }
  
  /**
   * Returns the current selection object from the workbench
   */
  IStructuredSelection getSelection()
  {
    return selection;
  }
  
  /**
   * Returns workbench
   */
  IWorkbench getWorkbench()
  {
    return workbench;
  }
}
