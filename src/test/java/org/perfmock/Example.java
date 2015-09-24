package org.perfmock;

import com.mockrunner.jdbc.PreparedStatementResultSetHandler;
import com.mockrunner.mock.jdbc.MockResultSet;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.junit.Assert.assertEquals;

/**
 * Example test mocking Hibernate's communication to DB.
 *
 * @author Radim Vansa &lt;rvansa@redhat.com&gt;
 */
public class Example {
   // Make sure the driver is loaded & registered
   private static PerfMockDriver MOCK_DRIVER = PerfMockDriver.getInstance();

   // Configure mock responses
   static {
      PreparedStatementResultSetHandler handler = MOCK_DRIVER.getPreparedStatementHandler();

      MockResultSet callNextValue = handler.createResultSet();
      callNextValue.addRow(new Object[] { 1 });
      // If we want to remove distinct values for each call, we would have to use FunctionalMockResultSet.
      // However, at this point we cannot map the parameters to a statement to the returned value
      // (e.g. if we wanted to provide proper column in 'select x, y from ... where x = 42')
      handler.prepareResultSet("call next value for hibernate_sequence", callNextValue);

      handler.prepareUpdateCount("insert into ExampleEntity (text, id) values (?, ?)", 1);

      MockResultSet selectResult = handler.createResultSet();
      selectResult.addColumn("id1_0_0_", new Object[] { 1 });
      // note that we're inserting "foo" and then reading "bar" to verify that we're really mocking the call
      selectResult.addColumn("text2_0_0_", new Object[] { "bar" });
      handler.prepareResultSet("select exampleent0_.id as id1_0_0_, exampleent0_.text as text2_0_0_ from ExampleEntity exampleent0_ where exampleent0_.id=?", selectResult);
   }

   @Test
   public void test() {
      EntityManagerFactory factory = Persistence.createEntityManagerFactory("myPersistenceUnit");
      try {
         MOCK_DRIVER.setMocking(true);
         EntityManager em = factory.createEntityManager();
         try {
            em.getTransaction().begin();
            ExampleEntity entity1 = new ExampleEntity();
            entity1.setText("foo");
            em.persist(entity1);
            em.flush();
            // make sure we are using DB
            em.clear();
            ExampleEntity entity2 = em.find(ExampleEntity.class, entity1.getId());
            assertEquals("bar", entity2.getText());
            em.getTransaction().commit();
         } finally {
            em.close();
         }
      } finally {
         MOCK_DRIVER.setMocking(false);
         factory.close();
      }
   }
}
