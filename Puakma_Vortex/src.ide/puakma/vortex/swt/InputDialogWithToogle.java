/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    20/07/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.swt;

import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * Standard input dialog with toogle checkbox on the bottom.
 *
 * @author Martin Novak
 * @see InputDialog2
 */
public class InputDialogWithToogle extends InputDialog2
{
  public static final String KEY_TOOGLE = "toogle";
  private boolean toogle;
  private Button toogleBtn;
  private String toogleText;

  public InputDialogWithToogle(Shell parentShell, String dialogTitle,
                               String dialogMessage, String textLabel, String initialValue,
                               KeyValueValidator validator, boolean toogle, String toogleText)
  {
    super(parentShell, dialogTitle, dialogMessage, textLabel, initialValue, validator);
    
    this.toogle = toogle;
    this.toogleText = toogleText;
  }
  
  /**
   * After dialog closes, this returns user-selected toogle button state
   */
  public boolean getToogle()
  {
    return toogle;
  }

  protected void createCustomControls(Composite parent)
  {
    toogleBtn = new Button(parent, SWT.CHECK);
    GridData gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL
                               | GridData.VERTICAL_ALIGN_CENTER);
    gd.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
    toogleBtn.setLayoutData(gd);
    toogleBtn.setText(toogleText);
  }
  
  protected void initialize()
  {
    super.initialize();
    
    toogleBtn.setSelection(toogle);
    toogleBtn.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        validateInput();
      }
    });
  }

  protected void okPressed()
  {
    this.toogle = toogleBtn.getSelection();
    
    super.okPressed();
  }

  protected void gatherItemsToValidate(Map toValidate)
  {
    super.gatherItemsToValidate(toValidate);
    toValidate.put(KEY_TOOGLE, new Boolean(toogleBtn.getSelection()));
  }
}
