/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Mar 12, 2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.dbeditor.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;

import puakma.coreide.FkConnectionImpl;
import puakma.coreide.objects2.ApplicationObject;
import puakma.coreide.objects2.FkConnection;
import puakma.coreide.objects2.Table;
import puakma.coreide.objects2.TableColumn;

/**
 * This class implements connection between two tables/columns. Note that the connection
 * is not bidirectional, so we know only the source part in the model, the target is
 * unknown. Thus we have to create this object as a bridge between source, and target
 * parts. The {@link ConnectionPart} instances will be saved in the
 * {@link Table#setData()} in the List there.
 * 
 * @author Martin Novak
 */
public class ConnectionPart extends AbstractConnectionEditPart
{
  protected void createEditPolicies()
  {

  }

  protected IFigure createFigure()
  {
    PolylineConnection connection = (PolylineConnection) super.createFigure();
    connection.setTargetDecoration(new PolygonDecoration()); // arrow at target endpoint
    // connection.setLineStyle(getCastedModel().getLineStyle()); // line drawing style
    return connection;
  }
}
