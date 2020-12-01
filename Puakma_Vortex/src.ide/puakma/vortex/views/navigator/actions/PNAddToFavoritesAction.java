/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Dec 19, 2005
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

import java.io.IOException;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.ui.IWorkbenchPart;

import puakma.coreide.ConfigurationManager;
import puakma.coreide.ConnectionPrefs;
import puakma.coreide.ConnectionPrefsImpl;
import puakma.coreide.objects2.Application;
import puakma.vortex.VortexPlugin;
import puakma.vortex.views.navigator.ATVApplicationNode;
import puakma.vortex.views.navigator.PuakmaResourceView;

public class PNAddToFavoritesAction extends PNBaseAction
{
  public PNAddToFavoritesAction(PuakmaResourceView view)
  {
    super("AddToFavoritesItem", view);
    
    setText("Add To Favorites");
  }

  public void run()
  {
    IStructuredSelection selection = getView().getSelection();
    if(selection.size() != 1)
      return;
    if(selection.getFirstElement() instanceof ATVApplicationNode == false)
      return;
    
    ATVApplicationNode node = (ATVApplicationNode) selection.getFirstElement();
    Application app = node.getApplication();
    ConnectionPrefs prefs = (ConnectionPrefs) app.getConnectionPrefs();
    ConfigurationManager manager = VortexPlugin.getDefault().getFavoritesAppsManager();
    prefs.setName(ConnectionPrefsImpl.getFullApplicationUrl(prefs));
    try {
      manager.save(prefs);
    }
    catch(IOException e) {
      VortexPlugin.log(e);
    }
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
    return (selection.getFirstElement() instanceof ATVApplicationNode) ? true : false;
  }

  public void selectionChanged(IWorkbenchPart part, ISelection selection)
  {
    
  }
}
