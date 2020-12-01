/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jan 13, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.design;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;

import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.JavaObject;
import puakma.vortex.project.ProjectUtils;


/**
 * This file represents input for the design object editor
 * 
 * @author Martin Novak
 */
public class PuakmaEditorInput implements IFileEditorInput, IStorageEditorInput, IPathEditorInput
{
	private DesignObject obj;

	public PuakmaEditorInput(DesignObject obj)
	{
		assert obj != null : "Invalid design object parameter - cannot be null";

		this.obj = obj;
	}

	public IPersistableElement getPersistable()
	{
		return null;
	}

	public DesignObject getAppObject()
	{
		return obj;
	}


	public IFile getFile()
	{
		if(obj.isClosed())
			return null;
		boolean isSource = obj instanceof JavaObject;
		return ProjectUtils.findIFile(obj, isSource);
	}

	public boolean exists() {
		if(obj.isClosed())
			return false;

		IFile file = getFile();
		if(file != null)
			return file.exists();
		return false;
	}

	public IStorage getStorage() throws CoreException
	{
		return getFile();
	}

	public ImageDescriptor getImageDescriptor()
	{
		return null;
	}

	public String getName()
	{
		return obj.getName();
	}

	public String getToolTipText()
	{
		return getName() + " - " + getFile().getName();
	}

	public Object getAdapter(Class adapter)
	{
		if(adapter == IFileEditorInput.class)
			return this;
		else if(adapter == DesignObject.class)
			return obj;
		else if(adapter == Application.class)
			return obj.getApplication();
		else if(adapter == IFile.class)
			return getFile();

		return null;
	}

	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj instanceof PuakmaEditorInput) {
			PuakmaEditorInput ei = (PuakmaEditorInput) obj;
			return ei.obj == this.obj;
		}
		else if(obj instanceof IFileEditorInput) {
			IFileEditorInput other = (IFileEditorInput) obj;
			return getFile().equals(other.getFile());
		}
		return super.equals(obj);
	}

	public int hashCode()
	{
		return getFile().hashCode();
	}

	/**
	 * Gets DesignObject associated to this input
	 * @return DesignObject object instance
	 */
	public DesignObject getDesignObject()
	{
		return obj;
	}

	public IPath getPath()
	{
		return getFile().getLocation();
	}
}
