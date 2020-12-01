/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 10, 2006
 * 
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.dbschema.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.ActionFactory;

import puakma.coreide.ObjectsFactory;
import puakma.coreide.objects2.Table;
import puakma.coreide.objects2.TableColumn;
import puakma.utils.lang.CollectionsUtil;
import puakma.vortex.editors.dbeditor.commands.TableColumnCreateCommand;
import puakma.vortex.editors.dbeditor.commands.TableColumnDeleteCommand;
import puakma.vortex.editors.dbeditor.commands.TableDeleteCommand;
import puakma.vortex.editors.dbeditor.parts.TablePart;

public class DatabaseSchemaEditorContextMenuProvider extends ContextMenuProvider
{
  public static final String GROUP_BEGIN = "beginGroup";
  public static final String GROUP_EDIT1 = "groupEdit1";
  public static final String GROUP_TYPE = "groupType";
  private ActionRegistry registry;
  private CommandStack stack;

  public DatabaseSchemaEditorContextMenuProvider(EditPartViewer viewer, ActionRegistry registry,
                                                 CommandStack stack)
  {
    super(viewer);
    if(registry == null)
      throw new IllegalArgumentException("Invalid argument: registry is null");
    this.registry = registry;
    this.stack = stack;
  }
  
  public void buildContextMenu(IMenuManager menu)
  {
    IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();
    buildContextMenu(menu, selection);
  }

  public void buildContextMenu(IMenuManager menu, IStructuredSelection selection)  
  {
    IAction action;
    
    
    List<Object> l = new ArrayList<Object>();
    // CONVERT ALL EDIT PARTS TO MODEL, AND COPY THEM TO THE LIST
    Iterator<Object> it = selection.iterator();
    while(it.hasNext()) {
      Object o = it.next();
      if(o instanceof EditPart) {
        o = ((EditPart) o).getModel();
        if(o != null)
          l.add(o);
      }
      else
        l.add(o);
    }
    
    // ADD SOME MENU ITEMS TO EDIT TABLECOLUMN HERE
    boolean singleTableSelection = l.size() == 1 && l.get(0) instanceof Table;
    boolean singleColumnSelection = l.size() == 1 && l.get(0) instanceof TableColumn;
    boolean onlyTablesSelected = CollectionsUtil.areAllItemsOfType(l, Table.class);
    boolean onlyColumnsSelected = CollectionsUtil.areAllItemsOfType(l, TableColumn.class);
    boolean areTablesSelected = CollectionsUtil.areSomeItemsOfType(l, Table.class);
    boolean areColumnsSelected = CollectionsUtil.areSomeItemsOfType(l, TableColumn.class);
    
    menu.add(new Separator(GROUP_BEGIN));
    menu.add(new Separator(GROUP_TYPE));
    menu.add(new Separator(GEFActionConstants.GROUP_EDIT));
    menu.add(new Separator(GROUP_EDIT1));
    menu.add(new Separator(GEFActionConstants.GROUP_VIEW));
    menu.add(new Separator(GEFActionConstants.GROUP_UNDO));
    menu.add(new Separator(GEFActionConstants.GROUP_COPY));
    
    //menu.add(new Separator(GROUP_PRINT));
    //menu.add(new Separator(GROUP_FIND));
    //menu.add(new Separator(GROUP_ADD));
    //menu.add(new Separator(GROUP_REST));
    menu.add(new Separator(GEFActionConstants.MB_ADDITIONS));
    //menu.add(new Separator(GROUP_SAVE));
    
    createActions(menu, l, selection, singleColumnSelection, singleTableSelection,
                  onlyColumnsSelected, onlyTablesSelected, areColumnsSelected,
                  areTablesSelected);    

    menu.appendToGroup(GEFActionConstants.GROUP_UNDO, getAction(ActionFactory.UNDO.getId()));
    menu.appendToGroup(GEFActionConstants.GROUP_UNDO, getAction(ActionFactory.REDO.getId()));

    // menu.appendToGroup(GEFActionConstants.GROUP_EDIT,
    // getAction(ActionFactory.PASTE.getId()));

    action = getAction(GEFActionConstants.DIRECT_EDIT);
    if(action.isEnabled())
      menu.appendToGroup(GROUP_EDIT1, action);
  }

  /**
   * Adds all necessary actions to the registry.
   */
  private void createActions(IMenuManager menu, final List l, IStructuredSelection selection, final boolean singleColumnSelection,
                             boolean singleTableSelection, boolean onlyColumnsSelected,
                             boolean onlyTablesSelected, boolean areColumnsSelected,
                             boolean areTablesSelected)
  {
    boolean isSelectionOneTable = isSelectionOneTable(l);
    
    if(isSelectionOneTable && (areColumnsSelected || areTablesSelected)) {
      menu.appendToGroup(DatabaseSchemaEditorContextMenuProvider.GROUP_BEGIN, new Action("Add Column...") {
        public void run() {
          Object o = l.get(0);
          Map partRegistry = getViewer().getEditPartRegistry();
          TablePart tablePart;
          if(o instanceof Table) {
            tablePart = (TablePart) partRegistry.get(o);
          }
          else if(o instanceof TableColumn) {
            TableColumn c = (TableColumn) o;
            Table t = c.getTable();
            tablePart = (TablePart) partRegistry.get(t);
          }
          else
            throw new IllegalArgumentException("Invalid object here - might be only table or column");
          
          TableColumn col = ObjectsFactory.createTableColumn("XXX");
          TableColumnCreateCommand cmd = new TableColumnCreateCommand(tablePart, null, col, -1);
          stack.execute(cmd);
        }
      });
    }
    
    if(singleColumnSelection || onlyColumnsSelected) {
      String text = singleColumnSelection ? "Drop Column" : "Drop Columns";
      menu.appendToGroup(DatabaseSchemaEditorContextMenuProvider.GROUP_BEGIN, new Action(text) {
        public void run() {
          if(singleColumnSelection) {
            TableColumn column = (TableColumn) l.get(0);
            TableColumnDeleteCommand cmd = new TableColumnDeleteCommand(column);
            stack.execute(cmd);
          }
          else {
            CompoundCommand ccmd = new CompoundCommand("Drop Table Columns");
            
            Iterator it = l.iterator();
            while(it.hasNext()) {
              TableColumn col = (TableColumn) it.next();
              TableColumnDeleteCommand delCmd = new TableColumnDeleteCommand(col);
              ccmd.add(delCmd);
            }
            
            stack.execute(ccmd);
          }
        }
      });
    }
    
    if(singleTableSelection || onlyTablesSelected) {
      String text = singleColumnSelection ? "Drop Table" : "Drop Tables";
      menu.appendToGroup(DatabaseSchemaEditorContextMenuProvider.GROUP_BEGIN, new Action(text) {
        public void run() {
          if(singleColumnSelection) {
            Table table = (Table) l.get(0);
            TableDeleteCommand cmd = new TableDeleteCommand(table);
            stack.execute(cmd);
          }
          else {
            CompoundCommand ccmd = new CompoundCommand("Drop Tables");
            
            Iterator it = l.iterator();
            while(it.hasNext()) {
              Table table = (Table) it.next();
              TableDeleteCommand delCmd = new TableDeleteCommand(table);
              ccmd.add(delCmd);
            }
            
            stack.execute(ccmd);
          }
        }
      });
    }
    
    if(singleColumnSelection)
      addColumnTypeSubmenu(menu, (TableColumn) l.get(0));
    
    if(singleColumnSelection) {
      final TableColumn c = (TableColumn) l.get(0);
      menu.appendToGroup(GEFActionConstants.GROUP_EDIT, new DBSEPrimaryKeyAction(c, stack));
    }
  }

  /**
   * Returns true if in the list there are all columns, and tables from the same table.
   */
  private boolean isSelectionOneTable(List l)
  {
    Table t = null;
    Iterator it = l.iterator();
    while(it.hasNext()) {
      Object o = it.next();
      if(o instanceof Table) {
        if(t == null)
          t = (Table) o;
        else if(t != o)
          return false;
      }
      else if(o instanceof TableColumn) {
        Table tx = ((TableColumn) o).getTable();
        if(t == null)
          t = tx;
        else if(tx != t)
          return false;
      }
      else
        return false;
    }
    
    return true;
  }

  /**
   * Adds submenu containing all necessary types for the current column
   */
  private void addColumnTypeSubmenu(IMenuManager menu, TableColumn column)
  {
    IMenuManager manager;
    
    manager = new MenuManager("Mostly Used Types");
    manager.add(new ChangeTypeAction("INTEGER", stack, column));
    manager.add(new ChangeTypeAction("VARCHAR", stack, column));
    manager.add(new ChangeTypeAction("FLOAT", stack, column));
    manager.add(new ChangeTypeAction("DATE", stack, column));
    manager.add(new ChangeTypeAction("TIME", stack, column));
    menu.appendToGroup(GROUP_TYPE, manager);
    
    manager = new MenuManager("Integer Type");
    manager.add(new ChangeTypeAction("INTEGER", stack, column));
    menu.appendToGroup(GROUP_TYPE, manager);
    
    manager = new MenuManager("Float Type");
    manager.add(new ChangeTypeAction("FLOAT", stack, column));
    manager.add(new ChangeTypeAction("DOUBLE", stack, column));
    menu.appendToGroup(GROUP_TYPE, manager);
    
    manager = new MenuManager("Text Type");
    manager.add(new ChangeTypeAction("VARCHAR", stack, column));
    menu.appendToGroup(GROUP_TYPE, manager);
    
    manager = new MenuManager("Date Type");
    manager.add(new ChangeTypeAction("DATE", stack, column));
    manager.add(new ChangeTypeAction("TIME", stack, column));
    manager.add(new ChangeTypeAction("TIMESTAMP", stack, column));
    menu.appendToGroup(GROUP_TYPE, manager);
    
    manager = new MenuManager("Blob Type");
    menu.appendToGroup(GROUP_TYPE, manager);
    
    manager = new MenuManager("Other Type");
    menu.appendToGroup(GROUP_TYPE, manager);
    
  }

  private IAction getAction(String actionId)
  {
    return registry.getAction(actionId);
  }
}
