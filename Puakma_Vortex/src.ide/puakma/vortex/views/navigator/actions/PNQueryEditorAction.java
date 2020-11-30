/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Puakma Vortex 
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

import puakma.coreide.objects2.DatabaseConnection;
import puakma.vortex.WorkbenchUtils;
import puakma.vortex.views.navigator.AdapterUtils;
import puakma.vortex.views.navigator.PuakmaResourceView;

public class PNQueryEditorAction extends PNBaseAction
{
  public PNQueryEditorAction(PuakmaResourceView view)
  {
    super("OPEN_QUERY_EDITOR_ACTION", view);
    
    setText("SQL Query Editor");
  }

  public void run()
  {
    DatabaseConnection dc = getSingleSelectedDatabaseConnection();
    if(dc == null)
      return;
    
    WorkbenchUtils.openDatabaseQueryEditor(dc);
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
