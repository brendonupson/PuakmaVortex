/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Aug 5, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import puakma.coreide.ObjectsFactory;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.ApplicationObject;
import puakma.coreide.objects2.Database;
import puakma.coreide.objects2.DatabaseConnection;
import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.JavaObject;
import puakma.coreide.objects2.Keyword;
import puakma.coreide.objects2.Permission;
import puakma.coreide.objects2.Table;
import puakma.coreide.objects2.TableColumn;
import puakma.utils.MimeType;
import puakma.utils.MimeTypesResolver;
import puakma.vortex.editors.application.ApplicationEditor;
import puakma.vortex.editors.application.ApplicationEditorInput;
import puakma.vortex.editors.dbschema.DatabaseSchemaEditor;
import puakma.vortex.editors.dbschema.DatabaseSchemaEditorInput;
import puakma.vortex.editors.design.PuakmaEditor;
import puakma.vortex.editors.design.PuakmaEditorInput;
import puakma.vortex.editors.query.QueryEditor;
import puakma.vortex.editors.query.QueryEditorInput;
import puakma.vortex.project.ProjectManager;
import puakma.vortex.project.ProjectUtils;
import puakma.vortex.project.PuakmaProject2;

public class WorkbenchUtils
{
  /**
   * The default cache for shared images.
   */
  //private static ImagesCache imagesCache
  
  private static JavaElementLabelProvider javaLabelProvider = new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);

  /**
   * This function opens design object in the design object editor. If the design
   * object is java object, then simple java editor is opened.
   * @param object is the DesignObject which will be opened
   */
  public static void openDesignObject(DesignObject object)
  {
    assert object != null : "Cannot pass null design object";

    try {
      if(object instanceof JavaObject) {
        openJavaEditor(object);
      }
      else {
        openDesignEditor(object);
      }
    }
    catch(Exception e) {
      VortexPlugin.log(e);
      IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
      MessageDialog.openError(activeWorkbenchWindow.getShell(), "Error Opening Editor",
          "Cannot open for design object " + object.getName() + "\nReason:\n" + e.getLocalizedMessage());
    }
  }

  private static void openJavaEditor(DesignObject object) throws PartInitException, JavaModelException, IdeException
  {
    if(ensureStarted(object) == false)
      return;
    
    // WELL, WELL, WELL, LOOKS LIKE JAVA SUBSYSTEM HAS BEEN STARTED UP, SO WE
    // OPEN THAT SHIT!
    IFile file = ProjectUtils.getIFile(object, true);
    IJavaElement je = JavaCore.create(file);
    ICompilationUnit cu = JavaCore.createCompilationUnitFrom(file);
    IEditorPart javaEditor = JavaUI.openInEditor(cu);
    JavaUI.revealInEditor(javaEditor, je);
  }

  private static boolean ensureStarted(DesignObject object) throws IdeException
  {
//  CHECK THE STATUS OF THE PROJECT - IF IT'S NOT DOWNLOADED, DOWNLOAD IT
    final PuakmaProject2 project = ProjectManager.getProject(object.getApplication());
    if(project.javaStarted() == false) {
      IRunnableContext context = PlatformUI.getWorkbench().getProgressService();
      IRunnableWithProgress runnable = new IRunnableWithProgress() {
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
        {
          try {
            project.startJava(monitor);
          }
          catch(Exception e) { throw new InvocationTargetException(e); }
        }
      };
      try {
        context.run(true, true, runnable);
        return true;
      }
      catch(InvocationTargetException e) {
        if(e.getTargetException() instanceof IdeException)
          throw (IdeException)e.getTargetException();
        else throw new IdeException(e.getTargetException());
      }
      catch(InterruptedException e) {
        // IF INTERRUPTED, SIMPLY IGNORE
        return false;
      }
    }

    return true;
  }

  private static void openDesignEditor(DesignObject object) throws IdeException, PartInitException
  {
    boolean isSource = false;
    // CHECK OUT IF THE FILE EXISTS
    IFile file = ProjectUtils.getIFile(object, isSource);
    if(file.exists() == false)
      ProjectUtils.downloadFile(object, isSource);

    IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    IWorkbenchPage page = activeWorkbenchWindow.getActivePage();

    IEditorInput finput = new PuakmaEditorInput(object);
    page.openEditor(finput, PuakmaEditor.EDITOR_ID,true);
  }
  
  public static void openApplicationEditor(Application application)
  {
    internalOpenApplicationEditor(application);
  }
  
  private static ApplicationEditor internalOpenApplicationEditor(Application application)
  {
    IWorkbenchPage page = getActivePage();
    ApplicationEditorInput input = new ApplicationEditorInput();
    input.setApplication(application);
    
    try {
      IEditorPart part = page.openEditor(input,ApplicationEditor.EDITOR_ID, true);
      if(part instanceof ApplicationEditor == false)
        return null;
      return (ApplicationEditor) part;
    }
    catch(PartInitException e) {
      MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                              "Cannot create editor",
                              "Cannot create application settings editor\nReason:\n"
                              + e.getLocalizedMessage());
      VortexPlugin.log(e);
      return null;
    }
  }
  
  public static void openDatabaseObject(DatabaseConnection object)
  {
    ApplicationEditor editor = internalOpenApplicationEditor(object.getApplication());
    editor.selectDatabaseObject(object);
  }
  
  @SuppressWarnings("unused")
  private static IEditorPart getEditor(IWorkbenchPage page, String editorId, boolean restore)
  {
    IEditorReference[] refs = page.getEditorReferences();
    for(int i = 0; i < refs.length; ++i) {
      if(editorId.equals(refs[i].getId()))
        return refs[i].getEditor(restore);
    }
    
    return null;
  }

  /**
   * Gets the active page on the active workbench window.
   * 
   * @return IWorkbenchPage object
   */
  public static IWorkbenchPage getActivePage()
  {
    IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    return activeWorkbenchWindow.getActivePage();
  }

  public static void openApplicationObject(ApplicationObject obj)
  {
    if(obj instanceof DesignObject) {
      openDesignObject((DesignObject) obj);
    }
    else if(obj instanceof DatabaseConnection) {
      openDatabaseObject((DatabaseConnection)obj);
    }
    else {
      VortexPlugin.log("Operation not implemented - trying to open unsupported type to open");
    }
  }
  
  public static Image getImageFromCache(ApplicationObject object)
  {
    if(object instanceof JavaObject) {
      IFile file = ProjectUtils.getIFile((JavaObject)object, true);
//      return javaLabelProvider.getImage(file);
      //ILabelProvider provider = WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider();
      
//      IContentType contentType = IDE.getContentType(file);
//      IEditorRegistry reg = PlatformUI.getWorkbench().getEditorRegistry();
//      ImageDescriptor desc = reg.getImageDescriptor(file.getName(), contentType);
//      Image image = imagesCache.getImage(desc);
//      ILabelDecorator decorator = PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator();
//      decorator.decorateImage(image, file);
//      return image;
//      return desc.
      
//      IWorkbench wb = PlatformUI.getWorkbench();
      //ILabelDecorator decorator = PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator();
      //DecoratingLabelProvider dProvider = new DecoratingLabelProvider(new WorkbenchLabelProvider(),
      //    decorator);
//      return provider.getImage(file);
      
      return javaLabelProvider.getImage(file);
    }
    else {
      String key = getImageName(object);
      if(key == null) {
        return null;
  //      descriptor = PuakmaIdePlugin.getImageDescriptor("error_img.gif");
  //        ImageDescriptor.getMissingImageDescriptor();
      }
      return VortexPlugin.getDefault().getImage(key);
    }
  }

  /**
   * This function returns shared images for the application object. Note that this
   * images is small images, so it can be used in trees, and list views only.
   *
   * @param object is the application object for which we have image
   * @return ImageDescriptor object or default missing image descriptor
   */
  public static String getImageName(ApplicationObject object)
  {
    String key = "";

    if(object instanceof DesignObject) {
      DesignObject dobj = (DesignObject) object;
      switch(dobj.getDesignType()) {
        case DesignObject.TYPE_PAGE:
          key = "text.gif";
        break;
        case DesignObject.TYPE_RESOURCE:
          MimeType mt = new MimeType(dobj.getContentType());
          if("image".equals(mt.getMainType()))
            key = "image_file.gif";
          else if(new MimeType("text/xml").equals(mt))
            key = "xml_file.gif";
          else if(MimeTypesResolver.isWebFile(mt))
            key = "html_file.gif";
          else if(MimeTypesResolver.isDocument(mt))
            key = "doc_file.gif";
          else
            key = "exclamation.gif";
        break;
        case DesignObject.TYPE_JAR_LIBRARY:
          key = "jar_file.gif";
        break;
        case DesignObject.TYPE_ACTION:
          key = null;
        break;
        case DesignObject.TYPE_DOCUMENTATION:
          key = null;
        break;
        case DesignObject.TYPE_WIDGET:
          key = "web_module.gif";
        break;
        case DesignObject.TYPE_LIBRARY:
          key = "java_file.gif";
        break;
      }
    }
    else if(object instanceof DatabaseConnection)
      key = "database.gif";
    else if(object instanceof Table) {
      key = "table.gif";
    }
    else if(object instanceof TableColumn) {
      TableColumn col = (TableColumn) object;
      if(col.isPk() && col.isFk())
        key = "column_pkfk.gif";
      else if(col.isPk())
        key = "column_pk.gif";
      else if(col.isFk())
        key = "column_fk.gif";
      else
        key = "column.gif";
    }
    
    if(key == null)
      return null;
    
    return key;
  }

  /**
   * This returns dscriptive name of the object type like 'action' or 'jar library'
   * or 'keyword' according to the type of the object. The result is only
   * informative.
   *
   * @param obj is the object to check
   * @return String with object type name
   * @throws IllegalArgumentException if type of object is unknown
   */
  public static String getObjectTypeName(ApplicationObject obj)
  {
    if(obj instanceof DesignObject) {
      DesignObject dobj = (DesignObject) obj;
      switch(dobj.getDesignType()) {
        case DesignObject.TYPE_ACTION:
          return "action";
        case DesignObject.TYPE_JAR_LIBRARY:
          return "jar library";
        case DesignObject.TYPE_LIBRARY:
          return "java code";
        case DesignObject.TYPE_PAGE:
          return "page";
        case DesignObject.TYPE_RESOURCE:
          return "resource";
        case DesignObject.TYPE_SCHEDULEDACTION:
          return "scheduled action";
        case DesignObject.TYPE_WIDGET:
          return "widget";
        case DesignObject.TYPE_CONFIGURATION:
          return "vortex configuration";
      }
    }
    else if(obj instanceof Keyword)
      return "keyword";
    else if(obj instanceof Permission)
      return "permission";
    
    throw new IllegalArgumentException("Unknown object - " + obj.toString());
  }
  
  /**
   * This function closes all editors for the application object object.
   * 
   * @param object is the application object for which we want to close editor
   */
  public static void closeEditorsForObject(final ApplicationObject object)
  {
    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
        for(int j = 0; j < windows.length; ++j) {
          IWorkbenchPage[] pages = windows[j].getPages();
          for(int i = 0; i < pages.length; i++) {
            IEditorReference[] refs = pages[i].getEditorReferences();
            for(int k = 0; k < refs.length; ++k) {
              IEditorInput input;
              try {
                input = refs[k].getEditorInput();
                if(input instanceof IFileEditorInput) {
                  IFileEditorInput finput = (IFileEditorInput) input;
                  IFile file = finput.getFile();
                  DesignObject dob = ProjectUtils.getDesignObject(file);
                  if(dob == object) {
                    pages[i].closeEditor(refs[k].getEditor(false), false);
                  }
                }
              }
              catch(PartInitException e) {
                VortexPlugin.log(e);
              }
            }
          }
        }
      }
    });
  }
  
  public static void openDatabaseEditor()
  {
    try {
      DatabaseConnection dbCon = ObjectsFactory.createDbConnection("testDbCon");
      Database database = dbCon.getDatabase();
      Table table = ObjectsFactory.createTable("CUSTOMER");
      table.setDescription("Description for table CUSTOMER");
      TableColumn customerIDColumn = table.addColumn("CustomerId", "INTEGER");
      customerIDColumn.setDescription("Desc for CustomerID");
      TableColumn col = table.addColumn("Name", "VARCHAR(255)");
      col.setDescription("Desc for Name");
      database.addObject(table);
      
      Table table2 = ObjectsFactory.createTable("ITEM");
      table2.setDescription("Description for table ITEM");
      col = table2.addColumn("ItemId", "INTEGER");
      col.setPk(true);
      col.setAutoInc(true);
      col.setDescription("Desc for ItemID");
      col = table2.addColumn("Name", "VARCHAR(255)");
      col.setUnique(true);
      col.setDescription("Desc for Name");
      col = table2.addColumn("CustomerID","INTEGER");
      col.setRefTable(table);
      database.addObject(table2);
      
      Table table3 = ObjectsFactory.createTable("ITEMX");
      table3.setDescription("Description for table ITEMX");
      col = table3.addColumn("ItemId", "INTEGER");
      col.setPk(true);
      col.setAutoInc(true);
      col.setDescription("Desc for ItemID");
      col = table3.addColumn("Name", "VARCHAR(255)");
      col.setUnique(true);
      col.setDescription("Desc for Name");
      col = table3.addColumn("CustomerID","INTEGER");
      col.setRefTable(table);
      database.addObject(table3);
      
      IPath path = VortexPlugin.getPluginDirectory().append("tables.properties").makeAbsolute();
      DatabaseSchemaEditorInput input = new DatabaseSchemaEditorInput(database, path);
      IWorkbenchPage page = getActivePage();
    
      IDE.openEditor(page, input, DatabaseSchemaEditor.EDITOR_ID);
    }
    catch(Exception e) {
      VortexPlugin.log(e);
    }
  }

  public static void openDatabaseSchemaEditor(DatabaseConnection dc)
  {
    DatabaseSchemaEditorInput input = new DatabaseSchemaEditorInput(dc);
    try {
      IWorkbenchPage page = getActivePage();
      IDE.openEditor(page, input, DatabaseSchemaEditor.EDITOR_ID);
    }
    catch(PartInitException e) {
      VortexPlugin.log(e);
    }
  }

  public static void openDatabaseQueryEditor(DatabaseConnection dc)
  {
    QueryEditorInput input = new QueryEditorInput(dc);
    try {
      IWorkbenchPage page = getActivePage();
      IDE.openEditor(page, input, QueryEditor.EDITOR_ID);
    }
    catch(PartInitException e) {
      VortexPlugin.log(e);
    }
  }

  public static void openDatabaseSettings(DatabaseConnection dc)
  {
    ApplicationEditor editor = internalOpenApplicationEditor(dc.getApplication());
    editor.selectDatabaseObject(dc);
  }

  /**
   * Returns the top most shell, so for example message boxes can have this shell as parent.
   */
  public static Shell getTopmostShell()
  {
    return Display.getDefault().getActiveShell();
  }
  
  /**
   * Opens the Eclipse preference dialog.
   */
  public static boolean openManageJresDialog(String displayId, String[] displayIds)
  {
    Object data = null;
    PreferenceDialog dlg = PreferencesUtil.createPreferenceDialogOn(null, displayId, displayIds, data);
    return dlg.open() == Window.OK;
  }
}
