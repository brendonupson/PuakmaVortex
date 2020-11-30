/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jan 18, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.application;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.DatabaseConnection;
import puakma.vortex.VortexPlugin;

/**
 * Editor input for application.
 *
 * @author Martin Novak
 */
public class ApplicationEditorInput implements IEditorInput
{
  private Application application;
  private DatabaseConnection dbo;
  private int openPageIndex = ApplicationEditor.PAGE_MAIN;
  
  public ApplicationEditorInput() {}
  
  public ApplicationEditorInput(int openPage)
  {
    this.openPageIndex = openPage;
  }
  
  public ApplicationEditorInput(DatabaseConnection dbo)
  {
    this.openPageIndex = ApplicationEditor.PAGE_DATABASES;
    this.dbo = dbo;
  }

  public boolean exists()
  {
    return true;
  }

  public ImageDescriptor getImageDescriptor()
  {
    return VortexPlugin.getImageDescriptor("puakma.gif");
  }

  public String getName()
  {
    return application.getGroup() + '/' + application.getName();
  }

  public IPersistableElement getPersistable()
  {
    return null;
  }

  public String getToolTipText()
  {
    // TODO: improve this
    return getName();
  }

  public Object getAdapter(Class adapter)
  {
    return null;
  }
  
  public Application getApplication()
  {
    return application;
  }
  
  public void setApplication(Application application)
  {
    this.application = application;
  }
  
  public boolean equals(Object obj) {
    if(obj instanceof ApplicationEditorInput) {
      ApplicationEditorInput input = (ApplicationEditorInput) obj;

      if(application.equals(input.application))
        return true;
    }
    return false;
  }

  public int hashCode() {
    return application.hashCode();
  }

  /**
   * Returns the index of the page which should be opened.
   * 
   * @return index of the page to open
   */
  public int getOpenPageIndex()
  {
    return openPageIndex;
  }

  public DatabaseConnection getDatabaseObject()
  {
    return dbo;
  }
}
