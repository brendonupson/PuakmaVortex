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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;

import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.JavaObject;
import puakma.vortex.dialogs.scheduledAction.ScheduledActionPropertiesDialog;
import puakma.vortex.views.navigator.AdapterUtils;
import puakma.vortex.views.navigator.PuakmaResourceView;

public class PNScheduleActionAction extends PNBaseAction implements ISelectionListener
{
  public PNScheduleActionAction(PuakmaResourceView view)
  {
    super(ActionFactory.PROPERTIES.getId(), view);
    setText("Schedule Action");
  }

  public void run()
  {
    IStructuredSelection selection = getView().getSelection();
    if(selection.size() != 1)
      return;
    Object o = selection.getFirstElement();
    final JavaObject jo = (JavaObject) AdapterUtils.getObject(o, JavaObject.class);
    if(jo != null && jo.getDesignType() == DesignObject.TYPE_SCHEDULEDACTION) {
      final Display d = Display.getDefault();
      d.asyncExec(new Runnable() {
        public void run() {
          Shell shell = d.getActiveShell();
          ScheduledActionPropertiesDialog dlg = new ScheduledActionPropertiesDialog(shell, jo);
          dlg.open();
        }
      });
    }
  }

  public boolean handleKeyEvent(KeyEvent event)
  {
    if(event.keyCode == '\r' && (event.stateMask & SWT.MOD1) == SWT.MOD1)
      return true;
    return false;
  }

  public void selectionChanged(IWorkbenchPart part, ISelection iselection)
  {
    setEnabled(qualifyForSelection());
  }

  public boolean qualifyForSelection()
  {
    IStructuredSelection selection = getView().getSelection();
    if(selection.size() != 1)
      return false;
    Object o = selection.getFirstElement();
    DesignObject dob = (DesignObject) AdapterUtils.getObject(o, DesignObject.class);
    if(dob != null && dob.getDesignType() == DesignObject.TYPE_SCHEDULEDACTION)
      return true;
    return false;
  }
  
  public boolean isEnabled()
  {
    return qualifyForSelection();
  }
}
