/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Oct 29, 2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.project;

import org.eclipse.core.resources.IProject;

class ExternalProjectReference
{
  /**
   * If the project is loaded, we will handle the project events.
   */
  boolean loaded;
  
  /**
   * This is the external project name.
   */
  IProject project;

  /**
   * If the project exists, and this will be true.
   */
  boolean valid;

  /**
   * If the project is closed this will be true.
   */
  boolean closed;
}
