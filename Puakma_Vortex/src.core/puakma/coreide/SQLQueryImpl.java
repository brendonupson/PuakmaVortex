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
package puakma.coreide;

import puakma.coreide.database.DatabaseQueryResultBean;
import puakma.coreide.database.DatabaseQueryResultBean.Column;
import puakma.coreide.database.DatabaseQueryResultBean.RSColumnMetadata;
import puakma.coreide.database.DatabaseQueryResultBean.Row;
import puakma.coreide.designer.DatabaseDesigner;
import puakma.coreide.designer.PuakmaXmlCodes;
import puakma.coreide.objects2.DatabaseConnection;
import puakma.coreide.objects2.SQLQuery;

public class SQLQueryImpl implements SQLQuery
{
  /**
   * Actual row index.
   */
  private int rowIndex = -1;
  
  /**
   * Is the pointer to the application's database connection.
   */
  private DatabaseConnection dbConnection;
  
  private DatabaseQueryResultBean.Row[] rows;
  private RSColumnMetadata[] metadata;
  private String sql;
  private DatabaseQueryResultBean.Row[] generatedKeys;

  private RSColumnMetadata[] generatedKeysMetadata;
  
  public SQLQueryImpl(DatabaseConnection dbConnection)
  {
    this.dbConnection = dbConnection;
  }
  
  /**
   * Returns the proper database client.
   *
   * @return @{link puakma.coreide.designer.DatabaseDesigner} object
   */
  public DatabaseDesigner getDatabaseClient()
  {
    return ((DatabaseConnectionImpl) dbConnection).getDatabaseClient();
  }

  public String getSqlQuery()
  {
    synchronized(this) {
      return sql;
    }
  }

  public boolean next()
  {
    synchronized(this) {
      if(rowIndex >= rows.length - 1)
        return false;
      rowIndex++;
      return true;
    }
  }

  public boolean previous()
  {
    synchronized(this) {
      if(rowIndex <= 0)
        return false;
      rowIndex--;
      return true;
    }
  }

  public boolean first()
  {
    synchronized(this) {
      if(rows.length <= 0)
        return false;
      rowIndex = 0;
      return true;
    }
  }

  public boolean last()
  {
    synchronized(this) {
      if(rows.length <= 0)
        return false;
      rowIndex = rows.length - 1;
      return true;
    }
  }

  public int countRows()
  {
    synchronized(this) {
      return rows.length;
    }
  }

  public int columnCount()
  {
    synchronized(this) {
      if(metadata == null)
        return 0;
      return metadata.length;
    }
  }

  public String getColumnName(int colIndex)
  {
    synchronized(this) {
      return metadata[colIndex].name;
    }
  }

  public int columnIndex(String columnName)
  {
    synchronized(this) {
      // TREAT COLUMN NAMES CASE INSENSITIVE FOR NOW
      // TODO: CHANGE COLUMN CASE CHECKING AGAINST THE DATABASE
      for(int i = 0; i < metadata.length; ++i) {
        if(metadata[i].name.equalsIgnoreCase(columnName))
          return i;
      }
    }
    
    return -1;
  }

  public boolean boolValue(int colIndex) throws VortexDatabaseException
  {
    synchronized(this) {
      return Boolean.getBoolean(value(colIndex));
    }
  }

  public byte byteValue(int colIndex) throws VortexDatabaseException
  {
    synchronized(this) {
      return Byte.parseByte(value(colIndex));
    }
  }

  public byte[] byteArrayValue(int colIndex) throws VortexDatabaseException
  {
    synchronized(this) {
      checkColumn(colIndex);
      checkDataAvailable();
      
      Row row = rows[rowIndex];
      Column col = row.cols[colIndex];
      if(col.type == PuakmaXmlCodes.COLT_BYTEARRAY)
        return col.b;
      
      throw throwInvalidType(colIndex, "byte[]", col.type);
    }
  }

  /**
   * Throws an exception with an appropriate type error message.
   *
   * @param colIndex is the column index
   * @param wantType
   * @param hasType
   */
  private VortexDatabaseException throwInvalidType(int colIndex, String wantType, int hasType)
  {
    String[] typeNames = new String[] {
        "Unknown", "boolean", "byte", "char", "short", "int", "long", "float", "double",
        "String", "byte array",
    };
    String hasTypeName = typeNames[hasType];
    return new VortexDatabaseException("Invalid type in column " + colIndex + ": wanted "
        + wantType + " but the type was: " + hasTypeName);
  }

  public int intValue(int colIndex) throws VortexDatabaseException
  {
    synchronized(this) {
      return Integer.parseInt(value(colIndex));
    }
  }

  public long longValue(int colIndex) throws VortexDatabaseException
  {
    synchronized(this) {
      return Long.parseLong(value(colIndex));
    }
  }

  public float floatValue(int colIndex) throws VortexDatabaseException
  {
    synchronized(this) {
      return Float.parseFloat(value(colIndex));
    }
  }

  public double doubleValue(int colIndex) throws VortexDatabaseException
  {
    synchronized(this) {
      return Double.parseDouble(value(colIndex));
    }
  }

  public String value(int colIndex) throws VortexDatabaseException
  {
    synchronized(this) {
      checkColumn(colIndex);
      checkDataAvailable();
      
      Row row = rows[rowIndex];
      Column col = row.cols[colIndex];
      if(col.type == PuakmaXmlCodes.COLT_BYTEARRAY)
        return new String(col.b);
      else
        return col.o.toString();
    }
  }

  public boolean isNull(int colIndex) throws VortexDatabaseException
  {
    synchronized(this) {
      checkColumn(colIndex);
      checkDataAvailable();
      
      Row row = rows[rowIndex];
      Column col = row.cols[colIndex];
      return col.isNull;
    }
  }
  
  /**
   * Checks if the data can be read from the current row. If not, throws an exception.
   */
  private void checkDataAvailable()
  {
    if(rowIndex == -1)
      throw new IllegalStateException("Cursor is before data");
    if(rowIndex == rows.length)
      throw new IllegalStateException("Cursos is beyond data");
  }
  
  /**
   * This functions checks if the column index is valid. If not, throws IllegalStateException.
   * 
   * @param colIndex is the column index
   */
  private void checkColumn(int colIndex)
  {
    if(colIndex >= metadata.length)
      throw new IndexOutOfBoundsException("Invalid column index - " + colIndex);
  }

  public String[] getWarnings()
  {
    throw new IllegalStateException("Not implemented yet");
  }

  public void internalSetup(String sql, DatabaseQueryResultBean result)
  {
    this.sql = sql;
    if(result.resultSet != null) {
      this.rows = result.resultSet.rows;
      this.metadata = result.resultSet.columnMetadata;
    }
    else {
      // TODO: emulate here behavior from generated keys
      this.rows = new Row[0];
    }
    if(result.generatedKeys != null) {
      this.generatedKeys = result.generatedKeys.rows;
      this.generatedKeysMetadata = result.generatedKeys.columnMetadata;
    }
  }

  public byte[] byteArrayValueFromRowHandle(int rowHandle, int colIndex) throws VortexDatabaseException
  {
    synchronized(this) {
      checkColumn(colIndex);
      checkRow(rowHandle);
      
      Row row = rows[rowHandle];
      Column col = row.cols[colIndex];
      if(col.type == PuakmaXmlCodes.COLT_BYTEARRAY)
        return col.b;
      
      throw throwInvalidType(colIndex, "byte[]", col.type);
    }
  }

  private void checkRow(int rowHandle)
  {
    if(rowHandle > this.rows.length || rowHandle < 0)
      throw new IllegalArgumentException("Requesting invalid row - " + rowHandle);
  }

  public String valueFromRowHandle(int rowHandle, int colIndex) throws VortexDatabaseException
  {
    synchronized(this) {
      checkColumn(colIndex);
      checkRow(rowHandle);
      
      Row row = rows[rowHandle];
      Column col = row.cols[colIndex];
      if(col.type == PuakmaXmlCodes.COLT_BYTEARRAY)
        return new String(col.b);
      else
        return col.o.toString();
    }
  }

  public int getColumnFloatDecimals(int colIndex)
  {
    checkColumn(colIndex);
    
    return this.metadata[colIndex].precision;
  }

  public int getColumnSize(int colIndex)
  {
    checkColumn(colIndex);
    
    return this.metadata[colIndex].displaySize;
  }

  public int getColumnSqlType(int colIndex)
  {
    checkColumn(colIndex);
    
    return metadata[colIndex].type;
  }

  public String getColumnTypeName(int colIndex)
  {
    checkColumn(colIndex);
    
    return this.metadata[colIndex].typeName;
  }  
}
