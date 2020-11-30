/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 1, 2006
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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import puakma.SOAP.SOAPFaultException;
import puakma.coreide.designer.DatabaseDesigner;
import puakma.coreide.objects2.Database;
import puakma.coreide.objects2.ServerObject;
import puakma.coreide.objects2.Table;
import puakma.coreide.objects2.TableColumn;

public class TestDatabaseWorkingCopy extends BaseTest implements InvocationHandler
{
  boolean savedTableTestTable = false;
  boolean savedTableGagaTable = false;
  boolean createdTableTestTable = false;
  boolean createdTableGagaTable = false;
  DatabaseImpl db;
  public boolean createColName;
  public boolean createColAge;
  public boolean createColTestTableId;
  public int saveColAge;
  public int saveColName;
  public int saveColTestTable;

    public synchronized long savePuakmaTable(long idTable, long idDbCon, String tableName, String description, int buildOrder) throws SOAPFaultException, IOException
    {
      if(idTable == -1) {
        if(tableName.equalsIgnoreCase("TESTTABLE")) {
          createdTableTestTable = true;
          return 1;
        }
        else if(tableName.equalsIgnoreCase("GAGATABLE")) {
          createdTableGagaTable = true;
          return 2;
        }
        else
          throw new IllegalStateException("Invalid table name " + tableName);
      }
      
      if(idTable == 1)
        savedTableTestTable = true;
      else if(idTable == 2)
        savedTableGagaTable = true;
      else
        throw new IllegalStateException("Invalid table id");
      return idTable;
    }

    public long savePuakmaAttribute(long attId, long tableId, String attName, String type,
                                    String typeSize, boolean allowNull, boolean isPk,
                                    boolean isAutoIncrement, boolean isUnique, boolean isFtIndex,
                                    String refTable, String extraOptions, boolean cascadeDelete,
                                    String description, String defaultValue, int position,
                                    boolean cascadeUpdate) throws SOAPFaultException, IOException
    {
      if(attId == -1) {
        if(attName.equalsIgnoreCase("TestTableId")) {
          createColTestTableId = true;
          return 1;
        }
        else if(attName.equalsIgnoreCase("name")) {
          createColName = true;
          return 2;
        }
        else if(attName.equalsIgnoreCase("age")) {
          createColAge = true;
          return 3;
        }
      }
      
      if(attId == 1)
        saveColTestTable++;
      else if(attId == 2)
        saveColName++;
      else if(attId == 3)
        saveColAge++;
      return attId;
    }
  
  protected void setUp() throws Exception
  {
    db = new DatabaseImpl(null);
    ConnectionPrefs prefs = createEmptyPrefs();
    DatabaseDesigner designer = (DatabaseDesigner) Proxy.newProxyInstance(getClass().getClassLoader(),
                                                                          new Class[] { DatabaseDesigner.class },
                                                                          this);
    DesignerFactory factory = new DesignerFactoryImpl(designer);
    factory.setupConnectionPreferences(prefs);
    db.setDesignerFactory(factory);
  }

  public void testTableUniqueness() throws Exception
  {
    Table table = ObjectsFactory.createTable("TESTTABLE");
    TableColumn column = table.addColumn("TestTableID", "INTEGER");
    column = table.addColumn("name", "VARCHAR(255)");
    column = table.addColumn("age", "FLOAT(23,5)");
    db.addObject(table);
    table = ObjectsFactory.createTable("GAGATABLE");
    db.addObject(table);
    
    table = ObjectsFactory.createTable("testTable");
    try {
      db.addObject(table);
      assertTrue("Table shouldn't be add to database", false);
    }
    catch(Exception ex) {
      // DONOTHING...
    }
  }
  
  private boolean testTableNameChanged = false;
  private boolean testTableNameWCChanged = false;
  private boolean testTablePropAdd = false;
  private boolean wcAddedTablePropAdd = false;

  public void testWC1() throws Exception
  {
    db.setNew(false);
    db.addListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt)
      {
        if(Database.PROP_ADD_TABLE.equals(evt.getPropertyName())) {
          Table table = (Table) evt.getNewValue();
          if(table.getName().equalsIgnoreCase("TestTable"))
            testTablePropAdd = true;
          else if(table.getName().equalsIgnoreCase("AddedTable"))
            wcAddedTablePropAdd = true;
        }
      }
    });
    Table table = ObjectsFactory.createTable("TESTTABLE");
    table.addListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        if(ServerObject.PROP_NAME.equals(evt.getPropertyName())) {
          testTableNameChanged = true;
        }
      }
    });
    TableColumn column = table.addColumn("TestTableID", "INTEGER");
    assertEquals(false, createColTestTableId);
    column = table.addColumn("name", "VARCHAR(255)");
    column = table.addColumn("age", "FLOAT(23,5)");
    db.addObject(table);
    assertTrue(testTablePropAdd);
    assertTrue(column.isDirty());
    
    assertTrue(createdTableTestTable);
    assertEquals(true, createColTestTableId);
    
    table = ObjectsFactory.createTable("GAGATABLE");
    db.addObject(table);
    assertTrue(createdTableGagaTable);    
    
    Database wc = db.makeWorkingCopy();
    
    // AT FIRST CHECK ALL USED TABLES
    Table[] tables = wc.listTables();
    assertEquals(2, tables.length);
    
    // AND NOW CHECK ALL COLUMNS FROM TABLES
    table = wc.getTable("TestTable");
    assertNotNull(table);
    TableColumn col = table.getColumn("name");
    assertNotNull(col);
    assertEquals(255, col.getTypeSize());
    col = table.getColumn("age");
    assertNotNull(col);
    assertEquals(23, col.getTypeSize());
    assertEquals(5, col.getFloatDecimals());
    
    // AND AS WELL MODIFY SOMETHING AND TRY TO UPLOAD IT
    table = wc.getTable("TestTable");
    table.addListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        if(ServerObject.PROP_NAME.equals(evt.getPropertyName())) {
          testTableNameWCChanged = true;
        }
      }
    });
    table.setName("TestTable1");
    assertTrue(testTableNameWCChanged);
    col = table.getColumn("age");
    col.setName("age1");
    col.setDescription("description");
    col.setFloatDecimals(11);
    col.setTypeSize(12);
    col.setType("VARCHAR");
    table.commit();
    
    // ALSO ADD A NEW TABLE TO WORKING COPY - THIS CANNOT COMMIT THIS TABLE TO DATABASE!!!
    Table testTable = ObjectsFactory.createTable("AddedTable");
    wc.addObject(testTable);
    assertFalse(wcAddedTablePropAdd);
    
    assertTrue(savedTableTestTable);
    assertTrue(testTableNameChanged);
  }
  
  int addAhojCol = 0;
  int remAhojCol = 0;
  
  /**
   * This test tests adding/removing columns from table
   * @throws Exception
   */
  public void testWC3() throws Exception
  {
    Table table = ObjectsFactory.createTable("TESTTABLE");
    TableColumn column = table.addColumn("TestTableID", "INTEGER");
    column = table.addColumn("name", "VARCHAR(255)");
    column = table.addColumn("age", "FLOAT(23,5)");
    db.addObject(table);
    table = ObjectsFactory.createTable("GAGATABLE");
    db.addObject(table);
    
    Database wc = db.makeWorkingCopy();
    table = wc.getTable("TESTTABLE");
    table.addListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt)
      {
        if(evt.getPropertyName().equals(Table.PROP_COLUMN_ADD)) {
          if(((TableColumn)evt.getNewValue()).getName().equalsIgnoreCase("AhojColumn"))
            addAhojCol++;
        }
        else if(evt.getPropertyName().equals(Table.PROP_COLUMN_REMOVE)) {
          if(((TableColumn)evt.getOldValue()).getName().equalsIgnoreCase("AhojColumn1"))
            remAhojCol++;
        }
        throw new IllegalArgumentException("Invalid column added/removed");
      }
    });
    TableColumn col = table.addColumn("AhojColumn", "INTEGER");
    assertEquals(1, addAhojCol);
    col.setName("AhojColumn1");
    table.removeColumn(col);
    assertEquals(1, remAhojCol);
  }

  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
  {
    if(method.getName().equals("savePuakmaTable")) {
      return BaseTest.execute("savePuakmaTable", this, args);
    }
    else if(method.getName().equals("savePuakmaAttribute")) {
      return BaseTest.execute("savePuakmaAttribute", this, args);
    }
    return null;
  }
}
