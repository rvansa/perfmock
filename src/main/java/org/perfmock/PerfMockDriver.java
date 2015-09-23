
package org.perfmock;

import com.mockrunner.jdbc.CallableStatementResultSetHandler;
import com.mockrunner.jdbc.PreparedStatementResultSetHandler;
import com.mockrunner.jdbc.StatementResultSetHandler;
import com.mockrunner.mock.jdbc.MockConnection;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Implementation of the driver, singleton instance. Keeps reference to all open connections.
 * The connections are not mocked until you call {@link #setMocking(boolean)}
 *
 * @author Radim Vansa &lt;rvansa@redhat.com&gt;
 */
public class PerfMockDriver implements Driver {
   public static final String JDBC_URL_PREFIX = "jdbc:";
   public static final String MOCK_URL_PREFIX = "jdbc:perfmock:";

   private static final PerfMockDriver INSTANCE = new PerfMockDriver();

   static {
      try {
         DriverManager.registerDriver(INSTANCE);
      } catch (SQLException e) {
         throw new RuntimeException("Failed to register driver", e);
      }
   }

   private PerfStatementResultSetHandler statementHandler = new PerfStatementResultSetHandler();;
   private PerfPreparedStatementResultSetHandler preparedStatementHandler = new PerfPreparedStatementResultSetHandler();
   private PerfCallableStatementResultSetHandler callableStatementHandler = new PerfCallableStatementResultSetHandler();
   private Set<PerfMockConnection> connections = Collections.newSetFromMap(new ConcurrentHashMap<>());
   private Set<PerfMockXaResource> xaResources = Collections.newSetFromMap(new ConcurrentHashMap<>());
   private boolean mocking;

   /**
    * @return Singleton instance of this driver.
    */
   public static PerfMockDriver getInstance() {
      return INSTANCE;
   }

   static String unwrapUrl(String url) {
      return JDBC_URL_PREFIX + url.substring(MOCK_URL_PREFIX.length());
   }

   private PerfMockDriver() {}

   /**
    * Set whether the calls are invoked on real connection to DB or to mocked connection returning
    * pre-set results.
    *
    * @param mocking False for real DB, true for mock connection.
    */
   public void setMocking(boolean mocking) {
      for (Iterator<PerfMockConnection> iterator = connections.iterator(); iterator.hasNext(); ) {
         PerfMockConnection c = iterator.next();
         try {
            if (c.isClosed()) {
               iterator.remove();
            }
            Connection connection = getConnection(mocking, c.getUrl(), c.getInfo());
            if (connection == null) {
               c.close();
               iterator.remove();
            }
            c.setConnection(connection);
         } catch (SQLException e) {
            e.printStackTrace();
            iterator.remove();
         }
      }
      for (PerfMockXaResource xaResource : xaResources) {
         if (mocking) {
            xaResource.startMocking();
         } else {
            try {
               xaResource.stopMocking();
            } catch (SQLException e) {
               e.printStackTrace();
            }
         }
      }
      this.mocking = mocking;
   }

   /**
    * See {@link #setMocking(boolean)}
    *
    * @return True if the connection is mocked.
    */
   public boolean isMocking() {
      return mocking;
   }

   /**
    * @return Handler for non-prepared statements. See {@link Connection#createStatement()}
    */
   public StatementResultSetHandler getStatementHandler() {
      return statementHandler;
   }

   /**
    * @return Handler for prepared statements. See {@link Connection#prepareStatement(String)}
    */
   public PreparedStatementResultSetHandler getPreparedStatementHandler() {
      return preparedStatementHandler;
   }

   /**
    * @return Handler for callable statements. See {@link Connection#prepareCall(String)}
    */
   public CallableStatementResultSetHandler getCallableStatementHandler() {
      return callableStatementHandler;
   }

   public Connection connect(String url, Properties info) throws SQLException {
      if (url == null) {
         throw new SQLException("URL must not be null");
      }
      if (url.startsWith(MOCK_URL_PREFIX)) {
         String unwrappedUrl = unwrapUrl(url);
         Connection connection = getConnection(mocking, unwrappedUrl, info);
         if (connection == null) {
            return null;
         }
         PerfMockConnection hsc = new PerfMockConnection(unwrappedUrl, info, connection);
         connections.add(hsc);
         return hsc;
      }
      return null;
   }

   public boolean acceptsURL(String url) throws SQLException {
      return url.startsWith(MOCK_URL_PREFIX);
   }

   public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
      String unwrappedUrl = unwrapUrl(url);
      Driver realDriver = DriverManager.getDriver(unwrappedUrl);
      return realDriver.getPropertyInfo(unwrappedUrl, info);
   }

   public int getMajorVersion() {
      return 1;
   }

   public int getMinorVersion() {
      return 0;
   }

   public boolean jdbcCompliant() {
      return true;
   }

   public Logger getParentLogger() throws SQLFeatureNotSupportedException {
      throw new SQLFeatureNotSupportedException();
   }

   private Connection getConnection(boolean mocking, String url, Properties info) throws SQLException {
      Connection connection;
      if (mocking) {
         connection = new MockConnection(statementHandler, preparedStatementHandler, callableStatementHandler);
      } else {
         connection = DriverManager.getConnection(url, info);
      }
      return connection;
   }

   void registerXaResource(PerfMockXaResource xaResource) throws SQLException {
      xaResources.add(xaResource);
      if (mocking) {
         xaResource.startMocking();
      } else {
         xaResource.stopMocking();
      }
   }

   void unregisterXaResource(PerfMockXaResource xaResource) {
      xaResources.remove(xaResource);
   }

   /**
    * Called when the mock connection is closed.
    *
    * @param connection
    */
   void releaseConnection(PerfMockConnection connection) {
      connections.remove(this);
   }
}
