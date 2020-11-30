/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 14, 2006
 * 
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.editors.dbeditor.directedit;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import puakma.vortex.editors.dbeditor.figures.EditableFigure;

public class DbSchemaDirectEditManager extends DirectEditManager
{
  private EditableFigure label;
  private String originalValue;
  private ICellEditorValidator validator;
  private Font figureFont;
  private String lastValue;
  private boolean commiting = false;
  private String textToEdit;

  public DbSchemaDirectEditManager(GraphicalEditPart source, Class<?> editorType,
                                   EditableFigure label, ICellEditorValidator validator)
  {
    super(source, editorType, label);
    
    this.label = label;
    this.originalValue = textToEdit;
    this.validator = validator;
  }
  
  /**
   * Sets the current value which will be displayed in the cell editor. 
   */
  public void setTextToEdit(String textToEdit)
  {
    this.textToEdit = textToEdit;
  }
  
  protected void bringDown()
  {
    if(figureFont != null) {
      figureFont.dispose();
      figureFont = null;
    }
    if(commiting == false)
      label.setText(originalValue);
    super.bringDown();
  }

  protected void initCellEditor()
  {
    Text text = (Text) getCellEditor().getControl();
    
    // GET THE ORIGINAL TEXT FROM FIGURE AND SET IT AS EDITOR TEXT
    originalValue = textToEdit;
    getCellEditor().setValue(originalValue);
    lastValue = originalValue;
    
    // ADD LISTENER TO HANDLE SIZING OF THE TEXT CELL
    VerifyListener verifyListener = new VerifyListener() {
      public void verifyText(VerifyEvent event)
      {
        Text text = (Text) getCellEditor().getControl();
        String oldText = text.getText();
        String leftText = oldText.substring(0, event.start);
        String rightText = oldText.substring(event.end, oldText.length());
        GC gc = new GC(text);
        if(leftText == null)
          leftText = "";
        if(rightText == null)
          rightText = "";

        String newText = leftText + event.text + rightText;
        Point size = gc.textExtent(newText);

        gc.dispose();
        if(size.x != 0)
          size = text.computeSize(size.x, SWT.DEFAULT);
        else {
          // just make it square
          size.x = size.y;
        }
        text.setSize(size.x, size.y);
        
        lastValue = newText;
      }
    };
    text.addVerifyListener(verifyListener);
    
    // CALCULATE FIGURE FONT SIZE
    IFigure figure = getEditPart().getFigure();
    figureFont = figure.getFont();
    FontData data = figureFont.getFontData()[0];
    Dimension fontSize = new Dimension(0, data.getHeight());
    
    label.translateToAbsolute(fontSize);
    data.setHeight(fontSize.height);
    figureFont = new Font(null, data);
    
    // SETUP VALIDATION
    getCellEditor().setValidator(validator);
    
    text.setFont(figureFont);
    text.selectAll();
  }

  protected void commit()
  {
    if(commiting)
      return;
    commiting = true;
    
    // ASK VALIDATOR IF THE VALUE IS VALID OR NOT
    boolean doCommit = true;
    if(validator != null) {
      if(lastValue.equals(originalValue) == false) {
        String msg = validator.isValid(lastValue);
        doCommit = msg == null;
        // DISPLAY THE MESSAGE
        if(doCommit == false) {
          Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
          MessageDialog.openError(shell, "Invalid value", msg);
        }
      }
      else
        doCommit = false;
    }
    
    if(doCommit)
      super.commit();
    else {
      eraseFeedback();
      bringDown();
      label.setText(originalValue);
    }
    
    getEditPart().refresh();
    commiting  = false;
  }
  
  
}
