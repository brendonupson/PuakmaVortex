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

import java.beans.PropertyChangeListener;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.Clickable;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import puakma.utils.lang.ListenersList;
import puakma.vortex.VortexPlugin;
import puakma.vortex.editors.dbeditor.DbEditorControllerImpl;
import puakma.vortex.preferences.PreferenceConstants;


public class TableFigure extends Shape implements ActionListener
{
  public static final String EVT_PLUS_BTN_PRESSED = "plusBtnPressed";
  
  public static final String EVT_DROP_DOWN_MENU = "dropDownMenuPressed";
  
  private static final int ROUND_CORNER_SIZE = 20;

  public static final int BORDER_WIDTH = 4;

  private EditableFigure nameLabel;
  
  private Figure contentPane;
  
  private boolean selected;

  private Figure bottomLabel;

  private Clickable plusButton;

  private Clickable editButton;
  
  private ListenersList listeners = new ListenersList();

  public TableFigure(String name)
  {
    super();
    
    ToolbarLayout layout = new ToolbarLayout(false);
    layout.setStretchMinorAxis(true);
    setLayoutManager(layout);
    
    setFill(true);
    setOpaque(true);
    setBorder(new EmptyBorder(0));
    
    nameLabel = new EditableFigure(name);
    nameLabel.setBold(true);
    nameLabel.setLabelAlignment(PositionConstants.CENTER);
    int sideWidth = ROUND_CORNER_SIZE;
    nameLabel.setBorder(new EmptyBorder(2, sideWidth, 2, sideWidth));
    add(nameLabel);
    
    //setBorder(new LineBorder(borderColor));
//    setBackgroundColor(ColorConstants.lightGreen);
    setForegroundColor(ColorConstants.black);
    
    // SETUP CONTENT PANE FOR COLUMNS
    contentPane = new Figure();
    ToolbarLayout tl = new ToolbarLayout();
    tl.setStretchMinorAxis(true);
    contentPane.setLayoutManager(tl);
    contentPane.setBorder(new AbstractBorder() {
      public void paint(IFigure figure, Graphics gc, Insets insets)
      {
        Rectangle r = figure.getBounds();
        gc.drawLine(r.x, r.y, r.x + r.width, r.y);
      }
    
      public Insets getInsets(IFigure figure)
      {
        return  new Insets(2);
      }
    });
    add(contentPane);
    
    createBottomToolbar();
  }

  private void createBottomToolbar()
  {
    bottomLabel = new Figure();
    FlowLayout fl = new FlowLayout();
    bottomLabel.setLayoutManager(fl);
    add(bottomLabel);
    
    Image backImg = VortexPlugin.getDefault().getImage("btn_background.png");
    Image backImgHigh = VortexPlugin.getDefault().getImage("btn_background_high.png");
    Image plusImg = VortexPlugin.getDefault().getImage("figure_plus.png");
    Image editImg = VortexPlugin.getDefault().getImage("figure_edit.png");
    
    plusButton = new BottomButtonFigure(plusImg, backImg, backImgHigh);
    plusButton.addActionListener(this);
    bottomLabel.add(plusButton);
    
    editButton = new BottomButtonFigure(editImg, backImg, backImgHigh);
    editButton.addActionListener(this);
    bottomLabel.add(editButton);
  }
  
  public void actionPerformed(ActionEvent event)
  {
    Object source = event.getSource();
    if(source == editButton)
      listeners.fireEvent(TableFigure.this, EVT_DROP_DOWN_MENU, null, null);
    else if(source == plusButton)
      listeners.fireEvent(TableFigure.this, EVT_PLUS_BTN_PRESSED, null, null);
  }
  
  /**
   * Gets the content pane for the figure. Note that this might break when we want to separate columns
   * and indexes in the table.
   * 
   * @return Figure for children content
   */
  public IFigure getContentPane()
  {
    return contentPane;
  }
  
  public void setSelected(boolean selected)
  {
    this.selected = selected;
  }
  
  public boolean isSelected()
  {
    return this.selected;
  }

  public void setName(String name)
  {
    nameLabel.setText(name);
  }

  /**
   * Gets the figure of the name label.
   * 
   * @return EditableFigure representing name label
   */
  public EditableFigure getLabelFigure()
  {
    return nameLabel;
  }
  
  public Dimension getPreferredSize(int wHint, int hHint)
  {
    return super.getPreferredSize(wHint, hHint);
  }

  protected void fillShape(Graphics g)
  {
    Rectangle r = getBounds().getCopy();
    r.expand(getInsets());
    ColorRegistry reg = DbEditorControllerImpl.getColorRegistry();
    
    g.pushState();
    try {
      Color from;
      Color to;
      if(selected) {
        from = reg.get(PreferenceConstants.PREF_DBED_COLOR_TABLEHEAD_RIGHT);
        to = reg.get(PreferenceConstants.PREF_DBED_COLOR_TABLEHEAD_LEFT);        
      }
      else {
        from = reg.get(PreferenceConstants.PREF_DBED_COLOR_TABLEHEAD_LEFT);
        to = reg.get(PreferenceConstants.PREF_DBED_COLOR_TABLEHEAD_RIGHT);
      }
      
      r.width -= 1;
      r.height -= 1;
      
      // FILL ROUND-SHAPED BACKGROUND ON THE TOP
      Rectangle topLabelClip = createTopLabelClip(r);
      int cornerSize = topLabelClip.height;
      int doubleCornerSize = cornerSize + cornerSize;
        
      // LEFT CORNER
      Rectangle rx = topLabelClip.getCopy();
      rx.width = cornerSize;
      g.setClip(rx);
      g.setBackgroundColor(from);
      g.fillOval(rx.x, rx.y, doubleCornerSize, doubleCornerSize);
      
      // RIGHT CORNER
      rx.x = r.x + r.width - cornerSize;
      g.setClip(rx);
      g.setBackgroundColor(to);
      g.fillOval(rx.x - cornerSize, rx.y, doubleCornerSize, doubleCornerSize);
      
      // MIDDLE MAN
      rx = new Rectangle(topLabelClip.x + cornerSize, topLabelClip.y,
                                        topLabelClip.width - doubleCornerSize,
                                        topLabelClip.height);
      g.setForegroundColor(from);
      g.setBackgroundColor(to);
      g.setClip(r);
      g.fillGradient(rx, false);
    
      // FILL BACKGROUND IN THE MIDDLE HEIGHT
      if(selected) {
        from = reg.get(PreferenceConstants.PREF_DBED_COLOR_TABLEBODY_RIGHT);
        to = reg.get(PreferenceConstants.PREF_DBED_COLOR_TABLEBODY_LEFT);        
      }
      else {
        from = reg.get(PreferenceConstants.PREF_DBED_COLOR_TABLEBODY_LEFT);
        to = reg.get(PreferenceConstants.PREF_DBED_COLOR_TABLEBODY_RIGHT);
      }
      
      Rectangle middleClip = createMiddleClip(r);
      
      rx = middleClip.getCopy();
      rx.width = cornerSize;
      g.setBackgroundColor(from);
      g.fillRectangle(rx);
      
      rx.x = r.x + r.width - cornerSize;
      g.setBackgroundColor(to);
      g.fillRectangle(rx);
      
      rx = middleClip.getCopy();
      rx.x += cornerSize;
      rx.width = rx.width - doubleCornerSize;
      g.setForegroundColor(from);
      g.setBackgroundColor(to);
      g.fillGradient(rx, false);
      
      // DRAW THE BOTTOM
      Rectangle bottomClip = createBottomClip(r);
      g.setForegroundColor(new Color(null, 253, 253, 253));
      g.setBackgroundColor(new Color(null, 234, 234, 234));
      g.fillGradient(bottomClip, true);
      
//      rx = bottomClip.getCopy();
//      rx.height = rx.height / 2;
//      g.setBackgroundColor(new Color(null, 255, 255, 255));
//      g.fillRectangle(rx);
//      
//      rx.y = rx.y + rx.height;
//      rx.height = bottomClip.height - rx.height;
//      g.setBackgroundColor(new Color(null, 255, 255, 255));
//      g.fillRectangle(rx);
    }
    finally {
      g.popState();
    }
  }

  protected void outlineShape(Graphics g)
  {
    Rectangle r = getBounds().getCopy();
    r.expand(getInsets());
    
    g.pushState();
    try {
      r.width -= 1;
      r.height -= 1;
      g.setLineWidth(1);
      
      // DRAW TOP BORDER
      Rectangle clipR = createTopLabelClip(r);
      int cornerSize = clipR.height;
      
      g.setClip(clipR);
      Rectangle rx = clipR.getCopy();
      rx.height = rx.height + rx.height;
      rx.width -= 1;
      g.drawRoundRectangle(rx, cornerSize * 2, cornerSize * 2);
      
      // DRAW BORDER AROUND THE MIDDLE
      clipR = createMiddleClip(r);
      g.setClip(clipR);
      g.drawRectangle(r);
      
      // DRAW THE BORDER AT THE BOTTOM
      //clipR = createBottomClip(r);
      //g.setForegroundColor(new Color(null, 201, 201, 201));
      //g.drawLine(clipR.x, clipR.y, clipR.x + clipR.width, clipR.y);
    }
    finally {
      g.popState();
    }
  }
  
  private Rectangle createBottomClip(Rectangle r)
  {
    Rectangle rx = r.getCopy();
    int h = bottomLabel.getBounds().height;
    rx.y = r.y + r.height - h;
    rx.height = h;
    rx.width += 1;
    return rx;
  }

  private Rectangle createMiddleClip(Rectangle r)
  {
    Rectangle clipR = r.getCopy();
    int labelHeight = nameLabel.getBounds().height;
    int bottomLabelHeight = bottomLabel.getBounds().height;
    clipR.y += labelHeight;
    clipR.height = clipR.height - labelHeight - bottomLabelHeight + 1;
    clipR.width += 1;
    return clipR;
  }

  private Rectangle createTopLabelClip(Rectangle r)
  {
    Rectangle clipR = r.getCopy();
    clipR.height = nameLabel.getBounds().height;
    clipR.width += 1;
    return clipR;
  }
  
  public Clickable getPlusButton()
  {
    return plusButton;
  }

  public Clickable getEditButton()
  {
    return editButton;
  }
  
  public void addBottomButtonsListener(PropertyChangeListener listener)
  {
    listeners.addListener(listener);
  }
  
  public void removeBottomButtonsListener(PropertyChangeListener listener)
  {
    listeners.removeListener(listener);
  }
}
