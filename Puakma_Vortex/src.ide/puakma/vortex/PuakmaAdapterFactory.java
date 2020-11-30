/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Oct 31, 2005
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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jdt.core.ICompilationUnit;

import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.ApplicationObject;
import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.JavaObject;
import puakma.vortex.project.ProjectUtils;

public class PuakmaAdapterFactory implements IAdapterFactory
{
	public Class[] getAdapterList()
	{
		return new Class[] { ICompilationUnit.class, IResource.class };
	}

	public Object getAdapter(Object obj, Class adapterType)
	{
		if(obj instanceof DesignObject) {
			return findDesignObjectAdaptable((DesignObject)obj, adapterType);
		}
		else if(obj instanceof Application) {
			return findApplicationObjectAdaptable((ApplicationObject)obj, adapterType);
		}
		// TODO: ARe THERE SOME OTHER ADAPTABLE OBJECTS????????????
		return null;
	}

	private Object findDesignObjectAdaptable(DesignObject object, Class adapterType)
	{
		boolean isSource = false;
		// GUESS IF THE OBJECT IS WITH SOURCE OR NOT
		if(object instanceof JavaObject)
			isSource = true;
		if(adapterType == IResource.class) {
			return ProjectUtils.getIFile(object, isSource);
		}
		else if(adapterType == ICompilationUnit.class && object instanceof JavaObject) {
			return ProjectUtils.getCompilationUnit((JavaObject) object);
		}
		return null;
	}

	private Object findApplicationObjectAdaptable(ApplicationObject object, Class adapterType)
	{
		return null;
	}
}
