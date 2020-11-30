/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    08/05/2006
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

import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;
import puakma.coreide.DatabaseImpl;
import puakma.coreide.ObjectsFactory;
import puakma.coreide.objects2.Database;
import puakma.coreide.objects2.Table;
import puakma.coreide.objects2.TableColumn;
import puakma.utils.NameValuePair;
import puakma.utils.VelocityProcessor;

public class TestVTLDBGenerator extends TestCase
{
  VelocityProcessor processor;
  
  protected void setUp() throws Exception
  {
    processor = new VelocityProcessor("/Users/mn/Puakma/puakma.vortex/dbTemplates");
  }
  
  private Database getDatabase() throws Exception
  {
    Database db = new DatabaseImpl(null);
    
    Table t = ObjectsFactory.createTable("T1");
    db.addObject(t);
    TableColumn c = t.addColumn("T1Id", "INTEGER");
    c.setAutoInc(true);
    c.setAllowsNull(false);
    c.setPk(true);
    c.setDescription("T1 INTEGER column");
    
    c = t.addColumn("Flt", "FLOAT");
    c.setFloatDecimals(4);
    c.setDefaultValue("1.0");
    c.setDescription("T1 FLOAT column");
    
    return db;
  }

  public void test1() throws Exception
  {
    process("postgres");//"mysql");
  }
  
  private void process(String name) throws Exception
  {
    Database db = getDatabase();
    NameValuePair[] params = {
      new NameValuePair("DB", db) 
    };
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try {
      processor.processTemplate(name + ".dbtemplate", os , params);
      String s = new String(os.toByteArray(), "UTF-8");
      System.out.println(s);
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
}
