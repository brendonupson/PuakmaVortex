/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    02/07/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.project.inconsistency;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import puakma.coreide.event.InconsistenciesList;
import puakma.coreide.event.InconsistencyEvent;
import puakma.coreide.objects2.DatabaseObject;
import puakma.coreide.objects2.DesignObject;
import puakma.vortex.swt.DesignObjectPanel;
import puakma.vortex.swt.DialogBuilder2;
import puakma.vortex.swt.TitleAreaDialog2;

public class InconsDialog extends TitleAreaDialog2
{
  private org.eclipse.swt.widgets.List inconsistenciesList;
  private org.eclipse.swt.widgets.List inconsistentObjectsList;
  private DesignObjectPanel doPanel;
  private InconsistenciesList incons;
  private Text nameEdit;
  private Button btnDelete;
  private Button btnRename;
  
  public InconsDialog(Shell parentShell, InconsistenciesList incons)
  {
    super(parentShell, null);
    
    this.incons = incons;
  }

  protected Control createDialogArea(Composite parent)
  {
    Composite c = (Composite) super.createDialogArea(parent);
    DialogBuilder2 builder = new DialogBuilder2(c);
    
    GridLayout gl = (GridLayout) c.getLayout();
    gl.numColumns = 2;
    gl.makeColumnsEqualWidth = true;
    c.setLayout(gl);
    
    inconsistenciesList = builder.appendList();
    inconsistenciesList.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e)
      {
        selectInconsistency();
      }
    });
    inconsistentObjectsList = builder.appendList();
    
    // ADD SOME BUTTONS ABOUT WHAT USER SHOULD DO
    Composite c1 = builder.createComposite(3);
    c1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    
    Button[] btns = { builder.appendButton("Remove object"),
    builder.appendButton("Raname object")};
    btnDelete = btns[0];
    btnRename = btns[1];
    builder.setupToogleButtonsAsRadios(btns);
    nameEdit = builder.appendEdit("");
    nameEdit.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e)
      {
        nameChanged();
      }
    });
    
    builder.closeComposite();
    
    doPanel = new DesignObjectPanel();
    doPanel.create(builder.getCurrentComposite());
    Composite cx = doPanel.getComposite();
    cx.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
    
    builder.finishBuilder();
    
    // NOW SETUP UI
    setupUI();
    return c;
  }

  /**
   * Sets up user interface from the 
   */
  private void setupUI()
  {
    InconsistencyEvent[] events = incons.listEvents();
    for(int i = 0; i < events.length; ++i) {
      String text = events[i].getText();
      inconsistenciesList.add(text, i);
    }
    
    inconsistenciesList.select(0);
  }
  
  /**
   * Handler for selecting next inconsistency
   */
  private void selectInconsistency()
  {
    int index = inconsistenciesList.getSelectionIndex();
    if(index == -1)
      return;
    InconsistencyEvent event = incons.getEvent(index);
    
    inconsistentObjectsList.removeAll();
    DatabaseObject[] dbos = event.getDatabaseObjects();
    if(dbos != null) {
      for(int i = 0; i < dbos.length; ++i) {
        String str = event.getTextFor(dbos[i]);
        inconsistentObjectsList.add(str);
      }
    }
    else {
      DesignObject[] dobs = event.getDesignObjects();
      for(int i = 0; i < dobs.length; ++i) {
        String str = event.getTextFor(dobs[i]);
        inconsistentObjectsList.add(str);
      }
    }
  }
  
  /**
   * This pushes rename button in the case it is not pushed yet. It should be
   * called specially after
   */
  private void nameChanged()
  {
    if(btnRename.getSelection() == false) {
      btnDelete.setSelection(false);
      btnRename.setSelection(true);
    }
  }

  protected void initialize()
  {
    
  }
}
