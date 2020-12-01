/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 21, 2006
 * 
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.swt;

import java.net.URL;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.LineNumberRulerColumn;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColorCellEditor;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.HyperlinkSettings;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import puakma.utils.lang.ArrayUtils;
import puakma.utils.lang.StringUtil;
import puakma.vortex.VortexPlugin;

/**
 * This class is supposed to be extensible builder for dialog with standard user
 * interface, and also with forms user interface style. This is enhancement over
 * NewLFPage and DialogBuilder so users don't need to subclass those classes,
 * and also this class is not a subclass of Composite so you have more freedom
 * over creating your user interface stuff. Another advantage is that you are
 * able to select which kind of layout you want to use. This dialog builder uses
 * stack to determine what composite is currently used as a container.
 * 
 * @author Martin Novak
 */
public class DialogBuilder2
{
	private static class DefaultHyperlinkAdapter extends HyperlinkAdapter
	{
		public void linkActivated(HyperlinkEvent e) {
			try {
				Object obj = e.getHref();
				String str = (String) obj;
				URLStreamHandler handler = null;
				if(str.startsWith("custom://"))
					return;
				if(str.startsWith("prefs://") || str.startsWith("props://"))
					handler = new NullStreamHandler();
				URL url = new URL(null, str, handler);
				// PREFS PROTOCOL IS A SPECIAL THING FOR OPENING PREFERENCE PAGES
				if("prefs".equals(url.getProtocol())) {
					String prefPageId = url.getHost();
					PreferenceDialog dlg = PreferencesUtil.createPreferenceDialogOn(null, prefPageId,
							null, null);
					dlg.open();
				}
				else if("props".equals(url.getProtocol())) {
					String key = url.getPath();
					Object o = e.widget.getData(key);
					if(o instanceof IAdaptable) {
						IAdaptable element = (IAdaptable) o;
						String propPageId = url.getHost();
						PreferenceDialog dlg = PreferencesUtil.createPropertyDialogOn(null, element,
								propPageId, null, null);
						dlg.open();
					}
				}
				else {
					// TODO: maybe fileter it only to http, ftp
					IWorkbench wbench = PlatformUI.getWorkbench();
					IWorkbenchBrowserSupport bs = wbench.getBrowserSupport();
					bs.getExternalBrowser().openURL(url);
				}
			}
			catch(Exception e1) {
				VortexPlugin.log(e1);
			}
		}
	}

	private static final class CompositeInfo {
		Composite composite;
		int       columns;
		boolean   forms;
		
		CompositeInfo(Composite composite, int columns, boolean forms)
		{
			this.composite = composite;
			this.columns = columns;
			this.forms = forms;
		}
	}

	public static final class TableColumnInfo
	{
		public static final int CELL_MOD_NONE = 0;
		public static final int CELL_MOD_TEXT = 1;
		//    public static final int CELL_MOD_COMBO = 2;
		public static final int CELL_MOD_COLOR = 3;
		public static final int CELL_MOD_CHECK = 4;

		public String id;
		public String label;
		public int width;
		public int align;
		public int cellModifierType;
		public int index;
		private boolean resizable;
		public TableColumnInfo(String id, String label, int width)
		{
			this(id, label, width, CELL_MOD_NONE);
		}

		public TableColumnInfo(String id, String label, int width, int cellModifierType)
		{
			this(id, label, width, cellModifierType, true);
		}

		public TableColumnInfo(String id, String label, int width, int cellModifierType, boolean resizable)
		{
			this.id = id;
			this.label = label;
			this.width = width;
			this.cellModifierType = cellModifierType;
			this.resizable = resizable;
		}
	}

	/**
	 * This is true when OS is MacOSX
	 */
	private static final boolean IS_MAC = SWT.getPlatform().equals("carbon");

	/**
	 * Stack of parent composites
	 */
	private Stack<CompositeInfo> composites = new Stack<CompositeInfo>();
	
	/**
	 * This is the current composite we are using. This composite should not be on
	 * the top of the stack. If this is a root composite, stack should be empty.
	 */
	private CompositeInfo currentComposite;
	private FormToolkit toolkit;
	private List<Image> images = new ArrayList<Image>();
	private HyperlinkSettings hyperlinkSettings;

	public DialogBuilder2(Composite parent)
	{
		this(parent, 1);
	}

	public DialogBuilder2(Composite parent, int columns)
	{
		this(parent, columns, false, null);
	}

	public DialogBuilder2(Composite parent, int columns, boolean formsToolkit, FormToolkit toolkit)
	{
		this.currentComposite = new CompositeInfo(parent, columns, formsToolkit);

		this.toolkit = toolkit;
	}

	public DialogBuilder2(ScrolledForm form)
	{
		this.toolkit = new FormToolkit(Display.getDefault());
		Composite body = form.getBody();
		this.currentComposite = new CompositeInfo(body, 1, true);
	}

	public DialogBuilder2(Form form)
	{
		this.toolkit = new FormToolkit(Display.getDefault());
		Composite body = form.getBody();
		this.currentComposite = new CompositeInfo(body, 1, true);
	}

	/**
	 * Creates a forms look and feel composite.
	 * 
	 * <p>
	 * Note that this adds composite to the stack, so don't forget to close this
	 * forms composite.
	 * </p>
	 * 
	 * @param title is the title of the composite. If this parameter is null, no
	 *          title is being set.
	 * @param scrolling true if we should use scrolling composite for children
	 *          that doesn't fit
	 * @param cols is the number of children columns
	 * @return {@link Composite} object
	 */
	public Composite createFormsLFComposite(String title, boolean scrolling, int cols)
	{
		if(toolkit == null)
			toolkit = new FormToolkit(Display.getDefault());

		Composite form;
		if(scrolling)
			form = toolkit.createScrolledForm(currentComposite.composite);
		else
			form = toolkit.createForm(currentComposite.composite);

		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		form.setLayoutData(gd);
		Composite body;

		if(scrolling) {
			if(title != null)
				((ScrolledForm)form).setText(title);
			body = ((ScrolledForm)form).getBody();
		}
		else {
			if(title != null)
				((Form)form).setText(title);
			body = ((Form)form).getBody();
		}

		GridLayout gl = new GridLayout(cols, false);
		body.setLayout(gl);

		pushComposite(new CompositeInfo(body, cols, true));

		toolkit.paintBordersFor(body);
		return body;
	}

	private void pushComposite(CompositeInfo info)
	{
		composites.push(currentComposite);
		currentComposite = info;
	}

	public Composite createComposite()
	{
		return createComposite(1, 1);
	}

	public Composite createComposite(int columns)
	{
		return createComposite(columns, 1);
	}

	public Composite createComposite(int columns, int parentCols)
	{
		Composite c = new Composite(currentComposite.composite, SWT.NULL);

		// SETUP LAYOUT
		GridLayout gl = new GridLayout(columns, false);
		gl.marginHeight = gl.marginWidth = 0;
		c.setLayout(gl);

		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan = parentCols;
		c.setLayoutData(gd);

		// MOVE AROUND WITH STACK
		composites.push(currentComposite);
		currentComposite = new CompositeInfo(c, columns, currentComposite.forms);

		return c;
	}

	/**
	 * Creates a new group composite.
	 */
	public Group createGroup(String labelText, int childCols)
	{
		Group g = new Group(currentComposite.composite, SWT.SHADOW_IN);
		g.setText(labelText);

		GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false, currentComposite.columns, 1);
		g.setLayoutData(gd);

		GridLayout gl = new GridLayout(childCols, false);
		g.setLayout(gl);

		pushComposite(new CompositeInfo(g, childCols, currentComposite.forms));

		return g;
	}

	/**
	 * Closes the current group definition.
	 */
	public void closeGroup()
	{
		closeComposite();
	}

	/**
	 * Creates section on the form. Note that this function works only with
	 * dialogs with form toolkit. Note that this adds composite to the stack, so
	 * you have to close it using {@link #closeSection()}. This function uses
	 * just one parent column.
	 * 
	 * @param title is the section title
	 * @param description is the section description. If null or length of
	 *          description = 0 then no description is shown
	 * @param cols is the number of children columns
	 * @return Section object representing the section
	 */
	public Section createSection(String title, String description, int cols)
	{
		return createSection(title, description, cols, 1);
	}


	/**
	 * Creates section on the form. Note that this function works only with
	 * dialogs with form toolkit. Note that this adds composite to the stack, so
	 * you have to close it using {@link #closeSection()}.
	 * 
	 * @param title is the section title
	 * @param description is the section description. If null or length of
	 *          description = 0 then no description is shown
	 * @param cols is the number of children columns
	 * @param parentCols is the number of parent columns to fit in
	 * @return Section object representing the section
	 */
	public Section createSection(String title, String description, int cols, int parentCols)
	{
		int styles = ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR;
		boolean hasDescription = description != null && description.length() != 0;
		if(hasDescription)
			styles |= Section.DESCRIPTION;

		Section section = toolkit.createSection(currentComposite.composite, styles);

		GridLayout gl = new GridLayout();
		gl.numColumns = 1;
		gl.marginHeight = gl.marginWidth = 0;
		section.setLayout(gl);

		GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		gd.horizontalSpan = parentCols;
		section.setLayoutData(gd);

		section.setText(title);
		if(hasDescription)
			section.setDescription(description);
		section.setExpanded(true);

		// PUSH THE SECTION COMPOSITE
		composites.push(currentComposite);
		currentComposite = new CompositeInfo(section, cols, currentComposite.forms);

		// CREATE THE CLIENT COMPOSITE
		Composite client = toolkit.createComposite(section, SWT.NULL);
		GridLayout layout = new GridLayout(cols, false);
		layout.marginHeight = layout.marginWidth = 0;
		client.setLayout(layout);

		gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		client.setLayoutData(gd);

		composites.push(currentComposite);
		currentComposite = new CompositeInfo(client, cols, currentComposite.forms);

		section.setClient(client);

		return section;
	}

	/**
	 * This function pops up stack, so we have new parent composite.
	 */
	public void closeSection()
	{
		// POP TWICE SINCE WE ARE CLOSING SECTION, AND IT'S CLIENT
		currentComposite = (CompositeInfo) composites.pop();
		currentComposite = (CompositeInfo) composites.pop();
	}

	/**
	 * This function pops up stack, so we have new parent composite.
	 */
	public void closeComposite()
	{
		currentComposite = (CompositeInfo) composites.pop();

		// ALSO DISPOSE ALL IMAGES WHEN THE PARENT COMPOSITE CLOSES
		if(composites.size() == 1) {
			currentComposite.composite.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e)
				{
					Iterator<Image> it = images.iterator();
					while(it.hasNext()) {
						Image img = (Image) it.next();
						img.dispose();
					}
					images.clear();
				}
			});
		}
	}

	/**
	 * This function is now for checking that we have the proper count of
	 * composites in stack.
	 * 
	 * @throws IllegalStateException when there is some composite missing or not
	 *           poped from the stack
	 */
	public void finishBuilder()
	{
		if(composites.size() != 0)
			throw new IllegalStateException("There are still some composites left on the stack");
		if(currentComposite == null)
			throw new IllegalStateException("Parent composite has been poped from the stack");
	}

	/**
	 * Creates the row fully filled with check box.
	 */
	public Button createCheckboxRow(String text)
	{
		int style = SWT.CHECK;
		Button b = appendButton(text, style);
		GridData gd = (GridData) b.getLayoutData();
		gd.horizontalSpan = currentComposite.columns;
		return b;
	}

	public Button createButtonRow(String text)
	{
		return createButtonRow(text, SWT.PUSH);
	}

	public Button createButtonRow(String text, int style)
	{
		appendLabel("");
		return appendButton(text, style);
	}

	/**
	 * Creates a radio button with some label
	 */
	public Button createRadioButtonRow(String text)
	{
		int style = SWT.RADIO;
		return createButtonRow(text, style);
	}

	public Button createSingleToogleButton(String title)
	{
		Button b = appendButton(title, SWT.TOGGLE);

		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		//gd.horizontalSpan = currentComposite.columns > 1 ? currentComposite.columns -1 : 1;
		b.setLayoutData(gd);

		return b;
	}

	public Combo createComboRow(String label, boolean readOnly)
	{
		appendLabel(label);

		Combo c = appendCombo(readOnly);
		GridData gd = new GridData(SWT.FILL, SWT.BEGINNING, false, false);
		gd.horizontalSpan = currentComposite.columns > 1 ? currentComposite.columns -1 : 1;
		c.setLayoutData(gd);
		return c;
	}

	public Label createLabelRow(String label)
	{
		Label l = appendLabel(label);
		GridData gd = new GridData(SWT.FILL, SWT.TOP, false, false);
		gd.horizontalSpan = currentComposite.columns;
		l.setLayoutData(gd);
		return l;
	}

	public Label createSeparatorRow(boolean horizontal)
	{
		Label l;
		int style = SWT.SEPARATOR | (horizontal ? SWT.HORIZONTAL : SWT.VERTICAL);
		if(currentComposite.forms)
			l = toolkit.createLabel(currentComposite.composite, "", style);
		else {
			l = new Label(currentComposite.composite, style);
		}

		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, currentComposite.columns, 1);
		l.setLayoutData(gd);

		return l;
	}

	/**
	 * Sets up two labels row. The second label is supposed to be manipulated, so it's returned.
	 * 
	 * @param label1 is the first label caption
	 * @param label2 is the second (returned) label caption
	 * @return the second label
	 */
	public Label createTwoLabelRow(String label1, String label2)
	{
		// CREATE FIRST LABEL
		appendLabel(label1);
		// CREATE SECOND LABEL, AND RETURN IT
		Label l2 = appendLabel(label2);
		return l2;
	}

	//  public Hyperlink createHyperlinkRow(String text, String url)
	//  {
	//    Hyperlink hl = new Hyperlink(currentComposite.composite, SWT.NULL);
	//    hl.setText(text);
	//    hl.setToolTipText(url);
	//    
	//    
	//  }

	/**
	 * Creates a row with {@link Text} control and label.
	 * 
	 * @param label is the text label before {@link Text} control.
	 * @return {@link Text} control
	 */
	public Text createEditRow(String label)
	{
		return _createTextRow(label, false, 1);
	}

	/**
	 * This creates a row with {@link Text} control and label. The Text control
	 * accepts only numbers, nothing else.
	 * 
	 * @param label is the text label on the left side of {@link Text} control
	 * @return {@link Text} control
	 */
	public Text createNumericRow(String label)
	{
		Text t = createEditRow(label);
		SWTUtil.setIntValidation(t);
		return t;
	}

	/**
	 * Creates a multiline memo. This memo is based on {@link Text} control. On
	 * the left side is {@link Label} control with some text.
	 * 
	 * @param label is the text what appears on the label
	 * @param charsHeight is number of rows in the memo
	 * @return {@link Text} control
	 */
	public Text createMemoRow(String label, int charsHeight)
	{
		return _createTextRow(label, true, charsHeight);
	}

	/**
	 * Creates text/memo row.
	 * 
	 * @param label is the {@link Label} text
	 * @param multiline if true, we should make multiline control
	 * @param height number of rows in the multiline control
	 * @return {@link Text} control
	 */
	private Text _createTextRow(String label, boolean multiline, int height)
	{
		Label l = appendLabel(label);
		if(multiline) {
			GridData gd = (GridData) l.getLayoutData();
			gd.verticalAlignment = SWT.BEGINNING;
		}

		Text t;
		int style;
		if(multiline == false)
			style = SWT.BORDER;
		else
			style = SWT.WRAP | SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL;
		if(currentComposite.forms)
			t = toolkit.createText(currentComposite.composite, StringUtil.EMPTY_STRING, style);
		else {
			t = new Text(currentComposite.composite, style);
		}
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		if(multiline)
			gd.heightHint = SWTUtil.computeHeightOfChars(t, height);
		gd.horizontalSpan = currentComposite.columns > 1 ? currentComposite.columns -1 : 1;
		t.setLayoutData(gd);

		return t;
	}

	public Label createImage(String imgName)
	{
		return createImage(imgName, 1);
	}

	/**
	 * Creates a {@link Label} with some image from the icons directory.
	 * 
	 * @param imgName is the name of the file under image directory
	 * @param columns is number of columns which should be occupied by
	 *          {@link Label}. If columns == -1 then this adjusts image to all
	 *          columns in the current layout
	 */
	public Label createImage(String imgName, int columns)
	{
		if(columns == -1)
			columns = currentComposite.columns;

		Label l = appendLabel("");
		GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		gd.horizontalSpan = columns;
		l.setLayoutData(gd);
		ImageDescriptor d = VortexPlugin.getDefault().getImageDescriptor(imgName);
		Image img = d.createImage();
		// ADD IMAGES, ADN DISPOSE THEM WHEN THE TOP MOST COMPOSITE IS BEING DISPOSED
		images .add(img);
		l.setImage(img);
		return l;
	}

	public Table createTable()
	{
		Table table;
		int style = SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER;
		if(currentComposite.forms)
			table = toolkit.createTable(currentComposite.composite, style);
		else
			table = new Table(currentComposite.composite, style);

		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalSpan = currentComposite.columns;
		gd.heightHint = SWTUtil.computeHeightOfChars(table, 5);
		table.setLayoutData(gd);

		table.setHeaderVisible(true);
		return table;
	}

	/**
	 * Creates JFace {@link TableViewer} which is wrapped around the Table.
	 */
	public TableViewer createTableViewer()
	{
		Table table = createTable();
		TableViewer viewer = createTableViewer(table);

		return viewer;
	}

	public TableViewer createTableViewer(Table table)
	{
		TableViewer viewer = new TableViewer(table);

		viewer.setUseHashlookup(true);

		return viewer;
	}

	public static void setupTableViewer(TableViewer viewer, IContentProvider contentProvider,
			IBaseLabelProvider labelProvider,
			ViewerComparator comparator, ICellModifier cellModifier)
	{
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(labelProvider);
		viewer.setCellModifier(cellModifier);
		viewer.setComparator(comparator);
	}

	public static void setupTableColumns(TableViewer viewer, DialogBuilder2.TableColumnInfo[] columns)
	{
		Table table = viewer.getTable();
		String[] properties = new String[columns.length];
		boolean haveCellEditor = false;
		boolean haveProperties = false;
		CellEditor[] editors = new CellEditor[columns.length];

		for(int i = 0; i < columns.length; ++i) {
			TableColumn col = new TableColumn(table, SWT.NULL);
			col.setText(columns[i].label);
			columns[i].index = i;
			col.setResizable(columns[i].resizable);
			if(columns[i].width != 0)
				col.setWidth(columns[i].width);

			properties[i] = columns[i].id;
			if(properties[i] != null)
				haveProperties = true;

			switch(columns[i].cellModifierType) {
			case TableColumnInfo.CELL_MOD_TEXT:
				TextCellEditor editor = new TextCellEditor(table);
				editors[i] = editor;
				break;
			case TableColumnInfo.CELL_MOD_CHECK:
				editors[i] = new CheckboxCellEditor(table);
				break;
				// TODO: add support for combos
				//        case TableColumnInfo.CELL_MOD_COMBO:
				//          editors[i] = new ComboBoxCellEditor(table);
				//        break;
			case TableColumnInfo.CELL_MOD_COLOR:
				editors[i] = new ColorCellEditor(table);
				break;
			case TableColumnInfo.CELL_MOD_NONE:
				break;
			default:
				throw new IllegalArgumentException("Invalid cell modifier type: " + columns[i].cellModifierType);
			}
			if(columns[i].cellModifierType != TableColumnInfo.CELL_MOD_NONE)
				haveCellEditor = true;
		}

		//    if(haveCellEditor != haveProperties)
		//      throw new IllegalArgumentException("Properties, and cell editors doesn't correspond");

		if(haveProperties)
			viewer.setColumnProperties(properties);
		if(haveCellEditor)
			viewer.setCellEditors(editors);
	}

	/**
	 * This function creates JFace {@link TableViewer} object for table. Also
	 * setups {@link TableViewer} with content provider, label provider, and cell
	 * modifier.
	 * 
	 * @param table for which we build viewer
	 * @param contentProvider is the data provider for table
	 * @param labelProvider is the label provider for table
	 * @param columnProperties are the names of properties for editing. Note that
	 *          the indexes have to match indexes of columns
	 * @param cellMod is the cell modifier for the table
	 * @param editors is array with cell editors, so user can use them to edit
	 *          cells
	 * @return {@link TableViewer} object for table paramenter
	 */
	public TableViewer setupTableViewer(Table table,
			IStructuredContentProvider contentProvider,
			ITableLabelProvider labelProvider,
			String[] columnProperties, ICellModifier cellMod,
			CellEditor[] editors)
	{
		TableViewer viewer = createTableViewer(table);

		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(labelProvider);
		viewer.setCellModifier(cellMod);
		viewer.setCellEditors(editors);
		viewer.setColumnProperties(columnProperties);

		return viewer;
	}

	/**
	 * Creates a tree.
	 */
	public Tree createTree()
	{
		int style = SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL;
		boolean showHeader = false;
		return createTree(style, showHeader);
	}

	public Tree createTree(int style, boolean showHeader)
	{
		Tree tree;
		if(currentComposite.forms)
			tree = toolkit.createTree(currentComposite.composite, style);
		else
			tree = new Tree(currentComposite.composite, style);

		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalSpan = currentComposite.columns;
		tree.setLayoutData(gd);

		tree.setHeaderVisible(true);
		return tree;
	}

	/**
	 * Creates a new toolbar which fills the whole row. Don't forget to close the
	 * toolbar when you are done with toolbar button using {@link #closeToolbar()}.
	 */
	public ToolBar createToolbarRow()
	{
		int style = SWT.FLAT;
		ToolBar tb = new ToolBar(currentComposite.composite, style);

		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		tb.setLayoutData(gd);

		pushComposite(new CompositeInfo(tb, 1, currentComposite.forms));

		return tb;
	}

	/**
	 * Closes the toolbar composite, and removes it from the stack.
	 */
	public void closeToolbar()
	{
		closeComposite();
	}

	/**
	 * Appends the standard toolbar button to the toolbar control. Note that any
	 * of the parameters might be null which means that you don't want to set it.
	 */
	public ToolItem appendToolbarButton(String text, String tooltip, String imageName)
	{
		ToolItem item = appendToolbarButton(text, tooltip, imageName, SWT.PUSH);
		return item;
	}

	/**
	 * Appends separator button on {@link ToolBar} control.
	 */
	public ToolItem appendToolbarSeparator()
	{
		ToolItem item = appendToolbarButton(null, null, null, SWT.SEPARATOR);
		return item;
	}

	/**
	 * Appends the standard toolbar button to the toolbar control. Note that any
	 * of the parameters might be null which means that you don't want to set it.
	 * Image is taken from {@link VortexPlugin} image cache.
	 */
	public ToolItem appendToolbarButton(String text, String tooltip, String imageName, int style)
	{
		if(currentComposite.composite instanceof ToolBar == false)
			throw new IllegalStateException("The current composite is not toolbar");

		ToolBar tb = (ToolBar) currentComposite.composite;
		ToolItem item = new ToolItem(tb, style);
		if(text != null)
			item.setText(text);
		if(tooltip != null)
			item.setToolTipText(tooltip);
		if(imageName != null) {
			Image image = VortexPlugin.getDefault().getImage(imageName);
			item.setImage(image);
		}
		return item;
	}

	public CTabFolder createTabView()
	{
		int style = SWT.BOTTOM | SWT.BORDER | SWT.FLAT;
		CTabFolder folder = new CTabFolder(currentComposite.composite, style);
		folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		pushComposite(new CompositeInfo(folder, 1, currentComposite.forms));

		return folder;
	}

	/**
	 * This closes created tab folder.
	 */
	public void closeTabFolder()
	{
		closeComposite();
	}

	/**
	 * This function creates a tab item. Also creates a Composite there, so you
	 * can add your own controls to the tab item imediatelly
	 * 
	 * @param title is the text which appears on the tab
	 * @param columns is the number of columns in the children composite
	 * @return {@link CTabItem} object
	 */
	public CTabItem createTabItem(String title, int columns)
	{
		if(currentComposite.composite instanceof CTabFolder)
			throw new IllegalStateException("If you want to create a tab item, you have to create it under" +
					"tab folder!");

		CTabFolder tabFolder = (CTabFolder) currentComposite.composite;
		CTabItem item = new CTabItem(tabFolder, SWT.CLOSE);
		item.setText(title);

		// WE ALSO PROMISED TO CREATE COMPOSITE, SO USER COULD ADD THEIR OWN ITEMS BY THEMSELVES
		createComposite(columns);

		return item;
	}

	public void closeTabItem()
	{
		closeComposite();
	}

	public SashForm createSashForm(boolean horizontal)
	{
		int styles = SWT.SMOOTH;
		if(horizontal)
			styles |= SWT.HORIZONTAL;
		else
			styles |= SWT.VERTICAL;

		SashForm form = new SashForm(currentComposite.composite, styles);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		form.setLayoutData(gd);

		//  MOVE AROUND WITH STACK
		composites.push(currentComposite);
		currentComposite = new CompositeInfo(form, 1, currentComposite.forms);

		return form;
	}

	public void closeSashForm()
	{
		closeComposite();
	}

	/**
	 * Creates a new {@link ProgressBar} control on the parent composite.
	 * 
	 * @param horizontal if true then progress bar is horizontally oriented
	 * @return {@link ProgressBar} instance
	 */
	public ProgressBar createProgressBarRow(boolean horizontal)
	{
		int style = 0;
		if(horizontal)
			style |= SWT.HORIZONTAL;
		else
			style |= SWT.VERTICAL;
		ProgressBar bar = new ProgressBar(currentComposite.composite, style);
		if(horizontal)
			bar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		else
			bar.setLayoutData(new GridData(SWT.TOP, SWT.FILL, false, true));
		return bar;
	}

	/**
	 * This function sets up buttons, so they behave as radio buttons.
	 * 
	 * @param buttons is the array of buttons to setup this way
	 */
	public void setupToogleButtonsAsRadios(final Button[] buttons)
	{
		SelectionListener listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e)
			{
				for(int i = 0; i < buttons.length; ++i)
					buttons[i].setSelection(false);

				((Button) e.widget).setSelection (true);
			}
		};

		for(int i = 0; i < buttons.length; ++i)
			buttons[i].addSelectionListener(listener);
	}

	/**
	 * Creates a {@link FormText} control. Also installs on this control the
	 * default hyperlink handler. This hyperlink handler will open browser or
	 * external resource on request. Please note that if user want to enter text
	 * here, it's possible security shortcomming. Also there is special protocol
	 * "prefs" which opens preference page, so:
	 * <code>prefs://org.eclipse.jdt.debug.ui.preferences.VMPreferencePage</code>
	 * should open Eclipse's JRE configuration page.
	 * 
	 * @param text is the xml markup text which shows in the {@link FormText}
	 *          control
	 * @return {@link FormText} control
	 */
	public FormText createFormText(String text)
	{
		FormText ft;
		if(currentComposite.forms)
			ft = toolkit.createFormText(currentComposite.composite, false);
		else {
			ft = new FormText(currentComposite.composite, SWT.NULL);
		}

		if(ft.getHyperlinkSettings() == null)
			ft.setHyperlinkSettings(getHyperlinkSettings());
		ft.setText(text, true, true);

		ft.addHyperlinkListener(new DefaultHyperlinkAdapter());

		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan = currentComposite.columns;
		ft.setLayoutData(gd);

		return ft;
	}

	public HyperlinkSettings getHyperlinkSettings()
	{
		if(hyperlinkSettings == null)
			hyperlinkSettings = new HyperlinkSettings(Display.getDefault());
		return hyperlinkSettings;
	}

	/**
	 * Creates a {@link SourceViewer} object which behaves as a standard eclipse
	 * editor, so there is a control on the right side what shows line numbers.
	 */
	public SourceViewer createSourceViewer()
	{
		LineNumberRulerColumn ruler = new LineNumberRulerColumn();
		AnnotationModel m = new AnnotationModel();
		ruler.setModel(m);
		CompositeRuler cr = new CompositeRuler();
		cr.addDecorator(0, ruler);
		Document doc = new Document();
		SourceViewer v = new SourceViewer(currentComposite.composite, cr, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		v.setDocument(doc);

		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		v.getControl().setLayoutData(gd);
		return v;
	}

	public ProjectionViewer createProjectionViewer(Composite parent, IOverviewRuler overviewRuler,
			boolean showAnnotationOverview)
	{
		LineNumberRulerColumn ruler = new LineNumberRulerColumn();
		AnnotationModel m = new AnnotationModel();
		ruler.setModel(m);
		CompositeRuler cr = new CompositeRuler();
		cr.addDecorator(0, ruler);

		int styles = SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER;
		ProjectionViewer v = new ProjectionViewer(parent, cr, overviewRuler, showAnnotationOverview, styles);

		Document doc = new Document();
		v.setDocument(doc);

		return v;
	}

	//  protected IOverviewRuler createOverviewRuler(ISharedTextColors sharedColors)
	//  {
	//    int VERTICAL_RULER_WIDTH = 12;
	//    IOverviewRuler ruler = new OverviewRuler(new DefaultMarkerAnnotationAccess(),
	//                                             VERTICAL_RULER_WIDTH, sharedColors);
	//    Object fAnnotationPreferences = EditorsPlugin.getDefault().getMarkerAnnotationPreferences();
	//    Iterator e = fAnnotationPreferences.getAnnotationPreferences().iterator();
	//    while(e.hasNext()) {
	//      AnnotationPreference preference = (AnnotationPreference) e.next();
	//      if(preference.contributesToHeader())
	//        ruler.addHeaderAnnotationType(preference.getAnnotationType());
	//    }
	//    return ruler;
	//}

	/**
	 * Creates a new row with {@link Label}, {@link Text}, and {@link Button} to
	 * select file or directory. This allows to choose single file only.
	 * 
	 * @param labelText is the text on the label right to the {@link Text} control
	 * @param defaultPath is the default path to which points {@link Text}
	 *          control, and also dialog is initiated with this path
	 * @param isOpen if true it is open dialog, if false it is save dialog
	 * @param dialogTitle is the title on the open/save dialog window
	 * @param filterExtsNames file names filter. Array of two dimensional arrays
	 *          with the extensions on the first place, and name of the filter on
	 *          the second place. This is ignored when choosing directory.
	 */
	public Text createFileSelectionRow(String labelText, final String defaultPath,
			final boolean isOpen, final String dialogTitle,
			final String[][] filterExtsNames)
	{
		appendLabel(labelText);

		createComposite(2);
		final Text t = appendEdit(defaultPath);
		Button b = appendButton("Choose...");
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Composite currentComposite = getCurrentComposite();
				Shell shell = currentComposite.getShell();
				String fileName = chooseFile(shell, dialogTitle, defaultPath, filterExtsNames, isOpen);
				if(fileName != null)
					t.setText(fileName);
			}
		});
		closeComposite();

		return t;
	}

	private static String chooseFile(Shell shell, String dlgTitle, String initialPath,
			String[][] filterExtsNames, boolean isOpen)
	{
		String[] filterNames = (String[]) ArrayUtils.createArrayFromSubIndex(filterExtsNames, 0);
		String[] extensions = (String[]) ArrayUtils.createArrayFromSubIndex(filterExtsNames, 1);;

		int params = isOpen ? SWT.OPEN : SWT.SAVE;
		FileDialog dlg = new FileDialog(shell, params);
		//dlg.setFilterPath(initialPath);
		dlg.setText(dlgTitle);
		dlg.setFilterNames(filterNames);
		dlg.setFilterExtensions(extensions);
		dlg.setFileName(initialPath);
		return dlg.open();
	}

	/*
	private static String chooseDirectory(Shell shell, String initialPath, String dlgTitle)
	{
		DirectoryDialog dlg = new DirectoryDialog(shell, SWT.OPEN);
		dlg.setFilterPath(initialPath);
		dlg.setText(dlgTitle);
		dlg.setMessage(dlgTitle);
		String result = dlg.open();
		return result;
	}
	 */

	public Label appendLabel(String label)
	{
		return appendLabel(label, false);
	}

	public Label appendLabel(String label, boolean wrap)
	{
		Label l;
		int style = wrap ? SWT.WRAP : SWT.NONE;
		if(currentComposite.forms)
			l = toolkit.createLabel(currentComposite.composite, label, style);
		else {
			l = new Label(currentComposite.composite, SWT.NONE);
			l.setText(label);
		}

		GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
		l.setLayoutData(gd);

		return l;
	}

	public Combo appendCombo(boolean readOnly)
	{
		int style = readOnly ? SWT.READ_ONLY : SWT.NONE;
		Combo c = new Combo(currentComposite.composite, style);
		GridData gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		c.setLayoutData(gd);
		return c;
	}

	public Button appendButton(String text)
	{
		return appendButton(text, SWT.PUSH);
	}

	public Button appendCheckbox(String text)
	{
		int style = SWT.CHECK;
		Button b = appendButton(text, style);
		return b;
	}

	public Button appendButton(String text, int style)
	{
		Button b;
		if(currentComposite.forms)
			b = toolkit.createButton(currentComposite.composite, text, style);
		else {
			b = new Button(currentComposite.composite, style);
			b.setText(text);
		}
		GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		b.setLayoutData(gd);
		return b;
	}

	/**
	 * Appends new {@link Text} control which has border, and is single line.
	 */
	public Text appendEdit(String defaultText)
	{
		int style = SWT.SINGLE | SWT.BORDER;
		return appendEdit(defaultText, style);
	}

	/**
	 * Appends new {@link Text} control which has border, and behaves as a
	 * multiline memo control.
	 */
	public Text appendMemo(String defaultText)
	{
		int style = SWT.MULTI | SWT.BORDER;
		return appendEdit(defaultText, style);
	}

	/**
	 * Appends a new {@link Text} control with the custom style.
	 */
	public Text appendEdit(String defaultText, int style)
	{
		Text t;
		if(currentComposite.forms)
			t = toolkit.createText(currentComposite.composite, defaultText, style);
		else {
			t = new Text(currentComposite.composite, style);
			t.setText(defaultText);
		}

		int verticalAlign = ((style & SWT.MULTI) == SWT.MULTI) ? SWT.FILL : SWT.CENTER;
		GridData gd = new GridData(SWT.FILL, verticalAlign, true, false);
		t.setLayoutData(gd);
		return t;
	}

	public org.eclipse.swt.widgets.List appendList()
	{
		return appendList(false);
	}

	public org.eclipse.swt.widgets.List appendList(boolean multiSelection)
	{
		int style = SWT.BORDER | (multiSelection ? SWT.MULTI : SWT.SINGLE);
		return appendList(style);
	}

	public org.eclipse.swt.widgets.List appendList(int style)
	{
		org.eclipse.swt.widgets.List l;
		l = new org.eclipse.swt.widgets.List(currentComposite.composite, style);

		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		l.setLayoutData(gd);
		return l;
	}

	/**
	 * Appends hyperlink to the current {@link Composite}.
	 * 
	 * @param label is the text on the hyperlink
	 * @param addDefaultHandler if true then the default hyperlink handler is
	 *          added
	 */
	public Hyperlink appendHyperlink(String label, boolean addDefaultHandler)
	{
		Hyperlink hl;
		int style = SWT.NONE;
		if(currentComposite.forms)
			hl = toolkit.createHyperlink(currentComposite.composite, label, style);
		else {
			hl = new Hyperlink(currentComposite.composite, style);
			hl.setText(label);
		}

		if(addDefaultHandler)
			hl.addHyperlinkListener(new DialogBuilder2.DefaultHyperlinkAdapter());

		GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		hl.setLayoutData(gd);
		return hl;
	}

	/**
	 * Gets the actual {@link Composite} being used as a parent element for newly
	 * created children.
	 */
	public Composite getCurrentComposite()
	{
		return currentComposite.composite;
	}

	/**
	 * Gets the form toolkit. Returns null if the {@link FormToolkit} has not been
	 * initialized yet.
	 */
	public FormToolkit getFormToolkit()
	{
		return toolkit;
	}

	/**
	 * Gets the current section. Note that {@link Section} had to be the last
	 * container we have done.
	 */
	public Section getCurrentSection()
	{
		CompositeInfo info = (CompositeInfo) composites.get(composites.size() - 1);
		return (Section) info.composite;
	}

	public void setEffectiveColumns(int columns)
	{
		currentComposite.columns = columns;
	}

	public int getEffectiveColumns()
	{
		return currentComposite.columns;
	}

	/**
	 * Returns image of the checkbox for the current platform.
	 */
	public static Image getCheckboxButtonImage(boolean checked)
	{
		String key = checked ? "checked" : "unchecked";
		if(IS_MAC)
			key += "-mac";
		// TODO: add support for other platforms
		key += ".png";

		Image image = VortexPlugin.getDefault().getImage(key);
		return image;
	}

	public static void hookToolbarManaegmentMenu(final ToolBar tb)
	{
		final Menu menu = new Menu(tb);
		final MenuItem mIconsText = new MenuItem(menu, SWT.CHECK);
		mIconsText.setText("Icons and Text");
		final MenuItem mIcons = new MenuItem(menu, SWT.CHECK);
		mIcons.setText("Icons Only");
		final MenuItem mText = new MenuItem(menu, SWT.CHECK);
		mText.setText("Text Only");

		//    ToolBarManager man = new ToolBarManager();
		//    man.createControl(c);
		//    man.getItems()[0].

		SelectionAdapter sa = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Widget w = e.widget;
				if(w == mIconsText) {

				}
				else if(w == mIcons) {

				}
				else if(w == mText) {

				}

				ToolItem[] items = tb.getItems();
				for(int i = 0; i < items.length; ++i) {
					//items[i].set
				}
				super.widgetSelected(e);
			}
		};

		mIconsText.addSelectionListener(sa);
		mIcons.addSelectionListener(sa);
		mText.addSelectionListener(sa);

		tb.setMenu(menu);
	}
}
