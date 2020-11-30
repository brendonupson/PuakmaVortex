/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    01/05/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import puakma.coreide.ConnectionPrefs;
import puakma.coreide.VortexAuthentificationException;
import puakma.coreide.objects2.AuthenticationDialog;
import puakma.utils.lang.StringUtil;
import puakma.vortex.dialogs.DualInputDialog;

public class AuthenticationDialogImpl implements AuthenticationDialog, IInputValidator
{
  public AuthenticationDialogImpl() {}
  public boolean open(VortexAuthentificationException ex, final ConnectionPrefs prefs)
  {
    final boolean[] ret = new boolean[1];
    
    Display.getDefault().syncExec(new Runnable() {
      public void run()
      {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        Shell shell = window != null ? window.getShell() : null;
        String title = "Authentication failed";
        String msg = "Authentication failed - please provide new user or password";
        DualInputDialog dlg = new DualInputDialog(shell, title, msg, prefs.getUser(),
                                                  prefs.getPwd(), AuthenticationDialogImpl.this,
                                                  null);
        dlg.setPassword(true);
        
        if(dlg.open() == Window.OK) {
          if(StringUtil.compareStrings(prefs.getUser(), dlg.getValue()) == false ||
             StringUtil.compareStrings(prefs.getPwd(), dlg.getValue2()) == false) {
            prefs.setUser(dlg.getValue());
            prefs.setPwd(dlg.getValue2());
            prefs.setModified(true);
          }
          ret[0] = true;
        }
        
        ret[0] = false;
      }
    
    });
    return ret[0];
  }

  public String isValid(String newText)
  {
    if(newText.length() == 0)
      return "Enter user name";
    return null;
  }
}
