package puakma.blackbook.export;
/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    04/10/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import puakma.coreide.ConnectionPrefs;
import puakma.coreide.ConnectionPrefsImpl;
import puakma.coreide.ServerManager;
import puakma.coreide.objects2.Application;
import puakma.coreide.objects2.DesignObject;
import puakma.coreide.objects2.Server;
import puakma.utils.lang.ArrayUtils;


public class ExportBlackbookScript
{
  // /Users/mn/Puakma/puakma.vortex/
  static File rootFile = new File("docs/blackbook");
  
  
  static String[] dobsToIgnore = {
    "404", "403", "__Template"
  };
  
  static String head;
  static String tail;
  
  public static void main(String[] args) throws Exception
  {
    if(rootFile.exists() == false || rootFile.isDirectory() == false) {
      System.out.println(rootFile + " is not valid");
      return;
    }
    
    File[] subs = rootFile.listFiles();
    for(int i = 0; i < subs.length; ++i)
      if(subs[i].isFile())
        subs[i].delete();
      else
        recursivelyDelete(subs[i]);
    
    // READ HEAD AND TAIL RESOURCES
    head = readTextResource("head.html");
    tail = readTextResource("tail.html");
    
    ConnectionPrefs pref = new ConnectionPrefsImpl();
    pref.setHost("localhost");
    pref.setPort(8080);
    pref.setUser("SysAdmin");
    pref.setPwd("gagarin;");
    
    Server server = ServerManager.createServerConnection(pref);
    server.refresh();
    Application app = server.getApplication("puakma", "blackbook");
    app.refresh();
    List<DesignObject> objList = new ArrayList<DesignObject>();
    List<DesignObject> resList = new ArrayList<DesignObject>();
    DesignObject[] objs = app.listDesignObjects();
    for(int i = 0; i < objs.length; ++i) {
      if(objs[i].getDesignType() == DesignObject.TYPE_PAGE) {
        if(ArrayUtils.indexOf(dobsToIgnore, objs[i].getName()) == -1)
          objList.add(objs[i]);
      }
      else if(objs[i].getDesignType() == DesignObject.TYPE_RESOURCE) {
        resList.add(objs[i]);
      }
    }
    
    String[] names = new String[objList.size()];
    Iterator<DesignObject> it = objList.iterator();
    int i = 0;
    while(it.hasNext()) {
      DesignObject o = (DesignObject) it.next();
      names[i] = o.getName();
      i++;
    }
    
    // OK, SO NOW EXPORT
    it = objList.iterator();
    while(it.hasNext()) {
      processObject((DesignObject) it.next(), names);
    }
    // AND ALSO ALL RESOURCES
    it = resList.iterator();
    while(it.hasNext()) {
      processObject((DesignObject) it.next());
    }
  }

  private static String readTextResource(String string) throws IOException
  {
    InputStream is = ExportBlackbookScript.class.getResourceAsStream("head.html");
    byte[] total = new byte[0];
    byte[] b = new byte[1024];
    while(true) {
      int read = is.read(b);
      total = ArrayUtils.mergeArrays(total, b, total.length, read);
      if(read < 1024)
        break;
    }
    return new String(total, "UTF-8");
  }

  private static void recursivelyDelete(File file)
  {
    File[] subs = file.listFiles();
    for(int i = 0; i < subs.length; ++i) {
      if(subs[i].isFile())
        subs[i].delete();
    }
    
    file.delete();
  }

  private static void processObject(DesignObject object, String[] allObjects) throws Exception
  {
    // AT FIRST GET THE CONTENT OF THE PAGE,
    StringBuffer content = new StringBuffer(head);
    String title = object.getParameterValue("Title");
    if(title == null)
      title = "Puakma Blackbook";
  
    //  REPLACE ${TITLE} WITH THE PROPER ONE
    int pos = content.indexOf("${TITLE}");
    if(pos != -1)
      content.replace(pos, pos + "${TITLE}".length(), title);
  
    getDObjectContent(content, object, false);
    // REMOVE ALL PUAKMA TAGS
    removeAllPmaTags(content);
    // RENAME ALL REFERENCES TO OTHER PAGES TO .HTML
    for(int i = 0; i < allObjects.length; ++i) {
      while(true) {
        String toRepl = "href=\"" + allObjects[i] + "\"";
        pos = content.indexOf(toRepl);
        if(pos == -1)
          break;
        
        content.replace(pos, pos + toRepl.length(), "href=\"" + allObjects[i] + ".html\"");
      }
    }
    content.append(tail);
    // SAVE TO ROOT FOLDER
    File file = new File(rootFile, object.getName() + ".html");
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(file);
      fos.write(content.toString().getBytes("UTF-8"));
    }
    finally {
      if(fos != null) try { fos.close(); } catch(Exception ex) {  }
    }
  }
  
  private static void processObject(DesignObject object) throws Exception
  {
    // AT FIRST GET THE CONTENT OF THE PAGE,
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    object.download(bos, false);
    // SAVE TO ROOT FOLDER
    File file = new File(rootFile, object.getName());
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(file);
      fos.write(bos.toByteArray());
    }
    finally {
      if(fos != null) try { fos.close(); } catch(Exception ex) {  }
    }
  }

  private static void removeAllPmaTags(StringBuffer content)
  {
    int offset = 0;
    while(true) {
      int start = content.indexOf("<P@", offset);
      if(start == -1)
        return;
      
      int end = content.indexOf("@P>", start);
      if(end == -1)
        return;
      content.delete(start, end + 3);
      offset = end;
    }
  }

  private static StringBuffer getDObjectContent(StringBuffer sb, DesignObject object, boolean isSource) throws Exception
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    object.download(bos, false);
    sb.append(new String(bos.toByteArray(), "UTF-8"));
    return sb;
  }
}
