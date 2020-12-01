/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    May 18, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.application;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import puakma.coreide.ObjectsFactory;
import puakma.coreide.PuakmaCoreException;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.DatabaseConnection;
import puakma.utils.lang.StringUtil;
import puakma.vortex.JdbcDriverDefinition;
import puakma.vortex.VortexPlugin;
import puakma.vortex.swt.DialogBuilder2;
import puakma.vortex.swt.NewLFEditorPage;

/**
 * @author Martin Novak
 */
public class DatabaseConnectionPage extends NewLFEditorPage
{
  private static final String CANNOT_SAVE_DATABASE_CONNECTION = "Cannot Save Database Connection";
  private Application application;
  private DatabaseConnection dbo;

  private Combo nameCombo;
  private Text databaseNameText;
  private Text urlText;
  private Combo driverCombo;
  private Text urlConnectionOptions;
  private Text userName;
  private Text pwd;
  private Text description;
  // TODO: implement properties viewer
  private TableViewer propertiesViewer;
  private Button renConBtn;
  private Button delConBtn;
  /**
   * For checking database connection availability on the server
   */
  private Button pingButton;
  private Label pingResultLabel;
  
  private ModifyListener controlListener = new ModifyListener() {
    public void modifyText(ModifyEvent e)
    {
      setDirty(true);
    }
  };

  public DatabaseConnectionPage(Composite parent, ApplicationEditor editor, Application application)
  {
    super(parent, editor);

    this.editor = editor;
    this.application = application;
    
    DialogBuilder2 builder = new DialogBuilder2(this);
    builder.createFormsLFComposite("Database Connections", false, 1);

    createDatabasePropertiesSection(builder);
    createPingSection(builder);
    
    builder.closeComposite();
    builder.finishBuilder();
    
    initialize();
  }

  private void createPingSection(DialogBuilder2 builder)
  {
    builder.createSection("Advanced",
                          "Here you can assure that your database connection is valid, and is able to connect to the server",
                          4);
    
    // CREATE PING BUTTON
    pingButton = builder.createButtonRow("Ping database");
    pingButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        doPingDatabaseConnection();
      }
    });
    
    pingResultLabel = builder.createLabelRow("");
    builder.closeSection();
  }

  private void createDatabasePropertiesSection(DialogBuilder2 builder)
  {
    builder.createSection("Database connections", null, 5);
    
    nameCombo = builder.createComboRow("Connection name", true);
    nameCombo.addSelectionListener(new SelectionListener() {
      public void widgetSelected(SelectionEvent e)
      {
        if(isDirty()) {
          if(MessageDialog.openConfirm(getShell(), "Save Database Connection",
                                       "Database connection is not saved, do you want to save it?")) {
            try {
              saveDbObject();
            }
            catch(Exception ex) {
              VortexPlugin.log(ex);
              MessageDialog.openError(getShell(), CANNOT_SAVE_DATABASE_CONNECTION, "Cannot save database " +
                  "connection.\nReason:\n" + ex.getLocalizedMessage());
              e.doit = false;
            }
          }
          else
            e.doit = false;
        }

        refreshPage();
      }
      public void widgetDefaultSelected(SelectionEvent e) {  }
    });
    Button b = builder.appendButton("New...");
    b.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        addConnection();
      }
    });
    renConBtn = builder.appendButton("Rename...");
    renConBtn.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        renameConnection();
      }
    });
    delConBtn = builder.appendButton("Delete");
    delConBtn.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        remConnection();
      }
    });
    builder.closeSection();
    
    builder.createSection("General properties",
                          "Specify general properties of the database connection", 4);
    builder.setEffectiveColumns(2);
    urlText = builder.createEditRow("Database server URL");
    urlText.addModifyListener(controlListener);
    databaseNameText = builder.createEditRow("Database name");
    databaseNameText.addModifyListener(controlListener);

    driverCombo = builder.createComboRow("Driver class", false);
    driverCombo.addModifyListener(controlListener);
    driverCombo.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        int sel = driverCombo.getSelectionIndex();
        JdbcDriverDefinition[] defs = VortexPlugin.getDefault().listJdbcDriverDefinitions();
        urlText.setText(defs[sel].getSampleUrl());
        driverCombo.setText(defs[sel].getDriverClass());
      }
    });
    urlConnectionOptions = builder.createEditRow("Database url options");
    urlConnectionOptions.addModifyListener(controlListener);
    
    userName = builder.createEditRow("User name");
    userName.addModifyListener(controlListener);
    pwd = builder.createEditRow("Password");
    pwd.setEchoChar('*');
    pwd.addModifyListener(controlListener);
    builder.setEffectiveColumns(4);
    description = builder.createMemoRow("Description", 4);
    description.addModifyListener(controlListener);
    builder.closeSection();
  }
  
  /**
   * Initializes page, and fills all needed stuff to the controls.
   */
  private void initialize()
  {
    fillDatabaseCombo();
    fillDriversCombo();
    setDirty(false);
  }

  /**
   * This function pings database server using the currently filled connection
   * data.
   */
  protected void doPingDatabaseConnection()
  {
    DatabaseConnection dbc;
    try {
      dbc = createWorkingCopy();
      String sz[] = application.getServer().pingDatabase(dbc);
      String z = StringUtil.merge(sz, "\n");
      pingResultLabel.setText(z);
    }
    catch(PuakmaCoreException e) {
      pingResultLabel.setText("Database connection ping failed. Reason:\n" + e.getLocalizedMessage());
    }
  }

  /**
   * This function fill all known database drivers to driver combo box
   */
  private void fillDriversCombo()
  {
    JdbcDriverDefinition[] defs = VortexPlugin.getDefault().listJdbcDriverDefinitions();
    for(int i = 0; i < defs.length; ++i) {
      driverCombo.add(defs[i].getName() + " - " + defs[i].getDriverClass());
    }
  }

  /**
   * Fills combo with all of the existing database connection names
   */
  private void fillDatabaseCombo()
  {
    nameCombo.removeAll();
    nameCombo.setText("");
    DatabaseConnection[] dbObjs = application.listDatabases();
    for(int i = 0; i < dbObjs.length; ++i) {
      DatabaseConnection db = dbObjs[i];
      nameCombo.add(db.getName());
    }
    
    if(dbObjs.length > 0)
      nameCombo.select(0);
    else
      enabledAll(false);
    refreshPage();
  }

  /**
   * Enables/disables all controls on the page except new connection button
   *
   * @param enabled if true then enable controls, otherwise disbles them
   */
  private void enabledAll(boolean enabled)
  {
    nameCombo.setText("");
    databaseNameText.setText("");
    urlText.setText("");
    driverCombo.setText("");
    urlConnectionOptions.setText("");
    userName.setText("");
    pwd.setText("");
    description.setText("");

    nameCombo.setEnabled(enabled);
    renConBtn.setEnabled(enabled);
    delConBtn.setEnabled(enabled);
    databaseNameText.setEnabled(enabled);
    urlText.setEnabled(enabled);
    driverCombo.setEnabled(enabled);
    urlConnectionOptions.setEnabled(enabled);
    userName.setEnabled(enabled);
    pwd.setEnabled(enabled);
    description.setEnabled(enabled);
    // TODO: propertiesViewer.getTable().setEnabled(enabled);
    
    pingButton.setEnabled(enabled);
    pingResultLabel.setText("");
  }

  private void addConnection()
  {
    try {
      checkDatabaseSaved();
    }
    catch(Exception ex) {
      VortexPlugin.log(ex);
      MessageDialog.openError(getShell(), CANNOT_SAVE_DATABASE_CONNECTION,
                              "Connot save database connection\nReason:\n"
                              + ex.getLocalizedMessage());
      return;
    }
    
    InputDialog dlg = new InputDialog(getShell(), "New Database Connection", 
                                      "Type name of the new database connection", "", 
                                      new IInputValidator() {
      public String isValid(String newText)
      {
        if(newText.length() == 0)
          return "Database connection name cannot be empty";
        if(application.getDatabaseConnection(newText) != null)
          return "Database connection " + newText + " already exists";
        return null;
      }
    });

    if(dlg.open() == Window.OK) {
      DatabaseConnection dbo = ObjectsFactory.createDbConnection(dlg.getValue());
      try {
        application.addObject(dbo);
        nameCombo.add(dbo.getName());
        nameCombo.select(nameCombo.getItemCount() - 1);
        this.dbo = dbo;
        enabledAll(true);
        setDirty(false);
      }
      catch(PuakmaCoreException e) {
        VortexPlugin.log(e);
        MessageDialog.openError(getShell(), "Cannot Create New Database Connection",
            "Connot create new database connection\nReason:\n" + e.getLocalizedMessage());
      }
    }
  }

  /**
   * This function asks user to save database connection, and then saves it or
   * simply returns.
   */
  private void checkDatabaseSaved() throws PuakmaCoreException, IOException
  {
    if(isDirty() && MessageDialog.openConfirm(getShell(), "Save Database Connection",
                                      "Database connection is not saved, do you want to save it?")) {
      saveDbObject();
    }
  }
  
  private void remConnection()
  {
    try {
      dbo.remove();
      dbo = null;
      int index = nameCombo.getSelectionIndex();
      nameCombo.remove(index);
      if(nameCombo.getItemCount() == 0) {
        enabledAll(false);
      }
      else
        nameCombo.select(0);
      refreshPage();
    }
    catch(Exception e) {
      VortexPlugin.log(e);
    }
  }

  public void doSave(IProgressMonitor monitor)
  {
    try {
      saveDbObject();
    }
    catch(Exception e) {
      MessageDialog.openError(getShell(), "Cannot Update Database Connection", e.getLocalizedMessage());
    }
  }
  
  /**
   * Fills the whole page with information in the actually selected database connection
   * in the database connection combo box
   */
  private void refreshPage()
  {
    String text = nameCombo.getText();
    if(text.length() == 0)
      return;

    DatabaseConnection dbo = application.getDatabaseConnection(text);

    databaseNameText.setText(StringUtil.safeString(dbo.getDatabaseName()));
    driverCombo.setText(StringUtil.safeString(dbo.getDriverClass()));
    urlText.setText(StringUtil.safeString(dbo.getDatabaseUrl()));
    urlConnectionOptions.setText(StringUtil.safeString(dbo.getDatabaseUrlOptions()));
    userName.setText(StringUtil.safeString(dbo.getUserName()));
    pwd.setText(StringUtil.safeString(dbo.getPassword()));
    description.setText(StringUtil.safeString(dbo.getDescription()));

    this.dbo = dbo;
    setDirty(false);
  }

  public boolean setFocus()
  {
    return urlText.setFocus();
  }
  
  private void saveDbObject() throws PuakmaCoreException, IOException
  {
    // MAKE SOME CHECKS BEFORE SAVE

    if(dbo != null) {
      DatabaseConnection dbc = createWorkingCopy();
      dbc.commit();
    }

    setDirty(false);
  }
  
  private DatabaseConnection createWorkingCopy() throws PuakmaCoreException
  {
    String dbName = databaseNameText.getText();

    DatabaseConnection dbc = dbo.makeWorkingCopy();
    dbc.setDatabaseName(dbName);
    dbc.setDatabaseUrl(urlText.getText());
    dbc.setDriverClass(driverCombo.getText());
    dbc.setDatabaseUrlOptions(urlConnectionOptions.getText());
    dbc.setUserName(userName.getText());
    dbc.setPassword(pwd.getText());
    dbc.setDescription(description.getText());
    return dbc;
  }

  private void renameConnection()
  {
    final String oldName = nameCombo.getText();
    InputDialog dlg = new InputDialog(getShell(), "Rename Database Connection",
        "Type name of the database connection", oldName, new IInputValidator() {
          public String isValid(String newText)
          {
            if(newText.length() == 0)
              return "Database connection name cannot be empty";
            if(oldName.equals(newText))
              return "Same name";
            if(application.getDatabaseConnection(newText) != null)
              return "Database connection " + newText + " already exists";
            return null;
          }
        });

    if(dlg.open() == Window.OK) {
      try {
        DatabaseConnection dbo = application.getDatabaseConnection(oldName).makeWorkingCopy();
        String newName = dlg.getValue();
        dbo.setName(newName);
        dbo.commit();
        fillDatabaseCombo();
        enabledAll(true);

        selectDbInCombo(dbo);
      }
      catch(Exception e) {
        VortexPlugin.log(e);
        MessageDialog.openError(getShell(), "Cannot Create New Database Connection",
            "Connot create new database connection\nReason:\n" + e.getLocalizedMessage());
      }
    }    
  }

  /**
   * Selects database object in combo.
   * @param dbo is the database connection object to select
   */
  private void selectDbInCombo(DatabaseConnection dbo)
  {
    String[] items = nameCombo.getItems();
    int index = StringUtil.arrayContainsString(items, dbo.getName());
    nameCombo.select(index);
    refreshPage();
  }

  /**
   * Selects database object.
   *
   * @param object
   */
  public void selectDbObject(DatabaseConnection object)
  {
    try {
      checkDatabaseSaved();
      
      selectDbInCombo(object);
    }
    catch(Exception e) {
      VortexPlugin.log(e);
    }
  }

  public void disposePage()
  {
    
  }
}
