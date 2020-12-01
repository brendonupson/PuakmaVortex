/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Nov 7, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.query;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;

import puakma.coreide.objects2.DatabaseConnection;

public class QueryEditorInput implements IEditorInput, IStorageEditorInput
{
  private QueryStorage queryStorage = new QueryStorage();
  private DatabaseConnection connection;

  public QueryEditorInput(DatabaseConnection connection)
  {
    this.connection = connection;
  }
  
  public boolean exists()
  {
    return false;
  }

  public ImageDescriptor getImageDescriptor()
  {
    return null;
  }

  public String getName()
  {
    return connection.getName();
  }
  
  public boolean equals(Object obj)
  {
    if(obj instanceof QueryEditorInput) {
      QueryEditorInput input = (QueryEditorInput) obj;
      if(input.connection == connection)
        return true;
    }
    return false;
  }

  public IPersistableElement getPersistable()
  {
    return null;
  }

  public String getToolTipText()
  {
    return connection.getDatabaseUrl();
  }

  public Object getAdapter(Class adapter)
  {
    return null;
  }

  /**
   * @return DatabaseConnection object assiciated with this input
   */
  public DatabaseConnection getConnection()
  {
    return connection;
  }

  public IStorage getStorage() throws CoreException
  {
    return queryStorage;
  }
}
