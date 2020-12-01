/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Nov 1, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.views.navigator;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.actions.ActionGroup;

public class MultiActionGroup extends ActionGroup
{
  private IAction[] actions;

  private int currentSelection;

  private MenuItem[] items;
  
  private Image[] images;

  /**
   * Creates a new action group with a given set of actions.
   * @param actions the actions for this multi group
   * @param currentSelection decides which action is selected in the menu on
   *          start up. Denotes the location in the actions array of the current
   *          selected state. It cannot be null.
   */
  public MultiActionGroup(IAction[] actions, int currentSelection)
  {
    super();

    this.currentSelection = currentSelection;
    this.actions = actions;
    this.images = new Image[actions.length];
  }
  
  protected void updateCurrentSelection(int newSelection)
  {
    this.currentSelection = newSelection;
  }

  /**
   * Add the actions to the given menu manager.
   * @param viewMenu 
   */
  protected void addActions(IMenuManager viewMenu)
  {
    viewMenu.add(new Separator());
    items = new MenuItem[actions.length];

    for(int i = 0; i < actions.length; i++) {
      final int j = i;

      viewMenu.add(new ContributionItem() {
        public void fill(Menu menu, int index) {
          int style = SWT.CHECK;
          if((actions[j].getStyle() & IAction.AS_RADIO_BUTTON) != 0)
            style = SWT.RADIO;

          MenuItem mi = new MenuItem(menu, style, index);
          ImageDescriptor d = actions[j].getImageDescriptor();
          if(d != null) {
            Image img = d.createImage();
            images[j] = img;
            mi.setImage(img);
            items[j] = mi;
            mi.setText(actions[j].getText());
            mi.setSelection(currentSelection == j);
            mi.addDisposeListener(new DisposeListener() {
              public void widgetDisposed(DisposeEvent e) {
                images[j].dispose();
                images[j] = null;
              }
            });
          }
          mi.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
              if(currentSelection == j) {
                items[currentSelection].setSelection(true);
                return;
              }
              actions[j].run();

              // Update checked state
              items[currentSelection].setSelection(false);
              currentSelection = j;
              items[currentSelection].setSelection(true);
            }
          });
        }

        public boolean isDynamic()
        {
          return false;
        }
      });
    }
  }
}
