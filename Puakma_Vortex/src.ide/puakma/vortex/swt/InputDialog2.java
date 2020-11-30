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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A simple input dialog for soliciting an input string from the user.
 * <p>
 * This concrete dialog class can be instantiated as is, or further subclassed
 * as required.
 * </p>
 */
public class InputDialog2 extends TitleAreaDialog2
{
  public static final String KEY_INPUT = "input";

  /**
   * The input value; the empty string by default.
   */
  private String value = "";//$NON-NLS-1$

  /**
   * The input validator, or <code>null</code> if none.
   */
  private KeyValueValidator validator;

  /**
   * Input text widget.
   */
  private Text text;
  
  private String title;
  
  private String message;
  
  private String textLabel;

  /**
   * Creates an input dialog with OK and Cancel buttons. Note that the dialog
   * will have no visual representation (no widgets) until it is told to open.
   * <p>
   * Note that the <code>open</code> method blocks for input dialogs.
   * </p>
   * 
   * @param parentShell the parent shell, or <code>null</code> to create a
   *          top-level shell
   * @param dialogTitle the dialog title, or <code>null</code> if none
   * @param dialogMessage the dialog message, or <code>null</code> if none
   * @param initialValue the initial input value, or <code>null</code> if none
   *          (equivalent to the empty string)
   * @param validator an input validator, or <code>null</code> if none
   */
  public InputDialog2(Shell parentShell, String dialogTitle, String dialogMessage,
                      String textLabel, String initialValue, KeyValueValidator validator)
  {
    super(parentShell, null);
    
    this.title = dialogTitle;
    this.message = dialogMessage;
    this.textLabel = textLabel;
    if(initialValue == null)
      value = "";//$NON-NLS-1$
    else
      value = initialValue;
    this.validator = validator;
  }

  /*
   * (non-Javadoc) Method declared on Dialog.
   */
  protected void buttonPressed(int buttonId)
  {
    if(buttonId == IDialogConstants.OK_ID) {
      value = text.getText();
    }
    else {
      value = null;
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
      shell.setText(getTitle());
  }
  
  private String getTitle()
  {
    return title;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
   */
  protected void createButtonsForButtonBar(Composite parent)
  {
    // create OK and Cancel buttons by default
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
                            true);
    createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    // do this here because setting the text will set enablement on the ok
    // button
    text.setFocus();
    if(value != null) {
      text.setText(value);
      text.selectAll();
    }
    Button okBtn = getButton(IDialogConstants.OK_ID);
    okBtn.setEnabled(false);
  }
  
  /*
   * (non-Javadoc) Method declared on Dialog.
   */
  protected Control createDialogArea(Composite parent)
  {
    // create composite
    Composite composite = (Composite) super.createDialogArea(parent);
    
    // CREATE CONTAINER FOR LABEL AND TEXT
    Composite c = new Composite(composite, SWT.NULL);
    c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    GridLayout gl = new GridLayout(2, false);
    //gl.marginHeight = gl.marginWidth = 0;
    c.setLayout(gl);
    
    Label l = new Label(c, SWT.NULL);
    l.setText(textLabel);
    
    // create text control
    text = new Text(c, SWT.SINGLE | SWT.BORDER);
    text.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                                    | GridData.HORIZONTAL_ALIGN_FILL));

    createCustomControls(composite);

    return composite;
  }

  /**
   * This function is for implementor to implement custom controls in the
   * dialog.
   */
  protected void createCustomControls(Composite composite)
  {
    // DO NOTHIGN
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
   * Returns the validator.
   * 
   * @return the validator
   */
  protected KeyValueValidator getValidator()
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
      Map<String, String> toValidate = new HashMap<String, String>(3, 0.9f);
      gatherItemsToValidate(toValidate);
      errorMessage = validator.isValid(toValidate);
    }
    // Bug 16256: important not to treat "" (blank error) the same as null
    // (no error)
    setErrorMessage(errorMessage);
  }

  /**
   * This returns names, and values pairs containing values of the controls to validate.
   */
  protected void gatherItemsToValidate(Map<String, String> toValidate)
  {
    toValidate.put(KEY_INPUT, text.getText());
  }

  protected void initialize()
  {
    setTitle(title);
    setDescription(message);
    
    text.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e)
      {
        validateInput();
      }
    });
  }
  
  protected Point getInitialSize()
  {
    Point shellSize = super.getInitialSize();
    return new Point(shellSize.x, convertVerticalDLUsToPixels(130));
  }
}
