package puakma.vortex.editors.dbeditor.figures;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;

/**
 * Empty border which defines just some border around the figure. It doesn't
 * paint to the figure.
 * 
 * @author Martin Novak &lt;mn@puakma.net&gt;
 */
public class EmptyBorder extends AbstractBorder
{
  private Insets insets;

  public EmptyBorder(int size)
  {
    this.insets = new Insets(size);
  }

  public EmptyBorder(int top, int left, int bottom, int right)
  {
    this.insets = new Insets(top, left, bottom, right);
  }

  public EmptyBorder(Insets insets)
  {
    this.insets = insets;
  }

  public Insets getInsets(IFigure figure)
  {
    return new Insets(insets);
  }

  public void paint(IFigure figure, Graphics graphics, Insets insets)
  {

  }
}
