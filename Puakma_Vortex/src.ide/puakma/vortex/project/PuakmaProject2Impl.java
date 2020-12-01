/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jun 30, 2005
 *
 * Copyright (c) 2004, 2005, 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.project;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

import puakma.coreide.ObjectsFactory;
import puakma.coreide.PuakmaCoreException;
import puakma.coreide.RefreshEvent;
import puakma.coreide.RefreshEventInfoImpl;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.ApplicationEvent;
import puakma.coreide.objects2.ApplicationListener;
import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.JavaObject;
import puakma.coreide.objects2.ObjectChangeEvent;
import puakma.coreide.objects2.ServerDataStatus;
import puakma.utils.io.FileUtils;
import puakma.utils.lang.ArrayUtils;
import puakma.utils.lang.CollectionsUtil;
import puakma.vortex.IdeException;
import puakma.vortex.IdeInvocationException;
import puakma.vortex.VortexPlugin;
import puakma.vortex.WorkbenchUtils;
import puakma.vortex.editors.application.ApplicationEditorInput;
import puakma.vortex.project.queue.DUQueue;
import puakma.vortex.project.queue.DUQueueImpl;
import puakma.vortex.project.queue.DownloadItem;
import puakma.vortex.project.resource.ResourceDeltaFilesListVisitorTool;

import com.ibm.icu.util.StringTokenizer;

/**
 * This represents Eclipse project, BUT this is not really nature. Nature is something thought
 * by some motherfuckers, so we have to manage projects outside this shit...
 * 
 * TODO: add some handling of opening closed external projects
 * 
 * @author Martin Novak
 */
public class PuakmaProject2Impl implements PuakmaProject2
{
  private static final String VORTEX_JRE_CONFIGURATION_DO_NAME = ".vortexJreConfiguration";
  
  /**
   * Design object containing properties about external projects...
   */
  private static final String VORTEX_EXTERNAL_PROJECT_CONFIGURATION = ".vortexExternalProjects";

  /**
   * This is key for design object param which specifies how the design object has been
   * removed.
   */
  static final String KEY_ADD_REFRESH = "#__addSource__";
  
  static final String KEY_CHANGE_REFRESH = "#__changeSource__";
  
  public static final String KEY_REMOVE_REFRESH = "#__removeSource__";
  
  /**
   * This is set when design object has been removed by refreshing
   */
  public static final String OP_REFRESH = "refresh";
  
  /**
   * This is set when design object has been removed by some external change like user
   * removed class, etc...
   */
  static final String OP_EXTERNAL_FILE_CHANGE = "external";
  
  /**
   * This is set to design object properties when design object is removed normally using
   * funciton <code>object.remove()</code>. This is set by PuakmaProject2Impl
   * application listener
   */
  static final String OP_NORMALY = "normaly";
  
  /**
   * Unknown modification source. So basically it's null value [-;
   */
  static final String OP_UNKNONW = null;

  private static final String KEY_USE_SERVER_JRE = "useServerJre";

  private static final String KEY_JRE_VERSION = "jreVersion";

  static final boolean USE_NEW_UPLOADER_CODE = true;

  private IProject project;
  private IJavaProject javaProject;
  private Application application;
  //private String name;
  /**
   * If true then java initialization has been already started, and thus we can
   * open java objects...
   */
  private boolean javaStarted = false;

  /**
   * This is the lock for the uploading facilities
   */
  //Object downloadLock = new Object();
  
  /**
   * Application object change listener. Valid (non null) only after calling beginRefresh()
   */
  private ApplicationListener appListener;
  
  /**
   * This is set to true if we are not in the refresh mode, so we can upload changed files
   * from the filesystem.
   */
  boolean canUpload = false;

  private Object javaLock = new Object();

  private boolean javaStarting;

  /**
   * This is a download queue which downalods all design objects to disk. It uses
   * eclipse's {@link Job} subsystem to manage all thread pools, and synchronization.
   */
  private DUQueue downloadQueue = new DUQueueImpl(this);

  private JavaProjectConfiguration configuration;

  private boolean javaProjectSetupDone = false;

  private PuakmaProjectUploader filesVisitor;

  /**
   * This includes the list of external project references.
   */
  private List<ExternalProjectReference> externalProjectReferences = new ArrayList<ExternalProjectReference>();
  
  /**
   * Map including all external project references.
   */
  private Map<String, ExternalProjectReference> externalProjectReferencesMap = new HashMap<String, ExternalProjectReference>();
  
  /**
   * Map including all external project references. All those references are to
   * the projects which are not closed.
   */
  private Map<String, IProject> externalProjectsMap;
  
  /**
   * List of all valid external projects.
   */
  private List<IProject> externalProjects;

  private boolean synchronizeExternalProjects;

  private boolean openExternalProjectsOnSynchronize;

  private boolean open;

  private PuakmaProjectExternalUploader extFilesVisitor;

  /**
   * This initializes this object, creates a new eclipse project.
   * @param name is the name of the project
   * @throws IdeException if something fucks up... [-;
   */
  void createNew(String name, Application application) throws IdeException
  {
    try {
      // IF THERE IS EXISTING PROJECT, CLOSE IT, AND ERASE IT COMPLETELY
      checkExisting(name);
      
      // NOW CREATE NEW PROJECT
      project = createNewProject(name);
      
      open = true;
      setApplication(application);
    }
    catch(CoreException e) {
      VortexPlugin.log(e);
      throw new IdeException(e);
    }
  }

  /**
   * This function is called when we disconnect from the application. This should close,
   * and clean the project.
   */
  protected void close()
  {
    if(appListener != null) {
      application.removeListener(appListener);
      appListener = null;
    }
    
    if(USE_NEW_UPLOADER_CODE && filesVisitor != null) {
      ResourceDeltaFilesListVisitorTool.removeVisitor(filesVisitor);
      filesVisitor = null;
    }
    if(USE_NEW_UPLOADER_CODE && extFilesVisitor != null) {
      ResourceDeltaFilesListVisitorTool.removeVisitor(extFilesVisitor);
      extFilesVisitor = null;
    }
    
    // CLOSE ALL EDITORS
    Display.getDefault().syncExec(new Runnable() {
      public void run()
      {
        // PROTECT AGAINST NON-RUNNING WORKBENCH
        if(PlatformUI.isWorkbenchRunning() == false)
          return;
        
        IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
        for(int j = 0; j < windows.length; ++j) {
          IWorkbenchPage[] pages = windows[j].getPages();
          for(int i = 0; i < pages.length; i++) {
            IEditorReference[] refs = pages[i].getEditorReferences();
            for(int k = 0; k < refs.length; ++k) {
              IEditorInput input;
              try {
                input = refs[k].getEditorInput();
                if(input instanceof IFileEditorInput) {
                  IFileEditorInput finput = (IFileEditorInput) input;
                  IFile file = finput.getFile();
                  PuakmaProject2 project = ProjectUtils.getProject(file);
                  if(project == PuakmaProject2Impl.this) {
                    pages[i].closeEditor(refs[k].getEditor(false), false);
                  }
                }
                else if(input instanceof ApplicationEditorInput) {
                  ApplicationEditorInput appInput = (ApplicationEditorInput) input;
                  Application application = appInput.getApplication();
                  PuakmaProject2 project = ProjectManager.getProject(application);
                  if(project == PuakmaProject2Impl.this) {
                    pages[i].closeEditor(refs[k].getEditor(false), false);
                  }
                }
              }
              catch(PartInitException e) {  }
            }
          }
        }
      }
    });
    
    try {
      project.close(null);
    }
    catch(CoreException e) {
      VortexPlugin.log(e);
    }
    
    open = false;
  }

  /**
   * This function creates a new project
   *
   * @return IProject object representing a new eclipse project
   * @throws CoreException
   */
  private IProject createNewProject(String name) throws CoreException
  {
    IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
    prj.create(null);
    prj.open(null);
    prj.setDefaultCharset("UTF-8", null);
    return prj;
  }

  /**
   * This function adds java capabilities to the current project
   * 
   * @return java eclipse project
   * @throws CoreException
   */
  private IJavaProject createJavaProject(Application application) throws CoreException
  {
    // WELL, AT FIRST SETUP JAVA NATURE
    IProjectDescription desc = project.getDescription();
    desc.setNatureIds(new String[] { JavaCore.NATURE_ID });
    project.setDescription(desc, null);
    
    // WELL, NOW CREATE JAVA PROJECT
    IJavaProject javaProject = JavaCore.create(project);
    
    // SETUP BIN DIR
    IFolder binFldr = createDirectory(DIR_BIN);
    javaProject.setOutputLocation(binFldr.getFullPath(), null);
    
    // SETUP RESOURCES DIR
    createDirectory(DIR_RESOURCE);
    
    // SETUP LIB DIR
    createDirectory(DIR_LIB);
    createDirectory(DIR_LIB_SRC);
    
    // SETUP PAGES DIR
    createDirectory(DIR_PAGES);
    
    // SETUP JDK LIBRARIES
    setupJre(javaProject, application);
    
    // SETUP PUAKMA.JAR FILE
    try {
      addPuakmaJarToProjectClasspath(javaProject);
    }
    catch(IOException ioe) {
      throw new CoreException(new Status(IStatus.ERROR, "puakma.vortex", 0, "Cannot setup puakma.jar library in the project", ioe));
    }
    
    try {
      setupPluginInternalLibraries(javaProject);
    }
    catch(Exception e) {
      VortexPlugin.log(e);
    }

    // SETUP SOURCE DIR
    IFolder srcFolder = createDirectory(DIR_SRC);
    clearDefaultSourceEntries(javaProject);
    setFolderAsSourceEntry(javaProject, srcFolder);

    return javaProject;
  }
  
  /**
   * Gets all external project dependencies from the application, and sets up
   * project for it, listeners for changes in the external projects.
   */
//  private void hookExternalProjects(IJavaProject javaProject, Application app)
//  {
//    IWorkspace wspc = ResourcesPlugin.getWorkspace();
//    IWorkspaceRoot root = wspc.getRoot();
//    
//    List prjsList = listExternalProjects(app);
//    Iterator it = prjsList.iterator();
//    List validProjectsList = new ArrayList();
//    while(it.hasNext()) {
//      String projectName = (String) it.next();
//      IProject externalProject = root.getProject(projectName);
//      if(externalProject != null && externalProject.equals(javaProject.getProject()) == false) {
//        validProjectsList.add(externalProject);
//      }
//      else {
//        VortexPlugin.log("External project " + projectName + " which is referenced by application "
//                 + app.getFQName() + " cannot be found, so there might be unresolved references "
//                 + "in the Tornado application");
//      }
//    }
//  }

  /**
   * Lists all external projects in the application
   */
//  static List listExternalProjects(Application app)
//  {
//    List l = new ArrayList();
//    int i = 0;
//    while(true) {
//      String value = app.getParameterValue(PARAM_EXTERNAL_PROJECT_PREFIX + i);
//      if(value == null || value.length() == 0)
//        break;
//      l.add(value);
//    }
//    return l;
//  }

  private void setupJre(IJavaProject javaProject, Application application) throws CoreException
  {
    JavaProjectConfiguration conf = loadJavaProjectConfiguration(javaProject, application);
    setupJava(conf, application, javaProject);
  }

  /**
   * Gets the {@link IVMInstall} object from the java project configuration. If
   * java project configuration contains invalid values, it tries to recover
   * from the error.
   * @throws JavaModelException 
   */
  private IVMInstall getVMInstall(JavaProjectConfiguration conf, Application application) throws JavaModelException
  {
    IVMInstall install = null;
    
    if(conf.useLatestServerCompatibleJdk()) {
      install = ProjectUtils.getServerVMInstall(application);
      if(install == null)
        install = JavaRuntime.getDefaultVMInstall();
    }
    else {
      install = ProjectUtils.getVMInstallByEnvName(conf.getJavaVersion());
    }
    
    if(install == null) {
      String msg = "Cannot find JRE suitable for the project. JRE has to be compatible with ";
      if(conf.useLatestServerCompatibleJdk())
        msg += "java version: " + application.getServer().getEnvironmentProperty("java.version");
      else
        msg += "execution environment:" + conf.getJavaVersion() + " which can be setup in Preferences->Java->Installed JREs->Execution Environments";
      IStatus status = new Status(IStatus.ERROR, VortexPlugin.PLUGIN_ID, 0, msg, null);
      throw new JavaModelException(new CoreException(status));
    }
    
    return install;
  }

  /**
   * Loads java project configuration from the Tornado server.
   */
  private JavaProjectConfiguration loadJavaProjectConfiguration(IJavaProject javaProject,
                                                                Application application)
  {
    JavaProjectConfigurationImpl conf = new JavaProjectConfigurationImpl();
    DesignObject dob = application.getDesignObject(VORTEX_JRE_CONFIGURATION_DO_NAME);
    if(dob == null)
      return setupDefaultJavaProjectConfiguration(conf, application);
    
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      dob.download(bos, false);
      Properties p = new Properties();
      ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
      p.load(bis);
      boolean useJre = Boolean.valueOf(p.getProperty(KEY_USE_SERVER_JRE)).booleanValue();
      String jreVersion = p.getProperty(KEY_JRE_VERSION);
      conf.setUseLatestServerCompatibleJdk(useJre);
      conf.setJavaVersion(jreVersion);
      
      // LOAD ALL PROPERTIES TO JAVA PROJECT, IF THE PROPERTY IS NOT FOR JAVA
      // PROJECT, IT IS SILENTLY IGNORED BY JAVA PROJECT
      Enumeration en = p.keys();
      while(en.hasMoreElements()) {
        String key = (String) en.nextElement();
        String value = p.getProperty(key);
        javaProject.setOption(key, value);
      }
    }
    catch(Exception e) {
      return setupDefaultJavaProjectConfiguration(conf, application);
    }
    
    return conf;
  }

  /**
   * Setups the {@link JavaProjectConfiguration} object with the default values.
   * Note that this does NOT load values from server, but loadds the default
   * values. The Java version is at first determined from the server environment
   * variable, and if this fails, it loads the Eclipse's default JRE.
   */
  private JavaProjectConfiguration setupDefaultJavaProjectConfiguration(JavaProjectConfigurationImpl conf, Application application)
  {
    conf.setUseLatestServerCompatibleJdk(true);

    // GET JAVA VERSION FROM THE SERVER ENVIRONMENT
//    String envName = ProjectUtils.getServerEnvironmentName(application);
//    
//    if(envName != null) {
//      IExecutionEnvironment env = JavaRuntime.getExecutionEnvironmentsManager().getEnvironment(envName);
//      if(env != null) {
//        IVMInstall install = env.getDefaultVM();
//        if(install == null || env.isStrictlyCompatible(install) == false) {
//          IVMInstall[] installs = env.getCompatibleVMs();
//          String tmpName = envName;
//          envName = null;
//          // WELL WE SHOULD SEE IF THERE IS SOME COMPATIBLE JRE, IF YES, IT'S OK
//          for(int i = 0; i < installs.length; ++i) {
//            if(env.isStrictlyCompatible(installs[i])) {
//              envName = tmpName;
//              break;
//            }
//          }
//        }
//        else
//          envName = null;
//      }
//      
//      if(envName == null) { // NOTE THAT THERE IS NO ELSE ON PURPOSE!!!
//        envName = "J2SE-1.4";
//      }
//    }
    return conf;
  }

  /**
   * This function sets up lib directory inside vortex plugin as a source of external libraries
   * normally available in puakma/lib directory.
   * 
   * @param javaProject 
   * @throws IOException 
   * @throws JavaModelException 
   */
  private void setupPluginInternalLibraries(IJavaProject javaProject) throws IOException, JavaModelException
  {
//  GET PUAKMA.JAR PLUGIN BUNDLE
    Bundle bundle = VortexPlugin.getDefault().getBundle();
    // AND THE WHOLE PATH TO puakma.jar
    URL url = bundle.getEntry("/lib");
    // THERE MIGHT NOT BE /lib DIRECTORY IN THE PLUGIN
    if(url == null)
      return;
    
    URL localUrl = FileLocator.toFileURL(url);    
    String p = localUrl.getPath();
    File dir = new File(p);
    // IN THE CASE THAT WE DON'T HAVE lib DIRECTORY IGNORE THIS
    if(dir.isDirectory() == false)
      return;
    
    List<IPath> libraries = new ArrayList<IPath>();
    File[] files = dir.listFiles();
    for(int i = 0; i < files.length; ++i) {
      // IGNORE NON FILES AND UNREADABLE FILES
      if(files[i].isFile() == false || files[i].canRead() == false)
        continue;
      
      IPath path = new Path(files[i].toString()).makeAbsolute();
      // ALLOW ONLY JAR FILES TO BE ON THE PATH
      if("jar".equalsIgnoreCase(path.getFileExtension()) == false)
        continue;
      libraries.add(path);
    }
    
    ProjectUtils.addLibraryEntries(javaProject, libraries.toArray(new IPath[libraries.size()]), null, null);
  }

  /**                                                                                                                                    
   * Adds puakma.jar library to the project classpath. If puakma.jar is already
   * present in the classpath, does nothing.
   *
   * @param javaProject 
   * @throws CoreException
   * @throws IOException 
   */
  private void addPuakmaJarToProjectClasspath(IJavaProject javaProject) throws CoreException, IOException
  {
    // GET PUAKMA.JAR PLUGIN BUNDLE
    Bundle bundle = VortexPlugin.getDefault().getBundle();
    // AND THE WHOLE PATH TO puakma.jar
//    String url = bundle.getLocation();
    URL url = bundle.getEntry("lib-int/puakma.jar");

    URL fileUrl = FileLocator.toFileURL(url);
    String p = fileUrl.getPath();
    IPath path = new Path(p).makeAbsolute();
    
    //if(VortexPlugin.DEBUG_MODE == false) {
    // TODO: change this when eclipse bug is solved
    if(true) {
      IPath javaDocPath = path.removeLastSegments(1).append("docs/javadoc/");
      ProjectUtils.addLibraryEntries(javaProject, new IPath[] { path }, null, new IPath[] { javaDocPath });
    }
    else {
      IPath javaDocFile = path.removeLastSegments(1).append("javadoc.zip");
      IPath pathInsideJavaDoc = new Path("javadoc");
      ProjectUtils.addLibraryEntries(javaProject, new IPath[] { path }, null,
                                     new IPath[] { javaDocFile }, new IPath[] { pathInsideJavaDoc } );
    }
//    IPath path = new Path(p).makeAbsolute();
//
//    IClasspathEntry[] entries = javaProject.getRawClasspath();         
//    //  check if we haven't already puakma.jar present in the classpath
//    for(int i = 0; i < entries.length; ++i) {
//      IPath ep = entries[i].getPath();
//      if(ep.segmentCount() > 1) {
//        String segment = ep.segment(ep.segmentCount() - 1);
//        if(segment.equalsIgnoreCase("puakma.jar"))
//          return;
//      }
//    }
//    List cp = new ArrayList(entries.length + 1);
//    cp.addAll(Arrays.asList(entries));
//
//    IClasspathEntry cpe = JavaCore.newLibraryEntry(path, null, null);
//    cp.add(cpe);
//
//    javaProject.setRawClasspath((IClasspathEntry[]) cp.toArray(new IClasspathEntry[cp.size()]), null);
  }

  public static void addToClasspath(IJavaProject jproject, IClasspathEntry cpe) throws JavaModelException {
    IClasspathEntry[] oldEntries= jproject.getRawClasspath();
    for (int i= 0; i < oldEntries.length; i++) {
      if (oldEntries[i].equals(cpe)) {
        return;
      }                                                                                                                                  
    }
    int nEntries= oldEntries.length;
    IClasspathEntry[] newEntries= new IClasspathEntry[nEntries + 1];
    System.arraycopy(oldEntries, 0, newEntries, 0, nEntries);
    newEntries[nEntries] = cpe;
    jproject.setRawClasspath(newEntries, null);
  }

  /**
   * Creates a new directory under current project. But only if it's necessary - if
   * the current project doesn't exist.
   *
   * @param dir is the path relative to the project
   * @return IFolder object representing wanted folder
   * @throws CoreException
   */
  private IFolder createDirectory(String dir) throws CoreException
  {
    IFolder folder = project.getFolder(dir);
    if(folder.exists() == false) {
      try {
        folder.create(IResource.NONE, true, null);
      }
      catch(CoreException ex) {
        if(ex.getStatus().getCode() == IResourceStatus.PATH_OCCUPIED)                                                                      
          folder.refreshLocal(IResource.DEPTH_INFINITE, null);                                                                       
        else                                                                                                                               
          throw ex;
      }
    }
    return folder;
  }
  
  /**
   * Clears all source entries from the project.
   *
   * @param javaProject is the java project which we want to clear
   * @throws CoreException
   */
  public static void clearDefaultSourceEntries(IJavaProject javaProject) throws CoreException
  {
    IClasspathEntry[] entries = javaProject.getRawClasspath();
    List<IClasspathEntry> cp = new ArrayList<IClasspathEntry>(entries.length + 1);
    for(int i = 0; i < entries.length; i++) {
      if(entries[i].getEntryKind() != IClasspathEntry.CPE_SOURCE) {
        cp.add(entries[i]);
      }
    }
    javaProject.setRawClasspath(cp.toArray(new IClasspathEntry[cp.size()]), null);
  }
  
  /**
   * Sets folder as source folder.
   *
   * @param javaProject is the java project where do we set the source folder
   * @param folder is the source folder
   * @throws CoreException
   */
  public static void setFolderAsSourceEntry(IJavaProject javaProject, IFolder folder)
                                            throws CoreException
  {
    IClasspathEntry[] entries = javaProject.getRawClasspath();
    IPath folderPath = folder.getFullPath();
    // search the source path whether src directory is present or not... 
    for(int i = 0; i < entries.length; ++i) {
      IPath p = entries[i].getPath();
      if(p.segmentCount() > 1) {
        if(p.segment(0).equals(javaProject.getProject().getName())) {
          if(folderPath.segmentCount() != p.segmentCount())
            continue;

          for(int j = 0; j < folderPath.segmentCount(); j++) {
            if(folderPath.segment(j).equals(p.segment(j)) == false)
              break;
          }
          // folder is already in the source path, so we don't need to add it
          return;
        }
      }
    }

    IClasspathEntry[] newEntries= new IClasspathEntry[entries.length + 1];
    System.arraycopy(entries,0, newEntries,0, entries.length);
    newEntries[entries.length] = JavaCore.newSourceEntry(folder.getFullPath());
    javaProject.setRawClasspath(newEntries, null);  
  }

  /**
   * This function checks for existing project. If it founds it, then closes it, and erases it.
   * @throws CoreException
   */
  private void checkExisting(String name) throws CoreException
  {
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    IWorkspaceRoot root = workspace.getRoot();
    IProject project = root.getProject(name);
    if(project == null)
      return;
    
    if(project.isOpen())
      project.close(null);
    
    project.delete(true, true, null);
  }

  public void setApplication(Application application)
  {
    this.application = application;
    
    // SETUP APPLICATION LISTENER
    appListener = new ApplicationListener() {
      public void disconnect(Application application) {
        close();
      }
      public void objectChange(ObjectChangeEvent event) {
        acceptObjectChange(event);
      }
      public void update(ApplicationEvent event) { }
    };
    application.addListener(appListener);
  }

  public IProject getProject()
  {
    return project;
  }

  public IJavaProject getJavaProject()
  {
    return javaProject;
  }

  public IPath getBasePath()
  {
    return project.getLocation();
  }
  
  private void acceptObjectChange(ObjectChangeEvent event)
  {
    if(event.getObject() instanceof DesignObject == false)
      return;

    DesignObject obj = (DesignObject) event.getObject();
    // IGNORE ALL NON REFRESH EVENTS
    if(event.getEventType() == ObjectChangeEvent.EV_REMOVE) {
      handleRemoveObject(event);
    }
    else if(event.getEventType() == ObjectChangeEvent.EV_ADD_APP_OBJECT) {
    }
    else if(event.getEventType() == ObjectChangeEvent.EV_CHANGE) {
      // HANDLE RENAME OF DESIGN OBJECT (NOT JAVA OBJECT), AND RENAME FILES
      if(event.isRenamed() && obj instanceof JavaObject == false) {
        String oldName = event.getOldName();
        boolean isSource = false;
        
        try {
          // TODO: external projects won't work here correctly
          IFile file = ProjectUtils.getRenamedIFile(obj, isSource, oldName);
          IProject iPrj = ProjectManager.getIProject(obj.getApplication());
          IPath dest = iPrj.getFullPath().append(ProjectUtils.getFilePath(obj, isSource));
          if(file.exists())
            file.move(dest, true, true, null);
        }
        catch(CoreException e) {
          VortexPlugin.log(e);
        }
      }

      if(event.isRefresh() == false) {
        // TODO: handle this somehow
      }
    }
  }

  /**
   * This function handles removal of object. Note that this handles only
   * @param event
   */
  private void handleRemoveObject(ObjectChangeEvent event)
  {
    if(event.getObject() instanceof DesignObject) {
      DesignObject obj = (DesignObject) event.getObject();
      
      // IF THIS DESIGN OBJECT IS EXTERNAL ONE, AND EXTERNAL PROJECT IS OPEN, IGNORE THIS
      String externalProjectName = obj.getParameterValue(PARAMETER_EXTERNAL_REFERENCE);
      if(externalProjectName != null) {
        ExternalProjectReference ref = externalProjectReferencesMap.get(externalProjectName);
        if(ref != null && ref.closed == false)
          return;
      }

      // CLOSE EDITORS
      WorkbenchUtils.closeEditorsForObject(event.getObject());

      String removeType = (String) obj.getData(KEY_REMOVE_REFRESH);
      
      // IGNORE EXTERNAL CHANGE
      if(OP_EXTERNAL_FILE_CHANGE.equals(removeType))
        return;
      else if(removeType == OP_UNKNONW || OP_NORMALY.equals(removeType)
              || OP_REFRESH.equals(removeType)) {
        // RUN ONLY NORMAL REMOVALS
        obj.setData(KEY_REMOVE_REFRESH, OP_NORMALY);
        
        IFile srcFile = ProjectUtils.getIFile(obj, true);
        IFile binFile = ProjectUtils.getIFile(obj, false);

        try {
          srcFile.delete(true, true, null);
        }
        catch(CoreException e) {
          VortexPlugin.log(e);
        }
        try {
          binFile.delete(true, true, null);
        }
        catch(CoreException e) {
          VortexPlugin.log(e);
        }
      }
    }
  }

  public void refresh(RefreshEventInfoImpl info, IProgressMonitor monitor) throws IdeException
  {
    synchronized(javaLock) {
      List<DownloadItem> operations = collectAddedRemovedFiles(info);
      boolean doBuild = javaStarted;
      
      doDownload(operations, monitor, doBuild);
      
      ProjectManager.broadcastEvent(this, ProjectLifecycleListener.POST_REFRESH);
    }
  }
  
  public void startJava(IProgressMonitor monitor) throws IdeInvocationException, CoreException
  {
    if(monitor == null)
      monitor = new NullProgressMonitor();
    monitor.beginTask("Loading java project", 10);
    
    synchronized(javaLock) {
      try {
        if(javaProjectSetupDone == false) {
          setupJavaProject(new SubProgressMonitor(monitor, 1));
          if(checkExternalProjectDependencies()) {
            askUserForReplacingExternalProjectReferences();
            setupExternalProjectsInTheCurrentJavaProject();
          }
          
          javaProjectSetupDone = true;
        }
        
        if(javaStarted)
          return;
        
        javaStarting = true;

        List<DownloadItem> operations = new ArrayList<DownloadItem>();
        // WE WANNA TO ADD ALL THE OBJECTS
        DesignObject[] objs = application.listDesignObjects();
        List<DesignObject> objects = new LinkedList<DesignObject>();
        CollectionsUtil.addArrayToList(objects, objs);
        
        // AT FIRST WE SHOULD DOWNLOAD LIBRARIES, BECAUSE THEY HAVE TO BE SETUP AS THE
        // FIRST ONES
        setupLibraries(objects);
        
        // FILTER OUT THE EXTERNAL PROJECT FILES
        List<JavaObject> externalObjs = new ArrayList<JavaObject>();
        if(synchronizeExternalProjects)
          moveExternalDesignObjectsToSecondList(objects, externalObjs);
        
        // NOW ADD ALL FILES WE SHOULD DOWNLOAD HERE
        for(DesignObject o : objects) {
          if(o instanceof JavaObject)
            addAppObjectDownload(o, operations);
        }
        
        doDownload(operations, new SubProgressMonitor(monitor, 9), true);
        
        // AND UPLOAD THE EXTERNALY REFERENCED FILES
        if(synchronizeExternalProjects)
          uploadExternalReferences(externalObjs);
        
        javaStarted = true;
      }
      finally {
        javaStarting = false;
        monitor.done();
      }
    }
    
    ProjectManager.broadcastEvent(this, ProjectLifecycleListener.POST_JAVA_START);
  }
  
  /**
   * Goes thru the list of design objects present on the server, and if it finds any jar
   * library, it downloads this libraru, and sets up the library inseide the project.
   * A
   */
  private void setupLibraries(List<DesignObject> objects)
  {
    List<DownloadItem[]> downloads = new ArrayList<DownloadItem[]>();
    List<DownloadItem> downs = new ArrayList<DownloadItem>();
    
    for(DesignObject object : objects) {
      if(object.getDesignType() == DesignObject.TYPE_JAR_LIBRARY && object.getDesignSize(false) > 0) {
        DownloadItem[] items = new DownloadItem[2];
        items[0] = new DownloadItem(object, false, DownloadItem.TYPE_DOWNLOAD);
        if(object.getDesignSize(true) > 0) {
          items[1] = new DownloadItem(object, true, DownloadItem.TYPE_DOWNLOAD);
          downs.add(items[1]);
        }
        downloads.add(items);
        downs.add(items[0]);
      }
    }
    
    if(downloads.size() == 0)
      return;
    
    // DOWNLOAD ALL THE ITEMS
    getDownloadQueue().processBatch(downs, true);
    
    // AND SETUP THEM AS A LIBRARIES
    IPath[] javaDocPaths = null;
    List<IPath> pathsL = new ArrayList<IPath>();
    List<IPath> sourcePathsL = new ArrayList<IPath>();
    for(DownloadItem[] download : downloads) {
      DesignObject dob = download[0].getDesignObject();
      IFile bin = ProjectUtils.getIFile(dob, false);
      pathsL.add(bin.getFullPath());
      if(dob.getDesignSize(true) > 0) {
        IFile src = ProjectUtils.getIFile(dob, true);
        sourcePathsL.add(src.getFullPath());
      }
      else
        sourcePathsL.add(null);
    }
    IPath[] paths = pathsL.toArray(new IPath[pathsL.size()]);
    IPath[] sourcePaths = sourcePathsL.toArray(new IPath[sourcePathsL.size()]);
    try {
      ProjectUtils.addLibraryEntries(javaProject, paths, sourcePaths, javaDocPaths);
    }
    catch(Exception e) {
      VortexPlugin.log(e);
    }
  }

  /**
   * Picks the design objects which are itself only in the application, not
   * externally referenced. So this removes the design objects which are not
   * project only, and adds them to the externalObjs {@link List}.
   */
  private void moveExternalDesignObjectsToSecondList(List<DesignObject> objects, List<JavaObject> externalObjs)
  {
    Iterator<DesignObject> it = objects.iterator();
    while(it.hasNext()) {
      DesignObject obj = it.next();
      if(obj instanceof JavaObject) {
        String value = obj.getParameterValue(PARAMETER_EXTERNAL_REFERENCE);
        if(value != null && value.length() > 0) {
          if(externalProjectsMap.containsKey(value)) {
            externalObjs.add((JavaObject) obj);
            it.remove();
          }
        }
      }
    }
  }

  /**
   * Well, this is the hardcore routine which will do the heavy lifting of
   * uploading the shit to the server. But the upload will run again in the
   * background thread since we can afford this [-;
   */
  private void uploadExternalReferences(final List<JavaObject> externalObjs)
  {
    Job j = new Job("Synchronize external java objects") {
      protected IStatus run(IProgressMonitor monitor)
      {
        if(monitor == null)
          monitor = new NullProgressMonitor();
        try {
          uploadExternalReferences0(monitor, externalObjs);
        }
        finally {
          monitor.done();
        }
        return Status.OK_STATUS;
      }
    };
    j.schedule();
  }
  
  /**
   * Synchronizes all external references with the server.
   */
  private void uploadExternalReferences0(IProgressMonitor monitor, List<JavaObject> externalObjs)
  {
    List<JavaObject> objectsToRemove = new ArrayList<JavaObject>();
    List<IFile[]> filesToUpdate = new ArrayList<IFile[]>();
    List<JavaObject> objectsToUpdate = new ArrayList<JavaObject>();
    
    monitor.beginTask("Synchronizing external java object", 140);
    
    monitor.worked(5);
    
    // NOW RUN THRU ALL OBJECTS, AND DECIDE WHAT TO DO WITH THEM
    for(JavaObject jobj : externalObjs) {
      String prjName = jobj.getParameterValue(PARAMETER_EXTERNAL_REFERENCE);
      IProject project = externalProjectsMap.get(prjName);
      IFile[] files = findExternalDesignObjectFilesInProject(project, jobj);
      if(files != null) {
        objectsToUpdate.add(jobj);
        filesToUpdate.add(files);
      }
      else {
        objectsToRemove.add(jobj);
      }
    }
    
    monitor.worked(5);
    
    List<DownloadItem> dis = new ArrayList<DownloadItem>();
    Iterator<IFile[]> it1 = filesToUpdate.iterator();
    for(JavaObject jo : objectsToUpdate) {
      DownloadItem item;
      ServerDataStatus status = jo.getKnownServerStatus();
      IFile[] files = it1.next();
      // IF THE JAVA OBJECT IS NOT INNER CLASS, UPDATE IT
      String clzName = jo.getClassName();
      if(clzName.indexOf('$') == -1) {
        File file = files[1].getLocation().toFile();
        item = addExternalObjectUploadItem(jo, status, file, true);
        if(item != null)
          dis.add(item);
      }
      File file = files[0].getLocation().toFile();
      item = addExternalObjectUploadItem(jo, status, file, false);
      if(item != null)
        dis.add(item);
    }
    getDownloadQueue().processBatch(dis, false);
    
    monitor.worked(20);
    
    removeDesignObjects(new SubProgressMonitor(monitor, 50), objectsToRemove);
    
    // AND FINALLY RUN THRU THE OTHER PROJECT, AND GET WHAT NEEDS TO BE
    // UPLOADED, AND IT IS NOT
    uploadOtherProjectsWork(externalProjects, new SubProgressMonitor(monitor, 50));
  }

  /**
   * This function uploads all design objects which are new in the other referenced projects.
   */
  private void uploadOtherProjectsWork(List<IProject> projectsList, IProgressMonitor monitor)
  {
    monitor.beginTask("Uploading new java objects from the referenced projects", projectsList.size());
    
    try {
      for(final IProject project : projectsList) {
        IFolder folder = project.getFolder("/src");
        if(folder != null && folder.exists()) {
          final List<DownloadItem> uploads = new ArrayList<DownloadItem>();
          
          try {
            folder.accept(new IResourceProxyVisitor() {
              public boolean visit(IResourceProxy proxy) throws CoreException
              {
                if(proxy.getType() == IResource.FILE) {
                  JavaObject jo = checkIfResourceInTheApplicationAndUpload(proxy);
                  if(jo != null) {
                    DownloadItem item;
                    
                    jo.setData(PARAMETER_EXTERNAL_REFERENCE, project.getName());
                    
                    if(jo.getClassName().indexOf('$') == -1) {
                      item = new DownloadItem(jo, true, DownloadItem.TYPE_UPLOAD);
                      uploads.add(item);
                    }
                    item = new DownloadItem(jo, false, DownloadItem.TYPE_UPLOAD);
                    uploads.add(item);
                  }
                }
                return true;
              }
            }, IResource.NONE);
          }
          catch(Exception ex) {
            VortexPlugin.log(ex);
          }
          
          getDownloadQueue().processBatch(uploads, false);
        }
      }
    }
    finally {
      monitor.done();
    }
  }

  protected JavaObject checkIfResourceInTheApplicationAndUpload(IResourceProxy proxy)
  {
    String name = proxy.getName();
    if(name.indexOf(".class") != -1 && name.indexOf('$') == -1)
      return null;
    
    // WE GET THE PATH CONTAINING PACKAGE AND CLASS NAME
    IPath fullPath = proxy.requestFullPath();
    IPath path = fullPath.removeFirstSegments(2);
    String packageName = path.removeLastSegments(1).toString().replace('/', '.');
    String className = path.removeFileExtension().segment(path.segmentCount()-1);
    JavaObject jo = application.getJavaObject(packageName, className);
    if(jo == null) {
      jo = ObjectsFactory.createJavaObject(packageName, className, DesignObject.TYPE_LIBRARY);
      jo.setParameter(PARAMETER_EXTERNAL_REFERENCE, fullPath.segment(0));
      try {
        application.addObject(jo);
        return jo;
      }
      catch(PuakmaCoreException e) {
        VortexPlugin.log(e);
        return null;
      }
    }
    
    return jo;
  }

  private void removeDesignObjects(IProgressMonitor monitor, List<JavaObject> objectsToRemove)
  {
    Iterator<JavaObject> it;
    monitor.beginTask("Removing unnecessary java objects", objectsToRemove.size());
    try {
      // AND REMOVE FILES WHO HAS NOTHING TO DO HERE
      it = objectsToRemove.iterator();
      while(it.hasNext()) {
        JavaObject jo = it.next();
        try {
          application.removeObject(jo);
        }
        catch(Exception e) {
          VortexPlugin.log(e);
        }
        monitor.worked(1);
      }
    }
    finally {
      monitor.done();
    }
  }

  private DownloadItem addExternalObjectUploadItem(JavaObject jo, ServerDataStatus status,
                                                   File file, boolean isSource)
  {
    DownloadItem item;
    long size = file.length();
    if(status.getSourceLength() != size) {
      item = new DownloadItem(jo, isSource, DownloadItem.TYPE_UPLOAD);
      return item;
    }
    else {
      try {
        long crc = FileUtils.calculateCrc(file);
        if(crc != status.getSourceCrc32()) {
          item = new DownloadItem(jo, isSource, DownloadItem.TYPE_UPLOAD);
          return item;
        }
      }
      catch(Exception ex) {
        VortexPlugin.log(ex);
      }
    }
    
    return null;
  }
  
  private IFile[] findExternalDesignObjectFilesInProject(IProject project, JavaObject jobj)
  {
    boolean isInnerClass = jobj.getClassName().indexOf('$') != -1;
    IFile srcFile = null;
    IFile binFile = ProjectUtils.findFileInProject(project, jobj, false);
    boolean exists = binFile != null && binFile.exists();
    if(isInnerClass == false) {
      srcFile = ProjectUtils.findFileInProject(project, jobj, true);
      exists = srcFile != null && srcFile.exists();
    }
    if(exists)
      return new IFile[] { binFile, srcFile };
    else
      return null;
  }

  /**
   * This function setups references to the external java projects to the
   * current java project.
   */
  private void setupExternalProjectsInTheCurrentJavaProject() throws JavaModelException
  {
    externalProjects = new ArrayList<IProject>();
    externalProjectsMap = new HashMap<String, IProject>();
    
    // RUN THRU ALL EXTERNAL PROJECTS, AND ALSO OPEN SOME IF NECESSARY
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    Iterator<ExternalProjectReference> it = externalProjectReferences.iterator();
    MAINLOOP: while(it.hasNext()) {
      ExternalProjectReference prj = it.next();
      if(prj.closed) {
        if(this.openExternalProjectsOnSynchronize) {
          try {
            prj.project.open(null);
            prj.closed = false;
          }
          catch(CoreException e) {
            VortexPlugin.log(e);
            continue MAINLOOP;
          }
        }
        else
          continue MAINLOOP;
      }
      
      String prjName = prj.project.getName();
      IProject project = root.getProject(prjName);
      IJavaProject jProject = JavaCore.create(project);
      if(jProject.exists() == false) {
        VortexPlugin.warning("Java is not set in the externally referenced project " + prjName);
        continue MAINLOOP;
      }
      
      ProjectUtils.addProjectReference(javaProject, jProject);
      externalProjects.add(project);
      externalProjectsMap.put(prjName, project);
    }
    
    IProject[] projects = new IProject[externalProjects.size()];
    projects = externalProjects.toArray(projects);
    
    if(extFilesVisitor == null) {
      extFilesVisitor = new PuakmaProjectExternalUploader(this, projects);
      ResourceDeltaFilesListVisitorTool.addVisitor(extFilesVisitor);
    }
  }

  /**
   * This function downloads all the stuff specified by list operations.
   *
   * @param operations is the list of files to download/delete. Items are
   *                   of the type RefreshOperationInfo
   * @param monitor is the monitor checking progress of the operation
   * @param doBuild if true then we build the workspace
   * @throws IdeInvocationException is something goes wrong, and cannot be finished
   */
  public void doDownload(List<DownloadItem> operations, IProgressMonitor monitor, boolean doBuild) throws IdeInvocationException
  {
    // SETUP MONITOR
    if(monitor == null)
      monitor = new NullProgressMonitor();
    int totalWork = operations.size() + (doBuild ? operations.size()>>2 : 0);
    monitor.beginTask("Refresh", totalWork);

    try {
      // DOWNLOAD EVERYTHING
      //long time = System.currentTimeMillis();
      //monitor.setTaskName("Downloading files");
      //long time1 = System.currentTimeMillis();
      //System.out.println("MONITOR::BEGINTASK: " + (time1-time) + "ms");
      
      //time = System.currentTimeMillis();
      downloadQueue.processBatch(operations, true);
      //time1 = System.currentTimeMillis();
      //System.out.println("REFRESH LOCAL: " + (time1-time) + "ms");
      
      IJobManager manager = Job.getJobManager(); //Platform.getJobManager();

      monitor.setTaskName("Building workspace");

      project.refreshLocal(IResource.DEPTH_INFINITE, null);
      
      if(doBuild) {
        try {
        getProject().build(IncrementalProjectBuilder.AUTO_BUILD, new SubProgressMonitor(monitor, operations.size()>>2));
        }
        catch(OperationCanceledException ex) {
          // IGNORE THAT...
        }
      }
      
//      Job[] build = manager.find(ResourcesPlugin.FAMILY_AUTO_BUILD); 
//      if(build.length == 1)
//         build[0].join();
      
      // AND AT THE END ADD FILES VISITOR HERE
      if(filesVisitor == null) {
        filesVisitor = new PuakmaProjectUploader(this);
        ResourceDeltaFilesListVisitorTool.addVisitor(filesVisitor);
      }
    }
    catch(Exception e) {
      VortexPlugin.log(e);
      throw new IdeInvocationException(e);
    }
    finally {
      monitor.done();
    }
  }

  private List<DownloadItem> collectAddedRemovedFiles(RefreshEventInfoImpl info)
  {
    final List<DownloadItem> operations = new ArrayList<DownloadItem>();
    
    // AT FIRST REMOVE OBJECTS WHICH ARE NOT ON THE SERVER ANYMORE
    RefreshEvent[] events = info.getRemovedObjects();
    for(int i = 0; i < events.length; ++i) {
      DesignObject dob = events[i].getDob();
      if(javaStarted == false && dob instanceof JavaObject)
        continue;

      dob.setData(KEY_REMOVE_REFRESH, OP_REFRESH);
      removeAppObject(dob, operations);
    }
    
    // NOW WE SHOULD SELECT ALL THE NEW DESIGN OBJECTS, AND DOWNLOAD ALL THE JAVA
    // SOURCES, AND LIBRARIES
    events = info.getNewObjects();
    for(int i = 0; i < events.length; ++i) {
      DesignObject dob = events[i].getDob();
      if(javaStarted == false && dob instanceof JavaObject)
        continue;
      
      addAppObjectDownload(dob, operations);
    }
    
    events = info.getChangedObjects();
    for(int i = 0; i < events.length; ++i) {
      DesignObject dob = events[i].getDob();
      boolean isSource = ProjectUtils.isSourceMoreImportant(dob);
      if(ProjectUtils.shouldDownloadChange(this, events[i], isSource) == ProjectUtils.CHANGE_DOWNLOAD)
        operations.add(new DownloadItem(dob, isSource, DownloadItem.TYPE_DOWNLOAD));
      
      // ALSO AT JAR LIBRARIES CHECK IF THE LIBRARY HAS SOURCE, AND WHETHER IT
      // HAS CHANGED THE SOURCE CODE
      isSource = true;
      if(dob.getDesignType() == DesignObject.TYPE_JAR_LIBRARY
         && ProjectUtils.shouldDownloadChange(this, events[i], isSource) == ProjectUtils.CHANGE_DOWNLOAD) {
        operations.add(new DownloadItem(dob, isSource, DownloadItem.TYPE_DOWNLOAD));
      }
    }
    return operations;
  }

  /**
   * Checks if we should download the design object to the project.
   * 
   * @param object is the {@link DesignObject} to check
   * @param operations is the list to which we should add the design object download
   *          information
   */
  private void addAppObjectDownload(DesignObject object, List<DownloadItem> operations)
  {
    object.setData(KEY_ADD_REFRESH, OP_REFRESH);
    DownloadItem item = null;
    
    if(object instanceof JavaObject) {
      // DISABLE THIS OPERATION IF JAVA HAS NOT BEEN STARTED YET
      synchronized(javaLock) {
        if(javaStarted == false && javaStarting == false)
          return;
      }
      
      boolean isSource = true;
      if(object.getDesignSize(isSource) == 0)
        return;
      
      item = new DownloadItem(object, true, DownloadItem.TYPE_DOWNLOAD);
      operations.add(item);
    }
    else if(object.getDesignType() == DesignObject.TYPE_JAR_LIBRARY) {
      // DISABLE THIS OPERATION IF JAVA HAS NOT BEEN STARTED YET
      synchronized(javaLock) {
        if(javaStarted == false && javaStarting == false)
          return;
      }
      
      // SOURCE
      if(object.getDesignSize(true) > 0) {
        item = new DownloadItem(object, true, DownloadItem.TYPE_DOWNLOAD);
        operations.add(item);
      }
      
      // AND DATA
      if(object.getDesignSize(false) > 0) {
        item = new DownloadItem(object, false, DownloadItem.TYPE_DOWNLOAD);
        operations.add(item);
      }
    }
    
    // OR DO NOT DOWNLOAD ANYTHING
  }

  /**
   * This method should be called after removing design object from application, and
   * it should remove the appropriate files.
   *
   * @param object is the DesignObject to remove
   * @param operations is the List with all the operations we should performin
   *                   workspace, so this function will add
   *                   <code>RefreshOperationInfo</code> about removing some files
   */
  protected void removeAppObject(DesignObject object, List<DownloadItem> operations)
  {
    IFile srcFile;
    boolean isSource;
    if(object instanceof JavaObject) {
      isSource = true;
      srcFile = ProjectUtils.getIFile(object, isSource);
    }
    else if(object.getDesignType() == DesignObject.TYPE_JAR_LIBRARY) {
      // AT LIBRARIES WE HAVE TO REMOVE BOTH SOURCE AND DATA
      isSource = true;
      DownloadItem item = new DownloadItem(object, isSource, DownloadItem.TYPE_REMOVED_FROM_SERVER);
      operations.add(item);
      
      isSource = false;
      item = new DownloadItem(object, isSource, DownloadItem.TYPE_REMOVED_FROM_SERVER);
      operations.add(item);
      return;
    }
    else {
      isSource = false;
      srcFile = ProjectUtils.getIFile(object, isSource);
    }
    
    DownloadItem item = new DownloadItem(object, isSource, DownloadItem.TYPE_REMOVED_FROM_SERVER);
    operations.add(item);
  }

  protected void renameAppObject(DesignObject object, String oldName) throws CoreException
  {
    // TODO: finish that shit
    if(!(object instanceof JavaObject)) {
      // at first make the file name relative to the workspace
      IFolder folder = ProjectUtils.getFileFolder(project, object, false);
      IPath newPath = folder.getProjectRelativePath().append(object.getName() + "." + ProjectUtils.getFileExtension(object, false));
      IPath oldPath = folder.getProjectRelativePath().append(oldName + "." + ProjectUtils.getFileExtension(object, false));
      
      // and finally rename the file
      IFile file = project.getFile(oldPath);
      try {
        file.move(newPath, true, false, null);
      }
      catch(CoreException e) {
        VortexPlugin.log(e);
      }
    }
  }

  /**
   * Gets Puakma application which this eclipse project handles
   * @return <code>Application</code> object
   */
  public Application getApplication()
  {
    return application;
  }

  public void addLibrary(DesignObject object) throws JavaModelException
  {
    IFile binFile = ProjectUtils.getIFile(object, false);
    IPath binPath = binFile.getFullPath();
    IFile srcFile = ProjectUtils.getIFile(object, true);
    IPath srcPath = srcFile.getFullPath();
    
    // CHECK EXISTENCE OF PARAMETERS
    if(binFile.exists() == false)
      return;
    if(srcFile.exists() == false)
      srcPath = null;
    IClasspathEntry entry = JavaCore.newLibraryEntry(binPath, srcPath, null);
    addOrReplaceClasspath(javaProject, entry);
  }

  /**
   * Adds or replaces entry on classpath. Replacement is for specifying source
   * for library.
   *
   * @param javaProject is the IJavaProject into which we add/replace source
   * @param entry is the classpath entry to setup
   * @throws JavaModelException 
   */
  public static void addOrReplaceClasspath(IJavaProject javaProject, IClasspathEntry entry) throws JavaModelException
  {
    IClasspathEntry[] oldEntries= javaProject.getRawClasspath();

    for (int i= 0; i < oldEntries.length; i++) {
      // HAHA IF WE FIND THE SAME BINARY PATH, TRY TO UPDATE SOURCE
      if(oldEntries[i].getPath().equals(entry.getPath())) {
        oldEntries[i] = entry;
        javaProject.setRawClasspath(oldEntries, null);
        return;
      }
    }
    
    int nEntries = oldEntries.length;
    IClasspathEntry[] newEntries= new IClasspathEntry[nEntries + 1];
    System.arraycopy(oldEntries, 0, newEntries, 0, nEntries);
    newEntries[nEntries] = entry;
    javaProject.setRawClasspath(newEntries, null);
  }
  
  /**
   * Setups new JRE classpath for the {@link IJavaProject}. 
   */
  public static void setupJre(IJavaProject javaProject, IClasspathEntry entry) throws JavaModelException
  {
    if(entry.getEntryKind() != IClasspathEntry.CPE_CONTAINER)
      throw new IllegalArgumentException("Classpath entry is not valid JRE container");
    
    IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
    boolean found = false;
    for(int i = 0; i < oldEntries.length; ++i) {
      int kind = oldEntries[i].getEntryKind();
      if(kind == IClasspathEntry.CPE_CONTAINER) {
        oldEntries[i] = entry;
        found = true;
        break;
      }
    }
    if(found == false) {
      oldEntries = (IClasspathEntry[]) ArrayUtils.append(oldEntries, entry);
    }
    javaProject.setRawClasspath(oldEntries, null);
  }

  public boolean javaStarted()
  {
    return javaStarted;
  }

  public void addLibraryToClassPath(DesignObject object, boolean isSource) throws CoreException
  {
    IPath path = ProjectUtils.getFullFilePath(object, isSource);
    IClasspathEntry cpe;
    if(isSource)
      cpe = JavaCore.newContainerEntry(path);
    else
      cpe = JavaCore.newSourceEntry(path);
    addToClasspath(javaProject, cpe);
  }

  /**
   * @return {@link DownloadQueue} object which manages downloading/uploading
   */
  public DUQueue getDownloadQueue()
  {
    return this.downloadQueue;
  }

  public JavaProjectConfiguration getJavaConfiguration()
  {
    return configuration;
  }
  
  public void setupJavaProject(IProgressMonitor monitor) throws CoreException
  {
    synchronized(javaLock) {
      if(javaProjectSetupDone) {
        monitor.done();
        return;
      }

      // CONFIGURE THE PROJECT AS THE JAVA PROJECT
      javaProject = createJavaProject(application);
    }
  }

  /**
   * This function checks if the project does have external eclipse project
   * reference. If there are some external project references, it saves the
   * information to the class structures.
   */
  private boolean checkExternalProjectDependencies()
  {
    DesignObject obj = application.getDesignObject(VORTEX_EXTERNAL_PROJECT_CONFIGURATION);
    if(obj == null)
      return false;
    
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try {
      obj.download(os, false);
      Properties p = new Properties();
      ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
      p.load(is);
      String extProjects = p.getProperty("externalProjects");
      if(extProjects == null)
        return false;
      
      StringTokenizer tk = new StringTokenizer(extProjects, ",");
      
      while(tk.hasMoreTokens()) {
        checkExternalproject(p, tk.nextToken());
      }
      
      if(externalProjectReferences.size() == 0)
        return false;
      
      // OR WE HAVE TO ANALYZE THE EXTERNAL PROJECTS
      return true;
    }
    catch(Exception ex) {
      VortexPlugin.log(ex);
      return false;
    }
  }

  /**
   * Checks availability of the external project. If the project is available,
   * add it to the external structures.
   */
  private void checkExternalproject(Properties p, String name)
  {
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IProject prj = root.getProject(name);
    ExternalProjectReference ref = new ExternalProjectReference();
    if(prj != null && prj.exists()) {
      ref.project = prj;
      if(prj.isOpen() == false)
        ref.closed = true;
      ref.valid = true;
    }
    else {
      ref.valid = false;
    }
    
    externalProjectReferences.add(ref);
  }

  /**
   * Asks user if he wants to update project from the external Eclipse projects.
   */
  private void askUserForReplacingExternalProjectReferences()
  {
    // NOW AT FIRST CHECK IF THERE IS SOME PROJECT MISSING OR NOT
    Iterator<ExternalProjectReference> it = externalProjectReferences.iterator();
    boolean haveAtLeastOneValid = false;
    boolean haveAtLeastOneClosed0 = false;
    while(it.hasNext()) {
      ExternalProjectReference ref = it.next();
      if(ref.valid) {
        haveAtLeastOneValid  = true;
      }
      if(ref.closed)
        haveAtLeastOneClosed0 = true;
    }
    
    if(haveAtLeastOneValid == false)
      return;
    final boolean haveAtLeastOneClosed = haveAtLeastOneClosed0;
    
    // OK, SO ASK YUSR IF HE WANTS TO UPDATE EXTERNAL PROJECT REFERENCES
    // TODO: move UI code out of here
    final boolean[] result = new boolean[2];
    Display.getDefault().syncExec(new Runnable() {
      public void run()
      {
        Shell shell = Display.getDefault().getActiveShell();
        String title = "Update Tornado application externally referenced files";
        String msg = "This Tornado application contains referenced external projects\n"
                     + "which appear to be installed on your eclipse workspace. \n\n"
                     + "Do you wish to update those externally referenced objects now?";
        if(haveAtLeastOneClosed) {
          String toogleMsg = "Also open closed projects";
          MessageDialogWithToggle dlg = new MessageDialogWithToggle(shell, title, null, msg,
                                                                    MessageDialogWithToggle.QUESTION,
                                                                    new String[] { IDialogConstants.YES_LABEL,
                                                                    IDialogConstants.NO_LABEL },
                                                                    0, toogleMsg, true);
          result[0] = dlg.open() == 0;
          result[1] = dlg.getToggleState();
        }
        else {
          result[0] = MessageDialog.openQuestion(shell, title, msg);
        }
      }
    });
    this.synchronizeExternalProjects = result[0];
    this.openExternalProjectsOnSynchronize = result[1];
  }

  public void setJavaConfiguration(JavaProjectConfiguration conf) throws PuakmaCoreException, IOException, CoreException
  {
    saveJavaConfigurationToServer(conf);
    setupJava(conf, application, javaProject);
  }
  
  /**
   * Saves java configuration to the server.
   */
  private void saveJavaConfigurationToServer(JavaProjectConfiguration conf)
                                             throws PuakmaCoreException, IOException
  {
    DesignObject dob = application.getDesignObject(VORTEX_JRE_CONFIGURATION_DO_NAME);
    if(dob == null) {
      dob = ObjectsFactory.createDesignObject(VORTEX_JRE_CONFIGURATION_DO_NAME,
                                              DesignObject.TYPE_CONFIGURATION);
      application.addObject(dob);
    }
    
    Properties p = new Properties();
    p.setProperty(KEY_USE_SERVER_JRE, Boolean.toString(conf.useLatestServerCompatibleJdk()));
    String confJavaVersion = conf.getJavaVersion();
    if(confJavaVersion != null)
      p.setProperty(KEY_JRE_VERSION, confJavaVersion);
    
    
    boolean inheritJavaCoreOptions = true;
    Map m = javaProject.getOptions(inheritJavaCoreOptions);
    Iterator it = m.keySet().iterator();
    while(it.hasNext()) {
      String key = (String) it.next();
      String val = (String) m.get(key);
      p.setProperty(key, val);
    }
    
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    p.store(bos, null);
    ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
   
    dob.upload(bis, false);
  }
  
  public void uploadJavaConfiguration() throws PuakmaCoreException, IOException
  {
    saveJavaConfigurationToServer(configuration);
  }

  /**
   * Sets up java configuration to this project.
   */
  private void setupJava(JavaProjectConfiguration conf, Application application,
                         IJavaProject javaProject) throws CoreException
  {
    this.configuration = conf;
    
    IVMInstall vm = getVMInstall(conf, application);
    IPath path = JavaRuntime.newJREContainerPath(vm);
    IClasspathEntry entry = JavaCore.newContainerEntry(path);
    setupJre(javaProject, entry);
    
    String javaVersion;
    if(conf.useLatestServerCompatibleJdk())
      javaVersion = ProjectUtils.getServerJavaVersion(application);
    else
      javaVersion = ProjectUtils.convertEnvNameToJavaVersion(conf.getJavaVersion());
    
    javaProject.setOption(JavaCore.COMPILER_COMPLIANCE, javaVersion);
    javaProject.setOption(JavaCore.COMPILER_SOURCE, javaVersion);
    javaProject.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, javaVersion);
    
    ProjectUtils.buildProject(javaProject.getProject(), false, null);
  }

  public boolean isOpen()
  {
    return this.open;
  }
}
