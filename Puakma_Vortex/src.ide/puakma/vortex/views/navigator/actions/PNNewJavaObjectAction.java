/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Aug 10, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.views.navigator.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.ui.IWorkbenchPart;

import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.DesignObject;
import puakma.vortex.VortexPlugin;
import puakma.vortex.views.navigator.ATVApplicationNode;
import puakma.vortex.views.navigator.ATVParentNode;
import puakma.vortex.views.navigator.AdapterUtils;
import puakma.vortex.views.navigator.ApplicationTreeViewController;
import puakma.vortex.views.navigator.ContextMenuHandler;
import puakma.vortex.views.navigator.PuakmaResourceView;
import puakma.vortex.wizard.NewActionWizard;
import puakma.vortex.wizard.NewClassWizard;
import puakma.vortex.wizard.NewEnumerationWizard;
import puakma.vortex.wizard.NewInterfaceWizard;
import puakma.vortex.wizard.NewSOAPWidgetWizard;
import puakma.vortex.wizard.NewScheduledActionWizard;
import puakma.vortex.wizard.java.JavaObjectWizard;

public class PNNewJavaObjectAction extends PNBaseAction
{

	private int javaType;
	private int designType;

	public PNNewJavaObjectAction(int designType, int javaType, PuakmaResourceView view)
	{
		super("", view);
		this.javaType = javaType;
		this.designType = designType;

		assert designType == DesignObject.TYPE_ACTION || designType == DesignObject.TYPE_LIBRARY
				|| designType == DesignObject.TYPE_SCHEDULEDACTION || designType == DesignObject.TYPE_WIDGET
				:  "Invalid design type for new java object";

		if(javaType != JavaObjectWizard.TYPE_CLASS && designType != DesignObject.TYPE_LIBRARY)
			throw new IllegalArgumentException("Only classes can be created for non shared code design objects");

		switch(designType) {
		case DesignObject.TYPE_ACTION:
			setText("New Action");
			setImageDescriptor(VortexPlugin.getImageDescriptor("web_module.gif"));
			break;
		case DesignObject.TYPE_LIBRARY:
			if(javaType == JavaObjectWizard.TYPE_INTERFACE) {
				setText("New Interface");
				setImageDescriptor(VortexPlugin.getImageDescriptor("newint_wiz.gif"));
			}
			else if(javaType == JavaObjectWizard.TYPE_ENUM) {
				setText("New Enum");
				setImageDescriptor(VortexPlugin.getImageDescriptor("newenum_wiz.gif"));
			}
			else if(javaType == JavaObjectWizard.TYPE_ANOTATION) {
				setText("New Annotation");
				setImageDescriptor(VortexPlugin.getImageDescriptor("newannotation_wiz.gif"));
			}
			else {
				setText("New Class");
				setImageDescriptor(VortexPlugin.getImageDescriptor("newclass_wiz.gif"));
			}
			break;
		case DesignObject.TYPE_SCHEDULEDACTION:
			setText("New Scheduled Action");
			setImageDescriptor(VortexPlugin.getImageDescriptor("jar_file.gif"));
			break;
		case DesignObject.TYPE_WIDGET:
			setText("New SOAP Widget");
			setImageDescriptor(VortexPlugin.getImageDescriptor("web_module.gif"));
			break;
		}
	}

	public void run()
	{
		Application application = ContextMenuHandler.getApplicationFromSelection(getView().getSelection());
		JavaObjectWizard wizard = getWizard(application);
		configureWizard(wizard);
		if(designType != DesignObject.TYPE_LIBRARY)
			wizard.setForcePreviousAndNextButtons(true);
		WizardDialog dlg = new WizardDialog(getShell(), wizard);
		dlg.open();
	}

	private void configureWizard(JavaObjectWizard wizard)
	{
		IStructuredSelection selection = getView().getSelection();
		Object o = selection.getFirstElement();
		IPackageFragment fragment = (IPackageFragment) AdapterUtils.getObject(o, IPackageFragment.class);
		if(fragment != null)
			wizard.setPackageFragment(fragment);
	}

	private JavaObjectWizard getWizard(Application application)
	{
		switch(designType) {
		case DesignObject.TYPE_ACTION:
			return new NewActionWizard(application);
		case DesignObject.TYPE_LIBRARY:
			if(javaType == JavaObjectWizard.TYPE_INTERFACE)
				return new NewInterfaceWizard(application);
			else if(javaType == JavaObjectWizard.TYPE_ENUM)
				return new NewEnumerationWizard(application);
			else if(javaType == JavaObjectWizard.TYPE_ANOTATION)
				throw new IllegalStateException("Not implemented");
			else
				return new NewClassWizard(application, designType);
		case DesignObject.TYPE_SCHEDULEDACTION:
			return new NewScheduledActionWizard(application);
		case DesignObject.TYPE_WIDGET:
			return new NewSOAPWidgetWizard(application);
		}

		throw new IllegalArgumentException("Invalid requested java or design object type");
	}

	public boolean handleKeyEvent(KeyEvent event)
	{
		return false;
	}

	public boolean qualifyForSelection()
	{
		IStructuredSelection selection = getView().getSelection();
		if(selection.size() != 1)
			return false;
		Application application = ContextMenuHandler.getApplicationFromSelection(selection);
		if(application == null)
			return false;

		Object o = selection.getFirstElement();

		IPackageFragment fragment = (IPackageFragment) AdapterUtils.getObject(o, IPackageFragment.class);
		if(fragment != null)
			return true;

		if(o instanceof ATVParentNode && isValidParentNode(((ATVParentNode)o).getNodeType()))
			return true;
		// THIS IS AVAILABLE ONLY FOR CLASSES AND INTERFACES
		if(designType == DesignObject.TYPE_LIBRARY) {
			if(o instanceof IFolder)
				return true;
			else if(o instanceof ATVApplicationNode)
				return true;
			else if(o instanceof IFile) {
				// TODO: enhance this shit...
				return true;
			}
		}
		else {
			if(o instanceof ATVApplicationNode)
				return true;
			else if(o instanceof DesignObject) {
				DesignObject obj = (DesignObject) o;
				int type = obj.getDesignType();
				return type == this.designType;
			}
		}

		return false;
	}

	/**
	 * Matches if tree node id matches with the type of the design object.
	 * @param nodeType is the tree node type
	 * @return true if we can create action under this node for this type of design object
	 */
	private boolean isValidParentNode(int nodeType)
	{
		if(designType == DesignObject.TYPE_ACTION && nodeType == ApplicationTreeViewController.NODE_ACTIONS)
			return true;
		if(designType == DesignObject.TYPE_SCHEDULEDACTION && nodeType == ApplicationTreeViewController.NODE_SCHEDULED_ACTIONS)
			return true;
		if(designType == DesignObject.TYPE_WIDGET&& nodeType == ApplicationTreeViewController.NODE_WIDGETS)
			return true;
		if(designType == DesignObject.TYPE_LIBRARY && nodeType == ApplicationTreeViewController.NODE_SHARED_CODE)
			return true;

		return false;
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection)
	{
	}

}
