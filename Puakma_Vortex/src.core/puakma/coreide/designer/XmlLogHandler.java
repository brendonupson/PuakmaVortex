/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    12-abr-2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide.designer;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;

import puakma.coreide.ConsoleLogItem;
import puakma.utils.ElementAdapter;
import puakma.utils.PmaXmlHandler;
import puakma.utils.lang.StringUtil;

public class XmlLogHandler  extends PmaXmlHandler
{
  private static final String[] elements = {
    PuakmaXmlCodes.ELEM_LOG_ITEM
  };
  private final IElementHandler[] handlers = {
      new LogItemHandler(),
  };
  
  private List<ConsoleLogItem> logItems = new ArrayList<ConsoleLogItem>();
  private ConsoleLogItem item;
  
  private class LogItemHandler extends ElementAdapter {
    public void startElement(String name, Attributes attributes)
    {
      item = new ConsoleLogItem();
      long date = StringUtil.parseLong(attributes.getValue(PuakmaXmlCodes.ATT_DATE), 0);
      item.setDate(date);
      item.setId(StringUtil.parseLong(attributes.getValue(PuakmaXmlCodes.ATT_ID), -1));
      item.setItemSource(attributes.getValue(PuakmaXmlCodes.ATT_ITEM_SOURCE));
      item.setServerName(attributes.getValue(PuakmaXmlCodes.ATT_SERVER_NAME));
      int type;
      String tmp = attributes.getValue(PuakmaXmlCodes.ATT_TYPE);
      char c = tmp == null || tmp.length() == 0 ? 'I' : tmp.charAt(0);
      switch(c) {
        case 'I': case 'i': type = ConsoleLogItem.TYPE_INFO; break;
        case 'E': case 'e': type = ConsoleLogItem.TYPE_ERROR; break;
        case 'W': case 'w': type = ConsoleLogItem.tYPE_WARNING; break;
        default: type = ConsoleLogItem.TYPE_ERROR;
      }
      item.setType(type);
      item.setUserName(attributes.getValue(PuakmaXmlCodes.ATT_USERNAME));
      logItems.add(item);
    }

    public void endElement(String name)
    {
      item.setMessage(contextBuffer.toString());
    }
  }

  protected String[] getElements()
  {
    return elements;
  }

  protected IElementHandler[] getHandlers()
  {
    return handlers;
  }

  public ConsoleLogItem[] getLogItems()
  {
    return logItems.toArray(new ConsoleLogItem[logItems.size()]);
  }
}
