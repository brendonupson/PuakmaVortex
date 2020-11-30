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

import puakma.coreide.objects2.Table;
import puakma.coreide.objects2.TableColumn;
import puakma.utils.lang.StringUtil;
import puakma.vortex.editors.dbeditor.DbEditorController;
import puakma.vortex.editors.dbeditor.commands.PropertiesChangeCommand;
import puakma.vortex.swt.BasePropertiesPageController;
import puakma.vortex.swt.DialogBuilder2;

public class ColumnPageDescriptionPropertiesPage extends BasePropertiesPageController
{
  private Composite mainComposite;
  private DbEditorController controller;
  private Text descriptionText;
  private TableColumn column;

  public ColumnPageDescriptionPropertiesPage(DbEditorController controller)
  {
    if(controller == null)
      throw new IllegalArgumentException("Controller cannot be null");
    
    this.controller = controller;
  }

  public Composite createComposite(Composite parent)
  {
    DialogBuilder2 builder = new DialogBuilder2(parent);
    mainComposite = builder.createFormsLFComposite("Description", true, 2);
    Composite c = mainComposite.getParent().getParent();
    
    descriptionText = builder.createMemoRow("Description:", 5);
    
    builder.closeComposite();
    
    hookControl(descriptionText);
    
    return c;
  }

  public void save()
  {
    List<String> changedProps = new ArrayList<String>();
    List<String> changedValues = new ArrayList<String>();
    // CHECK WHICH TEXTS DIFFER FROM ORIGINAL VALUES
    String tmp = descriptionText.getText();
    if(tmp.equals(column.getDescription()) == false) {
      changedProps.add("description");
      changedValues.add(tmp);
    }
    
    if(changedProps.size() == 0)
      return;
    
    String[] propNames = changedProps.toArray(new String[changedProps.size()]);
    String[] newPropValues = changedValues.toArray(new String[changedProps.size()]);
    
    PropertiesChangeCommand command = new PropertiesChangeCommand(Table.class, propNames, column,
                                                                  newPropValues);
    controller.getCommandStack().execute(command);
    
    markSaved();
  }

  public void reset()
  {
    
  }

  public void setup(Object model)
  {
    if(model instanceof TableColumn == false)
      throw new IllegalArgumentException("Model passed to the editor has to be of type TableColumn");
    
    column = (TableColumn) model;
    descriptionText.setText(StringUtil.safeString(column.getDescription()));
  }

  public void dispose()
  {
    super.dispose();

    mainComposite = null;
  }

  public String getName()
  {
    return "Description";
  }

  public String getTooltip()
  {
    return "Column Description";
  }
}
