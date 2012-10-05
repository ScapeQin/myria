/**
 * 
 */
package edu.washington.escience.myriad.accessmethod;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.washington.escience.myriad.Schema;
import edu.washington.escience.myriad.TupleBatch;
import edu.washington.escience.myriad.Type;
import edu.washington.escience.myriad.parallel.DbException;
import edu.washington.escience.myriad.parallel.JdbcQueryScan;

/**
 * @author dhalperi
 * 
 */
public class JdbcAccessMethodTest {

  @Test
  public void test() throws DbException {
    /* Connection information */
    final String host = "54.245.108.198";
    final int port = 3306;
    final String user = "myriad";
    final String password = "nays26[shark";
    final String dbms = "mysql";
    final String databaseName = "myriad_test";
    final String jdbcDriverName = "com.mysql.jdbc.Driver";
    final int expectedNumResults = 250; /* Hardcoded in setup_testtablebig.sql */
    /* Query information */
    final String query = "select * from testtablebig";
    final Type[] types = new Type[] { Type.INT_TYPE };
    String[] columnNames = new String[] { "value" };
    Schema schema = new Schema(types, columnNames);

    /* Build up the QueryScan parameters and open the scan */
    String connectionString =
        "jdbc:" + dbms + "://" + host + ":" + port + "/" + databaseName + "?user=" + user + "&password=" + password;
    JdbcQueryScan scan = new JdbcQueryScan(jdbcDriverName, connectionString, query, schema,"","");

    scan.open();

    /* Count up the results and assert they match expectations */
    int count = 0;
    while (scan.hasNext()) {
      TupleBatch tb = (TupleBatch) scan.next();
      count += tb.numValidTuples();
    }
    assertTrue(count == expectedNumResults);

    /* Cleanup */
    scan.close();
  }

}