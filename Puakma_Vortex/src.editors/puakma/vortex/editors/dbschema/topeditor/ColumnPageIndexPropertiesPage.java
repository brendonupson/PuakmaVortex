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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import puakma.coreide.objects2.TableColumn;
import puakma.vortex.editors.dbeditor.DbEditorController;
import puakma.vortex.editors.dbeditor.commands.PropertiesChangeCommand;
import puakma.vortex.swt.BasePropertiesPageController;
import puakma.vortex.swt.DialogBuilder2;

public class ColumnPageIndexPropertiesPage extends BasePropertiesPageController
{
  private Composite mainComposite;
  private DbEditorController controller;
  private TableColumn column;
  private Button autoIncButton;
  private Button pkButton;
  private Button uniqButton;

  public ColumnPageIndexPropertiesPage(DbEditorController controller)
  {
    if(controller == null)
      throw new IllegalArgumentException("Controller cannot be null");
    
    this.controller = controller;
  }

  public Composite createComposite(Composite parent)
  {
    DialogBuilder2 builder = new DialogBuilder2(parent);
    mainComposite = builder.createFormsLFComposite("Index Column Properties", true, 2);
    Composite c = mainComposite.getParent().getParent();
    
    pkButton = builder.createCheckboxRow("Primary Key");
    uniqButton = builder.createCheckboxRow("Unique");
    autoIncButton = builder.createCheckboxRow("Auto Increment");
    
    builder.closeComposite();
    builder.finishBuilder();
    
    hookControl(pkButton);
    hookControl(uniqButton);
    hookControl(autoIncButton);
    
    return c;
  }

  public void save()
  {
    List<String> changedProps = new ArrayList<String>();
    List<Boolean> changedValues = new ArrayList<Boolean>();
    // CHECK WHICH TEXTS DIFFER FROM ORIGINAL VALUES
    boolean b = pkButton.getSelection();
    if(b != column.isPk()) {
      changedProps.add("pk");
      changedValues.add(Boolean.valueOf(b));
    }
    b = uniqButton.getSelection();
    if(b != column.isUnique()) {
      changedProps.add("unique");
      changedValues.add(new Boolean(b));
    }
    b = autoIncButton.getSelection();
    if(b != column.isAutoInc()) {
      changedProps.add("autoInc");
      changedValues.add(new Boolean(b));
    }
    
    if(changedProps.size() == 0)
      return;
    
    String[] propNames = changedProps.toArray(new String[changedProps.size()]);
    Object[] newPropValues = changedValues.toArray(new Object[changedProps.size()]);
    
    PropertiesChangeCommand command = new PropertiesChangeCommand(TableColumn.class, propNames, column,
                                                                  newPropValues);
    controller.getCommandStack().execute(command);    
  }

  public void reset()
  {
    column = null;
  }

  public void setup(Object model)
  {
    if(model instanceof TableColumn == false)
      throw new IllegalArgumentException("Model passed to the editor has to be of type TableColumn");
    
    column = (TableColumn) model;
    pkButton.setSelection(column.isPk());
    uniqButton.setSelection(column.isUnique());
    autoIncButton.setSelection(column.isAutoInc());
  }

  public void dispose()
  {
    super.dispose();
    
//    mainComposite.dispose();
    mainComposite = null;
    autoIncButton = null;
    pkButton = null;
    uniqButton = null;
    
    column = null;
  }

  public String getName()
  {
    return "Index";
  }

  public String getTooltip()
  {
    return "Index Options";
  }
}
