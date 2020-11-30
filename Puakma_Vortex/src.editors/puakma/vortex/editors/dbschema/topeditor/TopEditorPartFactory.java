package puakma.vortex.editors.dbschema.topeditor;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

import puakma.coreide.designer.ApplicationStructureBean.Database;
import puakma.coreide.objects2.Table;
import puakma.coreide.objects2.TableColumn;

public class TopEditorPartFactory implements EditPartFactory
{
  public EditPart createEditPart(EditPart context, Object model)
  {
    EditPart part = null;
    
    if(model instanceof Database) {
      part = new TopEditorDatabaseEditPart();
    }
    else if(model instanceof Table) {
      part = new TopEditorTableEditPart();
    }
    else if(model instanceof TableColumn) {
      part = new TopEditorTableEditPart();
    }
    
    if(part != null)
      part.setModel(model);
    
    return part;
  }
}
