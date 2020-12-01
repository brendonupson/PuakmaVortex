/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Dec 20, 2005
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import puakma.SOAP.SOAPFaultException;
import puakma.coreide.TableImpl.TableColumnDataBean;
import puakma.coreide.database.DatabaseSchemeBean.DbColumn;
import puakma.coreide.designer.DatabaseDesigner;
import puakma.coreide.designer.PuakmaXmlCodes;
import puakma.coreide.objects2.Database;
import puakma.coreide.objects2.FkConnection;
import puakma.coreide.objects2.Table;
import puakma.coreide.objects2.TableColumn;
import puakma.utils.SimpleXmlWriter;
import puakma.utils.lang.StringUtil;
import puakma.vortex.VortexPlugin;

class TableColumnImpl extends ApplicationObjectImpl implements TableColumn, PropertyChangeListener
{
  private static final String PARSE_TYPE_REGEX = "^([a-zA-Z][a-zA-Z]*)$";
  private static final String PARSE_TYPE_WITH_SIZE_REGEX = "^([a-zA-Z][a-zA-Z]*)\\s*\\(\\s*([0-9]+)\\s*\\)$";
  private static final String PARSE_TYPE_WITH_TWO_SIZES_REGEX = "^([a-zA-Z][a-zA-Z]*)\\s*\\(\\s*([0-9]+)\\s*,\\s*([0-9]+)\\s*\\)$";
  private static final Pattern PARSE_TYPE_PATTERN = Pattern.compile(PARSE_TYPE_REGEX);
  private static final Pattern PARSE_TYPE_WITH_SIZE_PATTERN = Pattern.compile(PARSE_TYPE_WITH_SIZE_REGEX);
  private static final Pattern PARSE_TYPE_WITH_TWO_SIZES_PATTERN = Pattern.compile(PARSE_TYPE_WITH_TWO_SIZES_REGEX);
  
  /**
   * The table associated with this column.
   */
  private TableImpl table;
  
  /**
   * Position of the column in the table.
   */
  private int position;

  private int decimalDigits;

  private int typeSize;
  
  /**
   * Specifies the DELETE action on foreign key
   */
  private int fkDeleteAction = CASCADE_ACTION_NONE;

  private String type = "";
  private boolean isPk;
  private boolean autoInc;
  private boolean uniq;
  private boolean allowsNull;
  private String defaultValue = "";
  private FkConnectionImpl refConnection;

  public TableColumnImpl(TableImpl table)
  {
    super(table != null ? (ApplicationImpl) table.getApplication() : null);
    
    this.table = table;
  }
  
  public void setTable(Table table)
  {
    synchronized(this) {
      this.table = (TableImpl) table;
    }
  }

  public void commit() throws PuakmaCoreException, IOException
  {
    // IGNORE NEW TABLES - WE WILL COMMIT NEW TABLES LATER...
    if(isWorkingCopy() == false && isNew() == false)
      throw new IllegalStateException("Cannot commit database which is not working copy or new");
    
    intCommit();
  }
   
  void intCommit() throws PuakmaCoreException, IOException
  {
    synchronized(this) {
      if(isNew() && table.isWorkingCopy()) {
        TableImpl origTable = (TableImpl) table.original;
        TableColumnImpl o = (TableColumnImpl) makeWorkingCopy();
        o.original = null;
        o.status = STATUS_NEW;
        origTable.addColumn(o);
        this.original = o;
        this.id = o.getId();
        setValid();
      }
      else {
        try {
          DatabaseDesigner designer = ((TableImpl)getTable()).getDatabaseImpl().getDatabaseClient();
          // TODO: how to deal with broken database client???
          if(designer == null)
            return;
          boolean isFtIndex = false;
          String refTable = "", extraOptions = "", defaultValue = "";
          int fkUpdateAction = 0;
          long newId = designer.savePuakmaAttribute2(getId(), table.getId(), getName(), getType(),
                                                     getSqlTypeSize(), allowsNull(), isPk(),
                                                     isAutoInc(), isUnique(), isFtIndex, refTable,
                                                     -1, extraOptions, getFkDeleteAction(),
                                                     fkUpdateAction, getDescription(),
                                                     defaultValue, getPosition());
          if(getId() != newId)
            setId(newId);
          if(isNew()) {
            setId(newId);
          }
          // SETUP VALIDITY, AND SOME LAST CODE CLEANUP
          if(original != null)
            ((TableColumnImpl)original).copyFromWorkingCopy(this);
  
          setValid();
        }
        catch(Exception e) {
          throw PuakmaLibraryUtils.handleDbException("Cannot save application to server", e);
        }
      }
    }
  }
  
  protected void copyFromWorkingCopy(TableColumnImpl workingCopy)
  {
    super.copyFromWorkingCopy(workingCopy);
    
    intSetPosition(workingCopy.getPosition());
    setFloatDecimals(workingCopy.getFloatDecimals());
    setTypeSize(workingCopy.getTypeSize());
    setType(workingCopy.getType());
    setAutoInc(workingCopy.isAutoInc());
    setUnique(workingCopy.isUnique());
    setAllowsNull(workingCopy.allowsNull());
    setPk(workingCopy.isPk());
    setDefaultValue(workingCopy.getDefaultValue());
    
    // OK, NOW WE SHOULD COPY FROM WORKING COPY THE FK STUFF. NOTE THAT IF WE COMMIT, WE SHOULD ALSO
    // BE VERY CAREFUL ABOUT FKs.
    if(workingCopy.refConnection != null) {
      TableImpl table = (TableImpl) refConnection.getTargetTable();
      if(table.isWorkingCopy() && table.isNew() == false)
        setRefTable((Table) table.original);
      else
        setRefTable(table);
    }
  }

  /**
   * This function creates a String with the full specification of the type size. So for FLOAT(11,1)
   * it will write "11,1", and for VARCHAR(255) and INTEGER.
   * 
   * @return String with combined specification of size
   */
  public String getFullTypeName()
  {
    StringBuffer sb = new StringBuffer();
    sb.append(getType());
    if(getTypeSize() != -1 && getTypeSize() != 0) {
      sb.append("(");
      sb.append(getTypeSize());
      int decimals = getFloatDecimals();
      if(decimals != -1 && decimals != 0) {
        sb.append(",");
        sb.append(decimals);
      }
      sb.append(")");
    }
    return sb.toString();
  }
  
  private String getSqlTypeSize()
  {
    StringBuffer sb = new StringBuffer();
    if(getTypeSize() != -1 && getTypeSize() != 0) {
      sb.append(getTypeSize());
      int decimals = getFloatDecimals();
      if(decimals != -1 && decimals != 0) {
        sb.append(",");
        sb.append(decimals);
      }
    }
    return sb.toString();
  }

  public void remove() throws PuakmaCoreException
  {
    throw new IllegalStateException("Not implemented yet");
  }

  public void setup(DbColumn bean)
  {
    setId(bean.id);
    setName(bean.name);
    setDescription(bean.description);
    intSetPosition(bean.position);
    setType(bean.typeName);
    setTypeSize(bean.columnSize);
    setFloatDecimals(bean.decimalDigits);
    setPk(bean.isPk);
    setUnique(bean.isUniq);
    setAutoInc(bean.autoInc);
    setDefaultValue(bean.defaultValue);
    setAllowsNull(bean.isNullable);

    setValid();
  }
  
  /**
   * Sets the type of the column. Note that type translated to the sql type defined in java is not
   * really proper for our purpose.
   * 
   * @param typeName
   */
  public void setType(String typeName)
  {
    if(typeName == null)
      throw new IllegalArgumentException("Type name parameter cannot be null");
    
    if(typeName.equalsIgnoreCase(this.type) == false) {
      String oldType = this.type;
      this.type = typeName;
      setDirty(true);
      fireEvent(PROP_TYPE, oldType, this.type);
    }
//    for(int i = 0; i < OFFICIAL_TYPE_NAMES.length; ++i) {
//      if(OFFICIAL_TYPE_NAMES[i].equalsIgnoreCase(typeName)) {
//        sqlType = OFFICIAL_TYPE_CONSTS[i];
//        return;
//      }
//    }
//    sqlType = Types.OTHER;
  }

  public void setTypeSize(int columnSize)
  {
    // TODO: check the argument if it is valid
    if(columnSize != typeSize) {
      int oldTypeSize = this.typeSize;
      this.typeSize = columnSize;
      setDirty(true);
      fireEvent(PROP_TYPE, oldTypeSize, this.typeSize);
    }
  }
  
  public int getFkDeleteAction()
  {
    return fkDeleteAction;
  }

  public void setFkDeleteAction(int fkDeleteAction)
  {
    if(fkDeleteAction < 0 || fkDeleteAction > 4)
      throw new IllegalArgumentException("Invalid foreign key delete action");
    
    if(fkDeleteAction != this.fkDeleteAction) {
      int oldVal = this.fkDeleteAction;
      this.fkDeleteAction = fkDeleteAction;
      setDirty(true);
      fireEvent(PROP_FK_DELETE_ACTION, oldVal, fkDeleteAction);
    }
  }

  public void setFloatDecimals(int decimals)
  {
    // TODO: check the argument validity
    if(this.decimalDigits != decimals) {
      int oldValue = decimals;
      this.decimalDigits = decimals;
      setDirty(true);
      fireEvent(PROP_DECIMAL_DIGITS, oldValue, decimals);
    }
  }
  
  public boolean allowsNull()
  {
    return allowsNull;
  }

  public String getDefaultValue()
  {
    return defaultValue;
  }

  public void setAllowsNull(boolean allow)
  {
    if(this.allowsNull != allow) {
      this.allowsNull = allow;
      setDirty(true);
      fireEvent(PROP_ALLOWS_NULL, !allow, allow);
    }
  }

  public void setDefaultValue(String defValue)
  {
    if(defValue == null)
      throw new IllegalArgumentException("Default value parameter cannot be null. If you want to set default value to NULL, use please setNullDefaultValue function");
    // TODO: add setNUllDefaultVallue function
    if(this.defaultValue.equals(defValue) == false) {
      String oldValue = this.defaultValue;
      this.defaultValue = defValue;
      setDirty(true);
      fireEvent(PROP_DEFAULT_VALUE, oldValue, defaultValue);
    }
  }

  void intSetPosition(int position)
  {
    // TODO: check range here
    
    if(this.position != position) {
      int oldValue = this.position;
      this.position = position;
      setDirty(true);
      fireEvent(PROP_POSITION, oldValue, position);
    }
  }

  public String toString()
  {
    return getName() + " : " + getFullTypeName();
  }

  public int getPosition()
  {
    return position;
  }

  public Table getTable()
  {
    return table;
  }

  public String getType()
  {
    return type;
  }

  public int getTypeSize()
  {
    return typeSize;
  }

  public int getFloatDecimals()
  {
    return decimalDigits;
  }

  public TableColumn makeWorkingCopy()
  {
    TableColumnImpl wc = new TableColumnImpl(null);
    
    super.makeCopy(wc);
    
    wc.position = this.position;
    wc.decimalDigits = this.decimalDigits;
    wc.typeSize = this.typeSize;
    wc.type = this.type;
    wc.allowsNull = this.allowsNull;
    wc.defaultValue = this.defaultValue;
    wc.isPk = this.isPk;
    wc.uniq = this.uniq;
    wc.autoInc = this.autoInc;
    
    wc.original = this;

    wc.refConnection = this.refConnection;
    
    return wc;
  }
  
  public void parseAndSetType(String type) throws VortexDatabaseException
  {
    Matcher m = PARSE_TYPE_PATTERN.matcher(type);
    if(m.find()) {
      setType(type);
      return;
    }
    m = PARSE_TYPE_WITH_SIZE_PATTERN.matcher(type);
    if(m.find()) {
      setType(m.group(1));
      setTypeSize(Integer.parseInt(m.group(2)));
      return;
    }
    m = PARSE_TYPE_WITH_TWO_SIZES_PATTERN.matcher(type);
    if(m.find()) {
      setType(m.group(1));
      setTypeSize(Integer.parseInt(m.group(2)));
      setFloatDecimals(Integer.parseInt(m.group(3)));
      return;
    }
    
    throw new VortexDatabaseException("Cannot parse type of the database column - " + type);
  }

  /**
   * Sends internal remove command to the server.
   */
  public void intSendRemoveCommand() throws IOException, SOAPFaultException
  {
    if(isNew())
      throw new IllegalStateException("Cannot remove new object from the server");
    
    DatabaseDesigner designer = table.getDatabaseImpl().getDatabaseClient();
    designer.deletePuakmaAttribute(getId());
  }
  
//  public int getSqlType()
//  {
//    return sqlType;
//  }
  
  /**
   * This function sets up bean for saving all columns from the table together. It's simple trick
   * to speed up processing 
   */
  void setupBean(TableColumnDataBean bean)
  {
    bean.attId = getId();
    bean.tableId = getTable().getId();
    bean.attName = getName();
    bean.type = getType();
    bean.typeSize = Integer.toString(getTypeSize());
//    bean.allowNull;
    bean.isPk = isPk;
    bean.isAutoIncrement = autoInc;
    bean.isUnique = uniq;
//    bean.isFtIndex;
//    bean.extraOptions;
//    bean.cascadeDelete;
    bean.description = getDescription();
//    bean.defaultValue;
    bean.position = getPosition();
//    bean.cascadeUpdate;

    if(refConnection != null)
      bean.refTable = Long.toString(refConnection.getTargetTable().getId());
    else
      bean.refTable = null;
  }

  public Table getRefTable()
  {
    if(refConnection != null)
      return refConnection.getTargetTable();
    else
      return null;
  }

  public void setRefTable(Table table)
  {
    synchronized(this) {
      if((this.refConnection == null && table != null)
         || (this.refConnection != null && this.refConnection.getTargetTable() != table)) {
        FkConnectionImpl oldConnection = this.refConnection;
        
        if(table != null)
          refConnection = new FkConnectionImpl(this, (TableImpl) table);
        else
          refConnection = null;
        
        fireEvent(PROP_REFERENCED_TABLE, oldConnection, refConnection);
        
        // ALSO NOTIFY THAT SOME TARGET CONNECTION CHANGED IN THE REFERENCED TABLE
        if(oldConnection != null)
          oldConnection.disposeConnection();
        if(refConnection != null)
          refConnection.fireNewConnection();
      }
    }
  }
  
  public FkConnection getRefConnection()
  {
    return refConnection;
  }

  public boolean isFk()
  {
    return refConnection != null;
  }

  public boolean isPk()
  {
    return isPk;
  }
  
  public void setPk(boolean isPk)
  {
    // TODO: check if pk is valid here
    
    if(this.isPk != isPk) {
      boolean oldPk = this.isPk;
      this.isPk = isPk;
      
      fireEvent(PROP_PK, oldPk, isPk);
    }
  }

  public void commitRelations() throws PuakmaCoreException, IOException
  {
//    if(refTable != null) {
//      if(refTable.isNew())
//        throw new IllegalStateException("Cannot commit relation to the table which is not on the server yet");
//      
//      long refTableId = refTable.getId();
//      DatabaseDesigner designer = table.getDatabaseImpl().getDatabaseClient();
//      try {
//        designer.savePuakmaTableFKs(getId(), refTableId, -1);
//      }
//      catch(SOAPFaultException e) {
//        throw new PuakmaCoreException(e);
//      }
//    }
  
    // TODO: what to do with this request? there are two places from where is
    // this called - when commiting database wc, and also when removing table
    // for some unspecified reason
    intCommitRelations();
  }
  
  void intCommitRelations() throws IOException, PuakmaCoreException
  {
    long idRef = -1;
    if(refConnection != null)
      idRef = refConnection.getTargetTable().getId();
    
    DatabaseDesigner designer = table.getDatabaseImpl().getDatabaseClient();
    try {
      designer.savePuakmaTableFKs(getId(), idRef, -1);
    }
    catch(SOAPFaultException e) {
      throw new PuakmaCoreException(e);
    }
  }

  public boolean isUnique()
  {
    return uniq;
  }

  public void setUnique(boolean uniq)
  {
    if(this.uniq != uniq) {
      boolean oldUniq= this.uniq;
      this.uniq = uniq;
      
      fireEvent(PROP_PK, oldUniq, uniq);
    } 
  }

  public boolean isAutoInc()
  {
    return autoInc;
  }

  public void setAutoInc(boolean autoInc)
  {
    // TODO: check if the table column can be auto incremental
    if(this.autoInc != autoInc) {
      boolean oldAutoInc = this.autoInc;
      this.autoInc = autoInc;
      
      fireEvent(PROP_PK, oldAutoInc, autoInc);
    }
  }

  public void adjustWorkingCopy()
  {
    if(isWorkingCopy() == false)
      throw new IllegalStateException("You have to call this function only in working copy");
    
    Table t = getTable();
    if(t.isWorkingCopy() == false)
      return;
    
    Database db = t.getDatabase();
    if(db.isWorkingCopy() == false)
      return;
    
    if(refConnection != null) {
      TableImpl rTable = (TableImpl) refConnection.getTargetTable();
      if(rTable != null && rTable.isWorkingCopy() == false) {
        String refTableName = rTable.getName();
        refConnection = new FkConnectionImpl(this, (TableImpl) db.getTable(refTableName));
      }
    }
  }

  /**
   * Setups the referenced table
   *
   * @param fkTable is the table we want to reference
   */
  void setupReferenceTable(long fkTable)
  {
    DatabaseImpl db = table.getDatabaseImpl();
    TableImpl table = (TableImpl) db.getTable(fkTable);
    if(table != null)
      setRefTable(table);
  }
  
  public void setupReferenceTable(String fkTableName)
  {
    DatabaseImpl db = table.getDatabaseImpl();
    TableImpl table = (TableImpl) db.getTable(fkTableName);
    if(table != null)
      setRefTable(table);
  }

  public void propertyChange(PropertyChangeEvent evt)
  {
    // TODO: remove property change listener on this, and replace the listening to
    // application to direct calls
//    String propName = evt.getPropertyName();
//    if(Database.PROP_REMOVE_TABLE.equals(propName)) {
//      Table removedTable = (Table) evt.getNewValue();
//      if(removedTable == this.refTable) {
//        setRefTable(null);
//        if(this.isWorkingCopy() == false && this.isNew() == false) {
//          try {
//            intCommitRelations();
//          }
//          catch(Exception e) {
//            VortexPlugin.log(e);
//          }
//        }
//      }
//    }
  }

  public void storeToXml(SimpleXmlWriter writer)
  {
    Map<String, String> m = new HashMap<String, String>();
    
    m.put(PuakmaXmlCodes.ATT_NAME, getName());
    //m.put(PuakmaXmlCodes.ATT_COLUMN_SIZE, );
    if(getFloatDecimals() > 0)
      m.put(PuakmaXmlCodes.ATT_DECIMAL_DIGITS, Integer.toString(getFloatDecimals()));
    m.put(PuakmaXmlCodes.ATT_DEFAULT, getDefaultValue());
    m.put(PuakmaXmlCodes.ATT_DESC, getDescription());
    //m.put(PuakmaXmlCodes.ATT_NULLABLE, Boolean.toString(is));
    m.put(PuakmaXmlCodes.ATT_POSITION, Integer.toString(getPosition()));
    //column.radix = parseInt(atts.getValue(PuakmaXmlCodes.ATT_RADIX), -1);
    //m.put(PuakmaXmlCodes.ATT_DATA_TYPE, getType());
    //m.put(PuakmaXmlCodes.ATT_TYPE_NAME, getType());
    m.put(PuakmaXmlCodes.ATT_TYPE, getType());
//    m.put(PuakmaXmlCodes.ATT_ID, Long.toString(getId()));
    if(refConnection != null && refConnection.getTargetTable() != null) {
      String fkTableName = refConnection.getTargetTable().getName();
      m.put(PuakmaXmlCodes.ATT_FK_TABLE_NAME, fkTableName);
    }
    if(isPk())
      m.put(PuakmaXmlCodes.ATT_IS_PK, Boolean.toString(isPk()));
    if(isUnique())
      m.put(PuakmaXmlCodes.ATT_IS_UNIQ, Boolean.toString(isUnique()));
    if(isAutoInc())
      m.put(PuakmaXmlCodes.ATT_AUTO_INCREMENT, Boolean.toString(isAutoInc()));
    //column.options = StringUtil.safeString(atts.getValue(PuakmaXmlCodes.ATT_EXTRA_OPTIONS));
    m.put(PuakmaXmlCodes.ATT_TYPE_SIZE, Integer.toString(getTypeSize()));
    
    writer.addTag(PuakmaXmlCodes.ELEM_COLUMN, m, true);
  }
}
