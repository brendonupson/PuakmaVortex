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

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

import puakma.coreide.PuakmaCoreException;
import puakma.coreide.RefreshEventInfoImpl;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.DesignObject;
import puakma.vortex.IdeException;

/**
 * @author Martin Novak
 */
public interface PuakmaProject2
{
  /**
   * This prefix is to identify external project for design objects. It is set
   * as parameter of each design object which links to the external project.
   */
  String PARAMETER_EXTERNAL_REFERENCE = "externalReference";
  
  String DIR_BIN = "bin";
  String DIR_SRC = "src";
  String DIR_PAGES = "pages";
  String DIR_LIB = "lib";
  String DIR_LIB_SRC = "libsrc";
  String DIR_RESOURCE = "resources";
  String DIR_CONFIGURATION = "config";

  /**
   * The default extension for the page design object
   */
  String PAGE_EXT = "phtml";
  String JAR_EXT = "jar";

  void setApplication(Application application);
  
  IProject getProject();
  
  IJavaProject getJavaProject();

  /**
   * Path of the root directory of the project
   */
  IPath getBasePath();
  
  /**
   * Downloads all the files from the server. This should be called from some
   * code after application refresh.
   * 
   * TODO: move this to internals of this class
   * 
   * @param info is the information about all the files which we have to
   *          download
   */
  void refresh(RefreshEventInfoImpl info, IProgressMonitor monitor) throws IdeException;

  Application getApplication();

  /**
   * This function starts java functionality for the project, which might can take
   * some time (expect quite longer times there). Refreshes design all the design
   * objects, downloads all the java objects, etc...
   * 
   * <p>Also fires some events like ProjectLifecycleListener#postJavaProjectDownload
   *
   * @param monitor is the monitor for measuring progress of operation
   * @throws IdeException 
   * @throws CoreException 
   */
  void startJava(IProgressMonitor monitor) throws IdeException, CoreException;
  
  /**
   * This function checks if the java functionality has been already started, so
   * we can open java objects.
   *
   * @return true if the java has already been started, false otherwise
   */
  boolean javaStarted();

  // TODO: remove this file???
  void addLibraryToClassPath(DesignObject project, boolean isSource) throws CoreException;
  
  JavaProjectConfiguration getJavaConfiguration();

  void setJavaConfiguration(JavaProjectConfiguration conf)
                                   throws JavaModelException, PuakmaCoreException,
                                          IOException, CoreException;
  
  void setupJavaProject(IProgressMonitor monitor) throws CoreException;

  void uploadJavaConfiguration() throws PuakmaCoreException, IOException;
  
  /**
   * Specifies if the project is open. This means that there is an eclipse project open.
   */
  boolean isOpen();
}
