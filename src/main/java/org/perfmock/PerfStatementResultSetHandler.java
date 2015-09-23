package org.perfmock;

import com.mockrunner.jdbc.StatementResultSetHandler;
import com.mockrunner.mock.jdbc.MockParameterMap;
import com.mockrunner.mock.jdbc.MockResultSet;
import com.mockrunner.mock.jdbc.MockStatement;

import java.util.Collections;
import java.util.List;

/**
 * Does not store any executed statements and returned results sets.
 *
 * @author Radim Vansa &lt;rvansa@redhat.com&gt;
 */
public class PerfStatementResultSetHandler extends StatementResultSetHandler  {
   @Override
   public void addStatement(MockStatement statement) {
      statement.setResultSetHandler(this);
   }

   @Override
   public List<MockStatement> getStatements() {
      return Collections.emptyList();
   }

   @Override
   public void clearStatements() {
   }

   @Override
   public void addExecutedStatement(String sql) {
   }

   @Override
   public void addReturnedResultSet(MockResultSet resultSet) {
   }

   @Override
   public void addReturnedResultSets(MockResultSet[] resultSets) {
   }
}
