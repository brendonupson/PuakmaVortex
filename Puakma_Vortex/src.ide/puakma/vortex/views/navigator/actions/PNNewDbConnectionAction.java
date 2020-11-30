/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Aug 10, 2005
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

import java.util.Iterator;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.ui.IWorkbenchPart;

import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.DatabaseConnection;
import puakma.vortex.VortexPlugin;
import puakma.vortex.views.navigator.ATVApplicationNode;
import puakma.vortex.views.navigator.ATVDbConnectionNode;
import puakma.vortex.views.navigator.ATVParentNode;
import puakma.vortex.views.navigator.ApplicationTreeViewController;
import puakma.vortex.views.navigator.ContextMenuHandler;
import puakma.vortex.views.navigator.PuakmaResourceView;
import puakma.vortex.wizard.NewDatabaseConnectionWizard;

public class PNNewDbConnectionAction extends PNBaseAction
{

  public PNNewDbConnectionAction(PuakmaResourceView view)
  {
    super("", view);
    setText("New Database Connection");
    setToolTipText("Creates New Database Connection");
    setImageDescriptor(VortexPlugin.getImageDescriptor("database.gif"));
  }

  public void run()
  {
    Application application = ContextMenuHandler.getApplicationFromSelection(getView().getSelection());
    NewDatabaseConnectionWizard w = new NewDatabaseConnectionWizard();
    w.init(application);
    WizardDialog dlg = new WizardDialog(getShell(), w);
    dlg.open();
  }

  public boolean handleKeyEvent(KeyEvent event)
  {
    return false;
  }

  public boolean qualifyForSelection()
  {
    IStructuredSelection selection = getView().getSelection();
    Application application = ContextMenuHandler.getApplicationFromSelection(selection);
    if(application == null)
      return false;
    
    Iterator it = selection.iterator();
    while(it.hasNext()) {
      Object o = it.next();
      if(o instanceof DatabaseConnection)
        return true;
      else if(o instanceof ATVDbConnectionNode)
        return true;
      if(o instanceof ATVParentNode && ((ATVParentNode)o).getNodeType() == ApplicationTreeViewController.NODE_DATABASES)
        return true;
      else if(o instanceof ATVApplicationNode)
        return true;
      else if(o instanceof ATVDbConnectionNode) {
        return true;
      }
    }
    
    return false;
  }

  public void selectionChanged(IWorkbenchPart part, ISelection selection)
  {
  }
}
