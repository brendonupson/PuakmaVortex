package puakma.vortex.editors.dbeditor.figures;

import org.eclipse.draw2d.Clickable;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Image;

public class BottomButtonFigure extends Clickable
{
  private Image img;

  private Dimension size = new Dimension();

  private int alignment = PositionConstants.CENTER;

  private Image backImage;

  private Image backImageHighlighted;

  private boolean pressed;

  public BottomButtonFigure(Image image)
  {
    super();
    
    setImage(image);
  }

  public BottomButtonFigure(Image image, Image backgroundImage)
  {
    super();
    
    setImage(image);
    setBackgroundImage(backgroundImage);
  }

  public BottomButtonFigure(Image image, Image backImg, Image backImgHigh)
  {
    super();
    
    setImage(image);
    setBackgroundImage(backImg);
    setBackgroundHighlightImage(backImgHigh);
  }

  /**
   * Calculates the necessary size to display the Image within the figure's
   * client area.
   * 
   * @see org.eclipse.draw2d.Figure#getPreferredSize(int, int)
   */
  public Dimension getPreferredSize(int wHint, int hHint)
  {
    if(getInsets() == NO_INSETS)
      return size;
    
    Insets i = getInsets();
    return size.getExpanded(i.getWidth(), i.getHeight());
  }

  /**
   * @return The Image that this Figure displays
   */
  public Image getImage()
  {
    return img;
  }

  public void setBackgroundHighlightImage(Image backImgHigh)
  {
    if(backImageHighlighted == backImgHigh)
      return;

    backImageHighlighted = backImgHigh;

    repaint();
  }

  public Image setBackgroundHighlightImage()
  {
    return backImageHighlighted;
  }

  public void setBackgroundImage(Image image)
  {
    if(backImage == image)
      return;

    backImage = image;

    repaint();
  }

  public Image getBackgroundImage()
  {
    return backImage;
  }
  
  protected void paintBorder(Graphics graphics)
  {
    // DO NOTHING
  }

  protected void paintFigure(Graphics g)
  {
    Rectangle area = getClientArea();
    org.eclipse.swt.graphics.Rectangle backSize = backImage.getBounds();

    boolean pressed = (getModel().isArmed() || getModel().isSelected());
    if(pressed == false && backImage != null) {
      g.drawImage(backImage, new Rectangle(backSize), area);
    }
    else if(pressed && backImageHighlighted != null) {
      g.drawImage(backImageHighlighted, new Rectangle(backSize), area);
    }

    if(getImage() == null)
      return;

    int x, y;
    switch(alignment & PositionConstants.NORTH_SOUTH) {
      case PositionConstants.NORTH:
        y = area.y;
      break;
      case PositionConstants.SOUTH:
        y = area.y + area.height - size.height;
      break;
      default:
        y = (area.height - size.height) / 2 + area.y;
      break;
    }
    switch(alignment & PositionConstants.EAST_WEST) {
      case PositionConstants.EAST:
        x = area.x + area.width - size.width;
      break;
      case PositionConstants.WEST:
        x = area.x;
      break;
      default:
        x = (area.width - size.width) / 2 + area.x;
      break;
    }
    g.drawImage(getImage(), x, y);
  }

  public void handleMousePressed(MouseEvent event)
  {
    this.pressed = true;

    super.handleMousePressed(event);

    repaint();
  }

  public void handleMouseReleased(MouseEvent event)
  {
    this.pressed = false;

    super.handleMouseReleased(event);

    repaint();
  }

  /**
   * Sets the alignment of the Image within this Figure. The alignment comes
   * into play when the ImageFigure is larger than the Image. The alignment
   * could be any valid combination of the following:
   * 
   * <UL>
   * <LI>PositionConstants.NORTH</LI>
   * <LI>PositionConstants.SOUTH</LI>
   * <LI>PositionConstants.EAST</LI>
   * <LI>PositionConstants.WEST</LI>
   * <LI>PositionConstants.CENTER or PositionConstants.NONE</LI>
   * </UL>
   * 
   * @param flag A constant indicating the alignment
   */
  public void setAlignment(int flag)
  {
    alignment = flag;
  }

  /**
   * Sets the Image that this ImageFigure displays.
   * <p>
   * IMPORTANT: Note that it is the client's responsibility to dispose the given
   * image.
   * 
   * @param image The Image to be displayed. It can be <code>null</code>.
   */
  public void setImage(Image image)
  {
    if(img == image)
      return;
    img = image;
    if(img != null)
      size = new Rectangle(image.getBounds()).getSize();
    else
      size = new Dimension();
    revalidate();
    repaint();
  }
}
