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
package puakma.vortex.wizard.importPmx;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;

import puakma.coreide.objects2.Server;
import puakma.vortex.VortexPlugin;

/**
 * This wizard creates a new page on the server.
 *
 * @author Martin Novak
 */
public class ImportPmxWizard extends Wizard
{
	/**
	 * Main, and the only page in the wizard
	 */
	private ImportPmxWizardPage mainPage;

	private String appName;
	private String groupName;
	private File file;
	private Server server;

	public void addPages()
	{
		super.addPages();

		setWindowTitle("Import Puakma Application");

		mainPage = new ImportPmxWizardPage();
		addPage(mainPage);
	}

	public boolean performFinish()
	{
		appName = mainPage.getName();
		groupName = mainPage.getGroup();
		file = mainPage.getFile();
		server = mainPage.getServer();
		mainPage.saveSettings();

		IRunnableWithProgress op = new IRunnableWithProgress() {

			public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException
					{
				doFinish(monitor);
					}
		};
		try {
			getContainer().run(false, false/* false */, op);
			return true;
		}
		catch(InterruptedException e) {
			VortexPlugin.log(e);
			return false;
		}
		catch(InvocationTargetException e) {
			VortexPlugin.log(e);
			return false;
		}
	}

	protected void doFinish(IProgressMonitor monitor)
	{
		try {
			server.importPmx(groupName, appName, file);
		}
		catch(Exception e) {
			VortexPlugin.log(e);

		}
	}
}
