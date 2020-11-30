/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Dec 17, 2005
 * 
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide.database;

import org.xml.sax.Attributes;

import puakma.coreide.designer.IElementHandler;
import puakma.coreide.designer.PuakmaXmlCodes;
import puakma.utils.ElementAdapter;
import puakma.utils.PmaXmlHandler;
import puakma.utils.lang.StringUtil;

/**
 * 
 * @author Martin Novak
 */
public class XmlDatabaseStructureParser extends PmaXmlHandler
{
  private static final String[] elements = {
      PuakmaXmlCodes.ELEM_JDBC, PuakmaXmlCodes.ELEM_SQL_KEYWORD, PuakmaXmlCodes.ELEM_SQL_STATE_TYPE,
      PuakmaXmlCodes.ELEM_NUMERIC_FUNCTION, PuakmaXmlCodes.ELEM_DATETIME_FUNCTION,
      PuakmaXmlCodes.ELEM_TABLE, PuakmaXmlCodes.ELEM_COLUMN,
      PuakmaXmlCodes.ELEM_PUAMA_SYSTEM_DATABASE, PuakmaXmlCodes.ELEM_CUSTOM
  };
  private final IElementHandler[] handlers = {
      new JdbcHandler(), new SqlKwHandler(), new SqlStateTypeHandler(),
      new NumericFunctionHandler(), new DateTimeHandler(),
      new TableHandler(), new ColumnHandler(),
      new PuakmaSystemDatabaseHandler(), new CustomDataHandler()
  };
  
  private boolean handlingJdbcDataNow = false;
  private DatabaseSchemeBean bean = new DatabaseSchemeBean();
  private DatabaseSchemeBean.DbTable table;
  
  private class JdbcHandler extends ElementAdapter {
    public void startElement(String name, Attributes attributes)
    {
      handlingJdbcDataNow = true;
    }
  }
  private class SqlKwHandler extends ElementAdapter {
    public void startElement(String name, Attributes attributes) {
      contextBuffer.setLength(0);
    }
    public void endElement(String name) {
      bean.keywords.add(contextBuffer.toString());
    }
  }
  private class SqlStateTypeHandler extends ElementAdapter {
    public void startElement(String name, Attributes attributes) {
      contextBuffer.setLength(0);
    }
    public void endElement(String name) {
      bean.sqlStateType = contextBuffer.toString();
    }
  }
  private class NumericFunctionHandler extends ElementAdapter {
    public void startElement(String name, Attributes attributes) {
      contextBuffer.setLength(0);
    }
    public void endElement(String name) {
      bean.numFunctions.add(contextBuffer.toString());
    }
  }
  private class DateTimeHandler extends ElementAdapter {
    public void startElement(String name, Attributes attributes) {
      contextBuffer.setLength(0);
    }
    public void endElement(String name) {
      bean.dateTimeFunctions.add(contextBuffer.toString());
    }
  }
  private class TableHandler extends ElementAdapter {
    public void startElement(String name, Attributes a) {
      table = new DatabaseSchemeBean.DbTable();
      table.buildOrder = StringUtil.parseInt(a.getValue(PuakmaXmlCodes.ATT_ORDER), -1);
      table.catalog = StringUtil.safeString(a.getValue(PuakmaXmlCodes.ATT_CATALOG_NAME));
      table.description = StringUtil.safeString(a.getValue(PuakmaXmlCodes.ATT_DESC));
      table.id = StringUtil.parseInt(a.getValue(PuakmaXmlCodes.ATT_ID), -1);
      table.name = StringUtil.safeString(a.getValue(PuakmaXmlCodes.ATT_NAME));
      table.schema = StringUtil.safeString(a.getValue(PuakmaXmlCodes.ATT_SCHEMA));
      table.type = StringUtil.parseInt(a.getValue(PuakmaXmlCodes.ATT_TYPE), -1);
      
      if(handlingJdbcDataNow)
        bean.jdbcTables.add(table);
      else
        bean.systemDbTables.add(table);
    }
    public void endElement(String name) {
      table = null;
    }
  }
  private class ColumnHandler extends ElementAdapter {
    public void startElement(String name, Attributes atts) {
      int ijk = -1;
      ijk++;
      DatabaseSchemeBean.DbColumn column = new DatabaseSchemeBean.DbColumn();
      column.name = StringUtil.safeString(atts.getValue(PuakmaXmlCodes.ATT_NAME));
      column.columnSize = StringUtil.parseInt(atts.getValue(PuakmaXmlCodes.ATT_COLUMN_SIZE), 0);
      column.decimalDigits = StringUtil.parseInt(atts.getValue(PuakmaXmlCodes.ATT_DECIMAL_DIGITS), 0);
      column.defaultValue = StringUtil.safeString(atts.getValue(PuakmaXmlCodes.ATT_DEFAULT));
      column.description = StringUtil.safeString(atts.getValue(PuakmaXmlCodes.ATT_DESC));
      column.isNullable = StringUtil.parseBool(atts.getValue(PuakmaXmlCodes.ATT_NULLABLE), true);
      column.position = StringUtil.parseInt(atts.getValue(PuakmaXmlCodes.ATT_POSITION), -1);
//      column.radix = parseInt(atts.getValue(PuakmaXmlCodes.ATT_RADIX), -1);
      column.dataType = StringUtil.parseInt(atts.getValue(PuakmaXmlCodes.ATT_DATA_TYPE), 0);
      column.typeName = StringUtil.safeString(atts.getValue(PuakmaXmlCodes.ATT_TYPE_NAME));
      if(column.typeName.length() == 0)
        column.typeName = atts.getValue(PuakmaXmlCodes.ATT_TYPE);
      column.id = StringUtil.parseInt(atts.getValue(PuakmaXmlCodes.ATT_ID), -1);
      String val1 = StringUtil.safeString(atts.getValue(PuakmaXmlCodes.ATT_FK_TABLE));
      column.fkTable = StringUtil.parseInt(val1, -1);
      column.fkTableName = StringUtil.safeString(atts.getValue(PuakmaXmlCodes.ATT_FK_TABLE_NAME));
      column.isPk = StringUtil.parseBool(atts.getValue(PuakmaXmlCodes.ATT_IS_PK), false);
      column.isUniq = StringUtil.parseBool(atts.getValue(PuakmaXmlCodes.ATT_IS_UNIQ), false);
      column.autoInc = StringUtil.parseBool(atts.getValue(PuakmaXmlCodes.ATT_AUTO_INCREMENT), false);
      //IGNORE OPTIONS
      //column.options = StringUtil.safeString(atts.getValue(PuakmaXmlCodes.ATT_EXTRA_OPTIONS));
      
      // PARSE TYPE SIZE IF PRESENT
      String typeSize = StringUtil.safeString(atts.getValue(PuakmaXmlCodes.ATT_TYPE_SIZE));
      if(typeSize.length() > 0) {
        String[] val = typeSize.split(",");
        if(val.length == 2) {
          int size = StringUtil.parseInt(val[0], -1);
          int dec = StringUtil.parseInt(val[1], -1);
          if(size != -1 && dec != -1) {
            column.columnSize = size;
            column.decimalDigits = dec;
          }
        }
        else if(val.length == 1) {
          int size = StringUtil.parseInt(val[0], -1);
          column.columnSize = size;
        }
//        else {
//          column.columnSize = -1;
//        }
      }
      
      if(handlingJdbcDataNow)
        table.columns.add(column);
      else
        table.columns.add(column);
    }
    public void endElement(String name) {
    }
  }
  private class PuakmaSystemDatabaseHandler extends ElementAdapter {
    public void startElement(String name, Attributes attributes)
    {
      handlingJdbcDataNow = false;
    }
  }
  private class CustomDataHandler extends ElementAdapter {
    public void startElement(String name, Attributes attributes) {
      contextBuffer.setLength(0);
    }
    public void endElement(String name) {
      bean.customData = contextBuffer.toString();
    }
  }

  protected String[] getElements()
  {
    return elements;
  }

  protected IElementHandler[] getHandlers()
  {
    return handlers;
  }
  
  public DatabaseSchemeBean getResult()
  {
    return bean;
  }
}
