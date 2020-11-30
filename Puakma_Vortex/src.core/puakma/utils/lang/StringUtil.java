/*
 * Author: Martin Novak
 * Date:   Dec 28, 2004
 */
package puakma.utils.lang;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class introduces some handy utilities for handling strings.
 *
 * @author Martin Novak
 */
public class StringUtil
{
  public static class Range
  {
    public int start;
    public int end;
    
    public Range()
    { }
    
    public Range(int start, int end)
    {
      this.start = start;
      this.end = end;
    }
  }

  public static final String EMPTY_STRING = "";
  
  private static final char[] arr = {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
  };
  
  private static final byte[] revArr = new byte[256];
  
  static {
    for(int i = 0; i < revArr.length; ++i) {
      revArr[i] = -1;
    }
    revArr['0'] = 0;  revArr['1'] = 1;  revArr['2'] = 2; revArr['3'] = 3;
    revArr['4'] = 4;  revArr['5'] = 5;  revArr['6'] = 6; revArr['7'] = 7;
    revArr['8'] = 8;  revArr['9'] = 9;  revArr['A'] = 10; revArr['a'] = 10;
    revArr['B'] = 11; revArr['b'] = 11; revArr['C'] = 12; revArr['c'] = 12;
    revArr['D'] = 13; revArr['d'] = 13; revArr['E'] = 14; revArr['e'] = 14;
    revArr['F'] = 15; revArr['f'] = 15;
  }

  
  /**
   * Returns safe string contained in parameter. It means that if str param is null,
   * then returns non-null string.
   *
   * @param str is the returning string
   * @return str param or empty string if str is nulls
   */
  public static String safeString(String str)
  {
    if(str == null)
      return EMPTY_STRING;
    else
      return str;
  }

  /**
   * Finds case insensitively the first index of second string in the first string.
   * If the second string is not present in the first, returns -1.
   *
   * @param fir string to be searched
   * @param sec string to search for
   * @return index of the second String in the first one
   */
  public static int indexOfSecondIgnoreCase(String fir, String sec)
  {
    // some speed improvements [-;
    if(fir.length() < sec.length())
      return -1;
    
    int len1 = fir.length();
    int len2 = sec.length();
    for(int i = 0; i < len1 - len2 + 1; ++i) {
      if(fir.regionMatches(true, i, sec, 0, len2))
        return i;
    }

    return -1;
  }

  /**
   * Trims the string. Note that you should use String.trim function
   *
   * @param str is the string to trim
   * @return string without leading and trailing spaces
   */
  public static String trim(String str)
  {
    int i, j;
    
    for(i = 0; i < str.length(); ++i) {
      if(Character.isWhitespace(str.charAt(i)) == false)
        break;
    }
    
    for(j = str.length() - 1; j > i; --j) {
      if(Character.isWhitespace(str.charAt(j)) == false)
        break;
    }
    
    if(i == 0 && j == str.length())
      return str;
    
    return str.substring(i, j + 1);
  }

  /**
   * Reads one line from input reader.
   * 
   * @param r is the reader
   * @return String with the result, or null if there is nothing to read
   * @throws IOException
   */
  public static String readLine(Reader r) throws IOException
  {
    int c = -1;
    int i = 0;
    StringBuffer sb = new StringBuffer();
    while(true) {
      c = r.read();
      
      if(c == -1)
        break;
      else if(c == '\r') {
        c = r.read();
        if(c == '\n' || c == -1)
          break;
      }
      else if(c == '\n')
        break;

      sb.append((char)c);
      i++;
    }
    if(i == 0)
      return null;
    sb.substring(0, i);
    return sb.toString();
  }

  /**
   * Checks if the array contains specified string. Note that the comparision is case sensitive.
   *
   * @param strings
   * @param searchedStr
   * @return index of the searched string in the array or -1 if the searchedStr is not
   * in the array.
   */
  public static int arrayContainsString(String[] strings, String searchedStr)
  {
    for(int i = 0; i < strings.length; ++i) {
      if(strings[i].equals(searchedStr))
        return i;
    }

    return -1;
  }

  /**
   * This method removes line from the StringBuffer, and returns it as the new String object.
   * Note that the line has to be ended by <code>CRLF</code> sequence!
   *
   * @param sb is the StringBuffer from which we should remove the line
   * @return String with the line
   */
  public static String removeCrLfLine(StringBuffer sb)
  {
    int pos = sb.indexOf("\r\n");
    if(pos == -1)
      return null;

    // get the line from the StringBuffer
    StringBuffer ret = new StringBuffer(sb.substring(0, pos));
    // remove the line
    sb.delete(0, pos + 2);
    // remove CRLF from the end of line
//    ret.delete(ret.length() - 3, ret.length() - 1);
    return ret.toString();
  }

  /**
   * This function merges the toString() values of array elements to one string
   * divided by divider. The null array is treated as to be zero length.
   *
   * @param array is the array we want to have merged to string using toString() function
   * @param divider is the division string between elements of the array
   * @return merged string with the content of array, and divided by divider:
   *         array[0]dividerArray[1]
   */
  public static String merge(Object[] array, String divider)
  {
    StringBuffer sb = new StringBuffer();
    if(array == null)
      return "";

    for(int i = 0; i < array.length; ++i) {
      sb.append(array[i].toString());
      if((i+1) < array.length)
        sb.append(divider);
    }
    return sb.toString();
  }

  /**
   * Safely compares two strings for equality. See String#equals(String) function
   * for comparision of two strings
   *
   * @param s1 is the first string
   * @param s2 is the second string to compare
   * @return true if those two strings are the same
   */
  public static boolean compareStrings(String s1, String s2)
  {
    boolean s1Empty = s1 == null || s1.length() == 0;
    boolean s2Empty = s2 == null || s2.length() == 0;
    if(s1Empty) {
      if(s2Empty)
        return true;
      return false;
    }
    if(s2Empty)
      return false;

    return s1.equals(s2);
  }
  
  /**
   * Safely compares two strings for equality ignoring case of characters. See
   * String#equalsIgnoreCase(String) function for comparision of two strings
   *
   * @param s1 is the first string
   * @param s2 is the second string to compare
   * @return true if those two strings are the same
   */
  public static boolean compareStringsIgnoreCase(String s1, String s2)
  {
    boolean s1Empty = s1 == null || s1.length() == 0;
    boolean s2Empty = s2 == null || s2.length() == 0;
    if(s1Empty) {
      if(s2Empty)
        return true;
      return false;
    }
    if(s2Empty)
      return false;

    return s1.equalsIgnoreCase(s2);
  }

  /**
   * This function converts byte buffer to string representation. Each byte is
   * represented by it's hexadecimal value.
   *
   * @param buffer is the buffer to convert
   * @return Hexa representation of the byte array
   */
  public static String bytesToHexString(byte[] buffer)
  {
    StringBuffer sb = new StringBuffer(buffer.length * 2);
    for(int i = 0; i < buffer.length; ++i) {
      byte b = buffer[i];
      char hi = arr[(b & 0xF0) >> 4];
      char low = arr[b & 0x0F];
      sb.append(hi);
      sb.append(low);
    }
    return sb.toString();
  }

  /**
   * This function should convert hexa string to bytes.
   *
   * @param hexSourceMD5 is the hexa string which is converted
   * @return array with the original bytes
   * @throws IllegalArgumentException if input is not hex string
   */
  public static byte[] hexStringToBytes(String hexSourceMD5)
  {
    if(hexSourceMD5.length() % 2 != 0)
      throw new IllegalArgumentException("Invalid length of hex string");
    byte[] ret = new byte[hexSourceMD5.length() / 2];
    
    try {
      for(int i = 0, j = 0; i < ret.length; ++i, j+=2) {
        char hiC = hexSourceMD5.charAt(j);
        char loC = hexSourceMD5.charAt(j + 1);
        ret[i] = (byte) ((byte) ((revArr[hiC] << 4) & 0xF0) | revArr[loC]);
      }
    }
    catch(IndexOutOfBoundsException e) {
      throw new IllegalArgumentException("Invalid hex string to convert");
    }
    
    return ret;
  }

  /**
   * Matches text with the standard wildcard pattern. Now it's used emulation
   * of regular expressions, so the speed is maybe not so good. And also this
   * ignores case of the pattern.
   *
   * @param text is text to match
   * @param pattern is the wildcard pattern
   * @return true if the text matches to pattern
   */
  public static boolean matchWildcardIgnoreCase(String text, String pattern)
  {
    pattern = cleanPattern(pattern).toString();
    pattern = "^(" + pattern + ").*$";
    Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
    Matcher m = p.matcher(text);
    if(m.matches())
      return true;
    return false;
  }
  
  private static StringBuffer cleanPattern(String pattern)
  {
    StringBuffer sb = new StringBuffer();
    for(int i = 0; i < pattern.length(); ++i) {
      switch(pattern.charAt(i)) {
        case '.':
          sb.append("\\.");
        break;
        case '?':
          sb.append(".");
        break;
        case '*':
          sb.append(".*");
        break;
        case '(':
          sb.append("\\(");
        break;
        case ')':
          sb.append("\\)");
        break;
        default:
          sb.append(pattern.charAt(i));
      }
    }
    return sb;
  }

  /**
   * Searches the array using binary search algorithm. The array of searched strings has to be sorted.
   * 
   * @param vals is the array we want to search
   * @param key is the key we want to find
   * @return index of the key in the array. If the key is not found, returns -1.
   */
  public static int findInSortedArrayIgnoreCase(String[] vals, String key)
  {
    int index = Arrays.binarySearch(vals, key, new Comparator<String>() {
      public int compare(String s1, String s2)
      {
        return s1.compareToIgnoreCase(s2);
      }
    });
    return index >= 0 ? index : -1;
  }
  
  /**
   * Parses int from value. If there is some error, returns defaultValue.
   *
   * @param value is the string to parse
   * @param defaultValue is returned on error
   * @return in with the content od value or defaultValue
   */
  public static int parseInt(String value, int defaultValue)
  {
    try {
      return Integer.parseInt(value);
    }
    catch(NumberFormatException ex) {
      return defaultValue;
    }
  }
  
  /**
   * Parses long from value. If there is some error, returns defaultValue.
   *
   * @param value is the string to parse
   * @param defaultValue is returned on error
   * @return in with the content od value or defaultValue
   */
  public static long parseLong(String value, long defaultValue)
  {
    try {
      return Long.parseLong(value);
    }
    catch(NumberFormatException ex) {
      return defaultValue;
    }
  }
  
  public static boolean parseBool(String value, boolean defaultValue)
  {
    if("true".equalsIgnoreCase(value))
      return true;
    else if("false".equalsIgnoreCase(value))
      return false;
    else if("1".equalsIgnoreCase(value))
      return true;
    else if("0".equalsIgnoreCase(value))
      return false;
    return defaultValue;
  }

  /**
   * Finds the range of the line in the text. The first line is number zero.
   * Note that if the line doesn't exist in the text, false is returned. If the
   * line has length of zero, start of the range will be the same as the end of
   * the range. The start is the index of the first letter, and end is the index
   * of line delimeter or one character after the last character in the text.
   * 
   * @param text is the text to examine
   * @param line is the line we want
   * @param range for saving the text range
   * @return true if the line is in the text, false otherwise
   */
  public static boolean findLineRange(String text, int line, Range range)
  {
    int index = 0;
    for(int currentLine = 0; currentLine <= line; ++currentLine) {
      if(currentLine == line) {
        range.start = index;
        int index1 = text.indexOf('\n', index);
        if(index1 != -1) {
          range.end = index1;
        }
        else
          range.end = text.length();
        return true;
      }
      
      index = text.indexOf('\n', index);
      if(index == -1)
        return false;
      index++;
    }
    return false;
  }

  /**
   * This function returns line from the text.
   * 
   * @param text is the input multiline text
   * @param lineNo is the line number
   * @return the first line of the text
   */
  public static String getLine(String text, int lineNo)
  {
    int i = 0;
    int cl = 0; // CURRENT LINE
    int len = text.length();
    int start = 0;
    int end = 0;
    
    while(i < len && cl <= lineNo) {
      char c = text.charAt(i);
      i++;
      
      if(c == '\n') {
        if(cl == lineNo) {
          end = i - 1;
          return text.substring(start, end);
        }
        else {
          start = i;
          cl++;
        }
      }
    }
    
    // IN THE CASE WE ARE AT THE END, WE SHOULD CHECK IT TO THE END
    if(cl == lineNo) {
      end = i;
      return text.substring(start, end);
    }
    
    return "";
  }
}
