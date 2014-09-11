/*
 * Triggs u Gaeggs.
 */
package zisch.json;


import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;


/**
 * Helper class to format JSON documents.
 * <p>
 * Instances of of this class are <strong><em>not</em> threadsafe</strong> and should not be used concurrently by
 * multiple threads.
 * 
 * @author zisch
 */
public class JsonWriter implements Closeable {

  private enum State {
    VALUE;
  }

  private final Appendable mOut;

  private final Charset mTargetCharset;

  private boolean mClosed = false;

  private State mState = null;

  /**
   * Constructor using {@code US-ASCII} as the default {@linkplain #getTargetCharset() target charset}.
   * 
   * @param out the {@link Appendable} to which the output of this writer shall be written
   */
  public JsonWriter (final Appendable out) {
    this(out, Charset.forName("US-ASCII"));
  }

  /**
   * Constructor.
   * 
   * @param out the {@link Appendable} to which the output of this writer shall be written
   * @param targetCharset the target character set (see {@link #getTargetCharset()})
   */
  public JsonWriter (final Appendable out, final Charset targetCharset) {
    // TODO: check out != null, targetCharset != null
    mOut = out;
    mTargetCharset = targetCharset;
  }

  /**
   * Returns the underlying {@link Appendable} to which the output of this writer is written.
   * 
   * @return the underlying {@link Appendable} to which the output of this writer is written
   */
  public Appendable getOut () {
    return mOut;
  }

  /**
   * Returns the target character set. The writer will encode any characters which cannot be represented in this target
   * charset with a <code>&#92;uXXXX</code> escape sequence. (Note that this does not affect &ldquo;special
   * characters&rdquo; which are always encoded, such as double quotes, backslashes or ISO control characters.)
   * <p>
   * By default the {@link JsonWriter} will use the {@code US-ASCII} charset, therefore encoding all characters with
   * code points greater than 127 (and minimizing any character encoding issues which might arise when exchanging JSON
   * documents).
   * 
   * @return the target character set
   */
  public Charset getTargetCharset () {
    return mTargetCharset;
  }

  /**
   * Closes the underlying {@linkplain #getOut() output} if and only if it implements the {@link AutoCloseable}
   * interface and this writer has not been closed before.
   */
  public void close () throws IOException {
    if (!mClosed) {
      mClosed = true;
      if (mOut instanceof AutoCloseable) {
        try {
          ((AutoCloseable) mOut).close();
        } catch (final Exception exc) {
          throw new IOException("Failed to close underlying Appendable (class '" + mOut.getClass().getName() + "'): "
                  + exc, exc);
        }
      }
    }
  }

  public JsonWriter writeStartObject () throws IOException {
    mOut.append('{');
    return this;
  }

  public JsonWriter writeEndObject () throws IOException {
    mOut.append('}');
    return this;
  }

  public JsonWriter writePrimitiveObjectValue (final String name, final Object value) {
    return this;
  }

  public JsonWriter writeStartObjectValue (final String name) {
    return this;
  }

  public JsonWriter writeStartArray () {
    return this;
  }

  public JsonWriter writeEndArray () {
    return this;
  }

  public JsonWriter writePrimitiveValue (final Object value) {
    return this;
  }

  private void checkState (final State... expected) {
    for (final State st : expected) {
      if (mState == st) {
        return;
      }
    }
    throw new IllegalStateException();
  }
}
