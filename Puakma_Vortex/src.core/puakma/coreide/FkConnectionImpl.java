/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Mar 14, 2006
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import puakma.coreide.objects2.ApplicationObject;
import puakma.coreide.objects2.Database;
import puakma.coreide.objects2.FkConnection;
import puakma.coreide.objects2.Table;
import puakma.coreide.objects2.TableColumn;
import puakma.utils.lang.ListenersList;

/**
 * This is a model of relationshit between two tables
 * 
 * @author Martin Novak
 */
public class FkConnectionImpl implements FkConnection
{
  /**
   * This is the column which is pointing at something
   */
  private TableColumn fkColumn;

  private TableImpl targetTable;

  FkConnectionImpl(TableColumnImpl sourceColumn, TableImpl targetTable)
  {
    setSourceColumn(sourceColumn);
    setTargetTable(targetTable);
  }

  void setTargetTable(TableImpl targetTable)
  {
    this.targetTable = targetTable;
  }
  
  public Table getTargetTable()
  {
    return this.targetTable;
  }

  void setSourceColumn(TableColumn fkColumn)
  {
    this.fkColumn = fkColumn;
  }
  
  public TableColumn getSourceColumn()
  {
    return this.fkColumn;
  }

  public void disposeConnection()
  {
    targetTable.removeTargetConnection(this);
  }

  public void fireNewConnection()
  {
    targetTable.addTargetConnection(this);
  }
}
