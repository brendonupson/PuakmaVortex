/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Aug 3, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.views.navigator.actions;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

import puakma.coreide.PuakmaCoreException;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.JavaObject;
import puakma.vortex.IdeException;
import puakma.vortex.VortexPlugin;
import puakma.vortex.dialogs.RenameJavaObject;
import puakma.vortex.project.ProjectManager;
import puakma.vortex.project.ProjectUtils;
import puakma.vortex.project.PuakmaProject2;
import puakma.vortex.views.navigator.ContextMenuHandler;
import puakma.vortex.views.navigator.PuakmaResourceView;

public class PNPasteAction extends PNBaseAction
{
  public PNPasteAction(PuakmaResourceView view)
  {
    super(ActionFactory.PASTE.getId(), view);
    
    setText("Paste");
    ImageDescriptor image = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_PASTE);
    setImageDescriptor(image);
  }
  
  public void run()
  {
    if(qualifyForSelection() == false)
      return;

    String cbdata = getClipboardData();
    
    // PROTECT AGAINST MULTIPLE SELECTION WHEN PASTING
    IStructuredSelection selection = getView().getSelection();
    if(selection.size() != 1)
      return;
    Application app = ContextMenuHandler.getApplicationFromSelection(selection);
    // SHOULD NEVET HAPPEN!!!
    if(app == null)
      return;

    String[] paths = cbdata.split("#:#");
    if(paths.length > 1) {
      String pref = paths[0];
      boolean cutting = false;
      if("CUT".equals(paths[0]))
        cutting = true;
      for(int i = 1; i < paths.length; ++i) {
        IPath path = new Path(paths[i]);
        DesignObject dobj = ProjectUtils.getDesignObject(path);
        if(dobj != null) {
          pasteDesignObject(dobj, app, null, cutting, new NullProgressMonitor());
        }
        else {
          // HMMMMM, WHAT TO DO HERE???
        }
      }
    }
  }

  /**
   * This function executes copying the design object.
   * 
   * @param dobj is the source design object
   * @param destApp is the destination application
   * @param destFolder is the destination folder if we are pasting java objects
   * @param cutting if true then we should remove the old design object
   * @param monitor is the progress monitor controlling the stuff
   */
  private void pasteDesignObject(DesignObject dobj, final Application destApp,
                                 final IFolder destFolder, final boolean cutting, IProgressMonitor monitor)
  {
    // SIZE = UPLOAD TO SERVER + UPLOAD FILE + DOWNLOAD FILE
    int totalWork = 2 + (dobj instanceof JavaObject ? 0 : 1);
    monitor.beginTask("Copy object", totalWork);

    try {
      // COPY FILES
      if(dobj instanceof JavaObject) {
        checkJavaInitialized(destApp, dobj.getApplication(), monitor);
        
        //  COPY THE OBJECT IN THE MODEL
        final JavaObject source = (JavaObject) dobj;
        
        Shell shell = Display.getDefault().getActiveShell();
        String targetPackage = source.getPackage();
        if(destFolder != null) {
          IPath p = destFolder.getProjectRelativePath().removeFirstSegments(1);
          targetPackage = p.toString().replace('/', '.');
        }
        // THE CONFLICT IN PACKAGE.CLASS OR DESIGN OBJECT NAME
        boolean thereIsConflict = false;
        if(destApp.getDesignObject(source.getName()) != null)
          thereIsConflict = true;
        else if(destApp.getJavaObject(targetPackage, source.getClassName()) != null)
          thereIsConflict = true;
        final RenameJavaObject dlg = new RenameJavaObject(shell, RenameJavaObject.RenameType.PASTE, source, destApp, targetPackage);
        if(thereIsConflict == false || dlg.open() == Window.OK) {
          final String newClzName = thereIsConflict ? dlg.getClassName() : source.getClassName();
          final String newPackageName = thereIsConflict ? dlg.getPackageName() : targetPackage;
          final String newName = thereIsConflict ? dlg.getName() : source.getName();
          
          IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) throws CoreException
            {
              try {
                pasteJavaObject(source, destApp, cutting, newName, newClzName, newPackageName);
              }
              catch(Exception e) {
                VortexPlugin.log(e);
              }
            }
          };
          runnable.run(monitor);
        }
      }
      else {
        // COPY THE OBJECT IN THE MODEL
        DesignObject source = dobj;
        String newName = source.getName();
        DesignObject dupl = destApp.getDesignObject(source.getName());
        if(dupl != null) {
          InputDialog dlg = new InputDialog(getView().getSite().getShell(), "Design Object Name Confilict",
                                            "Type new name for \"" + source.getName() + "\"",
                                            source.getName() + "1", new IInputValidator() {
                                              public String isValid(String newText)
                                              {
                                                if(newText.length() == 0)
                                                  return "Design object name cannot be empty";
                                                if(destApp.getDesignObject(newText) != null)
                                                  return "Design object " + newText + " already exists.";
                                                return null;
                                              }
                                            });
          if(dlg.open() != Window.CANCEL)
            newName = dlg.getValue();
          else
            return;
        }

        pasteResource(source, destApp, cutting, newName);
      }
    }
    catch(Exception e) {
      VortexPlugin.log(e);
    }
    finally {
      monitor.done();
    }
  }

  /**
   * This function ensures that all the applications are already java downloaded.
   *
   * @param app1 is the first application
   * @param app2 is the second application
   * @throws IdeException if something goes wrong here
   * @throws CoreException 
   */
  private void checkJavaInitialized(Application app1, Application app2, IProgressMonitor monitor)
                                    throws IdeException, CoreException
  {
    PuakmaProject2 project1 = ProjectManager.getProject(app1);
    PuakmaProject2 project2 = ProjectManager.getProject(app2);
    if(project1.javaStarted() == false)
      project1.startJava(new SubProgressMonitor(monitor, 1));
    if(project2.javaStarted() == false)
      project2.startJava(new SubProgressMonitor(monitor, 1));
  }

  private void pasteJavaObject(final JavaObject source, final Application destApp,
                  boolean isCut, String newName, String newClassName, String newPackageName)
                  throws JavaModelException, CoreException, PuakmaCoreException, IOException
  {
    // CREATE DUPLICATION OF THE OBJECT IF THE JAVA OBJECT IS NOT SHARED CODE
    if(source.getDesignType() != DesignObject.TYPE_LIBRARY) {
      JavaObject jo = (JavaObject) source.copy();
      jo.setName(newName);
      jo.setClassName(newClassName);
      destApp.addObject(jo);
    }
    
    ICompilationUnit cu;
    // JAVA SOURCES SHOULD BE ALREADY ON DISC
    IFile srcFile = ProjectUtils.getIFile(source, true);
    IProject iDestProject = ProjectManager.getIProject(destApp);
    
    IFolder destSrcFolder = (IFolder) iDestProject.findMember(PuakmaProject2.DIR_SRC);
    IPackageFragmentRoot root = (IPackageFragmentRoot) JavaCore.create(destSrcFolder);
    IPackageFragment fragment = root.createPackageFragment(newPackageName, true, null);
    cu = JavaCore.createCompilationUnitFrom(srcFile);
    cu.copy(fragment, null, newClassName + ".java", true, null);

    // REMOVE OLD OBJECT IF NECESSARY
    if(isCut)
      source.remove();
  }

  private void pasteResource(DesignObject source, final Application destApp,
                             boolean isCut, String newName) throws PuakmaCoreException,
                                                           IdeException, IOException,
                                                           CoreException
  {
    assert destApp != null : "Cannot paste to null application";

    DesignObject dest = source.copy();
    if(newName != null && newName.length() > 0)
      dest.setName(newName);
    destApp.addObject(dest);

    // CHECK IF THE FILES ARE DOWNLOADED
    boolean isSource = false;
    IFile srcFile = ProjectUtils.getIFile(source, isSource);
    if(srcFile.exists() == false) {
      ProjectUtils.downloadFile(source, isSource);
    }
    // COPY THE FILE TO THE DESTINATION
    IPath destPath = ProjectUtils.getFullFilePath(dest, isSource);
    srcFile.copy(destPath, true, null);
    // UPLOAD THE FILE TO THE SERVER
    ProjectUtils.uploadFile(dest, isSource);
    // REMOVE OLD OBJECT IF NECESSARY
    if(isCut)
      source.remove();
  }

  /**
   * Gets textual data from clipboard.
   * @return String with some textual data from clipboard or null if there are
   *         no such data
   */
  private String getClipboardData()
  {
    Display d = Display.getDefault();
    Clipboard cb = new Clipboard(d);
    TextTransfer txtTransf = TextTransfer.getInstance();
    String data = (String) cb.getContents(txtTransf);
    return data;
  }

  public boolean handleKeyEvent(KeyEvent event)
  {
    return false;
  }

  public void selectionChanged(IWorkbenchPart part, ISelection selection)
  {
    setEnabled(qualifyForSelection());
  }
  
  public boolean qualifyForSelection()
  {
    IStructuredSelection selection = getView().getSelection();
    if(selection.size() == 0)
      return false;
    return true;
  }
  
  public boolean isEnabled()
  {
    String cbdata = getClipboardData();
    // EVERY VALID CLIPBOARD DATA HAS TO END WITH ":"
    if(cbdata == null || cbdata.endsWith("#:#") == false)
      return false;
    IStructuredSelection selection = getView().getSelection();
    if(selection != null && selection.size() > 0)
      return true;
    return false;
  }
}
