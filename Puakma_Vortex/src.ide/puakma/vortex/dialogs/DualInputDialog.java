/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 28, 2005
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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A simple input dialog with two text fields.
 * 
 * @author Martin Novak
 */
public class DualInputDialog extends Dialog
{
  /**
   * The title of the dialog.
   */
  private String title;

  /**
   * The message to display, or <code>null</code> if none.
   */
  private String message;

  /**
   * The input value; the empty string by default.
   */
  private String value = "";//$NON-NLS-1$

  private String value2 = "";//$NON-NLS-1$

  /**
   * The input validator, or <code>null</code> if none.
   */
  private IInputValidator validator;
  
  /**
   * The input validator for the second control, or <code>null</code> if none.
   */
  private IInputValidator validator2;

  /**
   * Input text widget.
   */
  private Text text;

  /**
   * The second input text.
   */
  private Text text2;

  /**
   * Error message label widget.
   */
  private Text errorMessageText;

  private boolean isPassword;

  /**
   * Creates an input dialog with OK and Cancel buttons. Note that the dialog
   * will have no visual representation (no widgets) until it is told to open.
   * <p>
   * Note that the <code>open</code> method blocks for input dialogs.
   * </p>
   * 
   * @param parentShell the parent shell
   * @param dialogTitle the dialog title, or <code>null</code> if none
   * @param dialogMessage the dialog message, or <code>null</code> if none
   * @param initialValue the initial input value, or <code>null</code> if none
   *          (equivalent to the empty string)
   * @param initialValue2 is the initial value of the second text control, or
   *          can be possibly <code>null</code> if none
   * @param validator1 an input validator, or <code>null</code> if none
   * @param validator2
   */
  public DualInputDialog(Shell parentShell, String dialogTitle,
      String dialogMessage, String initialValue, String initialValue2,
      IInputValidator validator1, IInputValidator validator2)
  {
    super(parentShell);
    this.title = dialogTitle;
    message = dialogMessage;
    if(initialValue == null)
      value = "";//$NON-NLS-1$
    else
      value = initialValue;
    if(initialValue2 == null)
      value2 = "";//$NON-NLS-1$
    else
      value2 = initialValue;
    this.validator = validator1;
    this.validator2 = validator2;
  }

  /*
   * (non-Javadoc) Method declared on Dialog.
   */
  protected void buttonPressed(int buttonId)
  {
    if(buttonId == IDialogConstants.OK_ID) {
      value = text.getText();
      value2 = text2.getText();
    }
    else {
      value = null;
      value2 = null;
    }
    super.buttonPressed(buttonId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
   */
  protected void configureShell(Shell shell)
  {
    super.configureShell(shell);
    if(title != null)
      shell.setText(title);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
   */
  protected void createButtonsForButtonBar(Composite parent)
  {
    // create OK and Cancel buttons by default
    createButton(parent, IDialogConstants.OK_ID,
        IDialogConstants.OK_LABEL, true);
    createButton(parent, IDialogConstants.CANCEL_ID,
        IDialogConstants.CANCEL_LABEL, false);
    //do this here because setting the text will set enablement on the ok
    // button
    text.setFocus();
    if(value != null) {
      text.setText(value);
      text.selectAll();
    }
    
    if(value2 != null) {
      text2.setText(value);
    }
  }

  /*
   * (non-Javadoc) Method declared on Dialog.
   */
  protected Control createDialogArea(Composite parent)
  {
    // create composite
    Composite composite = (Composite) super.createDialogArea(parent);
    // create message
    if(message != null) {
      Label label = new Label(composite, SWT.WRAP);
      label.setText(message);
      GridData data = new GridData(GridData.GRAB_HORIZONTAL
          | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL
          | GridData.VERTICAL_ALIGN_CENTER);
      data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
      label.setLayoutData(data);
      label.setFont(parent.getFont());
    }
    text = new Text(composite, SWT.SINGLE | SWT.BORDER);
    text.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
    text.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e)
      {
        validateInput();
      }
    });
    int style = SWT.SINGLE | SWT.BORDER;
    if(isPassword)
      style |= SWT.PASSWORD;
    text2 = new Text(composite, style);
    text2.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
    text2.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e)
      {
        validateInput();
      }
    });
    errorMessageText = new Text(composite, SWT.READ_ONLY);
    errorMessageText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
    errorMessageText.setBackground(errorMessageText.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

    applyDialogFont(composite);
    return composite;
  }

  /**
   * Returns the error message label.
   * 
   * @return the error message label
   * @deprecated use setErrorMessage(String) instead
   */
  protected Label getErrorMessageLabel()
  {
    return null;
  }

  /**
   * Returns the text area.
   * 
   * @return the text area
   */
  protected Text getText()
  {
    return text;
  }
  
  /**
   * Returns the second text area.
   * 
   * @return the second text area
   */
  protected Text getText2()
  {
    return text2;
  }

  /**
   * Returns the validator.
   * 
   * @return the validator
   */
  protected IInputValidator getValidator()
  {
    return validator;
  }

  /**
   * Returns the string typed into this input dialog.
   * 
   * @return the input string
   */
  public String getValue()
  {
    return value;
  }
  
  /**
   * Returns the string typed into the second input dialog.
   * 
   * @return the second input string
   */
  public String getValue2()
  {
    return value2;
  }

  /**
   * Validates the input.
   * <p>
   * The default implementation of this framework method delegates the request
   * to the supplied input validator object; if it finds the input invalid, the
   * error message is displayed in the dialog's message line. This hook method
   * is called whenever the text changes in the input field.
   * </p>
   */
  protected void validateInput()
  {
    String errorMessage = null;
    if(validator != null) {
      errorMessage = validator.isValid(text.getText());
    }
    
    // validate the second input
    if(errorMessage == null || errorMessage.length() == 0)
      if(validator2 != null)
        errorMessage = validator2.isValid(text2.getText());

    // Bug 16256: important not to treat "" (blank error) the same as null
    // (no error)
    setErrorMessage(errorMessage);
  }

  /**
   * Sets or clears the error message. If not <code>null</code>, the OK
   * button is disabled.
   * 
   * @param errorMessage the error message, or <code>null</code> to clear
   * @since 3.0
   */
  public void setErrorMessage(String errorMessage)
  {
    errorMessageText.setText(errorMessage == null ? "" : errorMessage); //$NON-NLS-1$
    Button okButton = getButton(IDialogConstants.OK_ID);
    okButton.setEnabled(errorMessage == null);
    errorMessageText.getParent().update();
  }
  
  protected IDialogSettings getDialogBoundsSettings()
  {
    // NOT IMPLEMENTED FOR PURPOSE - THIS IS LIKE DIALOG BOX...
    return null;
  }

  /**
   * You can set this method if you want this dialog to be password dialog. This
   * means that if you want the second input to be masked as password.
   */
  public void setPassword(boolean pwd)
  {
    this.isPassword = pwd;
  }
}
