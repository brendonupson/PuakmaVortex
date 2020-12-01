/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Nov 4, 2004
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide.objects2;


/**
 * This classes handles server changes.
 *
 * @author Martin Novak
 */
public interface ServerListener
{
  /**
   * This event is fired when connection to the server is closed
   * @param server
   */
  public void closed(Server server);
  
  /**
   * This is fired when is some application added to the server.
   *
   * @param server is Server object
   * @param application is application informations
   */
  public void addApplication(Server server, Application application);
  
  /**
   * This is fired when some application is removed from the server.
   *
   * @param server is the server fromwhich is app removed
   * @param application is the application to be removed
   */
  public void removeApplication(Server server, Application application);
  
  /**
   * This is fired when application properties changes.
   *
   * @param server is the server associated with app
   * @param application is changed application
   */
  public void changeApplication(Server server, Application application);
  
  /**
   * TODO: remove this event
   * This event is fired when we refresh all applicatoins. It means, that if we
   * receive this signal, we should delete all referenced existing applications
   * in eg. UI, and replace them with these ones.
   *
   * @param server connection to the subscribed server
   * @param applications array with all applications on the server
   */
  public void refreshApplications(Server server, Application[] applications);

  /**
   * This is fired when something in the list of applications occurs. This can
   * be rename, update of properties, connecting, disconnecting, etc...
   *
   * @param event is the event which occurs
   */
  public void serverEvent(ServerEvent event);
}
