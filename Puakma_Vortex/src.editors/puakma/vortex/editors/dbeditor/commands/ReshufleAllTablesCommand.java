package puakma.vortex.editors.dbeditor.commands;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.commands.Command;

import puakma.vortex.editors.dbeditor.layout.GraphLayout;
import puakma.vortex.editors.dbeditor.layout.GraphXYLayoutManager;
import puakma.vortex.editors.dbeditor.parts.DatabaseSchemaPart;

/**
 * This command reshufles all tables, so they have kind of automatic layout.
 * 
 * @author Martin Novak &lt;mn@puakma.net&gt;
 */
public class ReshufleAllTablesCommand extends Command
{
  private DatabaseSchemaPart part;

  public ReshufleAllTablesCommand(DatabaseSchemaPart part)
  {
    this.part = part;
  }

  public void execute()
  {
    redo();
  }

  public void redo()
  {
    IFigure figure = part.getFigure();
    GraphXYLayoutManager xyLayout = (GraphXYLayoutManager) figure.getLayoutManager();
    GraphLayout graphLayout = new GraphLayout(part);

    part.setTableFigureBounds();
    //setLayoutManagerForFigure(container, activeLayout);
    //activeLayout.layout(container);
    
    
    // REMEMBER ALL TABLES POSITIONS
    rememberOldTablesPositions();

    // CHANGE MANAGER TO THE GRAPH LAYOUT MANAGER
    figure.setLayoutManager(graphLayout);

    // LAYOUT
    graphLayout.layout(figure);
    
    // SET THE MANAGER TO XY MANAGER BACK
    figure.setLayoutManager(xyLayout);
    
    // SAVE THE BOUNDS TO THE MODEL
    part.setTableModelBounds();
    
    // REMEMBER THE NEW POSITIONS
    rememberNewTablesPositions();
  }

  private void rememberOldTablesPositions()
  {
    
  }
  
  private void rememberNewTablesPositions()
  {
    
  }

  public void undo()
  {
    // TODO Auto-generated method stub
    super.undo();
  }

}
