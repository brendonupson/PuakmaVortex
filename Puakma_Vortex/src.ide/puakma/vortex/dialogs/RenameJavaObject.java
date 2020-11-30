/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    04/12/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without previous written permission
 * of the author.
 */
package puakma.vortex.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.JavaObject;
import puakma.vortex.IconConstants;
import puakma.vortex.VortexPlugin;
import puakma.vortex.swt.DialogBuilder2;
import puakma.vortex.swt.TitleAreaDialog2;

/**
 * Dialog for specifying new name of the design object. The supported modes are rename,
 * and paste.
 * 
 * @author Martin Novak
 */
public class RenameJavaObject extends TitleAreaDialog2 implements ModifyListener, SelectionListener
{
  /**
   * The type specifying the purpose of this rename dialog.
   * 
   * @author Martin Novak
   */
  public enum RenameType
  {
    RENAME, PASTE
  }

  /**
   * The identifier for this dialog. It will be used to save settings in dialog settings.
   */
  private static final String DIALOG_ID = RenameJavaObject.class.getName();

  private static final String SETTINGS_MATCH_CLASS_NAME = "matchClassName";
  
  private static final String SETTINGS_KEEP_PACKAGE = "keepPackage";

  /**
   * The purpose of this dialog
   */
  private RenameType type;

  /**
   * Referenced java object
   */
  private JavaObject jo;

  private Text nameEdit;

  private Button matchClassNameCheck;

  private Text classNameEdit;

  private Button keepPackageCheck;

  private Text packageNameEdit;
  
  private Application targetApp;
  
  private String finalName;
  
  private String finalClass;
  
  private String finalPackage;

  /**
   * This is the default package name which is being set for paste action.
   */
  private String defaultPackageName;

  /**
   * Signalizes that we are programatically modifying some field, so we shouldn't accept
   * events from these fields.
   */
  private boolean modifying;

  public RenameJavaObject(Shell shell, RenameType type, JavaObject jo, Application targetApp, String defaultPackageName)
  {
    super(shell, DIALOG_ID);

    this.type = type;
    this.jo = jo;
    this.targetApp = targetApp;
    this.defaultPackageName = defaultPackageName;
  }

  protected void initialize()
  {
    // RENAME BUTTONS UNDER MAC
    Button okBtn = getButton(IDialogConstants.OK_ID);
    //okBtn.setEnabled(false);
    if("carbon".equals(SWT.getPlatform())) {
      Button cancelBtn = getButton(IDialogConstants.CANCEL_ID);
      if(type == RenameType.RENAME) {
        okBtn.setText("Rename");
        //cancelBtn.setText("Do not rename");
      }
      else if(type == RenameType.PASTE) {
        okBtn.setText("Paste");
        //cancelBtn.setText("Do not paste");
      }
    }
    
    Image img = VortexPlugin.getImage(IconConstants.WIZARD_BANNER);
    setTitleImage(img);
    if(type == RenameType.PASTE) {
      nameEdit.setText(generateAutomaticDesignObjectNameForPaste());
      String title = "Paste " + getDesignObjectTypeName();
      getShell().setText(title);
      setTitle(title);
      setDescription("Name the " + getDesignObjectTypeName() + " and fill class name to paste");
    }
    else {
      nameEdit.setText(jo.getClassName());
      String title = "Rename " + jo.getName();
      getShell().setText(title);
      setTitle(title);
      setDescription("Type new name of the " + getDesignObjectTypeName());
    }
    
    IDialogSettings settings = super.getDialogBoundsSettings();
    boolean keepPackage = true;
    if(settings.get(SETTINGS_KEEP_PACKAGE) != null)
      keepPackage = settings.getBoolean(SETTINGS_KEEP_PACKAGE);
    keepPackageCheck.setSelection(keepPackage);
    packageNameEdit.setEnabled(keepPackage);
    packageNameEdit.setText(getDesignObjectPackageName());
    
    boolean matchClassName = true;
    if(settings.get(SETTINGS_MATCH_CLASS_NAME) != null)
      matchClassName = settings.getBoolean(SETTINGS_MATCH_CLASS_NAME);
    matchClassNameCheck.setSelection(matchClassName);
    classNameEdit.setEnabled(matchClassName);
    classNameEdit.setText(generateAutomaticClassName());
    
    // ADD SOME LISTENERS
    nameEdit.addModifyListener(this);
    keepPackageCheck.addSelectionListener(this);
    packageNameEdit.addModifyListener(this);
    matchClassNameCheck.addSelectionListener(this);
    classNameEdit.addModifyListener(this);
  }

  /**
   * Generates automatically new legal design object name from within application.
   */
  private String generateAutomaticDesignObjectNameForPaste()
  {
    String initName = jo.getName();
    String packageName = jo.getPackage();
    int counter = 0;
    
    while(true) {
      String testName = initName + counter;
      if(targetApp.getJavaObject(packageName, testName) == null)
        return testName;
    }
  }

  private String generateAutomaticClassName()
  {
    return nameEdit.getText();
  }

  protected Control createDialogArea(Composite parent)
  {
    Composite area = (Composite) super.createDialogArea(parent);

    String dobjType = getDesignObjectTypeName();

    DialogBuilder2 builder = new DialogBuilder2(area);
    builder.createComposite(2);

    nameEdit = builder.createEditRow("Name:");
    builder.createSeparatorRow(true);

    matchClassNameCheck = builder.createCheckboxRow("Match class name with " + dobjType + " name");
    classNameEdit = builder.createEditRow("Class name:");
    builder.createSeparatorRow(true);

    keepPackageCheck = builder.createCheckboxRow("Keep class in the existing package: "
                                                 + getDesignObjectPackageName());
    packageNameEdit = builder.createEditRow("Package name:");

    builder.closeComposite();
    builder.finishBuilder();

    return area;
  }

  /**
   * This generates automaticamente package name for this action. This is what will be
   * filled in package edit.
   */
  private String getDesignObjectPackageName()
  {
    if(type == RenameType.PASTE) {
      return defaultPackageName;
    }
    else
      return jo.getPackage();
  }

  private String getDesignObjectTypeName()
  {
    switch(jo.getDesignType()) {
      case DesignObject.TYPE_LIBRARY:
        return "class";
      case DesignObject.TYPE_ACTION:
        return "action";
      case DesignObject.TYPE_SCHEDULEDACTION:
        return "scheduled action";
      case DesignObject.TYPE_WIDGET:
        return "soap widget";
      default:
        return "Unknown design object type: " + jo.getDesignType() + " please report";
    }
  }

  /**
   * This function fills the packages list.
   * 
   * TODO: implement packages list here
   */
  private void fillPackagesList()
  {
    // IJavaSearchScope scope= SearchEngine.createJavaSearchScope(new IJavaElement[]
    // {root});
  }

  public void modifyText(ModifyEvent e)
  {
    if(modifying)
      return;
   
    this.modifying = true;
    try {
      setupClassAndPackageEdits();
      checkForError();
    }
    finally {
      this.modifying = false;
    }
  }

  public void widgetDefaultSelected(SelectionEvent e)
  {
    // IGNORE
  }

  public void widgetSelected(SelectionEvent e)
  {
    this.modifying = true;
    
    try {
      setupClassAndPackageEdits();
      checkForError();
    }
    finally {
      this.modifying = false;
    }
  }
  
  /**
   * Returns the list with all status messages. If there is some status message which does
   * have plugin dependent information to 1 or have empry message, then it means that
   * there is no error, but we just should block the ok button.
   */
  private List<IStatus> getErrors()
  {
    List<IStatus> errs = new ArrayList<IStatus>();
    
    // CHECK THE DeSIGN OBJECT NAME
    String name = nameEdit.getText();
    if(name.length() == 0)
      errs.add(generateStatus("Name of the design object cannot be empty", IStatus.ERROR));
    DesignObject dobj = targetApp.getDesignObject(name);
    String className = classNameEdit.getText();
    String packageName = packageNameEdit.getText();
    DesignObject dobj1 = targetApp.getJavaObject(packageName, className);
    if(dobj == jo && dobj1 == jo && type == RenameType.RENAME)
      errs.add(new Status(IStatus.ERROR, VortexPlugin.PLUGIN_ID, 1, "", null));
    else if(dobj != null)
      errs.add(generateStatus("Design object '" + name + "' already exists in the application", IStatus.ERROR));
    else if(dobj1 != null)
      errs.add(generateStatus("Class '" + packageName + "." + className + "' already exists in the application", IStatus.ERROR));
    
    // CHECK THE EXISTENCE OF THE PACKAGE.CLASS IN THE TARGET APPLICATION
    // ALSO NOTE THAT IF IT REFERS TO ITSELF, WE SHOULD IGNORE IT

     
    // CHECK THE CLASS NAME
    int idx = className.indexOf('.');
    if(idx == -1)
      errs.add(JavaConventions.validateJavaTypeName(className));
    else
      errs.add(generateStatus("Class name cannot contain '.'", IStatus.ERROR));
    
    // CHECK PACKAGE NAME
    if(packageName.length() > 0) {
      errs.add(JavaConventions.validatePackageName(packageName));
    }
    
    return errs;
  }
  
  /**
   * Simplification of generating status message. It fills the status with the proper
   * information from the vortex plugin.
   */
  private IStatus generateStatus(String msg, int severity)
  {
    return new Status(severity, VortexPlugin.PLUGIN_ID, 0, msg, null);
  }

  /**
   * Checks error in the dialog, and displays/clears the error.
   */
  private void checkForError()
  {
    List<IStatus> errors = getErrors();
    
    IStatus st = getFirstError(errors, IStatus.ERROR);
    if(st != null) {
      setErrorMessage(st.getMessage());
      return;
    }
    
    st = getFirstError(errors, IStatus.WARNING);
    if(st != null) {
      setMessage(st.getMessage(), IMessageProvider.NONE);
      return;
    }
    
    setErrorMessage(null);
  }

  /**
   * Returns the first status with the specified severity, or null if no status is found.
   */
  private IStatus getFirstError(List<IStatus> errors, int severity)
  {
    for(IStatus st : errors) {
      if(st.getSeverity() == severity)
        return st;
    }
    
    return null;
  }
  
  private void setupClassAndPackageEdits()
  {
    // AT FIRST SETUP CLASS NAME EDIT
    if(matchClassNameCheck.getSelection()) {
      classNameEdit.setText(generateAutomaticClassName());
    }
    
    if(keepPackageCheck.getSelection()) {
      packageNameEdit.setText(getDesignObjectPackageName());
    }
  }

  public boolean close()
  {
    // NOW WRITE SETTINGS TO THE SYSTEM
    IDialogSettings settings = super.getDialogBoundsSettings();
    settings.put(SETTINGS_KEEP_PACKAGE, keepPackageCheck.getSelection());
    settings.put(SETTINGS_MATCH_CLASS_NAME, matchClassNameCheck.getSelection());
    
    return super.close();
  }
  
  protected void okPressed()
  {
    this.finalName = nameEdit.getText();
    this.finalClass = classNameEdit.getText();
    this.finalPackage = packageNameEdit.getText();
    
    super.okPressed();
  }

  /**
   * Returns the final name of the design object. Note that calling does have sense only
   * after closing the dialog.
   */
  public String getName()
  {
    return finalName;
  }

  public String getClassName()
  {
    return finalClass;
  }

  public String getPackageName()
  {
    return finalPackage;
  }

  protected Point getInitialSize()
  {
    Point p = super.getInitialSize();
    p.y = convertVerticalDLUsToPixels(215);
    return p;
  }
}
