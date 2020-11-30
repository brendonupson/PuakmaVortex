/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jan 20, 2006
 * 
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.dbeditor;

import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.SelectionToolEntry;
import org.eclipse.gef.palette.ToolEntry;

import puakma.coreide.objects2.Table;
import puakma.coreide.objects2.TableColumn;
import puakma.vortex.VortexPlugin;

public class DbPaletteRoot extends PaletteRoot
{
  public DbPaletteRoot()
  {
    super();
    
    PaletteGroup selection = new PaletteGroup("Selection");
    
    // the selection tool
    ToolEntry tool = new SelectionToolEntry();
    selection.add(tool);
    
    add(selection);

    // use selection tool as default entry
    setDefaultEntry(tool);
    
    PaletteDrawer entities = new PaletteDrawer("Entities");
    
    // TODO: ADD LARGE ICONS THERE - IS IT REALLY NEEDED???
    CombinedTemplateCreationEntry tableEntry = new CombinedTemplateCreationEntry("Table",
                     "Create a new table", Table.class, new PaletteDataElementFactory(Table.class),
                     VortexPlugin.getDefault().getImageDescriptor("table.png"),
                     VortexPlugin.getDefault().getImageDescriptor("table.png"));
    
    CombinedTemplateCreationEntry columnEntry = new CombinedTemplateCreationEntry("Column",
        "Create a new column", TableColumn.class, new PaletteDataElementFactory(TableColumn.class),
        VortexPlugin.getDefault().getImageDescriptor("column.png"),
        VortexPlugin.getDefault().getImageDescriptor("column.png"));
    
    entities.add(tableEntry);
    entities.add(columnEntry);

    add(entities);
    
//    PaletteDrawer connections = new PaletteDrawer("Dependencies");
//    
//    ConnectionCreationToolEntry fkEntry = new ConnectionCreationToolEntry("Foreign Key",
//        "Create a new foreign key connection", null,
//        VortexPlugin.getDefault().getImageDescriptor("dependency.png"),
//        VortexPlugin.getDefault().getImageDescriptor("dependency.png"));
//    
//    connections.add(fkEntry);
//    
//    add(connections);
  }
}
