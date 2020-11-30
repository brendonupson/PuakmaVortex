/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jan 31, 2006
 * 
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.dbschema;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.part.FileEditorInput;

import puakma.coreide.objects2.Database;
import puakma.coreide.objects2.DatabaseConnection;
import puakma.vortex.VortexPlugin;

/**
 * Editor input for the database schema editor. Note that this editor input is only for
 * opening database schema editor from the tornado application. For opening database
 * schema editor from the file use {@link FileEditorInput} class.
 * 
 * @author Martin Novak
 */
public class DatabaseSchemaEditorInput implements IEditorInput
{
  /**
   * Database on which we operate
   */
  private Database database;

  /**
   * Database connection - if it's non null, we want to upload configuration, and
   * synchronize data
   */
  private DatabaseConnection connection;

  /**
   * Configuration file
   */
  private IFile file;

  private IPath path;

  public DatabaseSchemaEditorInput(DatabaseConnection connection)
  {
    this.connection = connection;
  }

  public DatabaseSchemaEditorInput(Database database, IFile configFile)
  {
    this.database = database.makeWorkingCopy();
    this.file = configFile;
  }

  public DatabaseSchemaEditorInput(Database database, IPath path)
  {
    this.database = database.makeWorkingCopy();
    this.path = path;
  }

  public boolean equals(Object obj)
  {
    if(obj instanceof DatabaseSchemaEditorInput) {
      // WE NEED TO COMPARE ORIGINALS SINCE WE WANT TO NOT TO OPEN TWO WORKING COPIES OF
      // THE SAME DATABASE
      DatabaseSchemaEditorInput inp = (DatabaseSchemaEditorInput) obj;
      Database thisOriginal = getDatabase().isWorkingCopy() ? getDatabase().getOriginal() : getDatabase();
      Database thatOriginal = inp.getDatabase().isWorkingCopy() ? inp.getDatabase().getOriginal() : inp
          .getDatabase();
      if(thisOriginal == thatOriginal)
        return true;
    }

    return false;
  }

  /**
   * Returns a path of the configuration file. If the configuration file is not null,
   * returns configuration file from workspace, otherwise provided path which might not be
   * from workspace.
   * 
   * @return full path to configuration file
   */
  public IPath getPath()
  {
    IPath ret;
    if(file != null && (ret = file.getLocation()) != null)
      return ret;
    return path;
  }

  public boolean exists()
  {
    return false;
  }

  public ImageDescriptor getImageDescriptor()
  {
    return VortexPlugin.getDefault().getImageDescriptor("database.png");
  }

  public String getName()
  {
    if(connection != null)
      return connection.getName();
    else
      return "Database Editor";
  }

  public IPersistableElement getPersistable()
  {
    return null;
  }

  public String getToolTipText()
  {
    if(connection != null)
      return connection.getDatabaseUrl();
    return "Not connected to the Tornado server";
  }

  public Object getAdapter(Class adapter)
  {
    return null;
  }

  /**
   * Returns working copy of the assigned database.
   */
  public Database getDatabase()
  {
    if(database == null)
      database = connection.getDatabase();
    if(database.isWorkingCopy() == false)
      database = database.makeWorkingCopy();

    return database;
  }

  public IFile getFile()
  {
    return file;
  }

  public DatabaseConnection getConnection()
  {
    return connection;
  }
}
