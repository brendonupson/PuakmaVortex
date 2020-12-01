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

import puakma.coreide.objects2.Database;
import puakma.coreide.objects2.Table;
import puakma.coreide.objects2.TableColumn;
import puakma.vortex.editors.dbeditor.DbEditorController;
import puakma.vortex.editors.dbeditor.commands.ForeignKeyCreateCommand;
import puakma.vortex.editors.dbeditor.commands.PropertiesChangeCommand;
import puakma.vortex.editors.dbschema.DatabaseSchemaEditor;
import puakma.vortex.properties.ControlChangeHelper;
import puakma.vortex.properties.ControlChangeListener;
import puakma.vortex.swt.DialogBuilder2;
import puakma.vortex.swt.SWTUtil;

public class TableColumnMainSection extends AbstractPropertySection implements ControlChangeListener,
                                                                               PropertyChangeListener
{
  private TableColumn column;
  
  /**
   * A helper to listen for events that indicate that a text field has been changed.
   */
  private ControlChangeHelper listener = new ControlChangeHelper(this);

  private Composite mainComposite;
  
  private Text nameEdit;

  private Text typeText;

  private Text typeSizeText;

  private Text floatDecimalsText;
  
  public void controlChanged(Control control)
  {
    IWorkbench wBench = PlatformUI.getWorkbench();
    IEditorPart part = wBench.getActiveWorkbenchWindow().getPages()[0].getActiveEditor();
    if(part instanceof DatabaseSchemaEditor) {
      PropertiesChangeCommand cmd = null;
      Class<TableColumn> clz = TableColumn.class;
      if(control == nameEdit && nameEdit.getText().equals(column.getName()) == false)
        cmd = new PropertiesChangeCommand(clz, TableColumn.PROP_NAME, column, nameEdit.getText());
      else if(control == typeText && typeText.getText().equals(column.getType()) == false)
        cmd = new PropertiesChangeCommand(clz, TableColumn.PROP_TYPE, column, typeText.getText());
      else if(control == typeSizeText && typeSizeText.getText().equals(Integer.toString(column.getTypeSize())) == false)
        cmd = new PropertiesChangeCommand(clz, TableColumn.PROP_TYPESIZE, column, typeSizeText.getText());
      else if(control == floatDecimalsText && floatDecimalsText.getText().equals(Integer.toString(column.getFloatDecimals())) == false)
        cmd = new PropertiesChangeCommand(clz, TableColumn.PROP_DECIMAL_DIGITS, column, floatDecimalsText.getText());
      
      if(cmd != null) {
        DatabaseSchemaEditor editor = (DatabaseSchemaEditor) part;
        DbEditorController controller = editor.getGraphicalViewerController();
        controller.getCommandStack().execute(cmd);
      }
    }
    
//    String newName = nameEdit.getText();
//    column.setName(newName);
//    String newType = typeText.getText();
//    column.setType(newType);
//    int newTypeSize = Integer.parseInt(typeSizeText.getText());
//    column.setTypeSize(newTypeSize);
//    String decsText = floatDecimalsText.getText();
//    int newDecimals = decsText.length() > 0 ? Integer.parseInt(decsText) : 0;
//    column.setFloatDecimals(newDecimals);
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
    
    nameEdit = builder.createEditRow("Name:");
    //builder.createLabelRow("");
    
    builder.createSection("Type Definition", null, 4, 2);
    builder.setEffectiveColumns(2);
    
    typeText = builder.createEditRow("Type:");
    builder.createLabelRow("");
    typeSizeText = builder.createEditRow("Size:");
    SWTUtil.setIntValidation(typeSizeText);
    floatDecimalsText = builder.createEditRow("Decimals:");
    SWTUtil.setIntValidation(floatDecimalsText);
    builder.closeSection();
    
    builder.finishBuilder();
    
    listener.startListeningTo(nameEdit);
    listener.startListeningTo(typeText);
    listener.startListeningTo(typeSizeText);
    listener.startListeningTo(floatDecimalsText);
  }

  public void refresh()
  {
    super.refresh();
    
    listener.startNonUserChange();
    
    nameEdit.setText(column.getName());
    typeText.setText(column.getType());
    typeSizeText.setText(Integer.toString(column.getTypeSize()));
    floatDecimalsText.setText(Integer.toString(column.getFloatDecimals()));
    
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
    String prop = evt.getPropertyName();
    if(TableColumn.PROP_NAME == prop || TableColumn.PROP_TYPE == prop
       || TableColumn.PROP_TYPESIZE == prop || TableColumn.PROP_DECIMAL_DIGITS == prop)
      refresh();
  }
}
