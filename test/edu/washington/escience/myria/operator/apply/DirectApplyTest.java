package edu.washington.escience.myria.operator.apply;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import edu.washington.escience.myria.DbException;
import edu.washington.escience.myria.Schema;
import edu.washington.escience.myria.TupleBatch;
import edu.washington.escience.myria.TupleBatchBuffer;
import edu.washington.escience.myria.Type;
import edu.washington.escience.myria.operator.TupleSource;

public class DirectApplyTest {

  private int numTuples;
  private int multiplicationFactor;

  @Before
  public void setUp() throws Exception {
    // numTuples = rand.nextInt(RANDOM_LIMIT);
    numTuples = 20000;
    // multiplicationFactor = rand.nextInt(RANDOM_LIMIT);
    multiplicationFactor = 2;
  }

  @Test
  public void testApplySqrt() throws DbException {
    final Schema schema = new Schema(ImmutableList.of(Type.LONG_TYPE), ImmutableList.of("a"));
    final TupleBatchBuffer tbb = new TupleBatchBuffer(schema);
    for (long i = 0; i < numTuples; i++) {
      tbb.put(0, (long) Math.pow(i, 2));
    }
    ImmutableList.Builder<Integer> arguments = ImmutableList.builder();
    arguments.add(0, 2);
    ImmutableList.Builder<IFunctionCaller> callers = ImmutableList.builder();
    callers.add(new IFunctionCaller(new SqrtIFunction(), arguments.build()));
    DirectApply directApply = new DirectApply(new TupleSource(tbb), callers.build());
    directApply.open(null);
    TupleBatch result;
    int resultSize = 0;
    while (!directApply.eos()) {
      result = directApply.nextReady();
      if (result != null) {
        assertEquals(2, result.getSchema().numColumns());
        assertEquals(Type.DOUBLE_TYPE, result.getSchema().getColumnType(1));
        for (int i = 0; i < result.numTuples(); i++) {
          assertEquals(i + resultSize, result.getDouble(1, i), 0.0000001);
        }
        resultSize += result.numTuples();
      }
    }
    assertEquals(numTuples, resultSize);
    directApply.close();
  }

  @Test
  public void testApplyMultiFunctions() throws DbException {
    final Schema schema = new Schema(ImmutableList.of(Type.LONG_TYPE), ImmutableList.of("a"));

    final TupleBatchBuffer tbb = new TupleBatchBuffer(schema);
    for (long i = 0; i < numTuples; i++) {
      tbb.put(0, (long) Math.pow(i, 2));
    }
    ImmutableList.Builder<Integer> argumentsOne = ImmutableList.builder();
    argumentsOne.add(0);
    ImmutableList.Builder<Integer> argumentsTwo = ImmutableList.builder();
    argumentsTwo.add(0, multiplicationFactor);
    ImmutableList.Builder<IFunctionCaller> callers = ImmutableList.builder();
    callers.add(new IFunctionCaller(new SqrtIFunction(), argumentsOne.build()));
    callers.add(new IFunctionCaller(new ConstantMultiplicationIFunction(), argumentsTwo.build()));
    DirectApply directApply = new DirectApply(new TupleSource(tbb), callers.build());
    directApply.open(null);
    TupleBatch result;
    int resultSize = 0;
    while (!directApply.eos()) {
      result = directApply.nextReady();
      if (result != null) {
        assertEquals(3, result.getSchema().numColumns());
        assertEquals(Type.DOUBLE_TYPE, result.getSchema().getColumnType(1));
        assertEquals(Type.LONG_TYPE, result.getSchema().getColumnType(2));
        for (int i = 0; i < result.numTuples(); i++) {
          assertEquals(i + resultSize, result.getDouble(1, i), 0.0000001);
          assertEquals((long) Math.pow(i + resultSize, 2) * multiplicationFactor, result.getLong(2, i));
        }
        resultSize += result.numTuples();
      }
    }
    assertEquals(numTuples, resultSize);
    directApply.close();
  }
}
