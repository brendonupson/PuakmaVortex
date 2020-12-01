package puakma.vortex.editors.dbschema;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.DirectEditAction;
import org.eclipse.gef.ui.actions.ToggleGridAction;
import org.eclipse.gef.ui.actions.ToggleSnapToGeometryAction;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.parts.SelectionSynchronizer;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

import puakma.coreide.ObjectsFactory;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.Database;
import puakma.coreide.objects2.DatabaseConnection;
import puakma.vortex.VortexPlugin;
import puakma.vortex.WorkbenchUtils;
import puakma.vortex.editors.dbeditor.DbEditorController;
import puakma.vortex.editors.dbeditor.DbEditorControllerImpl;
import puakma.vortex.editors.dbeditor.commands.ReshufleAllTablesCommand;
import puakma.vortex.editors.dbeditor.parts.DatabaseSchemaPart;
import puakma.vortex.editors.dbschema.actions.DatabaseSchemaEditorContextMenuProvider;
import puakma.vortex.editors.dbschema.generator.GeneratorWizard;
import puakma.vortex.editors.dbschema.topeditor.TopEditor;
import puakma.vortex.preferences.PreferenceConstants;
import puakma.vortex.swt.DialogBuilder2;

/**
 * This editor is simple GEF editor for drawing database schema.
 * 
 * @author Martin Novak
 */
public class DatabaseSchemaEditor extends BaseDatabaseSchemaEditor implements
ITabbedPropertySheetPageContributor,
PropertyChangeListener
{
	/**
	 * Id of this editor from plugins.xml file
	 */
	public static final String EDITOR_ID = "puakma.vortex.editors.dbschema.DatabaseSchemaEditor";

	public static final String COLOR_BACKGROUND_GRADIENT_FROM = "dbEditorGradFrom";

	public static final String COLOR_BACKGROUND_GRADIENT_TO = "dbEditorGradTo";

	public static final String COLOR_TABLE_BORDER = "dbEditorTableBorder";

	private static ColorRegistry colorRegistry;

	private TopEditor listEditor;

	public void dispose()
	{
		getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);

		super.dispose();
	}

	public void init(IEditorSite site, IEditorInput input) throws PartInitException
	{
		if(input instanceof DatabaseSchemaEditorInput == false && input instanceof IFileEditorInput == false)
			throw new PartInitException("Input has to be of type DatabaseSchemaInput or IFileEditorInput");

		super.init(site, input);

		// add selection change listener
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);

		if(input instanceof IFileEditorInput) {
			setPartName(input.getName());
		}
		else {
			DatabaseSchemaEditorInput dbInput = (DatabaseSchemaEditorInput) input;
			if(dbInput.getConnection() != null) {
				DatabaseConnection connection = dbInput.getConnection();
				Application application = connection.getApplication();
				setPartName(connection.getName());
				application.addListener(this);
			}
			else
				setPartName("Database schema");
		}
	}

	/**
	 * Returns casted editor input to {@link DatabaseSchemaEditorInput}
	 */
	public DatabaseSchemaEditorInput getDatabaseInput()
	{
		return (DatabaseSchemaEditorInput) getEditorInput();
	}

	/**
	 * Returns casted editor input to {@link IFileEditorInput}
	 */
	public IFileEditorInput getFileEditorInput()
	{
		return (IFileEditorInput) getEditorInput();
	}

	/**
	 * Returns {@link Database} object instance.
	 */
	public Database getDatabase()
	{
		return getGraphicalViewerController().getDatabase();
	}

	protected void configureGraphicalViewer()
	{
		super.configureGraphicalViewer();

		GraphicalViewer viewer = getGraphicalViewer();

		// SETUP ZOOM MANAGEMENT ACTIONS
		RootEditPart rootPart = viewer.getRootEditPart();
		if(rootPart instanceof ScalableFreeformRootEditPart) {
			ScalableFreeformRootEditPart scalablePart = (ScalableFreeformRootEditPart) rootPart;
			IAction zoomIn = new ZoomInAction(scalablePart.getZoomManager());
			IAction zoomOut = new ZoomOutAction(scalablePart.getZoomManager());
			getActionRegistry().registerAction(zoomIn);
			getActionRegistry().registerAction(zoomOut);

			// IServiceLocator locator = getSite();

			IKeyBindingService keyService = getSite().getKeyBindingService();
			// ((IKeyBindingService) locator.getService(IKeyBindingService.class));
			keyService.registerAction(zoomIn);
			keyService.registerAction(zoomOut);
		}

		// CREATE ACTIONS FOR EDITOR
		getActionRegistry().registerAction(new ToggleSnapToGeometryAction(getGraphicalViewer()));
		getActionRegistry().registerAction(new ToggleGridAction(getGraphicalViewer()));

		ContextMenuProvider cmProvider = new DatabaseSchemaEditorContextMenuProvider(viewer,
				getActionRegistry(),
				getCommandStack());
		viewer.setContextMenu(cmProvider);
		getSite().registerContextMenu("puakma.vortex.editor.databaseschema.contextmenu", cmProvider, viewer);
		if(listEditor != null)
			listEditor.setupGraphicalViewer(viewer);
	}

	public boolean isDirty()
	{
		return getCommandStack().isDirty() || (listEditor != null && listEditor.isDirty());
	}

	protected SelectionSynchronizer getSelectionSynchronizer()
	{
		return getGraphicalViewerController().getSelectionSynchronizer();
	}

	protected void createActions()
	{
		super.createActions();

		IAction action;
		ActionRegistry registry = getActionRegistry();

		// action = new CopyTemplateAction(this);
		// registry.registerAction(action); // TADY NENI ZADNE PRIDANI DO SELECTED ACTIONS???
		// action = new MatchWidthAction(this);
		// registry.registerAction(action);
		// getSelectionActions().add(action.getId());

		action = new DirectEditAction((IWorkbenchPart) this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());
	}

	public void doSave(IProgressMonitor monitor)
	{
		IEditorInput input = getEditorInput();
		if(input instanceof IFileEditorInput) {
			IFileEditorInput fileInput = (IFileEditorInput) input;
			try {
				monitor.beginTask("Saving...", 1);
				getGraphicalViewerController().saveAllStuffToFile(fileInput.getFile());
				getCommandStack().markSaveLocation();
			}
			catch(CoreException e) {
				VortexPlugin.log(e);
			}
			finally {
				monitor.done();
			}
		}
		else {
			DatabaseSchemaEditorInput dbInput = (DatabaseSchemaEditorInput) input;
			if(dbInput.getConnection() == null)
				return;

			Job j = new Job("Saving database definition") {
				protected IStatus run(IProgressMonitor monitor)
				{
					try {
						getGraphicalViewerController().saveAllStuffToServer(monitor);

						getCommandStack().markSaveLocation();
					}
					finally {
						Display.getDefault().asyncExec(new Runnable() {
							public void run()
							{
								firePropertyChange(PROP_DIRTY);
							}
						});
					}
					return Status.OK_STATUS;
				}
			};
			j.schedule();
		}
	}

	public void doSaveAs()
	{
		// DO NOTHING... [-;
	}

	public boolean isSaveAsAllowed()
	{
		return false;
	}

	public void createPartControl(Composite parent)
	{
		Composite c = new Composite(parent, SWT.NULL);
		GridLayout gl = new GridLayout();
		gl.horizontalSpacing = gl.verticalSpacing = 0;
		gl.marginHeight = gl.marginWidth = 0;
		c.setLayout(gl);

		createToolbar(c);

		super.createPartControl(c);

		IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
		boolean topEditorVisible = store.getBoolean(PreferenceConstants.PREF_DBED_TOP_EDITOR_VISIBLE);
		maximizeGraphicalViewer(!topEditorVisible);
	}

	protected void createNonGraphicalControl(Composite parent)
	{
		// EDITOR WITH LIST OF TABLES, COLUMNS, AND PROPERTIES
		listEditor = new TopEditor(parent, getGraphicalViewerController());
	}

	private void createToolbar(Composite parent)
	{
		// TOOLBAR
		ToolBar tb = new ToolBar(parent, SWT.FLAT | SWT.RIGHT);
		tb.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		DialogBuilder2.hookToolbarManaegmentMenu(tb);

		ToolItem ti = new ToolItem(tb, SWT.RIGHT);
		ti.setText("Generate");
		ti.setToolTipText("Generates database on database server using the jdbc driver");
		ti.setImage(VortexPlugin.getDefault().getImage("generate_db.png"));
		ti.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e)
			{
				generateDatabase();
			}
		});

		ti = new ToolItem(tb, SWT.SEPARATOR);

		ti = new ToolItem(tb, SWT.NULL);
		// ti.setText("Rearrange tables");
		ti.setToolTipText("Rearranges all tables in the editor");
		ti.setImage(VortexPlugin.getDefault().getImage("rearange.png"));
		ti.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e)
			{
				rearrangeTables();
			}
		});

		IEditorInput input = getEditorInput();
		if(input instanceof DatabaseSchemaEditorInput) {
			// TODO: allow exporting also for xml file definition
			DatabaseSchemaEditorInput dbInput = (DatabaseSchemaEditorInput) input;
			DatabaseConnection connection = dbInput.getConnection();
			if(connection != null) {
				ti = new ToolItem(tb, SWT.NULL);
				// ti.setText("Database Connection Properties");
				ti.setImage(VortexPlugin.getDefault().getImage("generate_db.png"));
				ti.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e)
					{
						showDatabaseConnectionProperties();
					}
				});
			}
		}

		ti = new ToolItem(tb, SWT.SEPARATOR);

		final ToolItem tish = new ToolItem(tb, SWT.CHECK);
		// tish.setText("Show/Hide Top Editor");
		tish.setToolTipText("Shows/hides the top editor");
		tish.setImage(VortexPlugin.getDefault().getImage("show_details.png"));
		IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
		boolean topEditorVisible = store.getBoolean(PreferenceConstants.PREF_DBED_TOP_EDITOR_VISIBLE);
		tish.setSelection(topEditorVisible);
		tish.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e)
			{
				showHideTopEditor();
			}
		});

		if(VortexPlugin.DEBUG_MODE) {
			ti = new ToolItem(tb, SWT.PUSH);
			ti.setText("Export...");
			ti.setToolTipText("Export database schema to external file in the filesystem");
			ti.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e)
				{

				}
			});
		}
	}

	private void showHideTopEditor()
	{
		boolean isVisible = isGraphicalViewerMaximized();
		maximizeGraphicalViewer(!isVisible);
		IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
		store.setValue(PreferenceConstants.PREF_DBED_TOP_EDITOR_VISIBLE, isVisible);
	}

	/**
	 * This shows database connection properties page.
	 * 
	 * TODO: maybe change this method to show just simple dialog
	 */
	protected void showDatabaseConnectionProperties()
	{
		DatabaseSchemaEditorInput input = getDatabaseInput();
		DatabaseConnection connection = input.getConnection();
		if(connection != null)
			WorkbenchUtils.openDatabaseObject(connection);
	}

	/**
	 * This function rearranges all tables in the editor view.
	 */
	private void rearrangeTables()
	{
		GraphicalViewer viewer = getGraphicalViewer();
		DatabaseSchemaPart part = (DatabaseSchemaPart) viewer.getEditPartRegistry().get(getDatabase());
		ReshufleAllTablesCommand cmd = new ReshufleAllTablesCommand(part);
		getCommandStack().execute(cmd);
	}

	/**
	 * Shows the dialog which lets you generate the database.
	 */
	private void generateDatabase()
	{
		GeneratorWizard wizard = new GeneratorWizard(getDatabase(), getDatabaseInput().getConnection());
		WizardDialog dlg = new WizardDialog(getEditorSite().getShell(), wizard);
		dlg.open();
	}

	/**
	 * This function gets the color registry for the editor
	 * 
	 * @return ColorRegistry object
	 */
	public static ColorRegistry getColorRegistry()
	{
		if(colorRegistry == null) {
			colorRegistry = new ColorRegistry();
		}
		colorRegistry.put(COLOR_BACKGROUND_GRADIENT_FROM, new RGB(0xb6, 0xce, 0xdd));// 0xdc,
		// 0xe6,
		// 0xec));
		colorRegistry.put(COLOR_BACKGROUND_GRADIENT_TO, new RGB(0xe7, 0xee, 0xf2));
		colorRegistry.put(COLOR_TABLE_BORDER, new RGB(0, 96, 110));

		return colorRegistry;
	}

	protected void initializeController()
	{
		DbEditorController controller = null;
		Database db = null;
		IEditorInput input = getEditorInput();

		if(input instanceof DatabaseSchemaEditorInput) {
			DatabaseSchemaEditorInput dbInput = (DatabaseSchemaEditorInput) input;
			db = dbInput.getDatabase();
			controller = new DbEditorControllerImpl(db, dbInput.getConnection());
			setController(controller);

			getGraphicalViewerController().loadPreferences();
		}
		else {
			Properties props = new Properties();
			IFileEditorInput fileInput = (IFileEditorInput) input;
			db = ObjectsFactory.createDatabase();
			try {
				// TODO: pass db as parameter here
				db = DbEditorControllerImpl.loadDatabaseFromXml(fileInput.getFile(), props);
			}
			catch(CoreException e) {
				VortexPlugin.log(e);
			}
			controller = new DbEditorControllerImpl(db, null);
			controller.setupGraphicalPropertiesForModel(props);
			setController(controller);
		}
	}

	public Object getAdapter(Class type)
	{
		if(type == IPropertySheetPage.class) {
			// PropertySheetPage page = new PropertySheetPage();
			TabbedPropertySheetPage page = new TabbedPropertySheetPage(this);
			return page;
			// page.setRootEntry(new UndoablePropertySheetEntry(getCommandStack()));
			// return page;
		}
		else
			return super.getAdapter(type);
	}

	public String getContributorId()
	{
		return getSite().getId();
	}

	public void propertyChange(final PropertyChangeEvent evt)
	{
		Display.getDefault().asyncExec(new Runnable() {
			public void run()
			{
				synchronizedPropertyChange(evt);
			}
		});
	}

	private void synchronizedPropertyChange(PropertyChangeEvent evt)
	{
		String prop = evt.getPropertyName();
		if(prop == Application.PROP_CLOSE) {
			getSite().getPage().closeEditor(this, false);
		}
	}
}
