/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    May 17, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.swt;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;

import puakma.utils.lang.ArrayUtils;
import puakma.vortex.editors.application.MultiEditorPage;

/**
 * @author Martin Novak
 */
public abstract class MultiPageEditorPart2 extends MultiPageEditorPart
{
  private MultiEditorPage[] pages;
  
  public MultiPageEditorPart2()
  {
    pages = new MultiEditorPage[0];
  }

  public boolean isDirty()
  {
    for(int i = 0; i < pages.length; ++i) {
      if(pages[i].isDirty())
        return true;
    }

    return false;
  }
  
  public void doSave(IProgressMonitor monitor)
  {
    int numPages = 0;
    for(int i = 0; i < pages.length; ++i) {
      if(pages[i].isDirty())
        numPages++;
    }

    monitor.beginTask("Saving...", numPages);

    try {
      for(int i = 0; i < pages.length; ++i) {
        if(pages[i].isDirty()) {
          pages[i].doSave(monitor);
          monitor.worked(1);
        }
      }
    }
    finally {
      monitor.done();
    }
  }

  
  public void doSaveAs()
  {
  }

  public boolean isSaveAsAllowed()
  {
    return false;
  }
  
  public int addPage(Control control)
  {
    if(control instanceof NewLFEditorPage)
      pages = (MultiEditorPage[]) ArrayUtils.append(pages, control);

    return super.addPage(control);
  }
  
  
  public void updateDirty()
  {
    firePropertyChange(PROP_DIRTY);
  }
  
  public void dispose()
  {
    for(int i = 0; i < pages.length; ++i) {
      pages[i].disposePage();
    }

    super.dispose();
  }

  public void setInput(IEditorInput input)
  {
    super.setInput(input);
    
//    for(int i = 0; i < pages.length; ++i) {
//      if(pages[i] instanceof IReusableEditor) {
//        ((IReusableEditor) pages[i]).setInput(input);
//      }
//    }
  }
}
