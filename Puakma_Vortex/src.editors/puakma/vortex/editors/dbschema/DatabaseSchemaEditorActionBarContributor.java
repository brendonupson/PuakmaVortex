/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 11, 2006
 * 
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.dbschema;

import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.ui.actions.ActionBarContributor;
import org.eclipse.gef.ui.actions.DeleteRetargetAction;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.RedoRetargetAction;
import org.eclipse.gef.ui.actions.UndoRetargetAction;
import org.eclipse.gef.ui.actions.ZoomComboContributionItem;
import org.eclipse.gef.ui.actions.ZoomInRetargetAction;
import org.eclipse.gef.ui.actions.ZoomOutRetargetAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.RetargetAction;

public class DatabaseSchemaEditorActionBarContributor extends ActionBarContributor
{
  /**
   * Create actions managed by this contributor.
   * @see org.eclipse.gef.ui.actions.ActionBarContributor#buildActions()
   */
  protected void buildActions()
  {
    addRetargetAction(new UndoRetargetAction());
    addRetargetAction(new RedoRetargetAction());
    addRetargetAction(new DeleteRetargetAction());
    
    addRetargetAction(new ZoomInRetargetAction());
    addRetargetAction(new ZoomOutRetargetAction());
    
    addRetargetAction(new RetargetAction(GEFActionConstants.TOGGLE_SNAP_TO_GEOMETRY,
        "Snap To Grid", IAction.AS_CHECK_BOX));

    addRetargetAction(new RetargetAction(GEFActionConstants.TOGGLE_GRID_VISIBILITY,
        "Toogle Grid", IAction.AS_CHECK_BOX));

  }

  /**
   * Add actions to the given toolbar.
   * @see org.eclipse.ui.part.EditorActionBarContributor#contributeToToolBar(org.eclipse.jface.action.IToolBarManager)
   */
  public void contributeToToolBar(IToolBarManager tbm)
  {
    super.contributeToToolBar(tbm);
    
    tbm.add(getAction(ActionFactory.UNDO.getId()));
    tbm.add(getAction(ActionFactory.REDO.getId()));
    
    tbm.add(new Separator());
    tbm.add(getAction(GEFActionConstants.ZOOM_IN));
    String[] zoomStrings = new String[] { ZoomManager.FIT_ALL, ZoomManager.FIT_HEIGHT, ZoomManager.FIT_WIDTH };
    tbm.add(new ZoomComboContributionItem(getPage(), zoomStrings));
    tbm.add(getAction(GEFActionConstants.ZOOM_OUT));
  }

  protected void declareGlobalActionKeys()
  {
    addGlobalActionKey(ActionFactory.PRINT.getId());
    addGlobalActionKey(ActionFactory.SELECT_ALL.getId());
    addGlobalActionKey(ActionFactory.PASTE.getId());
    addGlobalActionKey(ActionFactory.DELETE.getId());
  }
  
  public void contributeToMenu(IMenuManager menubar)
  {
    super.contributeToMenu(menubar);
    
    MenuManager viewMenu = new MenuManager("View");
    
    viewMenu.add(getAction(GEFActionConstants.ZOOM_IN));
    viewMenu.add(getAction(GEFActionConstants.ZOOM_OUT));
    
    viewMenu.add(new Separator());
    
    viewMenu.add(getAction(GEFActionConstants.TOGGLE_GRID_VISIBILITY));
    viewMenu.add(getAction(GEFActionConstants.TOGGLE_SNAP_TO_GEOMETRY));
    
    menubar.insertAfter(IWorkbenchActionConstants.M_EDIT, viewMenu);
  }
}
