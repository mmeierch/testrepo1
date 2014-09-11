/*
 * Triggs u Gaeggs.
 */
package zisch.json;


import java.io.IOException;


/**
 * Utility class with methods to format and parse JSON fragments.
 * 
 * @author zisch
 */
public final class JsonUtil {

  public String toJsonString (final CharSequence cs) {
    // TODO: check cs != null
    return appendJsonString(new StringBuilder(cs.length() + 16), cs).toString();
  }

  public StringBuilder appendJsonString (final StringBuilder out, final CharSequence cs) {
    try {
      appendJsonString((Appendable) out, cs);
    } catch (final IOException exc) {
      throw new IllegalStateException("Unexpected IOException while writing to StringBuilder: " + exc, exc);
    }
    return out;
  }

  public Appendable appendJsonString (final Appendable out, final CharSequence cs) throws IOException {
    // TODO: check out != null, cs != null
    out.append('"');
    final int len = cs.length();
    for (int i = 0; i < len; i++) {
      final char c = cs.charAt(i);
      switch (c) {
        case '"':
          out.append("\\\"");
          break;

        case '\\':
          out.append("\\\\");
          break;

        case '\b':
          out.append("\\b");
          break;

        case '\f':
          out.append("\\f");
          break;

        case '\n':
          out.append("\\n");
          break;

        case '\r':
          out.append("\\r");
          break;

        case '\t':
          out.append("\\t");
          break;

        default:
          if (mustEscape(c)) {
            final String v = Integer.toHexString(c);
            assert v.length() <= 4;
            out.append("\\u");
            final int zeroes = 4 - v.length();
            for (int j = 0; j < zeroes; j++) {
              out.append('0');
            }
            out.append(v);
          } else {
            out.append(c);
          }
      }
    }
    out.append('"');
    return out;
  }

  private boolean mustEscape (final char c) {
    return Character.isISOControl(c); // FIXME: compare with JavaLangUtil and use targetCharset!
  }

  private JsonUtil () {
    throw new AssertionError("not allowed");
  }
}
