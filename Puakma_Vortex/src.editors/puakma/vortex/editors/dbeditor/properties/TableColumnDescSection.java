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

import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.TableColumn;
import puakma.vortex.VortexPlugin;
import puakma.vortex.editors.dbeditor.DbEditorController;
import puakma.vortex.editors.dbeditor.commands.PropertiesChangeCommand;
import puakma.vortex.editors.dbschema.DatabaseSchemaEditor;
import puakma.vortex.properties.ControlChangeHelper;
import puakma.vortex.properties.ControlChangeListener;
import puakma.vortex.swt.DialogBuilder2;

public class TableColumnDescSection  extends AbstractPropertySection implements ControlChangeListener,
                                                                                PropertyChangeListener
{
  private TableColumn column;
  
  /**
   * A helper to listen for events that indicate that a text field has been
   * changed.
   */
  private ControlChangeHelper listener = new ControlChangeHelper(this);
  
  private Composite mainComposite;

  private Text descEdit;
  
  public void controlChanged(Control control)
  {
    IWorkbench wBench = PlatformUI.getWorkbench();
    IEditorPart part = wBench.getActiveWorkbenchWindow().getPages()[0].getActiveEditor();
    if(part instanceof DatabaseSchemaEditor) {
      PropertiesChangeCommand cmd = null;
      Class<TableColumn> clz = TableColumn.class;
      if(control == descEdit && column.getDescription().equals(descEdit.getText()) == false)
        cmd = new PropertiesChangeCommand(clz, TableColumn.PROP_DESCRIPTION, column, descEdit.getText());
      
      if(cmd != null) {
        DatabaseSchemaEditor editor = (DatabaseSchemaEditor) part;
        DbEditorController controller = editor.getGraphicalViewerController();
        controller.getCommandStack().execute(cmd);
      }
    }
//    String oldDesc = column.getDescription();
//    String newDesc = descEdit.getText();
//    if(oldDesc.equals(newDesc) == false) {
//      if(column.isWorkingCopy() || column.isNew()) {
//        column.setDescription(newDesc);
//      }
//      else {
//        TableColumn col = column.makeWorkingCopy();
//        col.setDescription(newDesc);
//        try {
//          col.commit();
//        }
//        catch(Exception e) {
//          VortexPlugin.log(e);
//        }
//      }
//    }
  }
  
  public void aboutToBeHidden()
  {
    super.aboutToBeHidden();
    
    // TODO: UPDATE ALL PROPERTIES IF THE TABLE IS NOT WORKING COPY
    column.removeListener(this);
  }

  public void aboutToBeShown()
  {
    super.aboutToBeShown();
    
    column.addListener(this);
  }

  public void createControls(Composite parent,
                             TabbedPropertySheetPage aTabbedPropertySheetPage)
  {
    super.createControls(parent, aTabbedPropertySheetPage);
    mainComposite = getWidgetFactory().createFlatFormComposite(parent);
    
    mainComposite.setLayout(new org.eclipse.swt.layout.GridLayout(2, false));
    
    DialogBuilder2 builder = new DialogBuilder2(mainComposite, 2, true, getWidgetFactory());
    
    builder.createSection("Description", null, 1);
    descEdit = builder.createMemoRow("Description", 5);
    builder.closeSection();
    
    builder.finishBuilder();
    
    listener.startListeningTo(descEdit);
  }

  public void refresh()
  {
    super.refresh();
    
    listener.startNonUserChange();
    
    descEdit.setText(column.getDescription());
    
    listener.finishNonUserChange();
  }

  public void setInput(IWorkbenchPart part, ISelection selection)
  {
    super.setInput(part, selection);
    IStructuredSelection sel = (IStructuredSelection) selection;
    Object item = sel.getFirstElement();
    this.column = null;
    
    if(item instanceof TableColumn)
      this.column = (TableColumn) item;
    else if(item instanceof IAdaptable)
      this.column = (TableColumn) ((IAdaptable) item).getAdapter(TableColumn.class);
    
    if(this.column == null)
      throw new IllegalArgumentException("Cannot pass object which is not TableColumn to this properties view");
  }

  public void propertyChange(PropertyChangeEvent evt)
  {
    String propName = evt.getPropertyName();
    if(propName == DesignObject.PROP_DESCRIPTION)
      refresh();
  }
}
