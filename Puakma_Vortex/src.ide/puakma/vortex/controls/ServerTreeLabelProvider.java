/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Nov 12, 2004
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.controls;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Provides labels for tree items.
 * 
 * @author Martin Novak
 */
public class ServerTreeLabelProvider extends LabelProvider
{

  public Image getImage(Object element)
  {
    return super.getImage(element);
  }

  public String getText(Object element)
  {
    if(element instanceof TreeObject) {
      TreeObject to = (TreeObject) element;
      return to.getName();
    }
    return super.getText(element);
  }
}
