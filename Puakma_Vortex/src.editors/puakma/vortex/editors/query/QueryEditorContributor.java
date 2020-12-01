/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Shining day in november 2005
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

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.EditorActionBarContributor;

/**
 * This class contributes to the menu for query editor.
 *
 * @author Martin Novak
 */
public class QueryEditorContributor extends EditorActionBarContributor
{
  private static final String RUN = "RUN";
  private IEditorPart activePart;
  private QEExecuteAction executeQueryAction;

  // TODO: steal some stuff there:
  //org.eclipse.ui.texteditor.BasicTextEditorActionContributor;
  
  public QueryEditorContributor()
  {
    super();
    
    executeQueryAction = new QEExecuteAction();
//    int kRet = Action.findKeyCode("RETURN");
//    executeQueryAction.setAccelerator(kRet | SWT.MOD1);
  }
  
  public void setActiveEditor(IEditorPart part)
  {
    super.setActiveEditor(part);
    if(activePart == part || part instanceof QueryEditor == false)
      return;

    activePart = part;
    QueryEditor qe = (QueryEditor) activePart;

    executeQueryAction.setEditor(qe);
    
    IActionBars bars = getActionBars();
    if(bars != null) {
//      "puakma.vortex.queryEditor.execute";
      
      bars.updateActionBars();
    }
  }

  public void contributeToMenu(IMenuManager manager)
  {
//    IMenuManager menu = new MenuManager("&Database");
//    manager.prependToGroup(IWorkbenchActionConstants.MB_ADDITIONS, menu);
//    
//    menu.add(new Separator(RUN));
//    
//    menu.appendToGroup(RUN, executeQueryAction);
  }
}
