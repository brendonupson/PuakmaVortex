/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Dec 21, 2004
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.views.navigator;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

import puakma.coreide.FilterMatcher;
import puakma.coreide.PuakmaCoreException;
import puakma.coreide.ServerManager;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.ApplicationEvent;
import puakma.coreide.objects2.ApplicationListener;
import puakma.coreide.objects2.ApplicationObject;
import puakma.coreide.objects2.Database;
import puakma.coreide.objects2.DatabaseConnection;
import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.Keyword;
import puakma.coreide.objects2.ObjectChangeEvent;
import puakma.coreide.objects2.Role;
import puakma.coreide.objects2.Table;
import puakma.coreide.objects2.TableColumn;
import puakma.utils.MimeType;
import puakma.utils.MimeTypesResolver;
import puakma.utils.lang.ArrayUtils;
import puakma.vortex.VortexPlugin;
import puakma.vortex.controls.BaseTreeViewController;
import puakma.vortex.controls.TreeObject;
import puakma.vortex.controls.TreeParent;
import puakma.vortex.preferences.PreferenceConstants;
import puakma.vortex.project.ProjectLifecycleListener;
import puakma.vortex.project.ProjectManager;
import puakma.vortex.project.PuakmaProject2;
import puakma.vortex.views.parts.ATVBaseNode;

/**
 * This class is responsible for providing content with the
 * TODO: use collators from ICU4j 
 * @author Martin Novak
 */
public class ApplicationTreeViewController extends BaseTreeViewController implements PropertyChangeListener
{
	/**
	 * Database object node id.
	 */
	public static final int NODE_APPLICATION = 1;

	public static final int NODE_DATABASES = 2;
	public static final int NODE_PAGES = 3;
	public static final int NODE_RESOURCES = 4;
	public static final int NODE_ACTIONS = 5;
	public static final int NODE_WIDGETS = 6;
	public static final int NODE_SCHEDULED_ACTIONS = 7;
	public static final int NODE_SHARED_CODE = 8;

	//------------------------------------------------------------------------------
	//                        RESOURCE TREE OBJECT IDS
	//==============================================================================
	public static final int NODE_RESOURCES_WEB = 21;
	public static final int NODE_RESOURCES_IMAGES = 22;
	public static final int NODE_RESOURCES_XML = 23;
	public static final int NODE_RESOURCES_DOCUMENTS = 24;
	public static final int NODE_RESOURCES_OTHER = 25;

	//------------------------------------------------------------------------------
	//                        DATABASE TREE OBJECT IDS
	//==============================================================================
	public static final int NODE_DATABASE_OBJECT = 41;
	//  public static final int NODE_DATABASE_TABLE = 42;
	//  public static final int NODE_DATABASE_COLUMN = 43;
	//  public static final int NODE_DATABASE_INDEX = 44;

	/**
	 * All top level nodes, but sorted
	 */
	public static final int[] TOP_NODES_SORTED = {
		NODE_DATABASES, NODE_PAGES, NODE_RESOURCES,
		NODE_ACTIONS, NODE_SCHEDULED_ACTIONS, NODE_WIDGETS,
		NODE_SHARED_CODE,
	};

	/**
	 * All sorted nodes under resources node
	 */
	public static final int[] RES_NODES_SORTED = {
		NODE_RESOURCES_WEB, NODE_RESOURCES_IMAGES, NODE_RESOURCES_XML,
		NODE_RESOURCES_OTHER,
	};

	public static final String KEY_TREE_ADAPTER = "#treeAdapt#";

	private ApplicationTreeViewer viewer;

	private PmaJavaContentProvider javaProvider;

	/**
	 * List of all application listeners. They are unhooked automatically when
	 * application is disconnected.
	 */
	private List<ApplicationListener> appListeners = new ArrayList<ApplicationListener>();

	private InternalProjectLifecycleListener projectLifeListener;

	private IElementChangedListener elementChangeListener;

	private ATVResourceModelChangeListener resourceListener;

	private final class InternalProjectLifecycleListener implements ProjectLifecycleListener
	{
		public void projectEvent(final PuakmaProject2 project, final int eventType, Object param)
		{
			if(eventType == ProjectLifecycleListener.POST_JAVA_START) {
				// final DesignObject[] objects =
				// app.listDesignObjectsByType(DesignObject.TYPE_LIBRARY);
				Display.getDefault().asyncExec(new Runnable() {
					public void run()
					{
						Application app = project.getApplication();
						IFolder folder = ProjectManager.getIProject(app).getFolder(
								PuakmaProject2.DIR_SRC);
						viewer.refresh(folder);
						// viewer.update(objects, null);
					}
				});
			}
		}
	}

	//  final class ThisDatabaseListener implements DatabaseListener
	//  {
	//    private ATVDbConnectionNode node;
	//    
	//    public ThisDatabaseListener(ATVDbConnectionNode node)
	//    {
	//      this.node = node;
	//    }
	//
	//    public void evClose(Database database)
	//    {
	//      // DO NOTHING...
	//    }
	//
	//    public void objectChange(ObjectChangeEvent event)
	//    {
	//      if(event.getObject() instanceof Table) {
	//        final Table table = (Table) event.getObject();
	//        // IGNORE INVALID DATABASES
	//        if(table.getDatabase() != node.getDatabase())
	//          return;
	//        switch(event.getEventType()) {
	//          case ObjectChangeEvent.EV_ADD_APP_OBJECT:
	//            Display.getDefault().asyncExec(new Runnable() {
	//              public void run() {
	//                viewer.refresh(node, false);
	//                //viewer.add(node, tableNode);
	//              }
	//            });
	//          break;
	//        }
	//      }
	//    }
	//  }

	private class ThisApplicationListener implements ApplicationListener
	{
		private Application application;
		private ATVApplicationNode node;

		public ThisApplicationListener(Application application, ATVApplicationNode node)
		{
			this.application = application;
			this.node = node;
		}

		public void disconnect(final Application application)
		{
			Display.getDefault().asyncExec(new Runnable() {
				public void run()
				{
					disconnectApp(application, node);
				}
			});
		}

		public void objectChange(final ObjectChangeEvent event)
		{
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					ApplicationObject object = event.getObject();
					switch(event.getEventType()) {
					case ObjectChangeEvent.EV_CHANGE:
						if(event.isRenamed()) {
							if(object instanceof DesignObject)
								viewer.update(getWrapper((DesignObject) object), null);
							else
								viewer.update(object, null);
						}
						treeUpdateApplicationObject(object, node);
						break;
					case ObjectChangeEvent.EV_ADD_APP_OBJECT:
						treeAddApplicationObject(object, node);
						break;
					case ObjectChangeEvent.EV_REMOVE:
						if(object instanceof DesignObject)
							viewer.remove(getWrapper((DesignObject)object));
						else
							viewer.remove(object);
						break;
					}
				}
			});
		}

		public void update(final ApplicationEvent event)
		{
		}

		/**
		 * This function removes all the references, listeners from this object. Specially database
		 * listeners.
		 */
		 public void close()
		{
			// TODO: repair listeners
			//      Iterator it = dbListeners.iterator();
		//      while(it.hasNext()) {
			 //        ThisDatabaseListener listener = (ThisDatabaseListener) it.next();
			 //        listener.node.getDatabase().removeListener(listener);
			 //        it.remove();
			 //      }
		}
	}

	public ApplicationTreeViewController(ApplicationTreeViewer viewer, boolean isFlat)
	{
		this.collator = Collator.getInstance();
		this.viewer = viewer;

		initialize();
	}

	/**
	 * Initializes all the listeners, and binds them to the correct things
	 */
	protected void initialize()
	{
		root = new TreeParent("",null);

		this.javaProvider = new PmaJavaContentProvider(viewer);

		ServerManager.addListener(this);

		this.elementChangeListener = new ATVJavaModelChangeListener(viewer);
		//JavaCore.addElementChangedListener(elementChangeListener, ElementChangedEvent.POST_RECONCILE);

		projectLifeListener = new InternalProjectLifecycleListener();
		ProjectManager.addLifecycleListener(projectLifeListener);

		this.resourceListener = new ATVResourceModelChangeListener(viewer);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceListener);
	}

	public void dispose()
	{
		ServerManager.removeListener(this);

		// FREE PROJECT LIFECYCLE LISTENER
		ProjectManager.removeLifecycleListener(projectLifeListener);

		// FREE JAVA CHANGE LISTENER
		JavaCore.removeElementChangedListener(elementChangeListener);

		// FREE RESOURCES LISTENER
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceListener);

		// ALSO FREE ALL CONNECTION LISTENERS
		Iterator<ApplicationListener> it = appListeners.iterator();
		while(it.hasNext()) {
			ThisApplicationListener listener = (ThisApplicationListener) it.next();
			listener.close();
			listener.application.removeListener(listener);
			it.remove();
		}
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
	}

	/**
	 * Fires after some application is added.
	 *
	 * @param connection
	 */
	void connectToApp(Application connection)
	{
		final ATVApplicationNode node = new ATVApplicationNode(connection, root);

		// now add all needed parent nodes
		node.databaseNode = new ATVParentNode("Databases", NODE_DATABASES, node);
		node.pagesNode = new ATVParentNode("Pages", NODE_PAGES, node);
		node.resourcesNode = new ATVResourcesNode("Resources", NODE_RESOURCES, node);
		node.actionsNode = new ATVParentNode("Actions", NODE_ACTIONS, node);
		node.scheduledNode = new ATVParentNode("Scheduled Actions", NODE_SCHEDULED_ACTIONS, node);
		node.widgetsNode = new ATVParentNode("Widgets", NODE_WIDGETS, node);
		node.sharedNode = new ATVParentNode("Shared Code", NODE_SHARED_CODE, node);

		// THIS IS CLASS BROWSER
		final IFolder srcFolder = addSourceNode(node, connection);

		node.srcNode = srcFolder;

		// ADD SOME NODES UNDER RESOURCES
		node.resourcesNode.webNode = new ATVParentNode("Web", NODE_RESOURCES_WEB, node.resourcesNode);
		node.resourcesNode.imagesNode = new ATVParentNode("Images", NODE_RESOURCES_IMAGES, node.resourcesNode);
		node.resourcesNode.xmlNode = new ATVParentNode("XML/XSL", NODE_RESOURCES_XML, node.resourcesNode);
		node.resourcesNode.othersNode = new ATVParentNode("Other", NODE_RESOURCES_OTHER, node.resourcesNode);

		viewer.refresh(root);
		viewer.expandToLevel(node, 1);

		// now register this viewer for listening to all application nodes
		ApplicationListener l = new ThisApplicationListener(connection, node);
		appListeners.add(l);
		connection.addListener(l);
	}

	/**
	 * @return all application nodes of all connected applications
	 */
	public ATVApplicationNode[] listApplicationNodes()
	{
		synchronized(this) {
			ATVApplicationNode[] items = new ATVApplicationNode[appListeners.size()];
			Iterator<ApplicationListener> it = appListeners.iterator();
			int i = 0;
			while(it.hasNext()) {
				ThisApplicationListener listener = (ThisApplicationListener) it.next();
				items[i] = listener.node;
				i++;
			}

			return items;
		}
	}

	/**
	 * Adds source node to the tree for displlaying the java sources
	 *
	 * @param node
	 * @param application
	 */
	private IFolder addSourceNode(ATVApplicationNode node, Application application)
	{
		PuakmaProject2 project = ProjectManager.getProject(application);
		IFolder srcFolder = project.getProject().getFolder(PuakmaProject2.DIR_SRC);
		node.children.add(srcFolder);
		return srcFolder;
	}

	/**
	 * Handles disconnecting event from the application.
	 *
	 * @param application is the application being disconnected
	 * @param node is the ATVApplicationNode which should be removed
	 */
	void disconnectApp(Application application, ATVApplicationNode node)
	{
		root.children.remove(node);
		viewer.refresh();

		// REMOVE ALSO DATABASE LISTENERS
		Iterator<ApplicationListener> it = appListeners .iterator();
		while(it.hasNext()) {
			ThisApplicationListener listener = (ThisApplicationListener) it.next();
			if(listener.application.equals(application)) {
				listener.close();
				application.removeListener(listener);
				it.remove();
				break;
			}
		}
	}

	protected ATVParentNode getParentNodeForAppObject(ApplicationObject obj, ATVApplicationNode node)
	{
		if(obj instanceof DatabaseConnection) {
			return node.databaseNode;
		}
		else if(obj instanceof Role) {
			// DONOTHING>>>>>> [[-;
		}
		else if(obj instanceof Keyword) {

		}
		else if(obj instanceof DesignObject) {
			DesignObject dobj = (DesignObject) obj;
			switch(dobj.getDesignType()) {
			case DesignObject.TYPE_ACTION:
				return node.actionsNode;
			case DesignObject.TYPE_DOCUMENTATION:
				return node.documentationNode;
			case DesignObject.TYPE_LIBRARY:
				return node.sharedNode;
			case DesignObject.TYPE_JAR_LIBRARY:
				return node.sharedNode;
			case DesignObject.TYPE_PAGE:
				return node.pagesNode;
			case DesignObject.TYPE_RESOURCE: {
				MimeType mt = new MimeType(dobj.getContentType());
				if("image".equals(mt.getMainType()))
					return node.resourcesNode.imagesNode;
				else if(MimeTypesResolver.MIME_XML.equals(mt))
					return node.resourcesNode.xmlNode;
				else if(MimeTypesResolver.isWebFile(mt))
					return node.resourcesNode.webNode;
				else
					return node.resourcesNode.othersNode;
			}
			case DesignObject.TYPE_SCHEDULEDACTION:
				return node.scheduledNode;
			case DesignObject.TYPE_WIDGET:
				return node.widgetsNode;
			case DesignObject.TYPE_CONFIGURATION:
				return null;
			default:
				VortexPlugin.log("AppTreeViewController - invalid design object type: " + dobj.getDesignType() + " in object " + dobj.toString());
			}
		}
		else if(obj instanceof DatabaseConnection) {
			return node.databaseNode;
		}
		else if(obj instanceof Table) {
			ATVParentNode n = node.databaseNode;
			Table table = (Table) obj;
			Database db = table.getDatabase();
			DatabaseConnection con = db.getDatabaseConnection();
			for(Object o : n.children) {
				if(o instanceof ATVDbConnectionNode) {
					ATVDbConnectionNode dbNode = (ATVDbConnectionNode) o;
					if(dbNode.getDatabaseObject() == con) {
						return dbNode;
					}
				}
			}
		}
		else if(obj instanceof TableColumn) {
			// IN 1.1 THERE IS NO SUPPORT FOR TABLE COLUMNS IN NAVIGATOR1
			return null;
		}
		else
			VortexPlugin.log("Invalid application object: " + obj);

		return null;
	}

	public void treeAddApplicationObject(ApplicationObject object, ATVApplicationNode node)
	{
		ATVParentNode parent = getParentNodeForAppObject(object, node);
		if(parent != null) {
			Object child = createTreeObjectForAppObject(object, parent);
			viewer.add(parent, child);
		}
	}

	private Object createTreeObjectForAppObject(ApplicationObject object, TreeParent parent)
	{
		if(object instanceof DatabaseConnection) {
			return new ATVDbConnectionNode(this, (DatabaseConnection)object, parent);
		}
		else if(object instanceof DesignObject)
			return getWrapper((DesignObject) object);
		return object;
	}

	public void treeUpdateApplicationObject(ApplicationObject object, ATVApplicationNode node)
	{
		// TODO: handle some changes, etc...
	}

	public Object[] getElements(Object inputElement)
	{
		if(root == null)
			initialize();
		return getChildren(root);
	}

	/**
	 * Returns all child elements.
	 *
	 * @param parentElement
	 * @return the array of child elements or null if the request was targeted
	 * for bad object (without children)
	 */
	public Object[] getChildren(Object parentElement)
	{
		if(parentElement instanceof ATVBaseNode) {
			ATVBaseNode node = (ATVBaseNode) parentElement;
			return node.getChildren();
		}
		else if(parentElement instanceof ATVParentNode) {
			// GET THE TYPE OF THE PARENT ELEMENT
			ATVParentNode parent = (ATVParentNode) parentElement;
			int type = parent.getNodeType();
			Application application = parent.getAppNode().getApplication();
			int doType = 0;
			int[] types = {
					NODE_ACTIONS,  NODE_PAGES,
					NODE_RESOURCES_DOCUMENTS, NODE_SCHEDULED_ACTIONS,
					NODE_WIDGETS,
			};
			int[] doTypes = {
					DesignObject.TYPE_ACTION, DesignObject.TYPE_PAGE,
					DesignObject.TYPE_DOCUMENTATION, DesignObject.TYPE_SCHEDULEDACTION,
					DesignObject.TYPE_WIDGET,

			};
			for(int i = 0; i < types.length; ++i) {
				if(types[i] == parent.getNodeType()) {
					doType = doTypes[i];
					break;
				}
			}
			if(doType != 0)
				return getWrappers(application.listDesignObjectsByType(doType));

			// WELL, SO NOW CHECK SHARED CODE
			if(type == NODE_SHARED_CODE) {
				DesignObject[] ret = application.listDesignObjectsByType(DesignObject.TYPE_JAR_LIBRARY);
				DesignObject[] ret1 = application.listDesignObjectsByType(DesignObject.TYPE_LIBRARY);
				ret = (DesignObject[]) ArrayUtils.mergeArrays(ret, ret1);
				return getWrappers(ret);
			}

			// AND CHECK IF THE PARENT IS NOT ACCIDENTLY THE RESOURCES MAIN NODE
			ATVApplicationNode appNode = parent.getAppNode();
			if(appNode.resourcesNode == parent) {
				return parent.getChildren();
			}
			// NOW DO THE SAME WITH DATABASES
			else if(appNode.databaseNode == parent) {
				DatabaseConnection[] connections = application.listDatabases();
				ATVDbConnectionNode[] nodes = new ATVDbConnectionNode[connections.length];
				for(int i = 0; i < connections.length; ++i) {
					nodes[i] = new ATVDbConnectionNode(this, connections[i], parent);
				}
				return nodes;
			}

			if(parent.nodeTypeId == NODE_DATABASE_OBJECT) {
				ATVDbConnectionNode node = (ATVDbConnectionNode) parent;
				return getDatabaseTableSubNodes(node);
			}

			// WELL, AND THERE IS TIME FOR RESOURCES! [-;
			String[] cTypes = new String[0];
			String[] IMAGES = new String[] { "image/jpeg", "image/png", "image/gif" };
			String[] WEB = new String[] { "text/html", "text/css", "text/javascript" };
			String[] XML = new String[] { "text/xml", };
			switch(type) {
			case NODE_RESOURCES_IMAGES:
				cTypes = IMAGES;
				break;
			case NODE_RESOURCES_WEB:
				cTypes = WEB;
				break;
			case NODE_RESOURCES_XML:
				cTypes = XML;
				break;
			case NODE_RESOURCES_OTHER:
				return getWrappers(filterNonMatching(application, IMAGES, WEB, XML));
			}

			return getWrappers(filterTypes(application, cTypes));
		}
		else if(parentElement instanceof TreeParent) {
			TreeParent parent = (TreeParent)parentElement;
			return parent.children.toArray();
		}
		else if(parentElement instanceof ApplicationObject) {
			return new Object[0];
		}
		else {
			return javaProvider.getChildren(parentElement);
		}
	}

	private Object[] getDatabaseTableSubNodes(final ATVDbConnectionNode node)
	{
		synchronized(node) {
			if(node.isRefreshed()) {
				return node.getChildObjects();
			}
			else if(node.isRefreshing()) {
				return node.getChildObjects();
			}
			else { // NODE NEEDS REFRESH
				node.setRefreshing(true);
			}
		}
		node.hookListener();

		Job j = new Job("DatabaseRefresh [" + node.getDatabaseObject().getName() + "]") {
			protected IStatus run(IProgressMonitor monitor)
			{
				Database db = node.getDatabase();
				if(db.isOpen() == false) {
					try {
						db.refresh();
					}
					catch(PuakmaCoreException e) {
						return new Status(IStatus.ERROR, VortexPlugin.PLUGIN_ID, 0, e.getLocalizedMessage(), e);
					}
				}
				synchronized(node) {
					node.setRefreshing(false);
					node.setRefreshed(true);
				}
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						viewer.refresh(node);
					}
				});
				return Status.OK_STATUS;
			}
		};

		j.schedule();
		return node.getChildObjects();
	}

	/**
	 * Gets top node for the application.
	 * @param application for which we want top tree node
	 * @return tree node for application or null if there is not any
	 */
	private ATVApplicationNode getApplicationNode(Application application)
	{
		Iterator<ApplicationListener> it = appListeners.iterator();
		while(it.hasNext()) {
			ThisApplicationListener listener = (ThisApplicationListener) it.next();
			ATVApplicationNode node = listener.node;
			if(application == node.application)
				return node;
		}

		return null;
	}

	/**
	 * Filters all the resources for the "other" resource node.
	 * 
	 * @param application is the application in which we want to filter resources
	 * @param images are images we want to avoid
	 * @param web are web resource we want to avoid
	 * @param xml are xml resources we want to avoid
	 * @return list of other parameter non matching resources
	 */
	private DesignObject[] filterNonMatching(Application application, String[] images, String[] web, String[] xml)
	{
		final String[] types = (String[]) ArrayUtils.mergeArrays(images, web, xml);
		return application.listDesignObjects(new FilterMatcher<DesignObject>() {
			public boolean matches(DesignObject dob) {
				String contentType = dob.getContentType();
				for(int i = 0; i < types.length; ++i)
					if(dob.getDesignType() != DesignObject.TYPE_RESOURCE || types[i].equals(contentType))
						return false;
				return true;
			}
		});
	}

	private DesignObject[] filterTypes(Application application, final String[] types)
	{
		DesignObject[] objs = application.listDesignObjects(new FilterMatcher<DesignObject>() {
			public boolean matches(DesignObject dob) {
				String contentType = dob.getContentType();
				for(int i = 0; i < types.length; ++i)
					if(dob.getDesignType() == DesignObject.TYPE_RESOURCE && types[i].equals(contentType))
						return true;
				return false;
			}
		});
		return objs;
	}

	/**
	 * Returns the parent node of element.
	 *
	 * @param element is the element which is requested for parent
	 * @return parent object or null if element doesn't have any parent element
	 */
	public Object getParent(Object element)
	{
		IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
		boolean useFlatPackages = store.getBoolean(PreferenceConstants.PREF_NAVIGATOR_USE_FLAT_PACKAGES);

		if(element instanceof ATVBaseNode) {
			ATVBaseNode node = (ATVBaseNode) element;
			return node.getParent();
		}
		else if(element instanceof ApplicationObject) {
			ApplicationObject ao = (ApplicationObject) element;
			ATVApplicationNode appNode = getApplicationNode(ao.getApplication());
			return getParentNodeForAppObject(ao, appNode);
		}
		else if(element instanceof TreeObject) {
			TreeObject obj = (TreeObject) element;
			return obj.getParent();
		}
		else if(element instanceof IPackageFragment && useFlatPackages == false) {
			// IF THE VIEW IS FLAT, PACKAGE IS UNDER DIFFERENT HIERARCHY THEN IN FLAT VIEW
			IPackageFragment fragment = (IPackageFragment) element;
			String packName = fragment.getElementName();
			int index = packName.lastIndexOf('.');
			if(index != -1) {
				packName = packName.substring(0, index);
			}
			//System.out.println("PARENT FOR " + fragment.getElementName() + " IS " + packName);
			IPackageFragmentRoot root = (IPackageFragmentRoot) fragment.getParent();
			return root.getPackageFragment(packName);
		}
		return javaProvider.getParent(element);
	}

	/**
	 * Returns true if the element can be expanded.
	 * 
	 * @param element is the tree object which is asked to check expand availability
	 * @return true if the element can be expanded
	 */
	public boolean hasChildren(Object element)
	{
		if(element instanceof ATVParentNode) {
			ATVParentNode node = (ATVParentNode) element;
			if(node.nodeTypeId == NODE_RESOURCES)
				return true;
			else if(node.nodeTypeId == NODE_DATABASE_OBJECT) {
				return true;
			}
			else 
				return true;
		}
		else if(element instanceof TreeParent) {
			TreeParent parent = (TreeParent) element;
			return (parent.children.size() > 0) ? true : false;
		}
		else if(element instanceof Table) {
			Table table = (Table) element;
			return table.listColumns().length > 0 ? true : false;
		}
		return javaProvider.hasChildren(element);
	}

	/**
	 * Gets the object wrapper for the design object.
	 * 
	 * @param object is the design object for which we want wrapper
	 * @return DesignObjectTreeWrap instance for the DesignObject
	 */
	static DesignObjectTreeWrap getWrapper(DesignObject object)
	{
		DesignObjectTreeWrap wrap = (DesignObjectTreeWrap) object.getData(KEY_TREE_ADAPTER);
		if(wrap == null) {
			wrap = new DesignObjectTreeWrap(object);
			object.setData(KEY_TREE_ADAPTER, wrap);
		}
		return wrap;
	}

	/**
	 * Gets wrapped array for the design objects.
	 * 
	 * @param objects are the design objects to wrap
	 * @return array with DesignObjectTreeWrap instances
	 */
	static DesignObjectTreeWrap[] getWrappers(DesignObject[] objects)
	{
		DesignObjectTreeWrap[] wraps = new DesignObjectTreeWrap[objects.length];
		for(int i = 0; i < wraps.length; ++i) {
			wraps[i] = getWrapper(objects[i]);
		}
		return wraps;
	}

	/**
	 * Refreshes all the nodes which are under top level hierarchy, and contains some java
	 * elements - like actions, shared code, class browser...
	 */
	public void refreshTornadoNodes()
	{
		ATVApplicationNode[] nodes = listApplicationNodes();
		for(int i = 0; i < nodes.length; ++i) {
			Object[] javaNodes = nodes[i].listAllJavaNodes();
			for(int j = 0; j < javaNodes.length; ++j) {
				viewer.refresh(javaNodes[j], true);
			}
		}
	}

	public TreeViewer getViewer()
	{
		return viewer;
	}

	public void propertyChange(final PropertyChangeEvent evt)
	{
		String prop = evt.getPropertyName();
		if(ServerManager.PROP_APP_CONNECTED.equals(prop)) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run()
				{
					connectToApp((Application) evt.getNewValue());
				}
			});
		}
	}
}
