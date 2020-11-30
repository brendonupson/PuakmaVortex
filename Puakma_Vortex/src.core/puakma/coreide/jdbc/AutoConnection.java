/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Oct 25, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This class encapsulates automatic database resources allocation and freeing from database
 * connection provided by user.
 *
 * @author Martin Novak
 */
public class AutoConnection
{
  /**
   * List of ResultSet object for cleanup
   */
  private List<ResultSet> resultSets = new LinkedList<ResultSet>();
  
  /**
   * List of Statement object for cleaning
   */
  private List<Statement> statements = new LinkedList<Statement>();
  
  /**
   * Current connection
   */
  protected Connection connection = null;

  /**
   * If true then the connection is initially transactional. We should return the connection
   * in the state in which it was originally.
   */
  private boolean autoCommit;

  /**
   * If true then we are using db transaction in this session
   */
  private boolean useTransaction;

  /**
   * This is for control - if some developer forgots to commit or rollback transaction,
   * it's rolled back automatically...
   */
  private boolean transactionDone;
  
  /**
   * This is logger for widget
   */
  private SimpleLogger logger;

  /**
   * Metadata information for this database
   */
  private DatabaseMetaData metadata;
  
  /**
   * Thanks to this class we can implement a simple log everywhere
   * @author Martin Novak
   */
  public static interface SimpleLogger {
    public void logException(String message, Throwable t);
  }
  
  public AutoConnection(Connection connection, boolean transactional, SimpleLogger logger) throws SQLException
  {
    this.connection = connection;
    
    this.logger = logger;
    
    this.autoCommit = connection.getAutoCommit();
    this.useTransaction = transactional;
    this.transactionDone = false;
    
    connection.setAutoCommit(transactional == false);
  }

  /**
   * Commits the transaction.
   *
   * @throws SQLException
   */
  protected void commitTransaction() throws SQLException
  {
    if(useTransaction == false)
      throw new RuntimeException("Trying to commit transaction in non-transactional mode.");

    connection.commit();
    transactionDone = true;
  }
  
  /**
   * Rolls the transaction back.
   */
  protected void rollbackTransaction()
  {
    if(useTransaction == false)
      throw new RuntimeException("Trying to rollback transaction in non-transactional mode.");

    try {
      connection.rollback();
      transactionDone = true;
    }
    catch(SQLException e) {
      if(logger != null)
        logger.logException("Cannot rollback transaction. Reason: " + e.getLocalizedMessage(), e);
      else
        e.printStackTrace();
    }
  }
  
  public PreparedStatement prepareStatement(String sql) throws SQLException
  {
    PreparedStatement pst = connection.prepareStatement(sql);
    statements.add(pst);
    return pst;
  }
  
  public Statement createStatement() throws SQLException
  {
    Statement st = connection.createStatement();
    statements.add(st);
    return st;
  }
  
  /**
   * This closes all the allocated resources excepte
   *
   */
  public void close()
  {
    for(ResultSet rs : resultSets) {
      try { rs.close(); }
      catch(SQLException e) {  }
    }
    
    for(Statement st : statements) {
      try { st.close(); }
      catch(SQLException e) {  }
    }
    
    // CLEAR METADATA
    metadata = null;
    
    if(connection != null) {
      if(useTransaction && transactionDone == false)
        rollbackTransaction();

      try {
        if(connection.getAutoCommit() != autoCommit)
          connection.setAutoCommit(autoCommit);
      }
      catch(SQLException e) {
        if(logger != null)
          logger.logException("Error which setting connection transaction state back. "
                       + e.getLocalizedMessage(), e);
        else
          e.printStackTrace();
      }
      
      if(useTransaction && transactionDone == false) {
        throw new IllegalStateException("Transaction hasn't been commited or rolled back. Rolling back by default.");
      }
      
      useTransaction = false;
      transactionDone = true;
      connection = null;
    }
  }
  
  /**
   * Gets the Connection object we assigned at the beginning. This always returns that
   * object because close() actually closes all the <b>allocated</b> resources, not we own.
   *
   * @return Connection object
   */
  public Connection getConnection()
  {
    return connection;
  }

  /**
   * Gets database metadata - information about the current database
   * @return DatabaseMetadata object
   */
  public DatabaseMetaData getMetaData()
  {
    if(metadata == null) {
      try {
        metadata = connection.getMetaData();
      }
      catch(SQLException e) {
        throw new RuntimeException("Cannot fetch database metadata", e);
      }
    }
    
    return metadata;
  }

  public void addResultSet(ResultSet rs)
  {
    resultSets.add(rs);
  }
}
