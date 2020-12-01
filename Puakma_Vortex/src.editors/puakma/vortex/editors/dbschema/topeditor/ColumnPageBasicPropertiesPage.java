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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import puakma.coreide.objects2.TableColumn;
import puakma.vortex.editors.dbeditor.DbEditorController;
import puakma.vortex.editors.dbeditor.commands.PropertiesChangeCommand;
import puakma.vortex.swt.BasePropertiesPageController;
import puakma.vortex.swt.DialogBuilder2;

public class ColumnPageBasicPropertiesPage extends BasePropertiesPageController
{
  private Composite mainComposite;
  private DbEditorController controller;
  private Text nameText;
  // TODO: change to combo, and fill it with information about data types from database
  private Text typeCombo;
  private Text sizeText;
  private Text decimalsText;
  private TableColumn column;

  public ColumnPageBasicPropertiesPage(DbEditorController controller)
  {
    if(controller == null)
      throw new IllegalArgumentException("Controller cannot be null");
    
    this.controller = controller;
  }

  public Composite createComposite(Composite parent)
  {
    DialogBuilder2 builder = new DialogBuilder2(parent);
    mainComposite = builder.createFormsLFComposite("General Column Properties", true, 2);
    Composite c = mainComposite.getParent().getParent();
    
    nameText = builder.createEditRow("Name:");
    typeCombo = builder.createEditRow("Type");
    sizeText = builder.createNumericRow("Type Size:");
    decimalsText = builder.createNumericRow("Decimals:");
    builder.createLabelRow(" ");
    
    builder.closeComposite();
    
    hookControl(nameText);
    hookControl(typeCombo);
    hookControl(sizeText);
    hookControl(decimalsText);
    
    return c;
  }

  public void save()
  {
    List<String> changedProps = new ArrayList<String>();
    List<String> changedValues = new ArrayList<String>();
    // CHECK WHICH TEXTS DIFFER FROM ORIGINAL VALUES
    String tmp = nameText.getText();
    if(tmp.equals(column.getName()) == false) {
      changedProps.add("name");
      changedValues.add(tmp);
    }
    tmp = typeCombo.getText();
    if(tmp.equals(column.getType()) == false) {
      changedProps.add("type");
      changedValues.add(tmp);
    }
    tmp = sizeText.getText();
    if(tmp.length() > 0 && tmp.equals(Integer.toString(column.getTypeSize())) == false) {
      changedProps.add("typeSize");
      changedValues.add(tmp);
    }
    tmp = decimalsText.getText();
    if(tmp.length() > 0 && tmp.equals(Integer.toString(column.getFloatDecimals())) == false) {
      changedProps.add("floatDecimals");
      changedValues.add(tmp);
    }
    
    if(changedProps.size() == 0)
      return;
    
    String[] propNames = changedProps.toArray(new String[changedProps.size()]);
    String[] newPropValues = changedValues.toArray(new String[changedProps.size()]);
    
    PropertiesChangeCommand command = new PropertiesChangeCommand(TableColumn.class, propNames, column,
                                                                  newPropValues);
    controller.getCommandStack().execute(command);
    
    markSaved();
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
    nameText.setText(column.getName());
    typeCombo.setText(column.getType());
    sizeText.setText(Integer.toString(column.getTypeSize()));
    decimalsText.setText(Integer.toString(column.getFloatDecimals()));
  }

  public void dispose()
  {
    super.dispose();
//    mainComposite.dispose();
    
    mainComposite = null;
    typeCombo = null;
    nameText = null;
    typeCombo = null;
    sizeText = null;
    decimalsText = null;
    
    column = null;
  }

  public String getName()
  {
    return "General";
  }

  public String getTooltip()
  {
    return "General column Properties";
  }
}
