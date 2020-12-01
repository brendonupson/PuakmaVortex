/*
 * Author: Martin Novak
 * Date:   Nov 10, 2005
 */
package puakma.coreide;

import java.sql.Connection;
import java.sql.DriverManager;

public class MysqlUtils
{
  public static Connection createMysqlConnection() throws Exception
  {
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/junit", "junit", "gagarin");
    return connection;
  }
}
