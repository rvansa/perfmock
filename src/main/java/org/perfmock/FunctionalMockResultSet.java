package org.perfmock;

import com.mockrunner.mock.jdbc.MockResultSet;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.function.Supplier;

/**
 * Allows to specify {@link Supplier suppliers} as values - when the value is read,
 * the supplier is invoked.
 *
 * @author Radim Vansa &lt;rvansa@redhat.com&gt;
 */
public class FunctionalMockResultSet extends MockResultSet {
   private static final Field wasNull;

   static {
      try {
         wasNull = MockResultSet.class.getDeclaredField("wasNull");
         wasNull.setAccessible(true);
      } catch (NoSuchFieldException e) {
         throw new IllegalStateException("Field wasNull must be present", e);
      }
   }

   public FunctionalMockResultSet(String id)
   {
      super(id);
   }

   public FunctionalMockResultSet(String id, String cursorName)
   {
      super(id, cursorName);
   }

   @Override
   public Object getObject(String columnName) throws SQLException {
      Object value = super.getObject(columnName);
      if (value instanceof Supplier) {
         value = ((Supplier) value).get();
         if (value == null) {
            try {
               wasNull.set(this, true);
            } catch (IllegalAccessException e) {
               throw new IllegalStateException(e);
            }
         }
      }
      return value;
   }
}
