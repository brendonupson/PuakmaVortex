/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    09/11/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.project;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import puakma.coreide.ObjectsFactory;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.JavaObject;
import puakma.vortex.IdeException;
import puakma.vortex.VortexPlugin;
import puakma.vortex.project.queue.DownloadItem;
import puakma.vortex.project.resource.IFileDelta;
import puakma.vortex.project.resource.IFileDeltaListVisitor;

public class PuakmaProjectExternalUploader implements IFileDeltaListVisitor
{
  private PuakmaProject2Impl pProject;

  private IProject[] projects;
  
  private static final int NEW = 0;
  private static final int REM = 2;
  private static final int MOVE = 3;
  private static final int CHANGED = 4;
  
  public static class ObjectInfo
  {
    public boolean isSource;
    public int status;
    public IFileDelta delta;
    public String packageName;
    public String className;
    private JavaObject jo;
    public ObjectInfo(boolean isSource, int status, IFileDelta delta) {
      this.isSource = isSource; this.status = status; this.delta = delta;
    }
    public ObjectInfo(boolean isSource, int status, IFileDelta delta, String packageName,
                      String className)
    {
      this.isSource = isSource; this.status = status; this.delta = delta;
      this.packageName = packageName; this.className = className;
    }
    public ObjectInfo(boolean isSource, int status, IFileDelta delta, JavaObject jo) {
      this.isSource = isSource; this.status = status; this.delta = delta;
      this.jo = jo;
    }
    public ObjectInfo(boolean isSource, int status, IFileDelta delta, JavaObject jo,
                      String packageName, String className) {
      this.isSource = isSource; this.status = status; this.delta = delta;
      this.packageName = packageName; this.className = className;
      this.jo = jo;
    }
  }


  public PuakmaProjectExternalUploader(PuakmaProject2Impl pProject, IProject[] projects)
  {
    if(projects == null)
      throw new IllegalArgumentException("Invalid projects list was passed to the function");

    this.pProject = pProject;
    this.projects = projects;
  }

  public void acceptAllDeltas(IFileDelta[] deltas)
  {
    Application application = pProject.getApplication();
    final List<ObjectInfo> operations = new ArrayList<ObjectInfo>();
    
    for(int i = 0; i < deltas.length; ++i) {
      String firstDir = deltas[i].getProjectPath().segment(0);
      String extension = deltas[i].getFullPath().getFileExtension();
      boolean isBinDir = "bin".equals(firstDir);
      boolean isSrcDir = "src".equals(firstDir);
      boolean isClassFile = "class".equals(extension);
      boolean isJavaFile = "java".equals(extension);
      IFile file = deltas[i].getFile();
      boolean isInner = (file.getName().indexOf("$") != -1);
      
      if(((isBinDir && isClassFile) || (isSrcDir && isJavaFile)) == false)
        continue;
      
      // WE SHOULD ALSO LOOK WHETHER THE FILE IS NOT PRESENT ALSO AT THE MAIN
      // PROJECT, IF YES, IGNORE IT...
      if(deltaPresentInMainProject(deltas[i]))
        continue;
      
      // PROCESS PATH AND CLASS NAME
      IPath path = file.getFullPath();
      String className = path.removeFileExtension().segment(path.segmentCount() - 1);
      String packageName = path.removeFirstSegments(2).removeLastSegments(1).toString();
      if(packageName.startsWith("/"))
        packageName = packageName.substring(1);
      packageName = packageName.replace('/', '.');
      
      switch(deltas[i].getType()) {
      // PROCESS ADDED OBJECTS
        case IFileDelta.ADDED: {
          JavaObject jo = application.getJavaObject(packageName, className);
          if(jo != null) {
            operations.add(new ObjectInfo(isJavaFile, CHANGED, deltas[i], jo));
          }
          else {
            operations.add(new ObjectInfo(isJavaFile, NEW, deltas[i], packageName, className));
          }
        } break;
        // PROCESS REMOVED OBJECTS
        case IFileDelta.REMOVED: {
          JavaObject jo = application.getJavaObject(packageName, className);
          // IGNORE ALREADY REMOVED FILES
          if(jo == null)
            continue;
          //  IF THE MODIFICATION HAS BEEN PERFORMED AT SOME OTHER PLACE, IGNORE THIS
          if(isInner || isJavaFile) {
            operations.add(new ObjectInfo(isJavaFile, REM, deltas[i], jo));
          }
        } break;
        // PROCESS MOVED OBJECTS
        case IFileDelta.MOVED: {
          if(deltas[i].isMovingFromDifferentProject()) {
            // PROCESS ADDITION FROM COMPLETELY DIFFERENT PROJECT
            ObjectInfo info = new ObjectInfo(isJavaFile, NEW, deltas[i], packageName, className);
            // TODO: copy all design object properties from the different project to here
            operations.add(info);
          }
          else {
            // OR JUST UPDATE CLASSPATH THINGS
            try {
              String oldPackage = ProjectUtils.getPackageFromPath(deltas[i].getMovedFromPath());
              String oldClass = ProjectUtils.getClassFromPath(deltas[i].getMovedFromPath());
              JavaObject jo = application.getJavaObject(oldPackage, oldClass);
              if(jo == null) {
                operations.add(new ObjectInfo(isJavaFile, MOVE, deltas[i], jo, packageName, className));
              }
              else {
                // WE NEED TO CHANGE PACKAGE AND CLASS NAME HERE BECAUSE FURTHER
                // IT MIGHT MAKE SOME THREADING ERRORS.
                if(jo != null) {
                  jo.setPackage(packageName);
                  jo.setClassName(className);
                }
                operations.add(new ObjectInfo(isJavaFile, CHANGED, deltas[i]));
              }
            }
            catch(IdeException e) {
              VortexPlugin.log(e);
            }
          }
        } break;
        // PROCESS CHANGED OBJECTS
        case IFileDelta.CHANGED: {
          JavaObject jo = application.getJavaObject(packageName, className);
          if(jo == null) {
            // JUST FOR THE CASE SOMETHING GOES WRONG AT SOME POINT WE WANT TO HAVE THIS HERE
            operations.add(new ObjectInfo(isJavaFile, NEW, deltas[i], packageName, className));
          }
          else {
            // IF NO REFRESH FLAG IS NOT SET THEN NO UPLOAD SHOULD OCCUR, BECAUSE IT'S NOT
            // BLOCKED ANYMORE
            if(deltas[i].isMarkersChange() && deltas[i].isContentChange() == false)
              continue;
            
            operations.add(new ObjectInfo(isJavaFile, CHANGED, deltas[i], jo));
          }
        } break;
      }
    }
    
    if(operations.size() == 0)
      return;
    
    Iterator it = operations.iterator();
    while(it.hasNext()) {
      ObjectInfo oper = (ObjectInfo) it.next();
      System.out.println("EXTOP: " + oper.delta.toString());
    }
    
    Job j = new Job("Vortex Resource External Change Management") {
      protected IStatus run(IProgressMonitor monitor)
      {
        processAllChanges(operations, monitor);
        return Status.OK_STATUS;
      }
    };
    j.schedule();
  }
  
  /**
   * Checks if the file is present in the main project as well. If yes, returns
   * true.
   */
  private boolean deltaPresentInMainProject(IFileDelta delta)
  {
    IProject mainProject = pProject.getProject();
    IPath path = delta.getProjectPath();
    IResource res = mainProject.findMember(path);
    return res != null && res.getType() == IResource.FILE;
  }

  /**
   * This function does the actual upload to the server. This function should
   * run in one thread, and then pass other work for downloading to the other
   * threads. So the strategy is to at first add new objects here, then select
   * all uploads, and upload all stuff, and at the end remove some objects in
   * this thread.
   * 
   * <p>
   * But caution there. There must be synchronization for adding objects in this
   * thread since it might want to upload the items before the objects are being
   * added. And also when having CHANGE event, we should double check here if
   * the object is not already in the application. This is because this:
   * 
   * <pre>
   *   THREAD1         THREAD2
   *    ADD
   *    ADDING          CHANGE OF STILL NOT ADDED OBJECT
   *    ADDING
   */
  private void processAllChanges(List operations, IProgressMonitor monitor)
  {
    if(operations.size() == 0)
      return;

    // IF JAVA HASN'T STARTED YET, ABORT THIS...
    if(pProject.javaStarted() == false)
      return;
    
    Application application = pProject.getApplication();
    
    // NOW ADDING PART HERE:
    synchronized(this) {
      Iterator it = operations.iterator();
      while(it.hasNext()) {
        ObjectInfo info = (ObjectInfo) it.next();
        if(info.status == NEW) {
          try {
            String packageName = info.packageName;
            String className = info.className;
            JavaObject jo = application.getJavaObject(packageName, className);
            if(jo == null) {
              jo = ObjectsFactory.createJavaObject(packageName, className, DesignObject.TYPE_LIBRARY);
              jo.setParameter(PuakmaProject2Impl.PARAMETER_EXTERNAL_REFERENCE, info.delta.getProjectName());
              application.addObject(jo);
            }
            
            // AND ALSO SET THIS TO UPLOAD THE OBJECT
            info.jo = jo;
            info.status = CHANGED;
          }
          catch(Exception e) {
            VortexPlugin.log(e);
          }
        }
        else if(info.status == MOVE) {
          // ALSO SET TO UPLOAD THE FILE
          info.status = CHANGED;
        }
      }
    }
    
    // OK, SO NOW SHOULD BE EVERYTHING ADDED IN THE DATABASE, SO NOW CREATE THE
    // LIST OF THINGS TO BE UPLOADED
    Iterator it = operations.iterator();
    List<DownloadItem> uploads = new ArrayList<DownloadItem>();
    while(it.hasNext()) {
      ObjectInfo info = (ObjectInfo) it.next();
      if(info.status == CHANGED) {
        JavaObject jo = info.jo;
        DownloadItem di = new DownloadItem(jo, info.isSource, DownloadItem.TYPE_UPLOAD);
        uploads.add(di);
      }
    }
    
    // WE DON'T WANT TO BLOCK AT THE END OF PROCESS, WE WILL WAIT FOR FINISH
    // AT THE NEXT UPLOAD
    boolean blocking = false;
    pProject.getDownloadQueue().processBatch(uploads, blocking);
    
    // AND AT THE END PROCESS HERE REMOVALS
    it = operations.iterator();
    while(it.hasNext()) {
      ObjectInfo info = (ObjectInfo) it.next();
      if(info.status == REM) {
        JavaObject object = info.jo;
        boolean isInnerClass = info.delta.getFile().getName().indexOf('$') > 0;
        IFile srcFile = null;
        if(info.isSource == false) {
          IProject currentProject = info.delta.getProject();
          IPath p = new Path("src").append(object.getPackage().replace('.', '/'));
          p = p.append(object.getClassName() + ".java");
          srcFile = currentProject.getFile(p);
        }
        // WE CAN DELETE WHEN THE REMOVAL IS EITHER SOURCE FILE
        // OR INNER CLASS
        if(object != null && (info.isSource || (isInnerClass && srcFile.exists() == false))) {
          try {
            object.remove();
          }
          catch(Exception ex) {
            VortexPlugin.log("Cannot remove design object " + object.getName(), ex);
          }
        }
      }
    }
  }

  public IProject[] listProjects(IFileDeltaListVisitor visitor)
  {
    return projects;
  }
}
