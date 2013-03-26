package edu.washington.escience.myriad.operator.agg;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import com.google.common.collect.ImmutableList;

import edu.washington.escience.myriad.DbException;
import edu.washington.escience.myriad.Schema;
import edu.washington.escience.myriad.TupleBatch;
import edu.washington.escience.myriad.Type;
import edu.washington.escience.myriad.operator.Operator;

/**
 * The Aggregation operator that computes an aggregate (e.g., sum, avg, max, min). Note that we only support aggregates
 * over a single column, grouped by a single column.
 */
public class MultiGroupByAggregate_NotYetDone extends Operator {

  /**
   * A simple implementation of multiple-field group key
   * */
  protected static class SimpleArrayWrapper {
    public final Object[] groupFields;

    public SimpleArrayWrapper(final Object[] groupFields) {
      this.groupFields = groupFields;
    }

    @Override
    public boolean equals(final Object another) {
      if (this == another) {
        return true;
      }
      if (!(another instanceof SimpleArrayWrapper)) {
        return false;
      }
      return (another != null) && Arrays.equals(groupFields, ((SimpleArrayWrapper) another).groupFields);
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(groupFields);
    }
  }

  private static final long serialVersionUID = 1L;
  private final Schema schema;
  private Operator child;
  private final Aggregator[] agg;
  private final int[] afields; // Compute aggregate on each of the afields
  private final int[] gfields; // group by fields
  private final boolean groupBy;

  private final HashMap<SimpleArrayWrapper, Aggregator[]> groupAggs;

  /**
   * Constructor.
   * 
   * Implementation hint: depending on the type of afield, you will want to construct an {@link IntAggregator} or
   * {@link StringAggregator} to help you with your implementation of readNext().
   * 
   * 
   * @param child The Operator that is feeding us tuples.
   * @param afields The columns over which we are computing an aggregate.
   * @param gfields The columns over which we are grouping the result, or -1 if there is no grouping
   * @param aggOps The aggregation operator to use
   */
  public MultiGroupByAggregate_NotYetDone(final Operator child, final int[] afields, final int[] gfields,
      final int[] aggOps) {
    Objects.requireNonNull(afields);
    if (afields.length == 0) {
      throw new IllegalArgumentException("aggregation fields must not be empty");
    }

    if (gfields == null || gfields.length == 0) {
      this.gfields = new int[0];
      groupBy = false;
      groupAggs = null;
    } else {
      this.gfields = gfields;
      groupBy = true;
      groupAggs = new HashMap<SimpleArrayWrapper, Aggregator[]>();
    }

    final ImmutableList.Builder<Type> gTypes = ImmutableList.builder();
    final ImmutableList.Builder<String> gNames = ImmutableList.builder();

    final Schema childSchema = child.getSchema();
    for (final int i : this.gfields) {
      gTypes.add(childSchema.getColumnType(i));
      gNames.add(childSchema.getColumnName(i));
    }

    Schema outputSchema = new Schema(gTypes, gNames);

    this.child = child;
    this.afields = afields;
    agg = new Aggregator[aggOps.length];

    int idx = 0;
    for (final int afield : afields) {
      switch (childSchema.getColumnType(afield)) {
        case BOOLEAN_TYPE:
          agg[idx] = new BooleanAggregator(afield, childSchema.getColumnName(afield), aggOps[idx]);
          outputSchema = Schema.merge(outputSchema, agg[idx].getResultSchema());
          break;
        case INT_TYPE:
          agg[idx] = new IntegerAggregator(afield, childSchema.getColumnName(afield), aggOps[idx]);
          outputSchema = Schema.merge(outputSchema, agg[idx].getResultSchema());
          break;
        case LONG_TYPE:
          agg[idx] = new LongAggregator(afield, childSchema.getColumnName(afield), aggOps[idx]);
          outputSchema = Schema.merge(outputSchema, agg[idx].getResultSchema());
          break;
        case FLOAT_TYPE:
          agg[idx] = new FloatAggregator(afield, childSchema.getColumnName(afield), aggOps[idx]);
          outputSchema = Schema.merge(outputSchema, agg[idx].getResultSchema());
          break;
        case DOUBLE_TYPE:
          agg[idx] = new DoubleAggregator(afield, childSchema.getColumnName(afield), aggOps[idx]);
          outputSchema = Schema.merge(outputSchema, agg[idx].getResultSchema());
          break;
        case STRING_TYPE:
          agg[idx] = new StringAggregator(afield, childSchema.getColumnName(afield), aggOps[idx]);
          outputSchema = Schema.merge(outputSchema, agg[idx].getResultSchema());
          break;
      }
      idx++;
    }
    schema = outputSchema;
  }

  /**
   * @return the aggregate field
   * */
  public int[] aggregateFields() {
    return afields;
  }

  @Override
  protected void cleanup() throws DbException {
    groupAggs.clear();
  }

  @Override
  protected TupleBatch fetchNext() throws DbException {

    // Actually perform the aggregation
    TupleBatch tb = null;
    while ((tb = child.next()) != null) {
      if (!groupBy) {
        for (final Aggregator ag : agg) {
          ag.add(tb);
        }
      } else {
        // group by

      }
    }
    return null;
  }

  @Override
  protected TupleBatch fetchNextReady() throws DbException {
    // TODO non-blocking
    return fetchNext();
  }

  @Override
  public Operator[] getChildren() {
    return new Operator[] { child };
  }

  /**
   * The schema of the aggregate output. Grouping fields first and then aggregate fields. The aggregate
   */
  @Override
  public Schema getSchema() {
    return schema;
  }

  public int[] groupFields() {
    return gfields;
  }

  @Override
  protected void init() throws DbException {
  }

  @Override
  public void setChildren(final Operator[] children) {
    child = children[0];
  }

}