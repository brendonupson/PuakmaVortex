/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 23, 2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.dbeditor.commands;

import org.eclipse.gef.commands.Command;

import puakma.utils.lang.ArrayUtils;
import puakma.utils.lang.PropertyManipulator;

/**
 * This is kinda special command which should be created after some property is being changed.
 *
 * @author Martin Novak
 */
public class PropertiesChangeCommand extends Command
{
  private PropertyManipulator[] propertyManipulators;
  private Object bean;
  private Object[] newValues;
  private Object[] oldValues;

  public PropertiesChangeCommand(Class clazz, String propertyName, Object bean, Object newValue)
  {
    this(clazz, new String[] { propertyName }, bean, new Object[] { newValue });
  }
  
  public PropertiesChangeCommand(Class clazz, String[] propertyNames, Object bean, Object[] newValues)
  {
    this.propertyManipulators = new PropertyManipulator[propertyNames.length];
    this.oldValues = new Object[propertyNames.length];
    this.bean = bean;
    for(int i = 0; i < propertyNames.length; ++i) {
      propertyManipulators[i] = new PropertyManipulator(clazz, propertyNames[i]);
      this.oldValues[i] = propertyManipulators[i].getPropertyFromObject(bean);
    }
    this.newValues = (Object[]) ArrayUtils.copyArray(newValues);
  }
  
  public void execute()
  {
    redo();
  }

  public void redo()
  {
    for(int i = 0; i < propertyManipulators.length;  ++i) {
      propertyManipulators[i].setPropertyOnObject(bean, newValues[i]);
    }
  }

  public void undo()
  {
    for(int i = 0; i < propertyManipulators.length;  ++i) {
      propertyManipulators[i].setPropertyOnObject(bean, oldValues[i]);
    }
  }
}
