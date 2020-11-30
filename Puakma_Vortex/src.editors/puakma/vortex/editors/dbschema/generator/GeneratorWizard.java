/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    04/05/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.dbschema.generator;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import puakma.coreide.PuakmaCoreException;
import puakma.coreide.database.DatabaseGenerator2;
import puakma.coreide.database.SQLCommandDescriptor;
import puakma.coreide.database.SQLUtil;
import puakma.coreide.objects2.Database;
import puakma.coreide.objects2.DatabaseConnection;
import puakma.vortex.VortexPlugin;

public class GeneratorWizard extends Wizard
{
	private DatabaseGenerator2 generator;
	private GeneratorOptionsPage optionsPage;
	private GeneratorMainPage mainPage;
	private IWizardPage currentPage;
	private Database database;
	private DatabaseConnection connection;
	private List regions;

	public GeneratorWizard(Database database, DatabaseConnection connection)
	{
		this.database = database;
		this.connection = connection;
	}

	public void addPages()
	{
		super.addPages();

		optionsPage = new GeneratorOptionsPage(database, connection);
		addPage(optionsPage);

		mainPage = new GeneratorMainPage();
		addPage(mainPage);

		currentPage = optionsPage;
	}

	public boolean canFinish()
	{
		return currentPage == mainPage;
	}

	public IWizardPage getPreviousPage(IWizardPage page)
	{
		currentPage = super.getPreviousPage(page);
		return currentPage;
	}

	public IWizardPage getNextPage(IWizardPage page)
	{
		if(page == optionsPage) {
			generator = optionsPage.setupAndGetGenerator();
			mainPage.setGenerator(generator);
			currentPage = mainPage;
		}
		else
			currentPage = super.getNextPage(page);

		return currentPage;
	}

	public boolean performFinish()
	{
		try {
			final String sql = mainPage.getSqlText();
			final boolean[] ok = { false };
			getContainer().run(true, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException,
				InterruptedException
				{
					try {
						ok[0] = performFinish2(sql, monitor);
					}
					catch(Exception e) {
						throw new InvocationTargetException(e);
					}
				}
			});

			// IF THE COMMAND DIDN'T ENDED SUCCESFULLY, WE SHOULD DISPLAY THE ERRORS
			if(ok[0] == false) {
				mainPage.displayErrors(this.regions);
			}

			return ok[0];
		}
		catch(InvocationTargetException e) {
			VortexPlugin.log(e.getTargetException());
		}
		catch(InterruptedException e) {
			// IGNORE INTERUPTION
		}
		return false;
	}

	/**
	 * This performs commands execution. Note that this should be run in the
	 * background thread.
	 */
	public boolean performFinish2(String sql, IProgressMonitor monitor) throws PuakmaCoreException, IOException
	{
		boolean ret = true;
		this.regions = null;

		if(monitor == null)
			monitor = new NullProgressMonitor();
		monitor.beginTask("Generating database...", 1);

		try {
			SQLCommandDescriptor[] descs = SQLUtil.splitSqlToCommands(sql, SQLUtil.ESCAPE_TYPE_MYSQL);
			int errorAction = generator.getErrorPolicy();
			database.executeBatch(descs, errorAction);

			// NOW CHECK THE RESULT FOR THE ERROR. IF WE DETECT SOME SQL ERROR, WE
			// SHOULD DISPLAY THEM. SO WE FILL errorCommandDescriptor VARIABLE
			for(int i = 0; i < descs.length; ++i) {
				if(descs[i].exceptionStackTrace != null) {
					this.regions = Arrays.asList(descs);
					ret = false;
					break;
				}
			}

			monitor.worked(1);
		}
		finally {
			monitor.done();
		}

		return ret;
	}
}
