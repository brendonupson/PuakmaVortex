/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    May 12, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import puakma.SOAP.SOAPFaultException;
import puakma.SOAP.SoapProxy;
import puakma.coreide.designer.AppDesigner;
import puakma.coreide.designer.ApplicationStructureBean;
import puakma.coreide.designer.DownloadDesigner;
import puakma.coreide.designer.ServerDesigner;
import puakma.coreide.designer.XMLWholeApplicationParser;
import puakma.coreide.designer.ApplicationStructureBean.DObject;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.ApplicationListener;
import puakma.coreide.objects2.ApplicationObject;
import puakma.coreide.objects2.DatabaseConnection;
import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.JavaObject;
import puakma.coreide.objects2.Keyword;
import puakma.coreide.objects2.ObjectChangeEvent;
import puakma.coreide.objects2.Role;
import puakma.coreide.objects2.Server;
import puakma.coreide.objects2.TornadoDatabaseConstraints;
import puakma.utils.StringNameValuePair;
import puakma.utils.XmlUtils;
import puakma.utils.lang.CollectionsUtil;
import puakma.utils.lang.EqualsComparator;
import puakma.utils.lang.StringUtil;
import puakma.vortex.VortexPlugin;
import puakma.vortex.editors.pma.Logger;

/**
 * @author Martin Novak
 */
class ApplicationImpl extends ServerObjectImpl implements Application
{
	private String group;

	private boolean open;

	private String inheritFrom;

	private String templateName;

	private AppDesigner appDesigner;

	private DownloadDesigner[] downloadDesigner;

	private List<RoleImpl> roles = new ArrayList<RoleImpl>();

	private List<KeywordImpl> keywords = new ArrayList<KeywordImpl>();

	private List<DatabaseConnectionImpl> dbConnections = new ArrayList<DatabaseConnectionImpl>();

	/**
	 * Design objects list. Intentionaly package visible since it is referenced from
	 * DesignObjectStore as a kind of friend.
	 */
	DesignObjectsStore dobjects = new DesignObjectsStore(this);

	private List<ApplicationListener> listeners = new ArrayList<ApplicationListener>();

	private ParametersImpl params = new ParametersImpl(this);

	/**
	 * {@link Map} object with all server side system properties
	 * 
	 * @see System#getProperties()
	 */
	private Map<String, String> systemProperties = new HashMap<String, String>();

	public ApplicationImpl(ServerImpl server)
	{
		super(server);
	}

	protected void setServer(ServerImpl server)
	{
		super.setServer(server);

		if(server != null) {
			appDesigner = server.getDesignerFactory().newAppDesigner(server.getFullPathToDesigner(),
					server.getUserName(), server.getPassword());
			downloadDesigner = new DownloadDesigner[4];
			for(int x = 0; x < downloadDesigner.length; ++x) {
				downloadDesigner[x] = (DownloadDesigner) SoapProxy.createSoapClient(DownloadDesigner.class,
						server.getFullPathToDesigner()
						+ ServerImpl.SERVER_DOWNLOADER_EXEC_PATH,
						server.getUserName(),
						server.getPassword());

			}
		}
	}

	public boolean isOpen()
	{
		return open;
	}

	public void open() throws PuakmaCoreException
	{
		synchronized(this) {
			if(isOpen())
				throw new PuakmaCoreException("Application is already connected");
			open = true;
			// TODO: distinguish between creating a new application and opening application.
			setValid();
		}

		server.fireApplicationConnect(this);
	}

	public void close()
	{
		synchronized(this) {
			open = false;
		}

		super.close();

		fireDisconnect();
		fireEvent(PROP_CLOSE, false, true);

		server.fireApplicationDisconnect(this);
		ServerManager.fireApplicationDisconnected(this);

		synchronized(roles) {
			for(RoleImpl role : roles) {
				role.close();
			}

			roles.clear();
		}

		synchronized(keywords) {
			for(KeywordImpl kw : keywords) {
				kw.close();
			}

			keywords.clear();
		}

		synchronized(dbConnections) {
			for(DatabaseConnectionImpl dbCon : dbConnections) {
				try {
					dbCon.close();
				} catch(Exception ex) { PuakmaLibraryManager.log(ex); }
			}
			dbConnections.clear();
		}

		synchronized(dobjects) {
			for(DesignObjectImpl dob : dobjects) {
				dob.close();
			}
			dobjects.clear();
		}
	}

	/**
	 * This function fires disconnect event for all application listeners.
	 */
	private void fireDisconnect()
	{
		ApplicationListener[] ls = listApplicationListeners();
		for(int i = 0; i < ls.length; ++i) {
			try {
				ls[i].disconnect(this);
			}
			catch(Exception ex) {
				PuakmaLibraryManager.log(ex);
			}
		}
	}

	@SuppressWarnings("unused")
	private DesignObjectImpl[] listDesignObjectImpls()
	{
		synchronized(dobjects) {
			return (DesignObjectImpl[]) dobjects.toArray(new DesignObjectImpl[dobjects.size()]);
		}
	}

	DatabaseConnectionImpl[] listDatabaseImpls()
	{
		synchronized(dbConnections) {
			return dbConnections.toArray(new DatabaseConnectionImpl[dbConnections.size()]);
		}
	}

	public void commit() throws PuakmaCoreException
	{
		if(isRemoved())
			throw new PuakmaCoreException("Cannot commit removed application");
		if((isNew() || isWorkingCopy()) == false)
			throw new PuakmaCoreException("Cannot commit application, it has to be working copy");

		synchronized(this) {
			try {
				AppDesigner designer = getAppDesigner();
				long newId = designer.saveApplication(getId(), getGroup(), getName(), getInheritFrom(),
						getTemplateName(), getDescription());
				if(getId() != newId)
					setId(newId);
				// IF THE APPLICATION IS NEW, WE SHOULD ALSO UPDATE PARAMETERS
				if(isNew()) {
					id = newId;
					params.commitParams();
				}
				if(isNew() == false)
					((ApplicationImpl)original).copyFromWorkingCopy(this);

				setValid();
				params.setValid();
			}
			catch(Exception e) {
				throw PuakmaLibraryUtils.handleException("Cannot save application to server", e);
			}
		}
	}

	public String getGroup()
	{
		return group;
	}

	public void setGroup(String group)
	{
		if(group == null)
			throw new IllegalArgumentException("Invalid parameter group - null");
		//group = StringUtil.EMPTY_STRING;

		if(group.equals(this.group) == false) {
			this.group = group;
			setDirty(true);
		}
	}

	public String getFQName()
	{
		return (group == null ? "" : group + "/") + getName();
	}

	public long getId()
	{
		return id;
	}

	/**
	 * Sets the new id for this object.
	 *
	 * @param id is the new identifier
	 */
	public void setId(long id)
	{
		if(id != this.id) {
			this.id = id;
			setDirty(true);
		}
	}

	public String getInheritFrom()
	{
		return inheritFrom;
	}

	public void setInheritFrom(String inherited)
	{
		this.inheritFrom = inherited;
	}

	public String getTemplateName()
	{
		return templateName;
	}

	public void setTemplateName(String template)
	{
		this.templateName = template;
	}

	/**
	 * Updates application information from other bean. The only updated infos can be name,
	 * description, etc...
	 *
	 * @param app is the application from which we update this one
	 */
	void updateFromBean(Application app)
	{
		setId(app.getId());
		setDescription(app.getDescription());
		setGroup(app.getGroup());
		setInheritFrom(app.getInheritFrom());
		setName(app.getName());
		setTemplateName(app.getTemplateName());

		if(isDirty())
			fireAppChange();

		setDirty(false);
	}

	/**
	 * Fires change event on this application
	 */
	private void fireAppChange()
	{

	}

	public void addObject(ApplicationObject object) throws PuakmaCoreException
	{
		addObject(object, false);
	}

	public void addObject(ApplicationObject object, boolean isRefresh) throws PuakmaCoreException
	{
		if(object == null)
			throw new IllegalArgumentException("Cannot add null object");
		if(object.isWorkingCopy())
			throw new PuakmaCoreException("Object cannot be working copy.");
		Server server = getServer();
		if(server != null) {
			TornadoDatabaseConstraints cons = server.getTornadoDatabaseConstraints();
			if(cons.getMaxDObj_NameLen() < object.getName().length())
				throw new PuakmaCoreException("Name of the object \"" + object.getName() + "\" is longer than the server allows");
		}
		//    if(object.isValid() && object.getApplication() != null)
		//      throw new PuakmaCoreException("Object is already assigned to some application");

		if(object.getApplication() == null)
			((ApplicationObjectImpl)object).setApplication(this);

		if(object instanceof DesignObjectImpl) {
			DesignObjectImpl dobj = (DesignObjectImpl) object;
			synchronized(dobjects) {
				if(dobj.getDesignType() == DesignObject.TYPE_LIBRARY && dobj.getName().length() == 0)
					DesignObjectsStore.setupNonConflictJavaObjectName((JavaObject) dobj, dobjects,
							getServer().getTornadoDatabaseConstraints());

				// ENABLE TWO SAME NAMED DESIGN OBJECTS IF REFRESHING, BUT NOT OTHERWISE
				if(getDesignObject(dobj.getName()) != null)
					throw new PuakmaCoreException("Object '" + object.getName() + "' already exists");
				// AND ALSO CHECK CONSISTENCY OF JAVA OBJECTS
				if(dobj instanceof JavaObject) {
					JavaObject jo = (JavaObject) dobj;
					if(getJavaObject(jo.getPackage(), jo.getClassName()) != null)
						throw new PuakmaCoreException("Class " + jo.getFullyQualifiedName() + " already exists");
				}

				dobj.setApplication(this);

				if(dobj.isNew())
					dobj.commit();

				dobjects.add(dobj);
				dobj.setValid();
			}

			fireAddObject(dobj, isRefresh);
		}
		else if(object instanceof RoleImpl) {
			RoleImpl role = (RoleImpl) object;
			synchronized(roles) {
				if(getRole(role.getName()) != null)
					throw new PuakmaCoreException("Role " + object.getName() + " already exists");

				role.setApplication(this);

				// commit object only and only if we are open
				if(role.isNew()) {
					role.commit();
				}

				roles.add(role);
				role.setValid();
			}

			fireAddObject(role, isRefresh);
		}
		else if(object instanceof KeywordImpl) {
			KeywordImpl kw = (KeywordImpl) object;
			synchronized(keywords) {
				if(getKeyword(kw.getName()) != null)
					throw new PuakmaCoreException("Keyword " + object.getName() + " already exists");

				kw.setApplication(this);

				if(kw.isNew()) {
					kw.commit();
					// TODO: what about commiting data???
				}

				keywords.add(kw);
				kw.setValid();

				fireAddObject(kw, isRefresh);
			}
		}
		else if(object instanceof DatabaseConnectionImpl) {
			DatabaseConnectionImpl db = (DatabaseConnectionImpl) object;
			synchronized(dbConnections) {
				if(getDatabaseConnection(db.getName()) != null)
					throw new PuakmaCoreException("Object " + object.getName() + " already exists in system");

				if(db.isNew()) {
					db.commit();
				}

				dbConnections.add(db);
				db.setValid();
			}
			fireAddObject(db, isRefresh);
		}
		else
			throw new IllegalArgumentException("Unknow application object " + object.getName() + " class: " + object.getClass().getName());
	}

	public void removeObject(ApplicationObject object) throws PuakmaCoreException, IOException
	{
		if(object.isValid() == false)
			throw new PuakmaCoreException("Object is not valid");
		if(object.isWorkingCopy())
			throw new PuakmaCoreException("Object cannot be working copy");
		if(object.getApplication() != this)
			throw new PuakmaCoreException("Object doesn't belong to this application");

		object.remove();
	}

	AppDesigner getAppDesigner()
	{
		return appDesigner;
	}

	/**
	 * This functions returns download designer for this application object.
	 * @return DownloadDesigner object
	 */
	public DownloadDesigner getDownloadDesigner()
	{
		synchronized(downloadDesigner) {
			while(true) {
				for(int i = 0; i < downloadDesigner.length; ++i) {
					if(downloadDesigner[i] != null) {
						DownloadDesigner des = downloadDesigner[i];
						downloadDesigner[i] = null;
						return des;
					}
				}

				try { downloadDesigner.wait(); } catch(Exception e) { e.printStackTrace(); }
			} // while
		} // synchronized
	}

	public void returnDesigner(DownloadDesigner designer)
	{
		if(designer == null)
			return;

		synchronized(downloadDesigner) {
			for(int i = 0; i < downloadDesigner.length; ++i) {
				if(downloadDesigner[i] == null) {
					downloadDesigner[i] = designer;
					downloadDesigner.notifyAll();
					return;
				}
			}

			// hmmm... some error???
			downloadDesigner.notifyAll();
		}
	}

	public void remove() throws PuakmaCoreException
	{
		if(isRemoved())
			throw new PuakmaCoreException("Cannot remove removed application.");
		if(isWorkingCopy())
			throw new PuakmaCoreException("Cannot remove working copy");

		if(isOpen())
			close();

		synchronized(this) {
			try {
				ServerDesigner designer = server.getServerDesigner();
				designer.removeApplication(getId());
				setRemoved();
				server.notifyRemove(this);
			}
			catch(Exception e) {
				throw PuakmaLibraryUtils.handleException(e);
			}
		}
	}

	public Role[] listRoles()
	{
		synchronized(roles) {
			Role[] roles = new Role[this.roles.size()];
			roles = this.roles.toArray(roles);
			return roles;
		}
	}

	/**
	 * Refreshes roles.
	 *
	 * @param bean is the data from the server
	 * @param info 
	 */
	private void refreshRoles(ApplicationStructureBean bean, RefreshEventInfoImpl info)
	{
		List<RoleImpl> removed;
		synchronized(roles) {
			for(ApplicationStructureBean.Role aRole : bean.roles) {
				RoleImpl current = (RoleImpl) getRole(aRole.id);
				boolean isNew = current == null ? true : false;

				RoleImpl role = (RoleImpl) ObjectsFactory.createRole(aRole.name, aRole.roleDescription);
				role.setId(aRole.id);
				try {
					for(ApplicationStructureBean.Role.Permission aPerm : aRole.permissions)
						role.importPermission(aPerm.permId, aPerm.permName, aPerm.permDescription);

					if(isNew) {
						role.setValid();
						addObject(role);
					}
					else {
						synchronized(current) {
							current.refreshFrom(role);
							if(current.isDirty())
								current.setDirty(false);
						}
					}
				}
				catch(PuakmaCoreException e) {
					// no exception can occur because we are still not connected, but log it!
					PuakmaLibraryManager.log(e);
				}
			}

			//    remove objects which are not anymore on the server
			removed = CollectionsUtil.firstMinusSecond(roles, bean.roles,
					new EqualsComparator<RoleImpl, ApplicationStructureBean.Role>() {
				public boolean equals(RoleImpl dbo, ApplicationStructureBean.Role dbb) {
					return dbo.getId() == dbb.id;
				}
			});
		}
		// remove them all!!!
		Iterator<RoleImpl> it = removed.iterator();
		while(it.hasNext()) {
			try {
				RoleImpl dbo = it.next();
				dbo.setRemoved();
				dbo.remove();
			}
			catch(PuakmaCoreException e) {
				PuakmaLibraryManager.log(e);
			}
		}
	}

	/**
	 * Refreshes all the keywords.
	 *
	 * @param bean is the data from the server
	 * @param info 
	 */
	private void refreshKeywords(ApplicationStructureBean bean, RefreshEventInfoImpl info)
	{
		Iterator<KeywordImpl> it;
		List<KeywordImpl> removed;
		synchronized(keywords) {
			for(ApplicationStructureBean.Keyword kwb : bean.keywords) {
				// FIND EXISTING KEYWORD
				boolean isNew = false;
				KeywordImpl current = (KeywordImpl) getKeyword(kwb.id);
				if(current == null)
					isNew = true;

				List<ApplicationStructureBean.Keyword.KeywordData> l = new ArrayList<ApplicationStructureBean.Keyword.KeywordData>(kwb.datas);
				List<String> dataList = new ArrayList<String>();
				// sort values
				Collections.sort(l, new Comparator<ApplicationStructureBean.Keyword.KeywordData>() {
					public int compare(ApplicationStructureBean.Keyword.KeywordData d1, ApplicationStructureBean.Keyword.KeywordData d2)
					{
						return d1.order - d2.order;
					}
					public boolean equals(Object obj)
					{
						return obj == this;
					}
				});

				for(ApplicationStructureBean.Keyword.KeywordData data : l)
					dataList.add(data.data);

				KeywordImpl kw = (KeywordImpl) ObjectsFactory.createKeyword(kwb.name);
				kw.initialize(this, kwb.id, dataList);
				try {
					if(isNew) {
						kw.setValid();
						addObject(kw);
					}
					else {
						synchronized(current) {
							current.refreshFrom(kw);
							if(current.isDirty()) {
								current.setDirty(false);
							}
						}
					}
				}
				catch(PuakmaCoreException e) {
					PuakmaLibraryManager.log(e);
				}
			}

			// remove objects which are not anymore on the server
			removed = CollectionsUtil.firstMinusSecond(keywords, bean.keywords,
					new EqualsComparator<KeywordImpl,ApplicationStructureBean.Keyword>() {
				public boolean equals(KeywordImpl dbo, ApplicationStructureBean.Keyword dbb) {
					return dbo.getId() == dbb.id;
				}
			});
		}
		// remove them all!!!
		it = removed.iterator();
		while(it.hasNext()) {
			try {
				KeywordImpl dbo = it.next();
				dbo.setRemoved();
				dbo.remove();
			}
			catch(PuakmaCoreException e) {
				PuakmaLibraryManager.log(e);
			}
		}
	}

	/**
	 * Refreshes all database connection objects
	 *
	 * @param bean is the data from the server
	 * @param info 
	 */
	private void refreshDatabases(ApplicationStructureBean bean, RefreshEventInfoImpl info)
	{
		List<DatabaseConnectionImpl> removed;
		synchronized(dbConnections) {
			for(ApplicationStructureBean.Database dbb : bean.dbConnections)
				refreshOneDatabase(dbb);

			// remove objects which are not anymore on the server
			removed = CollectionsUtil.firstMinusSecond(dbConnections, bean.dbConnections,
					new EqualsComparator<DatabaseConnectionImpl, ApplicationStructureBean.Database>() {
				public boolean equals(DatabaseConnectionImpl dbo, ApplicationStructureBean.Database dbb) {
					return dbo.getId() == dbb.id;
				}
			});
		} // synchronized
		Iterator<DatabaseConnectionImpl> it = removed.iterator();
		while(it.hasNext()) {
			try {
				DatabaseConnectionImpl dbo = it.next();
				dbo.setRemoved();
				dbo.remove();
			}
			catch(PuakmaCoreException e) {
				PuakmaLibraryManager.log(e);
			}
		}
	}

	/**
	 * Refreshes one database from database bean.
	 */
	private void refreshOneDatabase(ApplicationStructureBean.Database dbb)
	{
		// check if this is new object or not
		boolean isNew = false;
		DatabaseConnectionImpl current = getDatabaseConnection(dbb.id);
		if(current == null)
			isNew = true;

		DatabaseConnectionImpl dbi = new DatabaseConnectionImpl(null);
		dbi.setId(dbb.id);
		dbi.setName(dbb.conName);
		dbi.setDatabaseName(dbb.dbName);
		dbi.setDatabaseUrlOptions(dbb.options);
		dbi.setDatabaseUrl(dbb.url);
		dbi.setDescription(dbb.comment);
		dbi.setDriverClass(dbb.driverClass);
		dbi.setPassword(dbb.pwd);
		dbi.setUserName(dbb.userName);

		try {
			if(isNew) {
				dbi.setValid();
				addObject(dbi);
			}
			else {
				synchronized(current) {
					current.refreshFrom(dbi);
					if(current.isDirty()) {
						current.setDirty(false);
					}
				}
			}
		}
		catch(PuakmaCoreException e) {
			PuakmaLibraryManager.log(e);
		}
	}

	private void refreshDesignObjects(ApplicationStructureBean bean, RefreshEventInfoImpl info)
	{
		List<DesignObjectImpl> removed;

		synchronized(dobjects) {
			// remove objects which are not anymore on the server
			removed = dobjects.substractBeans(bean.designObjects);
			//it = removed.iterator();
			//while(it.hasNext()) {
			for(DesignObjectImpl dbo : removed) {
				try {
					//DesignObjectImpl dbo = (DesignObjectImpl) it.next();
					dbo.setRemoved();
					dbo.remove();
					info.addRemoved(dbo);
				}
				catch(PuakmaCoreException e) {
					PuakmaLibraryManager.log(e);
				}
			}

			// NOW APPLY UPDATE, AND ADDITIONS
			ADD_LOOP: for(ApplicationStructureBean.DObject dbean : bean.designObjects) {
				// CHECK IF INPUT IS VALID, AND IN SIMPLE CASES TRY TO RECOVER IT
				if(isValidDObjectBean(dbean) == false)
					continue ADD_LOOP;
				if(isValidDObjectBeanInsideContext(dbean) == false)
					continue ADD_LOOP;

				// check if this is new object or not
				boolean isNew = false;
				DesignObjectImpl current = (DesignObjectImpl) getDesignObject(dbean.id);
				if(current == null)
					isNew = true;
				// ALSO CHECK IF THIS HAS NOT BEEN IGNORED AS IT HAS THE SAME NAME
				// AS SOME OTHER DESIGN OBJECT
				if(isNew && getDesignObject(dbean.name) != null)
					continue ADD_LOOP;

				// TODO: what to do with this??? is the next if ok???
				int type = dbean.designType;
				if(dbean.isLibrary)
					type = DesignObject.TYPE_JAR_LIBRARY;

				try {
					if(isNew) {
						DesignObjectImpl dbo = ObjectsFactory.setupDesignObjectFromBean(dbean, info);
						addObject(dbo, true);
					}
					else
						current.refreshFrom(dbean, info, false);
				}
				catch(PuakmaCoreException e) {
					PuakmaLibraryManager.log(e);
				}
			} // while
		} // synchronized
	}

	/**
	 * Validates design object within the context of the {@link Application}. In
	 * simple cases it tries to recover the design object.
	 * 
	 * <p>
	 * Cases in which it tries to recover:
	 * <ul>
	 * <li>class name is empty - this might be caused by invalid upload to the
	 * server, so keep the old class name
	 * </ul>
	 * 
	 * @param dbean is the design object to validate
	 * @return true if the design object bean is consistent, false otherwise
	 */
	private boolean isValidDObjectBeanInsideContext(DObject dbean)
	{
		if(dbean.isLibrary == false && PuakmaLibraryUtils.isValidJavaObjectType(dbean.designType)) {
			if(dbean.className.length() == 0) {
				DesignObject o = getDesignObject(dbean.id);
				if(o instanceof JavaObject) {
					JavaObject jo = (JavaObject) o;
					dbean.className = jo.getClassName();
				}
				else
					return false;
			}
		}

		return true;
	}

	/**
	 * This function validates dbean object if it contains valid values
	 * 
	 * @param dbean is the database bean which is got from the server
	 * @return true if bean is ok, and we can put it to the application
	 */
	static boolean isValidDObjectBean(DObject dbean)
	{
		// CHECK VALID DATABASE ID
		if(dbean.id < 0)
			return false;

		// CHECK VALID NAME
		if(dbean.name == null || dbean.name.length() == 0)
			return false;

		// CHECK VALID DESIGN TYPE
		if(ObjectsFactory.isValidDesignObjectType(dbean.designType) == false)
			return false;
		// ALSO CHECK JAR_LIBRARY WHICH IS NOT VALID OBJECT ON THE SERVER IT IS VORTEX
		// INTERNAL TYPE, NOT SOAPDESIGNER'S
		if(dbean.designType == DesignObject.TYPE_JAR_LIBRARY)
			return false;

		if(PuakmaLibraryUtils.isValidJavaObjectType(dbean.designType)) {
			// TRY TO RECOVER THIS FROM THE OLD VALUE
			if(dbean.className == null) {
				return false;
			}
			if(dbean.packageName == null)
				dbean.packageName = StringUtil.EMPTY_STRING;
		}

		return true;
	}

	public DesignObject getDesignObject(long id)
	{
		synchronized(dobjects) {
			Iterator<DesignObjectImpl> it = dobjects.iterator();
			while(it.hasNext()) {
				DesignObject dobj = it.next();
				if(dobj.getId() == id)
					return dobj;
			}
			return null;
		}
	}

	public DesignObject getDesignObject(String name)
	{
		synchronized(dobjects) {
			Iterator<DesignObjectImpl> it = dobjects.iterator();
			while(it.hasNext()) {
				DesignObject dobj = it.next();
				if(dobj.getName().equals(name))
					return dobj;
			}
			return null;
		}
	}

	/**
	 * Gets role by identifier.
	 *
	 * @param id is the identifier of the role we want
	 * @return Role object or null if there is no role with identifier id
	 */
	private Role getRole(long id)
	{
		synchronized(roles) {
			for(RoleImpl role : roles) {
				if(role.getId() == id)
					return role;
			}
			return null;
		}
	}

	/**
	 * Gets database connectin by id
	 *
	 * @param id is id of database connection for which are we looking for
	 * @return DatabaseConnectionImpl instance or null if there is no object with this id
	 */
	private DatabaseConnectionImpl getDatabaseConnection(long id)
	{
		synchronized(dbConnections) {
			for(DatabaseConnectionImpl dbo : dbConnections) {
				if(dbo.getId() == id)
					return dbo;
			}
		}
		return null;
	}

	/**
	 * Gets keyword by it's identifier.
	 *
	 * @param id is the identifier of the keyword
	 * @return Keyword object or null if there is no keyword with the identifier
	 */
	private Keyword getKeyword(long id)
	{
		synchronized(keywords) {
			for(KeywordImpl kw : keywords) {
				if(kw.getId() == id)
					return kw;
			}
			return null;
		}
	}

	public Role getRole(String roleName)
	{
		synchronized(roles) {
			Iterator it = roles.iterator();
			while(it.hasNext()) {
				Role role = (Role) it.next();

				if(roleName.equals(role.getName()))
					return role;
			}
			return null;
		}
	}

	/**
	 * This function should be called always when some object is removed
	 * from application. This function should only remove object from internal
	 * structures, it doesn't touch server. This function sets refresh parameter for
	 * notification to false.
	 *
	 * @param object is the object which is about to be removed from application
	 */
	void notifyRemove(ApplicationObjectImpl object)
	{
		notifyRemove(object, false);
	}

	/**
	 * This function should be called always when some object is removed
	 * from application. This function should only remove object from internal
	 * structures, it doesn't touch server.
	 *
	 * @param object is the object which is about to be removed from application
	 * @param isRefresh specifies if the notification is sent by application refresh
	 */
	void notifyRemove(ApplicationObjectImpl object, boolean isRefresh)
	{
		if(object instanceof RoleImpl) {
			synchronized(roles) {
				roles.remove(object);
			}

			fireRemoveEvent(object, isRefresh);
		}
		else if(object instanceof KeywordImpl) {
			synchronized(keywords) {
				keywords.remove(object);
			}

			fireRemoveEvent(object, isRefresh);
		}
		else if(object instanceof DatabaseConnectionImpl) {
			synchronized(dbConnections) {
				dbConnections.remove(object);
			}

			fireRemoveEvent(object, isRefresh);
		}
		else if(object instanceof DesignObjectImpl) {
			synchronized(dobjects) {
				dobjects.remove((DesignObjectImpl) object);
			}

			fireRemoveEvent(object, isRefresh);
		}
		else
			throw new UnsupportedOperationException("Invalid object class - " + object.getClass().getName() +" - still unsupported");
	}

	public Keyword[] listKeywords()
	{
		synchronized(keywords) {
			Keyword[] kwds = new Keyword[keywords.size()];
			return keywords.toArray(kwds);
		}
	}

	public Keyword getKeyword(String kwName)
	{
		synchronized(keywords) {
			for(KeywordImpl kw : keywords) {
				if(kw.getName().equals(kwName))
					return kw;
			}

			return null;
		}
	}

	public boolean isClosed()
	{
		return open == false ? true : false;
	}

	public DatabaseConnection[] listDatabases()
	{
		synchronized(dbConnections) {
			return dbConnections.toArray(new DatabaseConnection[dbConnections.size()]);
		}
	}

	public DatabaseConnection getDatabaseConnection(String dbconName)
	{
		synchronized(dbConnections) {
			Iterator<DatabaseConnectionImpl> it = dbConnections.iterator();
			while(it.hasNext()) {
				DatabaseConnection dbo = it.next();
				if(dbconName.equals(dbo.getName()))
					return dbo;
			}
			return null;
		}
	}

	public RefreshEventInfoImpl refresh() throws PuakmaCoreException
	{
		RefreshEventInfoImpl info = new RefreshEventInfoImpl();

		try {
			ApplicationStructureBean bean = loadAppStructureFromServer();

			setGroup(bean.appGroup);
			setName(bean.appName);
			setDescription(bean.description);
			setId(bean.appId);
			setInheritFrom(bean.inheritFrom);
			setTemplateName(bean.templateName);

			params.refreshFrom(bean.params);

			// TODO: uncomment this when have implemented inconsistencies
			//if(refreshCount == 0)
			//  resolveInconsistencies(bean);

			// PARSE ROLES
			refreshRoles(bean, info);

			// PARSE KEYWORDS
			refreshKeywords(bean, info);

			// PARSE DATABASES
			refreshDatabases(bean, info);

			// PARSE DESIGN OBJECTS
			refreshDesignObjects(bean, info);

			// AND PARSE SERVER-SIDE SYSTEM PROPERTIES
			refreshSystemProperties(bean, info);

			// CLEAR SOME FLAGS, AND SETUP APPLICATION AS CONNECTED
			setDirty(false);
			open = true;

			if(PuakmaLibraryManager.DEBUG_MODE) {
				checkDesignObjectsConsistency();
			}
			// TODO: fire some event

			//      connection.setupApplication(bean);

			//      connection.refresh();

			return info;
		}
		catch(Exception e) {
			PuakmaLibraryManager.log(e);
			throw PuakmaLibraryUtils.handleException(e);
		}
	}

	/**
	 * Checks the all design objects consistency. Whether they have setup the correct name,
	 * ids, applications, etc...
	 */
	private void checkDesignObjectsConsistency()
	{
		synchronized(dobjects) {
			for(DesignObjectImpl dob : dobjects) {
				if(dob.getApplication() == null)
					printInconsistency("Application is null", dob);
				if(dob.getId() < 0)
					printInconsistency("Invalid id: " + dob.getId(), dob);
				if(dob.status != DesignObjectImpl.STATUS_VALID)
					printInconsistency("Status is not valid, but: " + dob.status, dob);
			}
		}
	}

	/**
	 * Prints the inconistency for the design object.
	 */
	private void printInconsistency(String message, DesignObjectImpl dob)
	{
		System.out.println("INCONSISTENCY in " + dob + " : " + message);
	}

	/**
	 * Loads {@link ApplicationStructureBean} object representing structure of the
	 * application from the server.
	 */
	ApplicationStructureBean loadAppStructureFromServer() throws IOException, SOAPFaultException,
	SAXException, ParserConfigurationException
	{
		String xml = getAppDesigner().getApplicationXml(id);
		if(VortexPlugin.DEBUG_MODE)
			logApplicationXmlToFile(xml);
		XMLWholeApplicationParser handler = new XMLWholeApplicationParser();
		XmlUtils.parseXml(handler, xml);
		ApplicationStructureBean bean = handler.getApplication();
		return bean;
	}

	/**
	 * This functions resolves all inconsistencies in the application structure.
	 * This means that it asks user what does he want to do with those
	 * inconsistencies.
	 * TODO: finish implementation of this!
	 */
	private ApplicationStructureBean resolveInconsistencies(ApplicationStructureBean bean) throws IOException,
	SOAPFaultException, SAXException, ParserConfigurationException
	{
		if(1 == 1)
			return bean;
		else {
			InconsistenciesListImpl inconsList = new InconsistenciesListImpl(this, bean);
			if(inconsList.countInconsistencies() > 0) {
				ServerManager.fireResolveInconsistencies(inconsList);
				return loadAppStructureFromServer();
			}
			else
				return bean;
		}
	}

	/**
	 * Logs the xml file of the application to file with the current day, and time
	 * as a name.
	 */
	private void logApplicationXmlToFile(String xml)
	{
		Calendar c = Calendar.getInstance();
		FileOutputStream fos = null;
		try {
			String fileName = "log_" + c.get(Calendar.DAY_OF_MONTH) + '_' + c.get(Calendar.MONTH)
					+ '_' + c.get(Calendar.HOUR_OF_DAY) + '_' + c.get(Calendar.MINUTE);
			fos = new FileOutputStream(new File(fileName));
			if(xml == null)
				fos.write("error - null".getBytes("UTF-8"));
			else
				fos.write(xml.getBytes("UTF-8"));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			if(fos != null)
				try { fos.close(); }
			catch(IOException e2) { }
		}
	}

	/**
	 * Refreshes puakma server's system properties information.
	 * @see System#getProperties()
	 */
	private void refreshSystemProperties(ApplicationStructureBean bean, RefreshEventInfoImpl info)
	{
		synchronized(systemProperties) {
			// IGNORE REFRESHING OF ALREADY REFRESHED ITEMS
			if(systemProperties.size() > 0)
				return;

			for(StringNameValuePair pair : bean.sysProps) {
				systemProperties.put(pair.getName(), pair.getValue());
			}
		}
	}

	public void addListener(ApplicationListener listener)
	{
		if(isWorkingCopy()) {
			((ApplicationImpl)original).addListener(listener);
			return;
		}

		synchronized(listeners) {
			if(listeners.contains(listener) == false)
				listeners.add(listener);
		}

		// FIRE EVENT WHICH ADDS ALL THE DESIGN OBJECTS
		DesignObject[] dobjs = listDesignObjects();
		for(int j = 0; j < dobjs.length; ++j) {
			try {
				ObjectChangeEvent event = new ObjectChangeEvent(ObjectChangeEvent.EV_ADD_APP_OBJECT, dobjs[j], false);
				listener.objectChange(event);
			}
			catch(Exception e) {
				PuakmaLibraryManager.log(e);
			}
		}
	}

	/**
	 * This function lists all the <code>ApplicationListeners</code> signed up to listen
	 * for events.
	 *
	 * @return array with all the copied <code>ApplicationListener</code> objects.
	 */
	private ApplicationListener[] listApplicationListeners()
	{
		synchronized(listeners) {
			ApplicationListener[] listeners = new ApplicationListener[this.listeners.size()];
			listeners = this.listeners.toArray(listeners);
			return listeners;
		}
	}

	public void removeListener(ApplicationListener listener)
	{
		if(isWorkingCopy()) {
			((ApplicationImpl)original).removeListener(listener);
			return;
		}

		synchronized(listeners) {
			listeners.remove(listener);
		}
	}

	public DesignObject[] listDesignObjects()
	{
		synchronized(dobjects) {
			return (DesignObject[]) dobjects.toArray(new DesignObject[dobjects.size()]);
		}
	}

	public DesignObject[] listDesignObjectsByType(int type)
	{
		synchronized(dobjects) {
			List<DesignObject> l = new ArrayList<DesignObject>();
			Iterator<DesignObjectImpl> it = dobjects.iterator();
			while(it.hasNext()) {
				DesignObject dob = it.next();
				if(dob.getDesignType() == type)
					l.add(dob);
			}

			return l.toArray(new DesignObject[l.size()]);
		}
	}

	public DesignObject[] listDesignObjects(FilterMatcher<DesignObject> matcher)
	{
		assert matcher != null : "Argument matcher cannot be null";

		DesignObject[] res = listDesignObjects();
		List<DesignObject> l = new ArrayList<DesignObject>();
		for(DesignObject dobj : res) {
			if(matcher.matches(dobj))
				l.add(dobj);
		}
		return l.toArray(new DesignObject[l.size()]);
	}

	public JavaObject getJavaObject(String packageName, String className)
	{
		synchronized(dobjects) {
			Iterator<DesignObjectImpl> it = dobjects.iterator();
			while(it.hasNext()) {
				DesignObjectImpl dobj = it.next();
				if(dobj instanceof JavaObjectImpl) {
					JavaObjectImpl jo = (JavaObjectImpl) dobj;
					if(StringUtil.compareStrings(jo.getClassName(), className) &&
							StringUtil.compareStrings(jo.getPackage(), packageName))
						return jo;
				}
			}
		}

		return null;
	}

	void copyFromWorkingCopy(ApplicationImpl wCopy)
	{
		super.copyFromWorkingCopy(wCopy);
		this.templateName = wCopy.templateName;
		this.inheritFrom = wCopy.inheritFrom;
		this.group = wCopy.group;
	}

	public Application makeWorkingCopy()
	{
		ApplicationImpl wCopy = new ApplicationImpl((ServerImpl) getServer());
		synchronized(this) {
			super.makeCopy(wCopy);
			setupAsWorkingCopy(wCopy);
			wCopy.group = this.group;
			wCopy.inheritFrom = this.inheritFrom;
			wCopy.templateName = this.templateName;
			wCopy.params = this.params.makeWorkingCopy();
		}
		return wCopy;
	}

	public void addParameter(String name, String value)
	{
		params.addParameter(name, value);
	}

	public void setParameter(String name, String value)
	{
		params.setParameter(name, value);
	}

	public void setParameters(String name, String[] values)
	{
		params.setParameters(name, values);
	}

	public String getParameterValue(String name)
	{
		return params.getParameterValue(name);
	}

	public String[] getParameterValues(String name)
	{
		return params.getParameterValues(name);
	}

	public String[] listParameters()
	{
		return params.listParameters();
	}

	public void commitParams() throws PuakmaCoreException
	{
		params.commitParams();
	}

	public void removeParameter(String name)
	{
		params.removeParameter(name);
	}

	public void removeParameterValue(String name, String value)
	{
		params.removeParameterValue(name, value);
	}

	public boolean isReservedPageProperty(String name)
	{
		return params.isReservedPageProperty(name);
	}

	public boolean isReservedAppProperty(String name)
	{
		return params.isReservedAppProperty(name);
	}

	//------------------------------------------------------------------------------
	//                        EVENT FIREING MANAGEMENT
	//==============================================================================

	void fireAddObject(ApplicationObject object, boolean isRefresh)
	{
		fireEvent(PROP_NEW_OBJECT, null, object);

		ApplicationListener[] ls = listApplicationListeners();
		for(ApplicationListener listener : ls) {
			try {
				ObjectChangeEvent event = new ObjectChangeEvent(ObjectChangeEvent.EV_ADD_APP_OBJECT, object, isRefresh);
				listener.objectChange(event);
			}
			catch(Exception e) {
				PuakmaLibraryManager.log(e);
			}
		}
	}

	void fireRemoveEvent(ApplicationObject object, boolean isRefresh)
	{
		if(object instanceof DesignObject)
			fireEvent(PROP_REMOVE_OBJECT, null, object);

		ApplicationListener[] ls = listApplicationListeners();
		for(ApplicationListener listener : ls) {
			try {
				ObjectChangeEvent event = new ObjectChangeEvent(ObjectChangeEvent.EV_REMOVE,
						object, isRefresh);
				listener.objectChange(event);
			}
			catch(Exception e) {
				PuakmaLibraryManager.log(e);
			}
		}
	}

	public void fireUpdateObject(DesignObjectImpl object, boolean isNew, String oldName,
			String oldPackage, String oldClass, boolean isRefresh)
	{
		if(object == null)
		{
			Logger.logException(new NullPointerException());
			return;
		}
		
		// TODO: change this function to not to call this for new objects
		if(isNew == false) {
			if(oldName.equals(object.getName()) == false)
				fireEvent(PROP_RENAME_OBJECT, oldName, object);
			if(object instanceof JavaObject) {
				JavaObject jo = (JavaObject) object;
				if(jo.getClassName().equals(oldClass) == false || jo.getPackage().equals(oldPackage) == false) {
					String oldFQClassName = (oldPackage != null && oldPackage.length() > 0) ? (oldPackage + "." + oldClass) : oldClass;
					fireEvent(PROP_DESIGN_OBJECT_CLASS_NAME_CHANGE, oldFQClassName, object);
				}
			}
		}

		ApplicationListener[] ls = listApplicationListeners();
		for(ApplicationListener listener : ls) {
			try {
				ObjectChangeEvent event = new ObjectChangeEvent(ObjectChangeEvent.EV_CHANGE, object, isRefresh);
				event.setOldClass(oldClass);
				event.setOldPackage(oldPackage);
				event.setOldName(oldName);
				listener.objectChange(event);
			}
			catch(Exception ex) {
				PuakmaLibraryManager.log(ex);
			}
		}
	}

	/**
	 * Creates new, and unique name for the java object without name. Note that
	 * constraints for names are those: name has to be maximally 30 characters long.
	 *
	 * <p>TODO: improve this!!!
	 * @param jo is the object for which we generate the name
	 * @return String with the new name or existing name if the java object has some
	 */
	String generateNameForClass(JavaObject jo)
	{
		if(jo.getName() != null)
			return jo.getName();

		// TODO: add some assertions like jo is new, etc...
		//    synchronized(dobjects) {
		if(jo.getPackage() == null || jo.getPackage().length() == 0) {
			String name = jo.getClassName();
			if(name.length() > 30)
				name = name.substring(0, 29);
			return name;
		}
		else {
			String name = jo.getClassName();
			String packageName = jo.getPackage();
			String[] names = packageName.split(".");
			int i = names.length - 1;
			while(i >= 0) {
				String name1 = packageName + "." + name;
				if(name1.length() <= 30)
					name = name1;
				else
					break;
			}

			return name;
		}
		//    }
	}

	public void flushDesignCache() throws PuakmaCoreException
	{
		try {
			appDesigner.flushDesignCache();
		}
		catch(Exception e) {
			throw PuakmaLibraryUtils.handleException("Cannot flush design cache", e);
		}
	}

	public ConnectionPrefsReader getConnectionPrefs()
	{
		ConnectionPrefs prefs = server.getConnectionPrefs();

		prefs.setApplication(StringUtil.safeString(getName()));
		prefs.setGroup(StringUtil.safeString(getGroup()));    

		return prefs;
	}

	public String getProperty(String name)
	{
		synchronized(systemProperties) {
			return systemProperties.get(name);
		}
	}
}
