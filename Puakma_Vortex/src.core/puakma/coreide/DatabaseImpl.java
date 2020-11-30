/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Nov 21, 2005
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

import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import puakma.SOAP.SOAPFaultException;
import puakma.coreide.database.DatabaseQueryResultBean;
import puakma.coreide.database.DatabaseSchemeBean;
import puakma.coreide.database.SQLCommandDescriptor;
import puakma.coreide.database.XmlDatabaseResultParser;
import puakma.coreide.database.XmlDatabaseStructureParser;
import puakma.coreide.database.DatabaseSchemeBean.DbColumn;
import puakma.coreide.database.DatabaseSchemeBean.DbTable;
import puakma.coreide.designer.DatabaseDesigner;
import puakma.coreide.designer.PuakmaXmlCodes;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.ApplicationObject;
import puakma.coreide.objects2.Database;
import puakma.coreide.objects2.DatabaseConnection;
import puakma.coreide.objects2.DatabaseObject;
import puakma.coreide.objects2.FkConnection;
import puakma.coreide.objects2.ProgressMonitor;
import puakma.coreide.objects2.SQLQuery;
import puakma.coreide.objects2.Table;
import puakma.coreide.objects2.TableColumn;
import puakma.utils.NameValuePair;
import puakma.utils.SimpleXmlWriter;
import puakma.utils.XmlUtils;
import puakma.utils.lang.ListenersList;

class DatabaseImpl implements Database
{
  private ListenersList listeners = new ListenersList();
  /**
   * If true, the database is somehow refreshed from the source, and there is no need to refresh it
   * once more.
   */
  private boolean open = false;
  
  private DatabaseConnectionImpl dbConnection;
  /**
   * List of all tables.
   */
  private List<TableImpl> tables = new ArrayList<TableImpl>();
  /**
   * True if backing jdbc db is empty
   */
  private boolean backingEmpty;
  private boolean workingCopy;
  private DatabaseImpl orginal;
  private DesignerFactory designerFactory;
  private boolean isNew = true;
  private Map<String, Object> dataTable = new HashMap<String, Object>(0);
  /**
   * List of tables that are about to be removed, but not commited to the server yet
   */
  private List<TableImpl> removedTables = new ArrayList<TableImpl>();
  
  public DatabaseImpl(DatabaseConnection dbConnection)
  {
    this.dbConnection = (DatabaseConnectionImpl) dbConnection;
  }
  
  /**
   * Loads the whole content of the database from the fucking jdbc server, and also sets up
   * the mode.
   */
  void refresh(DatabaseSchemeBean bean)
  {
    List<DatabaseSchemeBean.DbTable> jdbc = new ArrayList<DatabaseSchemeBean.DbTable>(bean.jdbcTables);
    List<DatabaseSchemeBean.DbTable> sys = new ArrayList<DatabaseSchemeBean.DbTable>(bean.systemDbTables);
    List<Object[]> addedTables = new ArrayList<Object[]>();
    
    synchronized(tables) {
      // RUN ONE TABLE AFTER ANOTHER
      Iterator<DatabaseSchemeBean.DbTable> it = sys.iterator();
      while(it.hasNext()) {
        DbTable sTable = it.next();
        
        // NOW FIND THE SAME COLUMN AMONG THE TABLES IN SYSTEM DATABASE
        Iterator<DbTable> it1 = jdbc.iterator();
        DbTable jTable = null;
        INNER: while(it1.hasNext()) {
          jTable = it1.next();
          if(jTable.name.equalsIgnoreCase(sTable.name)) {
            it.remove();
            break INNER;
          }
          jTable = null;
        }
        if(jTable == null) {
          // TODO: DO WE NEED TO HANDLE THIS???
          // IGNORE FOR NOW. 1.1 IS FOR MANIPULATION OF PUAKMA DATABASE ONLY 1.2 ADDS SOME MORE SUPPORT
          // THERE
        }
        
        TableImpl table = (TableImpl) getTable(sTable.name);
        boolean addTable = (table == null);
        if(addTable) {
          table = new TableImpl(this);
          table.setup(jTable, sTable);
          table.original = null;
          table.setValid();
          tables.add(table);
        }
        else {
          table.setup(jTable, sTable);
          table.original = null;
          table.setValid();
        }
        
        // ADD ALL TABLES, AND IF THERE IS SOME CHANGE IN REFERENCING, IT WILL
        // BE PROPAGATED TO EACH TABLE IN THE NEXT STAGE
        Object[] o = new Object[] { table, sTable, jTable, Boolean.valueOf(addTable) };
        addedTables.add(o);
      } // for(ALL TABLES)
      
      // NOW SETUP ALL FOREIGN KEYS
      for(Object[] o : addedTables) {
        TableImpl table = (TableImpl) o[0];
        DbTable sTable = (DbTable) o[1];
        table.setupFks(sTable);
      }
      
      // AND ALSO FIRE SOME EVENTS
      for(Object[] o : addedTables) {
        TableImpl table = (TableImpl) o[0];
        listeners.fireEvent(this, PROP_ADD_TABLE, null, table);
        if(dbConnection != null) {
          ApplicationImpl application = (ApplicationImpl) dbConnection.getApplication();
          application.fireAddObject(table, true);
        }
      }
    }
  }

  public Table getTable(String tableName)
  {
    synchronized(tables) {
      for(Table table : tables)
        if(table.getName().equalsIgnoreCase(tableName))
          return table;
      
      return null;
    }
  }
  
  public Table getTable(long tableId)
  {
    synchronized(tables) {
      for(Table table : tables)
        if(tableId == table.getId())
          return table;
      
      return null;
    }
  }

  public Table[] listTables()
  {
    synchronized(tables) {
      return tables.toArray(new Table[tables.size()]);
    }
  }
  
  TableImpl[] listTableImpls()
  {
    synchronized(tables) {
      return tables.toArray(new TableImpl[tables.size()]);
    }
  }

  public SQLQuery executeQuery(String sql) throws VortexDatabaseException, IOException
  {
    boolean updateable = false;
    try {
      synchronized(this) {
        SQLQueryImpl query = new SQLQueryImpl(dbConnection);
        DatabaseDesigner dbClient = getDatabaseClient();
        XmlDatabaseResultParser handler = new XmlDatabaseResultParser();
        String xml = dbClient.executeQuery(dbConnection.getId(), sql, updateable);
        XmlUtils.parseXml(handler, xml);
        DatabaseQueryResultBean result = handler.getResult();
        if(result.exception != null) {
          throw new VortexDatabaseException(result.exception);
        }
        query.internalSetup(sql, result);
        return query;
      }
    }
    catch(SOAPFaultException ex) {
      throw PuakmaLibraryUtils.handleDbException(ex);
    }
    catch(PuakmaCoreException ex) {
      throw PuakmaLibraryUtils.handleDbException(ex);
    }
    catch(SAXException ex) {
      throw PuakmaLibraryUtils.handleDbException(ex);
    }
    catch(ParserConfigurationException ex) {
      throw PuakmaLibraryUtils.handleDbException(ex);
    }
  }
  
  public SQLCommandDescriptor[] executeBatch(String[] sql, int errorAction) throws VortexDatabaseException, IOException
  {
    SQLCommandDescriptor[] sqlCommands = new SQLCommandDescriptor[sql.length];
    for(int i = 0; i < sql.length; ++i) {
      sqlCommands[i] = new SQLCommandDescriptor();
      sqlCommands[i].sql = sql[i];
    }
    
    executeBatch(sqlCommands, errorAction);
    
    return sqlCommands;
  }
  
  public void executeBatch(SQLCommandDescriptor[] sqlCommands, int errorAction) throws VortexDatabaseException, IOException
  {
    boolean failOnError = errorAction == ERROR_FAIL;
    boolean transaction = errorAction == ERROR_TRANSACTION;
    DatabaseDesigner designer = getDatabaseClient();
    try {
      String[] sql = new String[sqlCommands.length];
      for(int i = 0; i < sql.length; ++i)
        sql[i] = sqlCommands[i].sql;
      
      String[] ret = designer.executeBatch(dbConnection.getId(), sql, failOnError, transaction);
      
      // NOW PROCESS THE COMMAND
      for(int i = 0; i < ret.length; ++i) {
        if(ret[i] == null)
          continue;
        
        try {
          int xxx = Integer.parseInt(ret[i]);
          sqlCommands[i].result = xxx;
        }
        catch(NumberFormatException ex) {
          // IF THE NUMBER IS INVALID, IT WOULD BE PROBABLY PROPERTIES FILE WITH
          // SOME STUFF IN THERE
          Properties p = new Properties();
          ByteArrayInputStream bis = new ByteArrayInputStream(ret[i].getBytes("ISO-8859-1"));
          p.load(bis);
          
          sqlCommands[i].exceptionStackTrace = p.getProperty("stackTrace");
          sqlCommands[i].exceptionMessage = p.getProperty("msg");
          sqlCommands[i].sqlErrorState = p.getProperty("sqlState");
          try { sqlCommands[i].sqlErrorCode = Integer.parseInt(p.getProperty("sqlErrorCode")); }
          catch(Exception nfe1) {  }
        }
      }
    }
    catch(SOAPFaultException e) {
      throw PuakmaLibraryUtils.handleDbException(e);
    }
  }

  DatabaseDesigner getDatabaseClient()
  {
    if(designerFactory != null)
      return designerFactory.newDbDesigner();
    // IF THIS IS WORKING COPY, WE SHOULD USE ORIGINALS DATABASE CLIENT
    if(isWorkingCopy())
      return orginal.getDatabaseClient();
    
    return dbConnection.getDatabaseClient();
  }
  
  /**
   * @return DatabaseConnection object to which is this database assigned
   */
  public DatabaseConnection getDatabaseConnection()
  {
    return dbConnection;
  }

  public boolean isBackingEmpty()
  {
    // TODO: implement real time check here
    return backingEmpty;
  }

  public Application getApplication()
  {
    return dbConnection == null ? null : dbConnection.getApplication();
  }

  public void refresh() throws PuakmaCoreException
  {
    try {
      DatabaseDesigner dbClient = getDatabaseClient();
      String xml = dbClient.getDdl(dbConnection.getId());
      DatabaseSchemeBean bean = parseDatabaseBean(xml);
      refresh(bean);
      open = true;
    }
    catch(IOException ex) {
      throw PuakmaLibraryUtils.handleException(ex);
    }
    catch(SOAPFaultException e) {
      throw PuakmaLibraryUtils.handleException(e);
    }
    catch(SAXException ex) {
      throw PuakmaLibraryUtils.handleException(ex);
    }
    catch(ParserConfigurationException ex) {
      throw PuakmaLibraryUtils.handleException(ex);
    }
  }

  /**
   * Parses database xml, and returns {@link DatabaseSchemeBean} object.
   */
  static DatabaseSchemeBean parseDatabaseBean(String xml) throws SAXException, IOException, ParserConfigurationException
  {
    XmlDatabaseStructureParser handler = new XmlDatabaseStructureParser();
    XmlUtils.parseXml(handler, xml);
    DatabaseSchemeBean bean = handler.getResult();
    return bean;
  }

  public void addListener(PropertyChangeListener listener)
  {
    synchronized(listeners) {
      listeners.addListener(listener);
    }
  }

  public void removeListener(PropertyChangeListener listener)
  {
    synchronized(listeners) {
      listeners.removeListener(listener);
    }
  }

  public boolean isOpen()
  {
    return open;
  }

  public void close()
  {
    tables.clear();
    listeners.clear();
  }

  /**
   * @return id of the row in the DATABASECONNECTION table.
   */
  public long getConnectionId()
  {
    return dbConnection.getId();
  }

  public void addObject(DatabaseObject dbo) throws PuakmaCoreException, IOException
  {
    if(dbo instanceof ApplicationObject) {
      ApplicationObject obj = (ApplicationObject) dbo;
      if(obj.isNew() == false)
        throw new IllegalArgumentException("Cannot add object which is not new.");
      if(obj.isWorkingCopy())
        throw new IllegalArgumentException("Cannot add working copy");
    }
    
    // TODO: check also against new/wc status of db object
    if(dbo instanceof TableImpl) {
      TableImpl table = (TableImpl) dbo;
      synchronized(tables) {
        for(Table t : tables) {
          if(t.getName().equalsIgnoreCase(table.getName()))
            throw new PuakmaCoreException("Table " + table.getName() + " already exists in database");
        }
        
        table.setDatabase(this);
        // WE CAN COMMIT THIS ONLY IF WE ARE NOT WORKING COPIES
        if(this.isWorkingCopy() == false && this.isNew() == false) {
          table.commit();
        }
        tables.add(table);
        
        listeners.fireEvent(this, PROP_ADD_TABLE, null, table);
        if(dbConnection != null) {
          ApplicationImpl app = (ApplicationImpl) dbConnection.getApplication();
          app.fireAddObject(table, false);
        }
      }
    }
    else if(dbo instanceof TableColumn)
      throw new IllegalArgumentException("Cannot add column to database, must be added to table.");
    else
      throw new IllegalArgumentException("Want to add unknown database object.");
  }
  
  public void removeObject(DatabaseObject dbo) throws PuakmaCoreException, IOException
  {
    // TODO: check also against new/wc status of db object
    if(dbo instanceof Table) {
      TableImpl table = (TableImpl) dbo;
      synchronized(tables) {
        // WE CAN COMMIT THIS ONLY IF WE ARE NOT WORKING COPIES
        boolean wc = this.isWorkingCopy() || this.isNew();
        if(wc) {
          removedTables.add(table);
        }
        else {
          try {
            table.intRemove();
          }
          catch(SOAPFaultException e) {
            throw new PuakmaCoreException(e);
          }
        }
        tables.remove(table);
        table.setDatabase(null);
        
        listeners.fireEvent(this, PROP_REMOVE_TABLE, table, null);
        if(dbConnection != null) {
          ApplicationImpl app = (ApplicationImpl) dbConnection.getApplication();
          app.fireRemoveEvent(table, false);
        }
        
        // ALSO REMOVE EXISTING DATABASE CONNECTIONS POINTING TO THE TABLE TO BE REMOVED
        List<FkConnection> conns = table.getTargetConnections();
        for(FkConnection con : conns) {
          TableColumn col = con.getSourceColumn();
          col.setRefTable(null);
        }
      }
    }
    else if(dbo instanceof TableColumn)
      throw new IllegalArgumentException("Cannot remove column to database, must be removed from table.");
    else
      throw new IllegalArgumentException("Want to remove an unknown database object.");
  }

  public void commit(ProgressMonitor monitor) throws VortexMultiException
  {
    if(isWorkingCopy() == false && isNew() == false)
      throw new IllegalStateException("Database has to be working copy to commit");
    
    VortexMultiException multiEx = new VortexMultiException();
    isNew = false;
    
    TableImpl[] tables = listTableImpls();
    int work = tables.length * 2;
    monitor.setup(work);
    
    try {
      // AT FIRST REMOVE ALL TABLES WHO ARE NOT IN DATABASE ANYMORE
      // TODO: THIS IS NOT REALLY THREAD SAFE, BUT KIND OF OPTIMISTIC ASSUMPTION
      synchronized(this.tables) {
        for(TableImpl t : removedTables) {
          try {
            t.intRemove();
          }
          catch(Exception e) {
            multiEx.addException(e);
          }
        }
        removedTables.clear();
      }
      
      // SO GO THRU ALL TABLES, AND COMMIT WORKING COPIES THERE
      for(int i = 0; i < tables.length; ++i) {
        try {
          tables[i].commit();
          monitor.worked(1);
        }
        catch(Exception e) {
          multiEx.addException(e);
        }
      }
      
      // NOW PERFORM SECOND COMMIT ON THE TABLES - RELATIONS
      for(int i = 0; i < tables.length; ++i) {
        try {
          tables[i].commitRelationsInternal();
        }
        catch(Exception e) {
          multiEx.addException(e);
        }
      }
      
      if(multiEx.isEmpty() == false)
        throw multiEx;
    }
    finally {
      monitor.done();
    }
  }

  public boolean isWorkingCopy()
  {
    return workingCopy;
  }

  public Database makeWorkingCopy()
  {
    DatabaseImpl wc = new DatabaseImpl(dbConnection);
    wc.orginal = this;
    wc.workingCopy = true;
    
    // OK, SO MAKE WORKING COPIES OF ALL TABLES, SO IT'S GONNA BE EASIER TO HANDLE COMMITS...
    for(TableImpl table : tables) {
      TableImpl copy = (TableImpl) table.makeWorkingCopy();
      copy.setDatabase(wc);
      
      // ADD COPIED TABLE TO COPY OF DATABASE
      wc.tables.add(copy);
    }
    
    wc.adjustWorkingCopy();
    
    return wc;
  }

  private void adjustWorkingCopy()
  {
    if(workingCopy == false)
      throw new IllegalStateException("You have to call this function only in working copy");
    
    for(TableImpl t : tables)
      t.adjustWorkingCopy();
  }

  /**
   * Sets up the database designer factory. It's quite usefull function in tests.
   *
   * @param factory is the designer factory supplying DatabaseDesigner objects to this object
   */
  public void setDesignerFactory(DesignerFactory factory)
  {
    this.designerFactory = factory;
  }

  public void setNew(boolean isNew)
  {
    this.isNew = isNew;
  }
  
  public boolean isNew()
  {
    return this.isNew;
  }
  
  public Object getData(String key)
  {
    synchronized(dataTable) {
      return dataTable.get(key);
    }
  }
  
  public void setData(String key, Object data)
  {
    assert key != null : "Key for data inserted to the object cannot be null";
    
    NameValuePair pair = new NameValuePair(key, data);
    Object oldValue;
    
    synchronized(dataTable) {
      oldValue = dataTable.get(key);
      if(data == null)
        dataTable.remove(key);
      else
        dataTable.put(key, data);
    }
    
    listeners.fireEvent(this, PROP_DATA, oldValue, pair);
  }

  /**
   * Finds column by id among all tables.
   */
  TableColumnImpl findColumnByIdFromAllTables(long colId)
  {
    synchronized(tables) {
      for(Table t : tables) {
        TableColumnImpl tci = (TableColumnImpl) t.getColumn(colId);
        if(tci != null)
          return tci;
      }
    }
    
    return null;
  }

  /**
   * Returns original {@link Database} object if this is working copy. If this
   * is origianl, returns null.
   */
  public Database getOriginal()
  {
    return this.orginal;
  }

  public String loadContentFromXml(InputStream is) throws SAXException, IOException, ParserConfigurationException
  {
    DatabaseSchemeBean bean = null;
    XmlDatabaseStructureParser parser = new XmlDatabaseStructureParser();
    XmlUtils.parseXml(parser, is);
    bean = parser.getResult();
    
    refresh(bean);
    
    // TODO: remove this ugly hack, and support some native loading instead of refresh
    TableImpl[] tables = listTableImpls();
    for(TableImpl table : tables) {
      table.setNew();
      for(TableColumnImpl col : table.listColumnImpls()) {
        col.setNew();
      }
    }
    
    // ADJUST FOREIGN KEYS - TODO: move this code also to some other operation
    for(DatabaseSchemeBean.DbTable tableBean : bean.systemDbTables) {
      for(DbColumn columnBean : tableBean.columns) {
        if(columnBean.fkTableName != null && columnBean.fkTableName.length() > 0) {
          TableImpl t = (TableImpl) getTable(tableBean.name);
          TableColumnImpl column = (TableColumnImpl) t.getColumn(columnBean.name);
          TableImpl refTable = (TableImpl) getTable(columnBean.fkTableName);
          if(refTable != null) {
            column.setRefTable(refTable);
          }
        }
      }
    }
    
    return bean.customData;
  }

  public void saveContentToXml(OutputStream os, String customData) throws IOException
  {
    SimpleXmlWriter writer = new SimpleXmlWriter(SimpleXmlWriter.FLAG_NO_EMPTY_ATTRIBUTES);
    
    writer.addTag(PuakmaXmlCodes.ELEM_RESULT, false);
    
    TableImpl[] tables = listTableImpls();
    for(TableImpl table : tables) {
      table.storeToXml(writer);
    }
    
    if(customData != null && customData.length() > 0)
      writer.addTag(PuakmaXmlCodes.ELEM_CUSTOM, customData);
    
    writer.closeTag(PuakmaXmlCodes.ELEM_RESULT);
    String str = writer.toString();
    os.write(str.getBytes("UTF-8"));
  }
}
