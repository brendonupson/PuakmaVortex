/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    04/05/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.dbschema.generator;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import puakma.coreide.database.DatabaseGenerator2;
import puakma.coreide.objects2.Database;
import puakma.coreide.objects2.DatabaseConnection;
import puakma.vortex.VortexPlugin;
import puakma.vortex.preferences.PreferenceConstants;
import puakma.vortex.swt.ChangeablePage;
import puakma.vortex.swt.DialogBuilder2;
import puakma.vortex.swt.UIControlStatusSaver;

public class GeneratorOptionsPage extends WizardPage
{
  public static final String CONST_MYSQL = "Mysql";
  public static final String CONST_POSTGRE = "PostgreSQL";
  private static final String CONST_HSQLDB = "HsqlDB";
  /**
   * Note that we save generator for each item in the combo box
   */
  private DatabaseGenerator2[] generators;
  /**
   * Pages used for changing user interface
   */
  private ChangeablePage[] pages;
  private Database database;
  private Combo dbSelectionCombo;
  private Combo createDropIfCombo;
  private DatabaseConnection dbConnection;
  private UIControlStatusSaver statusSaver;
  /**
   * The index for the lastly used database
   */
  private int lastDbIndex;
  private boolean dirty;
  private Button[] errorHandlingCombo;

  protected GeneratorOptionsPage(Database database)
  {
    super("generatorOptions1");
    
    this.database = database;
  }
  
  protected GeneratorOptionsPage(Database database, DatabaseConnection connection)
  {
    super("generatorOptions1");
    
    this.database = database;
    this.dbConnection = connection;
    
    this.statusSaver = new UIControlStatusSaver();
    
    setTitle("Setup Generator");
    setDescription("Setup SQL DDL generator");
  }

  public void createControl(Composite parent)
  {
    DialogBuilder2 builder = new DialogBuilder2(parent);
    Composite composite = builder.createComposite(2);
    
    dbSelectionCombo = builder.createComboRow("Database", true);
    
    createDropIfCombo = builder.createComboRow("Existing table handling", true);
    
    builder.createSeparatorRow(true);
    
    builder.createGroup("Error handling", 1);
    errorHandlingCombo = new Button[3];
    errorHandlingCombo[0] = builder.createRadioButtonRow("Stop execution");
    errorHandlingCombo[1] = builder.createRadioButtonRow("Continue");
    errorHandlingCombo[2] = builder.createRadioButtonRow("Use transactions - on error rolls back.\nNote that this doesn't work on some databases like Mysql.");
    builder.closeGroup();
    
    builder.closeComposite();
    builder.finishBuilder();
    
    setControl(composite);
    
    setupUI();
  }

  /**
   * Sets up all user interface interaction.
   */
  private void setupUI()
  {
    IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
    
    // ADD ITEMS TO GENERATORS
    dbSelectionCombo.add(CONST_MYSQL);
    //dbSelectionCombo.add(CONST_POSTGRE);
    dbSelectionCombo.add(CONST_HSQLDB);
    
    // CREATE GENERATORS ARRAY
    generators = new DatabaseGenerator2[dbSelectionCombo.getItemCount()];
    
    // OK, SO NOW TRY TO DETECT FROM THE DATABASE CONNECTION WHAT DATABASE WE USE
    if(dbConnection != null) {
      String driver = dbConnection.getDriverClass();
      if(driver.indexOf("mysql") != -1)
        dbSelectionCombo.select(0);
      else if(driver.indexOf("hsql") != -1)
        dbSelectionCombo.select(1);
      else if(driver.indexOf("postgre") != -1)
        dbSelectionCombo.select(2);
      else
        dbSelectionCombo.select(0);
    }
    else
      dbSelectionCombo.select(0);
    // AND APPLY SELECTION
    selectDifferentDatabase();
    
    dbSelectionCombo.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        selectDifferentDatabase();
      }
    });
    
    // ADD DROP EXISTING TABLES MODE
    createDropIfCombo.add("Drop old tables");
    createDropIfCombo.add("Create new tables if old doesn't exist");
    int existingTablesAction = store.getInt(PreferenceConstants.PREF_DBGEN_EXISTING_TABLES_ACTION);
    createDropIfCombo.select(existingTablesAction);
    
    // NOW ADD ERROR HANDLING MODE
    for(int i = 0; i < errorHandlingCombo.length; ++i)
      errorHandlingCombo[i].setSelection(false);
    int errorHandlingDefaults = store.getInt(PreferenceConstants.PREF_DBGEN_ERROR_HANDLING);
    if(errorHandlingDefaults < 0 || errorHandlingDefaults >= errorHandlingCombo.length)
      errorHandlingDefaults = PreferenceConstants.DBGEN_ERROR_STOP;
    errorHandlingCombo[errorHandlingDefaults].setSelection(true);
    
    
    // HOOKUP ALL CONTROLS
    statusSaver.addControl(createDropIfCombo);
    statusSaver.addControl(dbSelectionCombo);
    statusSaver.saveStatus();
  }

  /**
   * Gets the generator for the database. Also sets dirty status to false.
   */
  public DatabaseGenerator2 setupAndGetGenerator()
  {
    int existingTableActionIndex = createDropIfCombo.getSelectionIndex();
    int errorHandlingIndex = 0;
    for(int i = 0; i < errorHandlingCombo.length; ++i) {
      if(errorHandlingCombo[i].getSelection()) {
        errorHandlingIndex = i;
        break;
      }
    }
    
    // SAVE THE PREFERENCES OF THE GENERATOR DIALOG
    IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
    store.setValue(PreferenceConstants.PREF_DBGEN_EXISTING_TABLES_ACTION, existingTableActionIndex);
    store.setValue(PreferenceConstants.PREF_DBGEN_ERROR_HANDLING, errorHandlingIndex);
    
    int index = dbSelectionCombo.getSelectionIndex();
    if(index == -1)
      return null;
    
    DatabaseGenerator2 generator = generators[index];
    // TODO: setup generator from the controls
    if(existingTableActionIndex == 0)
      generator.setDropTablePolicy(DatabaseGenerator2.DROP_POLICY_DROP);
    else
      generator.setDropTablePolicy(DatabaseGenerator2.DROP_POLICY_CREATE_IF_NOT_EXIST);
    generator.setErrorPolicy(errorHandlingIndex);
    
    dirty = false;
    return generator;
  }

  /**
   * This is a handler for selecting another database from combo box.
   */
  private void selectDifferentDatabase()
  {
    int index = dbSelectionCombo.getSelectionIndex();
    if(index == -1)
      return;
    
    DatabaseGenerator2 generator = generators[index];
    if(generator == null) {
      String text = dbSelectionCombo.getItem(index);
      if(CONST_MYSQL.equals(text)) {
        generator = new DatabaseGenerator2(database, DatabaseGenerator2.DB_MYSQL);
      }
      else if(CONST_HSQLDB.equals(text)) {
        generator = new DatabaseGenerator2(database, DatabaseGenerator2.DB_HSQLDB);
      }
      else {
        generator = new DatabaseGenerator2(database, DatabaseGenerator2.DB_POSTGRE);
      }
      generators[index] = generator;
    }
    
    // TODO: setup User interface here from generator
  }
  
  /**
   * Checks if the generator is dirty. This means that if something in the
   * current generator has been changed or the generator for database has been
   * changed.
   */
  public boolean isDirty()
  {
    int index = dbSelectionCombo.getSelectionIndex();
    return dirty || index != lastDbIndex;
  }
}
