/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Dec 13, 2004
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide.designer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import puakma.coreide.PuakmaLibraryManager;
import puakma.utils.lang.StringUtil;

/**
 * XML parser which parses the whole server xml file. the xml file format
 * is described in the file
 * 
 * @author Martin Novak
 */
public class XMLServerParser extends DefaultHandler implements PuakmaXmlCodes
{
  /**
   * Buffer for internal datas
   */
  StringBuffer contextBuffer = new StringBuffer();

  static final String mapOfElems[] = { ELEM_PUAKMA_SERVER, ELEM_PUAKMA_APPLICATION,
                                       ELEM_SERVER_INFO_STRING, ELEM_PUAKMA_APPLICATION_DESC
  };

  final IElementHandler[] handlers = { new PuakmaServerHandler(), new PuakmaAppHandler(),
                                       new ServerInfoHandler(), new AppDescription(),
  };
  
  ServerStructureBean server;
  private ApplicationStructureBean app;
  boolean processComment = false;
  
  /**
   * Handles PuakmaServer node.
   */
  class PuakmaServerHandler implements IElementHandler
  {
    public void startElement(String name, Attributes attributes) {
      server = new ServerStructureBean();
      server.x500Name = StringUtil.safeString(attributes.getValue(ATT_X500NAME));
    }
    public void endElement(String name) { }
  }
  
  /**
   * Handles application.
   */
  class PuakmaAppHandler implements IElementHandler {
    public void startElement(String name, Attributes attributes)
    {
      app = new ApplicationStructureBean();
      app.appGroup = StringUtil.safeString(attributes.getValue(ATT_GROUP));
      app.appName = StringUtil.safeString(attributes.getValue(ATT_NAME));
      app.appId = Long.parseLong(attributes.getValue(ATT_ID));
      app.templateName = StringUtil.safeString(attributes.getValue(ATT_TEMPLATE));
      app.inheritFrom = StringUtil.safeString(attributes.getValue(ATT_INHERIT));
      server.apps.add(app);
    }
    public void endElement(String name)
    {
      if(app.description == null)
        app.description = StringUtil.EMPTY_STRING;
      
      app = null;
    }
  }

  class ServerInfoHandler implements IElementHandler {
    public void startElement(String name, Attributes attributes)
    {
      contextBuffer.setLength(0);
    }
    public void endElement(String name)
    {
      String comment = contextBuffer.toString();
      if(comment.length() > 0) {
        server.serverString = comment;
      }
    }
  }
  
  class AppDescription implements IElementHandler {
    public void startElement(String name, Attributes attributes)
    {
      contextBuffer.setLength(0);
    }
    public void endElement(String name)
    {
      String comment = contextBuffer.toString();
      if(comment.length() > 0) {
        app.description = comment;
      }
    }
  }
  
  public XMLServerParser()
  {
    assert(handlers.length == mapOfElems.length);
  }

  public void characters(char[] ch, int start, int length) throws SAXException
  {
    contextBuffer.append(ch, start, length);
  }

  public void endDocument() throws SAXException
  {
    super.endDocument();
  }

  public void endElement(String uri, String localName, String qName)
      throws SAXException
  {
    for(int i = 0; i < mapOfElems.length; ++i) {
      if(mapOfElems[i].equals(qName)) {
        handlers[i].endElement(qName);
        break;
      }
    }
  }

  public void startDocument() throws SAXException
  {
    super.startDocument();
  }

  public void startElement(String uri, String localName, String qName,
      Attributes attributes) throws SAXException
  {
    for(int i = 0; i < mapOfElems.length; ++i) {
      try {
      if(mapOfElems[i].equals(qName)) {
        handlers[i].startElement(qName, attributes);
        break;
      }
      }
      catch(Exception e) {
        PuakmaLibraryManager.log(e);
      }
    }
  }

  public ServerStructureBean getServer()
  {
    return server;
  }
}
