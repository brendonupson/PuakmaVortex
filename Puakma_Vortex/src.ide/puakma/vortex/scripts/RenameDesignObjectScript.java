/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    20/07/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.scripts;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;

import puakma.coreide.ObjectsFactory;
import puakma.coreide.PuakmaCoreException;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.JavaObject;
import puakma.utils.ClassFileDecompiler;
import puakma.vortex.VortexPlugin;
import puakma.vortex.project.ProjectUtils;

public class RenameDesignObjectScript implements VortexScript
{
  private boolean refactorClass;
  private DesignObject dObject;
  private String newName;
  private String oldName;

  public RenameDesignObjectScript(DesignObject dobject)
  {
    this.dObject = dobject;
    this.oldName = dobject.getName();
  }

  /**
   * Sets if the java object should try to rename class as well as the java object.
   */
  public void setRefactorClass(boolean refactorClass)
  {
    this.refactorClass = refactorClass;
  }
  
  /**
   * Sets up new name of the {@link JavaObject}
   */
  protected void setRenameTo(String newName)
  {
    this.newName = newName;
  }

  public void run()
  {
    checkNewNameValid();
    if(dObject instanceof JavaObject && refactorClass)
      checkRefactorClass();
    
    try {
      renameDesignObject();
      
      if(dObject instanceof JavaObject && refactorClass)
        renameClass();
    }
    catch(Exception ex) {
      VortexPlugin.log(ex);
      MessageDialog.openError(null, "Error Renaming Design Object",
                              "Cannot rename design object.\nReason: "
                                  + ex.getLocalizedMessage());
    }
  }
  
  /**
   * Renames the design object.
   */
  private void renameDesignObject() throws PuakmaCoreException, IOException
  {
    DesignObject wc = dObject.makeWorkingCopy();
    wc.setName(newName);
    wc.commit();
  }
  
  /**
   * Renames class to match the class name.
   */
  private void renameClass() throws JavaModelException
  {
    IProgressMonitor monitor = new NullProgressMonitor();
    IFile file = ProjectUtils.getIFile(dObject, true);
    ICompilationUnit cu = (ICompilationUnit) JavaCore.create(file);
    String newCUName = newName + ".java";
    cu.rename(newCUName, true, monitor);
  }

  /**
   * Checks if the new name is valid name for the JavaObject.
   */
  protected void checkNewNameValid()
  {
    String error = getNewNameError();
    if(error != null)
      throw new IllegalStateException(error);
  }
  
  /**
   * Gets the error message for the new name. If there is no error, returns null.
   */
  protected String getNewNameError()
  {
    String error = ObjectsFactory.getDesignObjectNameError(newName);
    if(error != null)
      return error;
    
    Application app = dObject.getApplication();
    DesignObject dob = app.getDesignObject(newName);
    if(dob != null)
      return "Design object '" + newName + "' already exists in application";
    
    return null;
  }
  
  /**
   * Checks if the new name is also valid to refactor class.
   */
  protected void checkRefactorClass()
  {
    String error = getRefactorClassError();
    if(error != null)
      throw new IllegalStateException(error);
  }
  
  protected String getRefactorClassError()
  {
    String error = ClassFileDecompiler.checkClassName(newName);
    if(error != null)
      return error;
    
    Application app = dObject.getApplication();
    String pkg = ((JavaObject) dObject).getPackage();
    JavaObject jo = app.getJavaObject(pkg, newName);
    if(jo != null) {
      String fullName = pkg + "." + newName;
      return "Cannot refactor class because class '" + fullName
                                      + "' already exists in application.";
    }  
    
    return null;
  }

  /**
   * Returns {@link JavaObject} instance assigned to this script
   */
  protected JavaObject getJavaObject()
  {
    return (JavaObject) dObject;
  }
  
  protected DesignObject getDesignObject()
  {
    return dObject;
  }
  
  /**
   * Returns original name of the design object.
   */
  protected String getOldName()
  {
    return oldName;
  }
}
