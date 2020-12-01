/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Sep 15, 2005
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

import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

/**
 * This class is representing very simple xml writer.
 * @author Martin Novak
 */
public class SimpleXmlWriter
{
  private static final String CONST_ENDTAG_END = ">\n";

  private static final String CONST_ENDTAG_START = "</";

  private static final String CONST_TAG_END = ">";

  private static final String CONST_TAG_START = "<";

  public static final int FLAG_NO_EMPTY_ATTRIBUTES = 0x01;

  private StringBuffer sb = new StringBuffer();
  
  private Stack<String> stack = new Stack<String>();

  private int flags;
  
  public SimpleXmlWriter()
  {
  }
  
  public SimpleXmlWriter(int flags)
  {
    this.flags = flags;
  }

  public void append(SimpleXmlWriter xml)
  {
    if(xml.stack.size() != 0)
      throw new IllegalStateException("Appending non-closed stack");
    sb.append(xml.sb);
  }
  
  public void addTag(String name, String[] attribs, String[] vals, boolean close)
  {
    if(close == false)
      stack.push(name);
    
    sb.append(CONST_TAG_START);
    sb.append(name);
    
    if(attribs != null) {
      for(int i = 0; i < attribs.length; ++i) {
        if(vals[i] == null)
          continue;
        if(((flags & FLAG_NO_EMPTY_ATTRIBUTES) == FLAG_NO_EMPTY_ATTRIBUTES) && vals[i].length() == 0)
          continue;
        sb.append(" " + attribs[i] + "=\"");
        encode(vals[i]);
        sb.append("\"");
      }
    }
    
    if(close)
      sb.append(" />\n");
    else {
      sb.append(CONST_TAG_END);
      sb.append('\n');
    }
  }
  
  public void addTag(String name, boolean close)
  {
    addTag(name, (StringNameValuePair[]) null, close);
  }

  public void addTag(String name)
  {
    addTag(name, (StringNameValuePair[]) null, false);
  }
  
  public void addTag(String name, Map<String, String> map, boolean close)
  {
    int size = map.keySet().size();
    String[] names = new String[size];
    String[] vals = new String[size];
    int i = 0;
    for(String key : map.keySet()) {
      names[i] = key;
      String value = map.get(key);
      if(value == null)
        continue;
      if(((flags & FLAG_NO_EMPTY_ATTRIBUTES) == FLAG_NO_EMPTY_ATTRIBUTES) && value.length() == 0)
        continue;
      vals[i] = value;
      i++;
    }
    
    addTag(name, names, vals, close);
  }
  
  /**
   * This function adds a new tag where attributes are stored in StringNameValuePair object
   * array.
   * 
   * @param tagName is the name of the xml tag
   * @param attributes is array with all attributes, and its values
   * @param close if true then we should also close the tag
   */
  public void addTag(String tagName, StringNameValuePair[] attributes, boolean close)
  {
    if(close == false)
      stack.push(tagName);
    
    sb.append(CONST_TAG_START);
    sb.append(tagName);
    
    if(attributes != null)
    for(int i = 0; i < attributes.length; ++i) {
      if(attributes[i].getValue() == null)
        continue;
      if(((flags & FLAG_NO_EMPTY_ATTRIBUTES) == FLAG_NO_EMPTY_ATTRIBUTES) && attributes[i].getValue().length() == 0)
        continue;
      sb.append(" ");
      sb.append(attributes[i].getName());
      sb.append("=\"");
      encode(attributes[i].getValue());
      sb.append("\"");
    }
    
    if(close)
      sb.append("/>\n");
    else {
      sb.append(CONST_TAG_END);
      sb.append('\n');
    }
  }

  
  public void addTag(String name, String content)
  {
    sb.append(CONST_TAG_START);
    sb.append(name);
    sb.append(CONST_TAG_END);
    encode(content);
    sb.append(CONST_ENDTAG_START);
    sb.append(name);
    sb.append(CONST_ENDTAG_END);
    sb.append('\n');
  }
  
  /**
   * Adds a tag with some content, and attributes
   * 
   * @param name
   * @param attributes
   * @param content
   */
  public void addTag(String name, StringNameValuePair[] attributes, String content)
  {
    sb.append(CONST_TAG_START);
    sb.append(name);
    if(attributes != null) {
      for(int i = 0; i < attributes.length; ++i) {
        if(attributes[i].getValue() == null)
          continue;
        if(((flags & FLAG_NO_EMPTY_ATTRIBUTES) == FLAG_NO_EMPTY_ATTRIBUTES) && attributes[i].getValue().length() == 0)
          continue;
        sb.append(" ");
        sb.append(attributes[i].getName());
        sb.append("=\"");
        encode(attributes[i].getValue());
        sb.append("\"");
      }
    }
    sb.append(CONST_TAG_END);
    encode(content);
    sb.append(CONST_ENDTAG_START);
    sb.append(name);
    sb.append(CONST_ENDTAG_END);
    sb.append('\n');
  }
  
  private void encode(String content)
  {
    if(content == null)
      return;

    char[] a = content.toCharArray();
    for(int i = 0; i < a.length; ++i) {
      switch(a[i]) {
        case '"': sb.append("&quot;"); break;
        case '&': sb.append("&amp;"); break;
        case '<': sb.append("&lt;"); break;
        case '>': sb.append("&gt;"); break;
        case '\r': sb.append("&#13;"); break;
        case '\'': sb.append("&apos;"); break;
        default: sb.append(a[i]);
      }
    }
  }

  /**
   * Writes the closing tag to xml.
   *
   * @param name of the tag to close
   */
  public void closeTag(String name)
  {
    String popName = stack.pop();
    if(name.equals(popName) == false)
      throw new IllegalStateException("Invalid closing tag. Trying to close with: " + name
          + " but the tag we should close is: " + popName);
    sb.append(CONST_ENDTAG_START);
    sb.append(name);
    sb.append(CONST_ENDTAG_END);
  }
  
  /**
   * Closes the current tag.
   */
  public void closeTag()
  {
    String name = stack.pop();
    if(name == null)
      throw new IllegalStateException("There is no tag left to close");
    
    sb.append(CONST_ENDTAG_START);
    sb.append(name);
    sb.append(CONST_ENDTAG_END);
  }
  
  public StringBuffer getStringBuffer()
  {
    return sb;
  }
  
  public String toString()
  {
    StringBuffer tmp = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    return tmp.append(sb).toString();
  }

  /**
   * Hardly resets the xml writer.
   */
  public void reset()
  {
    sb = new StringBuffer();
  }
}
