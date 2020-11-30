/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    May 17, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.application;

import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.forms.widgets.Section;

import puakma.coreide.ObjectsFactory;
import puakma.coreide.PuakmaCoreException;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.Keyword;
import puakma.utils.lang.StringUtil;
import puakma.vortex.IconConstants;
import puakma.vortex.VortexPlugin;
import puakma.vortex.swt.DialogBuilder2;
import puakma.vortex.swt.NewLFEditorPage;
import puakma.vortex.swt.SWTUtil;
import puakma.vortex.swt.DialogBuilder2.TableColumnInfo;

/**
 * @author Martin Novak
 */
public class KeywordsPage extends NewLFEditorPage
{
  public static final String COLUMN_NAME = "name";
  private Application application;
  private TableViewer keywordViewer;
  private TableViewer dataViewer;
  private KeywordProvider keywordProvider = new KeywordProvider();
  private DataProvider dataProvider = new DataProvider();
  
  /**
   * This class is for providing the content of the roles. Input is always Role interface.
   *
   * @author Martin Novak
   */
  public class KeywordProvider extends LabelProvider
                               implements IStructuredContentProvider, ICellModifier, ITableLabelProvider
  {
    private TableViewer viewer;
    private Application application;
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
    {
      this.viewer = (TableViewer) viewer;
      this.application = (Application) newInput;
    }

    public boolean canModify(Object element, String property)
    {
      return true;
    }

    public Object getValue(Object element, String property)
    {
      if(COLUMN_NAME.equals(property))
        return StringUtil.safeString(((Keyword)element).getName());
      return null;
    }

    public void modify(Object element, String property, Object value)
    {
      TableItem item = (TableItem) element;
      Keyword kw = (Keyword) item.getData();
      Keyword copy = kw.makeWorkingCopy();

      if(COLUMN_NAME.equals(property))
        copy.setName((String)value);

      try {
        copy.commit();
        viewer.update(kw, new String[] { COLUMN_NAME });
      }
      catch(Exception e) {
        VortexPlugin.log(e);
        MessageDialog.openError(getShell(), "Cannot update keyword",
                                "Cannot save keyword to the server.\nReason:\n" + e.getLocalizedMessage());
      }
    }
    public Object[] getElements(Object inputElement)
    {
      return application.listKeywords();
    }
    public Image getColumnImage(Object element, int columnIndex)
    {
      return null;
    }
    public String getColumnText(Object element, int columnIndex)
    {
      if(columnIndex == 0)
        return StringUtil.safeString(((Keyword)element).getName());
      return null;
    }
  }
  
  /**
   * This class is here for providing content for keyword data.
   *
   * @author Martin Novak
   */
  public class DataProvider extends LabelProvider
                            implements IStructuredContentProvider, ICellModifier, ITableLabelProvider
  {
    private TableViewer viewer;
    private Keyword kw;
    public Object[] getElements(Object inputElement)
    {
      return kw.listData();
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
    {
      this.viewer = (TableViewer) viewer;
      this.kw = (Keyword) newInput;
    }

    public boolean canModify(Object element, String property)
    {
      return true;
    }

    public Object getValue(Object element, String property)
    {
      if(COLUMN_NAME.equals(property))
        return StringUtil.safeString(((String)element));
      return null;
    }

    public void modify(Object element, String property, Object value)
    {
      TableItem item = (TableItem) element;
      Table table = dataViewer.getTable();
      TableItem[] items = table.getItems();
      int index = -1;
      for(int i = 0; i < items.length; ++i) {
        if(items[i] == item) {
          index = i;
          break;
        }
      }
      if(index == -1)
        throw new IllegalStateException("Cannot find the proper index for the cell editor");
      
//      int index = dataViewer.getTable().getSelectionIndex();
//      String oldData = (String) item.getData();
//      String oldData1 = kw.getData(index);
//      if(oldData.equals(oldData1) == false)
//        throw new IllegalStateException("Inconsistent data");

      try {
        Keyword k = kw.makeWorkingCopy();
        k.setData((String) value, index);
        k.commitData();
        
        viewer.setInput(kw);
        viewer.getTable().select(index);
      }
      catch(PuakmaCoreException e) {
        VortexPlugin.log(e);
        MessageDialog.openError(getShell(), "Error updating data",
                                "Cannot update data.\nReason:\n" + e.getLocalizedMessage());
      }
    }

    public Image getColumnImage(Object element, int columnIndex)
    {
      return null;
    }

    public String getColumnText(Object element, int columnIndex)
    {
      if(columnIndex == 0)
        return StringUtil.safeString((String)element);
      return null;
    }
  }

  public KeywordsPage(Composite parent, ApplicationEditor editor, Application application)
  {
    super(parent, editor);
    
    this.editor = editor;
    this.application = application;
    
    DialogBuilder2 builder = new DialogBuilder2(this);
    builder.createFormsLFComposite("Keywords", false, 2);
    Composite c = builder.getCurrentComposite();
    GridLayout gl = (GridLayout) c.getLayout();
    gl.makeColumnsEqualWidth = true;

    createLeftSection(builder);
    createRightSection(builder);
    
    configureKeywordTable();
    configureDataTable();
    
    builder.closeComposite();
    builder.finishBuilder();
    
    // so now we can initialize all table viewers
    keywordViewer.setInput(application);
  }

  private void createRightSection(DialogBuilder2 builder)
  {
    Section sec = builder.createSection("Data", "Specify all data used in the keyword", 1);
    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    sec.setLayoutData(gd);
    
    // create button toolbar
    builder.createToolbarRow();
    
    ToolItem item = builder.appendToolbarButton(null, "Add Keyword Data", IconConstants.NEW_FILE);
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        addData();
      }
    });
    
    item = builder.appendToolbarButton(null, "Remove Keyword Data", IconConstants.DELETE);
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        removeData();
      }
    });
    
    item = builder.appendToolbarButton(null, "Move Keyword Data Up", IconConstants.ARROW_UP);
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        moveUp();
      }
    });
    
    item = builder.appendToolbarButton(null, "Move Keyword Data Down", IconConstants.ARROW_DOWN);
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        moveDown();
      }
    });
    builder.closeToolbar();
    
    dataViewer = builder.createTableViewer();
    Table table = dataViewer.getTable();
    gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    table.setLayoutData(gd);
    
    builder.closeSection();
  }

  private void createLeftSection(DialogBuilder2 builder)
  {
    Section sec = builder.createSection("Keywords", "Define keywords used in application", 1);
    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    sec.setLayoutData(gd);
    
    builder.createToolbarRow();
    
    ToolItem item = builder.appendToolbarButton(null, "Add Keyword", IconConstants.NEW_FILE);
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        addKeyword();
      }
    });
    item = builder.appendToolbarButton(null, "Remove Keyword", IconConstants.DELETE);
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        removeKeyword();
      }
    });    
    builder.closeToolbar();

    
    keywordViewer = builder.createTableViewer();
    Table table = keywordViewer.getTable();
    gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    table.setLayoutData(gd);
    
    builder.closeSection();
  }
  
  private void configureKeywordTable()
  {
    Table table = keywordViewer.getTable();
    DialogBuilder2.setupTableViewer(keywordViewer, keywordProvider, keywordProvider, null, keywordProvider);
    TableColumnInfo[] columns = {
        new TableColumnInfo(COLUMN_NAME, "Keyword name", SWTUtil.computeWidthOfChars(table, 30),
                            TableColumnInfo.CELL_MOD_TEXT, true),
    };
    DialogBuilder2.setupTableColumns(keywordViewer, columns);
    
    keywordViewer.getTable().setHeaderVisible(true);
    keywordViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event)
      {
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        if(selection.size() != 1) {
          dataViewer.setInput(null);
        }
        else {
          Keyword kw = (Keyword) selection.getFirstElement();
          dataViewer.setInput(kw);
        }
      }
    });
  }
  
  /**
   * Configures viewer which displays keyword data.
   */
  private void configureDataTable()
  {
    Table table = dataViewer.getTable();
    DialogBuilder2.setupTableViewer(dataViewer, dataProvider, dataProvider, null, dataProvider);
    TableColumnInfo[] columns = {
        new TableColumnInfo(COLUMN_NAME, "Data", SWTUtil.computeWidthOfChars(table, 30),
                            TableColumnInfo.CELL_MOD_TEXT, true),
    };
    DialogBuilder2.setupTableColumns(dataViewer, columns);
    
    dataViewer.getTable().setHeaderVisible(true);
  }
  
  private void addKeyword()
  {
    InputDialog dlg = new InputDialog(getShell(), "Add Keyword", "Type name of the new keyword", "", new IInputValidator() {
      public String isValid(String newText)
      {
        if(application.getRole(newText) != null)
          return "Keyword " + newText + " already exists.";
        return null;
      }
    });
    if(dlg.open() == Window.OK) {
      Keyword kw = ObjectsFactory.createKeyword(dlg.getValue());
      try {
        application.addObject(kw);
        keywordViewer.add(kw);
      }
      catch(PuakmaCoreException e) {
        VortexPlugin.log(e);
        MessageDialog.openError(getShell(), "Unable to add keyword",
                                "Unable to add keyword.\nReason:\n"+e.getLocalizedMessage());
      }
    }
  }
  
  private void removeKeyword()
  {
    IStructuredSelection selection = (IStructuredSelection) keywordViewer.getSelection();
    Iterator it = selection.iterator();
    boolean hasErrors = false;

    while(it.hasNext()) {
      Keyword kw = (Keyword) it.next();
      try {
        kw.remove();
        keywordViewer.remove(kw);
      }
      catch(Exception e) {
        VortexPlugin.log(e);
        hasErrors = true;
      }
    }
    
    dataViewer.setInput(null);
    
    // create
    if(hasErrors)
      MessageDialog.openError(getShell(), "Unable to remove role(s)", "Unable to remove one or more role(s). See error log for more details.");
  }
  
  private void addData()
  {
    IStructuredSelection selection = (IStructuredSelection) keywordViewer.getSelection();
    if(selection.size() != 1)
      return;
    Keyword kw = (Keyword) selection.getFirstElement();
    kw = kw.makeWorkingCopy();
    
    InputDialog dlg = new InputDialog(getShell(), "Add Keyword Data", "Type new data for keyword", "", null);
    
    if(dlg.open() == Window.OK) {
      try {
        String str = dlg.getValue();
        kw.addData(str);
        kw.commitData();
        dataViewer.add(str);
      }
      catch(PuakmaCoreException e) {
        VortexPlugin.log(e);
        MessageDialog.openError(getShell(), "Unable to add keyword data",
                                "Unable to add permission.\nReason:\n"+e.getLocalizedMessage());
      }
    }
  }

  private void removeData()
  {
    Keyword kw = getSelectedKeyword();
    kw = kw.makeWorkingCopy();

    Table dataTable = dataViewer.getTable();
    int[] indexes = dataTable.getSelectionIndices();
    if(indexes.length == 0)
      return;

    try {
      for(int i = 0; i < indexes.length; ++i) {
        kw.removeData(indexes[i]);
      }
      kw.commitData();
      dataViewer.setInput(kw);
    }
    catch(PuakmaCoreException e) {
      MessageDialog.openError(getShell(), "Unable to remove role(s)",
                              "Unable to remove one or more role(s). See error log for more details.");
    }
  }

  /**
   * Gets the currently selected keyword.
   *
   * @return Keyword object if some keyword is selected or null if no keywprd is selected
   */
  private Keyword getSelectedKeyword()
  {
    IStructuredSelection kwSelection = (IStructuredSelection) keywordViewer.getSelection();
    if(kwSelection.size() != 1)
      return null;
    Keyword kw = (Keyword) kwSelection.getFirstElement();
    return kw;
  }
  
  private void moveUp()
  {
    Keyword kw = getSelectedKeyword();
    if(kw == null)
      return;

    Table table = dataViewer.getTable();
    if(table.getSelectionCount() != 1)
      return;
    int index = table.getSelectionIndex();
    try {
      kw = kw.makeWorkingCopy();
      kw.shiftValue(index, index - 1);
      kw.commitData();
      dataViewer.setInput(kw);
      table.select(index - 1);
    }
    catch(PuakmaCoreException e) {
      VortexPlugin.log(e);
      MessageDialog.openError(getShell(), "Cannot shift value down",
                              "Cannot shift value down.\nReason:\n" + e.getLocalizedMessage());
    }
    
  }
  
  private void moveDown()
  {
    Keyword kw = getSelectedKeyword();
    if(kw == null)
      return;

    Table table = dataViewer.getTable();
    if(table.getSelectionCount() != 1)
      return;
    int index = table.getSelectionIndex();
    if(index >= table.getItemCount() - 1)
      return;
    try {
      kw = kw.makeWorkingCopy();
      kw.shiftValue(index, index + 1);
      kw.commitData();
      dataViewer.setInput(kw);
      table.select(index + 1);
    }
    catch(PuakmaCoreException e) {
      VortexPlugin.log(e);
      MessageDialog.openError(getShell(), "Cannot shift value down",
                              "Cannot shift value down.\nReason:\n" + e.getLocalizedMessage());
    }
  }

  public void doSave(IProgressMonitor monitor)
  {
  }

  public void disposePage()
  {
    // WE DON'T NEED TO DISPOSE ANYTHING
  }
}
