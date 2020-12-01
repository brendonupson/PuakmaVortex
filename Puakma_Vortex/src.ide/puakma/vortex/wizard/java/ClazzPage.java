/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Apr 5, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.wizard.java;

import java.lang.reflect.InvocationTargetException;
import java.security.InvalidParameterException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import puakma.addin.widgie.BusinessWidget;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.DesignObject;
import puakma.system.ActionRunner;
import puakma.vortex.IconConstants;
import puakma.vortex.VortexPlugin;
import puakma.vortex.controls.ConnectedAppsViewer;
import puakma.vortex.project.ProjectManager;
import puakma.vortex.project.PuakmaProject2;
import puakma.vortex.swt.StupidEclipseFatalBugAsyncProgressMonitorWrapper;

/**
 * This class represents the page for creating new class or interface on the server.
 *
 * @author Martin Novak
 */
public class ClazzPage extends NewTypeWizardPage
{
  /**
   * Array with all of the allowed design types
   */
  public static final int[] ALLOWED_TYPES = {
      DesignObject.TYPE_ACTION,   DesignObject.TYPE_SCHEDULEDACTION,
      DesignObject.TYPE_WIDGET,   DesignObject.TYPE_LIBRARY
  };
    
  private Application application;
  
  private int designType;

  /**
   * Old class name, used for updating class name after change of action name.
   */
  private String oldName;

  private ConnectedAppsViewer appsCombo;

  private int javaType;

  /**
   * Ctor.
   *
   * @param application is the application connection
   * @param designType is the design type - see CoreIdeConstants
   * @param isClass if true, we are creating class, otherwise interface
   * @param pageName is the name of this page
   */
  public ClazzPage(Application application, int designType, int javaType, String pageName)
  {
    super(javaType, pageName);
    
    boolean ok = false;
    for(int i = 0; i < ALLOWED_TYPES.length; ++i) {
      if(ALLOWED_TYPES[i] == designType)
        ok = true;
    }
    if(ok == false)
      throw new InvalidParameterException("Invalid design type - has to be of java type");
    
    this.application = application;
    this.designType = designType;
    this.javaType = javaType;
    
    String title;
    String msg = "Create New " + JavaObjectWizard.generateObjectName(designType, javaType);
    title = msg;
    setTitle(title);
    setImageDescriptor(VortexPlugin.getImageDescriptor(IconConstants.WIZARD_BANNER));
    setDescription(msg);
  }

  public void createControl(Composite parent)
  {
    initializeDialogUnits(parent);

    Composite composite = new Composite(parent, SWT.NONE);

    int nColumns = 4;

    GridLayout layout = new GridLayout();
    layout.numColumns = nColumns;
    composite.setLayout(layout);

    createApplicationListControls(composite, nColumns);
    
    createContainerControls(composite, nColumns);
    createPackageControls(composite, nColumns);
    setPackageFragment(((JavaObjectWizard)getWizard()).getInitialPackageFragment(), true);
    //  createEnclosingTypeControls(composite, nColumns);

    createSeparator(composite, nColumns);

    createTypeNameControls(composite, nColumns);
    
    createSuperClassControls(composite, nColumns);
    createSuperInterfacesControls(composite, nColumns);
        
    createCommentControls(composite, nColumns);
    enableCommentControl(true);

    setControl(composite);

    Dialog.applyDialogFont(composite);
    PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IJavaHelpContextIds.NEW_CLASS_WIZARD_PAGE);

    initialize();
  }
  
  private void createApplicationListControls(Composite composite, int columns)
  {
    if(application == null && designType == DesignObject.TYPE_LIBRARY) {
      Label l = new Label(composite, SWT.NONE);
      l.setText("Application:");
      appsCombo = new ConnectedAppsViewer(composite);
      appsCombo.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, columns-1, 1));
      appsCombo.getCombo().addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e)
        {
          tryToDownloadApplication();
        }
      });

      l = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
      l.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, columns, 1));
    }
  }
  
  /**
   * Checks if the currently selected application has been downloaded. If not, it tries to
   * download it.
   */
  protected void tryToDownloadApplication()
  {
    Application application = getApplication();
    if(application != null) {
      final PuakmaProject2 project = ProjectManager.getProject(application);
      if(project.javaStarted() == false) {
        try {
          IRunnableWithProgress runnable = new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
            {
              monitor = new StupidEclipseFatalBugAsyncProgressMonitorWrapper(monitor, Display.getDefault());
              try {
                project.startJava(monitor);
              }
              catch(Exception e) {
                throw new InvocationTargetException(e);
              }
            }
          };
          if(getWizard().getPreviousPage(this) == this)
            getContainer().run(true, false, runnable);
          else {
            IWorkbench workbench = PlatformUI.getWorkbench();
            workbench.getProgressService().busyCursorWhile(runnable);
          }
        }
        catch(Exception e) {
          // TODO: handle this better
          VortexPlugin.log(e);
        }
      }
    }
  }
  
  public Application getApplication()
  {
    if(appsCombo != null) {
      application = appsCombo.getSelectedApplication();
    }
    
    return application;
  }

  /**
   * Initializes the dialog after creating all the controls
   */
  public void initialize()
  {
    prefillDialog();
    
    setPageComplete(false);
  }

  /**
   * This function pre fills dialog.
   */
  public void prefillDialog()
  {
    String superClassName = "";
    boolean canModifySuperClass = false;

    if(designType == DesignObject.TYPE_LIBRARY) {
      superClassName = "";
      canModifySuperClass = true;
    }
    else if(designType == DesignObject.TYPE_ACTION || designType == DesignObject.TYPE_SCHEDULEDACTION) {
      superClassName = ActionRunner.class.getName();
    }
    else if(designType == DesignObject.TYPE_WIDGET)
      superClassName = BusinessWidget.class.getName();
    else throw new RuntimeException("Invalid java type");

    setSuperClass(superClassName, canModifySuperClass);

    tryToDownloadApplication();
    if(getApplication() != null)
      setupPackageFragmentRoot();
  }

  /**
   * This setups package fragment root after initialization of the wizard.
   */
  private void setupPackageFragmentRoot()
  {
    try {
      PuakmaProject2 pProject = ProjectManager.getProject(application);
      IProject project = pProject.getProject();

      if(project.hasNature(JavaCore.NATURE_ID)) {
        IJavaProject jProject = JavaCore.create(project);
        IPath iSourceDir = project.getFullPath().append(PuakmaProject2.DIR_SRC);
        IPackageFragmentRoot root = null;
        IPackageFragmentRoot[] allRoots = jProject.getAllPackageFragmentRoots();
        for(int i = 0; i < allRoots.length; i++) {
          IPackageFragmentRoot classpathRoot = allRoots[i];
          //  it must be already equal! our is and package fragent is too
          if(classpathRoot.getPath().toString().equals(iSourceDir.toString())) {
            root = classpathRoot;
            break;
          }
        }

        setPackageFragmentRoot(root, false);
      }
    }
    catch(CoreException e) {
      VortexPlugin.log(e);
    }
  }

  /**
   * Creates method according to the design element type.
   * @param newType
   * @param imports
   * @param monitor
   * @throws CoreException
   */
  protected void createTypeMembers(IType newType, ImportsManager imports, IProgressMonitor monitor)
                                   throws CoreException
  {
//    createInheritedMethods(newType, true, true, imports, new SubProgressMonitor(monitor, 1));

    if(designType == DesignObject.TYPE_ACTION || designType == DesignObject.TYPE_SCHEDULEDACTION) {
      String method = "public String execute() { return \"\"; }";
      newType.createMethod(method, null, false, null);
      imports.addImport("puakma.system.ActionRunner");
    }
    else if(designType == DesignObject.TYPE_WIDGET)
      imports.addImport("puakma.addin.widgie.BusinessWidget");
  }

  private void doStatusUpdate()
  {
    // OK NOW TRY TO CHECK IF THE CLASS IS IN THE APPLICATION
    IPackageFragment fragment = getPackageFragment();
    if(fragment != null) {
      String packageName = fragment.getElementName();
      String className = getTypeName();
      if(application.getJavaObject(packageName, className) != null) {
        // EXECUTE THIS PIECE OF CODE ONLY WHEN WE ARE IN UI THREAD. WHEN WE ARE NOT THERE,
        // DO NOT DO ANYTHING - WE ARE ACTUALLY IN THE WIZARD EXECUTION CODE...
        if(Display.getDefault() == Display.getCurrent())
          updateStatus(new Status(IStatus.ERROR, VortexPlugin.PLUGIN_ID, 0, "Class already exists on the server", null));
        return;
      }
    }
    
    // all used component status
    IStatus[] status = new IStatus[] { fContainerStatus,
        isEnclosingTypeSelected() ? fEnclosingTypeStatus : fPackageStatus,
        fTypeNameStatus, fModifierStatus, fSuperInterfacesStatus };

    // the mode severe status will be displayed and the ok button
    // enabled/disabled.
    updateStatus(status);
  }

  protected void handleFieldChanged(String fieldName)
  {
    super.handleFieldChanged(fieldName);

    doStatusUpdate();
  }

  /**
   * The wizard owning this page is responsible for calling this method with the
   * current selection. The selection is used to initialize the fields of the
   * wizard page.
   * 
   * @param selection used to initialize the fields
   */
  public void init(IStructuredSelection selection)
  {
    IJavaElement jelem = getInitialJavaElement(selection);
    initContainerPage(jelem);
    initTypePage(jelem);

    doStatusUpdate();
  }

  public void setupActionName(String actionName)
  {
    String typeName = getTypeName();
    if(typeName.equals(oldName) || typeName.length() == 0) {
      setTypeName(actionName, true);
      oldName = actionName;
    }
  }

  public void setupApplication(Application application)
  {
    this.application = application;
    setupPackageFragmentRoot();
  }
}
