/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 10, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.wizard.importPmx;

import java.io.File;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import puakma.coreide.ConnectionPrefs;
import puakma.coreide.ServerManager;
import puakma.coreide.objects2.Server;
import puakma.coreide.objects2.TornadoDatabaseConstraints;
import puakma.vortex.IconConstants;
import puakma.vortex.VortexPlugin;
import puakma.vortex.controls.ConnectionPrefsComposite;
import puakma.vortex.preferences.PreferenceConstants;
import puakma.vortex.swt.DialogBuilder2;


/**
 * @author Martin Novak
 */
public class ImportPmxWizardPage extends WizardPage
{
  private ConnectionPrefsComposite prefsComposite;
  private Text nameText;
  private Text groupText;
  private Text fileText;
  
  /**
   * If the connection is not null then we know about ApplicationBeans (we refreshed them)
   * otherwise we have to refresh them. Well, we don't know about the appbeans, but
   * we already requested refresh.
   */
  private Server connection;

  private ModifyListener listener = new ModifyListener() {
    public void modifyText(ModifyEvent e)
    {
      updateErrorMsg();
    }
  };

  protected ImportPmxWizardPage()
  {
    super("newPagePage");
    
    setTitle("Import Pmx File");
    setImageDescriptor(VortexPlugin.getImageDescriptor(IconConstants.WIZARD_BANNER));
    setDescription("Import Puakma Application from Pmx File.");
  }

  public void createControl(Composite parent)
  {
    DialogBuilder2 builder = new DialogBuilder2(parent);
    builder.createComposite(2);
    
    prefsComposite = new ConnectionPrefsComposite(builder.getCurrentComposite(), false);
    prefsComposite.grabRow(2);
    
    groupText = builder.createEditRow("Group:");
    groupText.addModifyListener(listener);
    nameText = builder.createEditRow("Name:");
    nameText.addModifyListener(listener);

    IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
    String defaultPath = store.getString(PreferenceConstants.PREF_IMPORT_LAST_FILE);
    String[][] filterExtsNames = {
        {"pmx", "Tornado archive"},
        {"*", "All files"},
    };
    fileText = builder.createFileSelectionRow("Select pmx file:", defaultPath, true,
                                              "Select Tornado Application Import File",
                                              filterExtsNames);
    fileText.addModifyListener(listener);

    setControl(builder.getCurrentComposite());
    builder.closeComposite();
    builder.finishBuilder();
    
    setPageComplete(false);
    groupText.setFocus();
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
    // Now try to find if the application already exists or not
    //
    ConnectionPrefs prefs = prefsComposite.getSelectedPreference();
    try {
      if(prefs != null) {
        if(connection == null) {
          // TODO:  maybe do not ask for user name/password on invalid user/pwd
          connection = ServerManager.createServerConnection(prefs);
          connection.refresh();
        }
        if(connection.getApplication(groupText.getText(), nameText.getText()) != null)
          return "Application " + (groupText.getText().length() == 0 ? "" : groupText.getText() + '/')
                 + nameText.getText() + " is already present on the server";
      }
    }
    catch(Exception e) {
      return e.getLocalizedMessage();
    }

    // CHECK THE LENGTH OF APPLICATION NAME AND GROUP
    String name = nameText.getText();
    if(name.length() == 0)
      return "You have to type name of the new application.";
    TornadoDatabaseConstraints consts = connection.getTornadoDatabaseConstraints();
    if(name.length() > consts.getMaxApplication_AppNameLen())
      return "Name of the application cannot be longer than "
             + consts.getMaxApplication_AppNameLen()
             + " which is the maximum on the server";
    String group = groupText.getText();
    if(group.length() > consts.getMaxApplication_AppGroupLen())
      return "Name of the group cannot be longer than "
             + consts.getMaxApplication_AppNameLen()
             + " which is the maximum on the server";
    
    if(fileText.getText().length() == 0)
      return "You have to choose file to import";
    if(getFile().isFile() == false)
      return "File " + getFile().toString() + " si not file";
    if(getFile().canRead() == false)
      return "File " + getFile().toString() + " si not readable";

    return null;
  }
  
  public String getName()
  {
    return nameText.getText();
  }
  
  public String getGroup()
  {
    return groupText.getText();
  }
  
  public File getFile()
  {
    return new File(fileText.getText());
  }

  public Server getServer()
  {
    return connection;
  }

  /**
   * Saves the last file dialog location to preference store
   */
  public void saveSettings()
  {
    IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
    // LAST FILE
    store.setValue(PreferenceConstants.PREF_IMPORT_LAST_FILE, getFile().toString());
  }
}
