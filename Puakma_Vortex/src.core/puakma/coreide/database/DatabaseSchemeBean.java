/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Dec 15, 2005
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

import java.util.ArrayList;
import java.util.List;

public class DatabaseSchemeBean
{
  public String sqlStateType;
  public List<String> keywords = new ArrayList<String>();
  public List<String> numFunctions = new ArrayList<String>();
  public List<String> dateTimeFunctions = new ArrayList<String>();
  
  public List<DatabaseSchemeBean.DbTable> jdbcTables = new ArrayList<DatabaseSchemeBean.DbTable>();
  public List<DatabaseSchemeBean.DbTable> systemDbTables = new ArrayList<DatabaseSchemeBean.DbTable>();
  public String customData;
  
  public static class DbTable {
    public String name;
    public String description;
    public int type;
    
    // SPECIFIC FOR SYSTEM DATABASE
    public long id;
    public int buildOrder;
    
    // IGNORE
    public String catalog;
    // IGNORE
    public String schema;

    public List<DatabaseSchemeBean.DbColumn> columns = new ArrayList<DatabaseSchemeBean.DbColumn>();
  }
  
  public static class DbColumn {
    public int columnSize = -1;
    public int decimalDigits = -1;
    public String defaultValue;
    public String description;
    public String name;
    public boolean isNullable;
    public int position;
    public boolean isPk;
    public boolean isUniq;
    public boolean autoInc;
    
    // 
    public int dataType;
    public long id;
    // EQUAL TO TYPE COLUMN IN ATTRIBUTES TABLE
    public String typeName;
    // FK TABLE
    public long fkTable;
    public String fkTableName;
    
    // NOT REALLY NEEDED - IGNORE NOW
    //public int radix;
  }
}
