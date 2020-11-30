package puakma.vortex.editors.dbschema.topeditor;

import java.util.List;

import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gef.editparts.AbstractEditPart;

public class TopEditorRootEditPart extends AbstractEditPart implements RootEditPart
{

  private EditPartViewer viewer;
  private EditPart contents;

  protected void addChildVisual(EditPart child, int index)
  {

  }

  protected void createEditPolicies()
  {

  }

  protected void removeChildVisual(EditPart child)
  {

  }
  
  protected List getModelChildren()
  {
    return super.getModelChildren();
  }

  public DragTracker getDragTracker(Request request)
  {
    return null;
  }

  public EditPart getContents()
  {
    return contents;
  }

  public void setContents(EditPart editpart)
  {
    if (contents == editpart)
        return;
    if (contents != null)
        removeChild(contents);
    contents = editpart;
    if (contents != null)
        addChild(contents, 0);
  }

  public void setViewer(EditPartViewer viewer)
  {
    this.viewer = viewer;
  }

  public EditPartViewer getViewer()
  {
    return this.viewer;
  }
  
  public Command getCommand(Request request)
  {
    return UnexecutableCommand.INSTANCE;
  }
}
