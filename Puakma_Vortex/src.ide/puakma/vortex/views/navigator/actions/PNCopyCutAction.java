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

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

import puakma.coreide.objects2.DatabaseObject;
import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.JavaObject;
import puakma.vortex.project.ProjectUtils;
import puakma.vortex.views.navigator.AdapterUtils;
import puakma.vortex.views.navigator.PuakmaResourceView;

public class PNCopyCutAction extends PNBaseAction implements ISelectionListener
{
  public static final String COPY_PATHS_DELIM = "#:#";
  public static final String COPY_PREFIX = "COPY" + COPY_PATHS_DELIM;
  public static final String CUT_PREFIX = "CUT#:#" + COPY_PATHS_DELIM;

  /**
   * If true then we are cutting the design object, otherwise copying
   */
  private boolean cut = false;
  
  public PNCopyCutAction(PuakmaResourceView view, boolean isCut)
  {
    super(isCut ? ActionFactory.CUT.getId() : ActionFactory.COPY.getId(), view);
    this.cut = isCut;
    setText(isCut ? "Cut" : "Copy");
    IWorkbench workbench = PlatformUI.getWorkbench();
    ImageDescriptor image;
    if(isCut)
      image = workbench.getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_CUT);
    else
      image = workbench.getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY);
    
    setImageDescriptor(image);
  }

  public void run()
  {
    String selectionString = createClipboardString();
    if(selectionString == null || selectionString.length() == 0)
      return;
    
    // AND COPY OBJECT IDENTIFIER TO THE CLIPBOARD
    Display d = Display.getDefault();
    Clipboard cb = new Clipboard(d);
    TextTransfer transfer = TextTransfer.getInstance();
    cb.setContents(new Object[] {selectionString}, new Transfer[] {transfer});
    
    PNPasteAction pasteAction = getView().getPasteAction();
    if(pasteAction != null)
      pasteAction.selectionChanged(getView(), getView().getSelection());
  }

  /**
   * Creates string for the clipboard. The output format is workspace-relative
   * file names for DesignObjects. The names are divided by "#:#" character.
   *
   * @return String containing IFile paths relative to workspace divided by "#:#"
   */
  private String createClipboardString()
  {
    String ret = cut ? CUT_PREFIX : COPY_PREFIX;
    IStructuredSelection selection = getView().getSelection();
    if(selection.size() == 0)
      return null;

    Iterator it = selection.iterator();
    SEL_LOOP: while(it.hasNext()) {
      Object o = it.next();
      DesignObject dobj = (DesignObject) AdapterUtils.getObject(o, DesignObject.class);
      // SO NOW HANDLE VARIOUS
      if(dobj != null) {
        IPath p = ProjectUtils.getFullFilePath(dobj, dobj instanceof JavaObject);
        String path = p.toString();
        path = path + COPY_PATHS_DELIM;
        ret += path;
        continue SEL_LOOP;
      }
      // TODO: check if the file is valid for our purposes
      IFile file = (IFile) AdapterUtils.getObject(o, IFile.class);
      if(file != null) {
          IPath p = file.getFullPath();
          String path = p.toString() + COPY_PATHS_DELIM;
          ret += path;
          continue SEL_LOOP;
      }
        
        // TODO: add support for this shit at first in the paste...
//        IFolder folder = (IFolder) a. getAdapter(IFolder.class);
//        if(folder != null) {
//          continue SEL_LOOP;
//        }
        
        // CANNOT ACCEPT ANYTHING ELSE
        return "";
    }
    
    return ret;
  }

  public boolean handleKeyEvent(KeyEvent event)
  {
    return false;
  }

  public void selectionChanged(IWorkbenchPart part, ISelection iselection)
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
    IStructuredSelection selection = getView().getSelection();
    Iterator it = selection.iterator();
    boolean enable = true;
    while(it.hasNext()) {
      Object o = it.next();
      DesignObject dob = (DesignObject) AdapterUtils.getObject(o, DesignObject.class);
      if(dob == null && AdapterUtils.getObject(o, IFile.class) == null) {
          enable = false;
      }
      else if(dob != null && dob instanceof DatabaseObject)
        enable = false;
      else if(dob != null)
          enable = true;
      else if(o instanceof IFile)
        enable = true;
      else if(o instanceof ICompilationUnit)
        enable = true;
      else
        enable = false;
      
      if(enable == false)
        return false;
    }
    if(selection.size() > 0)
        return enable;
    else
      return false;
  }
}
