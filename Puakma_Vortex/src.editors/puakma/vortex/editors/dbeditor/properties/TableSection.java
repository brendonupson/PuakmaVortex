package puakma.vortex.editors.dbeditor.properties;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

import puakma.coreide.objects2.Table;
import puakma.coreide.objects2.TableColumn;
import puakma.vortex.editors.dbeditor.DbEditorController;
import puakma.vortex.editors.dbeditor.commands.PropertiesChangeCommand;
import puakma.vortex.editors.dbschema.DatabaseSchemaEditor;
import puakma.vortex.properties.ControlChangeHelper;
import puakma.vortex.properties.ControlChangeListener;
import puakma.vortex.swt.DialogBuilder2;

/**
 * This section implements properties viewer for {@link Table} objects.
 * 
 * @author Martin Novak &lt;mn@puakma.net&gt;
 */
public class TableSection extends AbstractPropertySection implements ControlChangeListener, PropertyChangeListener
{
  private Text nameEdit;
  
  private Table table;

  private Text descEdit;
  
  /**
   * A helper to listen for events that indicate that a text field has been
   * changed.
   */
  private ControlChangeHelper listener = new ControlChangeHelper(this);
  
  public void controlChanged(Control control)
  {
    IWorkbench wBench = PlatformUI.getWorkbench();
    IEditorPart part = wBench.getActiveWorkbenchWindow().getPages()[0].getActiveEditor();
    if(part instanceof DatabaseSchemaEditor) {
      PropertiesChangeCommand cmd = null;
      Class<TableColumn> clz = TableColumn.class;
      if(control == nameEdit && table.getName().equals(nameEdit.getText()) == false) {
        cmd = new PropertiesChangeCommand(clz, Table.PROP_NAME, table, nameEdit.getText());
      }
      else if(control == descEdit && table.getDescription().equals(descEdit.getText()) == false)
        cmd = new PropertiesChangeCommand(clz, Table.PROP_DESCRIPTION, table, descEdit.getText());
      
      if(cmd != null) {
        DatabaseSchemaEditor editor = (DatabaseSchemaEditor) part;
        DbEditorController controller = editor.getGraphicalViewerController();
        controller.getCommandStack().execute(cmd);
      }
    }
    
//    String newName = nameEdit.getText();
//    table.setName(newName);
//    String newDesc = descEdit.getText();
//    table.setDescription(newDesc);
  }

  public void aboutToBeHidden()
  {
    super.aboutToBeHidden();
    
    table.removeListener(this);
  }

  public void aboutToBeShown()
  {
    super.aboutToBeShown();
    
    table.addListener(this);
  }

  public void createControls(Composite parent,
                             TabbedPropertySheetPage aTabbedPropertySheetPage)
  {
    super.createControls(parent, aTabbedPropertySheetPage);
    Composite composite = getWidgetFactory().createFlatFormComposite(parent);
    composite.setLayout(new org.eclipse.swt.layout.GridLayout(2, false));
    
    DialogBuilder2 builder = new DialogBuilder2(composite, 1, true, getWidgetFactory());
    
    //builder.createFormsLFComposite("", false, 2);
    
    nameEdit = builder.createEditRow("Name");
    descEdit = builder.createMemoRow("Description", 4);
    
    //builder.closeComposite();
    builder.finishBuilder();
    
    listener.startListeningTo(nameEdit);
    listener.startListeningTo(descEdit);
  }

  public void refresh()
  {
    super.refresh();
    
    listener.startNonUserChange();
    
    try {
      nameEdit.setText(table.getName());
      descEdit.setText(table.getDescription());
    }
    finally {
      listener.finishNonUserChange();
    }
  }

  public void setInput(IWorkbenchPart part, ISelection selection)
  {
    super.setInput(part, selection);
    IStructuredSelection sel = (IStructuredSelection) selection;
    Object item = sel.getFirstElement();
    this.table = null;
    
    if(item instanceof Table)
      this.table = (Table) item;
    else if(item instanceof IAdaptable)
      this.table = (Table) ((IAdaptable) item).getAdapter(Table.class);
    
    if(this.table == null)
      throw new IllegalArgumentException("Cannot pass object which is not Table to this properties view");
  }

  public void propertyChange(PropertyChangeEvent evt)
  {
    String prop = evt.getPropertyName();
    if(Table.PROP_NAME == prop || Table.PROP_DESCRIPTION == prop)
      refresh();
  }

  public void dispose()
  {
    if(table != null)
      table.removeListener(this);
    
    super.dispose();
  }
}
