/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Dec 17, 2005
 * 
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.utils;

import org.xml.sax.Attributes;

import puakma.coreide.designer.IElementHandler;

/**
 * Abstract handler for xml data which helps to handle xml with less code.
 *
 * @author Martin Novak
 */
public abstract class ElementAdapter implements IElementHandler
{
  public void startElement(String name, Attributes attributes)
  {
    // EMPTY
  }

  public void endElement(String name)
  {
    // EMPTY
  }
}
