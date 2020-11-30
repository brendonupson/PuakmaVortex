/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 23, 2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.swt;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;

import puakma.utils.lang.CollectionsUtil;

public class TabbedPropertiesController
{
	public static final String PROP_DIRTY = "#dirtyStatus#";
	private static final String KEY_PROPS_PAGE = "vortex.properties.page";
	private Map<Class, PropertiesPage[]> clazzToPropsMap = new HashMap();
	private CTabFolder tabView;
	private Class currentClazz;
	private ArrayList<PropertiesPage> currentPages = new ArrayList<PropertiesPage>();
	private int selectedIndex = -1;
	private Object oldInput;
	private boolean dirty;
	/**
	 * The listener which listens to the dirty status.
	 */
	private PropertyChangeListener dirtyListener;

	public TabbedPropertiesController(Composite parent)
	{
		DialogBuilder2 builder = new DialogBuilder2(parent);

		tabView = builder.createTabView();

		// AT THE CHANGE OF THE PAGE SAVE THE OLD PAGE
		tabView.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e)
			{
				// SAVE OLD PAGE
				int newIndex = tabView.getSelectionIndex();
				if(selectedIndex != -1) {
					PropertiesPage page = (PropertiesPage) tabView.getItem(selectedIndex).getData(KEY_PROPS_PAGE);
					page.save();
					selectedIndex = newIndex;
				}
			}
		});
	}

	/**
	 * This function adds properties pages to the controller
	 *
	 * @param clazz
	 * @param pages
	 */
	public void assignModelClassWithPages(Class clazz, PropertiesPage[] pages)
	{
		clazzToPropsMap.put(clazz, pages);
	}

	public void setInput(Object input)
	{
		if(input == oldInput)
			return;

		oldInput = input;

		if(input == null) {
			savePages();
			resetPages();
			disposeChildren();
			currentClazz = null;
			return;
		}

		Class clazz = input.getClass();
		PropertiesPage[] pages = (PropertiesPage[]) clazzToPropsMap.get(clazz);
		X: if(pages == null) {
			// TRY SLOWER READING
			Iterator it = clazzToPropsMap.keySet().iterator();
			while(it.hasNext()) {
				Class c = (Class) it.next();
				if(c.isAssignableFrom(clazz)) {
					pages = (PropertiesPage[]) clazzToPropsMap.get(c);
					break X;
				}
			}

			throw new IllegalStateException("Object not registered");
		}

		// IF THERE ARE SOME PAGES, RECREATE THEM
		if(currentClazz != null) {
			savePages();
			resetPages();
		}

		// IF THE CURRENT CLAZZ OF THE MODEL IS THE SAME AS THE PREVIOUS ONE, DON'T DiSPOSE AND
		// RECREATE UI.
		if(clazz != currentClazz || currentClazz == null) {
			disposeChildren();
			fillNewPages(pages);
			createUIForClazz(clazz);
		}

		currentClazz = clazz;
		setupChildren(input);
	}

	private void fillNewPages(PropertiesPage[] pages)
	{
		System.out.println("PROPERTIES::FILLNEWPAGES");

		currentPages.clear();

		// NOW FILL CURRENT PAGES
		CollectionsUtil.addArrayToList(currentPages, pages);
	}

	/**
	 * This function saves the current page.
	 */
	public void savePages()
	{
		CTabItem item = tabView.getSelection();
		if(item == null)
			return;

		System.out.println("PROPERTIES::SAVE");

		PropertiesPage page = (PropertiesPage) item.getData(KEY_PROPS_PAGE);
		page.save();
	}

	private void resetPages()
	{
		System.out.println("PROPERTIES::RESET");

		Iterator it = currentPages.iterator();
		while(it.hasNext()) {
			PropertiesPage page = (PropertiesPage) it.next();
			page.reset();
		}
	}

	/**
	 * Disposes all children, and also clears currentPages array.
	 */
	private void disposeChildren()
	{
		System.out.println("PROPERTIES::DISPOSECHILDREN");

		Iterator it = currentPages.iterator();
		while(it.hasNext()) {
			PropertiesPage page = (PropertiesPage) it.next();
			page.dispose();
		}
		currentPages.clear();

		tabView.setSelection(tabView.getItemCount() - 1);
		CTabItem[] items = tabView.getItems();
		for(int i = 0; i < items.length; ++i) {
			items[i].dispose();
		}
	}

	private void createUIForClazz(Class clazz)
	{
		System.out.println("PROPERTIES::CREATEUI");

		Iterator it = currentPages.iterator();
		while(it.hasNext()) {
			CTabItem item = new CTabItem(tabView, SWT.NULL);
			PropertiesPage page = (PropertiesPage) it.next();
			item.setData(KEY_PROPS_PAGE, page);
			item.setText(page.getName());
			Composite c = page.createComposite(tabView);
			item.setControl(c);
		}

		if(currentPages.size() > 0) {
			tabView.setSelection(0);
			selectedIndex = 0;
		}
		else
			selectedIndex = -1;
	}

	private void setupChildren(Object model)
	{
		System.out.println("PROPERTIES::SETUPUI");

		Iterator it = currentPages.iterator();
		while(it.hasNext()) {
			PropertiesPage page = (PropertiesPage) it.next();
			page.setup(model);
		}
	}

	public void setDirtyListener(PropertyChangeListener listener)
	{
		this.dirtyListener = listener;
	}

	public boolean isDirty()
	{
		return dirty;
	}

	/**
	 * Saves the modified content of the pages inside of the editor
	 *
	 */
	public void doSave()
	{

	}

	/**
	 * This fires event to the editor that the page dirty status has been changed.
	 */
	public void fireDirtyChange()
	{
		PropertyChangeEvent event = new PropertyChangeEvent(this, PROP_DIRTY, null, null);
		dirtyListener.propertyChange(event);
	}
}
