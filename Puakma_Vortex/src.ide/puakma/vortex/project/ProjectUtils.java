/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jul 7, 2005
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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.zip.CRC32;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;

import puakma.coreide.PuakmaCoreException;
import puakma.coreide.RefreshEvent;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.JavaObject;
import puakma.coreide.objects2.ResourceObject;
import puakma.coreide.objects2.ServerDataStatus;
import puakma.utils.MimeTypesResolver;
import puakma.utils.lang.ArrayUtils;
import puakma.utils.lang.ClassUtil;
import puakma.utils.lang.JDKUtils;
import puakma.utils.lang.StringUtil;
import puakma.vortex.IdeException;
import puakma.vortex.VortexPlugin;
import puakma.vortex.editors.application.JavaPropertiesPage;
import puakma.vortex.project.queue.DUQueue;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.zip.CRC32;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;

import puakma.coreide.PuakmaCoreException;
import puakma.coreide.RefreshEvent;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.JavaObject;
import puakma.coreide.objects2.ResourceObject;
import puakma.coreide.objects2.ServerDataStatus;
import puakma.utils.MimeTypesResolver;
import puakma.utils.lang.ArrayUtils;
import puakma.utils.lang.ClassUtil;
import puakma.utils.lang.JDKUtils;
import puakma.utils.lang.StringUtil;
import puakma.vortex.IdeException;
import puakma.vortex.VortexPlugin;
import puakma.vortex.editors.application.JavaPropertiesPage;
import puakma.vortex.project.queue.DUQueue;

/**
 * Utility class for eclipse projects.
 *
 * @author Martin Novak
 */
public class ProjectUtils
{
	public static final int CHANGE_ASK_USER = 0;
	public static final int CHANGE_IGNORE   = 1;
	public static final int CHANGE_DOWNLOAD = 2;

	/**
	 * Gets Path of the data/source of the design object relatively to the project.
	 *
	 * @param object is the design object to get path
	 * @param isSource if true then source path should be generated otherwise data path will
	 *                 be generated
	 * @return <code>IPath</code> object with the path to the object
	 */
	public static IPath getFilePath(DesignObject object, boolean isSource)
	{
		IPath p = getFileDirectoryPath(object, isSource);
		p = p.append(getFileName(object, isSource));
		return p;
	}

	/**
	 * This function is the same as getFilePath, but it returns the path with
	 * the name of the project.
	 *
	 * @param object is the object to get path
	 * @param isSource true then path of the source is retrieved
	 * @return IPath relative to the workspace
	 */
	public static IPath getFullFilePath(DesignObject object, boolean isSource)
	{
		IProject prj = ProjectManager.getIProject(object.getApplication());
		//IProject prj = (IProject) ProjectManager.getProject(object.getApplication());
		IPath p = prj.getFullPath();
		p = p.append(getFilePath(object, isSource));
		return p;
	}

	/**
	 * Gets the file extension for the data/source of the design object.
	 *
	 * @param object is the design object to get extension from
	 * @param isSource if true, then try to get extension for source, otherwise for data
	 * @return the most accurate design object file extension (without "." character
	 *         at the beginning)
	 */
	public static String getFileExtension(DesignObject object, boolean isSource)
	{
		if(object instanceof JavaObject) {
			String s;
			if(isSource)
				s = "java";
			else
				s = "class";
			return s;
		}
		else if(object instanceof ResourceObject) {
			ResourceObject obj = (ResourceObject) object;
			String s;
			switch(obj.getDesignType()) {
			case DesignObject.TYPE_PAGE:
				s = PuakmaProject2.PAGE_EXT;
				break;
			case DesignObject.TYPE_JAR_LIBRARY:
				s = PuakmaProject2.JAR_EXT;
				break;
			case DesignObject.TYPE_CONFIGURATION:
			case DesignObject.TYPE_RESOURCE:
				s = MimeTypesResolver.getDefaultExt(obj.getContentType());
				break;
			default:
				throw new IllegalArgumentException("Invalid design object type");
			}

			return s;
		}
		else
			throw new IllegalArgumentException("Invalid design object");
	}

	public static String getFileName(DesignObject object, boolean isSource)
	{
		if(object instanceof JavaObject) {
			JavaObject obj = (JavaObject) object;
			String s;
			if(isSource)
				s = ClassUtil.getBaseClass(obj.getClassName()) + "." + getFileExtension(object, isSource);
			else
				s = obj.getClassName() + "." + getFileExtension(object, isSource);
			return s;
		}
		else if(object instanceof ResourceObject) {
			ResourceObject obj = (ResourceObject) object;
			String s;
			switch(obj.getDesignType()) {
			case DesignObject.TYPE_PAGE:
			case DesignObject.TYPE_JAR_LIBRARY:
			case DesignObject.TYPE_RESOURCE:
			case DesignObject.TYPE_CONFIGURATION:
				s = obj.getName();
				break;
			default:
				throw new IllegalArgumentException("Invalid design object type");
			}

			return s + "." + getFileExtension(object, isSource);
		}
		else
			throw new IllegalArgumentException("Invalid design object");
	}

	/**
	 * Returns path of the design object relatively to the project
	 * @param object
	 * @param isSource
	 * @return IPath object containing directory of the design object src/bin file
	 */
	public static IPath getFileDirectoryPath(DesignObject object, boolean isSource)
	{
		IPath p;
		if(object instanceof JavaObject) {
			JavaObject obj = (JavaObject) object;
			if(isSource)
				p = new Path(PuakmaProject2.DIR_SRC);
			else
				p = new Path(PuakmaProject2.DIR_BIN);
			// NOW ADD PACKAGE
			if(obj.getPackage() != null && obj.getPackage().length() > 0) {
				String packageName = obj.getPackage().replace('.', '/');
				p = p.append(packageName);
			}
		}
		else if(object instanceof ResourceObject) {
			ResourceObject obj = (ResourceObject) object;
			switch(obj.getDesignType()) {
			case DesignObject.TYPE_PAGE:
				p = new Path(PuakmaProject2.DIR_PAGES);
				break;
			case DesignObject.TYPE_JAR_LIBRARY:
				if(isSource)
					p = new Path(PuakmaProject2.DIR_LIB_SRC);
				else
					p = new Path(PuakmaProject2.DIR_LIB);
				break;
			case DesignObject.TYPE_RESOURCE:
				p = new Path(PuakmaProject2.DIR_RESOURCE);
				break;
			case DesignObject.TYPE_CONFIGURATION:
				p = new Path(PuakmaProject2.DIR_CONFIGURATION);
				break;
			default:
				throw new IllegalArgumentException("Invalid design object type");
			}
		}
		else
			throw new IllegalArgumentException("Invalid design object");
		return p;
	}

	public static IFolder getFileFolder(IProject project, DesignObject object, boolean isSource) throws CoreException
	{
		IPath basePath = getFileDirectoryPath(object, isSource);
		IFolder folder = project.getFolder(basePath);
		return folder;
	}

	/**
	 * This returns <code>IFile</code> object relative to the project which
	 * represents DesignObject's data/source. If there is collision between
	 * external project, and the main project, the main project file is picked.
	 * 
	 * @param object is the DesignObject object for which we are looking for file
	 * @param isSource if true then we are looking for source, otherwise for data
	 * @return IFile object - file can exist, but it's not a need
	 */
	public static IFile getIFile(DesignObject object, boolean isSource)
	{
		IProject iProject = getMainIProject(object.getApplication());
		IPath path = getFilePath(object, isSource);
		IFile extFile = iProject.getFile(path);
		return extFile;
	}

	/**
	 * Finds the correct {@link IFile} for the design object. Note that if the
	 * file doesn't exist, returns null.
	 */
	public static IFile findIFile(DesignObject object, boolean isSource)
	{
		// AT FIRST TRY TO GET THE FILE FROM THE LOCAL PROJECT
		IProject iProject = getMainIProject(object.getApplication());
		IPath path = getFilePath(object, isSource);
		IFile file = iProject.getFile(path);
		if(file.exists())
			return file;
		if(isObjectFromExternalProject(object) == false)
			return file;

		// IF THAT FAILS, TRY TO GET AN EXTERNAL FILE
		iProject = getExternalIProject(object);
		if(iProject != null) {
			file = iProject.getFile(path);
			if(file.exists())
				return file;
		}

		return null;
	}

	public static boolean isObjectFromExternalProject(DesignObject object)
	{
		return object.getParameterValue(PuakmaProject2.PARAMETER_EXTERNAL_REFERENCE) != null;
	}

	public static IFile getIFile(DesignObject object, boolean isSource, IProject project)
	{
		IPath path = getFilePath(object, isSource);
		return project.getFile(path);
	}

	public static IProject getExternalIProject(DesignObject object)
	{
		Object o = object.getParameterValue(PuakmaProject2.PARAMETER_EXTERNAL_REFERENCE);
		if(o != null) {
			String name = (String) o;
			IWorkspace wSpace = ResourcesPlugin.getWorkspace();
			IWorkspaceRoot wRoot = wSpace.getRoot();
			IProject project = wRoot.getProject(name);
			return project;
		}

		return null;
	}

	public static IProject getMainIProject(Application application)
	{
		if(application == null)
			throw new IllegalArgumentException("Application parameter is null");

		PuakmaProject2 pProject = ProjectManager.getProject(application);
		return pProject.getProject();
	}

	/**
	 * This function returns IFile object from the relative
	 * @param path is the path from which we convert the 
	 * @return IFile object
	 */
	public static IFile getIFile(IPath path)
	{
		PuakmaProject2 pProject = ProjectManager.getProject(path.segment(0));
		IProject prj = pProject.getProject();
		return prj.getFile(path.removeFirstSegments(1));
	}

	/**
	 * This function returns IFile object, but the one with some different name. This
	 * can be used for example to get file before is renamed
	 *
	 * @param object is the DesignObject for directory
	 * @param isSource specifies if we want design source or data
	 * @param fileName is the different file name from the original 
	 * @return IFile file
	 * @throws CoreException 
	 */
	public static IFile getRenamedIFile(DesignObject object, boolean isSource,
			String fileName) throws CoreException
			{
		if(object instanceof JavaObject)
			return getIFile(object, isSource);

		IProject iProject = ProjectManager.getIProject(object.getApplication());
		IFolder folder = getFileFolder(iProject, object, isSource);
		IPath path = folder.getProjectRelativePath().append(fileName
				+ "." + getFileExtension(object, isSource));
		IFile file = iProject.getFile(path);
		return file;
			}

	/**
	 * This function gets <code>JavaObject</code> object from <code>IResource</code> object
	 * 
	 * @param project is the Puakma project inside which we want to search
	 *                for the java object 
	 * @param res is the workspace resource which we use as search key
	 * @return JavaObject object or null if the resource is unknown
	 */
	public static JavaObject getJavaObject(PuakmaProject2 project, IResource res)
	{
		return getJavaObject(project, res.getFullPath());
	}

	/**
	 * TThis function returns {@link JavaObject} from the {@link IFile} object.
	 * 
	 * @param file is the file in the workspace
	 * @return {@link JavaObject} or null if there is no such {@link JavaObject}
	 */
	public static JavaObject getJavaObject(IFile file)
	{
		IPath fullPath = file.getFullPath();
		String projectName = file.getProject().getName();
		PuakmaProject2 project = ProjectManager.getProject(projectName);
		if(project == null)
			return null;
		JavaObject jo = getJavaObject(project, fullPath);
		return jo;
	}

	/**
	 * This function gets <code>JavaObject</code> object from <code>IPath</code> object
	 * 
	 * @param project is the Puakma project inside which we want to search
	 *                for the java object 
	 * @param fullPath is the workspace relative path to the resource we want to find
	 * @return JavaObject object or null if the resource is unknown
	 */
	public static JavaObject getJavaObject(PuakmaProject2 project, IPath fullPath)
	{
		// CHECK FOR VALID FILE
		if("java".equals(fullPath.getFileExtension()) == false
				&& "class".equals(fullPath.getFileExtension()) == false)
			return null;
		// REMOVE PROJECT AND BIN/SRC FROM THE BEGINNING
		fullPath = fullPath.removeFirstSegments(2);
		// CREATE PACKAGE NAME
		IPath packagePath = fullPath.removeLastSegments(1);
		String packageName = StringUtil.merge(packagePath.segments(), ".");
		// CREATE CLASS NAME
		String className = fullPath.removeFileExtension().segment(fullPath.segmentCount() - 1);
		// GET JAVAOBJECT
		Application app = project.getApplication();
		JavaObject obj = app.getJavaObject(packageName, className);
		return obj;
	}

	/**
	 * This function returns class name from the workspace resource.
	 * 
	 * TODO: remove IdeException from this object, and return null or empty class name
	 *
	 * @param res is the workspace resource from which we extract class name
	 * @return non null class name
	 * @throws IdeException If the path to the resource is not valid for the java
	 *                      objects - throws IdeException. Also throws IdeException
	 *                      if the resource file extension is not class or java.
	 */
	public static String getClassFromResource(IResource res) throws IdeException
	{
		IPath p = checkPathForErrors(res);

		String ret = p.segment(p.segmentCount() - 1);
		return ret;
	}

	public static String getClassFromPath(IPath path) throws IdeException
	{
		IPath p = checkPathForErrors(path);

		String ret = p.segment(p.segmentCount() - 1);
		return ret;
	}

	/**
	 * Returns package name from the resource.
	 * 
	 * @param res is the workspace resource from which we extract class name
	 * @return non null package name
	 * @throws IdeException If the path to the resource is not valid for the java
	 *           objects - throws IdeException. Also throws IdeException if the
	 *           resource file extension is not class or java.
	 */
	public static String getPackageFromResource(IResource res) throws IdeException
	{
		IPath p = checkPathForErrors(res).removeLastSegments(1);

		String[] paths = p.segments();
		String ret = StringUtil.merge(paths, ".");
		return ret;
	}

	/**
	 * Returns package name from the path. Note that the path is for file, not for
	 * directory.
	 * 
	 * @param path is the path of the resource
	 * @return non null package name
	 * @throws IdeException If the path to the resource is not valid for the java
	 *           objects
	 */
	public static String getPackageFromPath(IPath path) throws IdeException
	{
		IPath p = checkPathForErrors(path).removeLastSegments(1);
		String[] paths = p.segments();
		String ret = StringUtil.merge(paths, ".");
		return ret;
	}

	/**
	 * Checks path for java resource. And also returns path relative to bin/src
	 * directory without extension.
	 *
	 * @param res is the resource to check
	 * @return IPath object relative to src/bin dir and without extension
	 * @throws IdeException on some resource path error
	 */
	private static IPath checkPathForErrors(IResource res) throws IdeException
	{
		IPath p = checkPathForErrors(res.getFullPath());
		return p;
	}


	private static IPath checkPathForErrors(IPath p) throws IdeException
	{
		String ext = p.getFileExtension();
		if("class".equals(ext) == false && "java".equals(ext) == false)
			throw new IdeException("Cannot get class name - invalid java file extension.");
		int segmentsCount = p.segmentCount();
		if(segmentsCount == 0)
			throw new IdeException("Invalid path to resource " + p.toString());
		String dir = p.segment(1);
		if(("class".equals(ext) && PuakmaProject2.DIR_BIN.equals(dir)) == false
				&& ("java".equals(ext) && PuakmaProject2.DIR_SRC.equals(dir)) == false)
			throw new IdeException("Invalid directory for java files");
		p = p.removeFirstSegments(2).removeFileExtension();
		return p;
	}

	public static IFolder createFileFolder(IProject project, DesignObject object,
			boolean isSource) throws CoreException
			{
		IFolder folder = getFileFolder(project, object, isSource);
		createFolder(folder);
		return folder;
			}

	public static void createFolder(IFolder folder) throws CoreException
	{
		LinkedList<IFolder> l = new LinkedList<IFolder>();
		while(folder.exists() == false) {
			l.add(folder);
			IResource res = folder.getParent();
			if(res instanceof IFolder)
				folder = (IFolder) res;
			else
				break;
		}

		ListIterator<IFolder> it = l.listIterator(l.size());
		while(it.hasPrevious()) {
			folder = it.previous();
			folder.create(true, true, null);
		}
	}

	/**
	 * Gets the design object which resides in the file. Note that file with inside
	 * directories are supported too, so design objects can be named eg. "Images/2004/Img11"
	 *
	 * @param file is the file of the DesignObject
	 * @return DesignObject instance or null if the file doesn't belong to any
	 *         opened application
	 */
	public static DesignObject getDesignObject(IFile file)
	{
		IPath path = file.getFullPath();
		return getDesignObject(path);
	}

	/**
	 * Gets DesignObject according to the full path.
	 *
	 * @param fullPath is the resource's full path in the workspace
	 * @return DesignObject associated with the path or null if there is
	 *         no DesignObject on this path
	 */
	public static DesignObject getDesignObject(IPath fullPath)
	{
		IPath prjPath = fullPath.removeFirstSegments(1);
		if(prjPath.segmentCount() < 2)
			return null;

		String s = fullPath.segment(0);
		PuakmaProject2 project = ProjectManager.getProject(s);
		if(project == null)
			return null;
		Application application = project.getApplication();
		String root = prjPath.segment(0);
		if(PuakmaProject2.DIR_BIN.equals(root) || PuakmaProject2.DIR_SRC.equals(root)) {
			return getJavaObject(project, fullPath);
		}
		else if(PuakmaProject2.DIR_LIB.equals(root) || PuakmaProject2.DIR_LIB_SRC.equals(root)
				|| PuakmaProject2.DIR_PAGES.equals(root) || PuakmaProject2.DIR_RESOURCE.equals(root))
		{
			prjPath = prjPath.removeFirstSegments(1).removeFileExtension();
			String name = StringUtil.merge(prjPath.segments(), "/");
			return application.getDesignObject(name);
		}
		return null;
	}

	/**
	 * This function checks if the resource is resource from some puakma project.
	 * @param resource is the resource to check
	 * @return true if the resource is from the puakma project
	 */
	public static PuakmaProject2 getProject(IResource resource)
	{
		IPath path = resource.getFullPath();
		if(path.segmentCount() < 1)
			return null;
		String prjName = path.segment(0);
		return ProjectManager.getProject(prjName);
	}

	/**
	 * Downloads the design object default file
	 * @param obj
	 * @param isSource 
	 * @throws IdeException 
	 */
	public static void downloadFile(DesignObject obj, boolean isSource) throws IdeException
	{
		IFile file = getIFile(obj, isSource);
		ByteArrayOutputStream os = null;
		ByteArrayInputStream is = null;
		try {
			os = new ByteArrayOutputStream();
			obj.download(os, isSource);
			is = new ByteArrayInputStream(os.toByteArray());
			if(file.exists()) {
				file.setContents(is, true, true, null);
			}
			else {
				createFolder((IFolder) file.getParent());
				file.create(is, true, null);
			}
		}
		catch(Exception e) {
			throw new IdeException("Cannot get content of the design object. Reason: "
					+ e.getLocalizedMessage(), e);
		}
		finally {
			try {
				if(os != null)
					os.close();
			}
			catch(IOException e) {
				VortexPlugin.log(e);
			}
		}
	}

	public static void uploadFile(DesignObject obj, boolean isSource) throws IdeException
	{
		uploadFile(obj, isSource, true);
	}

	public static void uploadFile(DesignObject obj, boolean isSource, boolean flushCache) throws IdeException
	{
		uploadFile(obj, isSource, flushCache, false);
	}

	/**
	 * This function before uploads the file checks if the CRC of the file on the disk is not the same
	 * as on the server. If so, it does nothing, so we can optimize performance for some cases when
	 * upload is not necessary, but the event is fired.
	 * 
	 * <p>TODO: reimplement this using download queue
	 * 
	 * @param obj
	 * @param isSource
	 * @param flushCache
	 * @param checkCrc if true then this function checks crc before it uploads the file
	 * @return true if the file was uploaded, false otherwise
	 * @throws IdeException
	 */
	public static boolean uploadFile(DesignObject obj, boolean isSource, boolean flushCache, boolean checkCrc) throws IdeException
	{
		IFile file = getIFile(obj, isSource);
		if(file.exists() == false)
			throw new IdeException("Cannot upload design object " + obj.toString() + 
					". Reason: File doesn't exist");
		PuakmaProject2Impl prj = (PuakmaProject2Impl) ProjectUtils.getProject(file);
		DUQueue queue = prj.getDownloadQueue();
		try {
			return queue.uploadInTheSameThread(obj, isSource);
		}
		catch(CoreException e) {
			throw new IdeException(e);
		}
		catch(IOException e) {
			throw new IdeException(e);
		}
	}

	public static void setFileContent(IFile file, FileInputStream is) throws CoreException
	{
		if(file.exists())
			file.setContents(is, true, true, null);
		else {
			createFolder((IFolder) file.getParent());
			file.create(is, true, null);
		}
	}

	/**
	 * Copies file to workspace.
	 * 
	 * @param srcFile is the source file outside of the workspace
	 * @param destFile is the destination file in the workspace
	 * @param overWrite if true then file will be overwritten, otherwise doesn't
	 *                  do anything
	 * @throws CoreException if there is some error writing file to the workspace
	 * @throws FileNotFoundException 
	 */
	public static void copyFile(File srcFile, IFile destFile, boolean overWrite) throws CoreException, FileNotFoundException
	{
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(srcFile);
			if(destFile.exists()) {
				if(overWrite)
					setFileContent(destFile, fis);
			}
			else {
				setFileContent(destFile, fis);
			}
		}
		finally {
			if(fis != null) try { fis.close(); } catch(IOException e) { }
		}
	}

	/**
	 * Returns compilation unit for the java object provided. Note that
	 * the compilation is always returned, because it doesn't matter if CU exists
	 * or not.
	 *
	 * @param object is the java object to create
	 * @return compilation unit for the java object
	 */
	public static ICompilationUnit getCompilationUnit(JavaObject object)
	{
		boolean isSource = true; // WE WANT SOURCE IN JAVA COMPILATION UNIT
		IFile file = getIFile(object, isSource);
		IJavaElement element = JavaCore.create(file);
		return (ICompilationUnit) element; // BECAUSE WE HAVE JAVA FILE THERE
	}

	/**
	 * This function checks resource whether it is or not a part of connected application.
	 *
	 * @param resource is non null resource we want to examine
	 * @return Application object or null if the resource is not a part of project
	 */
	public static Application getApplication(IResource resource)
	{
		assert resource != null : "Invalid null argument";

		IProject project = resource.getProject();
		PuakmaProject2 pp = ProjectManager.getProject(project.getName());
		if(pp != null)
			return pp.getApplication();
		else
			return null;
	}

	/**
	 * Adds libraries to the java project.
	 * @param javaProject is the java project
	 * @param paths are the absolute paths to the libraries to add to the project
	 * @throws JavaModelException
	 */
	public static void addLibraryEntries(IJavaProject javaProject, IPath[] paths,
			IPath[] srcPaths, IPath[] javaDocLocation) throws JavaModelException
			{
		addLibraryEntries(javaProject, paths, srcPaths, javaDocLocation, null);
			}

	public static void addLibraryEntries(IJavaProject javaProject, IPath[] paths,
			IPath[] srcPaths, IPath[] javaDocLocation,
			IPath[] insideJavaDocs) throws JavaModelException
			{
		IClasspathEntry[] entries = javaProject.getRawClasspath();
		MAIN: for(int i = 0; i < paths.length; ++i) {
			IPath path = paths[i];

			//  check if we haven't already puakma.jar present in the classpath
			for(IClasspathEntry entry : entries) {
				IPath ep = entry.getPath();
				if(ep.equals(path)) {
					paths = (IPath[]) ArrayUtils.remove(paths, i);
					continue MAIN;
				}
			}

			IClasspathAttribute[] extraAttribs = null;
			if(javaDocLocation == null || javaDocLocation[i] == null)
				extraAttribs = new IClasspathAttribute[0];
			else if(insideJavaDocs == null || insideJavaDocs[i] == null) {
				try {
					String loc = javaDocLocation[i].toString();
					loc = new File(loc).toURL().toExternalForm();
					extraAttribs = new IClasspathAttribute[] { JavaCore.newClasspathAttribute(IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME,
							loc) };
				}
				catch(Exception ex) {
					VortexPlugin.log(ex);
				}
			}
			else {
				try {
					String jarLoc = javaDocLocation[i].toString();
					String innerPath = insideJavaDocs[i].toString();
					StringBuffer buf= new StringBuffer();
					buf.append("jar:"); //$NON-NLS-1$
					buf.append(encodeExclamationMarks(new File(jarLoc).toURL().toExternalForm()));
					buf.append('!');
					if(innerPath.length() > 0) {
						if(innerPath.charAt(0) != '/')
							buf.append('/');
						buf.append(innerPath);
					}
					else
						buf.append('/');

					extraAttribs = new IClasspathAttribute[] { JavaCore.newClasspathAttribute(IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME,
							buf.toString()) };
				}
				catch(Exception ex) {
					VortexPlugin.log(ex);
				}
			}

			IPath srcPath = srcPaths != null ? srcPaths[i] : null;
			IClasspathEntry cpe = JavaCore.newLibraryEntry(path, srcPath, null, null, extraAttribs , false);
			entries = (IClasspathEntry[]) ArrayUtils.append(entries, cpe);
		}

		javaProject.setRawClasspath(entries, null);
			}

	/**
	 * Encodes exclamations inside the path string, so it can be used as a path inside jar.
	 */
	private static String encodeExclamationMarks(String str)
	{
		StringBuffer buf = new StringBuffer(str.length());
		for(int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			if(ch == '!') {
				buf.append("%21"); //$NON-NLS-1$
			}
			else {
				buf.append(ch);
			}
		}
		return buf.toString();
	}

	public static void addProjectReference(IJavaProject javaProject, IJavaProject externalProject) throws JavaModelException
	{
		IPath path = externalProject.getPath();
		IClasspathEntry entry = JavaCore.newProjectEntry(path);
		IClasspathEntry[] entries = javaProject.getRawClasspath();
		for(int i = 0; i < entries.length; ++i) {
			IPath ep = entries[i].getPath();
			if(ep.equals(path))
				return;
		}

		entries = (IClasspathEntry[]) ArrayUtils.append(entries, entry);
		javaProject.setRawClasspath(entries, null);
	}

	/**
	 * This function checks if the source is potentially more important for the user to edit
	 * @param dob is the requested design object
	 * @return true if the content which should be edited is source
	 */
	public static boolean isSourceMoreImportant(DesignObject dob)
	{
		switch(dob.getDesignType()) {
		case DesignObject.TYPE_ACTION:
		case DesignObject.TYPE_LIBRARY:
		case DesignObject.TYPE_SCHEDULEDACTION:
		case DesignObject.TYPE_WIDGET:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Computes CRC32 for some file
	 * @param file is the file to compute CRC32 on
	 * @return the CRC32 value of the content of the file
	 * @throws CoreException if something goes wrong with files
	 * @throws IOException 
	 */
	public static long computeCrc32(IFile file) throws CoreException, IOException
	{
		CRC32 crc = new CRC32();
		BufferedInputStream is = null;
		try {
			is = new BufferedInputStream(file.getContents());
			byte[] bytes = new byte[1024];
			int len = 0;

			while((len = is.read(bytes)) >= 0) {
				crc.update(bytes, 0, len);
			}
		}
		finally {
			if(is != null)
				try { is.close(); } catch(Exception ex) {}
		}
		return crc.getValue();
	}

	/**
	 * Selects if the change should be downloaded, or we should ask user to decide.
	 * 
	 * @param project is the project in which the event occured
	 * @param event is the change on the server's side
	 * @param isSource determines whether we should use source or data
	 * @return {@link #CHANGE_DOWNLOAD} if the item should be updated, or {@link #CHANGE_ASK_USER}
	 *         if the item should be decided by user or {@link #CHANGE_IGNORE} if the change should
	 *         be ignore because it is not on the local disk... Note also that on some errors it
	 *         returns {@link #CHANGE_IGNORE} rather then throws exception.
	 */
	public static int shouldDownloadChange(PuakmaProject2 project, RefreshEvent event, boolean isSource)
	{
		IFile file = ProjectUtils.getIFile(event.getDob(), isSource);
		if(file.exists() == false)
			return CHANGE_IGNORE;

		// IF THE SIZE ON DISK IS DIFFERENT THEN THE PREVIOUS SIZE, WE CHANGED THE FILE, SO WE NEED TO ASK
		// USER WHAT TO DO
		long localFileLen = file.getLocation().toFile().length();
		long prevFileLen = isSource ? event.getOldSourceSize() : event.getOldDataSize();
		if(localFileLen != prevFileLen)
			return CHANGE_ASK_USER;

		// OK, SO OUR FILE EXISTS, SO NOW COMPUTE CRC
		long localFileCrc;
		try {
			localFileCrc = ProjectUtils.computeCrc32(file);
		}
		catch(Exception e) {
			VortexPlugin.log(e);
			return CHANGE_IGNORE;
		}

		// AT FIRST CHECK IF WE HAVE CHANGED THE FILE SINCE WE HAVE DOWNLOADED IT
		// IF NOT, WE SHOULD DOWNLOAD THE FILE
		long prevServerCrc = isSource ? event.getOldCrc32Source() : event.getOldCrc32Data();
		if(localFileCrc == prevServerCrc) {
			return CHANGE_DOWNLOAD;
		}

		// OR WE BETTER ASK USER WHAT TO DO
		return CHANGE_ASK_USER;
	}

	/**
	 * Converts jdk version to eclipse java environment name.
	 *
	 * @param ver is the version according to {@link JDKUtils} class constants
	 * @return string with environment or null if ver is unknown
	 */
	public static String convertJDKVersionToEnvName(int ver)
	{
		ver = JDKUtils.normalizeToMainReleases(ver);
		String ret = null;
		if(ver == JDKUtils.JDK_VERSION_1_4_0)
			ret = "J2SE-1.4";
		else if(ver == JDKUtils.JDK_VERSION_1_5_0)
			ret = "J2SE-1.5";
		else if(ver == JDKUtils.JDK_VERSION_1_6_0)
			ret = "JavaSE-1.6";
		return ret;
	}

	/**
	 * Returns the environment name for the application. So this returns
	 * "J2SE-1.4" for java 1.4, etc...
	 */
	public static String getServerEnvironmentName(Application application)
	{
		String version = application.getServer().getEnvironmentProperty("java.version");
		if(version == null)
			return null;
		int ver = JDKUtils.guessJdk(version);
		ver = JDKUtils.normalizeToMainReleases(ver);
		String envName = convertJDKVersionToEnvName(ver);
		return envName;
	}

	public static IVMInstall getServerVMInstall(Application application)
	{
		String envName = getServerEnvironmentName(application);
		if(envName == null)
			envName = "J2SE-1.4";
		IVMInstall install = getVMInstallByEnvName(envName);
		return install;
	}

	/**
	 * Gets the jre installation which is strictly compatible with the execution environment.
	 */
	public static IVMInstall getVMInstallByEnvName(String envName)
	{
		if(envName == null)
			throw new IllegalArgumentException("Environment cannot be null");

		IExecutionEnvironmentsManager manager = JavaRuntime.getExecutionEnvironmentsManager();
		IExecutionEnvironment env = manager.getEnvironment(envName);
		if(env == null)
			throw new IllegalStateException("Environment '" + envName +
					"' cannot be found. Please check your installation. " +
					"Probably org.eclipse.pde.core plugin is missing");
		IVMInstall install = env.getDefaultVM();
		if(install != null && env.isStrictlyCompatible(install))
			install = null;

		if(install == null) {
			IVMInstall[] installs = env.getCompatibleVMs();
			for(int i = 0; i < installs.length; ++i) {
				if(env.isStrictlyCompatible(installs[i]))
					return installs[i];
			}
		}
		return install;
	}

	public static IVMInstall getVMInstallById(String id)
	{
		IVMInstallType[] types = JavaRuntime.getVMInstallTypes();
		for(int i = 0; i < types.length; ++i) {
			IVMInstall[] installs = types[i].getVMInstalls();
			for(int j = 0; j < installs.length; ++j) {
				if(installs[j].getId().equals(id))
					return installs[j];
			}
		}
		return null;
	}

	/**
	 * Converts environment name to java version string, so it means that converts
	 * J2SE-1.4 to 1.4, etc...
	 */
	public static String convertEnvNameToJavaVersion(String javaVersion)
	{
		String[] envTable = {
				JavaPropertiesPage.ENV_JAVA_1_5,
				JavaPropertiesPage.ENV_JAVA_1_4, JavaPropertiesPage.ENV_JAVA_1_6 
		};
		String[] javaVersionTable = {
				"1.5", "1.4", "1.6"
		};

		for(int i = 0; i < envTable.length; ++i) {
			if(envTable[i].equals(javaVersion))
				return javaVersionTable[i];
		}

		return null;
	}

	/**
	 * Gets the java version string from the server environment. If this is not
	 * possible, returns 1.4. Note that returned string would be one of 1.4, 1.5,
	 * and 1.6.
	 */
	public static String getServerJavaVersion(Application application)
	{
		String envName = ProjectUtils.getServerEnvironmentName(application);
		if(envName == null)
			envName = "J2SE-1.4";
		return convertEnvNameToJavaVersion(envName);
	}

	/**
	 * Builds a project.
	 * 
	 * @param project is the project to build
	 * @param waitUntilFinishes if true this thread will wait until the whole build is done
	 * @param monitor monitors operation progress
	 */
	public static void buildProject(IProject project, boolean waitUntilFinishes, IProgressMonitor monitor) throws CoreException
	{
		if(monitor == null)
			monitor = new NullProgressMonitor();

		try {
			project.build(IncrementalProjectBuilder.AUTO_BUILD, monitor);
			if(waitUntilFinishes) {
				IJobManager manager = Job.getJobManager(); //Platform.getJobManager();
				// DO NOT USE MONITOR HERE
				manager.join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
			}
		}
		catch(OperationCanceledException e) {
			// SILENCE HERE
		}
		catch(InterruptedException e) {
			// SILENCE HERE
		}
		finally {
			monitor.done();
		}
	}

	/**
	 * This function sets up jar library in the java project. If there is only
	 * source, it does nothing. If there is only binary, adds only binary. If
	 * there is both adds library reference which contains both.
	 */
	public static void setupLibraryInProject(IJavaProject project, DesignObject object) throws JavaModelException
	{
		if(object.getDesignType() == DesignObject.TYPE_JAR_LIBRARY)
			return;

		IFile binRes = getIFile(object, false);
		IFile srcRes = getIFile(object, true);
		boolean hasBin = binRes.exists();
		if(hasBin == false)
			return;
		boolean hasSrc = srcRes.exists();

		IPath[] paths = {
				binRes.getFullPath(),
		};
		IPath[] srcPaths = {
				hasSrc ? srcRes.getFullPath() : null,
		};
		addLibraryEntries(project, paths, srcPaths, null);
	}

	public static IFile findFileInProject(IProject project, JavaObject jobj, boolean isSource)
	{
		StringBuffer sb = new StringBuffer();
		if(isSource)
			sb.append("src/");
		else
			sb.append("bin/");
		sb.append(jobj.getPackage());
		sb.append(".");
		sb.append(jobj.getClassName());
		for(int i = 0; i < sb.length(); ++i) {
			if(sb.charAt(i) == '.')
				sb.setCharAt(i, '/');
		}

		if(isSource)
			sb.append(".java");
		else
			sb.append(".class");

		IPath path = new Path(sb.toString());
		IResource res = project.findMember(path);
		return res instanceof IFile ? (IFile) res : null;
	}
}
