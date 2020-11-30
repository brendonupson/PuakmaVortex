/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jan 12, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.WizardDialog;

import puakma.vortex.wizard.importPmx.ImportPmxWizard;


/**
 * @author Martin Novak
 */
public class UploadPmxDelegate extends BaseWorkbenchDelegate
{
  public void run(IAction action)
  {
    ImportPmxWizard wizard = new ImportPmxWizard();
    WizardDialog dlg = new WizardDialog(window.getShell(), wizard);
    dlg.open();
  }
}
