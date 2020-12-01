/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jan 10, 2005
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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IAdaptable;

import puakma.coreide.objects2.Application;
import puakma.vortex.controls.TreeParent;


/**
 * This class represents the main application node in the applications tree.
 *
 * @author Martin Novak
 */
public class ATVApplicationNode extends TreeParent implements IAdaptable
{
	public Application application;
	public ATVParentNode databaseNode;
	public ATVParentNode pagesNode;
	public ATVParentNode actionsNode;
	public ATVResourcesNode resourcesNode;
	public ATVParentNode sharedNode;
	public ATVParentNode documentationNode;
	public ATVParentNode scheduledNode;
	public ATVParentNode widgetsNode;
	public IFolder srcNode;

	public ATVApplicationNode(Application application, TreeParent parent)
	{
		super(application.getServer().getHost() + "/" + application.getFQName(), parent);
		this.application = application;
	}

	public Object getAdapter(Class adapter)
	{
		if(adapter == Application.class)
			return application;
		return null;
	}

	public Application getApplication()
	{
		return application;
	}

	/**
	 * Lists all nodes which contains some java elements bellow.
	 * @return array with all the nodes containing java elements bellow
	 */
	public Object[] listAllJavaNodes()
	{
		return new Object[] { actionsNode, sharedNode, scheduledNode, widgetsNode, srcNode };
	}
}
