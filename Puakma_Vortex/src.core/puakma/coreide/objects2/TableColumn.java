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
 * That class represents one table column.
 * 
 * @author Martin Novak
 */
public interface TableColumn extends ApplicationObject, DatabaseObject
{
  /**
   * This event is fired when type is changed
   */
  String PROP_TYPE = "type";

  /**
   * Fired when position of the column within table is changed. This is also fired in
   * Table.
   */
  String PROP_POSITION = "position";

  String PROP_DECIMAL_DIGITS = "floatDecimals";

  String PROP_TYPESIZE = "typeSize";

  String PROP_PK = "pk";

  String PROP_UNIQUE = "unique";

  String PROP_AUTO_INC = "autoInc";

  String PROP_ALLOWS_NULL = "allowsNull";

  String PROP_DEFAULT_VALUE = "defaultValue";

  String PROP_FK_DELETE_ACTION = "fkDeleteAction";

  /**
   * This event is send when we change the table which is being referenced by this column
   * as foreign key.
   */
  static final String PROP_REFERENCED_TABLE = "refTable";

  int CASCADE_ACTION_NONE = 0;

  int CASCADE_ACTION_DELETE = 1;

  int CASCADE_ACTION_SET_NULL = 2;

  int CASCADE_RESTRICT = 3;

  int CASCADE_ACTION_SET_DEFAULT = 4;

  /**
   * Gets the referenced table
   * 
   * @return {@link Table} object
   */
  Table getRefTable();

  /**
   * Sets referenced table. Note that this resets referenced column to the appropriate in
   * the fk table. It's because Vortex has to support both types - referencing to column,
   * and also referencing to table, and the column is being thinked as the same name
   * column. Also note that {@link VortexDatabaseException} is thrown when we cannot find
   * such column in the fk table.
   * 
   * @param table
   */
  void setRefTable(Table table);

  /**
   * Gets position of the column in the table. The position is zero indexed.
   * 
   * @return position of column in the table
   */
  int getPosition();

  void setType(String typeName);

  /**
   * Gets the database side data type. It might be for example: BLOB, VARCHAR, etc... Note
   * that this is without dimension.
   * 
   * @return String with data type
   */
  String getType();

  /**
   * Gets full type definition like FLOAT(7,2) or VARCHAR(255). This is usefull for sql
   * generation.
   * 
   * @return String with full type
   */
  String getFullTypeName();

  /**
   * @return type size for the actual type. Note that for floating point types you might
   *         appretiate also number of items after decimal point - determined by
   *         getFloatDecimals
   * 
   * @see TableColumn#getFloatDecimals()
   */
  int getTypeSize();

  void setTypeSize(int size);

  /**
   * @return number of decimals used in the float value.
   */
  int getFloatDecimals();

  void setFloatDecimals(int decimals);

  // /**
  // * Types defined in java.sqlTypes. Note that these values are determined in runtime
  // from type of
  // * the column.
  // *
  // * @return sql type as defined in java.sql.types
  // * @see java.sql.Types
  // */
  // int getSqlType();

  /**
   * @return Table in which is this column positioned.
   */
  Table getTable();

  TableColumn makeWorkingCopy();

  /**
   * Checks if this column is foreign key
   * 
   * @return true if this column is FK
   */
  boolean isFk();

  /**
   * Checks if the current column is PRIMARY KEY
   * 
   * @return true if the column is PK
   */
  boolean isPk();

  /**
   * Sets if this column is PRIMARY KEY or not.
   * 
   * @param isPk true if column should be PK
   */
  void setPk(boolean isPk);

  /**
   * Checks if the column has to have only unique values.
   * 
   * @return true if in the column can be only unique values
   */
  boolean isUnique();

  void setUnique(boolean uniq);

  /**
   * Checks if the column is auto incrementing its values. Note that different databases
   * support different schemas for creating auto incremental values. MySQL uses AUTO
   * INCREMENT, postgres sequences, etc...
   * 
   * @return true if this column is doing auto incrementation
   */
  boolean isAutoInc();

  /**
   * Sets if the column should autoincrement value on every new row added.
   * 
   * @param autoInc true if the column should be autoincrementing
   */
  void setAutoInc(boolean autoInc);

  /**
   * Commit at table columns has to be two phases. The first phase commits the column
   * content, and also relations which have their own working copies, and originals. The
   * second phase of commiting commits relations. This is because not all tables are on
   * the server at the first time when we call commit. Some of them are new tables, and
   * thus we don't have any information about the status of the model on the server.
   * 
   * @throws PuakmaCoreException
   * @throws IOException
   */
  void commitRelations() throws PuakmaCoreException, IOException;

  /**
   * Returns the default value of the column. So if new table is being created, this value
   * is supplied to unfilled column.
   */
  String getDefaultValue();

  void setDefaultValue(String defValue);

  /**
   * Returns true if the column allows NULL value in the content.
   */
  boolean allowsNull();

  void setAllowsNull(boolean allow);

  int getFkDeleteAction();

  void setFkDeleteAction(int fkDeleteAction);

  /**
   * Returns {@link FkConnection} object representing foreign key connection from this
   * column
   */
  FkConnection getRefConnection();
}
