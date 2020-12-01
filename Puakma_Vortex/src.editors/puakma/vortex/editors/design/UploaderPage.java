/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Mar 3, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.design;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;

import puakma.coreide.PuakmaLibraryManager;
import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.ResourceObject;
import puakma.utils.MimeType;
import puakma.utils.MimeTypesResolver;
import puakma.utils.io.FileUtils;
import puakma.utils.lang.StringUtil;
import puakma.vortex.VortexPlugin;
import puakma.vortex.preferences.PreferenceConstants;
import puakma.vortex.project.ProjectUtils;
import puakma.vortex.swt.DialogBuilder2;
import puakma.vortex.swt.MultiPageEditorPart2;
import puakma.vortex.swt.NewLFEditorPage;

/**
 * @author Martin Novak
 */
public class UploaderPage extends NewLFEditorPage
{
  /**
   * Supported image types by SWT library
   */
  public static final String[] SWT_IMAGES = {
      "image/png", "image/jpeg", "image/jpg", "image/gif"
  };

  /**
   * Is the associated design object.
   */
  private DesignObject obj;
  
  /**
   * Progress bar showing the actual progress of the download/upload operation.
   */
  private ProgressMonitorPart monPart;
  
  /**
   * The image which will be shown in the preview control. Has to be disposed!
   */
  private Image image;
  
  /**
   * If true then the file is already on the disk, and we shouldn't replace it.
   */
  private boolean isOnDisk = false;
  
  /**
   * Is the parent editor.
   */
  private MultiPageEditorPart2 editor;
  
  /**
   * This control will display the image itself
   */
  private Label imageContainer;

  private Button openBtn;

  private Button uploadBtn;

  private Button importBtn;

  private Button exportBtn;
  
  private Label dataSizeLabel;
  private Label sourceSizeLabel;
  
  /**
   * Constructs the Composite.
   *
   * @param parent is the parent composite
   * @param editor is the parent Puakma editor
   */
  protected UploaderPage(Composite parent, MultiPageEditorPart2 editor, DesignObject object)
  {
    super(parent, editor);
    
    this.obj = object;
    this.editor = editor;

    boolean isSomeResource = (obj.getDesignType() == DesignObject.TYPE_RESOURCE ||
                              obj.getDesignType() == DesignObject.TYPE_DOCUMENTATION);
    boolean isSource = false;

    String title;
    if(obj.getDesignType() == DesignObject.TYPE_RESOURCE)
      title = "Resource Object - ";
    else if(obj.getDesignType() == DesignObject.TYPE_DOCUMENTATION)
      title = "Documentation Object - ";
    else if(obj.getDesignType() == DesignObject.TYPE_JAR_LIBRARY)
      title = "Library Object - ";
    else
      title = "Unknown Object - ";
    
    title += obj.getName();

    DialogBuilder2 builder = new DialogBuilder2(this);
    builder.createFormsLFComposite(title, false, 1);

    //--------------------------------------------------------------------------------------
    //    RESOURCE OBJECT INFORMATIONS
    //
    builder.createSection("General Information", null, 4);
    
    dataSizeLabel = builder.createTwoLabelRow("Design data size:",
                                           Long.toString(obj.getDesignSize(true)) + " bytes");
    sourceSizeLabel = builder.createTwoLabelRow("Design source size:",
                                             Long.toString(obj.getDesignSize(true)) + " bytes");
    builder.closeSection();

    //--------------------------------------------------------------------------------------
    //    STUFF AROUND OPENING/UPLOADING DESIGN OBJECT
    //
    builder.createSection("Opening - should be better named", null, 4);
    
    if(isSomeResource) {
      openBtn = builder.appendButton("Open", SWT.BORDER);
      openBtn.addSelectionListener(new SelectionListener() {
        public void widgetSelected(SelectionEvent e) {
          openFile();
        }
        public void widgetDefaultSelected(SelectionEvent e) {  }
      });
      // TODO: v1.1 will be smarter here [-;
      //b = toolkit.createButton(client, "Open with...", SWT.BORDER);
      uploadBtn = builder.appendButton("Upload", SWT.BORDER);
      uploadBtn.addSelectionListener(new SelectionListener() {
        public void widgetSelected(SelectionEvent e) {
          uploadFile(null);
        }
        public void widgetDefaultSelected(SelectionEvent e) {  }
      });
    }
    
    // importing and exporting is again for everyone
    //
    importBtn = builder.appendButton("Import...", SWT.BORDER);
    importBtn.addSelectionListener(new SelectionListener() {
      public void widgetSelected(SelectionEvent e) {
        importFile();
      }
      public void widgetDefaultSelected(SelectionEvent e) { }
    });
    exportBtn = builder.appendButton("Export...", SWT.BORDER);
    exportBtn.addSelectionListener(new SelectionListener() {
      public void widgetSelected(SelectionEvent e) {
        exportFile();
      }
      public void widgetDefaultSelected(SelectionEvent e) {  }
    });
    
    builder.closeSection();
    
//    uploadCheck = createCheckBoxRow(client, "Upload automatically on file change detection.");
    
    //--------------------------------------------------------------------------------------
    //    IMAGE PREVIEW
    //
    if(isSomeResource && StringUtil.arrayContainsString(SWT_IMAGES, obj.getContentType()) != -1) {
      builder.createSection("Preview", null, 1);
      imageContainer = builder.appendLabel("");
    }
    
    //--------------------------------------------------------------------------------------
    //    PROGRESS BAR
    //
    monPart = new ProgressMonitorPart(builder.getCurrentComposite(), null);
    monPart.setVisible(false);
    
    //--------------------------------------------------------------------------------------
    //    POST CONTROLS SETUP
    //
    
    // check out if the file should be downloaded
    //
    IFile iFile = ProjectUtils.getIFile(obj, isSource);
    if(iFile.exists()) {
      isOnDisk = true;
    }
    
    String ct = obj.getContentType();
    if(obj instanceof ResourceObject && ct != null && MimeTypesResolver.isImageType(ct)) {
      if(isOnDisk) {
        updateImage();
      }
      else {
        // Also if the design object is image file, we should download it, and show it
        //
        enableButtons(false);
        downloadFile(null, false);
      }
    }
  }
  
  /**
   * Updates preview image on the control if necessary
   */
  protected void updateImage()
  {
    // check if the image is suitable for our needs
    String ct = obj.getContentType();
    int index = StringUtil.arrayContainsString(SWT_IMAGES, ct);
    if(index == -1)
      return;

    // check if the image is ready on the filesystem
    boolean isSource = false;
    IFile file = ProjectUtils.getIFile(obj, isSource);
    if(file.exists() == false) {
      // set to the empty preview
      if(imageContainer != null)
        imageContainer.setImage(null);
      return;
    }
    
    if(imageContainer == null)
      return;
    
    // discard old image
    imageContainer.setImage(null);
    if(image != null) {
      image.dispose();
      image = null;
    }

    // load new image
    try {
      image = new Image(Display.getDefault(), file.getLocation().toOSString());
      imageContainer.setImage(image);
    }
    catch(Exception e) {
      imageContainer.setText("Cannot load image: " + e.getLocalizedMessage());
    }
  }

  private void openFile()
  {
    downloadFile(null, true);
  }
  
  private void downloadFile(final File file, final boolean openInEditor)
  {
    if(isOnDisk == false) {
      enableButtons(false);
      
      // schedule new job in eclipse
      String str;
      if(file == null)
        str = "Downloading object " + obj.getName();
      else
        str = "Exporting object " + obj.getName();
      
      monPart.setVisible(true);


      Job job = new Job(str) {
        protected IStatus run(IProgressMonitor monitor) {
          boolean isSource = false;
          try {
            monPart.beginTask("Begin download", 1);
            if(file == null) {
              ProjectUtils.downloadFile(obj, isSource);
            }
            else {
              obj.download(file, isSource);
            }
            
            Display.getDefault().asyncExec(new Runnable() {
              public void run() {                
                isOnDisk = true;
                if(openInEditor)
                  openFileInEditor();
                updateImage();
              }
            });
          }
          catch(Exception e) {
            VortexPlugin.log(e);
          }
          finally {
            monPart.done();
            
            Display.getDefault().asyncExec(new Runnable() {
              public void run() {
                enableButtons(true);
              }
            });
          }
          
          return Status.OK_STATUS;
        }
      };

      job.schedule();
    }
    else if(file == null) {
      openFileInEditor();
      enableButtons(true);
    }
    else if(file != null) {
      enableButtons(true);
      try {
        boolean isSource = false;
        IFile iFile = ProjectUtils.getIFile(obj, isSource);
        FileUtils.copyFile(file, iFile.getLocation().toFile());
      }
      catch(IOException e) {
        VortexPlugin.log(e);
        MessageDialog.openError(getShell(), "Problem with export of design object file",
                "Cannot copy the file to destination.\nReason:\n" + e.getLocalizedMessage());
      }
    }
  }

  private void uploadFile(final File file)
  {
    Job job = new Job("Uploading object - " + obj.getName()) {
      protected IStatus run(IProgressMonitor monitor)
      {
        boolean isSource = false;
        IFile fDest = ProjectUtils.getIFile(obj, isSource);
        // COPY FILE TO THE PROJECT'S DESTINATION
        FileInputStream is = null;
        
        try {
          monPart.beginTask("Uploading file", 3);
          is = new FileInputStream(file);
          ProjectUtils.setFileContent(fDest, is);
          monPart.worked(1);
          ProjectUtils.uploadFile(obj, isSource);
          monPart.worked(2);
        }
        catch(final Exception e) {
          Display.getDefault().asyncExec(new Runnable() {
            public void run() {
              MessageDialog.openError(getShell(), "Cannot upload file", "Cannot upload the file.\n" +
                                      "Reason:\n" + e.getLocalizedMessage());
            }
          });

          String msg = e instanceof IOException ? "Cannot copy file to the project destination"
              : "Cannot upload file to the server";
          IStatus ret = new Status(IStatus.ERROR, VortexPlugin.PLUGIN_ID, 0,
                                   msg, e);
          return ret;
        }
        finally {
          try { if(is != null) is.close(); } catch(IOException e) {  }
          
          monPart.done();
          Display.getDefault().asyncExec(new Runnable() {
            public void run() {
              monPart.setVisible(false);
              enableButtons(true);
            }
          });
        }

        return Status.OK_STATUS;
      }
    };

    enableButtons(false);
    monPart.setVisible(true);
    job.schedule();
  }

  /**
   * This function imports file. It displays user save dialog, and then downloads the file.
   */
  private void importFile()
  {
    IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
    String defDir = store.getString(PreferenceConstants.PREF_DOEDITOR_DEFAULT_DIR);
    
    FileDialog fd = new FileDialog(getShell(), SWT.OPEN | SWT.SINGLE);
    fd.setFilterPath(defDir);
    fd.setFilterExtensions(new String[] { "*.*" });
    fd.setFilterNames(new String[] {"All files"});
    fd.setText("Import file as the resource");
    if(fd.open() != null) {
      String fileName = fd.getFileName();
      store.setValue(PreferenceConstants.PREF_DOEDITOR_DEFAULT_DIR, fileName);
      IPath path = new Path(fd.getFilterPath()).append(fileName);
      File file = path.toFile();

      enableButtons(false);
      
      uploadFile(file);
    }
  }
  
  private void exportFile()
  {
    MimeTypesResolver resolver = new MimeTypesResolver();
    try { resolver.init(); }
    catch(IOException e) { VortexPlugin.log(e); }
    MimeType mime = resolver.getMimeTypeFromMime(obj.getContentType());
    String[] exts;
    if(obj.getDesignType() == DesignObject.TYPE_RESOURCE)
      exts = new String[] { "bin" };
    else if(obj.getDesignType() == DesignObject.TYPE_JAR_LIBRARY)
      exts = new String[] { "jar" };
    else
      exts = new String[] { "bin" };

    String ext;
    if(obj.getDesignType() == DesignObject.TYPE_JAR_LIBRARY)
      ext = "jar";
    else if(mime != null)
      ext = mime.getFirstExtension();
    else {
        ext = "bin";
    }
    
    IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
    String defDir = store.getString(PreferenceConstants.PREF_DOEDITOR_DEFAULT_DIR);
    
    FileDialog fd = new FileDialog(getShell(), SWT.SAVE);
    fd.setFileName(obj.getName() + "." + ext);
    fd.setFilterPath(defDir);
    fd.setFilterExtensions(exts);
    fd.setFilterNames(new String[] {"All files"});
    fd.setText("Export object");

    if(fd.open() != null) {
      String fileName = fd.getFileName();
      store.setValue(PreferenceConstants.PREF_DOEDITOR_DEFAULT_DIR, fileName);
      IPath path = new Path(fd.getFilterPath()).append(fileName);
      File file = path.toFile();

      enableButtons(false);
      downloadFile(file, false);
    }
  }

  /**
   * This enables/disables actions while we are downloading/uploading stuff.
   *
   * @param enable if true, then we enable buttons, so user can click on them.
   */
  private void enableButtons(boolean enable)
  {
    monPart.setVisible(enable);

    importBtn.setEnabled(enable);
    exportBtn.setEnabled(enable);
    if(openBtn != null)
      openBtn.setEnabled(enable);
    if(uploadBtn != null)
      uploadBtn.setEnabled(enable);
    
    updateImage();
  }
  
  /**
   * This function opens the file in the external editor. Which editor it will be depends
   * on Eclipse, not on us.
   */
  protected void openFileInEditor()
  {
    try {
      // open the file
      IWorkbenchPage page = editor.getSite().getPage();
      boolean isSource = false;
      IFile file = ProjectUtils.getIFile(obj, isSource);
      page.openEditor(new FileEditorInput(file), IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
    }
    catch(PartInitException e) {
      PuakmaLibraryManager.log(e);
      MessageDialog.openError(getShell(), "Error opening external editor", "Cannot open external editor for the action: "
                              + obj.getName() + "\nReason:\n" + e.getLocalizedMessage());
    }
  }

  public void doSave(IProgressMonitor monitor)
  {
    throw new IllegalStateException("Not implemented yet");
  }

  public void disposePage()
  {
    if(image != null) {
      image.dispose();
      image = null;
    }    
  }
}
