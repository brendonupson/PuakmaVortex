/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 9, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.wizard;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import puakma.coreide.ObjectsFactory;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.DesignObject;
import puakma.utils.NameValuePair;
import puakma.vortex.VortexPlugin;
import puakma.vortex.WorkbenchUtils;
import puakma.vortex.project.ProjectUtils;
import puakma.vortex.templates.TemplateCreator;

/**
 * This wizard creates a new page on the server.
 *
 * @author Martin Novak
 */
public class NewPageWizard extends AbstractWizard
{
	/**
	 * Connection to the application we want to modify.
	 */
	private Application application;

	/**
	 * Main, and the only page in the wizard
	 */
	private NewPagePage mainPage;

	public void init(Application connection)
	{
		this.application = connection;
	}

	public void addPages()
	{
		super.addPages();

		setWindowTitle(WizardMessages.NewPageWizard_Title);
		mainPage = new NewPagePage(application);
		addPage(mainPage);
	}

	public boolean performFinish()
	{
		String name = mainPage.getName();
		String comment = mainPage.getComment();
		
		boolean isSource = false;
		
		final DesignObject obj = ObjectsFactory.createDesignObject(name, DesignObject.TYPE_PAGE);
		obj.setDescription(comment);
		
		if(application == null)
			application = mainPage.getApplication();

		try {
			// TODO: convert this to background thread!
			// add object to application
			application.addObject(obj);

			// now upload the new page to the server
			IFile file = ProjectUtils.getIFile(obj, isSource);
			NameValuePair[] params = new NameValuePair[0];
			TemplateCreator.processTemplate("page.vm", file, params);

			// AND UPLOAD PAGE TO THE SERVER
			obj.upload(file.getContents(), isSource);

			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					WorkbenchUtils.openDesignObject(obj);
				}
			});

			return true;
		}
		catch(Exception e) {
			VortexPlugin.log(e);
			MessageDialog.openError(getShell(), WizardMessages.NewPageWizard_Error_Create_Title,
					WizardMessages.NewPageWizard_Error_Create_Message + e.getLocalizedMessage());
		}

		return false;
	}
}
