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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import puakma.coreide.ConnectionPrefs;
import puakma.coreide.PuakmaCoreException;
import puakma.coreide.objects2.ILogger;
import puakma.coreide.objects2.Server;
import puakma.vortex.IdeException;
import puakma.vortex.VortexPlugin;
import puakma.vortex.project.ProjectManager;

public class NewApplicationWizard extends Wizard implements INewWizard
{
  private NewApplicationWizardPage mainPage;
  private NewAppImportTypePage templPage;
  private String appName;
  private String groupName;
  private Server server;

  /**
   * Constructor for SampleNewWizard.
   */
  public NewApplicationWizard()
  {
    super();

    setNeedsProgressMonitor(true);
  }

  /**
   * Adding the page to the wizard.
   */
  public void addPages()
  {
    setWindowTitle("New Puakma Application");

    mainPage = new NewApplicationWizardPage();
    addPage(mainPage);
    
    templPage = new NewAppImportTypePage();
    addPage(templPage);
  }

  /**
   * This method is called when 'Finish' button is pressed in the wizard. We
   * will create an operation and run it using wizard as execution context.
   */
  public boolean performFinish()
  {
    appName = mainPage.getName();
    groupName = mainPage.getGroup();
    server = mainPage.getServer();

    IRunnableWithProgress op = new IRunnableWithProgress() {
      public void run(IProgressMonitor monitor)
          throws InvocationTargetException
      {
        try {
          finishWork(monitor);
        }
        catch(Exception e) {
          throw new InvocationTargetException(e);
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
      VortexPlugin.log("Cannot create new application", realException, ILogger.ERROR_ERROR);
      return false;
    }
    return true;
  }

  protected void finishWork(IProgressMonitor monitor) throws PuakmaCoreException, InvocationTargetException, IOException, IdeException
  {
    // CREATE APPLICATION
    TemplateDefinition tpl = templPage.getTemplate();
    server.importPmx(groupName, appName, tpl.getPmxFile());
    
    // CONNECT TO THIS APPLICATION
    ConnectionPrefs prefs = server.getConnectionPrefs();
    prefs.setApplication(appName);
    prefs.setGroup(groupName);
    ProjectManager.connectToApplication(prefs, monitor);
  }

  /**
   * We will accept the selection in the workbench to see if we can initialize
   * from it.
   */
  public void init(IWorkbench workbench, IStructuredSelection selection)
  {
  }
}
