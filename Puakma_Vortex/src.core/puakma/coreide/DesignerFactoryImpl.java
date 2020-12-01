/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Oct 6, 2005
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

import java.lang.reflect.Constructor;

import puakma.SOAP.SoapProxy;
import puakma.coreide.designer.AppDesigner;
import puakma.coreide.designer.DatabaseDesigner;

public class DesignerFactoryImpl implements DesignerFactory
{
  private static final Class<? extends AppDesigner> appDesignerIface = AppDesigner.class;
  private static final Class<? extends DatabaseDesigner> dbDesignerIface = DatabaseDesigner.class;
  
  private Class<? extends AppDesigner> appDesignerClass;
  private Class<? extends DatabaseDesigner> dbDesignerClass;
  
  private AppDesigner appDesigner;
  private DatabaseDesigner dbDesigner;
  private ConnectionPrefsImpl prefs;

  /**
   * This is the default implementation which setups the default designer which is really
   * communicating with the server.
   */
  public DesignerFactoryImpl()
  {
    super();
  }
  
  public DesignerFactoryImpl(Class<? extends AppDesigner> appDesignerClass)
  {
    this.appDesignerClass = appDesignerClass;
  }
  
  public DesignerFactoryImpl(Class<? extends AppDesigner> appDesignerClass,
                             Class<? extends DatabaseDesigner> databaseDesignerClass)
  {
    checkIface(appDesignerIface, appDesignerClass);
    this.appDesignerClass = appDesignerClass;
    checkIface(dbDesignerIface, databaseDesignerClass);
    this.dbDesignerClass = databaseDesignerClass;
  }
  
  /**
   * This constructor is usable to create only when testing some class.
   *
   * @param appDesigner is the instance of the application designer
   */
  public DesignerFactoryImpl(AppDesigner appDesigner)
  {
    this.appDesigner = appDesigner;
  }
  
  /**
   * This constructor is usable to create only when testing some class.
   *
   * @param appDesigner is the instance of the application designer
   * @param dbDesigner is the instance of the database designer
   */
  public DesignerFactoryImpl(AppDesigner appDesigner, DatabaseDesigner dbDesigner)
  {
    this.appDesigner = appDesigner;
    this.dbDesigner = dbDesigner;
  }
  
  /**
   * This constructor is usable to create only when testing some class.
   *
   * @param dbDesigner is the instance of the database designer
   */
  public DesignerFactoryImpl(DatabaseDesigner dbDesigner)
  {
    this.dbDesigner = dbDesigner;
  }

  public void setupConnectionPreferences(ConnectionPrefs prefs)
  {
    this.prefs = new ConnectionPrefsImpl();
    this.prefs.copyFrom(prefs);
  }
  
  /**
   * Checks if class is inherited from interface. If not, throws some runtime exception.
   *
   * @param wantIface is the interface we want in the class
   * @param haveClass is the class which we have
   */
  private void checkIface(Class<?> wantIface, Class<?> haveClass)
  {
    assert wantIface.isInterface() : wantIface.getName() + " is not interface.";
    
    Class<?>[] ifaces = haveClass.getInterfaces();
    for(int i = 0; i < ifaces.length; ++i) {
      if(wantIface.equals(ifaces[i]))
        return;
    }
    
    // ALSO CHECK INTERFACES OF ALL SUPERCLASSES
    Class<?> superClazz = haveClass.getSuperclass();
    if(superClazz != null) {
      checkIface(wantIface, superClazz);
      return;
    }
    
    throw new IllegalArgumentException(haveClass.getName() + " is not inherited from " +
            "interface " + wantIface.getName());
  }

  public AppDesigner newAppDesigner(ConnectionPrefs prefs)
  {
    return newAppDesigner(prefs.getDesignerPath(), prefs.getUser(), prefs.getPwd());
  }

  public AppDesigner newAppDesigner(String baseSOAPDesignerPath, String userName, String pwd)
  {
    String url = baseSOAPDesignerPath + ServerImpl.APP_DESIGNER_EXEC_PATH;
    
    if(appDesigner != null)
      return appDesigner;
    else if(appDesignerClass != null) {
      try {
        final Class<?>[] ctorType = new Class[] {String.class, String.class, String.class};
        Constructor<? extends AppDesigner> con = appDesignerClass.getConstructor(ctorType);
        AppDesigner designer = con.newInstance(new Object[] {url, userName, pwd});
        return designer;
      }
      catch(Exception e) {
        throw new RuntimeException("Cannot instantiate class " + appDesignerClass.getName());
      }
    }
    else
      return (AppDesigner) SoapProxy.createSoapClient(AppDesigner.class, url, userName, pwd);
  }

  public DatabaseDesigner newDbDesigner()
  {
    assert prefs == null : "You have to call setup before creating new database designer";
    
    return newDbDesigner(prefs.getDesignerPath(), prefs.getUser(), prefs.getPwd());
  }
  
  public DatabaseDesigner newDbDesigner(ConnectionPrefs prefs)
  {
    return newDbDesigner(prefs.getDesignerPath(), prefs.getUser(), prefs.getPwd());
  }

  public DatabaseDesigner newDbDesigner(String baseSOAPDesignerPath, String user, String pwd)
  {
    String url = baseSOAPDesignerPath + ServerImpl.DATABASE_DESIGNER_EXEC_PATH;
    
    if(dbDesigner != null)
      return dbDesigner;
    else if(dbDesignerClass != null) {
      try {
        final Class<?>[] ctorType = new Class[] {String.class, String.class, String.class};
        Constructor<? extends DatabaseDesigner> con = dbDesignerClass.getConstructor(ctorType);
        DatabaseDesigner designer = con.newInstance(new Object[] { url, user, pwd } );
        return designer;
      }
      catch(Exception e) {
        throw new RuntimeException("Cannot instantiate class " + appDesignerClass.getName());
      }
    }
    else
      return (DatabaseDesigner) SoapProxy.createSoapClient(DatabaseDesigner.class, url, user, pwd);
  }
}
