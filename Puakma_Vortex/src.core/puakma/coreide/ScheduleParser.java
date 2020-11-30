/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    14/11/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide;

import java.util.HashMap;
import java.util.Map;

import com.ibm.icu.util.StringTokenizer;

/**
 * Class for parsing the schedule from the options string at scheduled actions.
 * 
 * Schedule=H,Interval=1,Days=SMTWHFA,StartTime=06:00,FinishTime=9:00
 * Schedule=N,LastRun=
 * 
 * @author Martin Novak
 */
public class ScheduleParser
{
  public static final char RUN_NONE = 'N';

  public static final char RUN_SECOND = 'S';

  public static final char RUN_MINUTE = 'I';

  public static final char RUN_HOUR = 'H';

  public static final char RUN_DAY = 'D';

  public static final char RUN_WEEK = 'W';

  public static final char RUN_MONTH = 'M';

  public static final char RUN_YEAR = 'Y';
  
  public static final char DAY_MONDAY = 'M';

  public static final char DAY_TUESDAY = 'T';

  public static final char DAY_WEDNESDAY = 'W';

  public static final char DAY_THURSDAY = 'H';

  public static final char DAY_FRIDAY = 'F';

  public static final char DAY_SATURDAY = 'A';

  public static final char DAY_SUNDAY = 'S';
  
  /**
   * Map containing characters for week days
   */
  private static final Map<Character, Character> daysMap = new HashMap<Character, Character>();
  
  /**
   * Contains all scheduled types - like NONE, SECOND, MINUTE, ...
   */
  private static final Map<Character, Character> typesMap = new HashMap<Character, Character>();
  
  static {
    daysMap.put('M', 'M');
    daysMap.put('T', 'T');
    daysMap.put('W', 'W');
    daysMap.put('H', 'H');
    daysMap.put('F', 'F');
    daysMap.put('A', 'A');
    daysMap.put('S', 'S');
    
    typesMap.put(RUN_NONE, RUN_NONE);
    typesMap.put(RUN_SECOND, RUN_SECOND);
    typesMap.put(RUN_MINUTE, RUN_MINUTE);
    typesMap.put(RUN_HOUR, RUN_HOUR);
    typesMap.put(RUN_DAY, RUN_DAY);
    typesMap.put(RUN_WEEK, RUN_WEEK);
    typesMap.put(RUN_MONTH, RUN_MONTH);
    typesMap.put(RUN_YEAR, RUN_YEAR);
  }
  
  private char type = RUN_NONE;
  
  private int interval = 1;
  
  private int date = 1;
  
  private int month = 1;
  
  private String startTime = "00:00";
  
  private String endTime = "00:00";
  
  private long lastRun;

  private String days = "MTWHFAS";

  public static ScheduleParser parse(String str)
  {
    ScheduleParser parser = new ScheduleParser();
    StringTokenizer tok = new StringTokenizer(str, ",");
    while(tok.hasMoreTokens()) {
      String elem = tok.nextToken();
      int index = elem.indexOf('=');
      if(index != -1) {
        String key = elem.substring(0, index);
        String value = elem.substring(index + 1);
        if(value.length() != 0) {
          parser.putItem(key, value);
        }
      }
    }
    return parser;
  }

  private void putItem(String key, String value)
  {
    if("Schedule".equals(key)) {
      if(value.length() == 1) {
        char c = value.charAt(0);
        if(typesMap.containsKey(c))
          type = c;
      }
    }
    else if("Interval".equals(key)) {
      try {
        interval = Integer.parseInt(value);
      }
      catch(Exception ex) {
        // IGNORE
      }
    }
    else if("Days".equals(key)) {
      Map<Character, Character> m = new HashMap<Character, Character>();
      StringBuffer sb = new StringBuffer();
      for(int i = 0; i < value.length(); ++i) {
        char c = value.charAt(i);
        if(daysMap.containsKey(c) == false)
          continue;
        m.put(c, c);
        sb.append(c);
      }
      
      days = sb.toString();
    }
    else if("Date".equals(key)) {
      try {
        date = Integer.parseInt(value);
      }
      catch(Exception ex) {
        // IGNORE
      }
    }
    else if("Month".equals(key)) {
      try {
        month = Integer.parseInt(value);
      }
      catch(Exception ex) {
        // IGNORE
      }
    }
    else if("StartTime".equals(key)) {
      startTime = value;
    }
    else if("FinishTime".equals(key)) {
      endTime = value;
    }
    else if("LastRun".equals(key)) {
      try {
        lastRun = Integer.parseInt(value);
      }
      catch(Exception ex) {
        // IGNORE
      }
    }
  }
  
  public void setDays(String days)
  {
    Map<Character, Character> m = new HashMap<Character, Character>();
    for(int i = 0; i < days.length(); ++i) {
      char c = days.charAt(i);
      if(daysMap.containsKey(new Character(c)) == false)
        throw new IllegalArgumentException("Invalid days specification: " + c);
      if(m.containsKey(c))
        throw new IllegalArgumentException("Invalid days specification - duplicated days: " + days);
      m.put(c, c);
    }
    
    this.days = days;
  }
  
  public String getDays()
  {
    return this.days;
  }
  
  public int getDate()
  {
    return date;
  }

  public void setDate(int date)
  {
    this.date = date;
  }

  public String getEndTime()
  {
    return endTime;
  }

  public void setEndTime(String endTime)
  {
    this.endTime = endTime;
  }

  public int getInterval()
  {
    return interval;
  }

  public void setInterval(int interval)
  {
    this.interval = interval;
  }

  public long getLastRun()
  {
    return lastRun;
  }

  public void setLastRun(long lastRun)
  {
    this.lastRun = lastRun;
  }

  public int getMonth()
  {
    return month;
  }

  public void setMonth(int month)
  {
    this.month = month;
  }

  public String getStartTime()
  {
    return startTime;
  }

  public void setStartTime(String startTime)
  {
    this.startTime = startTime;
  }

  public char getType()
  {
    return type;
  }

  public void setType(char type)
  {
    this.type = type;
  }

  /**
   * Generates the text to be inserted to the OPTIONS field of the design object.
   */
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("Schedule=");
    sb.append(type);
    
    if(type != RUN_NONE) {
      sb.append(",Interval=");
      sb.append(interval);
      
      if(type == RUN_SECOND || type == RUN_MINUTE || type == RUN_HOUR || type == RUN_DAY || type == RUN_WEEK) {
        sb.append(",Days=");
        sb.append(days);
        sb.append(",StartTime=");
        sb.append(startTime);
        
        if(type != RUN_DAY && type != RUN_WEEK) {
          sb.append(",FinishTime=");
          sb.append(endTime);
        }
      }
      else if(type == RUN_MONTH) {
        sb.append(",Date=");
        sb.append(date);
        sb.append(",StartTime=");
        sb.append(startTime);
      }
      else if(type == RUN_YEAR) {
        sb.append(",Date=");
        sb.append(date);
        sb.append(",Month=");
        sb.append(date);
        sb.append(",StartTime=");
        sb.append(startTime);
      }
    }
    
    sb.append(",LastRun=");
    if(lastRun > 0)
      sb.append(lastRun);
    
    return sb.toString();
  }
}
