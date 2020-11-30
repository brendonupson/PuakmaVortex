/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Aug 7, 2005
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.ApplicationObject;
import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.ServerObject;
import puakma.vortex.VortexPlugin;
import puakma.vortex.project.ProjectUtils;
import puakma.vortex.views.navigator.ATVApplicationNode;
import puakma.vortex.views.navigator.AdapterUtils;
import puakma.vortex.views.navigator.PuakmaResourceView;

public class PNRemoveAction extends PNBaseAction
{
  public PNRemoveAction(PuakmaResourceView view)
  {
    super(ActionFactory.DELETE.getId(), view);
    ImageDescriptor image = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE);
    setImageDescriptor(image);
    setText("Delete");
    setToolTipText("Deletes item(s) from application");
  }

  public void run()
  {
    IStructuredSelection selection = getView().getSelection();
    // ASK USER TO DELETE ALL ITEMS...
    String question = "";
    List<ServerObject> l = collectObjects(selection);
    Iterator<ServerObject> it = l.iterator();
    while(it.hasNext()) {
      Object o = it.next();
      if(o instanceof Application) {
        Application app = (Application) o;
        question += app.getFQName() + "\n";
      }
      else if(o instanceof ApplicationObject) {
        ApplicationObject obj = (ApplicationObject) o;
        Application app = obj.getApplication();
        question += app.getFQName() + "/" + obj.getName() + "\n";
      }
      else {
        MessageDialog.openError(getView().getSite().getShell(), "Internal Error",
            "Internal Error - Unknown Object To Delete");
      }
    }
    if(MessageDialog.openConfirm(getView().getSite().getShell(), "Confirm Delete",
                                 "Do You Want To Delete:\n" + question)) {
      // AND DO IT!!!
      it = l.iterator();
      while(it.hasNext()) {
        Object o = it.next();
        if(o instanceof Application) {
          Application app = (Application) o;
          try {
            app.close();
            app.remove();
          }
          catch(Exception e) {
            VortexPlugin.log(e);
            MessageDialog.openError(getView().getSite().getShell(), "Cannot Delete Application",
                "Cannot Delete Application.\nReason:\n"+e.getLocalizedMessage());
          }
        }
        else if(o instanceof ApplicationObject) {
          ApplicationObject aobj = (ApplicationObject) o;
          try {
            aobj.remove();
          }
          catch(Exception e) {
            VortexPlugin.log(e);
            MessageDialog.openError(getView().getSite().getShell(), "Cannot Delete Object",
                "Cannot Delete Object.\nReason:\n"+e.getLocalizedMessage());
          }
        }
      }
    }
  }

  public boolean handleKeyEvent(KeyEvent event)
  {
    return false;
  }

  public void selectionChanged(IWorkbenchPart part, ISelection sel)
  {
    setEnabled(qualifyForSelection());
  }
  
  
  public boolean qualifyForSelection()
  {
    IStructuredSelection selection = getView().getSelection();
    List<ServerObject> l = collectObjects(selection);
    return l != null && l.size() > 0;
  }

  /**
   * Gets the list of objects to remove.
   * @param selection is the selection from which we want to choose
   * @return null if the selection doesn't qualify for delete or size of selection
   *         is zero otherwise list with Application objects or ApplicationObject
   *         objects
   */
  public List<ServerObject> collectObjects(IStructuredSelection selection)
  {
    int delFlag = -1;
    List<ServerObject> l = new ArrayList<ServerObject>();
    Iterator it = selection.iterator();
    while(it.hasNext()) {
      Object o = it.next();
      DesignObject obj = (DesignObject) AdapterUtils.getObject(o, DesignObject.class);
      if(obj != null) {
        l.add(obj);
        continue;
      }
      IFile file = (IFile) AdapterUtils.getObject(o, IFile.class);
      if(file != null) {
        DesignObject dobj = ProjectUtils.getDesignObject(file);
        if(dobj == null)
          return null;
        if(delFlag != 1 && delFlag != -1)
          return null;
        l.add(dobj);
        delFlag = 1;
      }
      
      if(o instanceof ApplicationObject) {
        ApplicationObject aobj = (ApplicationObject) o;
        l.add(aobj);

        if(delFlag != 1 && delFlag != -1)
          return null;
        delFlag = 1;
      }
      if(o instanceof ATVApplicationNode) {
        ATVApplicationNode appNode = (ATVApplicationNode) o;
        Application app = appNode.getApplication();
        l.add(app);
        
        if(delFlag != 2 && delFlag != -1)
          return null;
        delFlag = 2;
      }
    }

    if(l.size() == 0)
      return null;
    return l;
  }
}
