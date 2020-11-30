/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jan 27, 2006
 * 
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.dbeditor;

import org.eclipse.gef.requests.CreationFactory;

import puakma.coreide.FkConnectionImpl;
import puakma.coreide.ObjectsFactory;
import puakma.coreide.objects2.Table;
import puakma.coreide.objects2.TableColumn;

/**
 * This is the element factory for adding new objects using palette.
 *
 * @author Martin Novak &lt;mn@puakma.net&gt;
 */
public class PaletteDataElementFactory implements CreationFactory
{
  private Class<?> clazz;

  public PaletteDataElementFactory(Class<?> clazz)
  {
    this.clazz = clazz;
  }

  public Object getNewObject()
  {
    try {
      if(clazz == Table.class) {
        return ObjectsFactory.createTable("Xxxx");
      }
      else if(clazz == TableColumn.class) {
        return ObjectsFactory.createTableColumn("Xxxx");
      }
//      else if(clazz == ConnectionImpl.class) {
//        return new ConnectionImpl();
//      }
      return clazz.newInstance();
    }
    catch(Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public Object getObjectType()
  {
    return clazz;
  }
}
