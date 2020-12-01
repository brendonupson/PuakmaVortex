/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Mar 15, 2005
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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;

import puakma.coreide.ObjectsFactory;
import puakma.coreide.PuakmaCoreException;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.ILogger;
import puakma.coreide.objects2.ResourceObject;
import puakma.utils.io.FileUtils;
import puakma.vortex.VortexPlugin;
import puakma.vortex.project.ProjectManager;
import puakma.vortex.project.ProjectUtils;
import puakma.vortex.project.PuakmaProject2;

/**
 * @author Martin Novak
 */
public class NewLibraryWizard extends AbstractWizard
{
	private Application application;

	private NewLibraryPage mainPage;

	public NewLibraryWizard()
	{
		this(null);
	}

	/**
	 * Initializes wizard with the current application connection.
	 *
	 * @param connection is the application in which we should create a new library
	 */
	public NewLibraryWizard(Application connection)
	{
		this.application = connection;

		setWindowTitle("Add java library");
	}

	public void addPages()
	{
		mainPage = new NewLibraryPage(application);
		addPage(mainPage);
	}

	/**
	 * This method creates the library object on the server, then it copies the jar file which
	 * will be then uploaded on the server.
	 *
	 * @return true if execution is without problems...
	 */
	public boolean performFinish()
	{
		// at first copy create the library object on the server
		//
		final String name = mainPage.getName();
		final IPath path = mainPage.getFile();
		if(application == null)
			application = mainPage.getApplication();

		// Now copy the file. Note that we have to copy the file in the workspace operation
		// thread or we might have some serious problems with synchronization
		//
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor)
			{
				try {
					boolean isSource = false;
					ResourceObject lib = (ResourceObject) ObjectsFactory.createDesignObject(name, DesignObject.TYPE_JAR_LIBRARY);
					application.addObject(lib);

					IFile original = ProjectUtils.getIFile(lib, isSource);
					File fDest = original.getLocation().toFile();
					File fSrc = path.toFile();

					// If the user is trying to upload the file which is on the same path as it was,
					// ignore it, and simply upload the file
					//
					try {
						if(fDest.equals(fSrc) == false)
							FileUtils.copyFile(fDest, fSrc);
					}
					catch(final IOException e) {
						Display.getDefault().asyncExec(new Runnable() {
							public void run()
							{
								MessageDialog.openError(getShell(), "Cannot upload file", "Cannot upload the file.\n" +
										"Reason:\n" + e.getLocalizedMessage());
							}
						});
						VortexPlugin.log(e);
						return;
					}

					// Now upload the file
					lib.upload(original.getContents(), isSource);

					PuakmaProject2 project = ProjectManager.getProject(application);
					isSource = false;
					project.addLibraryToClassPath(lib, isSource);
				}
				catch(PuakmaCoreException e) {
					VortexPlugin.log("Cannot create new library", e, ILogger.ERROR_ERROR);
				}
				catch(CoreException e) {
					VortexPlugin.log("Cannot create new library", e, ILogger.ERROR_ERROR);
				}
				catch(IOException e) {
					VortexPlugin.log("Cannot create new library", e, ILogger.ERROR_ERROR);
				}
			}
		};

		try {
			//      IWorkspace wsp = ResourcesPlugin.getWorkspace();
			//      wsp.run(runnable, null, 0, null);
			getContainer().run(false, true, runnable);
		}
		// TODO: rollback action
		catch(InvocationTargetException e) {
			e.printStackTrace();
		}
		catch(InterruptedException e) {
			e.printStackTrace();
		}

		return true;
	}
}
