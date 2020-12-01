/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Oct 4, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide.objects2;

import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import puakma.coreide.PuakmaCoreException;
import puakma.coreide.VortexDatabaseException;
import puakma.coreide.VortexMultiException;
import puakma.coreide.database.SQLCommandDescriptor;

/**
 * This interface represents the database.
 * 
 * <p>
 * Note that now all operations can be done only and only on tables, so eg if we think
 * that backing jdbc db is empty, we think about tables only.
 * 
 * <p>
 * Database can run in several modes
 * <ul>
 * <li>directly to puakma database</li>
 * <li>directly to the puakma database backed by immediate write to jdbc backend</li>
 * </ul>
 * Note that resolution of which mode are we running is determined on the client's side.
 * 
 * @author Martin Novak
 */
public interface Database
{
  public static final String PROP_ADD_TABLE = "database.addTable";

  /**
   * New value is null, old value is old table
   */
  public static final String PROP_REMOVE_TABLE = "database.removeTable";

  /**
   * Property for fireing change of custom data. Now is the same as in ServerObject.
   */
  public static final String PROP_DATA = ServerObject.PROP_DATA;

  public static final int DB_UNKNOWN = 0;

  public static final int DB_FIREBIRD = 2;

  public static final int DB_SYBASE = 3;

  public static final int DB_MYSQL = 4;

  public static final int ERROR_FAIL = 0;

  public static final int ERROR_CONTINUE = 1;

  public static final int ERROR_TRANSACTION = 2;

  // /**
  // * The database connection is being written to the puakma system database, and is
  // automatically
  // * backed to the jdbc database. This mode is set when database already exists, and
  // it's the same as
  // * in the puakma system db definition.
  // */
  // public static final int MODE_SYSDB_BACKED = 0;
  // public static final int MODE_SYSDB_ONLY = 1;

  /**
   * Returns the database connection for this database. It might return null for databases
   * which doesn't have assigned database connections or are not parts of an application.
   */
  DatabaseConnection getDatabaseConnection();

  /**
   * This adds database listener for listening to this database.
   * 
   * @param listener
   */
  public void addListener(PropertyChangeListener listener);

  /**
   * Removes listener from database. If there is no such listener, simply returns
   * 
   * @param listener is the database listener to remove from active ones
   */
  public void removeListener(PropertyChangeListener listener);

  /**
   * Checks if backing db is empty, so we can create a whole new database without cleaning
   * the backing database to create the actual schema.
   * 
   * @return true if backing database is empty
   */
  public boolean isBackingEmpty();

  /**
   * Lists all the table in the database.
   * 
   * @return array with all tables in the database
   */
  public Table[] listTables();

  /**
   * Gets table according to its name.
   * 
   * @param tableName is the name of wanted table
   * @return {@link Table} object or null if there is no table
   */
  public Table getTable(String tableName);

  /**
   * Gets the table by its identifier
   * 
   * @param tableId is the table's identifier
   * @return {@link Table} object or null if there is no such table
   */
  public Table getTable(long tableId);

  /**
   * This function creates, and executes query in the database.
   * 
   * @param sql is the sql command to execute
   * @return SQLQuery object with the result of the query
   * @throws VortexDatabaseException if database is not connected, or query is invalid,
   *             etc...
   */
  public SQLQuery executeQuery(String sql) throws VortexDatabaseException, IOException;

  /**
   * Executes batch of the sql commands.
   * 
   * @param sql is the sql command array to be executed
   * @param errorAction is one of the ERROR_FAIL, ERROR_CONTINUE, and ERROR_TRANSACTION
   * @return {@link SQLCommandDescriptor} array with the result
   */
  public SQLCommandDescriptor[] executeBatch(String[] sql, int errorAction) throws VortexDatabaseException,
                                                                           IOException;

  public void executeBatch(SQLCommandDescriptor[] descs, int errorAction) throws VortexDatabaseException,
                                                                         IOException;

  /**
   * This function checks if the database connection we are using is refreshed from the
   * server, or we should refresh it from the server.
   * 
   * @return true if all data should be ok, and there is no need to refresh database ddl
   */
  public boolean isOpen();

  /**
   * This refreshes the content of the database ddl.
   * 
   * @throws PuakmaCoreException
   */
  public void refresh() throws PuakmaCoreException;

  /**
   * This function adds some database object to the database. If database is working copy,
   * it's removed from memory only.
   * 
   * @param dbo is the database object
   * @throws PuakmaCoreException
   * @throws IOException
   */
  public void addObject(DatabaseObject dbo) throws PuakmaCoreException, IOException;

  /**
   * Removes an object from database. If database is working copy, it's removed from
   * memory only.
   * 
   * @param dbo is the database object to be removed
   * @throws PuakmaCoreException
   * @throws IOException
   */
  public void removeObject(DatabaseObject dbo) throws PuakmaCoreException, IOException;

  /**
   * This function commits database working copy to the server. Also note that this
   * function tries to commit as much as possible, so if one thing fails, it tries to
   * commit another thing unless all tables/other db objects has been tried.
   * 
   * @param monitor is the monitoring tool for progress of saving database to the server
   * @throws VortexMultiException lists all nested exception
   * @throws IllegalStateException if database is not working copy
   */
  public void commit(ProgressMonitor monitor) throws VortexMultiException;

  /**
   * Checks if the database is working copy
   * 
   * @return true if the database is working copy
   */
  public boolean isWorkingCopy();

  /**
   * Creates a new database which is a working copy of the current one.
   * 
   * @return Database object which is working copy of the current one
   */
  public Database makeWorkingCopy();

  /**
   * Returns original database when this database is working copy. If this database is not
   * working copy, returns null.
   */
  Database getOriginal();

  public Object getData(String key);

  public void setData(String key, Object value);

  /**
   * Adds the content of the input stream to database. The content has to contain xml.
   */
  String loadContentFromXml(InputStream is) throws SAXException, IOException, ParserConfigurationException;

  void saveContentToXml(OutputStream os, String customData) throws IOException;
}
