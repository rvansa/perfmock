# PerfMock

Library for mocking JDBC connections in benchmarks, with ability to dynamically switch between access to real DB
and calls mocked using [Mockrunner-JDBC](http://mockrunner.github.io/).

## Usage

1. Use URL in format `jdbc:perfmock:myDatabaseURL`, e.g. `jdbc:perfmock:h2:mem:testDB` - this creates
   a wrapped connection to `jdbc:h2:mem:testDB`.
2. Make sure that PerfMock is classloaded by calling `PerfMockDriver.getInstance();`
3. If you're using `javax.sql.DataSource` to access the connection, use class `org.perfmock.PerfMockDataSource`
   and add `realClass=my.db.DataSourceImplementation` to properties, e.g. `realClass=org.h2.jdbcx.JdbcDataSource`
4. Once you want to start mocking the calls to DB, call `PerfMockDriver.getInstance().setMocking(true);`

