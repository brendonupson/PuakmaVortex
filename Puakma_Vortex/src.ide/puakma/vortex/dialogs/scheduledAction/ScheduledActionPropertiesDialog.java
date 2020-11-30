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
package puakma.vortex.dialogs.scheduledAction;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import puakma.coreide.ScheduleParser;
import puakma.coreide.objects2.JavaObject;
import puakma.vortex.VortexPlugin;
import puakma.vortex.swt.DialogBuilder2;
import puakma.vortex.swt.SWTUtil;
import puakma.vortex.swt.TitleAreaDialog2;

import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.GregorianCalendar;

public class ScheduledActionPropertiesDialog extends TitleAreaDialog2 implements
                                                                     SelectionListener,
                                                                     ModifyListener, IRunnableWithProgress
{
  private static final Map<Integer, Character> indexToCharScheduleMap = new HashMap<Integer, Character>();
  private static final Map<Character, Integer> charToIndexScheduleMap = new HashMap<Character, Integer>();
  
  private static final Map<Integer, Character> indexToCharDaysMap = new HashMap<Integer, Character>();
  private static final Map<Character, Integer> charToIndexDaysMap = new HashMap<Character, Integer>();
  
  private static final String REGEX_HOUR_MATCH = "^([0-9]){1,2}:[0-9][0-9]$";
  private static final Pattern REGEX_HOUR_PATTERN = Pattern.compile(REGEX_HOUR_MATCH);
  
  static {
    indexToCharScheduleMap.put(0, ScheduleParser.RUN_NONE);
    indexToCharScheduleMap.put(1, ScheduleParser.RUN_SECOND);
    indexToCharScheduleMap.put(2, ScheduleParser.RUN_MINUTE);
    indexToCharScheduleMap.put(3, ScheduleParser.RUN_HOUR);
    indexToCharScheduleMap.put(4, ScheduleParser.RUN_DAY);
    indexToCharScheduleMap.put(5, ScheduleParser.RUN_WEEK);
    indexToCharScheduleMap.put(6, ScheduleParser.RUN_MONTH);
    indexToCharScheduleMap.put(7, ScheduleParser.RUN_YEAR);

    charToIndexScheduleMap.put(ScheduleParser.RUN_NONE, 0);
    charToIndexScheduleMap.put(ScheduleParser.RUN_SECOND, 1);
    charToIndexScheduleMap.put(ScheduleParser.RUN_MINUTE, 2);
    charToIndexScheduleMap.put(ScheduleParser.RUN_HOUR, 3);
    charToIndexScheduleMap.put(ScheduleParser.RUN_DAY, 4);
    charToIndexScheduleMap.put(ScheduleParser.RUN_WEEK, 5);
    charToIndexScheduleMap.put(ScheduleParser.RUN_MONTH, 6);
    charToIndexScheduleMap.put(ScheduleParser.RUN_YEAR, 7);
    
    indexToCharDaysMap.put(0, ScheduleParser.DAY_MONDAY);
    indexToCharDaysMap.put(1, ScheduleParser.DAY_TUESDAY);
    indexToCharDaysMap.put(2, ScheduleParser.DAY_WEDNESDAY);
    indexToCharDaysMap.put(3, ScheduleParser.DAY_THURSDAY);
    indexToCharDaysMap.put(4, ScheduleParser.DAY_FRIDAY);
    indexToCharDaysMap.put(5, ScheduleParser.DAY_SATURDAY);
    indexToCharDaysMap.put(6, ScheduleParser.DAY_SUNDAY);

    charToIndexDaysMap.put(ScheduleParser.DAY_MONDAY, 0);
    charToIndexDaysMap.put(ScheduleParser.DAY_TUESDAY, 1);
    charToIndexDaysMap.put(ScheduleParser.DAY_WEDNESDAY, 2);
    charToIndexDaysMap.put(ScheduleParser.DAY_THURSDAY, 3);
    charToIndexDaysMap.put(ScheduleParser.DAY_FRIDAY, 4);
    charToIndexDaysMap.put(ScheduleParser.DAY_SATURDAY, 5);
    charToIndexDaysMap.put(ScheduleParser.DAY_SUNDAY, 6);
  }
  
  private Combo scheduleTypeCombo;

  private Composite area;

  private Text interval;

  private Text date;

  private Combo month;

  private Text startTime;

  private Text endTime;

  private Label lastRun;

  private Button[] days;

  private Composite mainComp;
  
  private ScheduleParser parser;
  
  private JavaObject jo;

  public ScheduledActionPropertiesDialog(Shell parentShell, JavaObject jo)
  {
    super(parentShell, "scheduledActionDlgId");
    
    this.jo = jo;
    this.parser = ScheduleParser.parse(jo.getOptions());
  }

  protected void initialize()
  {
    getShell().setText("Edit Schedule");
    setTitle("Edit Schedule");
    setDescription("Edit Schedule for the action " + jo.getName());
    setTitleImage(VortexPlugin.getImage("wiz/puakma-wizard.png"));
    
    
    scheduleTypeCombo.add("None");
    scheduleTypeCombo.add("Second");
    scheduleTypeCombo.add("Minute");
    scheduleTypeCombo.add("Hour");
    scheduleTypeCombo.add("Day");
    scheduleTypeCombo.add("Week");
    scheduleTypeCombo.add("Month");
    scheduleTypeCombo.add("Year");
    int index = charToIndexScheduleMap.get(parser.getType());
    scheduleTypeCombo.select(index);
    
    scheduleTypeCombo.addSelectionListener(this);
    
    recreateArea();
    displayData();
  }

  protected Control createDialogArea(Composite parent)
  {
    mainComp = (Composite) super.createDialogArea(parent);
    
    DialogBuilder2 builder = new DialogBuilder2(mainComp, 2);

    scheduleTypeCombo = builder.createComboRow("Schedule type:", true);
    builder.createSeparatorRow(true);

    builder.finishBuilder();

    return mainComp;
  }

  protected void okPressed()
  {
    try {
      saveData();
      
      // TODO: add here control for the progress monitor...
      NullProgressMonitor monitor = new NullProgressMonitor();
      ModalContext.run(this, true, monitor, Display.getDefault());
      
      super.okPressed();
    }
    catch(InvocationTargetException e) {
      VortexPlugin.log(e);
    }
    catch(InterruptedException e) {
      // IGNORE
    }
  }
  
  /**
   * Updates error status of this dialog depending on the current values.
   */
  private void updateErrors()
  {
    String error = createErrorString();
    setErrorMessage(error);
  }
  
  /**
   * Creates message with error description.
   */
  private String createErrorString()
  {
    if(interval != null) {
      String text = interval.getText();
      if(text.length() == 0)
        return "Type scheduling interval";
      int t = Integer.parseInt(text);
      if(t < 0)
        return "Interval cannot be negative";
    }
    
    if(date != null) {
      String text = date.getText();
      if(text.length() == 0)
        return "Type date";
      int t = Integer.parseInt(text);
      if(t > 31)
        return "There is maximally 31 days in the month";
    }
    
    if(startTime != null) {
      if(REGEX_HOUR_PATTERN.matcher(startTime.getText()).matches() == false)
        return "Start time must be in the form [H]H:MM";
    }
    
    if(endTime != null) {
      if(REGEX_HOUR_PATTERN.matcher(endTime.getText()).matches() == false)
        return "Finish time must be in the form [H]H:MM";
    }
    
    return null;
  }

  /**
   * Saves data from dialog to the internal structures.
   */
  private void saveData()
  {
    if(interval != null) {
      try {
        int inte = Integer.parseInt(interval.getText());
        parser.setInterval(inte);
      }
      catch(Exception ex) {}
    }

    if(date != null) {
      try {
        int inte = Integer.parseInt(date.getText());
        parser.setDate(inte);
      }
      catch(Exception ex) {}
    }

    if(month != null) {
      int index = month.getSelectionIndex();
      parser.setMonth(index);
    }

    if(startTime != null) {
      parser.setStartTime(startTime.getText());
    }

    if(endTime != null) {
      parser.setEndTime(endTime.getText());
    }

    if(days != null) {
      StringBuffer sb = new StringBuffer();
      for(int i = 0; i < days.length; ++i) {
        boolean sel = days[i].getSelection();
        if(sel)
          sb.append(indexToCharDaysMap.get(i));
      }
      parser.setDays(sb.toString());
    }
  }
  
  /**
   * Destroys area which contains all the controls
   *
   */
  private void destroyArea()
  {
    area.dispose();
    area = null;

    interval = null;
    date = null;
    month = null;
    startTime = null;
    endTime = null;
    lastRun = null;
    days = null;
  }
  
  /**
   * Creates a new controls inside the area with the content according to the
   * current schema of scheduling.
   */
  private void recreateArea()
  {
    DialogBuilder2 builder = new DialogBuilder2(mainComp);
    area = builder.createComposite(7);
    
    char type = parser.getType();
    
    if(type != ScheduleParser.RUN_NONE) {
      interval = builder.createEditRow("Interval:");
      
      if(type == ScheduleParser.RUN_SECOND || type == ScheduleParser.RUN_MINUTE || 
         type == ScheduleParser.RUN_HOUR || type == ScheduleParser.RUN_DAY || 
         type == ScheduleParser.RUN_WEEK) {
        days = new Button[7];
        days[0] = builder.appendCheckbox("Monday");
        days[1] = builder.appendCheckbox("Tuesday");
        days[2] = builder.appendCheckbox("Wednesday");
        days[3] = builder.appendCheckbox("Thursday");
        days[4] = builder.appendCheckbox("Friday");
        days[5] = builder.appendCheckbox("Saturday");
        days[6] = builder.appendCheckbox("Sunday");
        
        startTime = builder.createEditRow("Start time:");
        
        if(type != ScheduleParser.RUN_DAY && type != ScheduleParser.RUN_WEEK) {
          endTime = builder.createEditRow("Finish time:");
        }
      }
      else if(type == ScheduleParser.RUN_MONTH) {
        date = builder.createEditRow("Date:");
        startTime = builder.createEditRow("Start time:");
      }
      else if(type == ScheduleParser.RUN_YEAR) {
        date = builder.createEditRow("Date:");
        //month = builder.createEditRow("Month:");
        month = builder.createComboRow("Month:", true);
        startTime = builder.createEditRow("Start time:");
      }
    }

    if(parser.getLastRun() == 0)
      lastRun = builder.createTwoLabelRow("Last run:", "Not available");
    else
      lastRun = builder.createTwoLabelRow("Last run:", Long.toString(parser.getLastRun()));
    
    // ADD ALL SELECTION LISTENERS
    // ALSO ADD SOME CHECKING LIKE INT VALIDATORS, ETC...
    if(interval != null) {
      interval.addModifyListener(this);
      SWTUtil.setIntValidation(interval);
    }
    if(date != null) {
      date.addModifyListener(this);
      SWTUtil.setIntValidation(date);
    }
    if(month != null) {
      month.addModifyListener(this);
      SimpleDateFormat format = new SimpleDateFormat("MMMM");
      DateFormat.getDateInstance(DateFormat.MONTH_FIELD);
      for(int i = 0; i < 12; ++i) {
        GregorianCalendar c = new GregorianCalendar(2006, i, 1);
        String s = format.format(c.getTime());
        int cc = UCharacter.toTitleCase(s.charAt(0));
        s = ((char)cc) + s.substring(1);
        month.add(s);
      }
    }
    if(startTime != null)
      startTime.addModifyListener(this);
    if(endTime != null)
      endTime.addModifyListener(this);
    if(days != null) {
      for(Button b : days) {
        if(b != null)
          b.addSelectionListener(this);
      }
    }
    
    mainComp.layout();
    getShell().layout(true);
    
    builder.closeComposite();
    builder.finishBuilder();
  }
  
  /**
   * Displays data inside controls.
   */
  private void displayData()
  {
    if(interval != null)
      interval.setText(Integer.toString(parser.getDate()));

    if(date != null)
      date.setText(Integer.toString(parser.getDate()));

    if(month != null) {
      int mon = parser.getMonth();
      if(mon > 0 && mon < 12)
        month.select(mon);
      else
        month.select(0);
    }
    
    if(startTime != null)
      startTime.setText(parser.getStartTime());

    if(endTime != null)
      endTime.setText(parser.getEndTime());

    if(days != null) {
      for(Button b : days) {
        if(b != null)
          b.setSelection(false);
      }
      
      String s = parser.getDays();
      for(int i = 0; i < s.length(); ++i) {
        char c = s.charAt(i);
        if(charToIndexDaysMap.containsKey(c)) {
          int index = charToIndexDaysMap.get(c);
          if(days[index] != null)
            days[index].setSelection(true);
        }
      }
      StringBuffer sb = new StringBuffer();
      for(int i = 0; i < days.length; ++i) {
        if(days[i] == null)
          continue;
        
        boolean sel = days[i].getSelection();
        if(sel)
          sb.append(indexToCharDaysMap.get(i));
      }
      parser.setDays(sb.toString());
    }
  }

  public void widgetDefaultSelected(SelectionEvent e)
  {
    updateErrors();
  }

  public void widgetSelected(SelectionEvent e)
  {
    if(e.widget == scheduleTypeCombo) {
      char type = indexToCharScheduleMap.get(scheduleTypeCombo.getSelectionIndex());
      parser.setType(type);
      
      saveData();
      
      destroyArea();
      recreateArea();
      
      displayData();
    }
    else {
      updateErrors();
    }
  }

  public void modifyText(ModifyEvent e)
  {
    updateErrors();
  }

  public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
  {
    monitor.beginTask("Setting schedule options", 10);
    
    try {
      JavaObject jo = (JavaObject) this.jo.makeWorkingCopy();
      jo.setOptions(parser.toString());
      monitor.worked(1);
      jo.commit();
      monitor.worked(9);
    }
    catch(Exception ex) {
      throw new InvocationTargetException(ex);
    }
    finally {
      monitor.done();
    }
  }
}
