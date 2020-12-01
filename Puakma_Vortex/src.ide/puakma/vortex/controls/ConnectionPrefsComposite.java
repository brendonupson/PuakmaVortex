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
package puakma.vortex.controls;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import puakma.coreide.ConfigurationManager;
import puakma.coreide.ConnectionPrefs;
import puakma.vortex.VortexPlugin;
import puakma.vortex.dialogs.ConnectionManagerDialog;


/**
 * @author Martin Novak
 */
public class ConnectionPrefsComposite extends Composite
{
  /**
   * Key for storing the default selected connection
   */
  private static final String DEFAULT_SELECTION = "puakma.vortex.serverList.defaultSelection";

  /**
   * Combo box with all connection preferences.
   */
  protected Combo combo;
  
  private Button editBtn;
  
  private Button refreshBtn;
  
  private ConnectionsPageListener[] listeners = new ConnectionsPageListener[0];
  
  /**
   * The last selected server connection name
   */
  private String lastSelection;
  
  private SelectionListener uiListener = new SelectionListener() {
    public void widgetDefaultSelected(SelectionEvent e) {  }
    public void widgetSelected(SelectionEvent e)
    {
      // SAVE LAST SELECTION
      if(e.widget == combo) {
        lastSelection = combo.getText();
        fireEvent(true);
      }
      else
        fireEvent(false);
    }
  };

  /**
   * @param parent
   * @param showRefreshBtn
   */
  public ConnectionPrefsComposite(Composite parent, boolean showRefreshBtn)
  {
    super(parent, SWT.NULL);
    
    GridLayout gl = new GridLayout(showRefreshBtn ? 4 : 3, false);
    gl.marginHeight = gl.marginWidth = 0;
    setLayout(gl);
    
    // add label
    Label l = new Label(this, SWT.NULL);
    l.setText("Available server connections:");
    
    // add combo box
    combo = new Combo(this, SWT.READ_ONLY);
    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
    combo.setLayoutData(gd);
    combo.addSelectionListener(uiListener);
    
    // and add all buttons
    editBtn = new Button(this, SWT.NULL);
    editBtn.setText("Edit...");
    editBtn.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {  }
      public void widgetSelected(SelectionEvent e) {
        editButtonPressed();
      }
    });
    if(showRefreshBtn) {
      refreshBtn = new Button(this, SWT.NULL);
      refreshBtn.addSelectionListener(uiListener);
      refreshBtn.setText("Refresh");
    }
    
    // ADD DESTROY LISTENER
    addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        // GET THE SELECTION FROM THE COMBO, AND SAVE IT TO THE PREFERENCES
        String name = lastSelection;
        IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
        store.setValue(DEFAULT_SELECTION, name);
      }
    });

    // DO SOME INITIALIZATION    
    refreshCombo();
    lastSelection = combo.getText();
  }
  
  public void addListener(ConnectionsPageListener listener)
  {
    for(int i = 0; i < listeners.length; ++i)
      if(listener == listeners[i])
        return;

    ConnectionsPageListener[] ls = new ConnectionsPageListener[listeners.length + 1];
    System.arraycopy(listeners, 0, ls, 0, listeners.length);
    ls[listeners.length] = listener;
    listeners = ls;
  }

  /**
   * Handler for pressing edit preferences list button.
   */
  private void editButtonPressed()
  {
//    ConnectionEditDialog dlg = new ConnectionEditDialog(getShell());
    ConnectionManagerDialog dlg = new ConnectionManagerDialog(getShell(), ConnectionManagerDialog.TYPE_CONNECTIONS);
    dlg.open();
    refreshCombo();
  }
  
  /**
   * Refreshes combo box with connection preferences.
   */
  private void refreshCombo()
  {
    combo.removeAll();

    // GET THE DEFAULT SERVER
    IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
    String defaultSel = store.getString(DEFAULT_SELECTION);
    
    int selected = 0;
    ConfigurationManager manager = VortexPlugin.getDefault().getServersManager();
    ConnectionPrefs[] prefs = manager.listConnectionPrefs();
    
    if(prefs.length == 0) {
      editButtonPressed();
      prefs = manager.listConnectionPrefs();
    }
    
    for(int i = 0; i < prefs.length; ++i) {
      combo.add(prefs[i].getName());
      if(defaultSel.equals(prefs[i].getName()))
        selected = i;
    }

    combo.select(selected);
    boolean isRefresh = true;
    fireEvent(isRefresh);
  }
  
  /**
   * Finds the currently selected connection preference.
   *
   * @return ConnectionPrefsImpl associated with selection or null
   */
  public ConnectionPrefs getSelectedPreference()
  {
    if(combo.getSelectionIndex() == -1)
      return null;

    String text = combo.getText();
    
    ConfigurationManager manager = VortexPlugin.getDefault().getServersManager();
    ConnectionPrefs[] prefs = manager.listConnectionPrefs();
    for(int i = 0; i < prefs.length; ++i) {
      if(text.equals(prefs[i].getName()))
        return prefs[i];
    }
    
    return null;
  }

  /**
   * This function setups control to grab the whole row in the parent dialog
   * @param numCols is the number of parent columns to take
   */
  public void grabRow(int numCols)
  {
    GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
    gd.horizontalSpan = numCols;
    setLayoutData(gd);
  }
  
  private void fireEvent(boolean isRefresh)
  {
    for(int i = 0; i < listeners.length; ++i) {
      try {
        if(isRefresh)
          listeners[i].changeSelection(combo.getText());
        else
          listeners[i].refreshPressed();
      }
      catch(Exception ex) { VortexPlugin.log(ex); }
    }
  }
}
