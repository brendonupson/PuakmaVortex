/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    29.6.2004
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.coreide.jdbc;


import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * This class is used as hack to load jdbc drivers, because of problems with classloaders.
 * Implements Driver interface and has jdbc driver as variable to call it.
 * 
 * @author Martin Novak
 */
public class JdbcDriverLoderFake implements Driver
{
	private Driver driver;

	public JdbcDriverLoderFake(Driver d)
	{
		this.driver = d;
	}

	public boolean acceptsURL(String u) throws SQLException
	{
		return this.driver.acceptsURL(u);
	}

	public Connection connect(String u, Properties p) throws SQLException
	{
		return this.driver.connect(u, p);
	}

	public int getMajorVersion()
	{
		return this.driver.getMajorVersion();
	}

	public int getMinorVersion()
	{
		return this.driver.getMinorVersion();
	}

	public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) throws SQLException
	{
		return this.driver.getPropertyInfo(u, p);
	}

	public boolean jdbcCompliant()
	{
		return this.driver.jdbcCompliant();
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new SQLFeatureNotSupportedException();
	}
}
