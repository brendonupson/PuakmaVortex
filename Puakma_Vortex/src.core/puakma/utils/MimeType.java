/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jan 10, 2005
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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import puakma.coreide.ParseException;


/**
 * Mime type object representation.
 * 
 * Note that all comparing doesn't care about character set.
 *
 * @author Martin Novak
 */
public class MimeType
{
  private String mimeType;
  private String mainType;
  private String subType;
  private String charset;
  private String firstExtension;
  /**
   * Hashtable with all extensions. Key is string, value is also string - both represents
   * extension.
   */
  private Hashtable<String, String> extensions;

  private static final String MSG = "Invalid mime type specification ";
  
  public MimeType()
  {
    extensions = new Hashtable<String, String>();
  }
  
  public MimeType(String mimeType)
  {
    extensions = new Hashtable<String, String>();
    try {
      parse(mimeType);
    }
    catch(Exception e) {
      this.mimeType = this.mainType = mimeType;
      this.subType = "";
    }
  }

  /**
   * @param mimeType
   * @throws ParseException
   */
  public synchronized void parse(String mimeType) throws ParseException
  {
    int divPos = mimeType.indexOf('/');
    int charPos = mimeType.indexOf(';');
    if(mimeType.lastIndexOf('/') != divPos || divPos == -1)
      throw new ParseException(MSG + mimeType);
    if(mimeType.lastIndexOf(';') != charPos)
      throw new ParseException(MSG + mimeType);
    
    mainType = mimeType.substring(0, divPos);
    if(mainType.length() == 0)
      throw new ParseException(MSG + mimeType);
    
    if(charPos == -1) {
      subType = mimeType.substring(divPos + 1);
    }
    else {
      subType = mimeType.substring(divPos + 1, charPos);
      if(subType.length() == 0)
        throw new ParseException(MSG + mimeType);

      charset = mimeType.substring(charPos + 1);
      int eqIndex = charset.indexOf('=');
      if(eqIndex != charset.lastIndexOf('='))
        throw new ParseException(MSG + mimeType);

      charset = charset.substring(eqIndex + 1);
      // note that charset still can be zero length
    }
    
    // trim all strings at the end
    mainType = mainType.trim();
    subType = subType.trim();
    if(charset != null)
      charset = charset.trim();
    
    // and create mime type variable
    this.mimeType = mainType + '/' + subType;
//    if(charset != null)
//      mimeType = mimeType + "; charset=" + charset;
  }
  
  
  public String getCharset()
  {
    return charset;
  }
  
  public String getMainType()
  {
    return mainType;
  }
  
  public String getSubType()
  {
    return subType;
  }
  
  public synchronized Iterator<String> extensionsIterator()
  {
    Set<String> s = extensions.keySet();
    return s.iterator();
  }

  public synchronized void addExtensions(String extension)
  {
    if(firstExtension == null)
      firstExtension = extension;

    this.extensions.put(extension, extension);
  }
  
  
  public boolean equals(Object obj)
  {
    if(obj instanceof MimeType) {
      MimeType mt = (MimeType) obj;
      String s1 = mt.getMainType() + '/' + mt.getSubType();
      String s2 = getMainType() + '/' + getSubType();
      if(s1.equals(s2))
        return true;
      return false;
    }
    else if(obj instanceof String) {
      try {
        MimeType mt = new MimeType();
        mt.parse((String) obj);
        String s1 = mt.getMainType() + '/' + mt.getSubType();
        String s2 = getMainType() + '/' + getSubType();
        if(s1.equals(s2))
          return true;
      }
      catch(ParseException e) {  }
      return false;
    }
    return false;
  }
  
  public int hashCode()
  {
    return mimeType.hashCode();
  }
  
  /**
   * Returns build mime type. It will be in the shape: mainType/subType.
   *
   * @return String with the MIME type.
   */
  public String toString()
  {
    return mimeType;
  }

  /**
   * Returns the most prefered extension.
   *
   * @return String with the first extension. If there is no extension, returns
   * null
   */
  public String getFirstExtension()
  {
    return firstExtension;
  }

  /**
   * Returns array with all extensions assigned to this mime type.
   *
   * @return array with all extensions assigned to the type
   */
  public synchronized String[] getExtensions()
  {
    return extensions.values().toArray(new String[extensions.size()]);
  }
}
