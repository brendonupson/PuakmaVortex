/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Dec 13, 2004
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide.designer;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import puakma.coreide.PuakmaLibraryManager;
import puakma.utils.StringNameValuePair;
import puakma.utils.lang.StringUtil;

/**
 * XML parser which parses the whole application xml file. the xml file format
 * is described in the file.
 * 
 * TODO: handle better null int parsing
 * TODO: handle better non-null required values recovery
 * 
 * @author Martin Novak
 */
public class XMLWholeApplicationParser extends DefaultHandler implements
    PuakmaXmlCodes
{

  /**
   * Buffer for internal datas
   */
  StringBuffer contextBuffer = new StringBuffer();

  static final String mapOfElems[] = { ELEM_PUAKMA_APPLICATION, ELEM_PUAKMA_APPLICATION_DESC,
                                       ELEM_DESIGN_ELEMENT, ELEM_PUAKMA_DESIGN_ELEM_COMMENT,
                                       ELEM_APP_PARAM, ELEM_KEYWORD, ELEM_KEYWORD_DATA,
                                       ELEM_ROLE, ELEM_ROLE_DESC, ELEM_DESIGN_PARAM,
                                       ELEM_DATABASE, ELEM_DATABASE_DESC, ELEM_PERMISSION,
                                       ELEM_PERMISSION_DESC, ELEM_SYSTEM_PROPERTY
  };

  final IElementHandler[] handlers = { new PuakmaApplicationHandler(), new PuakmaAppDeschandler(),
                                       new DesignElementHandler(), new DesignElementCommentHandler(),
                                       new AppParamHandler(), new KeywordHandler(), new KeywordDataHandler(),
                                       new RoleHandler(), new RoleDescHandler(), new DesignParamHandler(),
                                       new DatabaseHandler(), new DbDescHandler(), new PermissionHandler(),
                                       new PermissionDescHandler(), new SystemPropertyHandler(),
  };
  
  ApplicationStructureBean app;
  private ApplicationStructureBean.DObject dobj;
  boolean processComment = false;
  ApplicationStructureBean.Keyword kw;
  ApplicationStructureBean.Role role;
  ApplicationStructureBean.Role.Permission perm;
  private ApplicationStructureBean.Database dbObj;
  
  /**
   * Handles PuakmaApplication node.
   *
   * @author Martin Novak
   */
  class PuakmaApplicationHandler implements IElementHandler
  {
    int appId;

    public void startElement(String name, Attributes attributes) {
      int i = Integer.parseInt(attributes.getValue(ATT_ID));
      app = new ApplicationStructureBean();
      app.appId = i;
      app.appName = StringUtil.safeString(attributes.getValue(ATT_NAME));
      app.appGroup = StringUtil.safeString(attributes.getValue(ATT_GROUP));
      app.inheritFrom = StringUtil.safeString(attributes.getValue(ATT_INHERIT));
      app.templateName = StringUtil.safeString(attributes.getValue(ATT_TEMPLATE));
    }
    
    public void endElement(String name) {
    }
  }
  
  /**
   * Handles application description.
   */
  class PuakmaAppDeschandler implements IElementHandler {
    public void startElement(String name, Attributes attributes)
    {
      contextBuffer.setLength(0);
    }
    public void endElement(String name)
    {
      app.description = contextBuffer.toString();
    }
  }
  
  class DesignElementHandler implements IElementHandler {
    public void startElement(String name, Attributes attributes)
    {
      int type = Integer.parseInt(attributes.getValue(ATT_DESIGN_TYPE));
      String className = StringUtil.safeString(attributes.getValue(ATT_CLASS_NAME));
      String packageName = StringUtil.safeString(attributes.getValue(ATT_PACKAGE));
      String options = StringUtil.safeString(attributes.getValue(ATT_OPTIONS));
      int id = Integer.parseInt(attributes.getValue(ATT_ID));
      String contentType = StringUtil.safeString(attributes.getValue(ATT_CONTENT_TYPE));
      String inherit = StringUtil.safeString(attributes.getValue(ATT_INHERIT));
      String objName = StringUtil.safeString(attributes.getValue(ATT_NAME));
      String updated = StringUtil.safeString(attributes.getValue(ATT_UPDATED));
      String updatetBy = StringUtil.safeString(attributes.getValue(ATT_UPDATED_BY));
      Date lastUpdateTime = new Date(Long.parseLong(updated)); // THIS TIME IS IN GMT
      Calendar localCal = Calendar.getInstance(); // LOCAL CALENDAR BASED
                                                  // ON THE CURRENT TIME ZONE
      localCal.setTimeInMillis(lastUpdateTime.getTime());      // CONVERT TO LOCAL TIME
      lastUpdateTime = localCal.getTime();                     // AND SET BACK
      
      int dataLen = Integer.parseInt(attributes.getValue(ATT_DATA_LEN));
      int sourceLen = Integer.parseInt(attributes.getValue(ATT_SOURCE_LEN));
      String library = StringUtil.safeString(attributes.getValue(ATT_LIBRARY));
      boolean isLib = Boolean.valueOf(library).booleanValue();
      long sourceCrc = Long.parseLong(attributes.getValue(ATT_SOURCE_CRC32));
      long dataCrc = Long.parseLong(attributes.getValue(ATT_DATA_CRC32));

      // CREATE NEW OBJECT
      dobj = new ApplicationStructureBean.DObject();
      dobj.id = (id);
      dobj.designType = (type);
      dobj.name = objName;
      dobj.className = className;
      dobj.packageName = packageName;
      dobj.contentType = contentType;
      dobj.dobjInheritFrom = inherit;
      dobj.options = options;
      dobj.isLibrary = isLib;
      dobj.designDataSize = dataLen;
      dobj.designSourceSize = sourceLen;
      dobj.sourceCrc = sourceCrc;
      dobj.dataCrc32 = dataCrc;
      dobj.updatedBy = updatetBy;
      dobj.lastUpdateTime = lastUpdateTime;
      app.designObjects.add(dobj);
    }
    public void endElement(String name)
    {
    }
  }

  class DesignElementCommentHandler implements IElementHandler {
    public void startElement(String name, Attributes attributes)
    {
      contextBuffer.setLength(0);
    }
    public void endElement(String name)
    {
      String comment = contextBuffer.toString();
      if(comment.length() > 0) {
        dobj.dobjDescription = comment;
      }
    }
  }
  
  class DesignParamHandler implements IElementHandler {
    public void startElement(String name, Attributes attributes)
    {
      int id = Integer.parseInt(attributes.getValue(ATT_ID));
      String paramName = StringUtil.safeString(attributes.getValue(ATT_NAME));
      String value = StringUtil.safeString(attributes.getValue(ATT_VALUE));
      
      ApplicationStructureBean.ParamNameValue param = app.new ParamNameValue();
      param.id = id;
      param.name = paramName;
      param.value = value;
      dobj.parameters.add(param);
    }

    public void endElement(String name)
    {
    }
    
  }
  
  class AppParamHandler implements IElementHandler {
    public void startElement(String name, Attributes attributes)
    {
      ApplicationStructureBean.ParamNameValue param = app.new ParamNameValue();
      int id = Integer.parseInt(attributes.getValue(ATT_ID));
      param.id = id;
      param.name = StringUtil.safeString(attributes.getValue(ATT_NAME));
      param.value = StringUtil.safeString(attributes.getValue(ATT_VALUE));
      app.params.add(param);
    }
    public void endElement(String name)
    {
    }
  }
  class KeywordHandler implements IElementHandler {
    public void startElement(String name, Attributes attributes)
    {
      kw = app.new Keyword();
      kw.id = Integer.parseInt(attributes.getValue(ATT_ID));
      kw.name = StringUtil.safeString(attributes.getValue(ATT_NAME));
      app.keywords.add(kw);
    }
    public void endElement(String name)
    {
    }
  }
  class KeywordDataHandler implements IElementHandler {
    public void startElement(String name, Attributes attributes)
    {
      ApplicationStructureBean.Keyword.KeywordData kwd = kw.new KeywordData();
      kwd.data = StringUtil.safeString(attributes.getValue(ATT_DATA));
      kwd.kwId = Integer.parseInt(attributes.getValue(ATT_ID));
      kwd.order = Integer.parseInt(attributes.getValue(ATT_ORDER));
      kw.datas.add(kwd);
    }
    public void endElement(String name)
    {
    }
  }
  class RoleHandler implements IElementHandler {
    public void startElement(String name, Attributes attributes)
    {
      role = app.new Role();
      
      role.id = Long.parseLong(attributes.getValue(ATT_ID));
      role.name = StringUtil.safeString(attributes.getValue(ATT_NAME));
      
      app.roles.add(role);
    }
    public void endElement(String name)
    {
    }
  }
  class RoleDescHandler implements IElementHandler {
    public void startElement(String name, Attributes attributes)
    {
      contextBuffer.setLength(0);
    }
    public void endElement(String name)
    {
      String comment = contextBuffer.toString();
      if(comment.length() > 0) {
        role.roleDescription = comment;
      }
    }
  }
  
  class DatabaseHandler implements IElementHandler {
    public void startElement(String name, Attributes atts)
    {
      dbObj = app.new Database();
      dbObj.id = Long.parseLong(atts.getValue(ATT_ID));
      dbObj.conName = StringUtil.safeString(atts.getValue(ATT_NAME));
      dbObj.dbName = StringUtil.safeString(atts.getValue(ATT_DB_NAME));
      dbObj.url = StringUtil.safeString(atts.getValue(ATT_URL));
      dbObj.urlOptions = StringUtil.safeString(atts.getValue(ATT_URL_OPTIONS));
      dbObj.options = StringUtil.safeString(atts.getValue(ATT_OPTIONS));
      dbObj.userName = StringUtil.safeString(atts.getValue(ATT_USERNAME));
      dbObj.driverClass = StringUtil.safeString(atts.getValue(ATT_DRIVER));
      dbObj.pwd = StringUtil.safeString(atts.getValue(ATT_PWD));
      dbObj.comment = StringUtil.safeString(atts.getValue(ATT_DESC));
      dbObj.dbInheritFrom = StringUtil.safeString(atts.getValue(ATT_INHERIT));
      
      // now setup times...
      String updated = StringUtil.safeString(atts.getValue(ATT_UPDATED));
      //String updatetBy = StringUtil.safeString(atts.getValue(ATT_UPDATED_BY));
      Date d = Calendar.getInstance().getTime();
      if(updated != null && updated.length() > 0) {
        try {
          DateFormat df = DateFormat.getDateTimeInstance();
          d = df.parse(updated);
        }
        catch(ParseException e) {
          // cannot parse time, so remain the current one...
        }
      }
      dbObj.created = d;
      dbObj.createdBy = StringUtil.safeString(atts.getValue(ATT_CREATED));
      
      app.dbConnections.add(dbObj);
    }

    public void endElement(String name)
    {
    }
  }
  
  class DbDescHandler implements IElementHandler {
    public void startElement(String name, Attributes attributes)
    {
      contextBuffer.setLength(0);
    }

    public void endElement(String name)
    {
      String comment = contextBuffer.toString();
      if(comment.length() > 0) {
        dbObj.comment = comment;
      }
    }
  }
  
  class PermissionHandler implements IElementHandler {
    public void startElement(String name, Attributes attributes)
    {
      perm = role.new Permission();
      perm.permId = Long.parseLong(attributes.getValue(ATT_ID));
      perm.permName = StringUtil.safeString(attributes.getValue(ATT_NAME));
      role.permissions.add(perm);
    }

    public void endElement(String name)
    {
    }
  }

  class PermissionDescHandler implements IElementHandler {
    public void startElement(String name, Attributes attributes)
    {
      contextBuffer.setLength(0);
    }
    
    public void endElement(String name)
    {
      String comment = contextBuffer.toString();
      if(comment.length() > 0) {
        perm.permDescription = comment;
      }
    }
  }
  
  class SystemPropertyHandler implements IElementHandler
  {
    private StringNameValuePair sysProp;

    public void endElement(String name)
    {
      if(sysProp == null)
        return;
      String str = contextBuffer.toString();
      if(str.length() == 0) {
        sysProp = null;
        return;
      }
      
      sysProp.setValue(str);
      app.sysProps.add(sysProp);
      sysProp = null;
    }

    public void startElement(String name, Attributes attributes)
    {
      String propName = StringUtil.safeString(attributes.getValue(PuakmaXmlCodes.ATT_NAME));
      if(propName == null || propName.length() == 0)
        return;
      
      sysProp = new StringNameValuePair(propName);
      contextBuffer.setLength(0);
    }
    
  }
  
  public XMLWholeApplicationParser()
  {
    assert(handlers.length == mapOfElems.length);
  }

  public void characters(char[] ch, int start, int length) throws SAXException
  {
    contextBuffer.append(ch, start, length);
  }

  public void endDocument() throws SAXException
  {
    super.endDocument();
  }

  public void endElement(String uri, String localName, String qName)
      throws SAXException
  {
    for(int i = 0; i < mapOfElems.length; ++i) {
      if(mapOfElems[i].equals(qName)) {
        handlers[i].endElement(qName);
        break;
      }
    }
  }

  public void startDocument() throws SAXException
  {
    super.startDocument();
  }

  public void startElement(String uri, String localName, String qName,
      Attributes attributes) throws SAXException
  {
    for(int i = 0; i < mapOfElems.length; ++i) {
      try {
        if(mapOfElems[i].equals(qName)) {
          handlers[i].startElement(qName, attributes);
          break;
        }
      }
      catch(Exception e) {
        PuakmaLibraryManager.log(e);
      }
    }
  }

  /**
   * Returns filled application bean which represents server side status of the appliaction.
   */
  public ApplicationStructureBean getApplication()
  {
    return app;
  }
}
