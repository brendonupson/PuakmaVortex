/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Nov 25, 2005
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

import java.util.Arrays;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import puakma.coreide.ConfigurationManager;
import puakma.coreide.ConnectionPrefs;
import puakma.coreide.ConnectionPrefsImpl;
import puakma.coreide.ConnectionPrefsReader;
import puakma.utils.lang.ArrayUtils;
import puakma.vortex.VortexPlugin;

/**
 * This dialog manages connection preferences stored in one file.
 *
 * @author Martin Novak
 */
public class ConnectionManagerDialog extends Dialog
{
  public static final int TYPE_CONNECTIONS = 0;
  public static final int TYPE_FAVORITES = 1;
  private static final String DIALOG_ID = "ConnectionManagerDialog";

  private ConfigurationManager manager;
  private ExtendedConnectionPrefs composite;
  private List connectionsList;
  private int type;
  private ToolBar toolbar;
  
  public ConnectionManagerDialog(Shell parentShell, int type)
  {
    super(parentShell);
    this.type = type;
    
    // NOW SETUP MANAGER
    if(type == TYPE_FAVORITES)
      this.manager = VortexPlugin.getDefault().getFavoritesAppsManager();
    else
      this.manager = VortexPlugin.getDefault().getServersManager();
  }
  
  /**
   * The only button here we accept is with CLOSE id.
   */
  protected void buttonPressed(int buttonId)
  {
    if(buttonId == IDialogConstants.CLOSE_ID) {
      close();
    }
  }
  
  public boolean close()
  {
    doSave();
    return super.close();
  }

  protected void createButtonsForButtonBar(Composite parent)
  {
    createButton(parent, IDialogConstants.CLOSE_ID, IDialogConstants.CLOSE_LABEL, true);
  }

  protected Control createDialogArea(Composite parent) {
    parent = (Composite) super.createDialogArea(parent);
    
    GridLayout layout = new GridLayout(2, false);
    layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
    layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
    layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
    layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
    parent.setLayout(layout);
    
    SashForm sash = new SashForm(parent, SWT.HORIZONTAL);
    
    // SETUP DIALOG
    Composite c = new Composite(sash, SWT.NULL);
    layout = new GridLayout();
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
    layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
    c.setLayout(layout);
    c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    toolbar = new ToolBar(c, SWT.FLAT);
    toolbar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
    connectionsList = new List(c, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
    connectionsList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    
    Composite cmp = (Composite) (this.composite = new ConnectionComposite(sash, type == TYPE_FAVORITES));
    cmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    
    // SETUP THE TOOLBAR
    final ToolItem addTi = new ToolItem(toolbar, SWT.NONE);
    addTi.setText("Add");
    final ToolItem remTi = new ToolItem(toolbar, SWT.NONE);
    remTi.setText("Remove");
    
    // NOW HOOK ALL LISTENERS
    addTi.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e)
      {
      }
      public void widgetSelected(SelectionEvent e)
      {
        addConnection();
      }
    });
    remTi.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e)
      {
      }
      public void widgetSelected(SelectionEvent e)
      {
        remConnection();
      }
    });
    connectionsList.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) { }
      public void widgetSelected(SelectionEvent e)
      {
        connectionChanged();
      }
    });
    
    // SETUP SOME MORE STUFF
    sash.setWeights(new int[] {1,3});
    if(type == TYPE_FAVORITES)
      getShell().setText("Manage Favorite Applications");
    else
      getShell().setText("Manage Servers");
    
    // NOW LOAD THAT STUFF
    String[] names = listAllNames();
    Arrays.sort(names);
    for(int i = 0; i < names.length; ++i)
      connectionsList.add(names[i]);
    resetSelection();
    connectionChanged();
    
    return parent;
  }

  /**
   * This function is executed when selection changes in the connections list box.
   */
  private void connectionChanged()
  {
    doSave();
    
    String[] selection = connectionsList.getSelection();
    if(selection.length == 0) {
      resetSelection();
      return;
    }

    String name = selection[0];
    ConnectionPrefs pref = manager.getConnectionPref(name);
    composite.setup(pref);
  }

  /**
   * Saves the child dialog.
   */
  private void doSave()
  {
    if(composite.isDirty()) {
      // CHECK IF THE COMPOSITE CONTAINS VALID VALUES (NAME) IF NO ASK USER WHAT TO DO
      String saveName = checkParams();
      // SAVE IT
      doSave(saveName);
    }
  }

  /**
   * Check if the current connection has valid params, if not, ask user to correct them.
   */
  private String checkParams()
  {
    // CHECK DUPLICATE NAME
    if(composite.hasNameChanged()) {
      String name = composite.getName();
      final String[] names = listAllNames();
      for(int i = 0; i < names.length; ++i) {
        if(name.equalsIgnoreCase(names[i])) {
          InputDialog dlg = new InputDialog(getShell(), "Name error",
              "Type connection name", name, new IInputValidator() {
                public String isValid(String newText)
                {
                  if(newText.length() == 0)
                    return "Invalid name";
                  for(int j = 0; j < names.length; ++j) {
                    if(newText.equalsIgnoreCase(names[j]))
                      return "Connection with name " + newText + " already exists.\nPlease provide another name.";
                  }
                  return null;
                }
              });
          if(dlg.open() == Window.OK) {
            return dlg.getValue();
          }
        }
      }
    }
    // CHECK EMPTY NAME
    if(composite.getName().length() == 0) {
      final String[] names = listAllNames();
      InputDialog dlg = new InputDialog(getShell(), "Name error",
          "Type connection name", "", new IInputValidator() {
            public String isValid(String newText)
            {
              if(newText.length() == 0)
                return "Invalid name";
              for(int j = 0; j < names.length; ++j) {
                if(newText.equalsIgnoreCase(names[j]))
                  return "Connection with name " + newText + " already exists.\nPlease provide another name.";
              }
              return null;
            }
          });
      if(dlg.open() == Window.OK) {
        return dlg.getValue();
      }
    }
    
    return null;
  }

  /**
   * Lists all saved names in the directory with connection preferences.
   *
   * @return list of all names of the preferences.
   */
  private String[] listAllNames()
  {
    ConnectionPrefs[] prefs = manager.listConnectionPrefs();
    String[] s = new String[prefs.length];
    for(int i = 0; i < prefs.length; ++i) {
      s[i] = prefs[i].getName();
    }
    
    return s;
  }

  private void doSave(String saveName)
  {
    ConnectionPrefs prefs = new ConnectionPrefsImpl(composite);
    if(saveName != null) {
      prefs.setName(saveName);
    }
    if(composite.hasNameChanged()) {
      // ALSO UPDATE UI!
      int selection = connectionsList.getSelectionIndex();
      String[] items = connectionsList.getItems();
      for(int i = 0; i < items.length; ++i) {
        if(items[i].equals(composite.getPreviousName())) {
          items[i] = prefs.getName();
          break;
        }
      }
      connectionsList.setItems(items);
      connectionsList.select(selection);
    }
    try {
      // IF RENAMED, REMOVE OLD CONNECTION
      if(composite.getPreviousName().equalsIgnoreCase(prefs.getName()) == false) {
        try { manager.removeConnection(composite.getPreviousName()); }
        catch(Exception ex) { VortexPlugin.log(ex); }
      }

      manager.save(prefs);
      composite.setup(prefs);
    }
    catch(Exception e) {
      // TODO: should user see that???
      VortexPlugin.log(e);
    }
  }
  
  private void addConnection()
  {
    doSave();

    String[] names = listAllNames();
    String nameTemplate = "New Connection";
    String name = nameTemplate;
    int index = 1;
    while(true) {
      boolean found = false;
      for(int i = 0; i < names.length; ++i) {
        if(names[i].equalsIgnoreCase(name)) {
          found = true;
          break;
        }
      }
      if(found) {
        name = nameTemplate + index;
        index ++;
      }
      else
        break;
    }
    ConnectionPrefs prefs;
    try {
      prefs = manager.createConnectionPrefs(name);
      composite.setup(prefs);
      connectionsList.add(name);
      // ALSO SELECT THE NEWLY ADDED ITEM
      String[] items = connectionsList.getItems();
      index = ArrayUtils.indexOf(items, name);
      connectionsList.select(index);
    }
    catch(Exception ex) {
      VortexPlugin.log(ex);
    }
  }
  
  private void remConnection()
  {
    if(connectionsList.getItemCount() == 0)
      return;

    // REMOVE THE OLD CONNECTION
    String prevName = composite.getPreviousName();
    try {
      manager.removeConnection(prevName);
      String[] items = connectionsList.getItems();
      int index = ArrayUtils.indexOf(items, prevName);
      connectionsList.remove(index);
      
      int count = connectionsList.getItemCount();
      if(count > 0) {
        if(index >= count)
          index = count - 1;
        
        connectionsList.select(index);
        String name = connectionsList.getItem(index);
        ConnectionPrefsReader pref = manager.getConnectionPref(name);
        composite.setup(pref);
      }
      else {
        composite.setup(null);
      }
    }
    catch(Exception e) {
      VortexPlugin.log(e);
    }
    
    // NOW SETUP NEWLY THE DIALOG
    //resetSelection();
  }

  /**
   * This function selects the first item in the connections list.
   */
  private void selectTheFirstItem()
  {
    if(connectionsList.getItemCount() > 0) {
      connectionsList.select(0);
      String name = connectionsList.getItem(0);
      ConnectionPrefsReader pref = manager.getConnectionPref(name);
      composite.setup(pref);
    }
    else {
      composite.setup(null);
    }
  }

  /**
   * Resets the selection to the initial state. So if there are still some items, select the first one
   * otherwise disable connection preferences dialog.
   */
  private void resetSelection()
  {
    if(connectionsList.getItemCount() == 0) {
      composite.setup(null);
    }
    else {
      connectionsList.select(0);
    }
  }
  
  protected IDialogSettings getDialogBoundsSettings()
  {
    IDialogSettings settings = VortexPlugin.getDefault().getDialogSettings();
    IDialogSettings ret = settings.getSection(DIALOG_ID);
    if(ret == null)
      ret = settings.addNewSection(DIALOG_ID);
    return ret;
  }
}
