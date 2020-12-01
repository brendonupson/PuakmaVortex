/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    1.1.2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import puakma.coreide.ConfigurationManager;
import puakma.coreide.ConfigurationManagerImpl;
import puakma.coreide.PuakmaLibraryManager;
import puakma.coreide.ServerManager;
import puakma.coreide.VortexMultiException;
import puakma.coreide.objects2.ILogger;
import puakma.vortex.project.ProjectManager;
import puakma.vortex.project.inconsistency.InconsistencyListener;

/**
 * The main plugin class to be used in the desktop.
 */
public class VortexPlugin extends AbstractUIPlugin
{
	private static final String DEBUG_MODE_ID = "puakma.vortex.debug";

	private static final String SERVER_CONFIG_FILE = "server.config";

	private static final String RECENT_APPS_CONFIG_FILE = "recentApps.config";

	public static final String PUAKMA_HOME_CLASSPATH_VARIABLE = "PUAKMA_HOME";

	/**
	 * This sets if the vortex is running in the debug mode. It can be set to pass
	 * "-Dpuakma.vortex.debug=true" argument to the java command.
	 * 
	 * @see #DEBUG_MODE_ID
	 */
	public static boolean DEBUG_MODE = false;

	/**
	 * The shared instance.
	 */
	private static VortexPlugin plugin;

	/**
	 * Resource bundle.
	 */
	private ResourceBundle resourceBundle;

	private JdbcDriverDefinition[] drivers;

	/**
	 * Manager for favorite applications
	 */
	private ConfigurationManager favorites;

	/**
	 * Manager for recent applications
	 */
	private ConfigurationManager recent;

	/**
	 * Manager for servers
	 */
	private ConfigurationManager server;

	private RecentApplicationsListener recentAppsConnectionListener;

	private InconsistencyListener inconsListener;

	private static ILogger logger;

	private static int buildNumber = -1;

	private static String versionString;

	public static final String PLUGIN_ID = "puakma.vortex";

	private static final String FAVORITE_APP_CONFIG_FILE = "favoriteApps.config";

	/**
	 * The constructor.
	 */
	public VortexPlugin()
	{
		super();
		plugin = this;

		try {		
			resourceBundle = ResourceBundle.getBundle("puakma.vortex.ideResources");
		}
		catch(Exception x) {
			resourceBundle = null;
			VortexPlugin.log(x);
		}
	}

	/**
	 * This method is called upon plug-in activation
	 * 
	 * @param context is the plugin context
	 * @throws Exception can be thrown
	 */
	public void start(BundleContext context) throws Exception
	{
		super.start(context);

		// SETUP THE DEBUG MODE
		String debugMode = System.getProperty(DEBUG_MODE_ID);
		if("true".equals(debugMode))
			this.DEBUG_MODE = true;

		// at first configure ide plugin
		logger = new IdeLogger();

		// CHECK ALPHA/BETA STATUS
		//checkForAlphaVersion();

		// initialize core library
		PuakmaLibraryManager.configure(logger, DEBUG_MODE);

		// SETUP SERVER MANAGER AUTHENTICATION
		ServerManager.setAuthenticatorDialog(new AuthenticationDialogImpl());

		// INITIALIZE GLOBAL LISTENER FOR APPLICATIONS AND RECENT MENU
		recentAppsConnectionListener = new RecentApplicationsListener();
		ServerManager.addListener(recentAppsConnectionListener);
		inconsListener = new InconsistencyListener();
		ServerManager.addListener(inconsListener);

		// START ECLIPSE PROJECT STUFF
		ProjectManager.start();

		// Initialize JDBC definitions
		initJdbcDefinitions();
	}

	//  /**
	//   * This function checks for alpha version, and possibly enables the alpha features from it.
	//   * Look at the file beta.properties in the plugin directory, and set key alpha to version
	//   * you want. Note that notation of versions is multiplied by 10, so '1.1' = '11'.
	//   */
	//  private void checkForAlphaVersion()
	//  {
	//    URL betaFile = getPluginFileUrl(BETA_PROPERTIES_FILE);
	//    if(betaFile == null)
	//      return;
	//    InputStream is = null;
	//    try {
	//      is = betaFile.openStream();
	//      Properties p = new Properties();
	//      p.load(is);
	//      String str = p.getProperty("alpha");
	//      int i = Integer.parseInt(str);
	//      if(i == 11)
	//        ALPHA_VERSION_1_1  = true;
	//    }
	//    catch(Exception ex) {
	//      log(ex);
	//    }
	//    finally {
	//      try { if(is != null) is.close(); } catch(Exception e) {  }
	//    }
	//  }

	/**
	 * Loads all the jdbc definitions from configuration file
	 */
	private void initJdbcDefinitions()
	{
		InputStream is = null;
		try {
			is = openConfigStream("jdbc.config");
			Properties p = new Properties();
			p.load(is);

			String drivers = p.getProperty("drivers");
			StringTokenizer tok = new StringTokenizer(drivers, ",");
			List<JdbcDriverDefinition> ldrivers = new ArrayList<JdbcDriverDefinition>();
			while(tok.hasMoreTokens()) {
				String token = tok.nextToken();
				String name = p.getProperty(token + ".name");
				String clz = p.getProperty(token + ".driver");
				String url = p.getProperty(token + ".url");
				JdbcDriverDefinition def = new JdbcDriverDefinition(name, clz, url);
				ldrivers.add(def);
			}
			this.drivers = ldrivers.toArray(new JdbcDriverDefinition[ldrivers.size()]);
		}
		catch(IOException e) {
			VortexPlugin.log(e);
		}
		finally {
			try {if(is != null) is.close(); } catch(IOException e) { VortexPlugin.log(e); }
		}
	}

	/**
	 * Lists all known jdbc driver definitions.
	 * 
	 * @return array with JdbcDriverDefinition objects
	 */
	public JdbcDriverDefinition[] listJdbcDriverDefinitions()
	{
		return drivers.clone();
	}

	/**
	 * Gets database driver by jdbc class name
	 *
	 * @param clz is the driver class name
	 * @return is JdbcDriverDefinition with info about the driver or null if nothing
	 *         found
	 */
	public JdbcDriverDefinition getJdbcDriverByClass(String clz)
	{
		for(int i = 0; i < drivers.length; ++i) {
			if(clz.equals(drivers[i].getClass().getName()))
				return drivers[i];
		}
		return null;
	}

	/**
	 * This method is called when the plug-in is stopped
	 *
	 * @param context is the context in which is plugin running
	 * @throws Exception can throw Exception if needed
	 */
	public void stop(BundleContext context) throws Exception
	{
		super.stop(context);

		//    ResourcesPlugin.getWorkspace().removeResourceChangeListener(resChangeListener);
		ServerManager.removeListener(recentAppsConnectionListener);
		recentAppsConnectionListener = null;
		ServerManager.removeListener(inconsListener);
		inconsListener = null;

		ProjectManager.stop();

		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 * @return the default instance of the plugin
	 */
	public static VortexPlugin getDefault()
	{
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle, or 'key' if not
	 * found.
	 *
	 * @param key is the key for the text resource we need
	 * @return String with the resource or key if resource is not found
	 */
	public static String getResourceString(String key)
	{
		ResourceBundle bundle = VortexPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		}
		catch(MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle.
	 *
	 * @return ResourceBundle class of this project
	 */
	public ResourceBundle getResourceBundle()
	{
		return resourceBundle;
	}

	public synchronized static void log(String text, Throwable t, int level)
	{
		if(logger != null) {
			logger.log(text, t, level);
		}
	}

	/**
	 * This should log any exception generated in the eclipse ide in all modules
	 * which belongs to Puakma Vortex IDE.
	 * 
	 * @param t exception to log
	 */
	public static void log(Throwable t)
	{
		if(logger != null) {
			if(t instanceof InvocationTargetException) {
				InvocationTargetException te = (InvocationTargetException) t;
				t = te.getTargetException();
			}
			else if(t instanceof VortexMultiException) {
				VortexMultiException me = (VortexMultiException) t;
				Exception[] excs = me.listExceptions();
				for(int i = 0; i < excs.length; ++i)
					logger.log(null, excs[i], ILogger.ERROR_ERROR);

				return;
			}
			logger.log(null, t, ILogger.ERROR_ERROR);
		}
	}

	public static void log(String message, Throwable t)
	{
		if(logger != null)
			logger.log(message, t, ILogger.ERROR_ERROR);
	}

	/**
	 * Logs message as error.
	 *
	 * @param message is the error message to log
	 */
	public static void log(String message)
	{
		if(logger != null)
			logger.log(message, null, ILogger.ERROR_ERROR);
	}

	public static void warning(String message)
	{
		if(logger != null)
			logger.log(message, null, ILogger.ERROR_WARNING);
	}

	public static void info(String message)
	{
		if(logger != null)
			logger.log(message, null, ILogger.ERROR_INFO);
	}

	/**
	 * Logs message with priority.
	 *
	 * @param message is the error message to log
	 * @param level is the log level
	 * @see ILogger
	 */
	public static void log(String message, int level)
	{
		if(logger != null)
			logger.log(message, null, level);
	}


	public static void log(String string, IStatus status)
	{
		if(logger != null) {
			logger.log(string, status.getException(), status.getSeverity());
		}
		else {
			Throwable t = status.getException();
			t.printStackTrace();
		}
	}

	/**
	 * Returns cached image icon.
	 * 
	 * @param imageKey is the name of the image file in icons directory.
	 * @return Image object
	 */
	public static Image getImage(String imageKey)
	{
		ImageRegistry reg = getDefault().getImageRegistry();
		Image image = reg.get(imageKey);
		if(image == null) {
			image = getImageDescriptor(imageKey).createImage();
			reg.put(imageKey, image);
		}

		return image;
	}

	/**
	 * Returns ImageDescriptor object for the requested image. Note that you pass
	 * image name which is under /icons directory in the plugin directory.
	 * 
	 * @param name file name under /icons directory in the plugin directory
	 * @return ImageDescriptor object
	 */
	public static ImageDescriptor getImageDescriptor(String name)
	{
		String iconPath = "icons/";
		try {
			URL installURL = getDefault().getBundle().getEntry("/");
			URL url = new URL(installURL, iconPath + name);
			return ImageDescriptor.createFromURL(url);
		}
		catch(MalformedURLException e) {
			return null;
			//      return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	/**
	 * Executes Runnable object in the UI thread.
	 *
	 * @param runnable is the Runnable object to execute
	 */
	public static void asyncExec(Runnable runnable)
	{
		Display d = Display.getDefault();
		d.asyncExec(runnable);
	}

	public static IPath getPluginDirectory()
	{
		String path;
		try {
			path = FileLocator.toFileURL(getDefault().getBundle().getEntry("/")).getPath();
			IPath puakmaPath = new Path(path);
			return puakmaPath;
		}
		catch(IOException e) {
			VortexPlugin.log(e);
			return new Path("");
		}
	}

	//  public IPath getPuakmaClasspathVariable() {
	//    IPath puakmaPath = JavaCore.getClasspathVariable(PUAKMA_HOME_CLASSPATH_VARIABLE);
	//    if(puakmaPath == null) {
	//      this.initPuakmaClasspathVariable();
	//      puakmaPath = JavaCore.getClasspathVariable(PUAKMA_HOME_CLASSPATH_VARIABLE);
	//    }
	//    return puakmaPath;
	//  }


	//  public void initPuakmaClasspathVariable() {
	//    try {
	//      // TODO: change the path to the core ide plugin...
	//      JavaCore.setClasspathVariable(PUAKMA_HOME_CLASSPATH_VARIABLE,
	//                                    new Path(PuakmaIdePlugin.getPluginDirectory()), null);    
	//    }
	//    catch (JavaModelException e) {
	//      log(e);
	//    }
	//  }

	/**
	 * Returns plugin's configuration directory.
	 *
	 * @return String with configuration directory
	 */
	public static String getConfigDirectory()
	{
		try {
			return FileLocator.toFileURL(getDefault().getBundle().getEntry("/config")).getPath();
		}
		catch(IOException e) {
			VortexPlugin.log(e);
			return "";
		}
	}

	/**
	 * Gets the URL of the file relative to the plugin. Note that if the plugin is in jar,
	 * this is still valid [-;
	 * @param file is the absolute path to the file we want
	 * @return URL object pointing to the file
	 */
	public static URL getPluginFileUrl(String file)
	{
		if(file.startsWith("/") == false)
			file = "/" + file;
		return getDefault().getBundle().getEntry(file);
	}

	/**
	 * This opens configuration file from the config directory.
	 */
	public InputStream openConfigStream(String configFileName) throws IOException
	{
		return FileLocator.openStream(getBundle(), new Path("config").append(configFileName), false);
	}

	public synchronized ConfigurationManager getFavoritesAppsManager()
	{
		if(favorites == null)
			favorites = loadConnectionConfiguration(FAVORITE_APP_CONFIG_FILE);

		return favorites;
	}

	public synchronized ConfigurationManager getRecentAppsManager()
	{
		if(recent == null)
			recent = loadConnectionConfiguration(RECENT_APPS_CONFIG_FILE);

		return recent;
	}

	public synchronized ConfigurationManager getServersManager()
	{
		if(server == null)
			server = loadConnectionConfiguration(SERVER_CONFIG_FILE);

		return server;
	}

	/**
	 * Loads configuration from the file, and returns it. If the configuration file is not valid, or
	 * some error occurs, simply returns empty configuration.
	 *
	 * @param fileName is the file from which we load configuration
	 * @return connection configuration from the specified file
	 */
	private ConfigurationManager loadConnectionConfiguration(String fileName)
	{
		ConfigurationManager config = new ConfigurationManagerImpl();
		File f = getPluginWorkspaceDirectory();
		f = new File(f, fileName);
		config.setFile(f);

		if(f.exists() && f.isFile() && f.canRead()) {
			try {
				config.load();
			}
			catch(IOException e) {
				log(e);
			}
		}

		return config;
	}

	/**
	 * @return directory with the plugin working directory
	 */
	public File getPluginWorkspaceDirectory()
	{
		return Platform.getStateLocation(getBundle()).toFile();
	}

	/**
	 * This function creates {@link IStatus} from the exception.
	 * @param e is the exception we want to create {@link IStatus} object
	 * @return {@link IStatus} object
	 */
	public static IStatus createStatus(Throwable e)
	{
		return new Status(IStatus.ERROR, VortexPlugin.PLUGIN_ID, 0, e.getLocalizedMessage(), e);
	}

	/**
	 * @return build number string
	 */
	public static String getBuildNumberString()
	{
		if(buildNumber == -1)
			initVersionNumbers();

		return Integer.toString(buildNumber);
	}

	public static String getVersionNumberString()
	{
		if(versionString == null)
			initVersionNumbers();

		return versionString == null ? "" : versionString;
	}

	private static void initVersionNumbers()
	{
		Properties p = new Properties();
		InputStream is = null;
		try {
			is = FileLocator.openStream(getDefault().getBundle(), new Path("about.mappings"), false);
			if(is != null) {
				p.load(is);
			}
			String s = p.getProperty("0");
			versionString = p.getProperty("1");
			buildNumber = Integer.parseInt(s);
		}
		catch(Exception e) {
			VortexPlugin.log(e);
		}
		finally {
			if(is != null) try { is.close(); } catch(Exception ex) {  }
		}
	}

	/**
	 * Logs the debug message. This is enabled only and only when DEBUG_MODE is
	 * enabled.
	 */
	public static void debug(String message)
	{
		if(DEBUG_MODE)
			info(message);
	}
}
