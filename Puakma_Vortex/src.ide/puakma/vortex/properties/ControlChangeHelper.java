/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package puakma.vortex.properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * ControlChangeHelper notifies the listner of text lifecycle events on behalf of
 * the widget(s) it listens to.
 * 
 * @author Anthony Hunter
 * @author Martin Novak &lt;mn@puakma.net&gt;
 */
public class ControlChangeHelper implements Listener
{
  private boolean nonUserChange;
  
  private ControlChangeListener listener;
  
  public ControlChangeHelper(ControlChangeListener listener)
  {
    this.listener = listener;
  }

  /**
   * Marks the start of a programmatic change to the widget contents. Clients
   * must call startNonUserChange() before directly setting the widget contents
   * to avoid unwanted lifecycle events.
   * 
   * @throws IllegalArgumentException if a programmatic change is already in
   *           progress.
   */
  public void startNonUserChange()
  {
    if(nonUserChange)
      throw new IllegalStateException("we already started a non user change");//$NON-NLS-1$
    nonUserChange = true;
  }

  /**
   * Clients who call startNonUserChange() should call finishNonUserChange() as
   * soon as possible after the change is done.
   * 
   * @throws IllegalArgumentException if no change is in progress.
   */
  public void finishNonUserChange()
  {
    if(!nonUserChange)
      throw new IllegalStateException("we are not in a non user change");//$NON-NLS-1$
    nonUserChange = false;
  }

  /**
   * Returns true if a programmatic change is in progress.
   */
  public boolean isNonUserChange()
  {
    return nonUserChange;
  }

  /**
   * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
   */
  public void handleEvent(Event event)
  {
    if(nonUserChange)
      return;
    
    switch(event.type) {
      case SWT.KeyDown:
        if(event.character == SWT.CR)
          listener.controlChanged((Control) event.widget);
      break;
      case SWT.FocusOut:
        listener.controlChanged((Control) event.widget);
      break;
      case SWT.Selection:
        listener.controlChanged((Control) event.widget);
      break;
    }
  }

  /**
   * Registers this helper with the given control to listen for events which
   * indicate that a change is in progress (or done).
   */
  public void startListeningTo(Control control)
  {
    // AT TEXT CONTROL DON'T LISTEN TO SELECTION
    if(control instanceof Text == false) {
      control.addListener(SWT.Selection, this);
      // TODO: SHOULD COMBO LISTEN TO MODIFY LISTENER AS WELL???
    }
    // BUT IF THE TEXT IS NOT MULTILINE, LISTEN TO KEYDOWN (ENTER)
    else {
      control.addListener(SWT.Modify, this);
      control.addListener(SWT.FocusOut, this);
      int style = control.getStyle();
      if((style & SWT.MULTI) != SWT.MULTI)
        control.addListener(SWT.KeyDown, this);
    }
  }

  /**
   * Unregisters this helper from a control previously passed to
   * startListeningTo() and/or startListeningForEnter().
   */
  public void stopListeningTo(Control control)
  {
    control.removeListener(SWT.FocusOut, this);
    control.removeListener(SWT.Modify, this);
    control.removeListener(SWT.KeyDown, this);
    control.removeListener(SWT.Selection, this);
  }
}
