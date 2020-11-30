package puakma.vortex.editors.dbeditor.layout;

import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;

import puakma.vortex.editors.dbeditor.parts.DatabaseSchemaPart;

public class GraphXYLayoutManager extends FreeformLayout
{
  private DatabaseSchemaPart dbPart;

  public GraphXYLayoutManager(DatabaseSchemaPart dbPart)
  {
    this.dbPart = dbPart;
  }
  
  public void layout(IFigure parent)
  {
    dbPart.setTableFigureBounds();
    super.layout(parent);
    dbPart.setTableModelBounds();
  }

  public Object getConstraint(IFigure child)
  {
    Object constraint = constraints.get(child);
    if(constraint != null || constraint instanceof Rectangle) {
      return constraint;
    }
    else {
      Rectangle currentBounds = child.getBounds();
      return new Rectangle(currentBounds.x, currentBounds.y, -1, -1);
    }
  }
}
