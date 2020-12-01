/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Nov 29, 2005
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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import puakma.coreide.ConnectionPrefs;
import puakma.coreide.ConnectionPrefsImpl;
import puakma.vortex.IdeException;
import puakma.vortex.InterruptedAuthException;
import puakma.vortex.VortexPlugin;
import puakma.vortex.project.ProjectManager;

/**
 * This class represents menu item in the toolbar menu which connects to some favorite application.
 * 
 * <p>TODO: add some ability to enable/disable menu on the fly if we can actually connect
 * to the application.
 *
 * @author Martin Novak
 */
public class PNFavoriteApplicationAction extends Action implements IAction, IRunnableWithProgress
{
  public static final int SHOW_NAME = 0;
  public static final int SHOW_PATH = 1;
  
  private ConnectionPrefs prefs;
  
  public PNFavoriteApplicationAction(ConnectionPrefs prefs, int type)
  {
    assert type == SHOW_NAME || type == SHOW_PATH;
    
    this.prefs = prefs;
    
    if(type == SHOW_NAME)
      setText(prefs.getName());
    else
      setText(ConnectionPrefsImpl.getFullApplicationUrl(prefs));
  }

  public void run()
  {
    IWorkbench workbench = PlatformUI.getWorkbench();
    
    try {
      workbench.getProgressService().busyCursorWhile(this);
    }
    catch(InvocationTargetException e) {
      Throwable ex = e.getTargetException();
      MessageDialog.openError(workbench.getActiveWorkbenchWindow().getShell(), "Cannot Open Application",
          "Cannot open application:\n" + ex.getLocalizedMessage());
      VortexPlugin.log(e);
    }
    catch(InterruptedException e) {
      // DO NOTHING...
    }
  }

  public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
  {
    try {
      ProjectManager.connectToApplication(prefs, monitor);
    }
    catch(InterruptedAuthException e) {
      throw new InvocationTargetException(e);
    }
    catch(IdeException e) {
      throw new InvocationTargetException(e);
    }
    finally {
      monitor.done();
    }
  }
}
