/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    12/05/2006
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import puakma.utils.lang.ArrayUtils;

public class UIControlStatusSaver
{
  private Control[] controls = new Control[0];

  public void addControl(Control c)
  {
    controls = (Control[]) ArrayUtils.append(controls, c);
  }
  
  /**
   * Saves the status of the control.
   */
  public void saveStatus()
  {
    for(int i = 0; i < controls.length; ++i)
      saveControlStatus(controls[i]);
  }
  
  /**
   * Saves status for single control
   */
  private void saveControlStatus(Control control)
  {
    Object data = getControlCurrentData(control);
    control.setData(this.toString(), data);
  }

  /**
   * Gets the current data from the control.
   */
  private Object getControlCurrentData(Control control)
  {
    Object data = null;
    if(control instanceof Text) {
      Text t = (Text) control;
      data = t.getText();
    }
    else if(control instanceof Combo) {
      Combo c = (Combo) control;
      if((c.getStyle() & SWT.READ_ONLY) == SWT.READ_ONLY) {
        data = new Integer(c.getSelectionIndex());
      }
      else {
        data = c.getText();
      }
    }
    else
      throw new IllegalStateException("Unsupported control hooked: " + control);
    return data;
  }

  /**
   * Checks if some hooked control has been modified or not.
   * 
   * @return true if some control has been modified, false otherwise
   */
  public boolean isStatusModified()
  {
    for(int i = 0; i < controls.length; ++i) {
      Object current = getControlCurrentData(controls[i]);
      Object saved = controls[i].getData(this.toString());
      if(current.equals(saved) == false)
        return false;
    }
    
    return true;
  }
}
