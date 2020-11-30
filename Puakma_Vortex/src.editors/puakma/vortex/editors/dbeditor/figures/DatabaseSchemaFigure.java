/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jan 17, 2006
 * 
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.dbeditor.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ScalableFreeformLayeredPane;

public class DatabaseSchemaFigure extends ScalableFreeformLayeredPane
{
  public DatabaseSchemaFigure()
  {
    super();

    setOpaque(true);
    setBackgroundColor(ColorConstants.red);

    // XYLayout layout = new XYLayout();
    // setLayoutManager(layout);
  }
}
