/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Dec 14, 2004
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import puakma.utils.StringNameValuePair;
import puakma.utils.lang.StringUtil;


/**
 * Helper class for managing application attributes taken from the xml from
 * the server.
 * 
 * @author Martin Novak
 */
public class ApplicationStructureBean
{
  public long appId;
  public String appName = StringUtil.EMPTY_STRING;
  public String appGroup = StringUtil.EMPTY_STRING;
  public String inheritFrom = StringUtil.EMPTY_STRING;
  public String templateName = StringUtil.EMPTY_STRING;
  public String description = StringUtil.EMPTY_STRING;
  public List<ApplicationStructureBean.ParamNameValue> params = new ArrayList<ApplicationStructureBean.ParamNameValue>();
  
  /*
   * These lists are for passing objects to application connection
   */
  public List<ApplicationStructureBean.DObject> designObjects = new ArrayList<ApplicationStructureBean.DObject>();

  /**
   * Elements are of type DatabaseConnectionObject
   */
  public List<ApplicationStructureBean.Database> dbConnections = new ArrayList<ApplicationStructureBean.Database>();
  public List<ApplicationStructureBean.Role> roles = new ArrayList<ApplicationStructureBean.Role>();
  public List<ApplicationStructureBean.Keyword> keywords = new ArrayList<ApplicationStructureBean.Keyword>();
  /**
   * Server's system properties. NameValuePair
   */
  public List<StringNameValuePair> sysProps = new ArrayList<StringNameValuePair>();
  
  public class ParamNameValue {
    public long    id;
    public String name = StringUtil.EMPTY_STRING;
    public String value = StringUtil.EMPTY_STRING;
  }
  
  public class Keyword {
    public String name = StringUtil.EMPTY_STRING;
    public long id;
    public List<ApplicationStructureBean.Keyword.KeywordData> datas = new ArrayList<ApplicationStructureBean.Keyword.KeywordData>();
    
    public class KeywordData {
      public long kwId;
      public int order;
      public String data = StringUtil.EMPTY_STRING;
    } // class keyworddata
  } // class keyword
  
  public class Role {
    public long id;
    public String name = StringUtil.EMPTY_STRING;
    public String roleDescription = StringUtil.EMPTY_STRING;
    public List<ApplicationStructureBean.Role.Permission> permissions = new ArrayList<ApplicationStructureBean.Role.Permission>();
    
    public class Permission {
      public long permId;
      public String permName = StringUtil.EMPTY_STRING;
      public String permDescription = StringUtil.EMPTY_STRING;
    }
  }
  
  public class Database {
    public long id;
    public String conName = StringUtil.EMPTY_STRING;
    public String dbName = StringUtil.EMPTY_STRING;
    public String url = StringUtil.EMPTY_STRING;
    public String urlOptions = StringUtil.EMPTY_STRING;
    public String driverClass = StringUtil.EMPTY_STRING;
    public String userName = StringUtil.EMPTY_STRING;
    public String pwd = StringUtil.EMPTY_STRING;
    public Date   created;
    public String createdBy = StringUtil.EMPTY_STRING;
    public String options = StringUtil.EMPTY_STRING;
    public String comment = StringUtil.EMPTY_STRING;
    public String dbInheritFrom = StringUtil.EMPTY_STRING;
  }
  
  public static class DObject {
    public long id;
    public int designType;
    public String name = StringUtil.EMPTY_STRING;
    public String dobjDescription = StringUtil.EMPTY_STRING;
    public String className = StringUtil.EMPTY_STRING;
    public String packageName = StringUtil.EMPTY_STRING;
    public boolean isLibrary;
    public String contentType = StringUtil.EMPTY_STRING;
    public String options = StringUtil.EMPTY_STRING;
    public String dobjInheritFrom = StringUtil.EMPTY_STRING;
    public int designDataSize;
    public int designSourceSize;
    public List<ApplicationStructureBean.ParamNameValue> parameters = new ArrayList<ApplicationStructureBean.ParamNameValue>();
    public long sourceCrc;
    public long dataCrc32;
    public Date lastUpdateTime;
    public String updatedBy = StringUtil.EMPTY_STRING;
  }
}
