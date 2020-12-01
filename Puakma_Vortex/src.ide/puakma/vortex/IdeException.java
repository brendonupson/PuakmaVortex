/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Jun 13, 2005
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

/**
 * This exception is the base exception for all vortex exceptions.
 *
 * @author Martin Novak
 */
public class IdeException extends Exception
{
	private static final long serialVersionUID = 5725015509409159466L;

	public IdeException()
	{
		super();
	}

	public IdeException(String message)
	{
		super(message);
	}

	public IdeException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public IdeException(Throwable cause)
	{
		super(cause);
	}
}
