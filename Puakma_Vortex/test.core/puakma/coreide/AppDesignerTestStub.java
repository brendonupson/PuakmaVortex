/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    24/11/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide;

import java.io.IOException;

import puakma.SOAP.SOAPFaultException;
import puakma.coreide.designer.AppDesigner;

public class AppDesignerTestStub implements AppDesigner
{
  public void addAppParam(int appId, String paramName, String paramValue) throws IOException, SOAPFaultException
  {
    throw new IllegalStateException("Not implemented yet");
  }

  public void addDesignObjectParam(long objId, String paramName, String paramValue) throws IOException, SOAPFaultException
  {
    throw new IllegalStateException("Not implemented yet");
  }

  public void clearAppParams(int appId) throws IOException, SOAPFaultException
  {
    throw new IllegalStateException("Not implemented yet");
  }

  public void flushDesignCache() throws IOException, SOAPFaultException
  {
    throw new IllegalStateException("Not implemented yet");
  }

  public String getApplicationXml(long appId) throws IOException, SOAPFaultException
  {
    throw new IllegalStateException("Not implemented yet");
  }

  public String[][] getDesignObjectsSizeCrc32(long[] ids) throws IOException, SOAPFaultException
  {
    throw new IllegalStateException("Not implemented yet");
  }

  public long[] listDatabaseConnectionIds(long appId) throws IOException, SOAPFaultException
  {
    throw new IllegalStateException("Not implemented yet");
  }

  public void removeDatabaseConnection(long id) throws IOException, SOAPFaultException
  {
    throw new IllegalStateException("Not implemented yet");
  }

  public void removeDesignObject(long id) throws IOException, SOAPFaultException
  {
    throw new IllegalStateException("Not implemented yet");
  }

  public void removeKeyword(long kwId) throws IOException, SOAPFaultException
  {
    throw new IllegalStateException("Not implemented yet");
  }

  public void removePermission(long id) throws IOException, SOAPFaultException
  {
    throw new IllegalStateException("Not implemented yet");
  }

  public void removeRole(long id) throws IOException, SOAPFaultException
  {
    throw new IllegalStateException("Not implemented yet");
  }

  public long saveApplication(long appId, String group, String name, String inheritFrom, String templateName, String description) throws IOException, SOAPFaultException
  {
    throw new IllegalStateException("Not implemented yet");
  }

  public long saveDatabaseConnection(long id, long appId, String name, String description, String databaseName, String databaseUrl, String databaseURLOptions, String userName, String password, String driverClass) throws IOException, SOAPFaultException
  {
    throw new IllegalStateException("Not implemented yet");
  }

  public long saveKeyword(int appId, long kwId, String name, String[] data) throws IOException, SOAPFaultException
  {
    throw new IllegalStateException("Not implemented yet");
  }

  public long saveKeyword(long id, long appId, String name) throws IOException, SOAPFaultException
  {
    throw new IllegalStateException("Not implemented yet");
  }

  public long saveKeywordData(long kwId, String[] datas) throws IOException, SOAPFaultException
  {
    throw new IllegalStateException("Not implemented yet");
  }

  public long savePermission(long idPermission, long idRole, String name, String description) throws IOException, SOAPFaultException
  {
    throw new IllegalStateException("Not implemented yet");
  }

  public long saveRole(long appId, long idRole, String name, String description) throws IOException, SOAPFaultException
  {
    throw new IllegalStateException("Not implemented yet");
  }

  public long[] setAppParams(long appId, String[] paramNames, String[] paramValues) throws IOException, SOAPFaultException
  {
    throw new IllegalStateException("Not implemented yet");
  }

  public long[] setParams(long objId, String[] paramNames, String[] paramValues) throws IOException, SOAPFaultException
  {
    throw new IllegalStateException("Not implemented yet");
  }

  public long updateDesignObject(long id, long appId, String name, int designType, String contentType, String comment, String options, String inheritFrom) throws IOException, SOAPFaultException
  {
    throw new IllegalStateException("Not implemented yet");
  }

  public void updateDesignObjectName(long designObjectId, String newName) throws IOException, SOAPFaultException
  {
    throw new IllegalStateException("Not implemented yet");
  }
}
