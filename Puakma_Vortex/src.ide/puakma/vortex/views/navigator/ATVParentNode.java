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

import org.eclipse.core.runtime.IAdaptable;

import puakma.coreide.objects2.Application;
import puakma.vortex.controls.TreeParent;

/**
 * This class represents all nodes under application node like parent databases,
 * pages, actions, etc... nodes. Note that this node represents parent nodes!
 *
 * @author Martin Novak
 */
public class ATVParentNode extends TreeParent implements IAdaptable
{
	/**
	 * Node type id. Identifies which parent node is this node.
	 */
	int nodeTypeId;

	public ATVParentNode(String name, int nodeTypeId, TreeParent parent)
	{
		super(name, parent);
		this.nodeTypeId = nodeTypeId;
	}

	/**
	 * Returns node type id which identifies this node type among all nodes in the tree.
	 * @return node type id
	 */
	public int getNodeType()
	{
		return nodeTypeId;
	}

	/**
	 * Gets parent application node for this node.
	 *
	 * @return ATVApplication node which represents parent application node.
	 */
	public ATVApplicationNode getAppNode()
	{
		TreeParent p = this;
		do {
			p = p.getParent();
		} while(p instanceof ATVApplicationNode == false);
		return (ATVApplicationNode) p;
	}

	public Object getAdapter(Class adapter)
	{
		if(adapter == Application.class) {
			ATVApplicationNode appNode = getAppNode();
			return appNode.getApplication();
		}
		return null;
	}
}
