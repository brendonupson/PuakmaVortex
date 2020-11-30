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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import puakma.coreide.ParseException;


/**
 * @author Martin Novak
 */
public class MimeTypesResolver implements Cloneable
{
  public static final MimeType MIME_XML = new MimeType("text/xml");
  
  /*
   * MS OFFICE
   */
  public static final MimeType MIME_MS_WORD = new MimeType("application/msword");
  public static final MimeType MIME_MS_WRITE = new MimeType("application/x-mswrite");
  public static final MimeType MIME_MS_EXCEL = new MimeType("application/vnd.ms-excel");
  public static final MimeType MIME_MS_WORKS = new MimeType("application/vnd.ms-works");
  public static final MimeType MIME_MS_SCHEDULE = new MimeType("application/vnd.ms-schedule");
  public static final MimeType MIME_MS_POWERPOINT = new MimeType("application/vnd.ms-powerpoint");
  public static final MimeType MIME_MS_PROJECT = new MimeType("application/vnd.ms-project");
  public static final MimeType MIME_MS_PUBLISHER = new MimeType("application/x-mspublisher");
  
  public static final MimeType MIME_PDF = new MimeType("application/pdf");
  public static final MimeType MIME_POSTSCRIPT = new MimeType("application/postscript");
  
  public static final MimeType MIME_HTML = new MimeType("text/html");
  public static final MimeType MIME_CSS = new MimeType("text/css");
  public static final MimeType MIME_JAVASCRIPT = new MimeType("text/javascript");
  public static final MimeType MIME_X_JAVASCRIPT = new MimeType("application/x-javascript");
  
  /**
   * Array with all document mime types
   */
  public static final MimeType[] DOCUMENTS = {
    MIME_MS_WORD, MIME_MS_WRITE, MIME_MS_EXCEL, MIME_MS_WORKS, MIME_MS_SCHEDULE,
    MIME_MS_POWERPOINT, MIME_MS_PROJECT, MIME_MS_PUBLISHER,
    MIME_PDF, MIME_POSTSCRIPT,
  };
  
  public static final MimeType[] WEB_DOCS = {
    MIME_HTML, MIME_CSS, MIME_X_JAVASCRIPT, MIME_JAVASCRIPT,
  };

  /**
   * This is the default system resolver.
   */
  private static MimeTypesResolver defaultResolver;

  private Hashtable<MimeType, MimeType> mimeTypes;
  private Hashtable<String, MimeType> extensions;

  /**
   * Hash table with image types. They are loaded from resource imageFiles.conf
   */
  private Hashtable<String, String> imageTypes;
  private MimeType unknownType;
  
  public MimeTypesResolver()
  {
    mimeTypes = new Hashtable<MimeType, MimeType>();
    extensions = new Hashtable<String, MimeType>();
    imageTypes = new Hashtable<String, String>();
    
    unknownType = new MimeType("www/unknown");
  }
  
  public void init() throws IOException
  {
    InputStream is = null;
    try {
      is = MimeTypesResolver.class.getClassLoader().getResourceAsStream("puakma/utils/mimetypes.config");
      if(is == null)
        throw new IllegalStateException("Library is corrupted - cannot read resource datas");

      Properties p = new Properties();
      p.load(is);
      Enumeration e = p.keys();
      while(e.hasMoreElements()) {
        String key = (String) e.nextElement();
        String mime = p.getProperty(key);
        
        // get rid of all comments, and these shits
        mime = mime.trim();
        int whitePos = mime.indexOf(' ');
        int tabPos   = mime.indexOf('\t');
        
        if(whitePos != -1 || tabPos != -1) {
          int pos = mime.length();
          if(whitePos != -1 && tabPos != -1)
            pos = Math.min(whitePos, tabPos);
          else if(whitePos == -1 && tabPos != -1)
            pos = tabPos;
          else if(whitePos != -1 && tabPos == -1)
            pos = whitePos;
          mime = mime.substring(0,pos);
        }
        
        MimeType mt = new MimeType();
        
        try {
          mt.parse(mime);
        }
        catch(ParseException e1) {
          continue;
        }
        
        MimeType mt1;
        if((mt1 = mimeTypes.get(mt)) != null) {
          mt1.addExtensions(key);
        }
        else {
          mt.addExtensions(key);
          mimeTypes.put(mt, mt);
        }
        
        // TODO: what if there are two extensions???
        extensions.put(key.toLowerCase(), mt);
      }
    }
    finally {
      try { if(is != null) is.close(); }
      catch(Exception e) { e.printStackTrace(); }
    }
    
    // Hoho, and now parse the image files
    //
    try {
      is = MimeTypesResolver.class.getClassLoader().getResourceAsStream("puakma/utils/imageFiles.config");
      LineNumberReader reader = new LineNumberReader(new InputStreamReader(is));
      String line = reader.readLine();
      while(line != null) {
        line = line.trim();
        // skip comments and empty lines
        if(line.startsWith("#") == false && line.length() > 0) {
          imageTypes.put(line, line);
        }
        line = reader.readLine();
      }
    }
    finally {
      try { if(is != null) is.close(); }
      catch(Exception e) { e.printStackTrace(); }
    }
  }

  public MimeType getMimeTypeFromExt(String ext)
  {
    MimeType m = extensions.get(ext.toLowerCase());
    if(m == null)
      return unknownType;
    return m;
  }
  
  public MimeType getMimeType(MimeType mt)
  {
    return mimeTypes.get(mt);
  }
  
  public MimeType getMimeTypeFromMime(String mimeType)
  {
    MimeType mt = new MimeType(mimeType);
    return getMimeType(mt);
  }

  /**
   * Checks if the content type is image type or not
   *
   * @param contentType is the content type to check
   * @return true if the content type is really image type
   */
  public static boolean isImageType(String contentType)
  {
    initializeDefault();
    // we don't need to check extensions since we are comparing images with the list
    // of the default image types
    //
    return defaultResolver.imageTypes.contains(contentType);
  }
  
  /**
   * Returns (actually, now clones) the default mime type.
   *
   * @return MimeTypesResolver object
   */
  public static MimeTypesResolver getDefault()
  {
    initializeDefault();
    try {
      return (MimeTypesResolver) defaultResolver.clone();
    }
    catch(CloneNotSupportedException e) {
      e.printStackTrace();
    }
    
    return null;
  }

  public Object clone() throws CloneNotSupportedException
  {
    initializeDefault();
    // TODO: we should clone it better - all the objects, not simply think of parent
    // class method
    //
    return super.clone();
  }
  
  private static void initializeDefault()
  {
    if(defaultResolver != null)
      return;

    try {
      defaultResolver = new MimeTypesResolver();
      defaultResolver.init();
    }
    catch(IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Returns the default extension of the mime type.
   *
   * @param contentType is the searched content type
   * @return the default file extension or null if the content type is unknown or extension
   * is not set
   */
  public static String getDefaultExt(String contentType)
  {
    MimeType mt = new MimeType(contentType);
    mt = getDefault().getMimeType(mt);
    if(mt == null)
      return "bin";
    String ext = mt.getFirstExtension();
    if(ext == null || ext.length() == 0)
      return "bin";
    else
      return ext;
  }

  /**
   * Checks if the mime type is document. Searches through DOCUMENTS array.
   *
   * @param mt is the checked mime type
   * @return true if mime type represents document, false otherwise
   */
  public static boolean isDocument(MimeType mt)
  {
    for(int i = 0; i < DOCUMENTS.length; ++i) {
      if(DOCUMENTS[i].equals(mt))
        return true;
    }
    return false;
  }

  public static boolean isWebFile(MimeType mt)
  {
    for(int i = 0; i < WEB_DOCS.length; ++i) {
      if(WEB_DOCS[i].equals(mt))
        return true;
    }
    return false;
  }
}
