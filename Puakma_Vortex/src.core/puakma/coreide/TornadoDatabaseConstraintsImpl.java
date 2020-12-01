/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    26/08/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import puakma.SOAP.SOAPFaultException;
import puakma.coreide.database.DatabaseSchemeBean;
import puakma.coreide.database.XmlDatabaseStructureParser;
import puakma.coreide.database.DatabaseSchemeBean.DbColumn;
import puakma.coreide.database.DatabaseSchemeBean.DbTable;
import puakma.coreide.designer.ServerDesigner;
import puakma.coreide.objects2.TornadoDatabaseConstraints;
import puakma.utils.XmlUtils;

/**
 * TODO: add processing of tables for database development
 * 
 * @author Martin Novak
 */
public class TornadoDatabaseConstraintsImpl implements TornadoDatabaseConstraints
{
  private boolean loaded;
  private int appParam_ParamNameLen = 50;
  private int appParam_ParamValueLen = 200;
  private int app_AppGroupLen = 150;
  private int app_AppNameLen = 30;
  private int app_InheritLen = 40;
  private int app_TemplateNameLen = 40;
  private int dobjParams_ParamNameLen = 50;
  private int dobjParams_ParamValueLen = 200;
  private int dobj_ContentTypeLen = 50;
  private int dobj_InheritFromLen = 40;
  private int dobj_NameLen = 30;
  private int dobj_OptionsLen = 255;
  private int dobj_UpdatedByLen = 120;
  private int dbCon_DbConNameLen = 50;
  private int dbCon_DbDriverLen = 255;
  private int dbCon_DbNameLen = 50;
  private int dbCon_DbPasswordLen = 80;
  private int dbCon_DbUrlLen = 255;
  private int dbCon_DbUrlOptionsLen = 255;
  private int dbCon_DbUserNameLen = 100;
  private int dbCon_CreatedByLen = 120;
  private int dbCon_InheritFromLen = 100;
  private int dbCon_OptionsLen = 255;
  private int keywordData_DataLen = 255;
  private int keyword_NameLen = 255;
  private int permission_NameLen = 120;
  private int role_NameLen = 30;

  public int getMaxAppParam_ParamNameLen()
  {
    return appParam_ParamNameLen;
  }

  public int getMaxAppParam_ParamValueLen()
  {
    return appParam_ParamValueLen;
  }

  public int getMaxApplication_AppGroupLen()
  {
    return app_AppGroupLen;
  }

  public int getMaxApplication_AppNameLen()
  {
    return app_AppNameLen;
  }

  public int getMaxApplication_InheritLen()
  {
    return app_InheritLen;
  }

  public int getMaxApplication_TemplateNameLen()
  {
    return app_TemplateNameLen;
  }

  public int getMaxDOParams_ParamNameLen()
  {
    return dobjParams_ParamNameLen;
  }

  public int getMaxDOParams_ParamValueLen()
  {
    return dobjParams_ParamValueLen;
  }

  public int getMaxDObj_ContentTypeLen()
  {
    return dobj_ContentTypeLen;
  }

  public int getMaxDObj_InheritFromLen()
  {
    return dobj_InheritFromLen;
  }

  public int getMaxDObj_NameLen()
  {
    return dobj_NameLen;
  }

  public int getMaxDObj_OptionsLen()
  {
    return dobj_OptionsLen;
  }

  public int getMaxDObj_UpdatedByLen()
  {
    return dobj_UpdatedByLen;
  }

  public int getMaxDbCon_DbConNameLen()
  {
    return dbCon_DbConNameLen;
  }

  public int getMaxDbCon_DbDriverLen()
  {
    return dbCon_DbDriverLen;
  }

  public int getMaxDbCon_DbNameLen()
  {
    return dbCon_DbNameLen;
  }

  public int getMaxDbCon_DbPasswordLen()
  {
    return dbCon_DbPasswordLen;
  }

  public int getMaxDbCon_DbUrlLen()
  {
    return dbCon_DbUrlLen;
  }

  public int getMaxDbCon_DbUrlOptionsLen()
  {
    return dbCon_DbUrlOptionsLen;
  }

  public int getMaxDbCon_DbUserNameLen()
  {
    return dbCon_DbUserNameLen;
  }
  
  public int getMaxDbCon_CreatedByLen()
  {
    return dbCon_CreatedByLen;
  }

  public int getMaxDbCon_InheritFromLen()
  {
    return dbCon_InheritFromLen;
  }

  public int getMaxDbCon_OptionsLen()
  {
    return dbCon_OptionsLen;
  }

  public int getMaxKeywordData_DataLen()
  {
    return keywordData_DataLen;
  }

  public int getMaxKeyword_NameLen()
  {
    return keyword_NameLen;
  }

  public int getMaxPermission_NameLen()
  {
    return permission_NameLen;
  }

  public int getMaxRole_NameLen()
  {
    return role_NameLen;
  }

  public boolean isLoaded()
  {
    return loaded;
  }

  public void reload(ServerDesigner designer) throws IOException, SOAPFaultException, SAXException, ParserConfigurationException
  {
    String xml = designer.getTornadoDatabaseStructureXml();
    XmlDatabaseStructureParser parser = new XmlDatabaseStructureParser();
    XmlUtils.parseXml(parser, xml);
    DatabaseSchemeBean bean = parser.getResult();
    List<DatabaseSchemeBean.DbTable> tables = bean.jdbcTables;
    Iterator<DatabaseSchemeBean.DbTable> it = tables.iterator();
    while(it.hasNext()) {
      DatabaseSchemeBean.DbTable table = it.next();
      String name = table.name.toUpperCase();
      DatabaseSchemeBean.DbColumn col;
      
      if("APPLICATION".equals(name)) {
        col = getColumn(table, "AppName");
        app_AppNameLen = col.columnSize;
        col = getColumn(table, "AppGroup");
        app_AppGroupLen = col.columnSize;
        col = getColumn(table, "InheritFrom");
        app_InheritLen = col.columnSize;
        col = getColumn(table, "TemplateName");
        app_TemplateNameLen = col.columnSize;
      }
      else if("APPPARAM".equalsIgnoreCase(name)) {
        col = getColumn(table, "ParamName");
        appParam_ParamNameLen = col.columnSize;
        col = getColumn(table, "ParamValue");
        appParam_ParamValueLen = col.columnSize;
      }
      else if("DBCONNECTION".equalsIgnoreCase(name)) {
        col = getColumn(table, "DBConnectionName");
        dbCon_DbConNameLen = col.columnSize;
        col = getColumn(table, "DBName");
        dbCon_DbNameLen = col.columnSize;
        col = getColumn(table, "DBURL");
        dbCon_DbUrlLen = col.columnSize;
        col = getColumn(table, "DBURLOptions");
        dbCon_DbUrlOptionsLen = col.columnSize;
        col = getColumn(table, "DBUserName");
        dbCon_DbUserNameLen = col.columnSize;
        col = getColumn(table, "DBDriver");
        dbCon_DbDriverLen = col.columnSize;
        col = getColumn(table, "DBPassword");
        dbCon_DbPasswordLen = col.columnSize;
        col = getColumn(table, "CreatedBy");
        dbCon_CreatedByLen = col.columnSize;
        col = getColumn(table, "Options");
        dbCon_OptionsLen = col.columnSize;
        col = getColumn(table, "InheritFrom");
        dbCon_InheritFromLen = col.columnSize;
      }
      else if("DESIGNBUCKET".equalsIgnoreCase(name)) {
        col = getColumn(table, "Name");
        dobj_NameLen = col.columnSize;
        col = getColumn(table, "ContentType");
        dobj_ContentTypeLen = col.columnSize;
        col = getColumn(table, "UpdatedBy");
        dobj_UpdatedByLen = col.columnSize;
        col = getColumn(table, "Options");
        dobj_OptionsLen = col.columnSize;
        col = getColumn(table, "InheritFrom");
        dobj_InheritFromLen = col.columnSize;
      }
      else if("DESIGNBUCKETPARAM".equalsIgnoreCase(name)) {
        col = getColumn(table, "ParamName");
        dobjParams_ParamNameLen = col.columnSize;
        col = getColumn(table, "ParamValue");
        dobjParams_ParamValueLen = col.columnSize;
      }
      else if("KEYWORD".equalsIgnoreCase(name)) {
        col = getColumn(table, "Name");
        keyword_NameLen = col.columnSize;
      }
      else if("KEYWORDDATA".equalsIgnoreCase(name)) {
        col = getColumn(table, "Data");
        keywordData_DataLen = col.columnSize;
      }
      else if("PERMISSION".equalsIgnoreCase(name)) {
        col = getColumn(table, "Name");
        permission_NameLen = col.columnSize;
      }
      else if("ROLE".equalsIgnoreCase(name)) {
        col = getColumn(table, "RoleName");
        role_NameLen = col.columnSize;
      }
    }
    
    loaded = true;
  }

  /**
   * Finds column inside table bean.
   */
  private DbColumn getColumn(DbTable table, String colName)
  {
    Iterator<DatabaseSchemeBean.DbColumn> it = table.columns.iterator();
    colName = colName.toUpperCase();
    while(it.hasNext()) {
      DbColumn col = it.next();
      if(colName.equals(col.name.toUpperCase()))
        return col;
    }
    return null;
  }

}
