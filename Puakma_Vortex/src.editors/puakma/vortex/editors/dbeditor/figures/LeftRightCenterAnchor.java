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
package puakma.vortex.editors.dbeditor.figures;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;

/**
 * This anchor will define either left or right edge of the figure.
 *
 * @author Martin Novak &lt;mn@puakma.net&gt;
 */
public class LeftRightCenterAnchor extends AbstractConnectionAnchor
{
  public LeftRightCenterAnchor(IFigure figure)
  {
    super(figure);
  }

  public Point getLocation(Point reference)
  {
    // Rectangle r = getOwner().getBounds().getCopy();
    // getOwner().translateToAbsolute(r);
    // int off = r.height / 2;
    // if(r.contains(reference) || r.right() > reference.x)
    // return r.getTopLeft().translate(0, off);
    // else
    // return r.getTopRight().translate(0, off);

    Point p = getOwner().getBounds().getCenter();
    getOwner().translateToAbsolute(p);
    if(reference.x < p.x)
      p = getOwner().getBounds().getLeft();
    else
      p = getOwner().getBounds().getRight();
    getOwner().translateToAbsolute(p);
    return p;
  }
}
