/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    03/05/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import puakma.utils.lang.ArrayUtils;

/**
 * Base class for all properties page controllers. It adds some support for
 * modifying pages fields.
 * 
 * <p>
 * TODO: implement this using {@link UIControlStatusSaver}
 * 
 * @author Martin Novak
 */
public abstract class BasePropertiesPageController implements PropertiesPage, Listener
{
  private Control[] hookedModifyControls = new Control[0];
  private Control[] hookedSelectionControls = hookedModifyControls;
  private TabbedPropertiesController controller;
  private boolean dirty;
  // TODO: implement here or in some super class automatic properties setup
  
  protected void hookControl(Text c)
  {
    c.addListener(SWT.Modify, this);
    hookedModifyControls = (Control[]) ArrayUtils.append(hookedModifyControls, c);
  }
  
  protected void hookControl(Button b)
  {
    b.addListener(SWT.Selection, this);
    hookedSelectionControls = (Control[]) ArrayUtils.append(hookedSelectionControls, b);
  }
  
  protected void hookControl(Combo c)
  {
    if((c.getStyle() & SWT.READ_ONLY) == SWT.READ_ONLY) {
      c.addListener(SWT.Selection, this);
      hookedSelectionControls = (Control[]) ArrayUtils.append(hookedSelectionControls, c);
    }
    else {
      c.addListener(SWT.Modify, this);
      hookedModifyControls = (Control[]) ArrayUtils.append(hookedModifyControls, c);
    }
  }

  public void dispose()
  {
    for(int i = 0; i < hookedModifyControls.length; ++i) {
      hookedModifyControls[i].removeListener(SWT.Modify, this);
    }
    
    for(int i = 0; i < hookedSelectionControls.length; ++i) {
      hookedSelectionControls[i].removeListener(SWT.Selection, this);
    }
  }

  public void init(TabbedPropertiesController controller)
  {
    this.controller = controller;
  }
  
  public TabbedPropertiesController getController()
  {
    return controller;
  }
  
  public boolean isDirty()
  {
    return dirty;
  }
  
  /**
   * This should be called whenever user saves the data. It must be explicitly
   * called by clients.
   */
  protected void markSaved()
  {
    this.dirty = false;
  }

  public void handleEvent(Event event)
  {
    this.dirty = true;
    if(controller != null)
      controller.fireDirtyChange();
  }
}
