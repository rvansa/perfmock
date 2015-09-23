package org.perfmock;

import com.mockrunner.jdbc.CallableStatementResultSetHandler;
import com.mockrunner.mock.jdbc.MockCallableStatement;
import com.mockrunner.mock.jdbc.MockParameterMap;
import com.mockrunner.mock.jdbc.MockResultSet;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Does not store any executed statements and returned results sets.
 *
 * @author Radim Vansa &lt;rvansa@redhat.com&gt;
 */
public class PerfCallableStatementResultSetHandler extends CallableStatementResultSetHandler {
   @Override
   public void addCallableStatement(MockCallableStatement statement) {
      statement.setCallableStatementResultSetHandler(this);
   }

   @Override
   public List<MockCallableStatement> getCallableStatements() {
      return Collections.emptyList();
   }

   @Override
   public Map<String, List<MockCallableStatement>> getCallableStatementMap() {
      return Collections.emptyMap();
   }

   @Override
   public void clearCallableStatements() {
   }

   @Override
   public void addExecutedStatement(String sql) {
   }

   @Override
   public void addParameterMapForExecutedStatement(String sql, MockParameterMap parameters) {
   }

   @Override
   public void addReturnedResultSet(MockResultSet resultSet) {
   }

   @Override
   public void addReturnedResultSets(MockResultSet[] resultSets) {
   }
}
