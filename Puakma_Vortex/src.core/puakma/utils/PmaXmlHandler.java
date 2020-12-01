/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Oct 26, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.utils;

import java.lang.reflect.Field;
import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import puakma.coreide.PuakmaLibraryManager;
import puakma.coreide.designer.IElementHandler;

public abstract class PmaXmlHandler extends DefaultHandler
{
  /**
   * This is the place where are saved element handlers, and element names. Keys are Strings, values
   * are IElementHandlers
   */
  private HashMap<String, IElementHandler> handlers = new HashMap<String, IElementHandler>();
  
  /**
   * Buffer for internal datas
   */
  protected StringBuffer contextBuffer = new StringBuffer();
  
  public void startDocument() throws SAXException
  {
    String[] elements = getElements();
    IElementHandler[] handlers = getHandlers();
    assert elements.length == handlers.length;
    
    for(int i = 0; i < elements.length; ++i)
      this.handlers.put(elements[i], handlers[i]);
  }

  protected abstract String[] getElements();
  
  protected abstract IElementHandler[] getHandlers();

  protected void attributesToBean(Object bean, Attributes attribs) throws NoSuchFieldException
  {
    Class<? extends Object> clz = bean.getClass();
    for(int i = 0; i < attribs.getLength(); ++i) {
      String name = attribs.getQName(i);
      Field f = clz.getField(name);
      if(f == null)
        throw new IllegalAccessError("Field " + attribs.getQName(i) + " has not been found.");
      
      Class<?> type = f.getType();
      try {
        String value = attribs.getValue(i);
        if(type == Boolean.TYPE) {
          boolean boolVal = Boolean.getBoolean(value);
          f.setBoolean(bean, boolVal);
        }
        else if(type == Byte.TYPE) {
          byte val = Byte.parseByte(value);
          f.setByte(bean, val);
        }
        else if(type == Character.TYPE) {
          if(value.length() > 0)
            f.setChar(bean, value.charAt(0));
        }
        else if(type == Short.TYPE) {
          short val = Short.parseShort(value);
          f.setShort(bean, val);
        }
        else if(type == Integer.TYPE) {
          int intValue = Integer.parseInt(value);
          f.setInt(bean, intValue);
        }
        else if(type == Long.TYPE) {
          long val = Long.parseLong(value);
          f.setLong(bean, val);
        }
        else if(type == String.class) {
          f.set(bean, new String(attribs.getValue(i)));
        }
        else if(type == Float.TYPE) {
          float val = Float.parseFloat(value);
          f.setFloat(bean, val);
        }
        else if(type == Double.TYPE) {
          double dbl = Double.parseDouble(value);
          f.setDouble(bean, dbl);
        }
        else {
          System.out.println("Attribute " + attribs.getQName(i) + " doesn't match with bean field type");
        }
      }
      catch(NumberFormatException ex) {  }
      catch(IllegalArgumentException e) {  }
      catch(IllegalAccessException e) {  }
    }
//    else {
//      System.out.println("Attribute " + attribs.getLocalName(i) + " doesn't have bean field");
//    }
  }
  
  public void characters(char[] ch, int start, int length) throws SAXException
  {
    contextBuffer.append(ch, start, length);
  }
  
  public final void startElement(String uri, String localName, String qName,
      Attributes attributes) throws SAXException
  {
    try {
      IElementHandler handler = handlers.get(qName);
      if(handler != null)
        handler.startElement(qName, attributes);
    }
    catch(Exception e) {
      PuakmaLibraryManager.log(e);
    }
  }

  public final void endElement(String uri, String localName, String qName)
      throws SAXException
  {
    try {
      IElementHandler handler = handlers.get(qName);
      if(handler != null)
        handler.endElement(qName);
    }
    catch(Exception e) {
      PuakmaLibraryManager.log(e);
    }
  }
}
