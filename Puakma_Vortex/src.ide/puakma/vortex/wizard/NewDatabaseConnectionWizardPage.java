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

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import puakma.coreide.ObjectsFactory;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.DatabaseConnection;
import puakma.coreide.objects2.TornadoDatabaseConstraints;
import puakma.vortex.IconConstants;
import puakma.vortex.JdbcDriverDefinition;
import puakma.vortex.VortexPlugin;
import puakma.vortex.swt.DialogBuilder2;

public class NewDatabaseConnectionWizardPage extends WizardPage
{
  /**
   * Text field with database connection name
   */
  private Text conNameText;
  
  private Combo driverCombo;
  
  private Text dbNameText;
  
  private Text dbUrlText;
  
  private Text userText;
  
  private Text pwdText;
  
  private Application application;

  private ModifyListener listener = new ModifyListener() {
    public void modifyText(ModifyEvent e)
    {
      updateErrorMsg();
    }
  };

  protected NewDatabaseConnectionWizardPage(Application application)
  {
    super("newDbConnectionPage");
    
    this.application = application;
    
    setTitle("Create New Database Connection");
    setImageDescriptor(VortexPlugin.getImageDescriptor(IconConstants.WIZARD_BANNER));
    setDescription("Create New Database Connection.");
  }

  public void createControl(Composite parent)
  {
    DialogBuilder2 builder = new DialogBuilder2(parent);
    Composite content = builder.createComposite(2);
    
    conNameText = builder.createEditRow("Connection name:");
    conNameText.addModifyListener(listener);
    
    builder.createGroup("Database options", 2);
    
    boolean readOnly = false;
    driverCombo = builder.createComboRow("Database driver class:", readOnly);
    driverCombo.addModifyListener(listener);
    driverCombo.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {  }
      public void widgetSelected(SelectionEvent e) {
        int sel = driverCombo.getSelectionIndex();
        JdbcDriverDefinition[] defs = VortexPlugin.getDefault().listJdbcDriverDefinitions();
        dbUrlText.setText(defs[sel].getSampleUrl());
        driverCombo.setText(defs[sel].getDriverClass());
      }
    });
    dbNameText = builder.createEditRow("Database name:");
    dbNameText.addModifyListener(listener);
    dbUrlText = builder.createEditRow("Database url:");
    dbUrlText.addModifyListener(listener);
    
    builder.closeGroup();
    
    builder.createGroup("Database authentication", 2);
    
    userText = builder.createEditRow("User name:");
    userText.addModifyListener(listener);
    pwdText = builder.createEditRow("Password:");
    pwdText.addModifyListener(listener);
    pwdText.setEchoChar('*');
    
    builder.closeGroup();
    
    builder.closeComposite();
    builder.finishBuilder();
    
    setControl(content);
    setPageComplete(false);
    conNameText.setFocus();
    
    // AND FILL THE COMBO WITH THE DRIVERS
    JdbcDriverDefinition[] defs = VortexPlugin.getDefault().listJdbcDriverDefinitions();
    for(int i = 0; i < defs.length; ++i) {
      driverCombo.add(defs[i].getName() + " - " + defs[i].getDriverClass());
    }
  }
  
  /**
   * Updates error message after modifying some control.
   */
  private void updateErrorMsg()
  {
    String errorMsg = checkError();
    setErrorMessage(errorMsg);
    setPageComplete(errorMsg == null);
  }

  /**
   * Returns the most important error message. If there is no error in the dialog,
   * returns null.
   *
   * @return String with error message or null when no error is found
   */
  private String checkError()
  {
    Text[] c = new Text[] {
        conNameText, dbNameText, dbUrlText, userText,
    };
    String fields[] = new String[] {
      "connection name", "database name", "database url", "user name"
    };
    for(int i = 0; i < c.length; ++i) {
      if(c[i].getText().length() == 0)
        return "Field with " + fields[i] + " cannot be empty";
    }
    
    if(driverCombo.getText().length() == 0)
      return "You have to type class for the database driver";
    
    if(application.getDatabaseConnection(conNameText.getText()) != null)
      return "Database connection " + conNameText.getText() + " already exists";
    
    TornadoDatabaseConstraints consts = application.getServer().getTornadoDatabaseConstraints();
    if(conNameText.getText().length() > consts.getMaxDbCon_DbConNameLen())
      return "Name of the connection name cannot be longer than "
             + consts.getMaxDbCon_DbConNameLen()
             + " which is the maximum on the server";
    if(driverCombo.getText().length() > + consts.getMaxDbCon_DbDriverLen())
      return "Name of the driver class cannot be longer than "
             + consts.getMaxDbCon_DbDriverLen()
             + " which is the maximum on the server";
    if(dbNameText.getText().length() > consts.getMaxDbCon_DbNameLen())
      return "Name of the database name cannot be longer than "
             + consts.getMaxDbCon_DbConNameLen()
             + " which is the maximum on the server";
    if(conNameText.getText().length() > consts.getMaxDbCon_DbUrlLen())
      return "URL of the database cannot be longer than "
             + consts.getMaxDbCon_DbUrlLen()
             + " which is the maximum on the server";
    if(userText.getText().length() > consts.getMaxDbCon_DbUserNameLen())
      return "User name cannot be longer than "
             + consts.getMaxDbCon_DbUserNameLen()
             + " which is the maximum on the server";
    if(pwdText.getText().length() > consts.getMaxDbCon_DbPasswordLen())
      return "Password cannot be longer than "
             + consts.getMaxDbCon_DbPasswordLen()
             + " which is the maximum on the server";
    

    return null;
  }
  
  public String getConnectionName()
  {
    return conNameText.getText();
  }
  
  public String getDriverClass()
  {
    return driverCombo.getText();
  }
  
  public String getDatabaseName()
  {
    return dbNameText.getText();
  }
  
  public String getUrl()
  {
    return dbUrlText.getText();
  }
  
  public String getUserName()
  {
    return userText.getText();
  }
  
  public String getPwd()
  {
    return pwdText.getText();
  }

  public void setApplication(Application application)
  {
    this.application = application;
  }
  
  public DatabaseConnection getDatabaseObject()
  {
    DatabaseConnection obj = ObjectsFactory.createDbConnection(getConnectionName());

    obj.setName(getConnectionName());
    obj.setDriverClass(getDriverClass());
    obj.setDatabaseName(getDatabaseName());
    obj.setDatabaseUrl(getUrl());
    obj.setUserName(getUserName());
    obj.setPassword(getPwd());
    
    return obj;
  }
}
