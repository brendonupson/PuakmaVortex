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
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import puakma.vortex.editors.application.MultiEditorPage;

/**
 * This is a base class for pages in the multipage editors.
 *  
 * @author Martin Novak
 */
public abstract class NewLFEditorPage extends Composite implements MultiEditorPage
{
  private boolean dirty;
  protected MultiPageEditorPart2 editor;

  public NewLFEditorPage(Composite parent, MultiPageEditorPart2 editor)
  {
    super(parent, SWT.NULL);
    
    GridLayout gl = new GridLayout();
    gl.horizontalSpacing = gl.verticalSpacing = 0;
    gl.marginHeight = gl.marginWidth = 0;
    setLayout(gl);

    this.editor = editor;
  }

  /**
   * Checks if the page has some unsaved data.
   *
   * @return true if page has some unsaved data, otherwise false
   */
  public boolean isDirty()
  {
    return dirty;
  }
  
  public void setDirty(boolean dirty)
  {
    this.dirty = dirty;
    editor.updateDirty();
  }
  
  public abstract void doSave(IProgressMonitor monitor);
}
