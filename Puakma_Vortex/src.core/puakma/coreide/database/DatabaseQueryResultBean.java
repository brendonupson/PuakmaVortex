/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Oct 26, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide.database;

import puakma.coreide.designer.PuakmaXmlCodes;

public class DatabaseQueryResultBean implements PuakmaXmlCodes
{
  public String resultType;
  public DBResultSet resultSet;
  public DBResultSet generatedKeys;
  public ServerException exception;
  
  public static class ServerException
  {
    public String message;
    public String[] stackTrace;
    public int errorCode;
    public String sqlState;
    public String className;
  }
  
  public static class DBResultSet
  {
    public RSColumnMetadata[] columnMetadata;
    public Row[] rows = new Row[0];
  }
  
  /**
   * This is a bean into which we convert xml resultset column metadata facility
   * @author Martin Novak
   */
  public static class RSColumnMetadata
  {
    public String className;
    public String catalog;
    public String label;
    public int displaySize;
    public String name;
    public int type;
    public String typeName;
    public int precision;
    public int scale;
    public String schema;
    public String table;
    public boolean autoIncrement;
    public boolean caseSensitive;
    public boolean currency;
    public boolean definitelyWritable;
    public boolean nullable;
    public boolean readOnly;
    public boolean searchable;
    public boolean signed;
    public boolean writable;
  }
  
  public static class Row
  {
    public Column[] cols;
  }
  
  public static class Column
  {
    public int     index;
    public int     type;
    public Object  o;
    public byte[]  b;
    public boolean isNull;
  }
}
