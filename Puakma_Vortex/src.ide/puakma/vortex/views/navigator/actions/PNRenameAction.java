/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Nov 1, 2005
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
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;

import puakma.coreide.objects2.DesignObject;
import puakma.vortex.scripts.ui.RenameDesignObjectUIScript;
import puakma.vortex.views.navigator.AdapterUtils;
import puakma.vortex.views.navigator.PuakmaResourceView;

/**
 * @author Martin Novak
 */
public class PNRenameAction extends PNBaseAction
{
  public PNRenameAction(PuakmaResourceView view)
  {
    super(ActionFactory.RENAME.getId(), view);

    setText("Rename...");
  }

  public void run()
  {
    final DesignObject dob = getSelectedDesignObject();
    if(dob == null)
      return;
    
    RenameDesignObjectUIScript script = new RenameDesignObjectUIScript(dob);
    script.run();
    return;
    
//    InputDialog dlg = new InputDialog(getShell(), "Rename Design Object",
//        "Rename design object " + dob.getName(), dob.getName(),
//        new IInputValidator() {
//          public String isValid(String newText)
//          {
//            if(dob.getName().equals(newText))
//              return "Type another name";
//            Application app = dob.getApplication();
//            if(app.getDesignObject(newText) != null)
//              return "Design object " + newText + " already exists";
//            return null;
//          }
//        });
//    if(dlg.open() == Window.OK) {
//      DesignObject wCopy = dob.makeWorkingCopy();
//      wCopy.setName(dlg.getValue());
//      try {
//        wCopy.commit();
//      }
//      catch(PuakmaCoreException e) {
//        VortexPlugin.log(e);
//        MessageDialog.openError(getShell(), "Cannot Rename Design Object",
//            "Cannot rename design object " + dob.getName() + " to "
//                + dlg.getValue() + "\nReason:\n" + e.getLocalizedMessage());
//      }
//    }
  }

  public boolean handleKeyEvent(KeyEvent event)
  {
    return false;
  }

  public boolean qualifyForSelection()
  {
    return getSelectedDesignObject() != null;
  }

  private DesignObject getSelectedDesignObject()
  {
    IStructuredSelection selection = getView().getSelection();
    if(selection.size() == 1) {
      Object o = selection.getFirstElement();
      DesignObject dob = (DesignObject) AdapterUtils.getObject(o,
          DesignObject.class);
      if(dob != null) {
        return dob;
      }
    }
    return null;
  }

  public void selectionChanged(IWorkbenchPart part, ISelection selection)
  {
  }
}
