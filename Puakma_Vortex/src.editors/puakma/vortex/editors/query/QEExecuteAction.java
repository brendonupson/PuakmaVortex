/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Nov 21, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.query;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import puakma.vortex.VortexPlugin;
import puakma.vortex.WorkbenchUtils;

public class QEExecuteAction extends Action implements IEditorActionDelegate
{
  private QueryEditor editor;

  public QEExecuteAction()
  {
    super();

    setText("Execute query");
  }

  public void setEditor(QueryEditor editor)
  {
    this.editor = editor;
  }

  public void run()
  {
    QueryEditor editor = (QueryEditor) WorkbenchUtils.getActivePage().getActiveEditor();
    if(editor == null) {
      VortexPlugin.log("Editor is not set in database query action");
      return;
    }

    editor.executeQuery();
  }

  public void setActiveEditor(IAction action, IEditorPart targetEditor)
  {
    
  }

  public void run(IAction action)
  {
    run();
  }

  public void selectionChanged(IAction action, ISelection selection)
  {
    
  }

}
