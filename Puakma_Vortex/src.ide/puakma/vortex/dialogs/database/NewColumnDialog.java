package puakma.vortex.dialogs.database;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import puakma.coreide.ObjectsFactory;
import puakma.coreide.objects2.Table;
import puakma.coreide.objects2.TableColumn;
import puakma.vortex.VortexPlugin;
import puakma.vortex.swt.DialogBuilder2;
import puakma.vortex.swt.SWTUtil;
import puakma.vortex.swt.TitleAreaDialog2;

/**
 * Dialog for creating a new column in the table.
 *
 * @author Martin Novak
 */
public class NewColumnDialog extends TitleAreaDialog2 implements ModifyListener
{
  private Table table;
  private Text nameText;
  private Text typeText;
  private Text sizeText;

  public NewColumnDialog(Table table, Shell shell)
  {
    super(shell, "NewColumnDialog");
    
    if(table == null)
      throw new IllegalArgumentException();
    
    this.table = table;
  }

  protected void initialize()
  {
    setTitle("New Column");
    setDescription("Create a new column in the table " + table.getName());
    
    typeText.setText("VARCHAR");
    sizeText.setText("30");
    
    SWTUtil.setIntValidation(sizeText);
    nameText.addModifyListener(this);
    typeText.addModifyListener(this);
    sizeText.addModifyListener(this);
  }

  protected Control createDialogArea(Composite parent)
  {
    Composite area = (Composite) super.createDialogArea(parent);
//    GridLayout gl = (GridLayout) area.getLayout();
//    gl.numColumns = 2;

    DialogBuilder2 builder = new DialogBuilder2(area);
    builder.createComposite(2);
    
    nameText = builder.createEditRow("Name:");
    builder.createSeparatorRow(true);
    typeText = builder.createEditRow("Type:");
    sizeText = builder.createEditRow("Size:");
    
    builder.closeComposite();
    builder.finishBuilder();
    return area;
  }

  public void modifyText(ModifyEvent e)
  {
    String error = getErrorMessage();
    setErrorMessage(error);
  }

  public String getErrorMessage()
  {
    String name = nameText.getText();
    if(name.length() == 0)
      return "Column name cannot be empty";
    if(table.getColumn(name) != null)
      return "Column " + name + " already exists in the table " + table.getName();
    
    return null;
  }

  protected void okPressed()
  {
    final IProgressMonitor monitor = new NullProgressMonitor();
    final String tableName = nameText.getText();
    final String columnType = typeText.getText();
    String sColSize = sizeText.getText();
    final int columnSize;
    try {
      columnSize = Integer.parseInt(sColSize);
    }
    catch(Exception ex) {
      return;
    }
    
    try {
      ModalContext.run(new IRunnableWithProgress() {
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
        {
          createNewColumn(tableName, columnType, columnSize, monitor);
        }
        
      }, true, monitor, getShell().getDisplay());
      super.okPressed();
    }
    catch(InvocationTargetException e) {
      Throwable t = e.getTargetException();
      VortexPlugin.log(t);
      setErrorMessage(t.getLocalizedMessage());
      // BUT ENABLE OK BUTTON
      Button okBtn = getButton(IDialogConstants.OK_ID);
      okBtn.setEnabled(true);
      
    }
    catch(InterruptedException e) {
      // SHOULDN'T OCCUR
    }
  }

  protected void createNewColumn(String columnName, String columnType, int columnSize, IProgressMonitor monitor) throws InvocationTargetException
  {
    try {
      TableColumn column = ObjectsFactory.createTableColumn(columnName);
      column.setType(columnType);
      column.setTypeSize(columnSize);
      table.addColumn(column);
    }
    catch(Exception e) {
      throw new InvocationTargetException(e);
    }
  }
}
