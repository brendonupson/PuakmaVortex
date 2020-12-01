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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import puakma.coreide.ConnectionPrefs;
import puakma.coreide.ServerManager;
import puakma.coreide.objects2.Server;
import puakma.coreide.objects2.TornadoDatabaseConstraints;
import puakma.vortex.IconConstants;
import puakma.vortex.VortexPlugin;
import puakma.vortex.controls.ConnectionPrefsComposite;
import puakma.vortex.swt.DialogBuilder2;

public class NewApplicationWizardPage extends WizardPage
{
  /**
   * Selection of the server to which we should connect to.
   */
  private ConnectionPrefsComposite prefsComposite;
  
  /**
   * Text field with application name
   */
  private Text nameText;
  
  /**
   * Text field with group name
   */
  private Text groupText;
  
  /**
   * If the connection is not null then we know about ApplicationBeans (we refreshed them)
   * otherwise we have to refresh them. Well, we don't know about the appbeans, but
   * we already requested refresh.
   */
  Server server;

  private ModifyListener listener = new ModifyListener() {
    public void modifyText(ModifyEvent e)
    {
      updateErrorMsg();
    }
  };

  protected NewApplicationWizardPage()
  {
    super("newApplicationPage");
    
    setTitle("Create New Application");
    setImageDescriptor(VortexPlugin.getImageDescriptor(IconConstants.WIZARD_BANNER));
    setDescription("Enter basic properties for the new application.");
  }

  public void createControl(Composite parent)
  {
    DialogBuilder2 builder = new DialogBuilder2(parent);
    Composite content = builder.createComposite(2);
    
    prefsComposite = new ConnectionPrefsComposite(content, false);
    prefsComposite.grabRow(2);
    
    groupText = builder.createEditRow("Group");
    groupText.addModifyListener(listener);
    nameText = builder.createEditRow("Name");
    nameText.addModifyListener(listener);
    
    builder.closeComposite();
    builder.finishBuilder();
    
    setControl(content);
    setPageComplete(false);
    nameText.setFocus();
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
        if(server == null) {
          server = ServerManager.createServerConnection(prefs);
          server.refresh();
        }
        if(server.getApplication(groupText.getText(), nameText.getText()) != null)
          return "Application " + (groupText.getText().length() == 0 ? "" : groupText.getText() + '/')
                 + nameText.getText() + " is already present on the server";
      }
    }
    catch(Exception e) {
      return e.getLocalizedMessage();
    }

    String name = nameText.getText();
    if(name.length() == 0)
      return "You have to type name of the new application.";
    TornadoDatabaseConstraints consts = server.getTornadoDatabaseConstraints();
    if(name.length() > consts.getMaxApplication_AppNameLen())
      return "Name of the application cannot be longer than "
             + consts.getMaxApplication_AppNameLen()
             + " which is the maximum on the server";
    String group = groupText.getText();
    if(group.length() > consts.getMaxApplication_AppGroupLen())
      return "Name of the group cannot be longer than "
             + consts.getMaxApplication_AppGroupLen()
             + " which is the maximum on the server";
    
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

  public Server getServer()
  {
    return server;
  }
}
