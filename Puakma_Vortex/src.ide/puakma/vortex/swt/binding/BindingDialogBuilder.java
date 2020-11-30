/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    30/05/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.swt.binding;

import java.beans.IntrospectionException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import puakma.vortex.swt.DialogBuilder2;

public class BindingDialogBuilder extends DialogBuilder2 implements Listener
{
	public static final String PROP_DIRTY_CHANGE = "dirty";
	private PropertyChangeListener listener;
	private boolean dirty;
	private List<ControlBindingDescriptor> properties = new ArrayList<ControlBindingDescriptor>();

	public BindingDialogBuilder(Composite parent)
	{
		super(parent);
	}

	public void bindControl(Control control, String propName, Class<?> clazz)
			throws IntrospectionException
			{
		ControlBindingDescriptor desc = new ControlBindingDescriptor(control, propName, clazz);

		if(control instanceof Text) {
			control.addListener(SWT.Modify, this);
		}
		else if(control instanceof Combo) {
			Combo combo = (Combo) control;
			if((combo.getStyle() | SWT.READ_ONLY) == SWT.READ_ONLY)
				combo.addListener(SWT.Selection, this);
			else
				combo.addListener(SWT.Modify, this);
		}

		properties.add(desc);
			}

	public void handleEvent(Event event)
	{
		// IF WE DON'T NEED TO CHANGE DIRTY STATUS, SIMPLY RETURN
		if(dirty)
			return;

		if(event.type == SWT.Modify || event.type == SWT.Selection) {
			boolean oldDirty = dirty;
			dirty = true;

			if(listener != null) {
				PropertyChangeEvent evt = new PropertyChangeEvent(this, PROP_DIRTY_CHANGE,
						Boolean.valueOf(oldDirty),
						Boolean.valueOf(dirty));
				listener.propertyChange(evt);
			}
		}
	}

	public boolean isDirty()
	{
		return dirty;
	}

	public void setDirty(boolean b)
	{
		dirty = false;
	}

	public void commit(Object bean)
	{
		Iterator<ControlBindingDescriptor> it = properties.iterator();
		while(it.hasNext()) {
			ControlBindingDescriptor desc = (ControlBindingDescriptor) it.next();
			desc.setControlValueToBean(bean);
		}
	}
}
