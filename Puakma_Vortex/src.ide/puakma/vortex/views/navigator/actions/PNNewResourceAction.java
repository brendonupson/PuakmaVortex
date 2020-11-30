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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.ui.IWorkbenchPart;

import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.DesignObject;
import puakma.vortex.VortexPlugin;
import puakma.vortex.views.navigator.ATVApplicationNode;
import puakma.vortex.views.navigator.ATVParentNode;
import puakma.vortex.views.navigator.ApplicationTreeViewController;
import puakma.vortex.views.navigator.ContextMenuHandler;
import puakma.vortex.views.navigator.PuakmaResourceView;
import puakma.vortex.wizard.NewResourceWizard;

public class PNNewResourceAction extends PNBaseAction
{
  private int type;

  public PNNewResourceAction(PuakmaResourceView view, int type)
  {
    super("", view);
    
    assert type == NewResourceWizard.TYPE_NEW || type == NewResourceWizard.TYPE_UPLOAD :
      "Invalid resource wizard type";
    
    this.type = type;
    
    if(type == NewResourceWizard.TYPE_UPLOAD) {
      setText("Upload Resource");
      setToolTipText("Uploads New Resource");
    }
    else {
      setText("New Text Resource");
      setToolTipText("Uploads New Empty Text Resource");
    }
    setImageDescriptor(VortexPlugin.getImageDescriptor("newresource.gif"));
  }

  public void run()
  {
    Application application = ContextMenuHandler.getApplicationFromSelection(getView().getSelection());
    NewResourceWizard w = new NewResourceWizard(type);
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
    if(selection.size() != 1)
      return false;
    Application application = ContextMenuHandler.getApplicationFromSelection(selection);
    if(application == null)
      return false;
    
    Object o = selection.getFirstElement();
    if(o instanceof ATVParentNode) {
      int type = ((ATVParentNode)o).getNodeType();
      if(type == ApplicationTreeViewController.NODE_RESOURCES)
        return true;
      ATVParentNode pn = (ATVParentNode) o;
      if(pn.getParent() instanceof ATVParentNode) {
        if(((ATVParentNode)pn.getParent()).getNodeType() == ApplicationTreeViewController.NODE_RESOURCES)
          return true;
      }
    }
    else if(o instanceof ATVApplicationNode)
      return true;
    else if(o instanceof DesignObject) {
      DesignObject obj = (DesignObject) o;
      int type = obj.getDesignType();
      return type == DesignObject.TYPE_RESOURCE;
    }
    
    return false;
  }

  public void selectionChanged(IWorkbenchPart part, ISelection selection)
  {
  }

}
