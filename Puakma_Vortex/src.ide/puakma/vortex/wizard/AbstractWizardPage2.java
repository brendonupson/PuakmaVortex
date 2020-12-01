/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    3.8.2005
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

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import puakma.coreide.ServerManager;
import puakma.coreide.objects2.Application;
import puakma.vortex.controls.ConnectedAppsViewer;
import puakma.vortex.swt.DialogBuilder2;

public abstract class AbstractWizardPage2 extends WizardPage
{
	/**
	 * Is the current application
	 */
	private Application application;

	/**
	 * Combo with all connected applications
	 */
	private ConnectedAppsViewer appsCombo;

	protected AbstractWizardPage2(String pageName, Application application)
	{
		super(pageName);
		this.application = application;
	}

	public final void createControl(Composite parent)
	{
		DialogBuilder2 builder = new DialogBuilder2(parent);
		Composite c = builder.createComposite(2);

		if(application == null) {
			builder.appendLabel("Application:");

			appsCombo = new ConnectedAppsViewer(c);
			appsCombo.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			builder.createSeparatorRow(true);
		}

		createContent(builder);

		builder.closeComposite();
		builder.finishBuilder();

		setControl(c);

		if(getApplication() == null) {
			setErrorMessage(checkError());
		}
	}

	/**
	 * Clients should overwrite this function to create all controls on it.
	 *
	 * @param builder is the parent DialogBuilder object
	 */
	protected abstract void createContent(DialogBuilder2 builder);

	public Application getApplication()
	{
		if(application != null)
			return application;
		return appsCombo.getSelectedApplication();
	}

	protected String checkError()
	{
		// CHECK FOR CONNECTION
		if(application == null && appsCombo.getSelectedApplication() == null) {
			if(ServerManager.countApplications() == 0)
				return "You have to connect to some application at first";
			else
				return "You have to select some application";
		}

		return null;
	}
}
