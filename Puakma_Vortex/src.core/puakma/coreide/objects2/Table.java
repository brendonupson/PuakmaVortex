/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Sep 20, 2005
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

import java.io.IOException;
import java.util.List;

import puakma.coreide.PuakmaCoreException;
import puakma.coreide.VortexDatabaseException;

/**
 * This is abstraction for the database table.
 * 
 * <p>
 * Generic lifecycle:
 * <ul>
 * <li>NEW (in pmasystem and jdbc - basically it doesn't exist)</li>
 * <li>EXISTS - (valid in pmasystem, exists in jdbc) - basically if object is on the
 * server</li>
 * <li>REMOVED - (pmasystem and jdbc)
 * </ul>
 * all jdbc stuff is treated as exists/doesn't exist. That's it, we don't need more...
 * [[-;
 * 
 * @author Martin Novak
 */
public interface Table extends ApplicationObject, DatabaseObject
{
  /**
   * Event key for adding a column
   */
  public static final String PROP_COLUMN_ADD = "table.addColumn";

  /**
   * Event key for removing a column. Old value is the column which is removed
   */
  public static final String PROP_COLUMN_REMOVE = "table.removeColumn";

  /**
   * Key for event when some column moves its position
   */
  public static final String PROP_COLUMN_MOVE = "table.columnMoved";

  /**
   * Event when some of the connections pointing to this table are added or removed.
   */
  public static final String PROP_TARGET_CONNECTION_CHANGE = "targetConnectionChange";

  /**
   * Lists all columns in the table. Note that it lists columns in the order, so the first
   * column is also the first one who should be created in database.
   * 
   * @return TableColumn array with all the columns
   */
  public TableColumn[] listColumns();

  /**
   * Gets the column specified by its name
   * 
   * @param name is the name of wanted column
   * @return {@link TableColumn} object or null if there is no such column
   */
  public TableColumn getColumn(String name);

  /**
   * Gets the column by its identifier
   * 
   * @param id identifier of the column
   * @return {@link TableColumn} object or null if there is no such column
   */
  public TableColumn getColumn(long id);

  /**
   * @return Database under which the current node belongs.
   */
  public Database getDatabase();

  // --------------------------------------------------------------------------------------------------
  // FUNCTIONS FOR MANIPULATION WITH COLUMNS
  //

  /**
   * This function adds a new column to the table. If the table is not a working copy then
   * column is commited to the server, if it is working copy, column is added, and
   * commited later. This follows the rule that what is not working copy is immutable!
   * 
   * @param name is the name of the column
   * @param dataType is the type of the column - note that the type of the column is being
   *            checked for the current database, and exception is thrown if it doesn't
   *            match!
   * @return TableColumn object
   */
  public TableColumn addColumn(String name, String dataType) throws PuakmaCoreException, IOException;

  /**
   * This function adds a column to the table. If the table is not a working copy then
   * column is commited to the server, if it is working copy, column is added, and
   * commited later. This follows the rule that what is not working copy is immutable!
   * Also correct position is set to the column.
   * 
   * @param column is the column to add
   */
  public void addColumn(TableColumn column) throws PuakmaCoreException, IOException;

  /**
   * This function adds a column to the table. If the table is not a working copy then
   * column is commited to the server, if it is working copy, column is added, and
   * commited later. This follows the rule that what is not working copy is immutable!
   * Also correct position is set to the column.
   * 
   * @param column is the column to add
   */
  public void addColumn(int insertionIndex, TableColumn column) throws PuakmaCoreException, IOException;

  /**
   * This function removes column from the table. If the table is not a working copy then
   * column remove is commited to the server, if it is working copy, column is removed,
   * and commited later. This follows the rule that what is not working copy is immutable!
   * 
   * @param column is the column to remove
   * @throws VortexDatabaseException is thrown when something goes wrong
   */
  public void removeColumn(TableColumn column) throws VortexDatabaseException, IOException;

  /**
   * Chenges the position of some column. It moves column from the sourceIndex to
   * destination index.
   * 
   * @param sourceIndex is the index from which we move column
   * @param destinationIndex is the index of the destination
   * @throws IOException
   * @throws PuakmaCoreException
   */
  public void moveColumn(int sourceIndex, int destinationIndex) throws PuakmaCoreException, IOException;

  /**
   * Changes the position of the index in the table. It moves the column to the
   * destination index.
   * 
   * @param column is the column to move
   * @param destIndex is the destination index
   * @throws IOException
   * @throws PuakmaCoreException
   */
  public void moveColumn(TableColumn column, int destIndex) throws PuakmaCoreException, IOException;

  /**
   * Counts the number of columns in the table.
   * 
   * @return number of columns in the table
   */
  public int countColumns();

  /**
   * Checks dirty status on the table istself, and also on the columns bolonging to the
   * table
   */
  public boolean isDirty();

  /**
   * Commits relations on the table.
   * 
   * @throws PuakmaCoreException
   * @throws IOException
   */
  public void commitRelations() throws PuakmaCoreException, IOException;

  /**
   * Returns list of all foreign key connections which are pointing to this object.
   */
  List<FkConnection> getTargetConnections();
}
