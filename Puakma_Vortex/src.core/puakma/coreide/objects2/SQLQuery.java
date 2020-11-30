/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Nov 8, 2005
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

import puakma.coreide.VortexDatabaseException;

/**
 * SQL query result. Note that this result is hold in the memory, and it's fetched at once
 * from the server. So please forget it as soon as you don't need it.
 *
 * @author Martin Novak
 */
public interface SQLQuery
{
  public String[] getWarnings();
  
  public String getSqlQuery();
  
  public boolean next();
  
  public boolean previous();
  
  /**
   * Moves cursor to the first row.
   */
  public boolean first();

  /**
   * Moves cursor to the last row.
   */
  public boolean last();
  
  /**
   * @return number of rows returned by this query.
   */
  public int countRows();
  
  public int columnCount();
  
  /**
   * Gets the name of the column at some index.
   *
   * @param colIndex is the index of column we want
   * @return the name of the column at some specific index
   */
  public String getColumnName(int colIndex);
  
  public int columnIndex(String columnName);
  
  /**
   * Gets the type of the column from the jdbc metadata. So for sybase's int
   * will mysql return integer, etc...
   * 
   * @param colIndex is the index of column
   * @return String with the type of the column
   */
  public String getColumnTypeName(int colIndex);
  
  /**
   * Gets the SQLType in int form. This should be the same for all databases.
   * 
   * @param colIndex is the index of the column
   * @return int with the type of the column
   */
  public int getColumnSqlType(int colIndex);
  
  public int getColumnSize(int colIndex);
  
  /**
   * Gets the number of float decimals in the column
   * @param colIndex is the index of the column in the query
   * @return number of decimals in the result
   * @throws VortexDatabaseException 
   */
  public int getColumnFloatDecimals(int colIndex);
  
  public boolean boolValue(int colIndex) throws VortexDatabaseException;
  
  public byte byteValue(int colIndex) throws VortexDatabaseException;
  
  public byte[] byteArrayValue(int colIndex) throws VortexDatabaseException;
  
  /**
   * Gets the int value of the column in the current row
   * 
   * @param colIndex is the index of the column to get value from
   * @return int value of the column in the current row
   * @throws VortexDatabaseException if the column index is bigger then number of columns,
   * or if conversion to int fails or if value of column is null
   */
  public int intValue(int colIndex) throws VortexDatabaseException;
  
  /**
   * Gets the long value of the column in the current row
   * 
   * @param colIndex is the index of the column to get value from
   * @return long value of the column in the current row
   * @throws VortexDatabaseException if the column index is bigger then number of columns,
   * or if conversion to long fails or if value of column is null
   */
  public long longValue(int colIndex) throws VortexDatabaseException;
  
  /**
   * Gets the float value of the column in the current row
   * 
   * @param colIndex is the index of the column to get value from
   * @return float value of the column in the current row
   * @throws VortexDatabaseException if the column index is bigger then number of columns,
   * or if conversion to float fails or if value of column is null
   */
  public float floatValue(int colIndex) throws VortexDatabaseException;
  
  /**
   * Gets the double value of the column in the current row
   * 
   * @param colIndex is the index of the column to get value from
   * @return double value of the column in the current row
   * @throws VortexDatabaseException if the column index is bigger then number of columns,
   * or if conversion to double fails or if value of column is null
   */
  public double doubleValue(int colIndex) throws VortexDatabaseException;
  
  /**
   * Gets the {@link java.lang.String} value of the column in the current row
   * 
   * @param colIndex is the index of the column to get value from
   * @return String value of the column in the current row or null if the current column is
   * null
   * @throws VortexDatabaseException if the column index is bigger then number of columns,
   * or if the data is binary, and we are not able to convert them to String
   */
  public String value(int colIndex) throws VortexDatabaseException;
  
  /**
   * Checks if some column is NULL or not
   * 
   * @param colIndex is the index of the column
   * @return true if the column is NULL, false otherwise
   * @throws VortexDatabaseException 
   */
  public boolean isNull(int colIndex) throws VortexDatabaseException;
  
  /**
   * This functions gets column value from row handle. This is speed up version for some
   * special content providers.
   *
   * @param rowHandle is the row handle
   * @param colIndex is the column index we want
   * @return String value of the column specified by the handle
   * @throws VortexDatabaseException
   */
  public String valueFromRowHandle(int rowHandle, int colIndex) throws VortexDatabaseException;
  
  /**
   * This functions gets byte array column value from row handle. This is speed up version
   * for some special content providers.
   *
   * @param rowHandle is the row handle
   * @param colIndex is the column index we want
   * @return byte array value of the column specified by the handle
   * @throws VortexDatabaseException 
   * @throws VortexDatabaseException
   */
  public byte[] byteArrayValueFromRowHandle(int rowHandle, int colIndex) throws VortexDatabaseException;
}
