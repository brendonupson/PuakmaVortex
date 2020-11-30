/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Mar 2, 2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
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

import puakma.coreide.objects2.Database;
import puakma.coreide.objects2.DatabaseConnection;
import puakma.vortex.VortexPlugin;
import puakma.vortex.WorkbenchUtils;
import puakma.vortex.views.navigator.AdapterUtils;
import puakma.vortex.views.navigator.PuakmaResourceView;

public class PNOpenDbSchemaEditor extends PNBaseAction
{
  public PNOpenDbSchemaEditor(PuakmaResourceView view)
  {
    super("OPENDBVIEw", view);
    
    setText("Database Schema Designer");
  }

  public void run()
  {
    DatabaseConnection dc = getSingleSelectedDatabaseConnection();
    if(dc == null)
      return;
    
    Database db = dc.getDatabase();
    
    try {
      if(db.isOpen() == false) {
        db.refresh();
      }
  
      WorkbenchUtils.openDatabaseSchemaEditor(dc);
    }
    catch(Exception ex) {
      VortexPlugin.log("Cannot retrieve database data", ex);
    }
  }
  
  /**
   * Gets the database connection object if there is only one database object selected.
   * @return DatabaseConnection object or null if multiple objs are selected or no database
   * connection is selected.
   */
  private DatabaseConnection getSingleSelectedDatabaseConnection()
  {
    IStructuredSelection selection = getView().getSelection();
    if(selection.size() != 1)
      return null;
    Object o = selection.getFirstElement();
    return (DatabaseConnection) AdapterUtils.getObject(o, DatabaseConnection.class);
  }

  public boolean handleKeyEvent(KeyEvent event)
  {
    return false;
  }

  public boolean qualifyForSelection()
  {
    return getSingleSelectedDatabaseConnection() != null ? true : false;    
  }

  public void selectionChanged(IWorkbenchPart part, ISelection selection)
  {
  }
}
