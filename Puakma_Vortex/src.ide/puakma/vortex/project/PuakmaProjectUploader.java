/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    16/10/2006
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
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import puakma.coreide.ObjectsFactory;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.JavaObject;
import puakma.utils.lang.StringUtil;
import puakma.vortex.IdeException;
import puakma.vortex.VortexPlugin;
import puakma.vortex.project.queue.DownloadItem;
import puakma.vortex.project.resource.IFileDelta;

public class PuakmaProjectUploader extends AbstractProjectFileVisitor
{
  private static final int NEW = 0;
  private static final int SEMI_NEW = 1;
  private static final int REM = 2;
  private static final int MOVE = 3;
  private static final int CHANGED = 4;

  private PuakmaProject2Impl project;
  
  public static class ObjectInfo
  {
    public boolean isSource;
    public int status;
    public IFileDelta delta;
    public ObjectInfo(boolean isSource, int status, IFileDelta delta) {
      this.isSource = isSource; this.status = status; this.delta = delta;
    }
  }
  
  public PuakmaProjectUploader(PuakmaProject2Impl project)
  {
    super();
   
    if(project == null)
      throw new IllegalArgumentException("Invalid Vortex project has been passed to the function");
    
    this.project = project;
    
    IProject[] p = {project.getProject()};
    setupProjects(p);
  }
  
  /**
   * Gets the {@link PuakmaProject2Impl} instance, should never return null.
   */
  public PuakmaProject2Impl getProject()
  {
    return project;
  }
  
  public void acceptAllDeltas(IFileDelta[] deltas)
  {
    PuakmaProject2Impl project = getProject();
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
      
      switch(deltas[i].getType()) {
      // PROCESS ADDED OBJECTS
        case IFileDelta.ADDED: {
          JavaObject jo = ProjectUtils.getJavaObject(project, file);
          if(jo != null) {
          // IF THIS IS NOT REFRESH EVENT, CONTINUE WITH PROCESSING
            if(hasAddRefreshFlag(jo)) {
              if(isJavaFile == false)
                removeAddRefreshFlag(jo);
              continue;
            }
            operations.add(new ObjectInfo(isJavaFile, CHANGED, deltas[i]));
          }
          else {
            operations.add(new ObjectInfo(isJavaFile, NEW, deltas[i]));
          }
        } break;
        // PROCESS REMOVED OBJECTS
        case IFileDelta.REMOVED: {
          JavaObject obj = ProjectUtils.getJavaObject(project, file);
          // IGNORE ALREADY REMOVED FILES
          if(obj == null)
            continue;
          //  IF THE MODIFICATION HAS BEEN PERFORMED AT SOME OTHER PLACE, IGNORE THIS
          if(hasRemRefreshFlag(obj) || hasNormalRemChangeFlag(obj))
            continue;

          if(isInner || isJavaFile) {
            // SET REMOVAL STATUS ON OBJECT TO EXTERNAL CHANGE
            setRemExternalChange(obj);
            operations.add(new ObjectInfo(isJavaFile, REM, deltas[i]));
          }
        } break;
        // PROCESS MOVED OBJECTS
        case IFileDelta.MOVED: {
          if(deltas[i].isMovingFromDifferentProject()) {
            // PROCESS ADDITION FROM COMPLETELY DIFFERENT PROJECT
            ObjectInfo info = new ObjectInfo(isJavaFile, NEW, deltas[i]);
            // TODO: copy all design object properties from the different project to here
            operations.add(info);
          }
          else {
            // OR JUST UPDATE CLASSPATH THINGS
            try {
              Application application = ProjectUtils.getApplication(deltas[i].getFile());
              String oldPackage = ProjectUtils.getPackageFromPath(deltas[i].getMovedFromPath());
              String oldClass = ProjectUtils.getClassFromPath(deltas[i].getMovedFromPath());
              JavaObject jo = application.getJavaObject(oldPackage, oldClass);
              if(jo == null) {
                operations.add(new ObjectInfo(isJavaFile, MOVE, deltas[i]));
              }
              else {
                boolean isAutoBuilding = ResourcesPlugin.getWorkspace().getDescription().isAutoBuilding();
                // IGNORE REFRESH
                if(hasAddRefreshFlag(jo)) {
                  // THIS EVENT OCCURS ONLY WITH SOURCE, SO AGAIN, REMOVE THIS FLAG ALWAYS
                  removeAddRefreshFlag(jo);
                  if(isAutoBuilding)
                    continue;
                }
                String packageName = ProjectUtils.getPackageFromResource(deltas[i].getFile());
                String className = ProjectUtils.getClassFromResource(deltas[i].getFile());
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
          JavaObject jo = ProjectUtils.getJavaObject(deltas[i].getFile());
          if(jo == null) {
            // JUST FOR THE CASE SOMETHING GOES WRONG AT SOME POINT WE WANT TO HAVE THIS HERE
            operations.add(new ObjectInfo(isJavaFile, NEW, deltas[i]));
          }
          else {
            if(hasChangeRefreshFlag(jo)) {
              // IF MARKERS CHANGED, UPLOAD SOURCE
              if(isJavaFile && deltas[i].isMarkersChange())
                removeChangeRefreshFlag(jo);
              else if(isJavaFile == false) {
                removeChangeRefreshFlag(jo);
                continue;
              }
            }
            
            // IF NO REFRESH FLAG IS NOT SET THEN NO UPLOAD SHOULD OCCUR, BECAUSE IT'S NOT
            // BLOCKED ANYMORE
            else if(deltas[i].isMarkersChange() && deltas[i].isContentChange() == false)
              continue;
            
            operations.add(new ObjectInfo(isJavaFile, CHANGED, deltas[i]));
          }
        } break;
      }
    }
    
    if(operations.size() == 0)
      return;
    
    Iterator it = operations.iterator();
    while(it.hasNext()) {
      ObjectInfo oper = (ObjectInfo) it.next();
      System.out.println("OP: " + oper.delta.toString());
    }
    
    Job j = new Job("Vortex Resource Change Management") {
      protected IStatus run(IProgressMonitor monitor)
      {
        processAllChanges(operations, monitor);
        return Status.OK_STATUS;
      }
    };
    j.schedule();
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
    
    // AT FIRST START JAVA
    try {
      getProject().startJava(monitor);
    }
    catch(Exception e) {
      VortexPlugin.log(e);
    }
    
    Application application = project.getApplication();
    
    // NOW ADDING PART HERE:
    synchronized(this) {
      Iterator it = operations.iterator();
      while(it.hasNext()) {
        ObjectInfo info = (ObjectInfo) it.next();
        if(info.status == NEW) {
          try {
            String packageName = ProjectUtils.getPackageFromResource(info.delta.getFile());
            String className = ProjectUtils.getClassFromResource(info.delta.getFile());
            if(application.getJavaObject(packageName, className) == null) {
              JavaObject jo = ObjectsFactory.createJavaObject(packageName, className, DesignObject.TYPE_LIBRARY);
              application.addObject(jo);
            }
            // AND ALSO SET THIS TO UPLOAD THE OBJECT
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
        try {
          String packageName = ProjectUtils.getPackageFromResource(info.delta.getFile());
          String className = ProjectUtils.getClassFromResource(info.delta.getFile());
          JavaObject jo = application.getJavaObject(packageName, className);
          if(jo != null) {
            DownloadItem di = new DownloadItem(jo, info.isSource, DownloadItem.TYPE_UPLOAD);
            uploads.add(di);
          }
        }
        catch(IdeException e) {
          VortexPlugin.log(e);
        }
        finally {}
      }
    }
    
    // WE DON'T WANT TO BLOCK AT THE END OF PROCESS, WE WILL WAIT FOR FINISH
    // AT THE NEXT UPLOAD
    boolean blocking = false;
    getProject().getDownloadQueue().processBatch(uploads, blocking);
    
    // AND AT THE END PROCESS HERE REMOVALS
    it = operations.iterator();
    while(it.hasNext()) {
      ObjectInfo info = (ObjectInfo) it.next();
      if(info.status == REM) {
        JavaObject object = ProjectUtils.getJavaObject(info.delta.getFile());
        boolean isInnerClass = info.delta.getFile().getName().indexOf('$') > 0;
        IFile srcFile = info.isSource == false ? ProjectUtils.getIFile(object, true) : null;
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

  private boolean hasAddRefreshFlag(JavaObject jo)
  {
    String value = (String) jo.getData(PuakmaProject2Impl.KEY_ADD_REFRESH);
    if(StringUtil.compareStrings(PuakmaProject2Impl.OP_REFRESH, value))
      return true;
    return false;
  }
  
  private boolean hasChangeRefreshFlag(JavaObject jo)
  {
    String value = (String) jo.getData(PuakmaProject2Impl.KEY_CHANGE_REFRESH);
    if(StringUtil.compareStrings(PuakmaProject2Impl.OP_REFRESH, value))
      return true;
    return false;
  }
  
  private boolean hasRemRefreshFlag(JavaObject jo)
  {
    String value = (String) jo.getData(PuakmaProject2Impl.KEY_REMOVE_REFRESH);
    if(StringUtil.compareStrings(PuakmaProject2Impl.OP_REFRESH, value))
      return true;
    return false;
  }
  
  private boolean hasNormalAddChangeFlag(JavaObject jo)
  {
    String value = (String) jo.getData(PuakmaProject2Impl.KEY_ADD_REFRESH);
    if(StringUtil.compareStrings(PuakmaProject2Impl.OP_NORMALY, value))
      return true;
    return false;
  }
  
  private boolean hasNormalChangeChangeFlag(JavaObject jo)
  {
    String value = (String) jo.getData(PuakmaProject2Impl.KEY_CHANGE_REFRESH);
    if(StringUtil.compareStrings(PuakmaProject2Impl.OP_NORMALY, value))
      return true;
    return false;
  }
  
  private boolean hasNormalRemChangeFlag(JavaObject jo)
  {
    String value = (String) jo.getData(PuakmaProject2Impl.KEY_REMOVE_REFRESH);
    if(StringUtil.compareStrings(PuakmaProject2Impl.OP_NORMALY, value))
      return true;
    return false;
  }
  
  private void removeAddRefreshFlag(JavaObject jo)
  {
    jo.setData(PuakmaProject2Impl.KEY_ADD_REFRESH, null);
  }
  
  private void removeChangeRefreshFlag(JavaObject jo)
  {
    jo.setData(PuakmaProject2Impl.KEY_CHANGE_REFRESH, null);
  }
  
  private void setRemExternalChange(JavaObject jo)
  {
    jo.setData(PuakmaProject2Impl.KEY_REMOVE_REFRESH, PuakmaProject2Impl.OP_EXTERNAL_FILE_CHANGE);
  }
}
