/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    14/06/2006
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

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * This is faked {@link URLStreamHandler} class which does nothing, but throws
 * exception when you try to open connection.
 * 
 * @author Martin Novak
 */
public class NullStreamHandler extends URLStreamHandler
{
  protected URLConnection openConnection(URL u) throws IOException
  {
    throw new IOException("Not valid protocol for opening connection");
  }
}
