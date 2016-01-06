/**
 *
 */
package edu.washington.escience.myria.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import edu.washington.escience.myria.MyriaConstants;
import edu.washington.escience.myria.api.PipedStreamingOutput;

/**
 * 
 */
public class PipeSink implements DataSink {
  /** Required for Java serialization. */
  private static final long serialVersionUID = 1L;

  PipedOutputStream writerOutput;
  PipedInputStream input;
  PipedStreamingOutput reponseEntity;

  public PipeSink() throws IOException {
    writerOutput = new PipedOutputStream();
    input = new PipedInputStream(writerOutput, MyriaConstants.DEFAULT_PIPED_INPUT_STREAM_SIZE);
    reponseEntity = new PipedStreamingOutput(input);
  }

  @Override
  public OutputStream getOutputStream() {
    return writerOutput;
  }

  public PipedStreamingOutput getResponse() {
    return reponseEntity;
  }
}
