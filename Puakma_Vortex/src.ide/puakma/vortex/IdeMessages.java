/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Aug 9, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex;

import org.eclipse.osgi.util.NLS;

public class IdeMessages extends NLS
{
  private static final String RESOURCE_BUNDLE = "puakma.vortex.messages";
  
  static {
    NLS.initializeMessages(RESOURCE_BUNDLE, IdeMessages.class);
  }

  public static String ApplicationEditor_MainPage_Title;
  public static String ApplicationEditor_SecPage_Title;
  public static String ApplicationEditor_KwPage_Title;
  public static String ApplicationEditor_DbPage_Title;
  public static String ApplicationEditor_Title;
}
