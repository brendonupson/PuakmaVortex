/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Nov 12, 2004
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import puakma.coreide.ParseException;


/**
 * Creates/parses lines of text to columns.
 *
 * @author Martin Novak
 */
public class LineListCompiler
{
  /**
   * Escapes string with '\' character. List of escaped chars: \:\n
   * @param str input unescaped string
   * @return escaped String
   */
  protected static String escapeForLineList(String str)
  {
    if(str == null)
      return "";

    StringBuffer sb = new StringBuffer();
    for(int i = 0; i < str.length(); ++i) {
      char c = str.charAt(i);
      switch(c) {
        case ':':
          sb.append("\\:");
        break;
        case '\n':
          sb.append("\\n");
        break;
        case '\\':
          sb.append("\\\\");
        break;
        default:
          sb.append(c);
      }
    }
    return sb.toString();
  }

  /**
   * Unescapes all escaped strings.
   *
   * @param str is the string to unescape
   * @return unescaped string
   */
  protected static String unescapeString(String str)
  {
    StringBuffer sb = new StringBuffer();
    
    for(int i = 0; i < str.length(); ++i) {
      char c = str.charAt(i);
      switch(c) {
        case '\\':
          if(i + 1 < str.length()) { // defense agains buffer overflow
            // check character at position i + 1 to be some of the special characters
            char c1 = str.charAt(i + 1);
            switch(c1) {
              case ':':
              case '\\':
                sb.append(c1);
                i++;
              break;
              case 'n':
                sb.append('\n');
              break;
              default:
                sb.append('\\');
                sb.append(c1);
            }
          }
          else
            sb.append('\\');
        break;
        default:
          sb.append(c);
      }
    }
    
    return sb.toString();
  }
  
  /**
   * Creates escaped line from columns from result set.
   *
   * @param sb stringBuffer into which are datas appended
   * @param cols array with all column names
   * @param rs is the database query result
   * @throws SQLException
   */
  public static void appendLineFromResultSet(StringBuffer sb, String[] cols, ResultSet rs) throws SQLException
  {
    for(int i = 0; i < cols.length; i++) {
      String str = escapeForLineList(rs.getString(cols[i]));
      sb.append(str);
      sb.append(':');
    }

    sb.append('\n');
  }

  /**
   * Parses the text according the line paradigmat. The line is one record with many
   * items. Items are divided from each other by ':' character. Note that the text
   * should be unescaped.
   *
   * @param text is the unescaped text
   * @return List with items which are arrays of strings
   */
  public static List<String[]> parseText(String text)
  {
    List<String[]> res = new ArrayList<String[]>();
    StringBuffer txt = new StringBuffer();
    txt.append(text);
    String[] line = null;

    do {
      line = getLine(txt);
      if(line != null)
        res.add(line);
    } while(line != null);
    
    return res;
  }

  /**
   * Parses one line, and removes it from buffer txt.
   *
   * @param txt is the input string buffer.
   * @return Array with parsed escaped strings. Null if there is nothing to parse.
   */
  private static String[] getLine(StringBuffer txt)
  {
    List<String> l = new ArrayList<String>();
    
    if(txt.length() == 0) 
      return null;
    
    // find endofline
    int lineEnd = txt.indexOf("\n");
    int dotsPos = txt.indexOf(":");
    if(lineEnd <= 0 && dotsPos < 0)
      return null;
    if(lineEnd <= 0)
      lineEnd = txt.length();
    // cut string until EOL to new buffer
    StringBuffer sb = new StringBuffer();
    sb.append(txt.substring(0, lineEnd));
    txt.delete(0, lineEnd + 1);
    // and delete end of line
//    txt.deleteCharAt(0);
    
    // now tokenize the string
    LineListTokenizer tokenizer = new LineListTokenizer(sb.toString(),':');
    if(tokenizer.countTokens() == 0)
      return null;

    while(tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken();
      token = unescapeString(token);
      l.add(token);
    }

    return l.toArray(new String[l.size()]);
  }

  /**
   * Adds column names at the first line.
   * 
   * @param result is the resulting stringbuffer
   * @param cols are columns to be added
   */
  public static void addColumns(StringBuffer result, String[] cols)
  {
    if(cols == null)
      return;

    for(int i = 0; i < cols.length; ++i) {
      if(i != 0)
        result.append(':');
      result.append(escapeForLineList(cols[i]));
    }

    if(cols.length > 0)
      result.append('\n');
  }

  /**
   * Checks consistency of the first line in the parsed file.
   *
   * @param colsLine array with column names.
   * @param cols expected columns definition
   * @throws ParseException when columns don't match
   */
  public static void checkCols(String[] colsLine, String[] cols) throws ParseException
  {
    if(colsLine.length != cols.length)
      throw new ParseException("Column count doesn't match!");

    for(int i = 0; i < colsLine.length; ++i) {
      if(cols[i].equals(colsLine[i]) == false)
        throw new ParseException("Columns don't match!");
    }
  }
}
