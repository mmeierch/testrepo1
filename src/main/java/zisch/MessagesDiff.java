/*
 * Triggs u Gaeggs.
 */
package zisch;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class MessagesDiff {

  private static final Set<String> sLangSuffixes = new TreeSet<String>(Arrays.asList(new String[] { "EN", "DE", "FR",
          "IT" }));

  private static final Map<String, Map<String, String>> sOldTextsByLangAndKey = new TreeMap<String, Map<String, String>>();

  private static final Map<String, Map<String, String>> sNewTextsByLangAndKey = new TreeMap<String, Map<String, String>>();

  public static void main (final String[] args) throws Exception {
    final String propertiesFileBasepath = "C:\\Users\\Public\\Data\\RAD\\Shared\\PrinterBasket\\PAW_1_PrinterBasketBaseJava\\Res_MLV_20140829_001_";
    final File messagesXmlFile = new File(
            "C:\\Users\\Public\\Data\\RAD\\Shared\\PrinterBasket\\PAW_1_PrinterBasketBaseJava\\src\\main\\bundle\\messages.xml");

    // Load properties file:
    for (final String langSuff : sLangSuffixes) {
      final File propFile = new File(propertiesFileBasepath + langSuff + ".txt");
      final Properties props = new Properties();
      final Reader r = new InputStreamReader(new FileInputStream(propFile), "ISO-8859-1");
      try {
        props.load(r);
      } finally {
        r.close();
      }
      final Map<String, String> trg = new TreeMap<String, String>();
      sNewTextsByLangAndKey.put(langSuff, trg);
      for (final Map.Entry<Object, Object> e : props.entrySet()) {
        trg.put((String) e.getKey(), (String) e.getValue());
      }
    }

    // Load messages.xml:
    final Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(messagesXmlFile);
    final NodeList bundles = dom.getDocumentElement().getChildNodes();
    for (int i = 0; i < bundles.getLength(); i++) {
      final Node n = bundles.item(i);
      if (n instanceof Element) {
        final Element e = (Element) n;
        if (!e.getTagName().equals("bundle")) {
          throw new IllegalStateException("Found unexpected child element in root element: <" + e.getTagName() + ">");
        }
        processBundle(e);
      }
    }

    // Compare old and new:
    for (final String lang : sLangSuffixes) {
      System.out.println("========== " + lang + " ==========");
      System.out.println();
      final Map<String, String> oldMap = sOldTextsByLangAndKey.get(lang);
      final Map<String, String> newMap = sNewTextsByLangAndKey.get(lang);

      final Set<String> missingInOld = new TreeSet<String>(newMap.keySet());
      missingInOld.removeAll(oldMap.keySet());

      final Set<String> missingInNew = new TreeSet<String>(oldMap.keySet());
      missingInNew.removeAll(newMap.keySet());

      final Set<String> commonKeys = new TreeSet<String>(newMap.keySet());
      commonKeys.retainAll(oldMap.keySet());

      if (missingInOld.size() > 0) {
        System.out.println("Keys found in new properties but not in existing 'messages.xml':");
        for (final String k : missingInOld) {
          System.out.println("    " + k);
        }
        System.out.println();
      }

      if (missingInNew.size() > 0) {
        System.out.println("Keys found in existing 'messages.xml' but not in new properties:");
        for (final String k : missingInNew) {
          System.out.println("    " + k);
        }
        System.out.println();
      }

      boolean first = true;
      for (final String k : commonKeys) {
        final String oldValue = oldMap.get(k);
        final String newValue = newMap.get(k);
        if (!oldValue.equals(newValue)) {
          if (first) {
            System.out.println("Keys with differing values:");
            first = false;
          }
          System.out.println("    " + k + ": old == '" + oldValue + "', new == '" + newValue + "'");
        }
      }
      if (!first) {
        System.out.println();
      }
    }
  }

  private static void processBundle (final Element bundleElement) {
    final NodeList entries = bundleElement.getChildNodes();
    for (int i = 0; i < entries.getLength(); i++) {
      final Node n = entries.item(i);
      if (n instanceof Element) {
        final Element e = (Element) n;
        if (!e.getTagName().equals("entry")) {
          throw new IllegalStateException("Found unexpected child element in <bundle> element: <" + e.getTagName()
                  + ">");
        }
        final String key = e.getAttribute("key");
        final NodeList textEntries = e.getChildNodes();
        for (int j = 0; j < textEntries.getLength(); j++) {
          final Node tn = textEntries.item(j);
          if (tn instanceof Element) {
            final Element te = (Element) tn;
            if (te.getTagName().equals("text")) {
              final String txt = te.getTextContent();
              String locale = te.getAttribute("locale").toUpperCase(Locale.US);
              if (locale.isEmpty()) {
                locale = "EN";
              }
              if (!sLangSuffixes.contains(locale)) {
                throw new IllegalStateException("Found unexpected locale in <text> element: '" + locale + "'");
              }
              Map<String, String> trg = sOldTextsByLangAndKey.get(locale);
              if (trg == null) {
                trg = new TreeMap<String, String>();
                sOldTextsByLangAndKey.put(locale, trg);
              }
              trg.put(key, txt);
            }
          }
        }
      }
    }
  }

}
