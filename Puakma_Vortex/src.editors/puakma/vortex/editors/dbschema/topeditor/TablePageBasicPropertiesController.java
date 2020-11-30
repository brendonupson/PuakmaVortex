/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 23, 2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.dbschema.topeditor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import puakma.coreide.objects2.Table;
import puakma.utils.lang.StringUtil;
import puakma.vortex.editors.dbeditor.DbEditorController;
import puakma.vortex.editors.dbeditor.commands.PropertiesChangeCommand;
import puakma.vortex.swt.BasePropertiesPageController;
import puakma.vortex.swt.DialogBuilder2;

public class TablePageBasicPropertiesController extends BasePropertiesPageController implements PropertyChangeListener
{
  private Table table;
  private Text nameText;
  private Composite mainComposite;
  private Text descText;
  private DbEditorController controller;
  
  public TablePageBasicPropertiesController(DbEditorController controller)
  {
    this.controller = controller;
  }

  public Composite createComposite(Composite parent)
  {
    DialogBuilder2 builder = new DialogBuilder2(parent);
    mainComposite = builder.createFormsLFComposite("General properties", true, 2);
    Composite c = mainComposite.getParent().getParent();
    
    nameText = builder.createEditRow("Name:");
    descText = builder.createMemoRow("Description:", 4);
    
    builder.closeComposite();
    
    builder.finishBuilder();
    
    hookControl(nameText);
    hookControl(descText);
    
    return c;
  }

  public void save()
  {
    List<String> changedProps = new ArrayList<String>();
    List<String> changedValues = new ArrayList<String>();
    // CHECK WHICH TEXTS DIFFER FROM ORIGINAL VALUES
    String tmp = nameText.getText();
    if(tmp.equals(table.getName()) == false) {
      changedProps.add("name");
      changedValues.add(tmp);
    }
    tmp = descText.getText();
    if(tmp.equals(table.getDescription()) == false) {
      changedProps.add("description");
      changedValues.add(tmp);
    }
    
    if(changedProps.size() == 0)
      return;
    
    String[] propNames = changedProps.toArray(new String[changedProps.size()]);
    String[] newPropValues = changedValues.toArray(new String[changedProps.size()]);
    
    PropertiesChangeCommand command = new PropertiesChangeCommand(Table.class, propNames, table,
                                                                  newPropValues);
    controller.getCommandStack().execute(command);
    
    markSaved();
  }

  public void reset()
  {
    table.removeListener(this);
    
    // CLEAR ALL TEXTS
    nameText.setText(StringUtil.EMPTY_STRING);
    descText.setText(StringUtil.EMPTY_STRING);
  }

  public void setup(Object model)
  {
    if(model instanceof Table == false)
      throw new IllegalArgumentException("Model set to page is not instance of Table object");
    
    table = (Table) model;
    table.addListener(this);
    
    nameText.setText(table.getName());
    descText.setText(table.getDescription());
  }

  public void dispose()
  {
    super.dispose();
    
    //mainComposite.dispose();
    nameText = null;
    descText = null;
    mainComposite = null;
  }

  public String getName()
  {
    return "General";
  }

  public String getTooltip()
  {
    return "General Properties of Table \"" + table.getName() + "\"";
  }

  public void propertyChange(PropertyChangeEvent evt)
  {
    String propName = evt.getPropertyName();
    if(Table.PROP_NAME.equals(propName)) {
      nameText.setText((String) evt.getNewValue());
    }
    else if(Table.PROP_DESCRIPTION.equals(propName)) {
      descText.setText((String) evt.getNewValue());
    }
  }
}
