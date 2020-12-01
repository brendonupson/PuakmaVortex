/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Mar 13, 2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.dbeditor.parts;

import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;

public interface FkEditPart extends EditPart
{
  /**
   * This function sets target connection. This is called when we don't have connection model object
   * at all, so we have to set it up manually to save some CPU.
   * 
   * @param part is the new target connection part
   */
  public void addTargetConnectionPart(ConnectionEditPart part);
}
