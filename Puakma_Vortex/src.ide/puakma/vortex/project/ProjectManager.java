/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jun 30, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import puakma.coreide.ConfigurationManager;
import puakma.coreide.ConnectionPrefs;
import puakma.coreide.ConnectionPrefsImpl;
import puakma.coreide.PuakmaCoreException;
import puakma.coreide.RefreshEventInfoImpl;
import puakma.coreide.ServerManager;
import puakma.coreide.VortexAuthentificationException;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.Server;
import puakma.vortex.IdeException;
import puakma.vortex.InterruptedAuthException;
import puakma.vortex.VortexPlugin;
import puakma.vortex.swt.PasswordDialog;

/**
 * @author Martin Novak
 */
public class ProjectManager
{
	/**
	 * This is list of eclipse projects which are connected.
	 */
	private static List<PuakmaProject2Impl> projects = new Vector<PuakmaProject2Impl>();

	/**
	 * List containing all the project creating listeners. Items are of the type
	 * <code>PuakmaProjectCreationListener</code>.
	 */
	private static List<ProjectLifecycleListener> listeners = new ArrayList<ProjectLifecycleListener>();

	/**
	 * Connection listener - removes obsolete projects
	 */
	private static PropertyChangeListener listener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt)
		{
			if(ServerManager.PROP_APP_CLOSE.equals(evt.getPropertyName())) {
				synchronized(projects) {
					PuakmaProject2Impl prj = (PuakmaProject2Impl) getProject((Application) evt.getOldValue());
					if(prj != null) {
						prj.close();
						projects.remove(prj);
					}
				}
			}
		}
	};

	static PuakmaProject2Impl newProject(String host, String groupName,
			String appName, IProgressMonitor monitor,
			Application application) throws IdeException
	{
		String prjName = generateName(host, groupName, appName);

		// TRY TO SEARCH FOR THE PROJECT NAME IF IT IS ALREADY
		if(getProject(prjName) != null)
			throw new IdeException("Trying to duplicate Eclipse project " + prjName);

		PuakmaProject2Impl prj = new PuakmaProject2Impl();
		prj.createNew(prjName, application);
		synchronized(projects) {
			projects.add(prj);
		}

		return prj;
	}

	public static void start()
	{
		synchronized(projects) {
			// ADD GLOBAL CONNECTING/DISCONNECTING LISTENER
			ServerManager.addListener(listener);
		}
	}

	public static void stop()
	{
		synchronized(projects) {
			ServerManager.removeListener(listener);
		}
	}

	/**
	 * This function generates the project name from the host, application group, and application
	 * name. The resulting string will have format host[_group]_appName.
	 *
	 * @param host
	 * @param group
	 * @param appName
	 * @return String object with formated content: host[_group]_appName
	 */
	private static String generateName(String host, String group, String appName)
	{
		String name = host;
		if(group != null && group.length() != 0)
			name += "_" + group;
		name += "_" + appName;
		return name;
	}

	/**
	 * Gets <code>PuakmaProject2</code> acording to the <code>projectName</code> parameter.
	 *
	 * @param projectName is the name of the Eclipse project we want to get
	 * @return <code>PuakmaProject2Impl</code> object or null if there is no such project
	 */
	public static PuakmaProject2Impl getProject(String projectName)
	{
		synchronized(projects) {
			Iterator it = projects.iterator();
			while(it.hasNext()) {
				PuakmaProject2Impl prj = (PuakmaProject2Impl) it.next();
				IProject project = prj.getProject();
				if(projectName.equals(project.getName()))
					return prj;
			}
			return null;
		}
	}

	public static PuakmaProject2 getProject(Application application)
	{
		if(application == null)
			throw new IllegalArgumentException("Application parameter is null");

		synchronized(projects) {
			Iterator it = projects.iterator();
			while(it.hasNext()) {
				PuakmaProject2Impl prj = (PuakmaProject2Impl) it.next();
				if(prj.getApplication() == application)
					return prj;
			}
			return null;
		}
	}

	@Deprecated
	public static IProject getIProject(Application application)
	{
		PuakmaProject2 project = getProject(application);
		if(project != null) {
			IProject ret = project.getProject();
			return ret;
		}
		return null;
	}

	/**
	 * Adds a new lifecycle listener for projects. Also runs all the necessary functions
	 * on the existing listener for the existing projects. So they are in order:
	 * <ul>
	 * <li>ProjectLifecycleListener.POST_CREATE</li>
	 * <li>ProjectLifecycleListener.POST_REFRESH</li>
	 * <li>if(project.javaStarted()) ProjectLifecycleListener.POST_JAVA_STARTED</li>
	 * </ul>
	 * 
	 * so if you want to not to be notified about those events, you have to handle it somehow
	 * by yourself.
	 *
	 * @param listener
	 */
	public static void addLifecycleListener(ProjectLifecycleListener listener)
	{
		synchronized(listeners) {
			if(listeners.contains(listener))
				return;

			listeners.add(listener);
		}

		// EXECUTE LISTENER FUNCTIONS ACCORDING TO THE ORDER IN WHICH SHOULD BE EXECUTED
		PuakmaProject2Impl[] prjs = projects.toArray(new PuakmaProject2Impl[projects.size()]);
		for(int i = 0; i < prjs.length; ++i) {
			try {
				listener.projectEvent(prjs[i], ProjectLifecycleListener.POST_CREATE, null);
				listener.projectEvent(prjs[i], ProjectLifecycleListener.POST_REFRESH, null);
				if(prjs[i].javaStarted())
					listener.projectEvent(prjs[i], ProjectLifecycleListener.POST_JAVA_START, null);
			}
			catch(Exception ex) {
				VortexPlugin.log(ex);
			}
		}
	}

	public static void removeLifecycleListener(ProjectLifecycleListener listener)
	{
		synchronized(listeners) {
			listeners.remove(listener);
		}
	}

	/**
	 * This function broadcasts events about the project lifecycle
	 * @param eventType
	 */
	static void broadcastEvent(PuakmaProject2Impl project, int eventType)
	{
		ProjectLifecycleListener[] ls;
		synchronized(listeners) {
			ls = listeners.toArray(new ProjectLifecycleListener[listeners.size()]);
		}

		for(int i = 0; i < ls.length; ++i) {
			try { ls[i].projectEvent(project, eventType, null); }
			catch(Exception e) { VortexPlugin.log(e); }
		}
	}

	public static Application connectToApplication(ConnectionPrefs prefs, IProgressMonitor monitor) throws IdeException
	{
		// ASK FOR PASSWORD IF THERE IS NO SAVE PASSWORD SET
		if(prefs.getSavePwd() == false)
			askForPassword(prefs);

		monitor.beginTask("Connecting to application", 13);

		Server server = ServerManager.createServerConnection(prefs);
		Application application;
		try {
			String pwd = prefs.getPwd();
			server.setPassword(pwd);
			checkValidPassword(server, prefs);
			server.refresh();
			application = server.getApplication(prefs.getGroup(), prefs.getApplication());
			monitor.worked(1);
		}
		catch(Exception ex) {
			throw new IdeException(ex);
		}
		finally {
			monitor.done();
		}

		PuakmaProject2Impl project = null;
		try {
			project = newProject(prefs.getHost(), prefs.getGroup(), prefs
					.getApplication(), new SubProgressMonitor(monitor, 10), application);
			monitor.worked(2);

			application.open();

			RefreshEventInfoImpl info = application.refresh();
			monitor.worked(4);

			// THIRD PHASE - SETUP JAVA IN PROJECT
			//project.setupJavaProject(new SubProgressMonitor(monitor, 1));

			// FOURTH PHASE - SETUP PROJECT - DOWNLOAD FILES...
			project.refresh(info, new SubProgressMonitor(monitor, 5,
					SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
		}
		catch(Exception ex) {
			try {
				application.close();
			}
			catch(Exception ex1) {
				VortexPlugin.log(ex1);
			}

			if(project != null) {
				synchronized(projects) {
					if(project.isOpen())
						project.close();
					projects.remove(project);
				}
			}

			throw new IdeException(ex);
		}

		return application;
	}

	/**
	 * Checks if the password is valid. If not, then asks user for it. It keeps
	 * asking user as long as either password is valid or user presses cancel
	 * button.
	 */
	private static void checkValidPassword(Server server, ConnectionPrefs prefs) throws PuakmaCoreException, IOException, InterruptedAuthException
	{
		while(true) {
			try {
				server.ping();
				return;
			}
			catch(IOException ex) {
				throw ex;
			}
			catch(VortexAuthentificationException ex) {
				askForPassword(prefs);
			}
		}
	}

	/**
	 * Asks user for the password, and stores it to prefs class. Also if user
	 * selects to save the password, it tries to save the password. However saving
	 * password depends on whether prefs parameter has set the connection manager.
	 */
	private static void askForPassword(final ConnectionPrefs prefs) throws InterruptedAuthException
	{
		final InterruptedAuthException[] exceptions = new InterruptedAuthException[1];
		final String[] pwds = new String[1];
		final boolean[] savePwds = new boolean[1];

		Display.getDefault().syncExec(new Runnable() {
			public void run()
			{
				IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				PasswordDialog dlg = new PasswordDialog(win.getShell(), "Type password",
						"Type password for server " + ConnectionPrefsImpl.getFullApplicationUrl(prefs), "",
						null, prefs.getConfigurationManager() != null, prefs.getSavePwd());
				if(dlg.open() != Window.OK) {
					exceptions[0] = new InterruptedAuthException();
					return;
				}
				// CLONE THE PREFERENCE
				prefs.setPwd(dlg.getValue());
			}
		});

		if(exceptions[0] != null)
			throw exceptions[0];

		prefs.setPwd(pwds[0]);
		if(savePwds[0]) {
			prefs.setSavePwd(true);
			ConfigurationManager manager = prefs.getConfigurationManager();
			if(manager != null) {
				try {
					manager.save(prefs);
				}
				catch(IOException e) {
					VortexPlugin.log(e);
				}
			}
		}
	}
}
