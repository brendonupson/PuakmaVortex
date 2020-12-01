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

public interface DownloadDesigner
{
  public boolean uploadDesign(long designId, boolean isSource, byte[] data) throws IOException,
                                                                           SOAPFaultException;

  public boolean uploadDesign(long designId, boolean isSource, byte[] data,
                              boolean flushCache) throws IOException, SOAPFaultException;

  public byte[] downloadDesign(long designId, boolean isSource) throws IOException,
                                                               SOAPFaultException;

  public byte[] downloadPmx(long id, boolean includeSource) throws IOException,
                                                           SOAPFaultException;

  public int uploadPmx(String appGroup, String appName, byte[] pmxFile) throws IOException,
                                                                       SOAPFaultException;
}
