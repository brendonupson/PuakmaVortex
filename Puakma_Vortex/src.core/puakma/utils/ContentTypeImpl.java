/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Date: Aug 15,
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

//import com.lowagie.text.pdf.codec.Base64.InputStream;

public class ContentTypeImpl implements ContentType
{
	private Map contentTypes = new HashMap();
	private Map extensions = new HashMap();

	static {
		initialize();
	}

	private static ContentType createContentType(String contentType, String[] extensions)
	{
		return null;
	}

	private static void initialize()
	{
		InputStream is = null;
		
		try {
			is = (InputStream) MimeTypesResolver.class.getClassLoader().getResourceAsStream("puakma/utils/mimetypes.config");
			if(is == null)
				throw new IllegalStateException("Library is corrupted - cannot read resource datas");
			Properties p = new Properties();
			p.load(is);
			Enumeration e = p.keys();
			while(e.hasMoreElements()) {
				String key = (String) e.nextElement();
				String mime = p.getProperty(key);
				//get rid of all comments, and these shits
				mime = mime.trim();
				int whitePos = mime.indexOf(' ');
				int tabPos   = mime.indexOf('\t');

				if(whitePos != -1 || tabPos != -1) {
					int pos = mime.length();
					if(whitePos != -1 && tabPos != -1)
						pos = Math.min(whitePos, tabPos);
					else if(whitePos == -1 && tabPos != -1)
						pos = tabPos;
					else if(whitePos != -1 && tabPos == -1)
						pos = whitePos;
					mime = mime.substring(0,pos);
				}


			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		finally {
			try { if(is != null) is.close(); }
			catch(IOException e) {  }
		}
	}

	@Override
	public String getContentType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getEncoding() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMainType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSubType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] listExtensions() {
		// TODO Auto-generated method stub
		return null;
	}
}
