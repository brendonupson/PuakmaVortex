package puakma.vortex.editors.dbeditor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.ManhattanConnectionRouter;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.dnd.TemplateTransferDropTargetListener;
import org.eclipse.gef.editparts.LayerManager;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.requests.SimpleFactory;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.parts.ContentOutlinePage;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.gef.ui.parts.SelectionSynchronizer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.xml.sax.SAXException;

import puakma.coreide.ObjectsFactory;
import puakma.coreide.PuakmaCoreException;
import puakma.coreide.VortexMultiException;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.Database;
import puakma.coreide.objects2.DatabaseConnection;
import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.ProgressMonitor;
import puakma.coreide.objects2.Table;
import puakma.utils.lang.ArrayUtils;
import puakma.vortex.VortexPlugin;
import puakma.vortex.VortexToEclipseProgressMonitorBridge;
import puakma.vortex.editors.dbeditor.parts.DatabaseSchemaPart;
import puakma.vortex.editors.dbschema.BaseDatabaseSchemaEditor;
import puakma.vortex.editors.dbschema.DatabaseSchemaOutlinePage;
import puakma.vortex.preferences.PreferenceConstants;


public class DbEditorControllerImpl implements DbEditorController
{
  private static ColorRegistry colorRegistry;

  private SelectionSynchronizer synchronizer;
  
  private DefaultEditDomain editDomain;

  private IEditorPart editPart;

  private Database database;

  private PaletteRoot paletteRoot;

  private DatabaseConnection databaseConnection;
  
  public DbEditorControllerImpl(Database database, DatabaseConnection databaseConnection)
  {
    if(database == null)
      throw new IllegalArgumentException();

    this.database = database;
    this.databaseConnection = databaseConnection;
    
    intSetup();
  }
  
  /**
   * If the controller is giong to be used inside standard editor, we can also
   * set up {@link IEditorPart} object to it.
   */
  public DbEditorControllerImpl(Database database, DatabaseConnection databaseConnection,
                                IEditorPart editPart)
  {
    if(database == null)
      throw new IllegalArgumentException();

    this.database = database;
    this.databaseConnection = databaseConnection;
    this.editPart = editPart;
    
    intSetup();
  }

  /**
   * Internally setups the controller - generates some helper objects.
   */
  private void intSetup()
  {
    this.paletteRoot = new DbPaletteRoot();
    this.editDomain = new DefaultEditDomain(editPart);
    this.editDomain.setPaletteRoot(paletteRoot);
    this.synchronizer = new SelectionSynchronizer();
  }

  public GraphicalViewer createGraphicalViewer(Composite parent)
  {
    ScrollingGraphicalViewer viewer = new ScrollingGraphicalViewer();
    viewer.createControl(parent);
    
    configureGraphicalViewer(viewer);
    
    return viewer;
  }

  public DefaultEditDomain getEditDomain()
  {
    return editDomain;
  }
  
  public SelectionSynchronizer getSelectionSynchronizer()
  {;
    return synchronizer;
  }

  public void configureGraphicalViewer(GraphicalViewer viewer)
  {
    // SETUP EDIT DOMAIN
    getEditDomain().addViewer(viewer);
    
    // SETUP THE VIEWER
    viewer.getControl().setBackground(ColorConstants.listBackground);
    
    ScalableFreeformRootEditPart rootPart = new ScalableFreeformRootEditPart();
    viewer.setRootEditPart(rootPart);
    viewer.setEditPartFactory(new PartFactory());
    viewer.setKeyHandler(new GraphicalViewerKeyHandler(viewer));
    if(rootPart instanceof LayerManager) {
      LayerManager man = rootPart;
      ConnectionLayer layer = (ConnectionLayer) man.getLayer(LayerConstants.CONNECTION_LAYER);
      layer.setConnectionRouter(new ManhattanConnectionRouter());
    }
    
    ((FigureCanvas) viewer.getControl()).setScrollBarVisibility(FigureCanvas.AUTOMATIC);
    
    getSelectionSynchronizer().addViewer(viewer);
    
    // INITIALIZE ZOOM MANAGEMENT
    List<String> zoomLevels = new ArrayList<String>(3);
    zoomLevels.add(ZoomManager.FIT_ALL);
    zoomLevels.add(ZoomManager.FIT_WIDTH);
    zoomLevels.add(ZoomManager.FIT_HEIGHT);
    rootPart.getZoomManager().setZoomLevelContributions(zoomLevels);
    
    viewer.addDropTargetListener(createTransferDropTargetListener(viewer));
  }

  public void configurePalleteViewer(PaletteViewer viewer)
  {
    DefaultEditDomain domain = getEditDomain();
    domain.setPaletteViewer(viewer);
    //viewer.setPaletteRoot(getPaletteRoot());
  }

  public ContentOutlinePage createTreeOutlinePage(BaseDatabaseSchemaEditor editor)
  {
    DatabaseSchemaOutlinePage page = new DatabaseSchemaOutlinePage(editor, this);
    return page;
  }

  public void dispose()
  {
    getEditDomain().setActiveTool(null);
  }

  public Database getDatabase()
  {
    return database;
  }

  public PaletteRoot getPaletteRoot()
  {
    return paletteRoot;
  }

  /**
   * Create a transfer drop target listener. When using a CombinedTemplateCreationEntry
   * tool in the palette, this will enable model element creation by dragging from the palette.
   */
  private TransferDropTargetListener createTransferDropTargetListener(EditPartViewer viewer)
  {
    return new TemplateTransferDropTargetListener(viewer) {
      protected CreationFactory getFactory(Object template)
      {
        return new SimpleFactory((Class<?>) template);
      }
    };
  }

  public void initializeViewer(GraphicalViewer viewer)
  {
    viewer.setContents(getDatabase());
    
    DatabaseSchemaPart part = (DatabaseSchemaPart) viewer.getEditPartRegistry().get(getDatabase());
    part.reshuffleAllTables();
  }

  public CommandStack getCommandStack()
  {
    return getEditDomain().getCommandStack();
  }

  public void dirtyChanged()
  {
    // TODO Auto-generated method stub
    
  }
  
  /**
   * Returns {@link ColorRegistry} object which stores all color information
   * about the database schema diagrams.
   */
  public static ColorRegistry getColorRegistry()
  {
    if(colorRegistry == null) {
      colorRegistry = new ColorRegistry();
      IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
      store.addPropertyChangeListener(new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event)
        {
          updateColorRegistry(colorRegistry);
        }
      });
      
      updateColorRegistry(colorRegistry);
    }
    
    return colorRegistry;
  }

  /**
   * Updates the color registry from the current values from the preference store.
   */
  protected static void updateColorRegistry(ColorRegistry colorRegistry)
  {
    IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
    RGB color;
    color = PreferenceConverter.getColor(store, PreferenceConstants.PREF_DBED_COLOR_BACKGROUND);
    colorRegistry.put(PreferenceConstants.PREF_DBED_COLOR_BACKGROUND, color);
    color = PreferenceConverter.getColor(store, PreferenceConstants.PREF_DBED_COLOR_TABLEHEAD_LEFT);
    colorRegistry.put(PreferenceConstants.PREF_DBED_COLOR_TABLEHEAD_LEFT, color);
    color = PreferenceConverter.getColor(store, PreferenceConstants.PREF_DBED_COLOR_TABLEHEAD_RIGHT);
    colorRegistry.put(PreferenceConstants.PREF_DBED_COLOR_TABLEHEAD_RIGHT, color);
    color = PreferenceConverter.getColor(store, PreferenceConstants.PREF_DBED_COLOR_TABLEBODY_LEFT);
    colorRegistry.put(PreferenceConstants.PREF_DBED_COLOR_TABLEBODY_LEFT, color);
    color = PreferenceConverter.getColor(store, PreferenceConstants.PREF_DBED_COLOR_TABLEBODY_RIGHT);
    colorRegistry.put(PreferenceConstants.PREF_DBED_COLOR_TABLEBODY_RIGHT, color);
  }
  
  public void setupGraphicalPropertiesForModel(Properties props)
  {
    Table[] tables = database.listTables();
    for(int i = 0; i < tables.length; ++i) {
      String position = props.getProperty(tables[i].getName() + ".position");
      if(position != null) {
        int[] pos = ArrayUtils.parseIntArray(position, ',');
        if(pos.length == 2) {
          Rectangle r = new Rectangle(pos[0], pos[1], 0, 0);
          tables[i].setData(DatabaseSchemaPart.KEY_BOUNDS, r);
        }
      }
    }
  }

  public void loadPreferences()
  {
    Application application = this.databaseConnection.getApplication();
    String name = generatePropertiesDesignObjectName();
    DesignObject object = application.getDesignObject(name);
    if(object == null)
      return;
    
    ByteArrayOutputStream os = new ByteArrayOutputStream(object.getDesignSize(false));
    
    try {
      object.download(os, false);
      byte[] data = os.toByteArray();
      ByteArrayInputStream bis = new ByteArrayInputStream(data);
      Properties p = new Properties();
      p.load(bis);
      setupGraphicalPropertiesForModel(p);
    }
    catch(Exception ex) {
      
    }
  }

  public Properties getProperties()
  {
    Properties p = new Properties();
    
    Table[] tables = database.listTables();
    for(int i = 0; i < tables.length; ++i) {
      Rectangle r = (Rectangle) tables[i].getData(DatabaseSchemaPart.KEY_BOUNDS);
      if(r != null) {
        String key = tables[i].getName() + ".position";
        String pos = Integer.toString(r.x) + "," + Integer.toString(r.y);
        p.setProperty(key, pos);
      }
    }
    
    return p;
  }

  public void saveAllStuffToServer(IProgressMonitor monitor)
  {
    if(databaseConnection == null)
      throw new IllegalStateException("Database connection is not defined.");
    
    monitor.beginTask("Saving database status...", 10);
    
    try {
      // NOW WE SHOULD UPLOAD PREFERENCES TO THE SERVER
      try {
        uploadPreferences();
      }
      catch(Exception ex) {
        VortexPlugin.log(ex);
        // IF WE JUST LOG AND IGNORE THIS EXCEPTION, IT'S ALL RIGHT BECAUSE NOT
        // UPLOADING DATA IS NOT FATAL [-;
      }
      finally {
        monitor.worked(1);
      }
    
      // NOW COMMIT THE DATABASE TO THE SERVER
      try {
        uploadDatabaseData(new SubProgressMonitor(monitor, 9));
      }
      catch(Exception ex) {
        VortexPlugin.log(ex);
      }
    }
    finally {
      monitor.done();
    }
  }

  private void uploadDatabaseData(IProgressMonitor monitor) throws VortexMultiException
  {
    ProgressMonitor m = new VortexToEclipseProgressMonitorBridge(monitor,
                                                                 "Saving database status...");
    database.commit(m);
  }

  private void uploadPreferences() throws PuakmaCoreException, IOException
  {
    Properties p = getProperties();
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try {
      p.store(os, null);
    }
    catch(IOException e) {
      // IGNORE, CANNOT HAPPEN HERE SINCE WE WRITE ONLY TO MEMORY [-;
    }
    
    Application app = databaseConnection.getApplication();
    String name = generatePropertiesDesignObjectName();
    DesignObject obj = app.getDesignObject(name);
    if(obj == null) {
      obj = ObjectsFactory.createDesignObject(name, DesignObject.TYPE_CONFIGURATION);
      app.addObject(obj);
    }
    obj.upload(new ByteArrayInputStream(os.toByteArray()), false);
  }

  private String generatePropertiesDesignObjectName()
  {
    return ".vortex_" + databaseConnection.getName() + ".properties";
  }

  public static Database loadDatabaseFromXml(IFile file, Properties props) throws CoreException
  {
    Database db = ObjectsFactory.createDatabase();
    
    // IF THE FILE IS EMPTY JUST RETURN, WE SHOULD IGNORE SUCH A CASE
    File ffile = new File(file.getLocation().toOSString());
    if(ffile.length() > 0) {
      if(file.isSynchronized(IResource.DEPTH_ZERO) == false) {
        file.refreshLocal(IResource.DEPTH_ZERO, null);
      }
      
      InputStream is = null;
      try {
        is = file.getContents();
        String str = db.loadContentFromXml(is);
        if(str != null) {
          ByteArrayInputStream bis = new ByteArrayInputStream(str.getBytes("ISO-8859-1"));
          props.load(bis);
        }
      }
      catch(Exception e) {
        if(e instanceof CoreException)
          throw (CoreException) e;
        IStatus status = VortexPlugin.createStatus(e);
        throw new CoreException(status);
      }
      finally {
        if(is != null) try { is.close(); } catch(Exception ex) {}
      }
    }
    
    return db;
  }

  public void saveAllStuffToFile(IFile file) throws CoreException
  {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try {
      Properties p = getProperties();
      ByteArrayOutputStream pos = new ByteArrayOutputStream();
      p.store(pos, null);
      String positions = new String(pos.toByteArray(), "ISO-8859-1");
      database.saveContentToXml(os, positions);
      ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
      if(file.exists())
        file.setContents(is, true, true, null);
      else
        file.create(is, true, null);
    }
    catch(Exception e) {
      if(e instanceof CoreException)
        throw (CoreException) e;
      IStatus status = VortexPlugin.createStatus(e);
      throw new CoreException(status);
    }
  }
}
