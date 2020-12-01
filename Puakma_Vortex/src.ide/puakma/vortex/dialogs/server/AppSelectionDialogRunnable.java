/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    17/07/2006
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

import org.eclipse.core.runtime.IProgressMonitor;

import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.ProgressMonitor;
import puakma.vortex.swt.DialogBuilder2;

public interface AppSelectionDialogRunnable
{
  public void setController(AppSelectionDialogController controller);
  
  /**
   * Runs the clients code. If there is some error when executing code, throw
   * some Exception, and the wizard code sets up error message from the
   * {@link Exception#getLocalizedMessage()} function.
   * 
   * @param monPart is the {@link ProgressMonitor} which shows the progress of
   *          the main operation
   * 
   */
  public void run(IProgressMonitor monitor) throws Exception;

  /**
   * Clients should provide window title for the wizard.
   */
  public String getWindowTitle();

  /**
   * Clients should provide title text for the white area.
   */
  public String getTitle();

  /**
   * Clients should provide text for description in the white area on the top.
   */
  public String getDescription();

  public String getOkButtonText();

  /**
   * This sets up application every time selection changes.
   */
  public void setSelectedApplication(Application application);

  /**
   * When this function is called, client should add some custom content to the
   * dialog here.
   */
  public void appendCustomControls(DialogBuilder2 builder);
  
  /**
   * Validates custom controls input.
   */
  public String validateCustomControls();

  public void gatherData();
}
