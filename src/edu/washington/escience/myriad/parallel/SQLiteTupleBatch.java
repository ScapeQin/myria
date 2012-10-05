package edu.washington.escience.myriad.parallel;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import edu.washington.escience.myriad.Predicate;
import edu.washington.escience.myriad.Schema;
import edu.washington.escience.myriad.TupleBatch;
import edu.washington.escience.myriad.TupleBatchBuffer;
import edu.washington.escience.myriad.Type;
import edu.washington.escience.myriad.accessmethod.SQLiteAccessMethod;
import edu.washington.escience.myriad.column.Column;
import edu.washington.escience.myriad.table._TupleBatch;

// Not yet @ThreadSafe
public class SQLiteTupleBatch implements _TupleBatch {

  private static final long serialVersionUID = 1L;

  public static final int BATCH_SIZE = 100;

  private final Schema inputSchema;
  private transient String dataDir;
  private final String filename;
  private int numInputTuples;
  private final String tableName;

  public SQLiteTupleBatch(Schema inputSchema, String filename, String tableName) {
    this.inputSchema = Objects.requireNonNull(inputSchema);
    this.filename = filename;
    this.tableName = tableName;
  }

  @Override
  public synchronized SQLiteTupleBatch filter(int fieldIdx, Predicate.Op op, Object operand) {
    return this;
  }

  @Override
  public synchronized boolean getBoolean(int column, int row) {
    return false;
  }

  @Override
  public synchronized double getDouble(int column, int row) {
    return 0d;
  }

  @Override
  public synchronized float getFloat(int column, int row) {
    return 0f;
  }

  @Override
  public synchronized int getInt(int column, int row) {
    return 0;
  }

  @Override
  public synchronized long getLong(int column, int row) {
    return 0;
  }

  @Override
  public Schema inputSchema() {
    return inputSchema;
  }

  @Override
  public synchronized String getString(int column, int row) {
    return null;
  }

  @Override
  public synchronized int numInputTuples() {
    return numInputTuples;
  }

  public synchronized SQLiteTupleBatch[] partition(PartitionFunction<?, ?> p) {
    return null;
  }

  @Override
  public synchronized SQLiteTupleBatch project(int[] remainingColumns) {
    return this;
  }

  protected synchronized int[] outputColumnIndices() {
    int numInputColumns = this.inputSchema.numFields();
    int[] validC = new int[numInputColumns];
    int j = 0;
    for (int i = 0; i < numInputColumns; i++) {
      // operate on index i here
      validC[j++] = i;
    }
    return validC;
  }

  @Override
  public synchronized Schema outputSchema() {

    int[] columnIndices = this.outputColumnIndices();
    String[] columnNames = new String[columnIndices.length];
    Type[] columnTypes = new Type[columnIndices.length];
    int j = 0;
    for (int columnIndx : columnIndices) {
      columnNames[j] = this.inputSchema.getFieldName(columnIndx);
      columnTypes[j] = this.inputSchema.getFieldType(columnIndx);
      j++;
    }

    return new Schema(columnTypes, columnNames);
  }

  @Override
  public synchronized int numOutputTuples() {
    return this.numInputTuples;
  }

  @Override
  public synchronized _TupleBatch renameColumn(int inputColumnIdx, String newName) {
    return this;
  }

  @Override
  public synchronized _TupleBatch purgeFilters() {
    return this;
  }

  @Override
  public synchronized _TupleBatch purgeProjects() {
    return this;
  }

  @Override
  public synchronized _TupleBatch append(_TupleBatch another) {
    Iterator<Schema.TDItem> it = this.inputSchema.iterator();

    String[] fieldNames = new String[this.inputSchema.numFields()];
    String[] placeHolders = new String[this.inputSchema.numFields()];
    int i = 0;
    while (it.hasNext()) {
      Schema.TDItem item = it.next();
      placeHolders[i] = "?";
      fieldNames[i++] = item.getName();
    }

    SQLiteAccessMethod.tupleBatchInsert(this.dataDir + "/" + this.filename, "insert into " + this.tableName + " ( "
        + StringUtils.join(fieldNames, ',') + " ) values ( " + StringUtils.join(placeHolders, ',') + " )",
        new TupleBatch(another.outputSchema(), another.outputRawData(), another.numOutputTuples()));
    return this;
  }

  @Override
  public synchronized _TupleBatch union(_TupleBatch another) {
    return null;
  }

  @Override
  public synchronized _TupleBatch intersect(_TupleBatch another) {
    return null;
  }

  @Override
  public synchronized _TupleBatch except(_TupleBatch another) {
    return null;
  }

  @Override
  public synchronized _TupleBatch distinct() {
    return null;
  }

  @Override
  public synchronized _TupleBatch groupby() {
    return null;
  }

  @Override
  public synchronized _TupleBatch orderby() {
    return null;
  }

  @Override
  public synchronized _TupleBatch join(_TupleBatch other, Predicate p, _TupleBatch output) {
    return null;
  }

  @Override
  public List<Column> outputRawData() {
    return null;
  }

  @Override
  public TupleBatchBuffer[] partition(PartitionFunction<?, ?> p, TupleBatchBuffer[] buffers) {
    return null;
  }

  public void reset(String dataDir) {
    this.dataDir = dataDir;
  }

  @Override
  public _TupleBatch remove(int innerIdx) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int hashCode(int rowIndx) {
    throw new UnsupportedOperationException();
  }
}