package edu.washington.escience.myria.column.mutable;

import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
import com.google.common.base.Preconditions;
import com.google.common.hash.Hasher;
import com.google.protobuf.ByteString;

import edu.washington.escience.myria.TupleBatch;
import edu.washington.escience.myria.Type;
import edu.washington.escience.myria.column.Column;
import edu.washington.escience.myria.column.DoubleColumn;
import edu.washington.escience.myria.column.builder.ColumnBuilder;
import edu.washington.escience.myria.column.builder.DoubleColumnBuilder;
import edu.washington.escience.myria.proto.DataProto.ColumnMessage;
import edu.washington.escience.myria.proto.DataProto.DoubleColumnMessage;
import edu.washington.escience.myria.util.ImmutableIntArray;

/**
 * A mutable column of Double values.
 * 
 */
public final class DoubleMutableColumn implements MutableColumn<Double> {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /** Internal representation of the column data. */
  private final double[] data;
  /** The number of existing rows in this column. */
  private final int position;

  /**
   * Constructs a new column.
   * 
   * @param data the data
   * @param numData number of tuples.
   * */
  public DoubleMutableColumn(final double[] data, final int numData) {
    Preconditions.checkNotNull(data);
    Preconditions.checkArgument(numData <= TupleBatch.BATCH_SIZE);
    this.data = data;
    position = numData;
  }

  @Override
  public Double get(final int row) {
    return Double.valueOf(getDouble(row));
  }

  @Override
  public void getIntoJdbc(final int row, final PreparedStatement statement, final int jdbcIndex) throws SQLException {
    statement.setDouble(jdbcIndex, getDouble(row));
  }

  @Override
  public void getIntoSQLite(final int row, final SQLiteStatement statement, final int sqliteIndex)
      throws SQLiteException {
    statement.bind(sqliteIndex, getDouble(row));
  }

  /**
   * Returns the element at the specified row in this column.
   * 
   * @param row row of element to return.
   * @return the element at the specified row in this column.
   */
  public double getDouble(final int row) {
    Preconditions.checkElementIndex(row, position);
    return data[row];
  }

  @Override
  public Type getType() {
    return Type.DOUBLE_TYPE;
  }

  @Override
  public ColumnMessage serializeToProto() {
    ByteBuffer dataBytes = ByteBuffer.allocate(position * Double.SIZE / Byte.SIZE);
    for (int i = 0; i < position; i++) {
      dataBytes.putDouble(data[i]);
    }
    dataBytes.flip();
    final DoubleColumnMessage.Builder inner = DoubleColumnMessage.newBuilder().setData(ByteString.copyFrom(dataBytes));

    return ColumnMessage.newBuilder().setType(ColumnMessage.Type.DOUBLE).setDoubleColumn(inner).build();
  }

  @Override
  public ColumnMessage serializeToProto(final ImmutableIntArray validIndices) {
    ByteBuffer dataBytes = ByteBuffer.allocate(validIndices.length() * Double.SIZE / Byte.SIZE);
    for (int i : validIndices) {
      dataBytes.putDouble(data[i]);
    }
    dataBytes.flip();
    final DoubleColumnMessage.Builder inner = DoubleColumnMessage.newBuilder().setData(ByteString.copyFrom(dataBytes));

    return ColumnMessage.newBuilder().setType(ColumnMessage.Type.DOUBLE).setDoubleColumn(inner).build();
  }

  @Override
  public int size() {
    return position;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(size()).append(" elements: [");
    for (int i = 0; i < size(); ++i) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(data[i]);
    }
    sb.append(']');
    return sb.toString();
  }

  @Override
  public boolean equals(final int leftIdx, final Column<?> rightColumn, final int rightIdx) {
    return getDouble(leftIdx) == ((DoubleMutableColumn) rightColumn).getDouble(rightIdx);
  }

  @Override
  public void append(final int index, final ColumnBuilder<?> columnBuilder) {
    ((DoubleColumnBuilder) columnBuilder).append(getDouble(index));
  }

  @Override
  public void addToHasher(final int row, final Hasher hasher) {
    hasher.putDouble(getDouble(row));
  }

  @Override
  public void replace(final int index, final Double value) {
    replace(index, value.doubleValue());
  }

  /**
   * replace the value on a row with the given double value.
   * 
   * @param index row index
   * @param value the double value.
   */
  public void replace(final int index, final double value) {
    Preconditions.checkElementIndex(index, size());
    data[index] = value;
  }

  @Override
  public DoubleColumn toColumn() {
    return new DoubleColumn(data.clone(), position);
  }

  @Override
  public DoubleMutableColumn clone() {
    return new DoubleMutableColumn(data.clone(), position);
  }
}