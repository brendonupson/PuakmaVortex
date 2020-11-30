/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    02/07/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.swt;

import java.util.Date;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import puakma.coreide.objects2.DesignObject;
import puakma.utils.lang.StringUtil;

import com.ibm.icu.text.DateFormat;

/**
 * This panel shows design object properties. 
 * 
 * @author Martin Novak
 */
public class DesignObjectPanel
{
  private boolean useForms;
  private Label nameLabel;
  private Label idLabel;
  private Label authorLabel;
  private Label dateLabel;
  private DesignObject designObject;
  private Composite composite;
  
  public void setUseFormsFramework(boolean useForms)
  {
    this.useForms = useForms;
  }
  
  public void create(Composite parent)
  {
    DialogBuilder2 builder = new DialogBuilder2(parent);
    
    composite = builder.createComposite(4);
    
    nameLabel = builder.createTwoLabelRow("Name:", "");
    idLabel = builder.createTwoLabelRow("Id:", "");
    authorLabel = builder.createTwoLabelRow("Author:", "");
    dateLabel = builder.createTwoLabelRow("Date:", "");
    
    builder.closeComposite();
    
    builder.finishBuilder();
  }

  /**
   * Refreshes all labels from {@link DesignObject}.
   */
  public void refresh(DesignObject designObject)
  {
    this.designObject = designObject;
    
    nameLabel.setText(StringUtil.safeString(designObject.getName()));
    idLabel.setText(Long.toString(designObject.getId()));
    authorLabel.setText(StringUtil.safeString(designObject.getUpdatedByUser()));
    Date date = designObject.getLastUpdateTime();
    DateFormat fmt = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    String dateStr = fmt.format(date);
    dateLabel.setText(dateStr);
  }
  
  public Composite getComposite()
  {
    return composite;
  }
}
