package org.perfmock;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import java.sql.SQLException;

/**
 * Delegates methods to wrapped {@link XAResource} or invokes them as no-ops.
 *
 * @author Radim Vansa &lt;rvansa@redhat.com&gt;
 */
public class PerfMockXaResource implements XAResource {
   protected static final Xid[] NO_XIDS = new Xid[0];

   private final XAResourceSupplier supplier;
   private volatile XAResource realXaResource = null;

   public PerfMockXaResource(XAResourceSupplier xaResourceSupplier) {
      this.supplier = xaResourceSupplier;
   }

   public void startMocking() {
      realXaResource = null;
   }

   public void stopMocking() throws SQLException {
      realXaResource = supplier.get();
   }

   @Override
   public void commit(Xid xid, boolean onePhase) throws XAException {
      XAResource xaResource = getRealXaResource();
      if (xaResource != null) {
         xaResource.commit(xid, onePhase);
      }
   }

   protected XAResource getRealXaResource() {
      return realXaResource;
   }

   @Override
   public void end(Xid xid, int flags) throws XAException {
      XAResource xaResource = getRealXaResource();
      if (xaResource != null) {
         xaResource.end(xid, flags);
      }
   }

   @Override
   public void forget(Xid xid) throws XAException {
      XAResource xaResource = getRealXaResource();
      if (xaResource != null) {
         xaResource.forget(xid);
      }
   }

   @Override
   public int getTransactionTimeout() throws XAException {
      XAResource realXaResource = getRealXaResource();
      if (realXaResource != null) {
         return realXaResource.getTransactionTimeout();
      } else {
         return 0;
      }
   }

   @Override
   public boolean isSameRM(XAResource otherXAResource) throws XAException {
      if (otherXAResource == this) {
         return true;
      }
      XAResource xaResource = getRealXaResource();
      return (xaResource != null && xaResource.isSameRM(otherXAResource));
   }

   @Override
   public int prepare(Xid xid) throws XAException {
      XAResource xaResource = getRealXaResource();
      if (xaResource != null) {
         return xaResource.prepare(xid);
      } else {
         return XA_OK;
      }
   }

   @Override
   public Xid[] recover(int flag) throws XAException {
      XAResource xaResource = getRealXaResource();
      if (xaResource != null) {
         return xaResource.recover(flag);
      } else {
         return NO_XIDS;
      }
   }

   @Override
   public void rollback(Xid xid) throws XAException {
      XAResource xaResource = getRealXaResource();
      if (xaResource != null) {
         xaResource.rollback(xid);
      }
   }

   @Override
   public boolean setTransactionTimeout(int timeout) throws XAException {
      XAResource xaResource = getRealXaResource();
      if (xaResource != null) {
         return xaResource.setTransactionTimeout(timeout);
      } else {
         return false;
      }
   }

   @Override
   public void start(Xid xid, int flags) throws XAException {
      XAResource xaResource = getRealXaResource();
      if (xaResource != null) {
         xaResource.start(xid, flags);
      }
   }
}
