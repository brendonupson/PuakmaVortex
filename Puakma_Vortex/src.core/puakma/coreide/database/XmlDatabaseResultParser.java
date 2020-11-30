/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Oct 26, 2005
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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;

import puakma.coder.CoderB64;
import puakma.coreide.database.DatabaseQueryResultBean.Column;
import puakma.coreide.database.DatabaseQueryResultBean.RSColumnMetadata;
import puakma.coreide.database.DatabaseQueryResultBean.Row;
import puakma.coreide.database.DatabaseQueryResultBean.ServerException;
import puakma.coreide.designer.IElementHandler;
import puakma.coreide.designer.PuakmaXmlCodes;
import puakma.utils.PmaXmlHandler;
import puakma.utils.lang.ArrayUtils;

public class XmlDatabaseResultParser extends PmaXmlHandler implements PuakmaXmlCodes
{
  private static final String mapOfElems[] = { ELEM_COLUMN_VALUE, ELEM_ROW,
      ELEM_RS_METADATA, ELEM_RESULT, ELEM_EXCEPTION, ELEM_GENERATED_KEYS,
      ELEM_RESULT_SET, ELEM_STACKTRACE, ELEM_ERROR_CODE, ELEM_SQL_STATE };

  private final IElementHandler[] handlers = { new ColumnValueHandler(),
      new RowHandler(), new RsMetadataHandler(), new ResultHandler(), new ExceptionHandler(),
      new GeneratedKeysHandler(), new ResultSetHandler(), new StackTraceHandler(),
      new ErrorCodeHandler(), new SqlStateHandler(),
  };

  private DatabaseQueryResultBean dbResult;
  
  private DatabaseQueryResultBean.DBResultSet resultSet;
  private DatabaseQueryResultBean.Row row;
  private DatabaseQueryResultBean.Column col;
  private List<Column> cols = new ArrayList<Column>();
  private DatabaseQueryResultBean.RSColumnMetadata colMetadata;
  private ServerException exception;
  
  public XmlDatabaseResultParser()
  {
    dbResult = new DatabaseQueryResultBean();
  }
  
  class ColumnValueHandler implements IElementHandler
  {
    public void startElement(String name, Attributes attributes)
    {
      col = new DatabaseQueryResultBean.Column();
      col.type = Integer.parseInt(attributes.getValue(ATT_TYPE));
      col.index = Integer.parseInt(attributes.getValue(ATT_INDEX));
      contextBuffer.setLength(0);
    }

    public void endElement(String name)
    {
      String str = contextBuffer.toString();
      switch(col.type) {
        case PuakmaXmlCodes.COLT_BOOL:
          col.o = Boolean.valueOf(str);
        break;
        case PuakmaXmlCodes.COLT_BYTE:
          col.o = new Byte(str);
        break;
        case PuakmaXmlCodes.COLT_CHAR:
          if(str.length() > 0)
            col.o = new Character(str.charAt(0));
          else
            col.o = new Character('#');
        break;
        case PuakmaXmlCodes.COLT_SHORT:
          col.o = new Short(str);
        break;
        case PuakmaXmlCodes.COLT_INT:
          col.o = new Integer(str);
        break;
        case PuakmaXmlCodes.COLT_LONG:
          col.o = new Long(str);
        break;
        case PuakmaXmlCodes.COLT_FLOAT:
          col.o = new Float(str);
        break;
        case PuakmaXmlCodes.COLT_DOUBLE:
          col.o = new Double(str);
        break;
        case PuakmaXmlCodes.COLT_STRING:
          col.o = new String(str);
        break;
        case PuakmaXmlCodes.COLT_BYTEARRAY:
          CoderB64 coder = new CoderB64();
          try {
            col.b = coder.decode(str.getBytes("US-ASCII"));
          }
          catch(UnsupportedEncodingException e) {
            e.printStackTrace();
          }
        break;
          
      }
      cols.add(col);
      col = null;
    }
  }
  
  class RowHandler implements IElementHandler
  {
    public void startElement(String name, Attributes attributes)
    {
      row = new DatabaseQueryResultBean.Row();
    }

    public void endElement(String name)
    {
      row.cols = cols.toArray(new DatabaseQueryResultBean.Column[cols.size()]);
      cols.clear();
      resultSet.rows = (Row[]) ArrayUtils.append(resultSet.rows, row);
      row = null;
    }
  }
  
  class ResultHandler implements IElementHandler
  {
    public void endElement(String name)
    {
    }

    public void startElement(String name, Attributes attributes)
    {
    }
  }
  
  class RsMetadataHandler implements IElementHandler
  {
    public void startElement(String name, Attributes attributes)
    {
      try {
        colMetadata = new DatabaseQueryResultBean.RSColumnMetadata();
        XmlDatabaseResultParser.super.attributesToBean(colMetadata, attributes);
      }
      catch(NoSuchFieldException e) {
        throw new RuntimeException(e);
      }
    }

    public void endElement(String name)
    {
      if(resultSet.columnMetadata == null)
        resultSet.columnMetadata = new RSColumnMetadata[0];
      resultSet.columnMetadata = (RSColumnMetadata[]) ArrayUtils.append(
                                                          resultSet.columnMetadata, colMetadata);
      colMetadata = null;
    }
  }
  
  class ExceptionHandler implements IElementHandler
  {
    public void startElement(String name, Attributes attributes)
    {
      exception = new DatabaseQueryResultBean.ServerException();
      dbResult.exception = exception;
      exception.message = attributes.getValue(PuakmaXmlCodes.ATT_MESSAGE);
    }

    public void endElement(String name)
    {
      
    }
  }
  
  class GeneratedKeysHandler implements IElementHandler
  {
    public void startElement(String name, Attributes attributes)
    {
      resultSet = new DatabaseQueryResultBean.DBResultSet();
    }

    public void endElement(String name)
    {
      dbResult.generatedKeys = resultSet;
      resultSet = null;
    }
  }
  
  class ResultSetHandler implements IElementHandler
  {
    public void startElement(String name, Attributes attributes)
    {
      resultSet = new DatabaseQueryResultBean.DBResultSet();
    }

    public void endElement(String name)
    {
      dbResult.resultSet = resultSet;
      resultSet = null;
    }
  }
  
  class StackTraceHandler implements IElementHandler
  {
    public void startElement(String name, Attributes attributes)
    {
      contextBuffer.setLength(0);
    }

    public void endElement(String name)
    {
      String str = contextBuffer.toString();
      String[] lines = str.split("\n");
      exception.stackTrace = lines;
    }
  }
  
  class ErrorCodeHandler implements IElementHandler
  {
    public void startElement(String name, Attributes attributes)
    {
      contextBuffer.setLength(0);
    }

    public void endElement(String name)
    {
      try {
        exception.errorCode = Integer.parseInt(contextBuffer.toString());
      }
      catch(NumberFormatException ex) {
        exception.errorCode = -1;
      }
    }
  }
  
  class SqlStateHandler implements IElementHandler
  {
    public void startElement(String name, Attributes attributes)
    {
      contextBuffer.setLength(0);
    }

    public void endElement(String name)
    {
      exception.sqlState = contextBuffer.toString();
    }
  }
  
  public DatabaseQueryResultBean getResult()
  {
    return dbResult;
  }
  
  public ServerException getException()
  {
    return exception;
  }

  protected String[] getElements()
  {
    return mapOfElems;
  }

  protected IElementHandler[] getHandlers()
  {
    return handlers;
  }
}
