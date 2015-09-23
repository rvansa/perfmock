package org.perfmock;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.PooledConnection;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Provides {@linke Connection connections} obtained from {@link PerfMockDriver#connect(String, Properties)}.
 * Providing {@link XAConnection} requires instantiating a real {@link XADataSource} as we cannot fully mock
 * {@link javax.transaction.xa.XAResource} when working with real connection. Note that this is instantiated
 * only lazily, we don't have to create the data source when the {@link javax.transaction.xa.XAResource}
 * is mocked (by no-op operations).
 * The underlying {@link XADataSource} is configured using {@link #setRealClass(String)} (usually called
 * via reflection, based on the data source properties).
 *
 * One limitation of this implementation is that we can't tunnel all properties configured to the underlying
 * data source.
 *
 * @author Radim Vansa &lt;rvansa@redhat.com&gt;
 */
public class PerfMockDataSource implements XADataSource, ConnectionPoolDataSource, DataSource {
   private String mockingUrl;
   private String url;
   private PrintWriter logWriter;
   private int loginTimeout;
   private String user = "";
   private String password = "";
   private String realClass;

   public String getUrl() {
      return url;
   }

   public void setUrl(String url) {
      this.url = PerfMockDriver.unwrapUrl(url);
      this.mockingUrl = url;
   }

   public String getUser() {
      return user;
   }

   public void setUser(String user) {
      this.user = user;
   }

   public String getPassword() {
      return password;
   }

   public void setPassword(String password) {
      this.password = password;
   }

   public String getRealClass() {
      return realClass;
   }

   public void setRealClass(String realClass) {
      this.realClass = realClass;
   }

   protected Properties getProperties() {
      Properties properties = new Properties();
      properties.put("user", user);
      properties.put("password", password);
      return properties;
   }

   protected Properties getProperties(String username, String password) {
      Properties properties = getProperties();
      properties.put("user", username);
      properties.put("password", password);
      return properties;
   }

   @Override
   public Connection getConnection() throws SQLException {
      return PerfMockDriver.getInstance().connect(mockingUrl, getProperties());
   }

   @Override
   public Connection getConnection(String username, String password) throws SQLException {
      Properties properties = getProperties(username, password);
      return PerfMockDriver.getInstance().connect(mockingUrl, properties);
   }

   @Override
   public PrintWriter getLogWriter() throws SQLException {
      return logWriter;
   }

   @Override
   public void setLogWriter(PrintWriter out) throws SQLException {
      logWriter = out;
   }

   @Override
   public void setLoginTimeout(int seconds) throws SQLException {
      loginTimeout = seconds;
   }

   @Override
   public int getLoginTimeout() throws SQLException {
      return loginTimeout;
   }

   @Override
   public Logger getParentLogger() throws SQLFeatureNotSupportedException {
      throw new SQLFeatureNotSupportedException();
   }

   @Override
   public <T> T unwrap(Class<T> iface) throws SQLException {
      throw new SQLException("Interface not wrapped: " + iface);
   }

   @Override
   public boolean isWrapperFor(Class<?> iface) throws SQLException {
      return false;
   }

   @Override
   public PooledConnection getPooledConnection() throws SQLException {
      return getXAConnection();
   }

   @Override
   public PooledConnection getPooledConnection(String user, String password) throws SQLException {
      return getXAConnection(user, password);
   }

   @Override
   public XAConnection getXAConnection() throws SQLException {
      return new PerfMockXAConnection(PerfMockDriver.getInstance().connect(mockingUrl, getProperties()), () -> {
         XADataSource realXADataSource = getRealXADataSource();
         return realXADataSource != null ? realXADataSource.getXAConnection().getXAResource() : null;
      });
   }

   @Override
   public XAConnection getXAConnection(String user, String password) throws SQLException {
      return new PerfMockXAConnection(PerfMockDriver.getInstance().connect(mockingUrl, getProperties(user, password)), () -> {
         XADataSource realXADataSource = getRealXADataSource();
         return realXADataSource != null ? realXADataSource.getXAConnection(user, password).getXAResource() : null;
      });
   }

   protected XADataSource getRealXADataSource() {
      try {
         Class<?> realClass = Class.forName(this.realClass);
         if (!XADataSource.class.isAssignableFrom(realClass)) {
            return null;
         }
         XADataSource dataSource = (XADataSource) realClass.newInstance();
         // copy all data shared using getters/setter to the real datasource
         for (Method setter : dataSource.getClass().getMethods()) {
            if (!setter.getName().startsWith("set") || setter.getParameterCount() != 1) {
               continue;
            }
            Method getter;
            try {
               getter = PerfMockDataSource.class.getMethod("get" + setter.getName().substring(3));
            } catch (NoSuchMethodException e) {
               continue;
            }
            try {
               if (setter.getParameters()[0].getType().isAssignableFrom(getter.getReturnType())) {
                  setter.invoke(dataSource, getter.invoke(this));
               }
            } catch (InvocationTargetException e) {
               throw new RuntimeException("Failed to copy from " + getter.getName() + " to " + setter.getName(), e);
            }
         }
         return dataSource;
      } catch (Exception e) {
         throw new RuntimeException("Failed to create datasource " + realClass, e);
      }
   }

}
