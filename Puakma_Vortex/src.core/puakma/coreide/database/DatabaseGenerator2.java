/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    09/05/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide.database;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.io.IOException;
import java.io.InputStream;

import puakma.coreide.PuakmaCoreException;
import puakma.coreide.objects2.Database;
import puakma.vortex.VortexPlugin;

public class DatabaseGenerator2
{
  public static final int DB_MYSQL = 1;
  public static final int DB_POSTGRE = 2;
  public static final int DB_HSQLDB = 3;
  public static final String DROP_POLICY_CREATE_IF_NOT_EXIST = "createIfNotExists";
  public static final String DROP_POLICY_DROP = "dropTablesIfExists";
  public static final int ERROR_POLICY_STOP = 0;
  public static final int ERROR_POLICY_CONTINUE = 1;
  public static final int ERROR_POLICY_TRANSACTION = 2;
  
  private int type;
  private Database database;
  private String dropPolicy;
  private int errorPolicy;
  private Script[] scripts = new Script[4];
  
  public DatabaseGenerator2(Database database, int type)
  {
    if(type != DB_MYSQL && type != DB_POSTGRE && type != DB_HSQLDB)
      throw new IllegalArgumentException("Invalid database type - " + type);
    if(database == null)
      throw new IllegalArgumentException("Database passed to generator cannot be null");
    
    this.database = database;
    this.type = type;
    this.dropPolicy = DROP_POLICY_DROP;
  }

  /**
   * Returns the type of database we want to generate. The types are
   * {@link #DB_MYSQL}, {@link #DB_POSTGRE}, and {@link #DB_HSQLDB}.
   */
  public int getType()
  {
    return type;
  }
  
  /**
   * This function generates an sql generation script for the database we specified.
   */
  public String generateSql() throws PuakmaCoreException, IOException
  {
    if(type != DB_MYSQL && type != DB_POSTGRE && type != DB_HSQLDB)
      throw new IllegalStateException("Unknown database type - " + type);
    
    // LOAD SCRIPT IF NOT AVAILABLE OR DEBUG MODE IS PRESENT
    if(scripts[type] == null || VortexPlugin.DEBUG_MODE) {
      GroovyShell sh = new GroovyShell();
      InputStream is = null;
      
      try {
      if(type == DB_MYSQL)
        is = DatabaseGenerator2.class.getResourceAsStream("mysql.groovy");
      else if(type == DB_POSTGRE)
        is = DatabaseGenerator2.class.getResourceAsStream("postgres.groovy");
      else if(type == DB_HSQLDB)
        is = DatabaseGenerator2.class.getResourceAsStream("hsqldb.groovy");
    
      if(is != null)
        scripts[type] = sh.parse(is);
      }
      catch(Exception ex) {
        if(is != null) try { is.close(); } catch(Exception ex1) {  }
      }
    }
    
    Script script = scripts[type];
    Binding b = new Binding();
    b.setProperty("db", database);
    b.setProperty("dropTablePolicy", dropPolicy);
    script.setBinding(b);
    Object o = script.run();
    return o.toString();
  }

  public void setDropTablePolicy(String dropPolicy)
  {
    this.dropPolicy = dropPolicy;
  }

  /**
   * Sets the error policy for the generator. This can be
   * {@link #ERROR_POLICY_STOP}, {@link #ERROR_POLICY_CONTINUE}, and
   * {@link #ERROR_POLICY_TRANSACTION}.
   */
  public void setErrorPolicy(int errorPolicy)
  {
    if(errorPolicy < 0 || errorPolicy > 2)
      throw new IllegalArgumentException("Invalid error policy");
    
    this.errorPolicy = errorPolicy;
  }
  
  /**
   * Returns error policy for the generator. This can be
   * {@link #ERROR_POLICY_STOP}, {@link #ERROR_POLICY_CONTINUE}, and
   * {@link #ERROR_POLICY_TRANSACTION}.
   */
  public int getErrorPolicy()
  {
    return errorPolicy;
  }
}
