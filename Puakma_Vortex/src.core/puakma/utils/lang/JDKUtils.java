/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    10/06/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.utils.lang;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JDKUtils
{
	private static final String expression = "(\\d+).(\\d+).(\\d+)(_(\\d*)){0,1}";
	private static Pattern pattern = Pattern.compile(expression);

	public static final int JDK_UNKNOWN = 0x0;
	public static final int JDK_VERSION_1_4_0 = 0x140;
	public static final int JDK_VERSION_1_4_1 = 0x141;
	public static final int JDK_VERSION_1_4_2 = 0x142;
	public static final int JDK_VERSION_1_5_0 = 0x150;
	public static final int JDK_VERSION_1_6_0 = 0x160;

	public static int guessJdk(String jdkVersionString)
	{
		Matcher m = pattern.matcher(jdkVersionString);
		int imax;
		int imed;
		int imin = 0;
		if(m.find()) {
			imax = Integer.parseInt(m.group(1));
			imed = Integer.parseInt(m.group(2));

			if(m.group(3) != null)
				imin = Integer.parseInt(m.group(3));
		}
		else
			return JDK_UNKNOWN;

		int ret = ((imax << 8) & 0xF00) | ((imed << 4) & 0x0F0) | (imin & 0x00F);
		return ret;
	}

	public static int maxVersion(int rev)
	{
		return (rev & 0xF00) >> 8;
	}

	public static int medVersion(int rev)
	{
		return (rev & 0xF0) >> 4;
	}

	public static int minVersion(int rev)
	{
		return rev & 0xF;
	}

	public static int normalizeToMainReleases(int ver)
	{
		return ver & 0xFF0;
	}
}
