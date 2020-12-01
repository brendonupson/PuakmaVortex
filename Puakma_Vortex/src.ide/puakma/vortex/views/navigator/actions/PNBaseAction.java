/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Aug 3, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.views.navigator.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;

import puakma.vortex.views.navigator.PuakmaResourceView;

public abstract class PNBaseAction extends Action implements ISelectionListener
{
  private PuakmaResourceView view;

  public PNBaseAction(String id, PuakmaResourceView view)
  {
    this.view = view;
    setId(id);
  }
  
  public void init()
  {
    IWorkbenchPage page = view.getSite().getPage();
    page.addSelectionListener(this);
  }
  
  public void dispose()
  {
    getView().getSite().getPage().removeSelectionListener(this);
  }
  
  /**
   * Gets associated Puakma Navigator view.
   *
   * @return PuakmaResourceView object
   */
  public PuakmaResourceView getView()
  {
    return this.view;
  }
  
  public abstract void run();

  /**
   * This handles key event for the action.
   *
   * @param event is the key event to handle
   * @return true if the event shouldn't be further processed
   */
  public abstract boolean handleKeyEvent(KeyEvent event);
  
  public abstract boolean qualifyForSelection();
  
  public Shell getShell()
  {
    return getView().getSite().getShell();
  }
}
