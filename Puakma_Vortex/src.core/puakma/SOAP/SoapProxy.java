/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    01.06.2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.SOAP;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * This class much simplifies calling web services in Puakma Tornado. You simply
 * define interface which methods will be the exactly same as you define them on
 * the server with the exception that they have to throw {@link IOException},
 * and {@link SOAPFaultException} exceptions.
 * 
 * <b>Usage</b>
 * 
 * <p>
 * Define this interface in your client code:
 * 
 * <pre>
 * public interface TestInterface
 * {
 *   public void funtion() throws IOException, SOAPFaultException;
 * }</pre>
 * 
 * <p>
 * Then create widget on Puakma Tornado server:
 * 
 * <pre>
 * public class TestWidget extends BusinessWidget
 * {
 *   public void function() {
 *     // DO SOMETHING HERE...
 *   }
 * }</pre>
 * 
 * <p>
 * And when you want to invoke the widget, create this code:
 * 
 * <pre>
 * TestInterface client = (TestInterface) SoapProxy.createSoapClient(TestInterface.class,
 *                                                                   &quot;http://server/app.pma/widget&quot;,
 *                                                                   &quot;user&quot;, &quot;pwd&quot;);
 * client.function();</pre>
 * 
 * @author Martin Novak
 */
public class SoapProxy extends SOAPClient implements InvocationHandler
{
	private SoapProxy(String url, String userName, String pwd)
	{
		super();

		setURL(url);
		setUserNamePassword(userName, pwd);
	}

	/**
	 * Creates a new SOAP proxy client. This method much simplifies creating
	 * clients for soap, because you don't need to code anything in the call
	 * anymore. All necesarry work is done for you by the proxy.
	 * 
	 * <p>
	 * Here you should specify what interface will you use as a definition of the
	 * soap calls. Note also that all interface methods needs to throw
	 * {@link IOException}, and {@link SOAPFaultException}. If this is not
	 * satisfied, when you try to invoke the appropriate function, it throws some
	 * runtime exception. Note: this might change since we can move checking here.
	 * 
	 * @param clz is the interface we want to use as a definition for call
	 * @param url is url of SOAP widget
	 * @param userName is user name for Tornado server
	 * @param pwd is password for user
	 * @return instance of your interface
	 */
	public static final Object createSoapClient(Class clz, String url, String userName,
			String pwd)
	{
		SoapProxy handler = new SoapProxy(url, userName, pwd);
		Object ret = Proxy.newProxyInstance(clz.getClassLoader(), new Class[] { clz },
				handler);
		return ret;
	}

	public synchronized Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		// CHECK DECLARED EXCEPTION AT FIRST
		boolean haveSoapFaultException = false;
		boolean haveIOException = false;
		Class[] exceptions = method.getExceptionTypes();
		for(int i = 0; i < exceptions.length; ++i) {
			if(exceptions[i] == IOException.class)
				haveIOException = true;
			else if(exceptions[i] == SOAPFaultException.class)
				haveSoapFaultException = true;
		}
		if(haveIOException == false)
			throw new IllegalStateException("Called function have to throw IOException");
		else if(haveSoapFaultException == false)
			throw new IllegalStateException("Called function have to throw SOAPFaultException");

		clearParameters();
		setMethod(method.getName());
		if(args != null) {
			for(int i = 0; i < args.length; ++i) {
				// TODO: should we check parameter type here to not include any complex
				// types?
				addParameter(args[i]);
			}
		}

		return execute();
	}
}
