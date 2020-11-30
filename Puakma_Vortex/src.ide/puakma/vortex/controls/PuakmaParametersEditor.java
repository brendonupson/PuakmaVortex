/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 22, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.controls;

import java.util.Iterator;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.Parameters;
import puakma.utils.lang.StringUtil;
import puakma.vortex.dialogs.DualInputDialog;

/**
 * Editor for application/design object parameters. It's basically table with some
 * buttons to add/remove parameters.
 *
 * @author Martin Novak
 */
public class PuakmaParametersEditor
{
  /**
   * These properties cannot be edited in the table.
   */
  public static final String SPECIAL_DESIGN_OBJECT_PROPERTIES[] = {
    Parameters.PARAM_OPEN_ACTION, Parameters.PARAM_SAVE_ACTION, Parameters.PARAM_PARENT_PAGE,
  };
  
  /**
   * These properties cannot be edited in the table.
   */
  public static final String SPECIAL_APPLICATION_PROPERTIES[] = {
    Parameters.PARAM_OPEN_ACTION, Parameters.PARAM_OPEN_ACTION_1,
    Parameters.PARAM_SAVE_ACTION, Parameters.PARAM_SAVE_ACTION_1,
    Parameters.PARAM_DEFAULT_OPEN, Parameters.PARAM_DEFAULT_CHARSET,
    Parameters.PARAM_LOGIN_PAGE,
  };

  private static final String COLUMN_NAME = "Name";
  private static final String COLUMN_VALUE = "Value";
  
  static class NameValue {
    public String name;
    public String value;
  }
  
  /**
   * This is provides content of all the parameters for application/design object.
   *
   * @author Martin Novak
   */
  public class PropertiesProvider extends LabelProvider
                                  implements IStructuredContentProvider, ICellModifier,
                                             ITableLabelProvider
  {
    public void dispose() { }
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {  }
    public boolean canModify(Object element, String property)
    {
      return true;
    }

    public Object getValue(Object element, String property)
    {
      if(COLUMN_NAME.equals(property))
        return ((NameValue)element).name;
      else if(COLUMN_VALUE.equals(property)) {
        return getParameterValue(((NameValue)element).name);
      }
      return null;
    }

    public void modify(Object element, String property, Object value)
    {
      TableItem item = (TableItem) element;
      NameValue x = (NameValue) item.getData();

      if(COLUMN_NAME.equals(property)) {
        setParameterName(x.name, (String)value);
        x.name = (String) value;
      }
      else if(COLUMN_VALUE.equals(property)) {
        if(((String)value).length() > 0) {
          setParameterValue(x.name, (String)value);
          x.value = (String) value;
        }
      }
      
      viewer.refresh(x);
    }
    public Object[] getElements(Object inputElement)
    {
      String[] ps = params.listParameters();
      NameValue[] ret = new NameValue[ps.length];
      for(int i = 0; i < ps.length; ++i) {
        ret[i] = new NameValue();
        ret[i].name = ps[i];
      }
      return ret;
    }
    public Image getColumnImage(Object element, int columnIndex)
    {
      return null;
    }
    public String getColumnText(Object element, int columnIndex)
    {
      NameValue x = (NameValue) element;
      if(columnIndex == 0)
        return StringUtil.safeString(x.name);
      else if(columnIndex == 1)
        return StringUtil.safeString(getParameterValue(x.name));
      return "#ERROR#";
    }
  }
  
  public class Filter extends ViewerFilter {
    public boolean select(Viewer viewer, Object parentElement, Object element)
    {
      NameValue nv = (NameValue) element;
      String name = nv.name;
      if(application != null) {
        if(params.isReservedAppProperty(name))
          return false;
      }
      else {
        if(params.isReservedPageProperty(name))
          return false;
      }
      return true;
    }
  }

  public static interface DirtyListener {
    public void dirtyChanged();
  }
  
  private DirtyListener dirtyListener;
  private Application application;
  private DesignObject obj;
  private Parameters params;
  private TableViewer viewer;
  private Table table;
  private PuakmaParametersEditor.PropertiesProvider provider;
  private Composite parent;
  private int numCols;
  private FormToolkit toolkit;

  private boolean isDirty;

  /**
   * Use this constructor to create properties editor for application.
   *
   * @param parent is the parent Composite
   * @param params is the working copy of parameters
   * @param application is the application which properties you want to edit
   * @param toolkit is the parent form toolkit
   * @param numCols is the number of parent columns which should we grab
   */
  public PuakmaParametersEditor(Composite parent, FormToolkit toolkit, Parameters params,
                                Application application, int numCols)
  {
    this.parent = parent;
    this.application = application;
    this.params = params;
    this.toolkit = toolkit;
    this.numCols = numCols;
    
    createContent(toolkit);
  }

  /**
   * This constructor should be used when you want to edit design objectproperties.
   *
   * @param parent is the parent Composite
   * @param obj is the design object which properties you want to edit
   * @param toolkit is the parent form toolkit
   * @param numCols is the number of parent columns which should we grab
   */
  public PuakmaParametersEditor(Composite parent, FormToolkit toolkit, Parameters params,
                                DesignObject obj, int numCols)
  {
    this.parent = parent;
    this.obj = obj;
    this.params = params;
    this.toolkit = toolkit;
    this.numCols = numCols;
    
    createContent(toolkit);
  }
  
  private void createContent(FormToolkit toolkit)
  {
    provider = new PropertiesProvider();
    
    Button addBtn = toolkit.createButton(parent, "Add...", SWT.PUSH);
    addBtn.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        fireAddProperty();
      }
    });
    Button remBtn = toolkit.createButton(parent, "Remove", SWT.PUSH);
    remBtn.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        fireRemoveProperty();
      }
    });

    // create the table
    table = new Table(parent, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION );
    
    // setup table
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.heightHint = 150;
    gd.horizontalSpan = numCols;
    table.setLayoutData(gd);
  
    table.setLinesVisible(true);
    table.setHeaderVisible(true);
    
    // ok, now create all columns
    TableColumn col = new TableColumn(table, SWT.LEFT, 0);
    col.setText("Name");
    col.setWidth(200);
    
    col = new TableColumn(table, SWT.LEFT, 1);
    col.setText("Value");
    col.setWidth(300);
    
    // setup the viewer
    viewer = new TableViewer(table);
    viewer.setUseHashlookup(true);
    viewer.setColumnProperties(new String[] { "Name", "Value" });
    
    CellEditor[] editors = new CellEditor[2];
    editors[0] = new TextCellEditor(table);
    ((Text)editors[0].getControl()).addVerifyListener(new VerifyListener() {
      public void verifyText(VerifyEvent e)
      {
        String[] invalidNames = null;
        if(obj != null) {
          invalidNames = SPECIAL_DESIGN_OBJECT_PROPERTIES;
        }
        else if(application != null) {
          invalidNames = SPECIAL_APPLICATION_PROPERTIES;
        }
        
        if(invalidNames != null) {
          for(int i = 0; i < invalidNames.length; ++i) {
            if(invalidNames[i].equalsIgnoreCase(e.text)) {
              e.doit = false;
              return;
            }
          }
        }
      }
    });
    
    editors[1] = new TextCellEditor(table);
    
    viewer.setCellEditors(editors);
    viewer.setCellModifier(provider);
    viewer.addFilter(new Filter());
    
    viewer.setContentProvider(provider);
    viewer.setLabelProvider(provider);
    viewer.setInput(application == null ? obj : (Parameters)application);
  }

  /**
   * Removes the currently properties from design object/application.
   */
  protected void fireRemoveProperty()
  {
    if(((IStructuredSelection)viewer.getSelection()).isEmpty())
      return;

    if(MessageDialog.openConfirm(parent.getShell(),"Remove parameter","Do you want to remove parameter(s)?")) {
      IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
      Iterator it = selection.iterator();
      while(it.hasNext()) {
        NameValue nv = (NameValue) it.next();
        params.removeParameter(nv.name);
        setDirty(true);
        viewer.remove(nv);
      }
    }
  }

  /**
   * This method shows dialog to add property and value, and then possibly adds
   * property among the rest properties.
   */
  protected void fireAddProperty()
  {
    DualInputDialog dlg = new DualInputDialog(parent.getShell(),"Add parameter",
                                              "Add parameter. Type name, and value.",
                                              null,null,new IInputValidator() {
      public String isValid(String newText)
      {
        // at first - do not allow user to add special parameters like openaction, etc - simply
        // the things which are supposed to be edited in the combo boxes, etc...
        //
        if(obj != null) {
          if(params.isReservedPageProperty(newText))
              return "Use design object properties editor to edit this parameter";
        }
        else {
          if(params.isReservedAppProperty(newText))
              return "Use application properties editor to edit this parameter";
        }
        
        if(params.getParameterValue(newText) != null)
          return "Parameter " + newText + " already exists";
        
        return null;
      }
    }, new IInputValidator() {
      public String isValid(String newText)
      {
        if(newText == null || newText.length() == 0)
          return "Cannot set empty value parameter";
        return null;
      }
    });

    if(dlg.open() == Window.OK) {
      String name = dlg.getValue();
      String value = dlg.getValue2();
      params.addParameter(name, value);
      
      NameValue nv = new NameValue();
      nv.name = name;
      viewer.add(nv);
      setDirty(true);
    }
  }
  
  /**
   * Gets the safe value of the parameter for the current object/application.
   *
   * @return String object, never null
   */
  private String getParameterValue(String paramName)
  {
    String ret = null;
    ret = params.getParameterValue(paramName);
    
    ret = StringUtil.safeString(ret);
    return ret;
  }
  
  private void setParameterName(String pName, String newName)
  {
    String value = params.getParameterValue(pName);
    params.removeParameter(pName);
    params.setParameter(newName, value);
    setDirty(true);
  }
  
  private void setParameterValue(String pName, String value)
  {
    params.setParameter(pName, value);  
    setDirty(true);
  }

  public void setDirty(boolean dirty)
  {
    isDirty = dirty;
    if(dirtyListener != null)
      dirtyListener.dirtyChanged();
  }
  
  public void setDirtyListener(DirtyListener listener)
  {
    this.dirtyListener = listener;
  }

  public void dispose()
  {
  }

  public boolean isDirty()
  {
    return isDirty;
  }
}
