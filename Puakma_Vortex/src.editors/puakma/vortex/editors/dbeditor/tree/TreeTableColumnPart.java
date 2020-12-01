/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 11, 2006
 * 
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.dbeditor.tree;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.gef.editparts.AbstractTreeEditPart;
import org.eclipse.swt.graphics.Image;

import puakma.coreide.objects2.TableColumn;
import puakma.vortex.VortexPlugin;

public class TreeTableColumnPart extends AbstractTreeEditPart implements PropertyChangeListener
{
  public TreeTableColumnPart()
  {
  }

  public TreeTableColumnPart(TableColumn column)
  {
    super(column);
  }
  
  public TableColumn getColumn()
  {
    return (TableColumn) getModel();
  }
  
  public void activate()
  {
    if(isActive() == false) {
      super.activate();
      
      getColumn().addListener(this);
    }
  }

  public void deactivate()
  {
    if(isActive()) {
      getColumn().removeListener(this);
      
      super.deactivate();
    }
  }
  
  protected Image getImage()
  {
    return VortexPlugin.getDefault().getImage("column.png");
  }
  
  protected String getText()
  {
    return getColumn().toString();
  }

  public void propertyChange(PropertyChangeEvent evt)
  {
    refreshVisuals();
  }
  
  public Object getAdapter(Class key)
  {
    if(key == TableColumn.class)
      return getColumn();
    
    return super.getAdapter(key);
  }
}
