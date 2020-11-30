/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    30/06/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide.event;

import puakma.coreide.InconsistencyEventImpl;
import puakma.coreide.PuakmaCoreException;
import puakma.coreide.VortexMultiException;

/**
 * This is a inconsistencies list which tells client what is inconsistent, and
 * the client has to deal with it somehow. There is the current work flow:
 * 
 * <ol>
 * <li>application.refresh => checks inconsistencies, and fires up event in the
 * ServerManager
 * <li>client receives that event (this object), and eg shows dialog to user
 * <li>When the user corrects the data, it updates event, and client should
 * show user whether it is ok. This would be done using
 * {@link #verifyInconsistency(InconsistencyEventImpl)} if it is not ok, displays to user
 * the error
 * <li>when user is done with correcting the stuff, client should call
 * resolveAll method. If there is something wrong or not all stuff has been
 * corrected, it throws an exception. Note that here application might reload
 * itself from the server, so client has to reload list of inconsistencies by
 * itself.
 * </ol>
 * 
 * @author Martin Novak
 */
public interface InconsistenciesList
{
  public InconsistencyEvent[] listEvents();

  /**
   * Resolves all resolvable inconsistencies. If there are some inconsistencies
   * missing, it throws and exception with the information about those
   * inconsistencies. Note that after resolving inconsistencies, it marks
   * inconsistencies as invalid, and also removes them from the list of
   * inconsistencies.
   * 
   * @throws VortexaMultiException when some inconsistency still cannot be
   *           resolved because of some reason.
   */
  public void resolveAll() throws VortexMultiException;
  
  /**
   * Checks if the resolution is ok. So for example if the resolution for rename
   * is invalid, returns false
   */
  public void verifyInconsistency(InconsistencyEventImpl event) throws PuakmaCoreException;

  /**
   * Gets the event.
   */
  public InconsistencyEvent getEvent(int index);
}
