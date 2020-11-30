package puakma.vortex.controls;

import java.beans.PropertyChangeEvent;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;

import puakma.coreide.objects2.Table;

/**
 * Helper class helping to construct viewer for table columns.
 *
 * @author Martin Novak &lt;mn@puakma.net&gt;
 */
public class TableColumnsViewer
{
  private TableViewer viewer;
  
  private Table table;
  
  /**
   * If true then the table and columns has been hooked.
   */
  private boolean modelHooked;
  
  public TableColumnsViewer()
  {
    super();
  }

  public void propertyChange(PropertyChangeEvent evt)
  {
    String name = evt.getPropertyName();
    if(Table.PROP_COLUMN_ADD == name) {
      
    }
    else if(Table.PROP_COLUMN_REMOVE == name) {
      
    }
    else if(Table.PROP_COLUMN_MOVE == name) {
      
    }
  }
  
  public void createTableViewer(Composite parent, int style)
  {
    if(viewer != null)
      throw new IllegalStateException("Viewer has already been created");
    
    viewer = new TableViewer(parent, style);
    
    // AND NOW HOOKUP THE TABLE INPUT
    hookInput();
  }
  
  /**
   * Hooks up to the model.
   */
  private void hookInput()
  {
    if(modelHooked)
      throw new IllegalStateException("The model for viewer has already been set.");
    
    viewer.setInput(table);
  }
  
  public void setInput(Table table)
  {
    if(this.table != null)
      unhookInput();
    
    this.table = table;
    
    if(viewer != null)
      hookInput();
  }

  /**
   * Removes all hooks to the model.
   */
  private void unhookInput()
  {
    table = null;
    viewer.setInput(null);
    modelHooked = false;
  }

  public TableViewer getTableViewer()
  {
    return viewer;
  }
  
  public Table getTable()
  {
    return table;
  }

  public org.eclipse.swt.widgets.Table getTableControl()
  {
    return viewer.getTable();
  }
}
