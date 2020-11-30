/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Aug 16, 2005
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;

import puakma.coreide.objects2.Database;
import puakma.coreide.objects2.DatabaseConnection;
import puakma.coreide.objects2.Table;
import puakma.vortex.controls.TreeObject;
import puakma.vortex.controls.TreeParent;
import puakma.vortex.views.parts.ATVBaseNode;
import puakma.vortex.views.parts.ATVDatabaseTable;

/**
 * Parent node for all the database objects.
 *
 * @author Martin Novak
 */
public class ATVDbConnectionNode extends ATVParentNode implements PropertyChangeListener
{
  public static final String LOADING = "Loading...";
  private DatabaseConnection dbo;
  private boolean refreshed = false;
  private boolean refreshing = false;
  private ApplicationTreeViewController controller;
  private boolean hooked = false;
  private TreeViewer viewer;
  
  public ATVDbConnectionNode(ApplicationTreeViewController controller, DatabaseConnection object, TreeParent parent)
  {
    super(object.getName(), ApplicationTreeViewController.NODE_DATABASE_OBJECT, parent);
    
    this.controller = controller;
    this.dbo = object;
    this.viewer = controller.getViewer();
  }

  public DatabaseConnection getDatabaseObject()
  {
    return this.dbo;
  }

  public Object getAdapter(Class adapter)
  {
    if(adapter.equals(DatabaseConnection.class))
      return dbo;
    else
      return super.getAdapter(adapter);
  }

  public synchronized boolean isRefreshing()
  {
    return refreshing;
  }
  
  public synchronized boolean isRefreshed()
  {
    return refreshed;
  }
  
  public synchronized void setRefreshing(boolean b)
  {
    refreshing = b;
  }
  
  public synchronized void setRefreshed(boolean b)
  {
    refreshed = b;
  }

  /**
   * @return Database object belonging to this connection
   */
  public Database getDatabase()
  {
    return dbo.getDatabase();
  }

  public TreeObject[] getChildren()
  {
    throw new UnsupportedOperationException("Hahahahaa, this cannot happen!!!");
  }
  
  public Object[] getChildObjects()
  {
    synchronized(this) {
      if(refreshed) {
        Object[] objs = children.toArray();
        if(objs.length == 1 && objs[0] instanceof TreeObject
            && ((TreeObject)objs[0]).getName().equals(LOADING)) {
          children.clear();
          Database db = getDatabase();
          Table[] tables = db.listTables();
          List<ATVDatabaseTable> l = new ArrayList<ATVDatabaseTable>();
          for(int i = 0;i < tables.length; ++i) {
            ATVDatabaseTable node = new ATVDatabaseTable(null, viewer, tables[i]);
            l.add(node);
          }
          return l.toArray();
        }
        return objs;
      }
    }
    
    return new TreeObject[] { new TreeObject(LOADING, this) };
  }

  public synchronized void hookListener()
  {
    if(hooked)
      return;
    this.dbo.getDatabase().addListener(this);
    hooked = true;
  }

  public void propertyChange(final PropertyChangeEvent evt)
  {
    final String prop = evt.getPropertyName();
    Display.getDefault().asyncExec(new Runnable() {
      public void run()
      {
        if(Database.PROP_ADD_TABLE.equals(prop)) {
          TreeViewer viewer = getViewer();
          Table table = (Table) evt.getNewValue();
          ATVDatabaseTable node = new ATVDatabaseTable(null, viewer, table);
          viewer.add(this, node);
        }
        else if(Database.PROP_REMOVE_TABLE.equals(prop)) {
          TreeViewer viewer = getViewer();
          ATVBaseNode node = (ATVBaseNode) evt.getOldValue();
          viewer.remove(node);
        }
      }

    });
  }

  private TreeViewer getViewer()
  {
    return viewer;
  }
}
