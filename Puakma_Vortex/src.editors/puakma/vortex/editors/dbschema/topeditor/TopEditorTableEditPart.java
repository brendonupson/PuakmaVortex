package puakma.vortex.editors.dbschema.topeditor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractEditPart;

import puakma.coreide.objects2.Table;
import puakma.coreide.objects2.TableColumn;

/**
 * This is just empty silly thing for getting GEF be happy. It really does
 * nothing. All the control is being done via {@link TopEditor}
 * 
 * @author Martin Novak &lt;mn@puakma.net&gt;
 */
public class TopEditorTableEditPart extends AbstractEditPart
{
  protected void addChildVisual(EditPart child, int index)
  {

  }

  protected void createEditPolicies()
  {

  }

  protected void removeChildVisual(EditPart child)
  {

  }

  public DragTracker getDragTracker(Request request)
  {
    return null;
  }

  public List getChildren()
  {
    // TODO Auto-generated method stub
    return super.getChildren();
  }

  protected List<TableColumn> getModelChildren()
  {
    Table t = (Table) getModel();
    TableColumn[] cols = t.listColumns();
    List<TableColumn> l = new ArrayList<TableColumn>();
    for(int i = 0; i < cols.length; ++i)
      l.add(cols[i]);
    return l;
  }
  
  
}
