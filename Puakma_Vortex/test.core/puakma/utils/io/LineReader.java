/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jan 24, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.utils.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import puakma.utils.lang.StringUtil;


/**
 * Reads the content of the text file, and creates list with each line.
 * <p>Note that the class is synchronized.
 *
 * @author Martin Novak
 */
public class LineReader
{
  List<String> items = new Vector<String>();
  
  /**
   * @param is
   * @throws IOException
   */
  public void parse(InputStream is) throws IOException
  {
    synchronized(items) {
      items.clear();
      
      Reader r = new BufferedReader(new InputStreamReader(is));
      
      String line;
      while((line = StringUtil.readLine(r)) != null) {
        line = line.trim();
        if(line.length() > 0) {
          items.add(line);
        }
      }
    }
  }

  public boolean contains(String string)
  {
    return items.contains(string);
  }

  public Iterator<String> iterator()
  {
    return items.iterator();
  }

  public int size()
  {
    return items.size();
  }

}
