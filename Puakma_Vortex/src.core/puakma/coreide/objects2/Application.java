/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    May 5, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide.objects2;

import java.beans.PropertyChangeListener;
import java.io.IOException;

import puakma.coreide.ConnectionPrefsReader;
import puakma.coreide.FilterMatcher;
import puakma.coreide.PuakmaCoreException;
import puakma.coreide.RefreshEvent;
import puakma.coreide.RefreshEventInfoImpl;

/**
 * This interface is the Tornado application representation. Users can perform some common
 * stuff for application management.
 * 
 * @author Martin Novak
 */
public interface Application extends ServerObject, Parameters
{
	/**
	 * This event is called when application is refreshed from the server. Note
	 * that new Object in {@link PropertyChangeListener} is pointing to an array
	 * of {@link RefreshEvent} instances.
	 */
	String PROP_REFRESH = "refreshed";

	/**
	 * Event that notifies that some application object has been added to the application. Note
	 * that additions can be made also on the server by another user.
	 */
	String PROP_NEW_OBJECT = "newObject";

	/**
	 * Event notifying that some design object has been removed from the application.
	 */
	String PROP_REMOVE_OBJECT = "removeObject";

	/**
	 * This event is fired when the design object is being changed. It can occur localy when
	 * commiting to the server as well as remotely when some other user renames the design
	 * object on the server. The old value is the string with the old name, new value is the
	 * {@link DesignObject} which already contains the new name value.
	 */
	String PROP_RENAME_OBJECT = "renameObject";

	/**
	 * The basically same event as the previous one except that the oldValue will contain
	 * the whole old class name like java.lang.Object, and the newValue will contain the
	 * {@link DesignObject} which contains the updated values.
	 */
	String PROP_DESIGN_OBJECT_CLASS_NAME_CHANGE = "designObjectClassNameChange";

	/**
	 * Returns true/false indicating if the application is open
	 *
	 * @return true if the application is open.
	 */
	boolean isOpen();

	/**
	 * Opens the application.
	 */
	void open() throws PuakmaCoreException;

	/**
	 * Closes opened application. If the application is not opened, simply returns.
	 * @throws PuakmaCoreException
	 */
	void close() throws PuakmaCoreException;

	String getGroup();

	void setGroup(String group);

	/**
	 * Gets the fully qualified name of the application. This would be: [groupname/]appName
	 * @return String with fully qualified name 
	 */
	String getFQName();

	String getInheritFrom();

	void setInheritFrom(String inherited);

	String getTemplateName();

	void setTemplateName(String template);

	/**
	 * This creates the working copy of the application. Note that the list of design
	 * objects, keywords, etc is shared. Only application parameters is not independent
	 * part of application information, so they are not independent, but handled by
	 * working copies.
	 *
	 * @return a working copy of this object
	 */
	Application makeWorkingCopy();

	/**
	 * This adds application object to the application. There are two ways how to assign
	 * application object to application. The first one is to call this function. If you
	 * call this function, the whole application object will be uploaded on the server.
	 * If you assign Application in the constructor, you will need to call commit by yourself.
	 *
	 * @param appObject is the application object to add to the application
	 * @throws PuakmaCoreException 
	 */
	void addObject(ApplicationObject appObject) throws PuakmaCoreException;

	/**
	 * Removes application object from Application. There are again two ways - you can call this
	 * function or <code>ApplicationObject.remove()</code>.
	 *
	 * @param appObject is the application object to remove
	 * @throws PuakmaCoreException if upload to the server is not successful or some preconditions
	 * doesn't hold
	 * @throws IOException 
	 */
	void removeObject(ApplicationObject appObject) throws PuakmaCoreException, IOException;

	/**
	 * Lists all the roles in the application.
	 *
	 * @return array with all the roles in the applicaion
	 */
	Role[] listRoles();

	Keyword[] listKeywords();

	/**
	 * Gets role by its name. Note that role can be only one with specific name.
	 *
	 * @param roleName is the name of the role
	 * @return Role object with specific name
	 */
	Role getRole(String roleName);

	Keyword getKeyword(String kwName);

	/**
	 * Lists all existing database connections.
	 *
	 * @return array with all database connection objects
	 */
	DatabaseConnection[] listDatabases();

	/**
	 * Gets database connection object by name.
	 *
	 * @param dbconName is the database connection name
	 * @return DatabaseConnection instance or null if there is not database connection with that
	 *                        name
	 */
	DatabaseConnection getDatabaseConnection(String dbconName);

	DesignObject getDesignObject(long id);

	/**
	 * Gets the design object of the specific name.
	 *
	 * @param name is the name of the design object
	 * @return DesignObject object of the specific name
	 */
	DesignObject getDesignObject(String name);

	/**
	 * This function lists all the design objects in the application
	 * @return array of <code>DesignObject</code> objects in the application
	 */
	DesignObject[] listDesignObjects();

	/**
	 * Lists all the design objects according to the type of the object.
	 * @param type is the type of design object which we want
	 * @return array with all design objects found
	 */
	DesignObject[] listDesignObjectsByType(int type);

	/**
	 * Lists all the design objects according to matching the criteria from matcher.
	 * @param matcher object which selects what is included in result
	 * @return array with design objects matching criteria
	 */
	DesignObject[] listDesignObjects(FilterMatcher<DesignObject> matcher);

	/**
	 * This function returns <code>JavaObject</code> according to it's package and class.
	 *
	 * @param packageName is the package name
	 * @param className is the class name
	 * @return <code>JavaObject</code> object or null if there is no object with that class
	 *         and package
	 */
	JavaObject getJavaObject(String packageName, String className);

	//  NOT REALLY USEFULL NOW...
	//  /**
	//   * This function refreshes all database information for all registered databases. Note that this
	//   * doesn't refresh each database content.
	//   * 
	//   * @return array with all database ids
	//   * @throws PuakmaCoreException
	//   */
	//  public long[] refreshAllDbs() throws PuakmaCoreException;

	/**
	 * Refreshes application structure.
	 *
	 * @return object containing information about application refresh
	 * @throws PuakmaCoreException
	 */
	RefreshEventInfoImpl refresh() throws PuakmaCoreException;

	/**
	 * Adds listener for watching application events.
	 *
	 * @param listener is the listener to be add
	 */
	void addListener(ApplicationListener listener);

	/**
	 * Removes listener for watching application events. If the listener is not present, does
	 * nothing.
	 *
	 * @param listener is the listener which is to be removed
	 */
	void removeListener(ApplicationListener listener);

	/**
	 * This function flushes application's design object cache.
	 */
	void flushDesignCache() throws PuakmaCoreException;

	/**
	 * Constructs <code>ConnectionPrefsReader</code> object from this application
	 * @return <code>ConnectionPrefsReader</code> object
	 */
	ConnectionPrefsReader getConnectionPrefs();

	/**
	 * @return value of the system property. null if the property doesn't exist on
	 *         the server.
	 */
	String getProperty(String name);
}
