package org.perfmock;

import javax.transaction.xa.XAResource;

import java.sql.SQLException;

/**
 * Creates {@link XAResource} on demand.
 *
 * @author Radim Vansa &lt;rvansa@redhat.com&gt;
 */
interface XAResourceSupplier {
   /**
    * @return New instance of XAResource
    * @throws SQLException
    */
   XAResource get() throws SQLException;
}
