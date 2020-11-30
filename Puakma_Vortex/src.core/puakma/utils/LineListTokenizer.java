/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Nov 17, 2004
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


/**
 * @author Martin Novak
 */
public class LineListTokenizer
{
  /**
   * There is held the parsed string
   */
  String s;
  
  /**
   * Number of strings
   */
  int count;
  
  /**
   * Array with tokens
   */
  String[] tokens;
  
  /**
   * Delimeter character
   */
  char delim = ':';
  
  /**
   * Iterator position
   */
  int curToken = 0;

  public LineListTokenizer(String text)
  {
    this.s = text;
    
    parse();
  }
  
  /**
   * Constructor. Parses the text.
   *
   * @param text the input text
   * @param delim delimeter character. Default is ':'. If the delimeter is in the text
   * escapaed by '\', no new token is created.
   */
  public LineListTokenizer(String text, char delim)
  {
    this.s = text;
    this.delim = delim;
    
    parse();
  }
  
  /**
   * Parses the input string
   */
  private void parse()
  {
    // count all delims
    for(int i = 0; i < s.length(); ++i) {
      if(s.charAt(i) == '\\' && s.length() > i && s.charAt(i+1) == delim)
        ++i;
      else if(s.charAt(i) == delim)
        count++;
    }
    count++;
    
    tokens = new String[count];
    
    int currentToken = 0;
    StringBuffer sb = new StringBuffer();
    for(int i = 0; i < s.length(); ++i) {
      char c = s.charAt(i);
      if(c == '\\' && s.length() > i && s.charAt(i+1) == delim) {
        sb.append('\\');
        sb.append(delim);
        i += 2;
      }
      else if(c == ':') {
        tokens[currentToken] = sb.toString();
        sb = new StringBuffer();
        currentToken++;
      }
      else {
        sb.append(c);
      }
    }
    if(s.charAt(s.length() - 1) == delim) {
      tokens[currentToken] = "";
    }
    else {
      tokens[currentToken] = sb.toString();
      sb = new StringBuffer();
      currentToken++;
    }
  }
  
  /**
   * Returns number of tokens
   *
   * @return number of tokens
   */
  public int countTokens()
  {
    return this.count;
  }
  
  /**
   * Checks if we have more tokens available.
   *
   * @return true if we have some more tokens waiting for processing
   */
  public boolean hasMoreTokens()
  {
    return (tokens.length > curToken);
  }
  
  /**
   * Returns the next token.
   *
   * @return the next token
   */
  public String nextToken()
  {
    return tokens[curToken++];
  }
}
