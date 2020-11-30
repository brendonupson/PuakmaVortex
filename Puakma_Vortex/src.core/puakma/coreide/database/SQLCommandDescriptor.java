/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    11/05/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide.database;


public class SQLCommandDescriptor
{
	public String exceptionStackTrace;

	public String sql;

	/**
	 * Starting character of this command
	 */
	private int start;

	/**
	 * Ending character of this command
	 */
	private int end;

	public int result;

	public String exceptionMessage;

	public String sqlErrorState;

	public int sqlErrorCode;

	public String toString()
	{
		String str = sql + " [RESULT: result";
		if(exceptionStackTrace != null)
			str += sqlErrorState;
		str += "]";
		return str;
	}

	public int getStart()
	{
		return start;
	}

	public int getEnd()
	{
		return end;
	}

	public void setupPositions(int start, int end)
	{
		this.start = start;
		this.end = end;
	}

	public void setStart(int start)
	{
		this.start = start;
	}

	public void setEnd(int end)
	{
		this.end = end;
	}
}
