package puakma.vortex.editors.dbeditor.properties;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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

public class TableColumnIndexSection  extends AbstractPropertySection implements ControlChangeListener,
                                                                                 PropertyChangeListener
{
  private TableColumn column;
  
  /**
   * A helper to listen for events that indicate that a text field has been
   * changed.
   */
  private ControlChangeHelper listener = new ControlChangeHelper(this);

  private Composite mainComposite;

    private Button pkButton;
  
  private Button uniqueButton;
  
  private Button autoIncrementButton;
  
  private Combo fkCombo;
  
  public void controlChanged(Control control)
  {
    IWorkbench wBench = PlatformUI.getWorkbench();
    IEditorPart part = wBench.getActiveWorkbenchWindow().getPages()[0].getActiveEditor();
    if(part instanceof DatabaseSchemaEditor) {
      PropertiesChangeCommand cmd = null;
      Class<TableColumn> clz = TableColumn.class;
      if(control == pkButton)
        cmd = new PropertiesChangeCommand(clz, TableColumn.PROP_PK, column,
                                          Boolean.valueOf(pkButton.getSelection()));
      else if(control == uniqueButton)
        cmd = new PropertiesChangeCommand(clz, TableColumn.PROP_UNIQUE, column,
                                          Boolean.valueOf(uniqueButton.getSelection()));
      else if(control == autoIncrementButton)
        cmd = new PropertiesChangeCommand(clz, TableColumn.PROP_AUTO_INC, column,
                                          Boolean.valueOf(autoIncrementButton.getSelection()));
      else if(control == fkCombo) {
        String fkTableName = fkCombo.getText();
        Table fkTable = null;
        if(fkTableName.length() > 0) {
          Database db = column.getTable().getDatabase();
          fkTable = db.getTable(fkTableName);
        }
        ForeignKeyCreateCommand fkCmd = new ForeignKeyCreateCommand();
        fkCmd.setSource(column);
        fkCmd.setTarget(fkTable);
      }
      
      if(cmd != null) {
        DatabaseSchemaEditor editor = (DatabaseSchemaEditor) part;
        DbEditorController controller = editor.getGraphicalViewerController();
        controller.getCommandStack().execute(cmd);
      }
    }
//    boolean newPk = pkButton.getSelection();
//    column.setPk(newPk);
//    boolean newUniq = uniqueButton.getSelection();
//    column.setUnique(newUniq);
//    boolean newAutoInc = autoIncrementButton.getSelection();
//    column.setAutoInc(newAutoInc);
//    
//    String fkTableName = fkCombo.getText();
//    Table fkTable = null;
//    if(fkTableName.length() > 0) {
//      Database db = column.getTable().getDatabase();
//      fkTable = db.getTable(fkTableName);
//    }
//    column.setRefTable(fkTable);
  }
  
  public void aboutToBeHidden()
  {
    super.aboutToBeHidden();
    
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
    
    mainComposite.setLayout(new org.eclipse.swt.layout.GridLayout(4, false));
    
    DialogBuilder2 builder = new DialogBuilder2(mainComposite, 4, true, getWidgetFactory());
    
    builder.setEffectiveColumns(2);
    pkButton = builder.createCheckboxRow("Primary Key");
    builder.createLabelRow("");
    uniqueButton = builder.createCheckboxRow("Unique");
    builder.createLabelRow("");
    autoIncrementButton = builder.createCheckboxRow("Auto increment");
    builder.createLabelRow("");
    fkCombo = builder.createComboRow("Foreign key:", true);
    
    builder.finishBuilder();
    
    listener.startListeningTo(pkButton);
    listener.startListeningTo(uniqueButton);
    listener.startListeningTo(autoIncrementButton);
    listener.startListeningTo(fkCombo);
  }

  public void refresh()
  {
    super.refresh();
    
    listener.startNonUserChange();
    
    pkButton.setSelection(column.isPk());
    uniqueButton.setSelection(column.isUnique());
    autoIncrementButton.setSelection(column.isAutoInc());
    
    fkCombo.removeAll();
    fkCombo.add("");
    fkCombo.select(0);
    
    Table fkTable = column.getRefTable();
    Table[] tables = column.getTable().getDatabase().listTables();
    for(int i = 0; i < tables.length; ++i) {
      fkCombo.add(tables[i].getName());
      if(tables[i] == fkTable)
        fkCombo.select(i + 1);
    }
    
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
    if(TableColumn.PROP_PK == prop || TableColumn.PROP_AUTO_INC == prop
       || TableColumn.PROP_UNIQUE == prop || TableColumn.PROP_REFERENCED_TABLE == prop)
      refresh();
  }
}
