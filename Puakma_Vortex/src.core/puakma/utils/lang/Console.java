/*
 * Author: Martin Novak
 * Date:   Mar 18, 2005
 */
package puakma.utils.lang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * @author Martin Novak
 */
public class Console
{
  /**
   * The default print stream. We should write all the stuff using this stream.
   */
  static PrintStream consoleOut;
  
  /**
   * Lock for accessing output stream
   */
  private static Object lock = new Object();

  /**
   * Displays user the options question like lot of UNIX utilities.
   * 
   * <p>Example of the prompt:
   *
   * <p><pre>File already exists, do you want to everwrite it?
   * Yes[Y], No[N], All[A], Abort[B] (default: Yes): 
   * </pre>
   * 
   * <p>This function works this way: it asks the user for value. Return means the default
   * value. It accepts only one letter answer from the user. If the method gets more then
   * one letter in the response, repeats the question.
   *
   * @param question is the question for the user
   * @param defaultValue is the index of the default value from the <code>choiceLetters</code>
   *                     array. Value of -1 means that there is no default value.
   * @param choiceLetters is the array of the letters from which user should choose. Note
   * that the choices are case-insenitive! So if you provide two values with the same letter
   * except the case, you can get <code>IllegalArgumentException</code>
   * @param choiceDescriptions is the array of the descriptions from all the choices
   * @return character which was choosed from the options.
   */
  public static char getChoice(String question, int defaultValue, char[] choiceLetters,
                               String[] choiceDescriptions)
  {
    if(choiceLetters.length != choiceDescriptions.length)
      throw new IllegalArgumentException("Choice description and letters arrays has to be same length");
   if(defaultValue > choiceLetters.length || defaultValue < -1)
     throw new IllegalArgumentException("Invalid default value");
    
    //char retVal = defaultValue != -1 ? choiceLetters[defaultValue] : ' ';
    // main loop
    while(true) {

      println(question);
      String line = "";
      try {
        line = getLine().trim();
        
        // check the default value
        if(line.length() == 0 && defaultValue != -1)
          return choiceLetters[defaultValue];
        else if(line.length() == 0)
          continue;
        
        if(line.length() != 1 && line.length() != 0) {
          println("Invalid input value");
          continue;
        }
        
        char letter = line.charAt(0);
        for(int i = 0; i < choiceLetters.length; ++i) {
          char c = choiceLetters[i];
          if(Character.toUpperCase(letter) == Character.toUpperCase(c))
            return c;
        }
      }
      catch(IOException e) {
      }
    }
  }
  
  /**
   * Gets the line from the command prompt before return is pressed.
   *
   * @return String which was typed before return pressed
   * @throws IOException
   */
  public static String getLine() throws IOException
  {
    BufferedReader bufReader;
    bufReader = new BufferedReader(new InputStreamReader(System.in));
    return bufReader.readLine();
  }
  
  public static void print(char c)
  {
    initStream();
    consoleOut.print(c);
  }
  
  public static void print(int i)
  {
    initStream();
    consoleOut.print(i);
  }
  
  public static void print(long l)
  {
    initStream();
    consoleOut.print(l);
  }
  
  public static void print(String str)
  {
    initStream();
    consoleOut.print(str);
  }
  
  public static void print(Object obj)
  {
    initStream();
    consoleOut.print(obj);
  }
  
  public static void println(char c)
  {
    initStream();
    consoleOut.println(c);
  }
  
  public static void println(int i)
  {
    initStream();
    consoleOut.println(i);
  }
  
  public static void println(long l)
  {
    initStream();
    consoleOut.println(l);
  }
  
  public static void println(String str)
  {
    initStream();
    consoleOut.println(str);
  }
  
  public static void println(Object o)
  {
    initStream();
    consoleOut.println(o);
  }

  /**
   * Initializes console output stream. The console output stream is a hack for the macosx
   * terminal - and probably also for other environments which supports UTF-8 encoding.
   */
  private static void initStream()
  {
    synchronized(lock) {
      if(consoleOut != null)
        return;

      try {
        consoleOut = new PrintStream(System.out, true, "UTF-8");
      }
      catch(UnsupportedEncodingException e) {
        consoleOut = System.out;
      }
    }
  }

  /**
   * Gets password from the command line prompt, but it doesn't display what is user typing
   * in the command prompt, so it's kind a secure... [-;
   *
   * @param prompt is the prompt string for the password
   * @return string with password or null if the password is empty
   * @throws IOException
   */
  public static String getPassword(String prompt) throws IOException {
    initStream();

    // password holder
    String password = "";
    MaskingThread maskingthread = new MaskingThread(prompt);
    Thread thread = new Thread(maskingthread);
    thread.start();
    // block until enter is pressed
    while (true) {
       char c = (char)System.in.read();
       // assume enter pressed, stop masking
       maskingthread.stopMasking();

       if (c == '\r') {
          c = (char)System.in.read();
          if (c == '\n') {
             break;
          } else {
             continue;
          }
       } else if (c == '\n') {
          break;
       } else {
          // store the password
          password += c;
       }
    }
    return password;
  }
}

class MaskingThread extends Thread
{
  private boolean stop = false;

  //private int index;

  private String prompt;

  /**
   *@param prompt The prompt displayed to the user
   */
  public MaskingThread(String prompt)
  {
    this.prompt = prompt;
  }

  /**
   * Begin masking until asked to stop.
   */
  public void run()
  {
    while(!stop) {
      try {
        // attempt masking at this rate
        Thread.sleep(1);
      }
      catch(InterruptedException iex) {
        iex.printStackTrace();
      }
      if(!stop) {
        Console.consoleOut.print("\r" + prompt + " \r" + prompt);
      }
      System.out.flush();
    }
  }

  /**
   * Instruct the thread to stop masking.
   */
  public void stopMasking()
  {
    this.stop = true;
  }
}
