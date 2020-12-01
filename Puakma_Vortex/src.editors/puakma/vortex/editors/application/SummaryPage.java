/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Aug 16, 2005
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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Section;

import puakma.coreide.FilterMatcher;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.ApplicationAdapter;
import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.ObjectChangeEvent;
import puakma.utils.MimeType;
import puakma.utils.MimeTypesResolver;
import puakma.utils.lang.StringUtil;
import puakma.vortex.WorkbenchUtils;
import puakma.vortex.swt.DialogBuilder2;
import puakma.vortex.swt.MultiPageEditorPart2;
import puakma.vortex.swt.NewLFEditorPage;
import puakma.vortex.swt.SWTUtil;
import puakma.vortex.swt.DialogBuilder2.TableColumnInfo;

public class SummaryPage extends NewLFEditorPage
{
  private Application application;
  
  private InternalLabelProvider labelProvider = new InternalLabelProvider();
  
  private IStructuredContentProvider contentProvider = new IStructuredContentProvider() {
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
  
    public void dispose() {
    }

    public Object[] getElements(Object inputElement)
    {
      Application application = (Application) inputElement;
      return application.listDesignObjects();
    }
  };

  private ViewerFilter filter = new ViewerFilter() {
    public boolean select(Viewer viewer, Object parentElement, Object element)
    {
      if(element instanceof DesignObject) {
        return passFilter((DesignObject) element);
      }
      else
        return false;
    }
  };
  
  private Text nameFilterText;

  private static final int[] checkMap = {
      DesignObject.TYPE_PAGE, DesignObject.TYPE_RESOURCE,
      DesignObject.TYPE_ERROR, DesignObject.TYPE_ERROR, DesignObject.TYPE_ERROR,
      DesignObject.TYPE_ACTION, DesignObject.TYPE_SCHEDULEDACTION, DesignObject.TYPE_WIDGET,
      DesignObject.TYPE_ERROR
  };
  
  private static final String[] checkTexts = {
    "Pages", "Resources",
    "Web Resources", "Images", "XML Files",
    "Actions", "Scheduled Actions", "Widgets",
    "Shared Code",
  };

  private TableViewer viewer;
  private Button[] checks;
  private Button xmlCheck;
  private Button imgCheck;
  private Button webResCheck;
  private Button sharedCheck;
  private Combo modifiedUserCombo;
  //private Text modifiedTimeNum;
  private boolean started = false;
  
  private TableColumnInfo nameCol;
  private TableColumnInfo dsizeCol;
  private TableColumnInfo ssizeCol;
  private TableColumnInfo timeModCol;
  private TableColumnInfo perModCol;
  private TableColumnInfo descCol;
  
  private int headerSelectionIndex = 0;
  
  private InternalTableSorter sorter = new InternalTableSorter();

  private TableColumnInfo[] columns;

  private InternalApplicationListener appListener;
  
  private final class InternalApplicationListener extends ApplicationAdapter
  {
    public void objectChange(final ObjectChangeEvent event)
    {
      Display.getDefault().asyncExec(new Runnable() {
        public void run()
        {
          if(event.getObject() instanceof DesignObject == false ||
              viewer.getTable().isDisposed())
            return;
    
          switch(event.getEventType()) {
            case ObjectChangeEvent.EV_ADD_APP_OBJECT:
              if(started)
                viewer.refresh();
              updateUserCombo();
            break;
            case ObjectChangeEvent.EV_CHANGE:
              viewer.update(event.getObject(), null);
              updateUserCombo();
            break;
            case ObjectChangeEvent.EV_REMOVE:
              viewer.refresh();
              //remove(event.getObject());
              updateUserCombo();
            break;
          }
        }
      });
    }
  }

  private class InternalLabelProvider extends LabelProvider implements ITableLabelProvider {
    public Image getColumnImage(Object element, int columnIndex) {
      return null;
    }

    public String getColumnText(Object element, int index) {
      DesignObject object = (DesignObject) element;
      //nameCol, dsizeCol, ssizeCol, timeModCol, perModCol, descCol,
      if(index == nameCol.index)
        return object.getName();
      else if(index == descCol.index)
        return object.getDescription();
      else if(index == ssizeCol.index)
        return Integer.toString(object.getDesignSize(false));
      else if(index == dsizeCol.index)
        return Integer.toString(object.getDesignSize(true));
      else if(index == timeModCol.index) {
        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        return format.format(object.getLastUpdateTime());
      }
      else if(index == perModCol.index)
        return object.getUpdatedByUser();

      return "#ERROR#";
    }
  }
  
  private class InternalTableSorter extends ViewerComparator {
    public int compare(Viewer viewer, Object e1, Object e2)
    {
      DesignObject d1 = (DesignObject) e1, d2 = (DesignObject) e2;
      // TODO: implement reverse
      boolean rev = false;
      int ret = 0;
      int index = headerSelectionIndex;
      
      if(index == nameCol.index)
        ret = super.compare(viewer, d1.getName(), d2.getName());
      else if(index == descCol.index)
        ret = super.compare(viewer, d1.getDescription(), d2.getDescription());
      else if(index == ssizeCol.index)
          ret = d2.getDesignSize(false) - d1.getDesignSize(false);
      else if(index == dsizeCol.index)
          ret = d2.getDesignSize(true) - d1.getDesignSize(true);
      else if(index == timeModCol.index)
        ret = d1.getLastUpdateTime().compareTo(d2.getLastUpdateTime());
      else if(index == perModCol.index)
        ret = super.compare(viewer, d1.getUpdatedByUser(), d2.getUpdatedByUser());

      return rev ? -ret : ret;
    }
  }

  public SummaryPage(Composite parent, MultiPageEditorPart2 editor, Application application)
  {
    super(parent, editor);
    
    this.application = application;
    
    DialogBuilder2 builder = new DialogBuilder2(this);
    builder.createFormsLFComposite("Application Summary", false, 2);
    
    builder.createSection("Summary Report",
                  "You can see here summary report for application " + application.getFQName(), 1);
    Section sec = builder.getCurrentSection();
    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 4);
    sec.setLayoutData(gd);
    //gd.heightHint = SWTUtil.computeHeightOfChars(c, );
    
    viewer = builder.createTableViewer();
    Table table = viewer.getTable();
    viewer.getTable().setHeaderVisible(true);
    gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    table.setLayoutData(gd);
    
    viewer.getTable().setHeaderVisible(true);
    // TODO: setup this
    //setupTableViewer(viewer, contentProvider, labelProvider, null, columns, true, sorter, filter);
    builder.closeSection();
    
    ModifyListener modifyListener = new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        updateObjects();
      }
    };
    SelectionListener btnListener = new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        updateObjects();
      }
    };
    
    // CREATE FILTERS
    createNameFilter(builder, modifyListener);
    createDesignObjectTypeFilter(builder, btnListener);
    createUserFilter(builder, modifyListener);
    
    builder.closeComposite();
    builder.finishBuilder();
    
    initViewer();
  }

  private void initViewer()
  {
    Table table = viewer.getTable();
    nameCol = new TableColumnInfo("name", "Name", SWTUtil.computeWidthOfChars(table, 22));
    dsizeCol = new TableColumnInfo("dsize", "Data", SWTUtil.computeWidthOfChars(table, 8));
    ssizeCol = new TableColumnInfo("ssize", "Source", SWTUtil.computeWidthOfChars(table, 8));
    timeModCol = new TableColumnInfo("modtime", "Last Modified", SWTUtil.computeWidthOfChars(table, 13));
    perModCol = new TableColumnInfo("modby", "Modified By", SWTUtil.computeWidthOfChars(table, 13));
    descCol = new TableColumnInfo("desc", "Description", SWTUtil.computeWidthOfChars(table, 21));
    columns = new TableColumnInfo[] {
        nameCol, dsizeCol, ssizeCol, timeModCol, perModCol, descCol,
    };
    DialogBuilder2.setupTableColumns(viewer, columns);
    DialogBuilder2.setupTableViewer(viewer, contentProvider, labelProvider, sorter, null);
    
    appListener = new InternalApplicationListener();
    application.addListener(appListener);
    viewer.setInput(application);
    started = true;
    
    hookControlListener();
    viewer.addDoubleClickListener(new IDoubleClickListener() {
      public void doubleClick(DoubleClickEvent event)
      {
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        Iterator it = selection.iterator();
        while(it.hasNext()) {
          Object o = it.next();
          if(o instanceof DesignObject) {
            WorkbenchUtils.openDesignObject((DesignObject)o);
          }
        }
      }
    });
  }

  private void hookControlListener()
  {
    ControlListener cl = new ControlAdapter() {
      public void controlResized(ControlEvent e)
      {
        Table table = viewer.getTable();
        TableColumn[] cols = table.getColumns();
        int totalWidth = 0;
        for(int i = 0; i < cols.length; ++i)
          totalWidth += cols[i].getWidth();
        Rectangle size = table.getClientArea();
        int w = size.width + size.x;
        float mul = (float)(w) / (float)totalWidth;
        for(int i = 0; i < cols.length; ++i)
          cols[i].setWidth((int) (mul * cols[i].getWidth()));
      }
    };
    viewer.getTable().addControlListener(cl);
//    cl.controlResized(null);
  }

  private void createUserFilter(DialogBuilder2 builder, ModifyListener modifyListener)
  {
    builder.createSection("Modified By", "Filter Modification By User", 1);
    modifiedUserCombo = builder.appendCombo(false);
    modifiedUserCombo.addModifyListener(modifyListener);
    builder.closeSection();
  }

  private void createDesignObjectTypeFilter(DialogBuilder2 builder, SelectionListener btnListener)
  {
    builder.createSection("Type Filter", "Filter Design Object Type", 1);
    checks = new Button[checkMap.length];
    for(int i = 0; i < checkMap.length; ++i) {
      checks[i] = builder.appendButton(checkTexts[i], SWT.CHECK);
      checks[i].setSelection(true);
      checks[i].addSelectionListener(btnListener);
    }
    // NOW ASSIGN SOME SPECIALS
    webResCheck = checks[2]; imgCheck = checks[3]; xmlCheck = checks[4]; sharedCheck = checks[checks.length - 1];
    builder.closeSection();
  }

  private void createNameFilter(DialogBuilder2 builder, ModifyListener modifyListener)
  {
    builder.createSection("Name Filter", "Filter Design Object Name", 1);
    nameFilterText = builder.appendEdit("");
    nameFilterText.addModifyListener(modifyListener);
    builder.closeSection();
  }
  
  /**
   * Fills combobox with all programers names who were developing this application.
   */
  private void updateUserCombo()
  {
    final List<String> l = new ArrayList<String>();
//    l.add("");
    application.listDesignObjects(new FilterMatcher() {
      public boolean matches(Object obj)
      {
        DesignObject dob = (DesignObject) obj;
        String updatedBy = dob.getUpdatedByUser();
        if(updatedBy != null && l.contains(updatedBy) == false)
          l.add(dob.getUpdatedByUser());
        return false;
      }
    });
    String[] users = l.toArray(new String[l.size()]);
//    int index = modifiedUserCombo.getSelectionIndex();
//    if(index != -1)
    modifiedUserCombo.setItems(users);
  }

  /**
   * Updates all the objects in the report table.
   */
  protected void updateObjects()
  {
    viewer.refresh();
  }

  private boolean passFilter(DesignObject object)
  {
    // CHECK IF IT PASSES CHECKBOXES
    int dType = object.getDesignType();
    for(int i = 0; i < checkMap.length; ++i) {
      if(checkMap[i] == dType) {
        if(checks[i].getSelection() == false)
          return false;
      }
    }
    
    if((dType == DesignObject.TYPE_LIBRARY || dType == DesignObject.TYPE_JAR_LIBRARY)
        && sharedCheck.getSelection() == false)
      return false;
    if(dType == DesignObject.TYPE_RESOURCE) {
      String contentType = object.getContentType();
      if(contentType.startsWith("image/")) {
        if(imgCheck.getSelection() == false)
          return false;
      }
      else if(MimeTypesResolver.isWebFile(new MimeType(contentType))) {
        if(webResCheck.getSelection() == false)
          return false;
      }
      else if("text/xml".equals(contentType)) {
        if(xmlCheck.getSelection() == false)
          return false;
      }
    }

    String nameFilter = "*" + nameFilterText.getText();
    if(nameFilter.length() > 0 && StringUtil.matchWildcardIgnoreCase(object.getName(), nameFilter) == false)
      return false;
    
    String userFilter = "*" + modifiedUserCombo.getText();
    String updatedBy = StringUtil.safeString(object.getUpdatedByUser());
    if(userFilter.length() > 0 && 
        StringUtil.matchWildcardIgnoreCase(updatedBy, userFilter) == false)
      return false;
    
    return true;
  }

  public void doSave(IProgressMonitor monitor)
  {

  }

  public void disposePage()
  {
    application.removeListener(appListener);
  }
}
