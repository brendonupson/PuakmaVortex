/**
 * 
 */
package puakma.vortex.editors.dbschema;

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.parts.ContentOutlinePage;
import org.eclipse.gef.ui.parts.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.IPageSite;

import puakma.vortex.editors.dbeditor.DbEditorController;
import puakma.vortex.editors.dbeditor.TreePartFactory;
import puakma.vortex.editors.dbschema.actions.DatabaseSchemaEditorContextMenuProvider;

public class DatabaseSchemaOutlinePage extends ContentOutlinePage
{
  private DbEditorController controller;

  private BaseDatabaseSchemaEditor editor;

  public DatabaseSchemaOutlinePage(BaseDatabaseSchemaEditor editor,
                                   DbEditorController controller)
  {
    super(new TreeViewer());
    this.editor = editor;
    this.controller = controller;
  }

  public void dispose()
  {
    // unhook outline viewer
    controller.getSelectionSynchronizer().removeViewer(getViewer());
    // dispose
    super.dispose();
  }
  
  public Control getControl()
  {
    return getViewer().getControl();
  }

  public void init(IPageSite pageSite)
  {
    super.init(pageSite);

    ActionRegistry registry = editor.getActionRegistry();
    IActionBars bars = pageSite.getActionBars();
    String id = ActionFactory.UNDO.getId();
    bars.setGlobalActionHandler(id, registry.getAction(id));
    id = ActionFactory.REDO.getId();
    bars.setGlobalActionHandler(id, registry.getAction(id));
    id = ActionFactory.DELETE.getId();
    bars.setGlobalActionHandler(id, registry.getAction(id));
  }

  public void createControl(Composite parent)
  {
    EditPartViewer viewer = getViewer();
    // create outline viewer page
    viewer.createControl(parent);
    // configure outline viewer
    viewer.setEditDomain(controller.getEditDomain());
    viewer.setEditPartFactory(new TreePartFactory());
    // configure & add context menu to viewer
    ContextMenuProvider cmProvider = new DatabaseSchemaEditorContextMenuProvider(viewer,
                                         editor.getActionRegistry(), controller.getCommandStack());
    viewer.setContextMenu(cmProvider);
    getSite().registerContextMenu("puakma.vortex.dbschema.outline.contextmenu",
                                  cmProvider, getSite().getSelectionProvider());
    // hook outline viewer
    controller.getSelectionSynchronizer().addViewer(viewer);
    // initialize outline viewer with model
    viewer.setContents(controller.getDatabase());
    // show outline viewer
  }
}
