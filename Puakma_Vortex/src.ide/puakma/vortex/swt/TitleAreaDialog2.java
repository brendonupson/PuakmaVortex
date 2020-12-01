/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    22/05/2006
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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import puakma.vortex.VortexPlugin;

/**
 * This class is providing some simplification over setting
 * 
 * @author Martin Novak
 */
public abstract class TitleAreaDialog2 extends TitleAreaDialog
{
  private String dialogId;
  private String contextId;

  public TitleAreaDialog2(Shell parentShell, String dialogId)
  {
    super(parentShell);
    
    setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
    this.dialogId = dialogId;
  }
  
  public void setErrorMessage(String newErrorMessage)
  {
    super.setErrorMessage(newErrorMessage);
    
    enableOkButton(newErrorMessage == null);
  }
  
  /**
   * Enables/disables ok button, so the user can/cannot press ok to finish the dialog.
   */
  public void enableOkButton(boolean enable)
  {
    Button okBtn = getButton(IDialogConstants.OK_ID);
    if(okBtn != null)
      okBtn.setEnabled(enable);
  }
  
  public void setDescription(String description)
  {
    setMessage(description);
  }
  
  public void setHelpContextId(String contextId)
  {
    this.contextId = contextId;
  }

  /*
   * @see org.eclipse.jface.window.Window#configureShell(Shell)
   */
  protected void configureShell(Shell newShell)
  {
    super.configureShell(newShell);
    if(contextId != null)
      PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell, contextId);
  }

  protected IDialogSettings getDialogBoundsSettings()
  {
    if(dialogId == null)
      return null;
    
    IDialogSettings settings = VortexPlugin.getDefault().getDialogSettings();
    if(settings.getSection(dialogId) == null)
      settings.addNewSection(dialogId);
    return settings.getSection(dialogId);
  }
  
  protected Control createContents(Composite parent)
  {
    Control c = super.createContents(parent);
    
    initialize();
    
    return c;
  }

  /**
   * Clients have to override this method, and perform initialization here.
   */
  protected abstract void initialize();
  
  /**
   * Creates a separator above buttons.
   */
  protected Control createButtonBar(Composite parent)
  {
    Composite composite = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout();
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    layout.horizontalSpacing = 0;
    composite.setLayout(layout);
    composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    composite.setFont(parent.getFont());
    
    Label titleBarSeparator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
    titleBarSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    

    super.createButtonBar(composite);
    
    return composite;
  }

  protected Control createDialogArea(Composite parent)
  {
    Composite area = (Composite) super.createDialogArea(parent);
    
    Composite c = new Composite(area, SWT.NULL);
    
    GridLayout layout = new GridLayout();
    layout.marginHeight = convertHorizontalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
    layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
    layout.verticalSpacing = 0;
    layout.horizontalSpacing = 0;
    c.setLayout(layout);
    c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    c.setFont(parent.getFont());
    
    return c;
  }
  
  protected Point getInitialSize()
  {
    Point shellSize = super.getInitialSize();
    return new Point(shellSize.x, Math.max(convertVerticalDLUsToPixels(300), shellSize.y));
  }
}
