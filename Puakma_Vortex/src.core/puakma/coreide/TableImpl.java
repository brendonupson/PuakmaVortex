/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Nov 11, 2005
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import puakma.SOAP.SOAPFaultException;
import puakma.coreide.database.DatabaseSchemeBean.DbColumn;
import puakma.coreide.database.DatabaseSchemeBean.DbTable;
import puakma.coreide.designer.DatabaseDesigner;
import puakma.coreide.designer.PuakmaXmlCodes;
import puakma.coreide.objects2.Database;
import puakma.coreide.objects2.FkConnection;
import puakma.coreide.objects2.Table;
import puakma.coreide.objects2.TableColumn;
import puakma.utils.SimpleXmlWriter;
import puakma.utils.lang.ClassUtil;
import puakma.utils.lang.StringUtil;
import puakma.vortex.VortexPlugin;

class TableImpl extends ApplicationObjectImpl implements Table
{
  /**
   * Columns which exists in the puakma system database. Note that keys are names of
   * columns, and are always in uppercase since some databases are case insensitive
   */
  private List<TableColumnImpl> columns = new ArrayList<TableColumnImpl>();
  
  /**
   * Database object under which we belong
   */
  private DatabaseImpl database;
  
  /**
   * List of all columns to be removed when table is going to be commited.
   */
  private List<TableColumnImpl> columnsToRemove = new ArrayList<TableColumnImpl>();

  private List<FkConnection> targetConnections = new ArrayList<FkConnection>();
  
  /**
   * This is internal class for quick transfers of the all columns to the database
   * @author Martin Novak
   */
  public static final class TableColumnDataBean
  {
    public long attId;
    public long tableId;
    public String attName;
    public String type;
    public String typeSize;
    public boolean allowNull;
    public boolean isPk;
    public boolean isAutoIncrement;
    public boolean isUnique;
    public boolean isFtIndex;
    public String refTable;
    public String extraOptions;
    public boolean cascadeDelete;
    public String description;
    public String defaultValue;
    public int position;
    public boolean cascadeUpdate;
  }
  
  public TableImpl(DatabaseImpl db)
  {
    super(db != null ? (ApplicationImpl) db.getApplication() : null);
    
    this.database = db;
  }
  
  void setDatabase(DatabaseImpl db)
  {
    this.database = db;
  }
  
  /**
   * Constructs table from two sources.
   *
   * @param jTable is the jdbc table definition
   * @param sTable is the puakma datbaase table definition
   */
  public void setup(DbTable jTable, DbTable sTable)
  {
    this.name = sTable.name;
    this.description = sTable.description;
    // TODO
    //this.type = sTable.type;
    this.id = sTable.id;
    
    for(DbColumn colBean : sTable.columns) {
      TableColumnImpl column = new TableColumnImpl(this);
      column.setup(colBean);
      //String key = column.getName().toUpperCase();
      columns.add(column);
    }
  }
  
  /**
   * Runs the second round setup. This sets up the foreign keys management.
   */
  void setupFks(DbTable sTable)
  {
    for(DbColumn colBean : sTable.columns) {
      TableColumnImpl col = (TableColumnImpl) getColumn(colBean.id);
      
      // IF THE FOREIGN KEY IS VALID, SETUP FOREIGN KEY HERE
      if(colBean.fkTable > 0)
        col.setupReferenceTable(colBean.fkTable);
      else if(colBean.fkTableName != null && colBean.fkTableName.length() > 0)
        col.setupReferenceTable(colBean.fkTableName);
      else
        col.setRefTable(null);
    }
  }

  public synchronized TableColumn[] listColumns()
  {
    synchronized(columns) {
      TableColumn[] cols = new TableColumn[columns.size()];
      return columns.toArray(cols);
    }
  }
  
  public synchronized TableColumnImpl[] listColumnImpls()
  {
    synchronized(columns) {
      TableColumnImpl[] cols = new TableColumnImpl[columns.size()];
      return columns.toArray(cols);
    }
  }

  public TableColumn getColumn(String name)
  {
    synchronized(columns) {
      for(TableColumn col : columns) {
        if(name.equalsIgnoreCase(col.getName()))
          return col;
      }
      return null;
    }
  }

  public TableColumn getColumn(long id)
  {
    synchronized(columns) {
      for(TableColumn col : columns) {
        if(col.getId() == id)
          return col;
      }
      return null;
    }
  }
  
  public String getDescription()
  {
    return description;
  }

  public String getName()
  {
    return name;
  }
  
  public String toString()
  {
    StringBuffer sb = new StringBuffer(name);
    sb.append(" (");
    Iterator<TableColumnImpl> it = columns.iterator();
    while(it.hasNext()) {
      TableColumn col = it.next();
      sb.append(col.toString());
      if(it.hasNext())
        sb.append(", ");
    }
    sb.append(")");
    return sb.toString();
  }

  public void commit() throws PuakmaCoreException, IOException
  {
    if(isWorkingCopy() == false && isNew() == false)
      throw new IllegalStateException("Cannot commit table which is not working copy or new");
    
    synchronized(this) {
      if(isNew() && database.isWorkingCopy()) {
        // IF THIS IS NEW, WE SHOULD CHECK OUT ALSO IF THE PARENT IS WORKING
        // COPY, IF YES, THEN ADD THIS TO THE WORKING COPY DATABASE. NOTE THAT
        // AT THIS STATE WE KNOW ONLY THAT SOME PART OF DATA IS ON THE SERVER
        // SO WE WILL DO THIS ALSO WITH THE COLUMNS LATER
        DatabaseImpl origDb = (DatabaseImpl) database.getOriginal();
        // SETUP NEW ORIGINAL
        TableImpl o = (TableImpl) makeWorkingCopy();
        o.database = origDb;
        o.status = STATUS_NEW;
        o.original = null;
        TableColumn[] columns = listColumns();
        for(int i = 0; i < columns.length; ++i) {
          TableColumnImpl col = (TableColumnImpl) columns[i];
          col.original = null;
          col.status = STATUS_NEW;
        }
        origDb.addObject(o);
        
        // SETUP ORIGINAL
        o.setValid();
        
        // SETUP THIS AS A WORKING COPY
        id = o.getId();
        this.original = o;
        columns = listColumns();
        for(int i = 0; i < columns.length; ++i) {
          TableColumnImpl originalColumn = (TableColumnImpl) o.getColumn(columns[i].getName());
          TableColumnImpl wcColumn = (TableColumnImpl) columns[i];
          wcColumn.original = originalColumn;
          wcColumn.setId(originalColumn.id);
          wcColumn.setValid();
        }
        setValid();
      }
      else {
        try {
          // SAVE NOW THE TABLE
          DatabaseDesigner designer = getDatabaseImpl().getDatabaseClient();
          long dbConId = -1;
          if(getDatabase() != null && getDatabaseImpl().getDatabaseConnection() != null)
            dbConId = getDatabaseImpl().getDatabaseConnection().getId();
          int buildOrder = -1;
          long newId = designer.savePuakmaTable(id, dbConId, getName(), getDescription(), buildOrder);
          setId(newId);
          
          // SETUP VALIDITY, AND SOME LAST CODE CLEANUP
          if(original != null)
            ((TableImpl)original).copyFromWorkingCopy(this);
          
          // COMMIT ALSO ALL COLUMNS
          TableColumn[] cols = listColumns();
          for(int i = 0; i < cols.length; ++i) {
            //if(cols[i].isDirty())
            cols[i].commit();
          }
          
          // REMOVE COLUMNS TO REMOVE
          for(TableColumnImpl col : columnsToRemove) {
            TableColumnImpl original = (TableColumnImpl) col.original;
            if(original.getTable() != null)
              original.getTable().removeColumn(original);
            else
              VortexPlugin.warning("Original table " + original + " doesn't contain link to table");
          }
        }
        catch(Exception e) {
          if(e instanceof PuakmaCoreException)
            throw (PuakmaCoreException) e;
          else if(e instanceof IOException)
            throw (IOException) e;
          throw PuakmaLibraryUtils.handleException("Cannot save table definition to server", e);
        }
      }
    }
  }

  public void commitRelations() throws PuakmaCoreException, IOException
  {
    if(isWorkingCopy() == false && isNew() == false)
      throw new IllegalStateException("Cannot commit table which is not working copy or new");
  
    commitRelationsInternal();
  }
  
  /**
   * Same as commitRelations, but ignores working copy and new state checks, so
   * it might be used in complex commits from {@link DatabaseImpl}
   */
  void commitRelationsInternal() throws PuakmaCoreException, IOException
  {
    TableColumnImpl[] cols = listColumnImpls();
    // PERFORM SECOND COMMIT ON COLUMNS
    for(int i = 0; i < cols.length; ++i) {
      //if(cols[i].isDirty())
      cols[i].commitRelations();
    }
  }
  
  public boolean isDirty()
  {
    if(super.isDirty())
      return true;
    
    for(TableColumn col : listColumns())
      if(col.isDirty())
        return true;
    
    return false;
  }

  protected void copyFromWorkingCopy(TableImpl workingCopy)
  {
    super.copyFromWorkingCopy(workingCopy);
  }

  /**
   * @return casted reference to DatabaseImpl object
   */
  DatabaseImpl getDatabaseImpl()
  {
    return database;
  }

  public void remove() throws PuakmaCoreException, IOException
  {
    DatabaseImpl db = getDatabaseImpl();
    if(db != null) {
      db.removeObject(this);
    }
  }

  public boolean isOpen()
  {
    if(getApplication() == null)
      return false;
    
    return getApplication().isOpen();
  }

  public boolean isClosed()
  {
    if(getApplication() == null)
      return false;
    
    return getApplication().isClosed();
  }

  public Database getDatabase()
  {
    return database;
  }

  public void drop() throws PuakmaCoreException
  {
    if(isWorkingCopy())
       throw new PuakmaCoreException("Cannot remove working copy");
    if(isNew())
      throw new PuakmaCoreException("Cannot remove new nonexisting table from system database");
    if(isRemoved())
      throw new PuakmaCoreException("Should be already removed!!!");
    
    synchronized(this) {
      try {
        // DELETE TABLE FROM PUAKMA SYSTEM DATABASE
        DatabaseDesigner designer = database.getDatabaseClient();
        designer.deletePuakmaTable(getId());
        
        // TODO: what to do if the table has been removed from puakma eg database, and not from jdbc???
        setRemoved();
        application.notifyRemove(this);
      }
      catch(Exception e) {
        throw PuakmaLibraryUtils.handleException("Cannot remove keyword", e);
      }
    }
  }

  public Table makeWorkingCopy()
  {
    TableImpl wc = new TableImpl(null);
    super.makeCopy(wc);
    wc.original = this;
    
    // WELL, WELL, WELL, MAKE COPIES OF ALL COLUMNS AS WELL
    TableColumn[] cols = listColumns();
    for(int i = 0; i < cols.length; ++i) {
      TableColumnImpl col = (TableColumnImpl) cols[i];
      TableColumnImpl wcCol = (TableColumnImpl) col.makeWorkingCopy();
      wcCol.setTable(wc);
      
      wc.columns.add(wcCol);
    }
    
    return wc;
  }
  
  public TableColumn addColumn(String name, String type) throws PuakmaCoreException, IOException
  {
    // TODO: add column type checking here - is that really necessary???
    synchronized(columns) {
      TableColumnImpl col = new TableColumnImpl(this);
      col.setName(name);
      col.parseAndSetType(type);
      
      addColumn(col);
      
      return col;
    }
  }
  
  public void addColumn(TableColumn column) throws PuakmaCoreException, IOException
  {
    addColumn(-1, column);
  }
  
  public void addColumn(int insertionIndex, TableColumn column) throws PuakmaCoreException, IOException
  {
    synchronized(columns) {
      // CHECK IF THIS COLUMN EXISTS IN TABLE OR NOT. IF IT DOES, THROW AN EXCEPTION
      if(getColumn(column.getName()) != null)
        throw new VortexDatabaseException("Column " + name + " already exists in the table");
      
      TableColumnImpl ci = (TableColumnImpl) column;
      
      ci.setTable(this);
      
      if(insertionIndex == -1)
        insertionIndex = columns.size();
      
      columns.add(insertionIndex, (TableColumnImpl) column);
      updatePositionForAllColumns();
      
      if(this.isWorkingCopy() == false && this.isNew() == false) {
        try {
          saveAllColumns();
        }
        // ON ERROR REMOVE THE NEW COLUMN FROM TABLES LIST
        // THERE ARE MORE HANDLERS TO BE A LITTLE BIT MORE PRECISIOUS FOR THE
        // FUTURE
        catch(RuntimeException ex) {
          columns.remove(insertionIndex);
          throw ex;
        }
        catch(PuakmaCoreException ex) {
          columns.remove(insertionIndex);
          throw ex;
        }
        catch(IOException ex) {
          columns.remove(insertionIndex);
          throw ex;
        }
      }
      
      setDirty(false);

      fireEvent(PROP_COLUMN_ADD, null, column);
      DatabaseConnectionImpl dbCon = (DatabaseConnectionImpl) database.getDatabaseConnection();
      if(dbCon != null) {
        ApplicationImpl app = (ApplicationImpl) dbCon.getApplication();
        app.fireAddObject(column, false);
      }
    }
  }

  /**
   * This function updates position for all columns in the table. This just
   * reshufles the indexes.
   */
  private void updatePositionForAllColumns()
  {
    int i = 0;
    for(TableColumnImpl c : columns)
      c.intSetPosition(i++);
  }

  public void removeColumn(TableColumn column) throws VortexDatabaseException, IOException
  {
    TableColumnImpl col = (TableColumnImpl) column;
    
    synchronized(columns) {
      if(col.getTable() != this)
        throw new VortexDatabaseException("Trying to remove column " + col.getName()
                                          + " from an invalid table");
      
      if(isWorkingCopy() || isNew()) {
        int index = columns.indexOf(column);
        if(columns.remove(index) == null)
          throw new VortexDatabaseException("Iternal error - cannot find column " + column.getName()
                                            + " in the list of columns");
        
        // DO NOT ADD NEW COLUMNS TO THE COLUMNS TO REMOVE LIST
        if(column.isNew() == false)
          columnsToRemove.add(col);
      }
      else {
        try {
          col.intSendRemoveCommand();
          columns.remove(col);
        }
        catch(SOAPFaultException ex) {
          throw new VortexDatabaseException(ex);
        }
      }
      
      col.setRemoved();
      col.setTable(null);
      fireEvent(PROP_COLUMN_REMOVE, column, null);
      DatabaseConnectionImpl dbCon = (DatabaseConnectionImpl) database.getDatabaseConnection();
      if(dbCon != null) {
        ApplicationImpl app = (ApplicationImpl) dbCon.getApplication();
        app.fireRemoveEvent(column, false);
      }
    }
  }

  public void moveColumn(int sourceIndex, int destinationIndex) throws PuakmaCoreException, IOException
  {
    assert sourceIndex >= 0 && destinationIndex >= 0;
    // IGNORE IF SOURCE IS THE SAME AS DESTINATION
    if(sourceIndex == destinationIndex)
      return;
    
    synchronized(columns) {
      int size = columns.size();
      assert sourceIndex < size && destinationIndex < size;
      
      TableColumnImpl col = columns.remove(sourceIndex);
      if(isWorkingCopy() || isNew())
        columns.add(destinationIndex, col);
      else {
        columns.add(destinationIndex, col);
        
        // ALSO CHANGE POSITION FOR THE COLUMNS
        updatePositionForAllColumns();
        
        saveAllColumns();
      }
      setDirty(true);
      
      fireEvent(PROP_COLUMN_MOVE, sourceIndex, destinationIndex);
    }
  }
  
  /**
   * This saves all columns in one shot.
   * @throws PuakmaCoreException 
   * @throws IOException 
   */
  private void saveAllColumns() throws PuakmaCoreException, IOException
  {
    DatabaseDesigner designer = getDatabaseImpl().getDatabaseClient();
    TableColumnDataBean[] beans = new TableColumnDataBean[columns.size()];
    for(int i = 0; i < beans.length; ++i) {
      TableColumnImpl col = columns.get(i);
      beans[i] = new TableColumnDataBean();
      col.setupBean(beans[i]);
    }
    
    long[] minusOneArray = new long[beans.length];
    Arrays.fill(minusOneArray, -1);
    
    try {
      Class<TableColumnDataBean> c = TableColumnDataBean.class;
      designer.savePuakmaAttributes(
          ClassUtil.getLongArrayFromObjects(beans, c.getDeclaredField("attId")),
          ClassUtil.getLongArrayFromObjects(beans, c.getDeclaredField("tableId")),
          (String[]) ClassUtil.getObjectArrayFromObjects(beans, c.getDeclaredField("attName")),
          (String[]) ClassUtil.getObjectArrayFromObjects(beans, c.getDeclaredField("type")),
          (String[]) ClassUtil.getObjectArrayFromObjects(beans, c.getDeclaredField("typeSize")),
          ClassUtil.getBoolArrayFromObjects(beans, c.getDeclaredField("allowNull")),
          ClassUtil.getBoolArrayFromObjects(beans, c.getDeclaredField("isPk")),
          ClassUtil.getBoolArrayFromObjects(beans, c.getDeclaredField("isAutoIncrement")),
          ClassUtil.getBoolArrayFromObjects(beans, c.getDeclaredField("isUnique")),
          ClassUtil.getBoolArrayFromObjects(beans, c.getDeclaredField("isFtIndex")),
          (String[]) ClassUtil.getObjectArrayFromObjects(beans, c.getDeclaredField("refTable")),
          minusOneArray,
          //ClassUtil.getLongArrayFromObjects(beans, c.getDeclaredField("refColumn")),
          (String[]) ClassUtil.getObjectArrayFromObjects(beans, c.getDeclaredField("extraOptions")),
          ClassUtil.getBoolArrayFromObjects(beans, c.getDeclaredField("cascadeDelete")),
          (String[]) ClassUtil.getObjectArrayFromObjects(beans, c.getDeclaredField("description")),
          (String[]) ClassUtil.getObjectArrayFromObjects(beans, c.getDeclaredField("defaultValue")),
          ClassUtil.getIntArrayFromObjects(beans, c.getDeclaredField("position")),
          ClassUtil.getBoolArrayFromObjects(beans, c.getDeclaredField("cascadeUpdate")));
    }
    catch(SOAPFaultException e) {
      throw new PuakmaCoreException("Cannot save columns in table", e);
    }
    catch(SecurityException e) {
      throw new PuakmaCoreException(e);
    }
    catch(NoSuchFieldException e) {
      throw new PuakmaCoreException(e);
    }
  }

  public void moveColumn(TableColumn column, int destIndex) throws PuakmaCoreException, IOException
  {
    synchronized(columns) {
      int index = columns.indexOf(column);
      assert index == -1 : "Column " + column + " doesn't belong to the table " + getName();
      moveColumn(index, destIndex);
    }
  }

  public int countColumns()
  {
    synchronized(columns) {
      return columns.size();
    }
  }

  /**
   * This function adjusts links to the working copy
   */
  public void adjustWorkingCopy()
  {
    if(isWorkingCopy() == false)
      throw new IllegalStateException("You have to call this function only in working copy");
    
    for(TableColumnImpl col : columns)
      col.adjustWorkingCopy();
  }

  void intRemove() throws IOException, SOAPFaultException, PuakmaCoreException
  {
    if(isWorkingCopy() || isNew()) {
      TableImpl t = (TableImpl) original;
      t.remove();
    }
    else {
      DatabaseDesigner designer = getDatabaseImpl().getDatabaseClient();
      designer.deletePuakmaTable(getId());
    }
  }

  public void storeToXml(SimpleXmlWriter writer)
  {
    //m.put(PuakmaXmlCodes.ATT_ORDER, get
    //m.put(PuakmaXmlCodes.ATT_CATALOG_NAME, getC
    //m.put(PuakmaXmlCodes.ATT_SCHEMA,
    //m.put(PuakmaXmlCodes.ATT_TYPE, getTy
    
    String[] attribs = {PuakmaXmlCodes.ATT_DESC, PuakmaXmlCodes.ATT_NAME };
    String[] values = {getDescription(), getName()};
    
    writer.addTag(PuakmaXmlCodes.ELEM_TABLE, attribs, values, false);
    
    TableColumnImpl[] cols = listColumnImpls();
    for(TableColumnImpl col : cols) {
      col.storeToXml(writer);
    }
    
    writer.closeTag(PuakmaXmlCodes.ELEM_TABLE);
  }

  public List<FkConnection> getTargetConnections()
  {
    synchronized(targetConnections) {
      return new ArrayList<FkConnection>(targetConnections);
    }
  }

  public void addTargetConnection(FkConnectionImpl connection)
  {
    synchronized(targetConnections) {
      if(targetConnections.contains(connection) == false)
        targetConnections.add(connection);
    }
    
    fireEvent(PROP_TARGET_CONNECTION_CHANGE, null, connection);
  }

  public void removeTargetConnection(FkConnectionImpl connection)
  {
    synchronized(targetConnections) {
      targetConnections.remove(connection);
    }
    
    fireEvent(PROP_TARGET_CONNECTION_CHANGE, connection, null);
  }
}
