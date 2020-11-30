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
import puakma.coreide.objects2.Database;
import puakma.coreide.objects2.Table;
import puakma.vortex.VortexPlugin;
import puakma.vortex.swt.DialogBuilder2;
import puakma.vortex.swt.TitleAreaDialog2;

public class NewTableDialog extends TitleAreaDialog2 implements ModifyListener
{
  private Database database;
  private Text nameText;

  public NewTableDialog(Database database, Shell shell)
  {
    super(shell, "NewTableDialog");
    
    if(database == null)
      throw new IllegalArgumentException();
    
    this.database = database;
  }

  protected void initialize()
  {
    setTitle("New Table");
    setDescription("Create a new table");
    
    nameText.addModifyListener(this);
  }

  protected Control createDialogArea(Composite parent)
  {
    Composite area = (Composite) super.createDialogArea(parent);
    DialogBuilder2 builder = new DialogBuilder2(area);
    builder.setEffectiveColumns(2);
    
    nameText = builder.createEditRow("Name:");
    
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
    if(database.getTable(name) != null)
      return "Table " + name + " already exists in the database";
    
    return null;
  }

  protected void okPressed()
  {
    final IProgressMonitor monitor = new NullProgressMonitor();
    final String tableName = nameText.getText();
    try {
      ModalContext.run(new IRunnableWithProgress() {
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
        {
          createNewTable(tableName, monitor);
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

  protected void createNewTable(String tableName, IProgressMonitor monitor) throws InvocationTargetException
  {
    Table t = ObjectsFactory.createTable(tableName);
    try {
      database.addObject(t);
    }
    catch(Exception e) {
      throw new InvocationTargetException(e);
    }
  }
}
