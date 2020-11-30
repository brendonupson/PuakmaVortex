/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:      
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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.DatabaseConnection;

public class NewDatabaseConnectionWizard extends Wizard implements INewWizard
{
  private NewDatabaseConnectionWizardPage mainPage;
  private NewDatabaseConnectionPingPage pingPage;
  private Application application;

  public NewDatabaseConnectionWizard()
  {
    super();

    setNeedsProgressMonitor(true);
  }

  /**
   * Adding the page to the wizard.
   */
  public void addPages()
  {
    setWindowTitle("New Database Connection");

    mainPage = new NewDatabaseConnectionWizardPage(application);
    addPage(mainPage);
    
    pingPage = new NewDatabaseConnectionPingPage(application, mainPage);
    addPage(pingPage);
  }

  /**
   * This method is called when 'Finish' button is pressed in the wizard. We
   * will create an operation and run it using wizard as execution context.
   */
  public boolean performFinish()
  {
    final DatabaseConnection obj = mainPage.getDatabaseObject();

    IRunnableWithProgress op = new IRunnableWithProgress() {
      public void run(IProgressMonitor monitor) throws InvocationTargetException
      {
        try {
          monitor.beginTask("Creating Database Connection " + obj.getName(), 1);

          application.addObject(obj);

          monitor.worked(1);
        }
        catch(Exception e) {
          throw new InvocationTargetException(e);
        }
        finally {
          monitor.done();
        }
      }
    };
    try {
      getContainer().run(true, false, op);
    }
    catch(InterruptedException e) {
      return false;
    }
    catch(InvocationTargetException e) {
      Throwable realException = e.getTargetException();
      MessageDialog.openError(getShell(), "Error", realException.getMessage());
      return false;
    }
    return true;
  }

  /**
   * We will accept the selection in the workbench to see if we can initialize
   * from it.
   */
  public void init(IWorkbench workbench, IStructuredSelection selection)
  {
  }

  public void init(Application application)
  {
    this.application = application;
    if(mainPage != null)
      mainPage.setApplication(application);
  }
}
