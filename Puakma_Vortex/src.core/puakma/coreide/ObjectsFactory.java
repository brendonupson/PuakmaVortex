/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    May 18, 2005
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

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import puakma.coreide.database.DatabaseSchemeBean;
import puakma.coreide.database.XmlDatabaseStructureParser;
import puakma.coreide.designer.ApplicationStructureBean.DObject;
import puakma.coreide.objects2.Database;
import puakma.coreide.objects2.DatabaseConnection;
import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.JavaObject;
import puakma.coreide.objects2.Keyword;
import puakma.coreide.objects2.Role;
import puakma.coreide.objects2.Server;
import puakma.coreide.objects2.Table;
import puakma.coreide.objects2.TableColumn;
import puakma.utils.XmlUtils;

/**
 * The only purpose of this class is to create objects independent on the connected application,
 * server. So you can prefill object, and then add it to the application/server, and it
 * automatically commits changes.
 *
 * @author Martin Novak
 */
public class ObjectsFactory
{
  private static int[] VALID_TYPES = {
    DesignObject.TYPE_ACTION, DesignObject.TYPE_LIBRARY, DesignObject.TYPE_SCHEDULEDACTION,
    DesignObject.TYPE_WIDGET, DesignObject.TYPE_PAGE, DesignObject.TYPE_RESOURCE,
    DesignObject.TYPE_DOCUMENTATION, DesignObject.TYPE_JAR_LIBRARY, DesignObject.TYPE_CONFIGURATION,
  };
  /**
   * This function creates a new role.
   *
   * @param roleName is the name of the role
   * @param description is the description of the role
   * @return new Role object
   */
  public static Role createRole(String roleName, String description)
  {
    RoleImpl role = new RoleImpl(null);

    role.setName(roleName);
    role.setDescription(description);

    return role;
  }

  /**
   * This function creates a new keyword.
   *
   * @param keywordName
   * @return new Keyword object
   */
  public static Keyword createKeyword(String keywordName)
  {
    KeywordImpl kw = new KeywordImpl(null);
    kw.setName(keywordName);
    return kw;
  }

  /**
   * Creates a new empty database connection object.
   * 
   * @param dbconName is the database connection name
   * @return new DatabaseConnection object
   */
  public static DatabaseConnection createDbConnection(String dbconName)
  {
    DatabaseConnectionImpl dbo = new DatabaseConnectionImpl(null);
    dbo.setName(dbconName);

    return dbo;
  }
  
  public static Database createDatabase()
  {
    DatabaseImpl db = new DatabaseImpl(null);
    db.setNew(true);
    return db;
  }
  
  public static DesignObject createDesignObject(String name, int designType)
  {
    if(isValidDesignObjectType(designType) == false)
      throw new IllegalArgumentException("Invalid design type");

    DesignObject dob = null;
    switch(designType) {
      case DesignObject.TYPE_ACTION:
      case DesignObject.TYPE_LIBRARY:
      case DesignObject.TYPE_SCHEDULEDACTION:
      case DesignObject.TYPE_WIDGET:
        dob = new JavaObjectImpl(null, designType);
      break;
      case DesignObject.TYPE_PAGE:
      case DesignObject.TYPE_RESOURCE:
      case DesignObject.TYPE_DOCUMENTATION:
      case DesignObject.TYPE_JAR_LIBRARY:
      case DesignObject.TYPE_CONFIGURATION:
        dob = new ResourceObjectImpl(null, designType);
      break;
    }

    dob.setName(name);
    return dob;
  }
  
  public static JavaObject createJavaObject(String packageName, String className, int designType)
  {
    assert PuakmaLibraryUtils.isValidJavaObjectType(designType) : "Invalid design type for java object";
    
    JavaObject jo = new JavaObjectImpl(null, designType);
    jo.setPackage(packageName);
    jo.setClassName(className);
    return jo;
  }
  
  public static JavaObject createJavaObject(String name, String packageName, String className, int designType)
  {
    JavaObject jo = createJavaObject(packageName, className, designType);
    jo.setName(name);
    return jo;
  }

  /**
   * This function creates connection to the server without global notification
   * to the ServerManager.
   *
   * @param prefs is the connection preferences set to the server
   * @return Server object instance
   */
  public static Server createServer(ConnectionPrefsImpl prefs)
  {
    ServerImpl impl = new ServerImpl();
    impl.init(prefs);
    return impl;
  }
  
  /**
   * Checks if design type is valid design type for design object. Note that this
   * also thinks that DesignObject.TYPE_JAR_LIBRARY is ok type.
   * @param designType is the design object type
   *
   * @return true if this design object type can be used within application
   */
  public static boolean isValidDesignObjectType(int designType)
  {
    for(int i = 0; i < VALID_TYPES.length; ++i) {
      if(designType == VALID_TYPES[i])
        return true;
    }
    
    return false;
  }

  /**
   * This function creates a new empty table with some name.
   *
   * @param name is the name of the table
   * @return Table object
   */
  public static Table createTable(String name)
  {
    TableImpl table = new TableImpl(null);
    table.setName(name);
    return table;
  }

  public static TableColumn createTableColumn(String name)
  {
    TableColumnImpl col = new TableColumnImpl(null);
    col.setName(name);
    return col;
  }

  /**
   * Sets up design object from bean which has data transfered directly from the
   * server.
   */
  static DesignObjectImpl setupDesignObjectFromBean(DObject dbean, RefreshEventInfoImpl info)
  {
    int type = dbean.designType;
    if(type == DesignObject.TYPE_LIBRARY && dbean.isLibrary)
      type = DesignObject.TYPE_JAR_LIBRARY;
    DesignObjectImpl dbo = (DesignObjectImpl) ObjectsFactory.createDesignObject(dbean.name, type);
    dbo.refreshFrom(dbean, info, true);
    dbo.setDirty(false);
    dbo.setValid();
    return dbo;
  }

  /**
   * Checks the {@link DesignObject} name, and if it is not valid, returns non
   * null {@link String} with description of this error. If the name is ok,
   * returns null.
   */
  public static String getDesignObjectNameError(String name)
  {
    if(name == null)
      return "Name of the design object not set";
    if(name.length() == 0)
      return "Name of the design object cannot be empty";
    
    char[] illegalChars = {
      ' ', '\t', '\n', '\r'  
    };
    
    for(int i = 0; i < illegalChars.length; ++i) {
      if(name.indexOf(illegalChars[i]) != -1)
        return "Illegal character '" + illegalChars[i] + "' in the design object name";
    }
    
    return null;
  }
}
