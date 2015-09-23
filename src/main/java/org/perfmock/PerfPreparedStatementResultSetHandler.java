package org.perfmock;

import com.mockrunner.jdbc.PreparedStatementResultSetHandler;
import com.mockrunner.mock.jdbc.MockParameterMap;
import com.mockrunner.mock.jdbc.MockPreparedStatement;
import com.mockrunner.mock.jdbc.MockResultSet;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Does not store any executed statements and returned results sets.
 *
 * @author Radim Vansa &lt;rvansa@redhat.com&gt;
 */
public class PerfPreparedStatementResultSetHandler extends PreparedStatementResultSetHandler {
   @Override
   public void addPreparedStatement(MockPreparedStatement statement) {
      statement.setPreparedStatementResultSetHandler(this);
   }

   @Override
   public List<MockPreparedStatement> getPreparedStatements() {
      return Collections.emptyList();
   }

   @Override
   public Map<String, List<MockPreparedStatement>> getPreparedStatementMap() {
      return Collections.emptyMap();
   }

   @Override
   public void clearPreparedStatements() {
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
