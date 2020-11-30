/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Oct 3, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.controls;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import puakma.coreide.ServerManager;
import puakma.coreide.objects2.Application;

public class ConnectedAppsViewer
{
  private Combo combo;
  private ComboViewer viewer;

  public ConnectedAppsViewer(Composite parent)
  {
    combo = new Combo(parent, SWT.READ_ONLY);
    viewer = new ComboViewer(combo);
    
    viewer.setLabelProvider(new LabelProvider() {
      public String getText(Object element)
      {
        Application app = (Application) element;
        return app.getServer().getHost() + "/" + app.getFQName();
      }
    });
    
    Application[] apps = ServerManager.listConnectedApplications();
    for(int i = 0; i < apps.length; ++i) {
      viewer.add(apps[i]);
    }
    viewer.getCombo().select(0);
  }
  
  public int countApplications()
  {
    return combo.getItemCount();
  }
  
  public Combo getCombo()
  {
    return combo;
  }
  
  public ComboViewer getViewer()
  {
    return viewer;
  }

  /**
   * @return currently selected application or null if there is no application connected
   */
  public Application getSelectedApplication()
  {
    ComboViewer viewer = getViewer();
    IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
    Application app = (Application) selection.getFirstElement();
    return app;
  }
}
