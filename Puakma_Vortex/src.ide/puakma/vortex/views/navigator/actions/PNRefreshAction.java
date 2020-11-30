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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.ui.IWorkbenchPart;

import puakma.coreide.PuakmaCoreException;
import puakma.coreide.RefreshEventInfoImpl;
import puakma.coreide.objects2.Application;
import puakma.vortex.IdeException;
import puakma.vortex.VortexPlugin;
import puakma.vortex.project.ProjectManager;
import puakma.vortex.project.PuakmaProject2;
import puakma.vortex.views.navigator.ATVApplicationNode;
import puakma.vortex.views.navigator.ContextMenuHandler;
import puakma.vortex.views.navigator.PuakmaResourceView;

public class PNRefreshAction extends PNBaseAction
{
  public PNRefreshAction(PuakmaResourceView view)
  {
    super("", view);
    setImageDescriptor(VortexPlugin.getImageDescriptor("refresh.gif"));
    setText("Refresh");
    setToolTipText("Refreshes application");
  }

  public void run()
  {
    final List<Object> apps = new ArrayList<Object>();
    IStructuredSelection selection = getView().getSelection();
    Iterator it = selection.iterator();
    while(it.hasNext()) {
      Object o = it.next();
      if(o instanceof ATVApplicationNode) {
        apps.add(o);
      }
    }

    IRunnableWithProgress op = new IRunnableWithProgress() {
        public void run(IProgressMonitor monitor)
            throws InvocationTargetException, InterruptedException
      {
        Iterator<Object> it = apps.iterator();
        while(it.hasNext()) {
          Application app = null;
          try {
            ATVApplicationNode node = (ATVApplicationNode) it.next();
            app = node.application;
            PuakmaProject2 project = ProjectManager.getProject(app);
            
            RefreshEventInfoImpl info = app.refresh();
            project.refresh(info, monitor);
          }
          catch(PuakmaCoreException e) {
            VortexPlugin.log(e);
          }
          catch(IdeException e) {
            VortexPlugin.log(e);
          }
        }
      }
    };
    try {
      new ProgressMonitorDialog(getShell()).run(true, true, op);
    }
    catch(InvocationTargetException e) {
      // handle exception
    }
    catch(InterruptedException e) {
      // handle cancelation
    }
  }

  public boolean handleKeyEvent(KeyEvent event)
  {
    return false;
  }

  public boolean qualifyForSelection()
  {
    return ContextMenuHandler.selectionAreonlyApplications(getView().getSelection());
  }

  public void selectionChanged(IWorkbenchPart part, ISelection selection)
  {
  }
}
