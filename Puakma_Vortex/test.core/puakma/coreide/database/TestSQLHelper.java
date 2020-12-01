/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    10/05/2006
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

import junit.framework.TestCase;

public class TestSQLHelper extends TestCase
{
  public void testSplitSqlToCommands() throws Exception
  {
    String s = " \n dsa \n      ";
    assertEquals("dsa", s.trim());
    
    String select0 = "SELECT * FROM test WHERE x ='ahoj'";
    String select1 = "SELECT * FROM test WHERE x ='aho\\;j'";
    String select2 = "SELECT * FROM test WHERE x ='ah\\'oj'";
    String sql = select0 + " ;" + select1 + ";\n;; ;" + select2;
    SQLCommandDescriptor[] cmds = SQLHelper.splitSqlToCommands(sql, SQLHelper.ESCAPE_TYPE_MYSQL);
    assertEquals(3, cmds.length);
    
    assertEquals(select0, cmds[0].sql);
    //assertEquals(0, cmds[0].start);
    //assertEquals(34, cmds[0].end);
    assertEquals(cmds[0].sql, sql.substring(cmds[0].getStart(), cmds[0].getEnd()));
    
    assertEquals(select1, cmds[1].sql);
    assertEquals(cmds[1].sql, sql.substring(cmds[1].getStart(), cmds[1].getEnd()));
    
    assertEquals(select2, cmds[2].sql);
    assertEquals(cmds[2].sql, sql.substring(cmds[2].getStart(), cmds[2].getEnd()));
  }
  
  public void testSplitSqlToCommands2() throws Exception
  {
    String sql = ";;l;";
    SQLCommandDescriptor[] cmds = SQLHelper.splitSqlToCommands(sql, SQLHelper.ESCAPE_TYPE_MYSQL);
    assertEquals(1, cmds.length);
  }
  
  public void testSplotSqlToCommands3() throws Exception
  {
    String s0 = "DROP TABLE IF EXISTS TESTTABLE";
    String s1 = "CREATE  TABLE TESTTABLE (\n"
                + "TestTableID INTEGER (11)  AUTO_INCREMENT UNIQUE PRIMARY KEY,\n"
                + "Name VARCHAR (50) \n" + "fsfsd fsdfs\n" + "  )";
    String sql = "\n" + 
            s0 + ";\n" + 
            "\n" + 
            s1 +";\n" + 
            "\n" + 
            "DROP TABLE IF EXISTS XAB;\n" + 
            "\n" + 
            "CREATE  TABLE XAB (\n" + 
            "TestTableID INTEGER (0)  AUTO_INCREMENT PRIMARY KEY\n" + 
            "\n" + 
            "CONSTRAINT fk_XAB_TestTableID FOREIGN KEY TestTableID\n" + 
            "           REFERENCES TESTTABLE  );\n" + 
            "";
    SQLCommandDescriptor[] descs = SQLHelper.splitSqlToCommands(sql, SQLHelper.ESCAPE_TYPE_MYSQL);
    assertEquals(4, descs.length);
    
    assertEquals(s0, descs[0].sql);
    assertEquals(descs[0].sql, sql.substring(descs[0].getStart(), descs[0].getEnd()));
    
    assertEquals(s1, descs[1].sql);
    assertEquals(descs[1].sql, sql.substring(descs[1].getStart(), descs[1].getEnd()));
  }
}
