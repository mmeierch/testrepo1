/*
 * Triggs u Gaeggs.
 */
package zisch;


/**
 * TODO [javadoc]: type HangulAlgorithm
 * 
 * @author zisch
 */
final class HangulAlgorithm {

  private static final int SBase = 0xAC00;

  private static final int LBase = 0x1100;

  private static final int VBase = 0x1161;

  private static final int TBase = 0x11A7;

  private static final int LCount = 19;

  private static final int VCount = 21;

  private static final int TCount = 28;

  private static final int NCount = VCount * TCount; // 588

  private static final int SCount = LCount * NCount; // 11172

  static String decomposeHangul (final int cp) {
    final int SIndex = cp - SBase;
    if (SIndex < 0 || SIndex >= SCount) {
      return null;
    }
    final StringBuilder result = new StringBuilder(3);
    final int L = LBase + SIndex / NCount;
    final int V = VBase + (SIndex % NCount) / TCount;
    final int T = TBase + SIndex % TCount;
    result.append((char) L);
    result.append((char) V);
    if (T != TBase) {
      result.append((char) T);
    }
    return result.toString();
  }

  public static String composeHangul (final String source) {
    final int len = source.length();
    if (len == 0) {
      return "";
    }

    final StringBuilder result = new StringBuilder();
    char last = source.charAt(0); // copy first char
    result.append(last);

    for (int i = 1; i < len; ++i) {
      final char ch = source.charAt(i);

      // 1. check to see if two current characters are L and V
      final int LIndex = last - LBase;
      if (0 <= LIndex && LIndex < LCount) {
        final int VIndex = ch - VBase;
        if (0 <= VIndex && VIndex < VCount) {
          // make syllable of form LV
          last = (char) (SBase + (LIndex * VCount + VIndex) * TCount);
          result.setCharAt(result.length() - 1, last); // reset last
          continue; // discard ch
        }
      }

      // 2. check to see if two current characters are LV and T
      final int SIndex = last - SBase;
      if (0 <= SIndex && SIndex < SCount && (SIndex % TCount) == 0) {
        final int TIndex = ch - TBase;
        if (0 < TIndex && TIndex < TCount) {
          // make syllable of form LVT
          last += TIndex;
          result.setCharAt(result.length() - 1, last); // reset last
          continue; // discard ch
        }
      }

      // if neither case was true, just add the character
      last = ch;
      result.append(ch);
    }

    return result.toString();
  }

  private HangulAlgorithm () {
    throw new AssertionError("not allowed");
  }
}
