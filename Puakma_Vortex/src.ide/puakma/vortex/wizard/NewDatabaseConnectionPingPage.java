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
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import puakma.coreide.PuakmaCoreException;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.DatabaseConnection;
import puakma.utils.lang.StringUtil;
import puakma.vortex.IconConstants;
import puakma.vortex.VortexPlugin;
import puakma.vortex.swt.DialogBuilder2;

public class NewDatabaseConnectionPingPage extends WizardPage
{
  private Application application;
  
  private Text resultText;

  private NewDatabaseConnectionWizardPage mainPage;
  
  private String[] pingResult;


  protected NewDatabaseConnectionPingPage(Application application, NewDatabaseConnectionWizardPage mainPage)
  {
    super("newDbConnectionPingPage");
    
    this.application = application;
    this.mainPage = mainPage;
    
    setTitle("Test Database Connection");
    setImageDescriptor(VortexPlugin.getImageDescriptor(IconConstants.WIZARD_BANNER));
    setDescription("Test New Database Connection.");
  }

  public void createControl(Composite parent)
  {
    DialogBuilder2 builder = new DialogBuilder2(parent);
    Composite c = builder.createComposite();
    
    Button b = new Button(c, SWT.PUSH);
    b.setText("Test Database...");
    b.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {  }
      public void widgetSelected(SelectionEvent e) {
        pingDatabase();
      }
    });
    resultText = builder.createMemoRow("Database connection test results:", 8);
    
    builder.closeComposite();
    builder.finishBuilder();
    
    setControl(c);
    setPageComplete(true);
  }
  
  private void pingDatabase()
  {
    final DatabaseConnection obj = mainPage.getDatabaseObject();
    pingResult = new String[] {"Nothing tested yet..."};
    IRunnableWithProgress runnable = new IRunnableWithProgress() {
      public void run(IProgressMonitor monitor) throws InvocationTargetException,
          InterruptedException
      {
        try {
          monitor.beginTask("Ping database...", 1);
          pingResult = application.getServer().pingDatabase(obj);
          monitor.worked(1);
        }
        catch(PuakmaCoreException e) {
          throw new InvocationTargetException(e);
        }
        finally {
          monitor.done();
        }
      }
    };
    try {
      getContainer().run(true, false, runnable);
    }
    catch(InvocationTargetException e) {
      pingResult = new String[] { e.getTargetException().getLocalizedMessage() };
    }
    catch(InterruptedException e) {
      pingResult = new String[] { "Interrupted" };
    }
    
    String res = StringUtil.merge(pingResult, "\n");
    resultText.setText(res);
  }
}
