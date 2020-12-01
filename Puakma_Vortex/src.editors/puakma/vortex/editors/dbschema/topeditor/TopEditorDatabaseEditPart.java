package puakma.vortex.editors.dbschema.topeditor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractEditPart;

import puakma.coreide.objects2.Database;
import puakma.coreide.objects2.Table;

public class TopEditorDatabaseEditPart extends AbstractEditPart
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

  protected List<Table> getModelChildren()
  {
    Database db = (Database) getModel();
    List<Table> l = new ArrayList<Table>();
    Table[] tables = db.listTables();
    for(int i = 0; i < tables.length; ++i)
      l.add(tables[i]);
    return l;
  }
  
  protected void refreshChildren() {
    super.refreshChildren();
  }
}
