/*
 * BuildSQL.java
 * Builds a table based on the jdbc driver name, eg commands specific to that db.
 *
 * Created on 17 April 2003, 17:34
 *
 * - BU 14/11/2004 Altered class to remove static methods and allow one class 
 * to be used for all databases.
 */
package puakma.coreide.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import puakma.addin.http.action.HTTPSessionContext;
import puakma.addin.http.document.HTMLDocument;
import puakma.addin.http.document.TableManager;
import puakma.system.SystemContext;

public class BuildSQL
{
  public static final int DIALECT_MYSQL = 0;

  public static final int DIALECT_POSTGRESQL = 1;

  public static final int DIALECT_HSQLDB = 2;

  private HTTPSessionContext m_pSession;

  private SystemContext m_pSystem;

  private String m_sJDBCDriver;

  private int m_iRDBMSDialect = DIALECT_MYSQL;

  /** Creates a new instance of BuildSQL */
  public BuildSQL(HTTPSessionContext pSession, String sDriver)
  {
    m_pSession = pSession;
    m_pSystem = pSession.getSystemContext();
    m_sJDBCDriver = sDriver;

    String sLowDriver = m_sJDBCDriver.toLowerCase();
    if(sLowDriver.indexOf("mysql") > 0)
      m_iRDBMSDialect = DIALECT_MYSQL;
    if(sLowDriver.indexOf("postgres") > 0)
      m_iRDBMSDialect = DIALECT_POSTGRESQL;
    if(sLowDriver.indexOf("hsqldb") > 0)
      m_iRDBMSDialect = DIALECT_HSQLDB;
  }

  /**
   * 
   */
  public String getSQL(String szAppID, String szDBConnectionID)
  {
    String CRLF = "\r\n";
    StringBuffer sb = new StringBuffer();
    sb.append(createDatabase(szDBConnectionID, true) + CRLF + CRLF);
    String szName = connectionExists(szDBConnectionID);

    // now create the tables
    Vector<String> v = buildTables(Long.parseLong(szAppID), szName, szDBConnectionID, true);
    for(int i = 0; i < v.size(); i++) {
      sb.append(v.elementAt(i) + CRLF + CRLF);
    }

    return sb.toString();
  }

  /**
   * Return name if the connection exists...
   */
  public String connectionExists(String szDBConnectionID)
  {
    boolean bExists = false;
    Connection cx = null;
    String szName = "";

    try {
      cx = m_pSystem.getSystemConnection();
      Statement Stmt = cx.createStatement();
      ResultSet RS = Stmt
                         .executeQuery("SELECT DBConnectionName FROM DBCONNECTION WHERE DBConnectionID="
                                       + szDBConnectionID);
      if(RS.next())
        szName = RS.getString(1);
      RS.close();
      Stmt.close();
    }
    catch(Exception e) {
      m_pSystem.doError("Error getting connection: " + e.getMessage(), m_pSession);
    }
    m_pSystem.releaseSystemConnection(cx);
    return szName;
  }

  /**
   * 
   * 
   */
  public String createDatabase(String szDBConnectionID, boolean bSQLOnly)
  {
    TableManager tm = new TableManager(m_pSession,
                                       SystemContext.DBALIAS_SYSTEM, "DBCONNECTION");
    HTMLDocument docConnection = new HTMLDocument();
    tm.populateDocument("SELECT * FROM DBCONNECTION WHERE DBConnectionID="
                        + szDBConnectionID, docConnection);

    if(!docConnection.hasItem("DBConnectionID")) {
      return "";
    }

    String szDBURL = docConnection.getItemValue("DBURL");
    String szDBName = docConnection.getItemValue("DBName");
    String szUser = docConnection.getItemValue("DBUserName");
    String szPW = docConnection.getItemValue("DBPassword");
    String szDriverClass = docConnection.getItemValue("DBDriver");
    String szConnectionName = docConnection.getItemValue("DBConnectionName");

    String sCommand = "CREATE DATABASE " + szDBName;
    if(bSQLOnly)
      return sCommand;

    Connection cx = null;
    try {
      cx = m_pSession.getDataConnection(szConnectionName);
      Statement stmt = cx.createStatement();
      stmt.execute(sCommand);
      stmt.close();
    }
    catch(Exception e) {
      m_pSystem.doError("createDatabase() " + e.toString(),
                        m_pSession.getSessionContext());
    }

    m_pSession.releaseDataConnection(cx);
    return sCommand;
  }

  /**
   * 
   * returns a vector containing the sql commands
   * 
   * @param szDBConnectionID
   */
  public Vector<String> buildTables(long lAppID, String szDBConnectionName,
                            String szDBConnectionID, boolean bSQLOnly)
  {
    Connection cxData = null;
    Connection cxSys = null;
    Hashtable<String, String> htExistTables = new Hashtable<String, String>();
    Vector<String> vNewTablesName = new Vector<String>();
    Vector<String> vNewTablesID = new Vector<String>();
    Vector<String> vSQL = new Vector<String>();
    Statement stmt = null;
    ResultSet RS = null;

    try {
      cxData = m_pSession.getDataConnection(lAppID, szDBConnectionName);
      if(cxData != null) {
        stmt = cxData.createStatement();
        DatabaseMetaData dbmd = cxData.getMetaData();

        RS = dbmd.getTables(null, null, "%", null);// stmt.executeQuery("SHOW
                                                    // TABLES");
        while(RS.next()) {
          // System.out.println(RS.getString(1));
          String sTable = RS.getString(1);
          if(sTable != null)
            htExistTables.put(sTable.toLowerCase(), sTable);
        }
        RS.close();
        stmt.close();
      }
    }
    catch(Exception r) {
      vSQL.add(r.toString());
    }

    try {
      cxSys = m_pSystem.getSystemConnection();
      stmt = cxSys.createStatement();
      RS = stmt
               .executeQuery("SELECT TableName,TableID FROM PMATABLE WHERE DBConnectionID="
                             + szDBConnectionID + " ORDER BY BuildOrder");
      while(RS.next()) {
        if(!htExistTables.containsKey(RS.getString(1).toLowerCase()) || bSQLOnly) {
          vNewTablesName.add(RS.getString(1));
          vNewTablesID.add(RS.getString(2));
        }
      }
      RS.close();
      stmt.close();
    }
    catch(Exception e) {
      m_pSystem.doError("buildTables() " + e.toString(), m_pSession.getSessionContext());
      vSQL.add("buildTables() " + e.toString());
    }
    createTables(cxSys, cxData, lAppID, szDBConnectionName, vNewTablesName,
                          vNewTablesID, vSQL, bSQLOnly);
    m_pSystem.releaseSystemConnection(cxSys);
    m_pSession.releaseDataConnection(cxData);
    return vSQL;
  }

  /**
   * 
   */
  public static String getTableName(Connection cxSys, String szTableID) throws Exception
  {
    Statement Stmt = cxSys.createStatement();
    ResultSet RS;
    String szTableName = "";
    RS = Stmt.executeQuery("SELECT TableName FROM PMATABLE WHERE TableID=" + szTableID);
    if(RS.next()) {
      szTableName = RS.getString(1);
    }
    RS.close();
    Stmt.close();
    return szTableName;
  }

  /**
   * 
   * 
   */
  public int createTables(Connection cxSys, Connection cxData, long lAppID,
                          String szDBConnectionName, Vector<String> vNewTablesName,
                          Vector<String> vNewTablesID, Vector<String> vSQL, boolean bSQLOnly)
  {
    int iCreated = 0;
    Statement stmt = null;
    try {
      if(!bSQLOnly)
        stmt = cxData.createStatement();
      // System.out.println("t="+RS.getString(1));
      for(int i = 0; i < vNewTablesName.size(); i++) {
        String szSQL = getTableSQL(cxSys, vNewTablesName.elementAt(i),
                                   vNewTablesID.elementAt(i));
        ArrayList vCommands = puakma.util.Util.splitString(szSQL, "\r\n");
        // vSQL.add(szSQL);
        for(int k = 0; k < vCommands.size(); k++) {
          String sCommand = (String) vCommands.get(k);
          vSQL.add(sCommand);
          if(!bSQLOnly)
            stmt.execute(sCommand);
        }

        iCreated++;
      }
      if(stmt != null)
        stmt.close();
    }
    catch(Exception e) {
      vSQL.add("createTables() FAILED: " + e.toString());
    }
    if(!bSQLOnly)
      vSQL.add(iCreated + " tables created");
    return iCreated;
  }

  public String getTableSQL(Connection cxSys, String szTableName, String szTableID)
                                                                                   throws Exception
  {
    switch(m_iRDBMSDialect) {
      case DIALECT_MYSQL:
        return getMySQLTableSQL(cxSys, szTableName, szTableID);
      case DIALECT_POSTGRESQL:
        return getPostgreSQLTableSQL(cxSys, szTableName, szTableID);
      case DIALECT_HSQLDB:
        return getHSQLDBTableSQL(cxSys, szTableName, szTableID);
    }
    return "";
  }

  /**
   * for mysql databases
   */
  public String getMySQLTableSQL(Connection cxSys, String szTableName, String szTableID)
                                                                                        throws Exception
  {
    String szSQL = "CREATE TABLE " + szTableName + "(";
    StringBuffer sbAttr = new StringBuffer();
    String szFTFields = "";
    String szFTSQL = "";

    Statement Stmt = cxSys.createStatement();
    ResultSet RS;
    RS = Stmt.executeQuery("SELECT * FROM ATTRIBUTE WHERE TableID=" + szTableID);
    while(RS.next()) {
      String szPrimaryKey = "", szAttName = "", szAutoInc = "", szNull = "", szRefTable = "", szType = "";
      String szPrecision = "", szUnique = "";
      szAttName = RS.getString("AttributeName");
      String szFT = RS.getString("FTIndex");
      if(szFT != null && szFT.equals("1")) {
        if(szFTFields.length() == 0)
          szFTFields = szAttName;
        else
          szFTFields += "," + szAttName;
      }
      if(RS.getString("IsPrimaryKey").equals("1"))
        szPrimaryKey = " PRIMARY KEY";
      if(RS.getString("AutoIncrement").equals("1"))
        szAutoInc = " AUTO_INCREMENT";
      if(!RS.getString("AllowNull").equals("1"))
        szNull = " NOT NULL";
      if(RS.getString("IsUnique").equals("1"))
        szUnique = " UNIQUE";
      szRefTable = RS.getString("RefTable");
      if(szRefTable != null && szRefTable.trim().length() > 0) {
        szRefTable = " REFERENCES " + getTableName(cxSys, szRefTable);
        String szCascade = RS.getString("CascadeDelete");
        if(szCascade.equals("1"))
          szRefTable += " ON DELETE CASCADE";
      }
      String szExtra = RS.getString("ExtraOptions");
      szType = RS.getString("Type");
      if(szType.equals("CHAR") || szType.equals("VARCHAR") || szType.equals("NUMERIC")) {
        szPrecision = "(" + RS.getString("TypeSize") + ")";
      }

      String sCol = szAttName + " " + szType + szPrecision + szPrimaryKey + szUnique
                    + szAutoInc + szNull + szRefTable + " " + szExtra;
      if(sbAttr.length() == 0)
        sbAttr.append(sCol);
      else {
        sbAttr.append(", ");
        sbAttr.append(sCol);
      }

    }
    RS.close();
    Stmt.close();

    if(szFTFields.length() > 0)
      szFTSQL = ", FULLTEXT (" + szFTFields + ")";

    return szSQL + sbAttr.toString() + szFTSQL + ")";
  }

  /**
   * for postgresql databases
   */
  public static String getPostgreSQLTableSQL(Connection cxSys, String szTableName,
                                             String szTableID) throws Exception
  {
    String szSQL = "CREATE TABLE " + szTableName + "(";
    String szAttr = "";
    String szFTFields = "";
    String szFTSQL = "";
    String szSequence = "";

    Statement Stmt = cxSys.createStatement();
    ResultSet RS;
    RS = Stmt.executeQuery("SELECT * FROM ATTRIBUTE WHERE TableID=" + szTableID);
    while(RS.next()) {

      String szPrimaryKey = "", szAttName = "", szAutoInc = "", szNull = "", szRefTable = "", szType = "";
      String szPrecision = "", szUnique = "";
      szAttName = RS.getString("AttributeName");
      String szFT = RS.getString("FTIndex"); // not supported in postgresql

      String szExtra = RS.getString("ExtraOptions");
      if(RS.getString("IsPrimaryKey").equals("1"))
        szPrimaryKey = " PRIMARY KEY";
      String sAutoInc = RS.getString("AutoIncrement");
      if(sAutoInc != null && sAutoInc.equals("1")) {
        szSequence = "CREATE SEQUENCE seq_" + szTableName.toLowerCase() + "\r\n";
        szExtra = "DEFAULT nextval('seq_" + szTableName.toLowerCase() + "')";
      }
      if(!RS.getString("AllowNull").equals("1"))
        szNull = " NOT NULL";

      if(RS.getString("IsUnique").equals("1"))
        szUnique = " UNIQUE";
      szRefTable = RS.getString("RefTable");
      if(szRefTable != null && szRefTable.trim().length() > 0) {
        szRefTable = " REFERENCES " + getTableName(cxSys, szRefTable);
        String szCascade = RS.getString("CascadeDelete");
        if(szCascade.equals("1"))
          szRefTable += " ON DELETE CASCADE";
      }
      else
        szRefTable = "";

      szType = RS.getString("Type");
      if(szType.equals("CHAR") || szType.equals("VARCHAR") || szType.equals("NUMERIC")) {
        szPrecision = "(" + RS.getString("TypeSize") + ")";
      }
      if(szType.equalsIgnoreCase("LONGBLOB"))
        szType = "BYTEA";
      if(szType.equalsIgnoreCase("LONGTEXT"))
        szType = "TEXT";
      if(szType.equalsIgnoreCase("DATETIME"))
        szType = "TIMESTAMP";

      if(szAttr.length() == 0)
        szAttr += szAttName + " " + szType + szPrecision + szPrimaryKey + szUnique
                  + szAutoInc + szNull + szRefTable + " " + szExtra;
      else
        szAttr += ", " + szAttName + " " + szType + szPrecision + szPrimaryKey + szUnique
                  + szAutoInc + szNull + szRefTable + " " + szExtra;

    }
    RS.close();
    Stmt.close();

    // if(!szFTFields.equals("")) szFTSQL = ", FULLTEXT (" + szFTFields + ")";

    return szSequence + szSQL + szAttr + ")";
  }

  /**
   * for mysql databases
   */
  public String getHSQLDBTableSQL(Connection cxSys, String szTableName, String szTableID)
                                                                                         throws Exception
  {
    String szSQL = "CREATE TABLE " + szTableName + "(";
    StringBuffer sbAttr = new StringBuffer();
    String szFTFields = "";
    String szFTSQL = "";
    String sConstraints = "";
    String sForeignKeys = "";

    Statement Stmt = cxSys.createStatement();
    ResultSet RS;
    RS = Stmt.executeQuery("SELECT * FROM ATTRIBUTE WHERE TableID=" + szTableID);
    while(RS.next()) {
      String szPrimaryKey = "", szAttName = "", szAutoInc = "", szNull = "", szRefTable = "", szType = "";
      String szPrecision = "", szUnique = "";
      szAttName = RS.getString("AttributeName");
      String szFT = RS.getString("FTIndex"); // not supported

      if(RS.getString("IsPrimaryKey").equals("1"))
        szPrimaryKey = " PRIMARY KEY";
      if(RS.getString("AutoIncrement").equals("1"))
        szAutoInc = " IDENTITY";
      if(!RS.getString("AllowNull").equals("1"))
        szNull = " NOT NULL";
      if(RS.getString("IsUnique").equals("1"))
        szUnique = ", UNIQUE(" + szAttName + ")";
      szRefTable = RS.getString("RefTable");
      if(szRefTable != null && szRefTable.trim().length() > 0) {
        // System.out.println("ref=["+szRefTable+"]");
        szRefTable = " FOREIGN KEY(" + szAttName + ") REFERENCES "
                     + getTableName(cxSys, szRefTable) + "(" + szAttName + ")";
        String szCascade = RS.getString("CascadeDelete");
        if(szCascade.equals("1"))
          szRefTable += " ON DELETE CASCADE";
      }
      String szExtra = RS.getString("ExtraOptions");
      szType = RS.getString("Type");
      if(szType.equals("CHAR") || szType.equals("VARCHAR") || szType.equals("NUMERIC")) {
        szPrecision = "(" + RS.getString("TypeSize") + ")";
      }
      // add type mappings here....
      if(szType.equalsIgnoreCase("LONGTEXT"))
        szType = "LONGVARCHAR";
      if(szType.equalsIgnoreCase("LONGBLOB"))
        szType = "LONGVARBINARY";

      String sCol = szAttName + " " + szType + szPrecision + szNull + szAutoInc
                    + szPrimaryKey + " " + szExtra;// + szUnique;
      if(sbAttr.length() == 0)
        sbAttr.append(sCol.trim());
      else {
        sbAttr.append(", ");
        sbAttr.append(sCol.trim());
      }

      if(szUnique.trim().length() > 0) {
        sConstraints += szUnique;
      }

      if(szRefTable.trim().length() > 0) {
        sForeignKeys += "," + szRefTable;
      }

    }
    RS.close();
    Stmt.close();

    // if(szFTFields.length()>0) szFTSQL = ", FULLTEXT (" + szFTFields + ")";

    return szSQL + sbAttr.toString() + szFTSQL + sForeignKeys + sConstraints + ")";
  }

}
