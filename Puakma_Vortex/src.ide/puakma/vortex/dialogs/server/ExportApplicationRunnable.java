/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    28/07/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.dialogs.server;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import puakma.coreide.objects2.Application;
import puakma.vortex.VortexPlugin;
import puakma.vortex.preferences.PreferenceConstants;
import puakma.vortex.swt.DialogBuilder2;

public class ExportApplicationRunnable implements AppSelectionDialogRunnable
{
  private Text fileEdit;
  private AppSelectionDialogController controller;
  private Button exportSourceCheck;
  
  private Application application;
  private boolean exportSources;
  private String fileName;

  public String getDescription()
  {
    return "Export Tornado application as pmx file";
  }

  public String getOkButtonText()
  {
    return "Export";
  }

  public String getTitle()
  {
    return "Export Tornado Application";
  }

  public String getWindowTitle()
  {
    return "Export Tornado Application";
  }

  public void run(IProgressMonitor monitor) throws Exception
  {
    String group = application.getGroup();
    String name = application.getName();
    File file = new File(fileName);
    
    monitor.beginTask("Downloading sources", 1);
    
    IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
    store.setValue(PreferenceConstants.PREF_EXPORT_LAST_FILE, fileName);
    store.setValue(PreferenceConstants.PREF_EXPORT_SOURCES, exportSources);
    
    try {
      application.getServer().exportPmx(group, name, file, exportSources);
      monitor.worked(1);
    }
    finally {
      monitor.done();
    }
  }

  public void setSelectedApplication(Application application)
  {
    this.application = application;
  }

  public void appendCustomControls(DialogBuilder2 builder)
  {
    Composite c = builder.createComposite(2);
    GridData gd = (GridData) c.getLayoutData();
    gd.horizontalIndent = gd.verticalIndent = 5;
    
    IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
    String defaultPath = store.getString(PreferenceConstants.PREF_EXPORT_LAST_FILE);
    boolean isOpen = false;
    String dlgTitle = "Export Application as";
    String[][] filterExtNames = {
        { "pmx", "Tornado applications" },
        { "*", "All files" },
    };
    fileEdit = builder.createFileSelectionRow("Export as:", defaultPath, isOpen, dlgTitle, filterExtNames);
    fileEdit.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        controller.validateInput();
      }
    });
    
    //builder.appendButton("Export sources", SWT.CHECK, 2);
    exportSourceCheck = builder.createCheckboxRow("Export sources");
    exportSourceCheck.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        controller.validateInput();
      }
    });
    boolean exportSourcesChecked = store.getBoolean(PreferenceConstants.PREF_EXPORT_SOURCES);
    exportSourceCheck.setSelection(exportSourcesChecked);
    
    builder.closeComposite();
  }

  public String validateCustomControls()
  {
    String fileName = fileEdit.getText();
    File f = new File(fileName);
//    if(f.exists() == false)
//      return "File " + f + " doesn't exist";
    if(f.isDirectory())
      return "File " + f + " is a directory";
    
    return null;
  }

  public void setController(AppSelectionDialogController controller)
  {
    this.controller = controller;
  }

  public void gatherData()
  {
    this.fileName = fileEdit.getText();
    this.exportSources = exportSourceCheck.getSelection();
  }
}
