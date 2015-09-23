package org.perfmock;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.StatementEventListener;
import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * // TODO: Document this
 *
 * @author Radim Vansa &lt;rvansa@redhat.com&gt;
 */
public class PerfMockXAConnection implements XAConnection {
   private final ArrayList<ConnectionEventListener> listeners = new ArrayList<>();
   private final Connection connection;
   private final PerfMockXaResource xaResource;

   public PerfMockXAConnection(Connection connection, XAResourceSupplier xaResourceSupplier) throws SQLException {
      this.connection = connection;
      this.xaResource = new PerfMockXaResource(xaResourceSupplier);
      PerfMockDriver.getInstance().registerXaResource(xaResource);
   }

   @Override
   public XAResource getXAResource() throws SQLException {
      return xaResource;
   }

   @Override
   public Connection getConnection() throws SQLException {
      return new Handle(connection);
   }

   @Override
   public void close() throws SQLException {
      PerfMockDriver.getInstance().unregisterXaResource(xaResource);
      connection.close();
   }

   @Override
   public void addConnectionEventListener(ConnectionEventListener listener) {
      listeners.add(listener);
   }

   @Override
   public void removeConnectionEventListener(ConnectionEventListener listener) {
      listeners.remove(listener);
   }

   private void closedHandle() {
      for (ConnectionEventListener listener : listeners) {
         listener.connectionClosed(new ConnectionEvent(this));
      }
   }

   @Override
   public void addStatementEventListener(StatementEventListener listener) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void removeStatementEventListener(StatementEventListener listener) {
      throw new UnsupportedOperationException();
   }

   private class Handle extends PerfMockConnection {
      private boolean closed = false;

      public Handle(Connection connection) {
         super(null, null, connection);
      }

      @Override
      public boolean isClosed() throws SQLException {
         return closed || super.isClosed();
      }

      @Override
      public void close() throws SQLException {
         if (!closed) {
            rollback();
            setAutoCommit(true);
            closedHandle();
            closed = true;
         }
      }
   }
}
