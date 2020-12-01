/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Oct 9, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.views.navigator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;

public class AdapterUtils
{
	/**
	 * This function tries to find the class instance from the object. If this is not possible,
	 * it returns null.
	 *
	 * @param object is the object from which we want to extract object of specific class
	 * @param clazz is the class we want to get
	 * @return instanceof class or null if the object cannot extract that instance
	 */
	public static Object getObject(Object object, Class clazz)
	{
		if(object == null)
			return null;
		if(clazz.isAssignableFrom(object.getClass()))
			return object;
		else if(object instanceof IAdaptable)
			return ((IAdaptable)object).getAdapter(clazz);
		else
			return Platform.getAdapterManager().getAdapter(object, clazz);
	}
}
