/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    17/07/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.dialogs.server;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

import puakma.coreide.ConfigurationManager;
import puakma.coreide.ConnectionPrefs;
import puakma.coreide.ServerManager;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.Server;
import puakma.vortex.VortexPlugin;
import puakma.vortex.dialogs.ConnectionManagerDialog;
import puakma.vortex.preferences.PreferenceConstants;
import puakma.vortex.swt.DialogBuilder2;
import puakma.vortex.swt.SWTUtil;
import puakma.vortex.swt.TitleAreaDialog2;
import puakma.vortex.swt.TreeObject;

public class AppSelectionDialog extends TitleAreaDialog2 implements AppSelectionDialogController
{
  private AppSelectionDialogRunnable runnable;
  private TreeViewer tree;
  private Combo consCombo;
  private Composite mainComposite;
  private Text descMemo;
  private Button refreshBtn;
  private Button editBtn;
  private ProgressMonitorPart monPart;
  private int triesCounter;

  public AppSelectionDialog(Shell parent, AppSelectionDialogRunnable runnable)
  {
    super(parent, "openApplicationDlg");
    
    this.runnable = runnable;
    runnable.setController(this);
  }

  protected Control createDialogArea(Composite parent)
  {
    // NOW CREATE THE CONTENT
    mainComposite = (Composite) super.createDialogArea(parent);
    DialogBuilder2 builder = new DialogBuilder2(mainComposite);
    
    GridLayout gl = (GridLayout) mainComposite.getLayout();
    gl.marginWidth = gl.marginHeight = 0;
    mainComposite.setLayout(gl);
    
    createConnectionsComposite(builder);
    
    createTree(builder);
    
    createDescriptionMemo(builder);
    
    runnable.appendCustomControls(builder);
    
    createProgressPart(builder);
    
//    Label separator = new Label(mainComposite, SWT.HORIZONTAL | SWT.SEPARATOR);
//    separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    
    builder.finishBuilder();
    
    return mainComposite;
  }

  private void createProgressPart(DialogBuilder2 builder)
  {
    GridLayout gl = new GridLayout(1, false);
    monPart = new ProgressMonitorPart(mainComposite, gl);
    monPart.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    monPart.setVisible(false);
  }

  /**
   * Creates a memo with description about the application.
   */
  private void createDescriptionMemo(DialogBuilder2 builder)
  {
    descMemo = builder.appendEdit("", SWT.BORDER | SWT.READ_ONLY | SWT.MULTI);
    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
    gd.heightHint = SWTUtil.computeHeightOfChars(descMemo, 5);
    descMemo.setLayoutData(gd);
  }

  private void createTree(DialogBuilder2 builder)
  {
    Tree t = builder.createTree();
    t.setHeaderVisible(false);
    // ADJUST TREE TO FIT TO THE WHOLE DIALOG
    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    t.setLayoutData(gd);
    
    t.addMouseListener(new MouseAdapter() {
      public void mouseDoubleClick(MouseEvent e)
      {
        treeDoubleClicked();
      }
    });
    
    tree = new TreeViewer(t);
  }

  /**
   * Creates a composite with connections combo box, edit and refresh button.
   */
  private void createConnectionsComposite(DialogBuilder2 builder)
  {
    GridLayout gl;
    Composite c = builder.createComposite(4);
    gl = (GridLayout) c.getLayout();
    gl.marginWidth = gl.marginHeight = gl.marginBottom = 0;
    gl.marginLeft = gl.marginRight = gl.marginTop = 5;
    c.setLayout(gl);
    
    builder.appendLabel("Select connection");
    consCombo = builder.appendCombo(true);
    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
    consCombo.setLayoutData(gd);
    consCombo.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e)
      {
        changeConnectionSelection();
      }
    });
    
    editBtn = builder.appendButton("Edit");
    editBtn.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        editPressed();
      }
    });
    
    refreshBtn = builder.appendButton("Refresh");
    refreshBtn.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        refreshPressed();
      }
    });
    builder.closeComposite();
  }

  protected void initialize()
  {
    setTitle(runnable.getTitle());
    setDescription(runnable.getDescription());
    getShell().setText(runnable.getWindowTitle());
    
    Button okBtn = getButton(Window.OK);
    okBtn.setEnabled(false);
    
    // TODO: change this to use ICU collator
    tree.setSorter(new ViewerSorter());
    tree.setLabelProvider(new AppsLabelProvider());
    tree.setContentProvider(new AppsContentProvider());
    
    fillCombo();
    
    tree.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event)
      {
        treeSelectionHandler(event);
      }
    });
    tree.getTree().setFocus();
    
    refreshPressed();
  }

  /**
   * Handler for the refresh button. Refreshes the tree content with
   * applications from the current connection, and also some other minor things.
   * It lists
   */
  protected void refreshPressed()
  {
    Job j;
    synchronized(this) {
      String jobName = "refreshApplications" + triesCounter;
      final ConnectionPrefs prefs1= getSelectedPreference();

      triesCounter++;
      j = new Job(jobName) {
        long internalCount = triesCounter;
        ConnectionPrefs prefs = prefs1;
        protected IStatus run(IProgressMonitor monitor)
        {
          Application[] apps = null;
        
          try {
              apps = listApplications(prefs);
          }
          catch(final Exception ex) {
            // DISPLAY ERROR IN USER INTERFACE
            Display.getDefault().getDefault().asyncExec(new Runnable() {
              public void run() {
                displayError(ex);
              }
            });
            return Status.OK_STATUS;
          }
          
          synchronized(AppSelectionDialog.this) {
            if(internalCount < triesCounter)
              return Status.OK_STATUS;
          }
          final Application[] apps1 = apps;
          Display.getDefault().asyncExec(new Runnable() {
            public void run() {
              updateRefreshedTree(apps1);
            }
          });
          return Status.OK_STATUS;
        }
      };
    }
    j.schedule();
  }
  
  /**
   * Displays exception error in the dialog.
   */
  private void displayError(Throwable ex)
  {
    if(ex.getCause() != null)
      ex = ex.getCause();
    
    setErrorMessage(ex.getLocalizedMessage());
    tree.setInput(null);
    
    if(ex instanceof IOException == false)
      VortexPlugin.log(ex);
  }
  
  /**
   * Lists all applications from the connection preferences.
   */
  protected Application[] listApplications(ConnectionPrefs prefs) throws Exception
  {
    if(prefs != null) {
      Server server = ServerManager.createServerConnection(prefs);
      server.refresh();
      return server.listApplications();
    }
    else
      return null;
  }

  /**
   * Updates refreshed tree with applications
   */
  private void updateRefreshedTree(Application[] apps)
  {
    setErrorMessage(null);
    descMemo.setText("");
    tree.setInput(apps);
  }
  
  private void fillCombo()
  {
    int index = consCombo.getSelectionIndex();
    String name;
    if(index == -1) {
      IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
      name = store.getString(PreferenceConstants.PREF_CONN_LAST_SELECTED);
    }
    else
      name = consCombo.getItem(index);
    
    consCombo.removeAll();
    
    ConfigurationManager manager = VortexPlugin.getDefault().getServersManager();
    ConnectionPrefs[] prefs = manager.listConnectionPrefs();
    for(int i = 0; i < prefs.length; ++i) {
      consCombo.add(prefs[i].getName());
      if(prefs[i].getName().equals(name)) {
        consCombo.select(i);
        index = -2; // TELL THAT WE HAVE SELECTED SOMETHING
      }
    }
    
    if(index != -2 && prefs.length > 0)
      consCombo.select(0);
  }

  private ConnectionPrefs getSelectedPreference()
  {
    if(consCombo.getSelectionIndex() == -1)
      return null;

    String text = consCombo.getText();
    
    ConfigurationManager manager = VortexPlugin.getDefault().getServersManager();
    ConnectionPrefs[] prefs = manager.listConnectionPrefs();
    for(int i = 0; i < prefs.length; ++i) {
      if(text.equals(prefs[i].getName()))
        return prefs[i];
    }
    
    return null;
  }

  protected void editPressed()
  {
    ConnectionManagerDialog dlg = new ConnectionManagerDialog(getShell(), ConnectionManagerDialog.TYPE_CONNECTIONS);
    dlg.open();
    fillCombo();
  }

  protected Button getButton(int id)
  {
    Button b = super.getButton(id);
    if(id == Window.OK)
      b.setText(runnable.getOkButtonText());
    return b;
  }

  protected void okPressed()
  {
    Button cancelBtn = getButton(Window.CANCEL);

    Control focusControl = getShell().getDisplay().getFocusControl();
    if (focusControl != null && focusControl.getShell() != getShell()) {
        focusControl = null;
    }
    final Control focusControl1 = focusControl;
    
// TODO: resize the dialog on request
//    int linkedResourceGroupHeight;
//    Point groupSize = mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);    
//    linkedResourceGroupHeight = groupSize.y;
//    Point shellSize = getShell().getSize();
//    getShell().setSize(shellSize.x, shellSize.y + linkedResourceGroupHeight);
    
    try {
      //monPart = new ProgressMonitorPart(mainComposite, null);
      monPart.attachToCancelComponent(cancelBtn);
      
      monPart.setVisible(true);
      
      //mainComposite.layout();
      
      enableDialog(false);
      runnable.gatherData();
      
      ModalContext.run(new IRunnableWithProgress() {
        public void run(IProgressMonitor monitor) throws InvocationTargetException,
                                                         InterruptedException {
          final boolean[] done = new boolean[] { false };
          final Exception[] ex = new Exception[1];
          try {
            runnable.run(monitor);
            done[0] = true;
          }
          catch(InterruptedException e) {
            ex[0] = e;
            throw e;
          }
          catch(InvocationTargetException e) {
            ex[0] = e;
            throw e;
          }
          catch(Exception e) {
            ex[0] = e;
            throw new InvocationTargetException(e);
          }
          finally {
            Display.getDefault().asyncExec(new Runnable() {
              public void run() {
                if(done[0])
                  finishUserJobOk();
                else
                  finishUserJobWithError(focusControl1, ex[0]);
              }
            });
          }
        }
      }, true, monPart, getShell().getDisplay());
      
//      Job j = new Job("modalJob") {
//        protected IStatus run(IProgressMonitor monitor)
//        {
//          try {
//            runnable.run(monPart);
//            Display.getDefault().asyncExec(new Runnable() {
//              public void run() {
//                finishUserJobOk();
//              }
//            });
//          }
//          catch(final Exception e) {
//            Display.getDefault().asyncExec(new Runnable() {
//              public void run() {
//                finishUserJobWithError(focusControl1, e);
//              }
//            });
//            VortexPlugin.log(e);
//          }
//          return Status.OK_STATUS;
//        }
//      };
//      j.schedule();
    }
    catch(InvocationTargetException e) {
      finishUserJobWithError(focusControl1, e.getTargetException());
    }
    catch(InterruptedException e) {
      finishUserJobWithError(focusControl1, e);
    }
  }
  
  /**
   * This is performed when user's job finishes with no error.
   */
  private void finishUserJobOk()
  {
    super.okPressed();
  }

  /**
   * This is performed when user's job finishes with some error. Displays some
   * error message, enables all controls, and such stuff.
   */
  private void finishUserJobWithError(Control focusControl, Throwable e)
  {
    Button cancelBtn = getButton(Window.CANCEL);
    setErrorMessage(e.getLocalizedMessage());
    VortexPlugin.log(e);
    
    getShell().setEnabled(true);
    monPart.removeFromCancelComponent(cancelBtn);
    monPart.setVisible(false);
    
    enableDialog(true);
    
    if(focusControl != null)
      focusControl.setFocus();
  }
  
  /**
   * Enables/disables all controls in the dialog.
   */
  private void enableDialog(boolean enable)
  {
    Button okBtn = getButton(Window.OK);
    okBtn.setEnabled(enable);
    
    consCombo.setEnabled(enable);
    editBtn.setEnabled(enable);
    refreshBtn.setEnabled(enable);
    tree.getTree().setEnabled(enable);
    descMemo.setEnabled(enable);
  }
  
  /**
   * Handler for changing connection in the combo box. It should refresh
   * connection in the tree, and also save the newly selected connection.
   */
  protected void changeConnectionSelection()
  {
    saveConnectionComboSelection();
    
    refreshPressed();
  }

  /**
   * Saves the current connection selection to preference store.
   */
  private void saveConnectionComboSelection()
  {
    IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
    int index = consCombo.getSelectionIndex();
    if(index == -1) {
      tree.setInput(null);
      store.setToDefault(PreferenceConstants.PREF_CONN_LAST_SELECTED);
    }
    else {
      String name = consCombo.getItem(index);
      store.setValue(PreferenceConstants.PREF_CONN_LAST_SELECTED, name);
    }
  }

  /**
   * Handles application selection in the tree control.
   */
  private void treeSelectionHandler(SelectionChangedEvent event)
  {
    TreeObject to = (TreeObject) ((IStructuredSelection) event.getSelection()).getFirstElement();
    descMemo.setText("");
    
    
    if(to == null)
      runnable.setSelectedApplication(null);
    else {
      Application app = (Application) to.getData();
      runnable.setSelectedApplication(app);
      if(app != null) {
        String desc = app.getDescription();
        descMemo.setText(desc);
      }
    }
    
    validateInput();
  }

  public void validateInput()
  {
    String error = getErrorMessage();

    if(error == null || error.length() == 0)
      setErrorMessage(null);
    else
      setErrorMessage(error);
    Button okBtn = getButton(Window.OK);
    okBtn.setEnabled(error == null ? true : false);
  }

  /**
   * Returns error message for this dialog or null if there is no error. Note
   * also that "" error message is error, however we don't want to display
   * anything.
   */
  public String getErrorMessage()
  {
    String error = getSelfValidateMessage();

    if(error == null)
      error = runnable.validateCustomControls();
    else if(error.length() == 0) {
      String err = runnable.validateCustomControls();
      if(err != null && err.length() > 0)
        error = err;
    }
    
    return error;
  }
  
  private String getSelfValidateMessage()
  {
    TreeObject to = (TreeObject) ((IStructuredSelection) tree.getSelection()).getFirstElement();
    return (to == null || to.getData() == null) ? "" : null;
  }
  
  private void treeDoubleClicked()
  {
    TreeObject to = (TreeObject) ((IStructuredSelection) tree.getSelection()).getFirstElement();
    if(to == null)
      return;
    
    if(to.getData() != null) {
      String error = getErrorMessage();
      if(error == null)
        okPressed();
    }
    else {
      boolean expanded = tree.getExpandedState(to);
      tree.setExpandedState(to, !expanded);
    }
  }
}
