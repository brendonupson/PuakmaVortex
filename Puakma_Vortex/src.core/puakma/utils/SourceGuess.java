/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Apr 14, 2005
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * This class is used for guessing class name and package from java source file (*.java).
 * Why we cannot use some java parser? Java parsers has to have defined strict grammar,
 * which is too strict to guess class name and package name from damaged file.
 *
 * @author Martin Novak
 */
public class SourceGuess
{
  private static final String ERR_MSG = "String doesn't look like java source file";
  /**
   * Expression to get package name, and class name
   * \d\s;/\\*<>()
   */
//  public static final String EXPRESSION = "(([\\d\\w\\s]+package|^package)[\\s]+([a-zA-Z.]+);){0,1}[\\w\\d\\s]*(class|interface)[\\s]+([a-zA-Z]*)[\\s{]";
//  public static final String CLASS_EXPR = "(class|interface)[\\s]+([a-zA-Z]*)[\\s]*\\{";
  public static final String CLASS_EXPR = "(class|interface)[\\s]+([a-zA-Z0-9]+)([\\s]+(extends|implements)[\\s]+[a-zA-Z0-9]+[\\s]*|[\\s]*)\\{";
  public static final String PACK_EXPR = "(package)[\\s]+([a-zA-Z.]+)[\\s]*;";
//    "([\\s]+package|^package)[\\s]+([a-zA-Z.]+);[\\d\\s\\w;]*(class|interface)[\\s]+([a-zA-Z]*)[\\s{]";
  
  private static final Object lock = new Object();
  
  private static Pattern classExpr;
  private static Pattern packExpr;
  
  private String className;
  private String packageName;

  /**
   * This method guesses the class name, and package name of the source.
   *
   * @param javaSource
   * @throws ClassFileDecompilerException
   */
  public void guessContent(String javaSource) throws ClassFileDecompilerException
  {
    synchronized(lock) {
      if(classExpr == null) {
        try {
          classExpr = Pattern.compile(CLASS_EXPR);
          packExpr = Pattern.compile(PACK_EXPR);
        }
        catch(PatternSyntaxException e) {
          throw new ClassFileDecompilerException("Invalid pattern!");
        }
      }
    }
    
    Matcher pM = packExpr.matcher(javaSource);
    boolean found = pM.find();
    if(found == false)
      this.packageName = null;
    else
      this.packageName = pM.group(2);
    
    Matcher m = classExpr.matcher(javaSource);
    found = m.find();
    if(found == false)
      throw new ClassFileDecompilerException(ERR_MSG);

    try {
      this.className = m.group(2);
    }
    catch(Exception e) {
      e.printStackTrace();
      throw new ClassFileDecompilerException(ERR_MSG);
    }
  }
  
  public String getClassName()
  {
    return className;
  }

  public String getPackageName()
  {
    return packageName;
  }
}
