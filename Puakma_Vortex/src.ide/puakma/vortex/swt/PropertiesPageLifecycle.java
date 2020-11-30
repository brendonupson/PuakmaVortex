/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 22, 2006
 * 
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.swt;

/**
 * This interface is being implemented by dialogs which want to work with some master/detail
 * information. Lifecycle is like that:
 * <ul>
 * <li>select some item in UI</li>
 * <li>createComposite</li>
 * <li>setup</li>
 * <li>save</li>
 * <li>reset</li>
 * <li>setup</li>
 * <li>save</li>
 * <li>reset</li>
 * <li>dispose</li>
 * </ul>
 * 
 * @author Martin Novak
 */
public interface PropertiesPageLifecycle
{
  /**
   * This function should save the dialog data back to the model. This happens when before some other
   * object selects the different item in user interface.
   */
  public void save();
  
  /**
   * Implementors should unhook here any listeners, etc... This occurs before setup if we are setting
   * up the same object type. Change dirty status, etc...
   */
  public void reset();
  
  /**
   * Setups the model object for the controller
   */
  public void setup(Object model);
  
  /**
   * This should free resources allocated by the controller.
   */
  public void dispose();
  
  /**
   * Gets the dirty status. So if the content of the page is modified, it should setup dirty status,
   * and then it should be able to save if necessary.
   * 
   * @return true if the content has been modified
   */
  public boolean isDirty();
}
