/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Aug 11, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.dialogs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import puakma.coreide.ServerManager;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.ApplicationObject;
import puakma.coreide.objects2.DesignObject;
import puakma.utils.lang.StringUtil;
import puakma.vortex.VortexPlugin;
import puakma.vortex.WorkbenchUtils;
import puakma.vortex.swt.SWTUtil;

/**
 * The dialog for selecting, and opening the design object among all opened
 * applications.
 *
 * @author Martin Novak
 */
public class DesignObjectLoooookupDialog extends Dialog
{
	private static final String LOOKUP_DLG_SETTINGS = "puakma.vortex.DesignObjectLoooooookupDialogSettings";
	private static final String WIDTH = "width";
	private static final String HEIGHT = "height";
	private static final String DIALOG_ID = "DesignObjectLoooookupDialog";

	private Text input;
	private TableViewer viewer;
	private ObjectsProvider contentProvider = new ObjectsProvider();
	private IDialogSettings settings;
	//private Point location;
	//private Point size;
	private ObjectsSorter sorter = new ObjectsSorter();
	private InternalLabelProvider labelProvider = new InternalLabelProvider();
	private ISelectionChangedListener selListener = new ISelectionChangedListener(){
		public void selectionChanged(SelectionChangedEvent event)
		{
			// ENABLE/DISABLE OK BUTTON
			int selCount = viewer.getTable().getSelectionCount();
			boolean enabled = selCount > 0;
			getButton(IDialogConstants.OK_ID).setEnabled(enabled);
		}
	};
	class InternalLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex)
		{
			if(element instanceof DesignObject)
				return ((DesignObject)element).getName();
			else
				return "##ERROR##";
		}
		public Image getColumnImage(Object element, int columnIndex)
		{
			if(element instanceof DesignObject)
				return WorkbenchUtils.getImageFromCache((ApplicationObject) element);
			else
				return null;
		}
	}
	private class ObjectsSorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2)
		{
			int cat1 = category(e1);
			int cat2 = category(e2);

			if (cat1 != cat2)
				return cat1 - cat2;

			// cat1 == cat2

			String name1;
			String name2;

			if (viewer == null || !(viewer instanceof ContentViewer)) {
				name1 = e1.toString();
				name2 = e2.toString();
			} else {
				IBaseLabelProvider prov = ((ContentViewer) viewer)
						.getLabelProvider();
				if (prov instanceof ILabelProvider) {
					ILabelProvider lprov = (ILabelProvider) prov;
					name1 = lprov.getText(e1);
					name2 = lprov.getText(e2);
				} else {
					name1 = e1.toString();
					name2 = e2.toString();
				}
			}
			if (name1 == null)
				name1 = "";//$NON-NLS-1$
				if (name2 == null)
					name2 = "";//$NON-NLS-1$
					return name1.compareToIgnoreCase(name2);
					//      return collator.compare(name1, name2);
		}
	}
	private class ObjectsProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement)
		{
			return loooookupDesignObjects(inputElement);
		}

		public void dispose()
		{
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
		{
		}

	}

	public DesignObjectLoooookupDialog(Shell parent)
	{
		super(parent);

		setShellStyle(getShellStyle() | SWT.RESIZE);
		IDialogSettings _settings = VortexPlugin.getDefault().getDialogSettings();
		settings = _settings.getSection(LOOKUP_DLG_SETTINGS);
		if(settings == null) {
			settings= new DialogSettings(LOOKUP_DLG_SETTINGS);
			_settings.addSection(settings);
			settings.put(WIDTH, 480);
			settings.put(HEIGHT, 320);
		}
	}

	public Object[] loooookupDesignObjects(Object inputElement)
	{
		String text = (String) inputElement;
		if(text.length() == 0 || "*".equals(text))
			return new Object[0];

		List<DesignObject> l = new ArrayList<DesignObject>();
		Application[] apps = ServerManager.listConnectedApplications();
		for(int i = 0; i < apps.length; ++i) {
			DesignObject[] list = apps[i].listDesignObjects();
			for(int j = 0; j < list.length; ++j) {
				// FILTER NON MATCHING OBJECTS, AND ALSO JAVA FILES WHICH ARE NOT ACTIONS,
				// ETC...
				if(StringUtil.matchWildcardIgnoreCase(list[j].getName(), text) &&
						list[j].getDesignType() != DesignObject.TYPE_LIBRARY) {
					l.add(list[j]);
				}
			}
		}

		return l.toArray();
	}

	protected Control createDialogArea(Composite parent)
	{
		Composite area = (Composite)super.createDialogArea(parent);

		getShell().setText("Open Design Object");

		input = new Text(area, SWT.SINGLE | SWT.BORDER);
		GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		input.setLayoutData(gd);
		input.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e)
			{
				viewer.setInput(input.getText());
				viewer.getTable().setSelection(0);
				selListener.selectionChanged(null);
			}
		});
		input.addKeyListener(new KeyListener() {
			public void keyReleased(KeyEvent e) {  }
			public void keyPressed(KeyEvent e)
			{
				if(e.keyCode == SWT.ARROW_DOWN)
					viewer.getTable().setFocus();
			}
		});
		Label l = new Label(area, SWT.NULL);
		l.setText("Matching design objects:");

		Table table = new Table(area, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
		table.setFont(getShell().getFont());
		table.setHeaderVisible(false);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint = SWTUtil.computeWidthOfChars(table, 70);
		gd.heightHint= SWTUtil.getTableHeightHint(table, 10);
		table.setLayoutData(gd);
		viewer = new TableViewer(table);
		// SETUP VIEWER
		viewer.setLabelProvider(labelProvider);
		viewer.setContentProvider(contentProvider);
		viewer.setSorter(sorter);
		viewer.setUseHashlookup(true);
		viewer.addSelectionChangedListener(selListener);

		// INITIALIZE DATA
		viewer.setInput(input.getText());

		return area;
	}

	/**
	 * Override the buttons to have "Open" button instead of "Ok" button, but ids
	 * will be the same.
	 */
	protected void createButtonsForButtonBar(Composite parent)
	{
		Button ok = createButton(parent, IDialogConstants.OK_ID, "Open", true);
		ok.setEnabled(false);

		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	protected IDialogSettings getDialogBoundsSettings()
	{
		IDialogSettings settings = VortexPlugin.getDefault().getDialogSettings();
		IDialogSettings ret = settings.getSection(DIALOG_ID);
		if(ret == null)
			ret = settings.addNewSection(DIALOG_ID);
		return ret;
	}

	protected void okPressed()
	{
		// OPEN ALL SELECTED ITEMS
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		if(selection.size() > 0) {
			Iterator<?> it = selection.iterator();
			while(it.hasNext()) {
				Object o = it.next();
				if(o instanceof DesignObject) {
					WorkbenchUtils.openDesignObject((DesignObject) o);
				}
			}
		}
		else {
			// OR TRY TO LOAD THE FIRST ITEM IN THE LIST
			Object[] objs = loooookupDesignObjects(input.getText());

			if(objs.length > 0)
				WorkbenchUtils.openDesignObject((DesignObject) objs[0]);
		}

		super.okPressed();
	}
}
