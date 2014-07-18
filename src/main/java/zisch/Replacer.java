/*
 * Triggs u Gaeggs.
 */
package zisch;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;


/**
 * TODO [javadoc]: type Replacer
 * 
 * @author zisch
 */
public final class Replacer {

  public Replacer create (final Map<? extends CharSequence, ? extends CharSequence> mappings) {
    // FIXME: mappings must not be empty!
    final NodeBuilder root = new NodeBuilder();
    for (final Map.Entry<? extends CharSequence, ? extends CharSequence> e : mappings.entrySet()) {
      root.addSingleMapping(e.getKey(), e.getValue());
    }
    return null;
  }

  private static final class NodeBuilder {
    private final NavigableMap<Character, NodeBuilder> mNodesByKey = new TreeMap<Character, NodeBuilder>();

    private final List<NodeBuilder> mNodes = new ArrayList<NodeBuilder>();

    private final List<Character> mKeys = new ArrayList<Character>();

    private String mReplacement = null;

    void addSingleMapping (final CharSequence from, final CharSequence to) {
      if (from == null) {
        throw new IllegalArgumentException("mappings must not contain a null key.");
      }
      addSingleMapping(from, 0, to);
    }

    private void addSingleMapping (final CharSequence from, final int fromIdx, final CharSequence to) {
      if (fromIdx < from.length()) {
        // Find or create intermediate node and recurse:
        final Character key = from.charAt(fromIdx);
        NodeBuilder n = mNodesByKey.get(key);
        if (n == null) {
          n = new NodeBuilder();
          mNodesByKey.put(key, n);
        }
        n.addSingleMapping(from, fromIdx + 1, to);

      } else {
        // Set the replacement on the leaf node:
        assert mReplacement == null : "Unexpected NodeBuilder state: Replacement for key '" + from
                + "' already exists with value '" + mReplacement + "' but should be reset to "
                + (to == null ? "null" : "'" + to + "'");
        mReplacement = to == null ? null : to.toString();
      }
    }

    Replacer buildReplacer () {
      return null;
    }

    private Node buildNode () {
      return null;
    }
  }

  private static final class Node {
    private final Node[] mNodes;

    private final char[] mKeys;

    private final String mReplacement;

    private Node (final Node[] nodes, final char[] keys, final String replacement) {
      mNodes = nodes;
      mKeys = keys;
      mReplacement = replacement;
    }

    Node childForKey (final char key) {
      final int nodeIdx = Arrays.binarySearch(mKeys, key);
      return null;
    }
  }

  private final Node mRoot;

  private Replacer (final Node root) {
    mRoot = root;
  }

}
