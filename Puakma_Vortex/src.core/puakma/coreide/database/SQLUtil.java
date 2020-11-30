/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jan 15, 2006
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

import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import puakma.utils.lang.ArrayUtils;

/**
 * This class is for determining some stuff from SQLUtil constants. Specially conversions between
 * those constants, and string types.
 *
 * @author Martin Novak
 */
public class SQLUtil
{
  private static final String[] FLOAT_TYPES = {
    "FLOAT", "DOUBLE", "NUMERIC", "DECIMAL", "REAL"
  };
  private static final String[] INTEGER_TYPES = {
    "INTEGER", "SMALLINT", "TINYINT", "BIGINT", "BIT",
    "INT" // MYSQL
  };
  private static final String[] STRING_TYPES = {
    "VARCHAR", "CHAR", "VARCHAR2", "NVARCHAR",
    "BINARY", "LONGVARCHAR", "VARBINARY",
  };
  private static final int[] NUMERIC_TYPES = {
    Types.BIGINT, Types.BIT, Types.DECIMAL, Types.DOUBLE, Types.FLOAT, Types.INTEGER, Types.NUMERIC,
    Types.REAL, Types.SMALLINT, Types.TINYINT
  };
  private static final String[] BINARY_TYPES = {
    "BLOB", "CLOB", "BINARY", "VARBINARY", "LONGVARBINARY", "OTHER",
  };
  
  private static final String[] OFFICIAL_TYPE_NAMES = {
    "BIGINT", "BINARY", "BIT", "BLOB", "BOOLEAN", "CHAR", "CLOB",
    "DATE", "DECIMAL", "DOUBLE", "FLOAT", "INTEGER", "LONGVARBINARY",
    "LONGVARCHAR", "NUMERIC", "REAL", "SMALLINT", "TIME", "TIMESTAMP",
    "TINYINT", "VARBINARY", "VARCHAR",
    "INT", // MYSQL
  };
  // TODO: javaobject, array, ref
  private static final int[] OFFICIAL_TYPE_CONSTS = {
    Types.BIGINT, Types.BINARY, Types.BIT, Types.BLOB, Types.BOOLEAN, Types.CHAR, Types.CLOB,
    Types.DATE, Types.DECIMAL, Types.DOUBLE, Types.FLOAT, Types.INTEGER, Types.LONGVARBINARY,
    Types.LONGVARCHAR, Types.NUMERIC, Types.REAL, Types.SMALLINT, Types.TIME, Types.TIMESTAMP,
    Types.TINYINT, Types.VARBINARY, Types.VARCHAR,
    Types.INTEGER
  };
  
  static {
    assert OFFICIAL_TYPE_NAMES.length == OFFICIAL_TYPE_CONSTS.length : "Array sizes don't match.";
  }
  
  /**
   * This functions checks if the type name is floting point type
   * 
   * @param typeName is the sql type name like FLOAT or NUMERIC
   * @return true if the sql type is representing floating point
   */
  public static boolean isFloatType(String typeName)
  {
    int index = ArrayUtils.indexOfIgnoreCase(FLOAT_TYPES, typeName);
    return index != -1 ? true : false;
  }

  /**
   * This functions checks if the type name is integer type
   * 
   * @param typeName is the sql type name like FLOAT or NUMERIC
   * @return true if the sql type is representing integer
   */
  public static boolean isIntegerType(String typeName)
  {
    int index = ArrayUtils.indexOfIgnoreCase(INTEGER_TYPES, typeName);
    return index != -1 ? true : false;
  }

  /**
    * This functions checks if the type name is string type
    * 
    * @param typeName is the sql type name like FLOAT or NUMERIC
    * @return true if the sql type is representing string
    */
  public static boolean isStringType(String typeName)
  {
    int index = ArrayUtils.indexOfIgnoreCase(STRING_TYPES, typeName);
    return index != -1 ? true : false;
  }

  /**
   * Checks if the type can be floating point type. Note that parameter values
   * are taken from {@link Types} constants.
   * 
   * @param type is the type of sql type from {@link Types}
   * @return true if the type can be float
   */
  public static boolean isFloatType(int type)
  {
    switch(type) {
      case Types.FLOAT:
      case Types.DOUBLE:
      case Types.NUMERIC:
      case Types.REAL:
      case Types.DECIMAL:
        return true;
    }
    return false;
  }

  /**
   * Checks if the type is any numeric type - float or integer
   * 
   * @see Types
   */
  public static boolean isNumericType(int type)
  {
    return isIntegerType(type) || isFloatType(type);
  }

  /**
   * Checks if the type is integer type
   * @see Types
   */
  public static boolean isIntegerType(int type)
  {
    switch(type) {
      case Types.INTEGER:
      case Types.CHAR:
      case Types.SMALLINT:
      case Types.TINYINT:
      case Types.BIGINT:
        return true;
    }
    return false;
  }

  /**
   * Checks if the type is string type
   * @see Types
   */
  public static boolean isStringType(int type)
  {
    switch(type) {
      case Types.VARCHAR:
      case Types.LONGVARCHAR:
        return true;
    }
    return false;
  }

  /**
   * Resolves the type of the driver according to the driver class name. Note
   * that the default type is Mysql style escaping.
   */
  public static int getEscapeTypeFromDriverClass(String className)
  {
    int type = SQLUtil.ESCAPE_TYPE_MYSQL;
    
    if(className.indexOf("sybase") != -1 || className.indexOf("postgre") != -1)
      type = SQLUtil.ESCAPE_TYPE_SYBASE;
    
    return type;
  }

  /**
   * Splits up sql to more sql commands. Sql commands are separated by
   * semicolon, and special care must be taken to handle quotes, and escaping,
   * so user have to supply quoting style. Sybase quotes '', mysql \'.
   * 
   * @param sql is string with more sql commands separated by ';'
   * @return array with separate sql commands
   */
  public static SQLCommandDescriptor[] splitSqlToCommands(String sql, int type)
  {
    if(type != SQLUtil.ESCAPE_TYPE_MYSQL && type != SQLUtil.ESCAPE_TYPE_SYBASE)
      throw new IllegalArgumentException("Escaping type is invalid");
    
    List<SQLCommandDescriptor> l = new ArrayList<SQLCommandDescriptor>();
    int count = sql.length();
    StringBuffer sb = new StringBuffer();
    boolean inQuotes = false;
    char escapeChar = type == SQLUtil.ESCAPE_TYPE_MYSQL ? '\\' : '\'';
    int commandStart = 0;
    SQLCommandDescriptor prevDesc = null;
    
    for(int i = 0; i < count; ++i) {
      char c = sql.charAt(i);
      if(c == escapeChar && i+1 < count && sql.charAt(i+1) == '\'') {
        sb.append("\\'");
        i++;
      }
      else if(c == '\'') {
        inQuotes = !inQuotes;
        sb.append(c);
      }
      else if(c == ';' && inQuotes == false) {
        String s = sb.toString();
        if(prevDesc != null) {
          prevDesc.setEnd(commandStart - 1);
          prevDesc = null;
        }
        
        if(s.trim().length() > 0) {
          SQLCommandDescriptor desc = new SQLCommandDescriptor();
          desc.sql = s;
          desc.setStart(commandStart);
          l.add(desc);
          prevDesc = desc;
        }
        commandStart = i + 1;
        sb.setLength(0);
      }
      else
        sb.append(c);
    }
    
    String s = sb.toString();
    if(s.trim().length() > 0) {
      if(prevDesc != null) {
        prevDesc.setEnd(commandStart - 1);
      }
      SQLCommandDescriptor desc = new SQLCommandDescriptor();
      desc.sql = s;
      desc.setupPositions(commandStart, sql.length());
      l.add(desc);
    }
    
    // AND NOW TRIM ALL THOSE STRINGS
    Iterator<SQLCommandDescriptor> it = l.iterator();
    while(it.hasNext()) {
      SQLCommandDescriptor desc = it.next();
      String sx = desc.sql;
      
      int st = 0;
      int len = sx.length();
      while ((st < len) && (sx.charAt(st) <= ' '))
        st++;
      while ((st < len) && (sx.charAt(len - 1) <= ' '))
        len--;
      
      if((st > 0) || (len < count)) {
        desc.sql = sx.substring(st, len);
        desc.setupPositions(desc.getStart() + st, desc.getEnd() - (sx.length() - len));
      }
    }
    
    return l.toArray(new SQLCommandDescriptor[l.size()]);
  }

  public static final int ESCAPE_TYPE_MYSQL = 0;
  public static final int ESCAPE_TYPE_SYBASE = 1;
}
