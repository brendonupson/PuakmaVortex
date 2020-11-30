/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 2, 2006
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.TestCase;

public abstract class BaseTest extends TestCase
{
  /**
   * This function creates an empty connection preferences. This is useful as fake connection
   * preference for some tests which doesn't connect to the server.
   * 
   * @return ConnectionPrefs object
   */
  ConnectionPrefs createEmptyPrefs()
  {
    ConnectionPrefs prefs = new ConnectionPrefsImpl();
    prefs.setName("#test");
    prefs.setHost("localhost");
    prefs.setUser("SysAdmin");
    prefs.setPwd("pwd");
    return prefs;
  }
  
  ConnectionPrefs createValidPrefs()
  {
    ConnectionPrefs prefs = new ConnectionPrefsImpl();
    prefs.setName("#valid");
    prefs.setHost("localhost");
    prefs.setPort(8080);
    prefs.setUser("SysAdmin");
    prefs.setPwd("gagarin;");
    return prefs;
  }
  
  DesignerFactory createDesignerFactory()
  {
    DesignerFactoryImpl factory = new DesignerFactoryImpl();
    return factory;
  }
  
  ServerImpl createValidServer()
  {
    ServerImpl s = new ServerImpl();
    ConnectionPrefsImpl prefs = (ConnectionPrefsImpl) createValidPrefs();
    DesignerFactory factory = createDesignerFactory();
    s.setDesignerFactory(factory);
    s.init(prefs);
    return s;
  }

  /**
   * Executes the first method called name filled with arguments. Note that no
   * special handling is performed here.
   */
  public static Object execute(String name, Object source, Object[] args) throws IllegalArgumentException,
                                                                         IllegalAccessException,
                                                                         InvocationTargetException
  {
    Method[] methods = source.getClass().getMethods();
    for(int i = 0; i < methods.length; ++i) {
      Method m = methods[i];
      if(m.getName().equals(name)) {
        return m.invoke(source, args);
      }
    }
    return null;
  }
}
