/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    25.12.2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import puakma.coreide.ConnectionPrefsReader;
import puakma.vortex.swt.DialogBuilder2;
import puakma.vortex.swt.SWTUtil;

/**
 * This class is the composite which shows properties for connection to server or
 * application.
 *
 * @author Martin Novak
 */
public class ConnectionComposite extends Composite implements ExtendedConnectionPrefs, ModifyListener, SelectionListener
{
  private Text nameEdit;
  private Text hostEdit;
  private Text portEdit;
  private Button sslCheckBox;
  private Text userEdit;
  private Text pwdEdit;
  private Button savePwdCheckBox;
  private Text pathEdit;
  private Text groupEdit;
  private Text appEdit;
  private boolean dirty;
  private String previousName;

  public ConnectionComposite(Composite parent, boolean showApplicationFields)
  {
    super(parent, SWT.NULL);
    setLayout(new GridLayout(3, false));

    DialogBuilder2 builder = new DialogBuilder2(this, 3);
    
    nameEdit = builder.createEditRow("Name:");
    hostEdit = builder.createEditRow("Host:");
    portEdit = builder.createEditRow("Port:");
    SWTUtil.setIntValidation(portEdit);
    sslCheckBox = builder.createCheckboxRow("Use SSL");
    
    builder.appendLabel("SOAPDesigner path:");
    pathEdit = builder.appendEdit("");
    pathEdit.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    Button b = builder.appendButton("Default");
    b.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e)
      {
        pathEdit.setText(ConnectionPrefsReader.DEFAULT_PATH);
        setDirty(true);
      }
    });
    
    if(showApplicationFields) {
      Label l = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
      l.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
      
      groupEdit = builder.createEditRow("Group:");
      appEdit = builder.createEditRow("Application:");
    }
    
    builder.createSeparatorRow(true);
    
    userEdit = builder.createEditRow("User name:");
    pwdEdit = builder.createEditRow("Password:");
    // TODO: there is a bug in SWT - change it back to password field
    //PasswordRow(parent, "Password:");
    pwdEdit.setEchoChar('*');
    savePwdCheckBox = builder
        .createCheckboxRow("Save password (Note that the password is saved in unecrypted format)");
    
    // SETUP LISTENERS
    nameEdit.addModifyListener(this);
    hostEdit.addModifyListener(this);
    portEdit.addModifyListener(this);
    sslCheckBox.addSelectionListener(this);
    userEdit.addModifyListener(this);
    pwdEdit.addModifyListener(this);
    savePwdCheckBox.addSelectionListener(this);
    pathEdit.addModifyListener(this);
    if(groupEdit != null)
      groupEdit.addModifyListener(this);
    if(appEdit != null)
      appEdit.addModifyListener(this);
    
    builder.finishBuilder();
  }
  
  public void setDirty(boolean isDirty)
  {
    this.dirty = isDirty;
  }

  public void setup(ConnectionPrefsReader reader)
  {
    if(reader == null) {
      setEnabled(false);
      nameEdit.setText("");
      hostEdit.setText("");
      sslCheckBox.setSelection(false);
      portEdit.setText("");
      userEdit.setText("");
      pwdEdit.setText("");
      savePwdCheckBox.setSelection(false);
      pathEdit.setText(ConnectionPrefsReader.DEFAULT_PATH);
      if(groupEdit != null)
        groupEdit.setText("");
      if(appEdit != null)
        appEdit.setText("");
      
      this.previousName = null;
    }
    else {
      setEnabled(true);
      this.previousName = reader.getName();
      
      nameEdit.setText(reader.getName());
      hostEdit.setText(reader.getHost());
      portEdit.setText(Integer.toString(reader.getPort()));
      sslCheckBox.setSelection(reader.isUsingSsl());
      userEdit.setText(reader.getUser());
      pwdEdit.setText(reader.getPwd());
      savePwdCheckBox.setSelection(reader.getSavePwd());
      pathEdit.setText(reader.getDesignerPath());
      if(groupEdit != null)
        groupEdit.setText(reader.getGroup());
      if(appEdit != null)
        appEdit.setText(reader.getApplication());
    }
    
    setDirty(false);
  }

  public boolean isDirty()
  {
    return dirty;
  }
  
  public String getHost()
  {
    return hostEdit.getText();
  }

  public String getUser()
  {
    return userEdit.getText();
  }

  public int getPort()
  {
    try {
      return Integer.parseInt(portEdit.getText());
    }
    catch(NumberFormatException ex) {
      return sslCheckBox.getSelection() ? 443 : 80;
    }
  }

  public String getPwd()
  {
    if(getSavePwd())
      return pwdEdit.getText();
    else
      return "";
  }

  public String getName()
  {
    return nameEdit.getText();
  }

  public String getPreviousName()
  {
    return previousName;
  }
  
  public String getDesignerPath()
  {
    return pathEdit.getText();
  }

  public boolean isUsingSsl()
  {
    return sslCheckBox.getSelection();
  }

  public String getApplication()
  {
    if(appEdit == null)
      return "";
    return appEdit.getText();
  }

  public String getGroup()
  {
    if(groupEdit == null)
      return "";
    return groupEdit.getText();
  }

  public boolean hasNameChanged()
  {
    return getName().equals(previousName) == false;
  }

  public boolean getSavePwd()
  {
    return savePwdCheckBox.getSelection();
  }

  public void modifyText(ModifyEvent e)
  {
    setDirty(true);
  }

  public void widgetSelected(SelectionEvent e)
  {
    setDirty(true);
  }

  public void widgetDefaultSelected(SelectionEvent e)
  {
    setDirty(true);
  }
}
