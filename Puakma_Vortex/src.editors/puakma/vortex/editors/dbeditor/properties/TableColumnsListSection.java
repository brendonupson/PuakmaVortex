package puakma.vortex.editors.dbeditor.properties;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

import puakma.coreide.objects2.Table;
import puakma.vortex.controls.TableColumnsViewer;

public class TableColumnsListSection extends AbstractPropertySection implements PropertyChangeListener
{
  private Table table;
  
  private TableColumnsViewer viewer;
  
  public void aboutToBeHidden()
  {
    super.aboutToBeHidden();
    
    // TODO: UPDATE ALL PROPERTIES IF THE TABLE IS NOT WORKING COPY
    
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
    composite.setLayout(new org.eclipse.swt.layout.GridLayout(1, false));
    viewer = new TableColumnsViewer();
    viewer.createTableViewer(composite, SWT.BORDER);
    viewer.getTableControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
  }

  public void refresh()
  {
    super.refresh();
    
    viewer.setInput(table);
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
      this.table= (Table) ((IAdaptable) item).getAdapter(Table.class);
    
    if(this.table == null)
      throw new IllegalArgumentException("Cannot pass object which is not TableColumn to this properties view");
  }

  public void propertyChange(PropertyChangeEvent evt)
  {
    String prop = evt.getPropertyName();
    if(Table.PROP_NAME == prop || Table.PROP_DESCRIPTION == prop)
      refresh();
  }
}
