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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.forms.widgets.Section;

import puakma.coreide.ObjectsFactory;
import puakma.coreide.PuakmaCoreException;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.Permission;
import puakma.coreide.objects2.Role;
import puakma.utils.lang.StringUtil;
import puakma.vortex.IconConstants;
import puakma.vortex.VortexPlugin;
import puakma.vortex.swt.DialogBuilder2;
import puakma.vortex.swt.NewLFEditorPage;
import puakma.vortex.swt.DialogBuilder2.TableColumnInfo;

/**
 * @author Martin Novak
 */
public class RolesPage extends NewLFEditorPage
{
  public static final String COLUMN_NAME = "name";
  public static final String COLUMN_DESC = "desc";
  private Application application;
  private ApplicationEditor editor;
  private TableViewer roleViewer;
  private TableViewer permViewer;
  private RoleProvider roleProvider = new RoleProvider();
  private PermProvider permProvider = new PermProvider();
  
  private Listener controlsChangeListener = new Listener() {
    public void handleEvent(Event event)
    {
    }
  };
  
  /**
   * This class is for providing the content of the roles. Input is always Role interface.
   *
   * @author Martin Novak
   */
  public class RoleProvider extends LabelProvider
                            implements IStructuredContentProvider, ICellModifier, ITableLabelProvider
  {
    private TableViewer viewer;
    private Application application;
    public void dispose() { }
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
        return StringUtil.safeString(((Role)element).getName());
      else if(COLUMN_NAME.equals(property))
        return StringUtil.safeString(((Role)element).getDescription());
      return null;
    }

    public void modify(Object element, String property, Object value)
    {
      TableItem item = (TableItem) element;
      Role role = (Role) item.getData();
      Role copy = role.makeWorkingCopy();

      if(COLUMN_NAME.equals(property))
        copy.setName((String)value);
      else if(COLUMN_DESC.equals(property))
        copy.setDescription((String) value);

      try {
        copy.commit();
        viewer.update(role, new String[] { COLUMN_NAME, COLUMN_DESC });
      }
      catch(Exception e) {
        VortexPlugin.log(e);
        MessageDialog.openError(getShell(), "Cannot update role",
                                "Cannot save role to the server.\nReason:\n" + e.getLocalizedMessage());
      }
    }
    public Object[] getElements(Object inputElement)
    {
      return application.listRoles();
    }
    public Image getColumnImage(Object element, int columnIndex)
    {
      return null;
    }
    public String getColumnText(Object element, int columnIndex)
    {
      if(columnIndex == 0)
        return StringUtil.safeString(((Role)element).getName());
      else if(columnIndex == 1)
        return StringUtil.safeString(((Role)element).getDescription());
      return null;
    }
  }
  
  /**
   * This class is here for providing content for permissions.
   *
   * @author Martin Novak
   */
  public class PermProvider extends LabelProvider
                            implements IStructuredContentProvider, ICellModifier, ITableLabelProvider
  {
    private TableViewer viewer;
    private Role role;
    public Object[] getElements(Object inputElement)
    {
      return role.listPermissions();
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
    {
      this.viewer = (TableViewer) viewer;
      this.role = (Role) newInput;
    }

    public boolean canModify(Object element, String property)
    {
      return true;
    }

    public Object getValue(Object element, String property)
    {
      if(COLUMN_NAME.equals(property))
        return StringUtil.safeString(((Permission)element).getName());
      else if(COLUMN_DESC.equals(property))
        return StringUtil.safeString(((Permission)element).getDescription());
      return null;
    }

    public void modify(Object element, String property, Object value)
    {
      TableItem item = (TableItem) element;
      Permission perm = (Permission) item.getData();
      Permission copy = perm.makeWorkingCopy();
      if(COLUMN_NAME.equals(property))
        copy.setName((String)value);
      else if(COLUMN_DESC.equals(property))
        copy.setName((String)value);
      
      try {
        copy.commit();
        viewer.update(perm, new String[] {COLUMN_NAME, COLUMN_DESC});
      }
      catch(Exception e) {
        VortexPlugin.log(e);
        MessageDialog.openError(getShell(), "Error updating permission",
                                "Cannot update permission.\nReason:\n" + e.getLocalizedMessage());
      }
    }

    public Image getColumnImage(Object element, int columnIndex)
    {
      return null;
    }

    public String getColumnText(Object element, int columnIndex)
    {
      if(columnIndex == 0)
        return StringUtil.safeString(((Permission)element).getName());
      else if(columnIndex == 1)
        return StringUtil.safeString(((Permission)element).getDescription());
      return null;
    }
  }

  public RolesPage(Composite parent, ApplicationEditor editor, Application application)
  {
    super(parent, editor);

    this.editor = editor;
    this.application = application;

    DialogBuilder2 builder = new DialogBuilder2(this);
    Composite c = builder.createFormsLFComposite("Roles", false, 2);
    GridLayout gl = (GridLayout) c.getLayout();
    gl.makeColumnsEqualWidth = true;
    //GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    //gd.grabExcessVerticalSpace = true;
    //c.setLayoutData(gd);
    
    createRoleSection(builder);
    createPermissionSection(builder);
    
    initViewers(builder);
    
    builder.closeComposite();
    builder.finishBuilder();

    // so now we can initialize all table viewers
    roleViewer.setInput(application);
  }
  
  private void createRoleSection(DialogBuilder2 builder)
  {
    Section sec = builder.createSection("Roles", "Define the roles used in application", 1);
    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    sec.setLayoutData(gd);
    //GridData gd = (GridData) sec.getLayoutData();
    //gd.grabExcessVerticalSpace = true;
    
    builder.createToolbarRow();
    ToolItem item = builder.appendToolbarButton(null, "Create New Role", IconConstants.NEW_FILE);
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        addRole();
      }
    });

    item = builder.appendToolbarButton(null, "Remove Role", IconConstants.DELETE);
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        removeRole();
      }
    });
    builder.closeToolbar();
    
    roleViewer = builder.createTableViewer();
    Table table = roleViewer.getTable();
    gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    table.setLayoutData(gd);
    roleViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event)
      {
        IStructuredSelection selection = (IStructuredSelection)event.getSelection();
        if(selection.size() != 1) {
          permViewer.setInput(null);
        }
        else {
          Role role = (Role)selection.getFirstElement();
          permViewer.setInput(role);
        }
      }
    });
    
    builder.closeSection();
  }

  /**
   * Creates section with permission table, and some toolbar to manipulate with
   * those permissions.
   */
  private void createPermissionSection(DialogBuilder2 builder)
  {
    Section sec = builder.createSection("Permissions", "Specify permissions for users who have the role", 1);
    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    sec.setLayoutData(gd);
    
    builder.createToolbarRow();

    ToolItem item = builder.appendToolbarButton(null, "Create New Permission", IconConstants.NEW_FILE);
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        addPermission();
      }
    });
    
    item = builder.appendToolbarButton(null, "Remove Permission", IconConstants.DELETE);
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        removePermission();
      }
    });
    
    builder.closeToolbar();
    
    permViewer = builder.createTableViewer();
    Table table = permViewer.getTable();
    gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    table.setLayoutData(gd);
    
    builder.closeSection();
  }
  
  /**
   * Initializes both table viewers.
   */
  private void initViewers(DialogBuilder2 builder)
  {
    builder.setupTableViewer(roleViewer, roleProvider, roleProvider, null, roleProvider);
    TableColumnInfo[] cols = {
        new TableColumnInfo(COLUMN_NAME, "Role", 260, TableColumnInfo.CELL_MOD_TEXT),
    };
    builder.setupTableColumns(roleViewer, cols);
    
    cols = new TableColumnInfo[] {
        new TableColumnInfo(COLUMN_NAME, "Permission", 260, TableColumnInfo.CELL_MOD_TEXT),
        new TableColumnInfo(COLUMN_DESC, "Description", 300, TableColumnInfo.CELL_MOD_TEXT),
    };
    builder.setupTableColumns(permViewer, cols);
    builder.setupTableViewer(permViewer, permProvider, permProvider, null, permProvider);
  }

  /**
   * This is a event handler which asks user for creating new role, and then
   * adds this role.
   */
  private void addRole()
  {
    InputDialog dlg = new InputDialog(getShell(), "Add Role", "Type name of the new role", "", new IInputValidator() {
      public String isValid(String newText)
      {
        if(application.getRole(newText) != null)
          return "Role " + newText + " already exists.";
        return null;
      }
    });
    if(dlg.open() == Window.OK) {
      Role role = ObjectsFactory.createRole(dlg.getValue(), null);
      try {
        application.addObject(role);
        roleViewer.add(role);
      }
      catch(PuakmaCoreException e) {
        VortexPlugin.log(e);
        MessageDialog.openError(getShell(), "Unable to add role",
                                "Unable to add role.\nReason:\n"+e.getLocalizedMessage());
      }
    }
  }
  
  private void removeRole()
  {
    IStructuredSelection selection = (IStructuredSelection) roleViewer.getSelection();
    Iterator it = selection.iterator();
    boolean hasErrors = false;

    while(it.hasNext()) {
      Role role = (Role) it.next();
      try {
        role.remove();
        roleViewer.remove(role);
        permViewer.setInput(null);
      }
      catch(Exception e) {
        VortexPlugin.log(e);
        hasErrors = true;
      }
    }
    
    // create
    if(hasErrors)
      MessageDialog.openError(getShell(), "Unable to remove role(s)", "Unable to remove one or more role(s). See error log for more details.");
  }
  
  private void addPermission()
  {
    IStructuredSelection selection = (IStructuredSelection) roleViewer.getSelection();
    if(selection.size() != 1)
      return;
    final Role role = (Role) selection.getFirstElement();
    InputDialog dlg = new InputDialog(getShell(), "Add Permission", "Type name of the new permission", "", new IInputValidator() {
      public String isValid(String newText)
      {
        if(role.getPermission(newText) != null)
          return "Permission " + newText + " already exists.";
        return null;
      }
    });

    if(dlg.open() == Window.OK) {
      try {
        Permission perm = role.addPermission(dlg.getValue(), null);
        permViewer.add(perm);
      }
      catch(PuakmaCoreException e) {
        VortexPlugin.log(e);
        MessageDialog.openError(getShell(), "Unable to add permission",
                                "Unable to add permission.\nReason:\n"+e.getLocalizedMessage());
      }
    }
  }

  private void removePermission()
  {
    IStructuredSelection selection = (IStructuredSelection) permViewer.getSelection();
    Iterator it = selection.iterator();
    boolean hasErrors = false;

    while(it.hasNext()) {
      Permission perm = (Permission) it.next();
      try {
        perm.remove();
        permViewer.remove(perm);
      }
      catch(Exception e) {
        VortexPlugin.log(e);
        hasErrors = true;
      }
    }
    
    // create
    if(hasErrors)
      MessageDialog.openError(getShell(), "Unable to remove role(s)", "Unable to remove one or more role(s). See error log for more details.");
    
  }

  public void doSave(IProgressMonitor monitor)
  {
  }

  public void disposePage()
  {

  }
}
