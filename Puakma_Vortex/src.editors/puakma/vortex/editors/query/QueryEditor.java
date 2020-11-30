/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:      
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.query;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;

import puakma.coreide.VortexDatabaseException;
import puakma.coreide.database.SQLUtil;
import puakma.coreide.objects2.DatabaseConnection;
import puakma.coreide.objects2.SQLQuery;
import puakma.vortex.VortexPlugin;
import puakma.vortex.swt.SWTUtil;

public class QueryEditor extends EditorPart
{
	public static final String EDITOR_ID = "puakma.vortex.editors.query.QueryEditor";
	public static final String SQL_EDITOR_ID = "org.eclipse.wst.rdb.sqleditor.SQLEditor";
	private TableViewer viewer;
	private Table table;
	/**
	 * This object provides content for the table
	 */
	private IStructuredContentProvider contentProvider;
	private QueryEditorLabelProvider labelProvider;
	private AbstractDecoratedTextEditor editor;
	/**
	 * If set to true then query is being executed.
	 */
	private boolean blocking;
	private Color redColor;
	private SashForm form;
	private Text textField;

	public void doSave(IProgressMonitor monitor)
	{
	}

	public void doSaveAs()
	{
	}

	public void init(IEditorSite site, IEditorInput input) throws PartInitException
	{
		if(input instanceof QueryEditorInput == false)
			throw new PartInitException("This editor can accept only QueryEditorInput as input");

		setSite(site);
		setInput(input);

		QueryEditorInput qInput = (QueryEditorInput) input;
		setPartName(qInput.getConnection().getName());
	}

	/**
	 * @return query editor input object
	 */
	public QueryEditorInput getQueryEditorInput()
	{
		return (QueryEditorInput) getEditorInput();
	}

	public boolean isDirty()
	{
		return false;
	}

	public boolean isSaveAsAllowed()
	{
		return false;
	}

	public void createPartControl(Composite parent)
	{
		// SETUP RED COLOR FOR ERRORS
		redColor = new Color(parent.getShell().getDisplay(), 255, 0, 0);

		form = new SashForm(parent, SWT.NONE);
		form.setLayout(new GridLayout());
		form.setOrientation(SWT.VERTICAL);

		Composite c = new Composite(form, SWT.NONE);
		c.setLayout(new GridLayout(2, false));
		c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		editor = createEditor(c);
		c.getChildren()[0].setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		// NOW CREATE EXECUTE BUTTON
		Button b = new Button(c, SWT.PUSH);
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e)
			{
				executeQuery();
			}
		});
		b.setText("Execute");
		b.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));

		createViewer();

		// HACK
		editor.setAction("ContentAssistProposal", null);
		editor.setAction("ContentAssistTip", null);
		editor.setAction("ContentFormat", null);
		editor.setAction("SQLEditor.connectAction", null);
		editor.setAction("SQLEditor.disconnectAction", null);
		QEExecuteAction qeA = new QEExecuteAction();
		qeA.setEditor(this);
		editor.setAction("SQLEditor.runAction", qeA);
		editor.setAction("SQLEditor.setStatementTerminatorAction", null);
	}

	/**
	 * Creates a sql editor for typing queries
	 */
	private AbstractDecoratedTextEditor createEditor(Composite parent)
	{
		try {
			AbstractDecoratedTextEditor editor = (AbstractDecoratedTextEditor) createDynamicEditor();

			editor.init(getEditorSite(), getEditorInput());
			editor.createPartControl(parent);
			return editor;
		}
		catch(Exception ex) {
			VortexPlugin.log(ex);
		}
		return null;
	}

	private IEditorPart createDynamicEditor() throws PartInitException
	{
		IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
		// TRY TO GET SQL EDITOR, IF NOT FOUND, TRY TO GET THE DEFAULT EDITOR
		IEditorDescriptor editorDescriptor = editorRegistry.findEditor(SQL_EDITOR_ID);
		IEditorDescriptor defDescriptor = editorRegistry.findEditor(EditorsUI.DEFAULT_TEXT_EDITOR_ID);
		if(editorDescriptor == null)
			editorDescriptor = defDescriptor;

		IExtensionRegistry registry = Platform.getExtensionRegistry();

		//    IConfigurationElement[] confEls = registry.getConfigurationElementsFor("org.eclipse.ui",
		//        "editors", editorDescriptor.getId());
		IEditorPart editorPart;
		try {
			editorPart = getEditorPart(editorDescriptor);
		}
		catch(Exception e) {
			// well well well, editor hasn't been found, so try to create the default text editor
			try {
				editorPart = getEditorPart(defDescriptor);
			}
			catch(Exception ex) {
				VortexPlugin.log(e);
				VortexPlugin.log(ex);
				throw new PartInitException("Cannot open editor: " + e.getLocalizedMessage(), e);
			}
		}

		return editorPart;
	}


	private IEditorPart getEditorPart(IEditorDescriptor descriptor) throws CoreException
	{
		IConfigurationElement[] confEls;
		Object extension;
		EditorDescriptor desc = (EditorDescriptor) descriptor;
		confEls = new IConfigurationElement[] { desc.getConfigurationElement() };
		extension = WorkbenchPlugin.createExtension(confEls[0], IWorkbenchRegistryConstants.ATT_CLASS);
		return (IEditorPart) extension;
	}

	/**
	 * This function creates a table where you can see the results of the query
	 */
	private Table createDataTable(Composite parent)
	{
		Table table = new Table(parent, SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		this.viewer = new TableViewer(table);
		contentProvider = new QueryEditorContentProvider();
		labelProvider = new QueryEditorLabelProvider();

		viewer.setLabelProvider(labelProvider);
		viewer.getTable().setHeaderVisible(true);
		viewer.setContentProvider(contentProvider);
		return table;
	}

	public void executeQuery()
	{
		blocking = true;
		IDocument doc = editor.getDocumentProvider().getDocument(getQueryEditorInput());
		try {
			final String str = doc.get(0, doc.getLength());
			IRunnableWithProgress runnable = new IRunnableWithProgress() { 
				public void run(IProgressMonitor monitor) throws InvocationTargetException,
				InterruptedException
				{
					doExecuteQuery(monitor, str);
				}
			};
			getEditorSite().getWorkbenchWindow().run(true, false, runnable);
		}
		catch(InvocationTargetException e) {
			VortexPlugin.log(e.getTargetException());
		}
		catch(Exception e) {
			VortexPlugin.log(e);
		}
		finally {
			blocking = false;
		}
	}

	private void doExecuteQuery(final IProgressMonitor monitor, String sql) throws InvocationTargetException
	{
		try {
			monitor.beginTask("Executing sql query", 4);
			DatabaseConnection connection = getQueryEditorInput().getConnection();
			final SQLQuery query = connection.getDatabase().executeQuery(sql);
			monitor.worked(3);
			Display.getDefault().asyncExec(new Runnable() {
				public void run()
				{
					queryFinished(query);
					monitor.worked(1);
					monitor.done();
				}
			});
		}
		catch(final Exception e) {
			// OK, SO NOW DISPLAY LABEL WITH SOME ERROR TEXT
			Display.getDefault().asyncExec((new Runnable() {
				public void run()
				{
					queryFailed(e);
					monitor.done();
				}
			}));
		}
	}

	/**
	 * This function displays failed query message
	 * @param e is the query exception to display
	 */
	private void queryFailed(Exception e)
	{
		if(viewer != null) {
			destoryViewer();
		}
		if(textField == null) {
			initializeTextField();
		}
		textField.setText("Error message from server:\n\n" + e.getLocalizedMessage());
	}

	/**
	 * This function destroys the table viewer
	 *
	 */
	private void destoryViewer()
	{
		table.dispose();
		table = null;
		viewer = null;
	}

	private void initializeTextField()
	{
		textField = new Text(form, SWT.WRAP);
		textField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		form.setWeights(new int[] {1,4});
	}

	private void destroyTextField()
	{
		textField.dispose();
		textField = null;
	}

	private void createViewer()
	{
		table = createDataTable(form);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		form.setWeights(new int[] {1,4});
	}

	/**
	 * This function displays query result in the result table.
	 * @param query is the query result
	 */
	private void queryFinished(SQLQuery query)
	{
		if(query.columnCount() == 0) {
			if(viewer != null) {
				destoryViewer();
			}
			if(textField == null) {
				initializeTextField();
			}
			textField.setText("Query ok\n\nUpdated rows: TODO");
		}
		else {
			if(textField != null) {
				destroyTextField();
			}
			if(viewer == null) {
				createViewer();
			}
			// SETUP COLUMNS
			setupColumns(query);
			// SETUP RESULT
			labelProvider.changeInput(query);
			viewer.setInput(query);
		}
		blocking = false;
	}

	/**
	 * Setups columns in the table.
	 * @param query
	 */
	private void setupColumns(SQLQuery query)
	{
		int cols = query.columnCount();
		Table table = viewer.getTable();
		TableColumn[] tblCols = table.getColumns();
		for(int i = 0; i < tblCols.length; ++i) {
			tblCols[i].dispose();
		}
		//    String[] props;
		//    viewer.setColumnProperties(props);
		for(int i = 0; i < cols; ++i) {
			String colName = query.getColumnName(i);
			TableColumn col = new TableColumn(table, SWT.LEFT);

			// COMPUTE WIDTH OF CHARACTERS IN THE COLUMN. MAXIMAL WIDTH SHOULD BE 35
			// CHARACTERS
			int chars = guessCharsInColumn(query, table, i);
			int width = SWTUtil.computeWidthOfChars(viewer.getTable(), chars);
			col.setWidth(width);

			col.setMoveable(false);
			col.setResizable(true);
			col.setText(colName);
		}
	}

	/**
	 * Guesses number of characters we should display in some column. This means
	 * that this column will be as wide.
	 */
	private int guessCharsInColumn(SQLQuery query, Table table, int column)
	{
		int chars = query.getColumnSize(column);
		int type = query.getColumnSqlType(column);
		if(SQLUtil.isNumericType(type)) {
			if(chars > 8)
				chars = 7;
		}
		else if(SQLUtil.isStringType(type)) {
			if(chars > 20) {
				// NOW WE DETERMINE HOW MANY CHARACTERS WILL BE DISPLAYED BY WIDTH IN
				// THE FIRST PAGE OF LISTING
				int items = table.getSize().y / table.getItemHeight();
				if(items > query.countRows())
					items = query.countRows();
				int maxLen = 20;
				for(int j = 0; j < items; ++j) {
					String s;
					try {
						s = query.valueFromRowHandle(j, column);
						maxLen = Math.max(maxLen, s.length());
					}
					catch(VortexDatabaseException e) {
						VortexPlugin.log(e);
					}
				}
				chars = maxLen;
			}
		}
		else if(chars > 40) {
			chars = 40;
		}
		return chars;
	}

	public void setFocus()
	{
		editor.setFocus();
	}

	public void dispose()
	{
		redColor.dispose();
		redColor = null;
		super.dispose();
	}
}
