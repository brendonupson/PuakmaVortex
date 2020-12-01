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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import puakma.utils.lang.PropertyManipulator;

/**
 * This class represents binding relation from java bean to the SWT control. It
 * doesn't do anything, just store binding information.
 * 
 * @author Martin Novak
 */
class ControlBindingDescriptor extends PropertyManipulator
{
	private Control control;
	private boolean indexInput;

	public ControlBindingDescriptor(Control control, String propertyName, Class<?> beanClass)
			throws IntrospectionException
			{
		super(beanClass, propertyName);

		this.control = control;
			}

	public Control getControl()
	{
		return control;
	}

	public void setIndexInput(boolean indexed)
	{
		this.indexInput = indexed;
	}

	public boolean isIndexedInput()
	{
		return indexInput;
	}

	/**
	 * This function sets the control value to the property bean
	 */
	public void setControlValueToBean(Object bean)
	{
		Object value = null;

		if(control instanceof Text) {
			value = ((Text) control).getText();
		}
		else if(control instanceof Combo) {
			Combo combo = (Combo) control;
			if((combo.getStyle() & SWT.READ_ONLY) == SWT.READ_ONLY) {
				if(String.class.equals(getClass())) {
					value = combo.getText();
				}
				// OR IT IS NUMERICAL VALUE - NO ONE ELSE IS SUPPORTED HERE [-;
				else {
					value = new Integer(combo.getSelectionIndex());
				}
			}
			else
				value = combo.getText();
		}
		else
			throw new IllegalArgumentException("Invalid SWT control type");

		setPropertyOnObject(bean, value);
	}
}