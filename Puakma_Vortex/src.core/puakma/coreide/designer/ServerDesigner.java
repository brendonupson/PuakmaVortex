/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    05/06/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide.designer;

import java.io.IOException;

import puakma.SOAP.SOAPFaultException;

public interface ServerDesigner
{
  public byte[] listApplications() throws IOException, SOAPFaultException;

  public String getServerInfo() throws IOException, SOAPFaultException;

  public String initiateConnection() throws IOException, SOAPFaultException;

  public void removeApplication(long appId) throws IOException, SOAPFaultException;

  public String[] pingDatabaseServer(String driverClass, String userName,
                                     String password, String databaseUrl,
                                     String databaseName, String dbOptions) throws IOException,
                                                                           SOAPFaultException;

  public String[] ping() throws IOException, SOAPFaultException;

  public String getLastLogItems(long limitItems, long lastLogItemId) throws IOException,
                                                                    SOAPFaultException;

  public String executeCommand(String command) throws IOException, SOAPFaultException;
  
  public String[][] listEnvironmentProperties() throws IOException, SOAPFaultException;
  
  public String getTornadoDatabaseStructureXml() throws IOException, SOAPFaultException;
}
