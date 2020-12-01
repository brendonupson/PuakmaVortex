/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 9, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.wizard;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;

import puakma.coreide.ObjectsFactory;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.DesignObject;
import puakma.utils.MimeType;
import puakma.utils.MimeTypesResolver;
import puakma.vortex.VortexPlugin;
import puakma.vortex.WorkbenchUtils;
import puakma.vortex.project.ProjectUtils;


/**
 * This wizard creates a new page on the server.
 *
 * @author Martin Novak
 */
public class NewResourceWizard extends AbstractWizard
{
  private static final String WWW_UNKNOWN = "";
  
  public static final int TYPE_UPLOAD = 1;
  public static final int TYPE_NEW = 2;

  /**
   * Application in which we want to make new resource.
   */
  private Application application;
  
  /**
   * Main, and the only page in the wizard
   */
  private NewResourcePage mainPage;

  /**
   * Type of wizard - new empty file or upload file
   */
  private int type;
  
  /**
   * Creates a new wizard with the type of new empty resource.
   */
  public NewResourceWizard()
  {
    this(TYPE_NEW);
  }
  
  public NewResourceWizard(int type)
  {
    assert type == TYPE_UPLOAD || type == TYPE_NEW : WizardMessages.NewResourceWizard_Invalid_Resource_Wizard_Type;
    this.type = type;
  }

  public void init(Application connection)
  {
    this.application = connection;
  }
  
  public void addPages()
  {
    super.addPages();
    
    setWindowTitle("");
    setNeedsProgressMonitor(true);
    
    mainPage = new NewResourcePage(application, type);
    addPage(mainPage);
  }

  public boolean performFinish()
  {
    final String name = mainPage.getName();
    String fileName1 = null;
    if(type == TYPE_UPLOAD)
      fileName1 = mainPage.getFile();
    final String fileName = fileName1;
    final String comment = mainPage.getComment();
    if(application == null)
      application = mainPage.getApplication();
    
    try {
      getContainer().run(true, true, new IRunnableWithProgress() {
        public void run(IProgressMonitor monitor) throws InvocationTargetException,
            InterruptedException
        {
          doRun(monitor, name, fileName, comment);
        }
      });
      return true;
    }
    catch(InvocationTargetException e) {
      VortexPlugin.log(e);
    }
    catch(InterruptedException e) {
      VortexPlugin.log(e);
    }
    
    return false;
  }
  public void doRun(IProgressMonitor monitor, String name, String fileName, String comment) throws InvocationTargetException
  {
    monitor.beginTask("", 5);

    boolean isSource = false;
    final DesignObject obj = ObjectsFactory.createDesignObject(name, DesignObject.TYPE_RESOURCE);
    obj.setDescription(comment);
    
    if(type == TYPE_UPLOAD) {
      String ext = null;
      int index = fileName.lastIndexOf('.');
      if(index != -1 && index != fileName.length() - 1) {
        ext = fileName.substring(index + 1);
        MimeType mime = MimeTypesResolver.getDefault().getMimeTypeFromExt(ext);
        if(mime != null)
          obj.setContentType(mime.toString());
        else
          obj.setContentType(WWW_UNKNOWN);
      }
      else
        obj.setContentType(WWW_UNKNOWN);
    }
    else
      obj.setContentType("text/plain");

    try {
      // add object to application
      monitor.subTask(WizardMessages.NewResourceWizard_Run_Monitor_Add_Object);
      application.addObject(obj);
      monitor.worked(1);
      
      IFile iFile = ProjectUtils.getIFile(obj, isSource);
      
      // COPY THE FILE TO THE DESTINATION IN PROJECT
      if(type == TYPE_UPLOAD) {
        monitor.subTask("");
        File file = new File(fileName);
        ProjectUtils.copyFile(file, iFile, true);
        monitor.worked(1);
  
        // AND UPLOAD THE DESTINATION TO THE SERVER
        monitor.subTask("");
        ProjectUtils.uploadFile(obj, isSource);
      }
      else {
        iFile.create(new ByteArrayInputStream(new byte[0]), true, null);
      }
      
      Display.getDefault().asyncExec(new Runnable() {
        public void run() {
          WorkbenchUtils.openDesignObject(obj);
        }
      });
    }
    catch(Exception e) {
      throw new InvocationTargetException(e);
    }
    finally {
      monitor.done();
    }
  }
}
