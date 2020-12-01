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
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Text;

import puakma.vortex.VortexPlugin;

public class EditableFigure extends Label implements CellEditorLocator
{
  private boolean selected;
  private boolean fitSizeToParent;
  private boolean bold;
  /**
   * Tells if the font has been initialized yet
   */
  private boolean initFont = false;
  private Image image;

  public EditableFigure(String text)
  {
    super(text);

    setOpaque(false);
    setSelected(false);
    setTextAlignment(PositionConstants.LEFT);
    setLabelAlignment(PositionConstants.LEFT);
    //setBackgroundColor(new Color(null, 255, 0, 0));
  }

  public void setSelected(boolean selected)
  {
    this.selected = selected;

    if(selected) {
      setOpaque(true);
      setBackgroundColor(ColorConstants.menuBackgroundSelected);
    }
    else {
      setOpaque(false);
      setBackgroundColor(ColorConstants.white);
    }
  }

  public boolean getSelected()
  {
    return this.selected;
  }

  /**
   * Expands the size of the control by 2 pixel in each direction
   */
  public void relocate(CellEditor celleditor)
  {
    Text text = (Text) celleditor.getControl();
    org.eclipse.swt.graphics.Point pref = text.computeSize(SWT.DEFAULT, SWT.DEFAULT);
    Rectangle rect = getTextBounds().getCopy();
    translateToAbsolute(rect);
    if(text.getCharCount() > 1)
      text.setBounds(rect.x - 2, rect.y - 2, pref.x + 2, pref.y + 2);
    else
      text.setBounds(rect.x - 2, rect.y - 2, pref.y + 2, pref.y + 2);
  }
  
  public void setSnapToParent(boolean snapToParent)
  {
    this.fitSizeToParent = snapToParent;
  }

//  public Rectangle getBounds()
//  {
//    if(1 == 1)
//      return super.getBounds();
//    // TODO: change this!!!
//    Rectangle r = super.getBounds();
//    if(fitSizeToParent && getParent() != null) {
//      Rectangle parentSize = getParent().getBounds();
//      r.width = parentSize.width;
//    }
//    return r;
//  }

  public Font getFont()
  {
    if(initFont == false) {
      initFont = true;
      Font f = super.getFont();
      if(bold == false)
        return f;
      
      FontData[] fds = f.getFontData();
      fds[0].setStyle(SWT.BOLD);
      Font f1 = new Font(f.getDevice(), fds);
      setFont(f1);
    }
    
    return super.getFont();
  }

  /**
   * Sets the bold style font on the figure
   * @param bold true if the font is supposed to be bold
   */
  public void setBold(boolean bold)
  {
    this.initFont = false;
    this.bold = bold;
  }
  
  public void setImage(String image)
  {
    this.image = VortexPlugin.getDefault().getImage(image);
    setIconAlignment(PositionConstants.LEFT);
    setIcon(this.image);
  }

  public Dimension getPreferredSize(int wHint, int hHint)
  {
    // TODO Auto-generated method stub
    return super.getPreferredSize(wHint, hHint);
  }
}
