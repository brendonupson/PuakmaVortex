/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Shining day in november 2005 
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class QueryStorage implements IStorage
{
	public InputStream getContents() throws CoreException
	{
		ByteArrayInputStream bis = new ByteArrayInputStream(new byte[0]);
		return bis;
	}

	public IPath getFullPath()
	{
		return new Path("");
	}

	public String getName()
	{
		return "";
	}

	public boolean isReadOnly()
	{
		return false;
	}

	public Object getAdapter(Class adapter)
	{
		return null;
	}
}
