package zisch.lunr;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;


/**
 * TODO [javadoc]: type LunrIndex
 * <p>
 * <strong>NOTE:</strong> This class needs Java 1.7+ at runtime and will not work in a Java 1.6 engine (because
 * <em>Rhino</em> in Java 1.6 is missing some JavaScript features needed by <em>lunr.js</em>)!
 * <p>
 * FIXME: GZIP serialized index; use zlib.js do decompress it again when used!
 * 
 * @author zisch
 * 
 * @see <a href="http://lunrjs.com/">lunr.js</a>
 * @see <a href="https://github.com/imaya/zlib.js/blob/master/README.en.md">zlib.js</a>
 */
public final class LunrIndex {
  /**
   * TODO [javadoc]: method main
   * 
   * @param args TODO
   */
  public static void main (final String[] args) {
    final LunrIndex idx = new LunrIndex(new FieldSpec("title", 10), new FieldSpec("body"));

    mainAddDoc(idx, 0, "About the ScriptEngineManager", "Java provides the way to execute JavaScript also. "
            + "The solution is come along with ScriptEngineManager. This article deals the same.");
    mainAddDoc(idx, 1, "OWASP Encoder",
            "The OWASP Java Encoder is a Java 1.5+ simple-to-use drop-in high-performance encoder class "
                    + "with no dependencies and little baggage. "
                    + "This project will help Java web developers defend against Cross Site Scripting!");
    mainAddDoc(idx, 2, "UBS News", "With over 1.96 trillion US dollars in assets under management (AuM), "
            + "UBS remains the largest and fastest-growing wealth manager in the world, "
            + "according to Scorpio Partnership’s Global Private Banking Benchmark 2014. "
            + "This follows a 15.4 percent increase in the bank’s AuM in 2013. "
            + "It is the second year in a row that UBS has claimed the top spot in the study.");
    mainAddDoc(idx, 3, "An Odd Way to Find Even Numbers", "After numerous requests from users, "
            + "the project manager for WeightTracker asked Fred to add zebra striping to the weight journal window. "
            + "Fred had inherited oversight of the application after the original developer, "
            + "Louis, had been poached by their underperforming rival.");

    final String serialized = idx.serializeIndex();
    System.out.println("----");
    System.out.println(serialized);
  }

  private static void mainAddDoc (final LunrIndex idx, final long id, final String title, final String body) {
    final Map<String, String> fields = new LinkedHashMap<String, String>();
    fields.put("title", title);
    fields.put("body", body);
    idx.addDocument(id, fields);
  }

  /**
   * TODO [javadoc]: type FieldSpec
   * 
   * @author zisch
   */
  public static final class FieldSpec {
    private final String mName;

    private final int mBoost;

    /**
     * TODO [javadoc]: constructor FieldSpec
     * 
     * @param name TODO
     */
    public FieldSpec (final String name) {
      this(name, 1);
    }

    /**
     * TODO [javadoc]: constructor FieldSpec
     * 
     * @param name TODO
     * @param boost TODO
     */
    public FieldSpec (final String name, final int boost) {
      // TODO: check name is valid Java identifier and boost >= 1
      mName = name;
      mBoost = boost;
    }

    /**
     * @return TODO
     */
    public String getName () {
      return mName;
    }

    /**
     * @return TODO
     */
    public int getBoost () {
      return mBoost;
    }
  }

  private final String mRefName;

  private final ScriptEngine mEngine;

  private final Map<String, FieldSpec> mFields;

  /**
   * TODO [javadoc]: constructor LunrIndex
   * 
   * @param fields TODO
   */
  public LunrIndex (final FieldSpec... fields) {
    this("id", fields);
  }

  /**
   * TODO [javadoc]: constructor LunrIndex
   * 
   * @param refName TODO
   * @param fields TODO
   */
  public LunrIndex (final String refName, final FieldSpec... fields) {
    // TODO: check refName is valid Java identifier
    // TODO: check fields not empty and no nulls
    // TODO: check refName not a name used in fields!
    mRefName = refName;
    final Map<String, FieldSpec> fieldsByName = new LinkedHashMap<String, FieldSpec>();
    for (final FieldSpec fs : fields) {
      fieldsByName.put(fs.getName(), fs);
    }
    mFields = Collections.unmodifiableMap(fieldsByName);
    mEngine = loadLunrScript(mRefName, mFields);
  }

  /**
   * TODO [javadoc]: method addDocument
   * 
   * @param id TODO
   * @param fields TODO
   */
  public void addDocument (final long id, final Map<?, ?> fields) {
    // TODO: check that id >= 0
    final StringBuilder jsCode = new StringBuilder(128);
    jsCode.append("index.add({\n");
    jsCode.append(mRefName).append(": ").append(id);
    for (final Map.Entry<?, ?> f : fields.entrySet()) {
      final Object k = f.getKey();
      if (k == null) {
        throw new IllegalArgumentException("fields must not contain a null key");
      }
      final String kstr = k.toString();
      if (!mFields.containsKey(kstr)) {
        throw new IllegalArgumentException("Field '" + kstr
                + "' found in fields has not been specified fro this index.");
      }
      jsCode.append(",\n").append(k).append(": ");
      appendJsString(jsCode, f.getValue());
    }
    jsCode.append("\n})");
    evalJs("add document '" + id + "'", mEngine, jsCode);
  }

  /**
   * TODO [javadoc]: method serializeIndex
   * 
   * @return TODO
   */
  public String serializeIndex () {
    final String jsCode = "JSON.stringify(index.toJSON(), null)";
    final Object result = evalJs("serialize index", mEngine, jsCode);
    return (String) result;
  }

  private static void appendJsString (final StringBuilder jsCode, final Object value) {
    final String str = value == null ? "" : value.toString();
    jsCode.append('\'');
    for (int i = 0; i < str.length(); i++) {
      final char c = str.charAt(i);
      switch (c) {
        case '\'':
        case '"':
        case '\\':
          jsCode.append('\\');
      }
      jsCode.append(c);
    }
    jsCode.append('\'');
  }

  private static ScriptEngine loadLunrScript (final String refName, final Map<String, FieldSpec> fields) {
    final ScriptEngineManager factory = new ScriptEngineManager();
    final ScriptEngine engine = factory.getEngineByName("JavaScript");

    final String scriptName = "lunr.js";
    final InputStream is = LunrIndex.class.getResourceAsStream(scriptName);
    if (is == null) {
      throw new IllegalStateException("Cannot find lunr script '" + scriptName + "' in resources of '"
              + LunrIndex.class.getName() + "'.");
    }
    try {
      final Reader ir = new InputStreamReader(is, "UTF-8");
      engine.eval(ir);
    } catch (final IOException exc) {
      throw new IllegalStateException("Failed to load lunr script '" + scriptName + "' from resources of '"
              + LunrIndex.class.getName() + "': " + exc, exc);
    } catch (final ScriptException exc) {
      throw new IllegalStateException("Failed to load lunr script '" + scriptName + "' from resources of '"
              + LunrIndex.class.getName() + "': " + exc, exc);
    } finally {
      try {
        is.close();
      } catch (final IOException exc) {
        throw new IllegalStateException("Failed to close input stream from lunr script '" + scriptName
                + "' in resources of '" + LunrIndex.class.getName() + "': " + exc, exc);
      }
    }

    final StringBuilder jsCode = new StringBuilder(128);
    jsCode.append("var index = lunr(function () {\n");
    jsCode.append("this.ref('").append(refName).append("')\n");
    for (final FieldSpec fs : fields.values()) {
      jsCode.append("this.field('").append(fs.getName()).append("', {boost: ").append(fs.getBoost()).append("})\n");
    }
    jsCode.append("})");
    evalJs("initialize lunr index", engine, jsCode);

    return engine;
  }

  private static Object evalJs (final String actionLabel, final ScriptEngine engine, final CharSequence jsCode) {
    try {
      return engine.eval(jsCode.toString());
    } catch (final ScriptException exc) {
      throw new IllegalStateException("Failed to " + actionLabel + " with <" + jsCode + ">: " + exc, exc);
    }
  }
}
