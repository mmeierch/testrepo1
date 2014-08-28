/*
 * Triggs u Gaeggs.
 */
package zisch.htmlparse;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.TreeMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Test HTML parsing using tagsoup.
 * 
 * @author zisch
 */
public class HtmlParseTest {

  private static final boolean DEBUG = true;

  private static final String FACTORY_CLASS = "org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl";

  /**
   * Tests parsing to SAX. Note: Does not (yet) do any assertions but only executes the parsing process, ignoring the
   * generated events!
   * 
   * @throws Exception in case of errors
   */
  @Test
  public void testParseSax () throws Exception {
    final File srcFile = new File(HtmlParseTest.class.getResource("javadoc1.html").toURI());
    logDebug("Create SAXParserFactory ...");
    final SAXParserFactory spf = SAXParserFactory.newInstance(FACTORY_CLASS, null);
    logDebug("SAXParserFactory: " + spf + " (" + spf.getClass().getName() + ")");
    logDebug("Parsing to SAX ...");
    spf.newSAXParser().parse(srcFile, new DefaultHandler());
    logDebug("Done.");
  }

  /**
   * Tests parsing HTML to DOM.
   * 
   * @throws Exception in case of errors
   */
  @Test
  public void testParseDom () throws Exception {
    final File srcFile = new File(HtmlParseTest.class.getResource("javadoc1.html").toURI());

    logDebug("Create SAXParserFactory ...");
    final SAXParserFactory spf = SAXParserFactory.newInstance(FACTORY_CLASS, null);
    logDebug("SAXParserFactory: " + spf + " (" + spf.getClass().getName() + ")");

    final Sax2DomHandler s2d = new Sax2DomHandler();
    final SAXParser sp = spf.newSAXParser();
    sp.getXMLReader().setProperty("http://xml.org/sax/properties/lexical-handler", s2d);

    logDebug("Parsing to DOM ...");
    sp.parse(srcFile, s2d);
    final Document doc = s2d.getDocument();
    logDebug("Done.");

    if (DEBUG) {
      System.out.println();
      printDocument(doc);
      System.out.println();
    }

    final String actualDom = documentToString(doc);
    final String expectedDom = loadResource("javadoc1-dom.txt");
    Assert.assertEquals(expectedDom, actualDom);
  }

  private static void printDocument (final Document doc) {
    try {
      final Writer sw = new PrintWriter(System.out);
      formatNode(sw, doc, "");
      sw.flush();
    } catch (final Exception exc) {
      throw new IllegalStateException("Failed to format DOM Document: " + exc, exc);
    }
  }

  private static String documentToString (final Document doc) {
    try {
      final StringWriter sw = new StringWriter();
      formatNode(sw, doc, "");
      sw.close();
      return sw.toString();
    } catch (final Exception exc) {
      throw new IllegalStateException("Failed to format DOM Document: " + exc, exc);
    }
  }

  private static void formatNode (final Writer out, final Node rootNode, final String indent) throws DOMException,
          IOException {
    switch (rootNode.getNodeType()) {
      case Node.CDATA_SECTION_NODE:
      case Node.COMMENT_NODE:
      case Node.TEXT_NODE:
        formatTextNode(out, rootNode, indent);
        break;

      case Node.DOCUMENT_NODE:
        formatDocumentNode(out, indent);
        break;

      case Node.ELEMENT_NODE:
        formatElementNode(out, rootNode, indent);
        break;

      case Node.DOCUMENT_TYPE_NODE:
        formatDtdNode(out, rootNode, indent);
        break;

      default:
        throw new IllegalStateException("Unexpected node type: " + rootNode.getNodeType());
    }
    final NodeList nl = rootNode.getChildNodes();
    final String newIndent = indent + "  ";
    for (int i = 0; i < nl.getLength(); i++) {
      formatNode(out, nl.item(i), newIndent);
    }
  }

  private static void formatDtdNode (final Writer out, final Node rootNode, final String indent) throws IOException {
    final DocumentType dtd = (DocumentType) rootNode;
    out.write(indent);
    out.write("#dtd: [ name: ");
    out.write(escapeValue(dtd.getName()));
    out.write(", public: ");
    out.write(escapeValue(dtd.getPublicId()));
    out.write(", system: ");
    out.write(escapeValue(dtd.getSystemId()));
    out.write(" ]\n");
  }

  private static void formatDocumentNode (final Writer out, final String indent) throws IOException {
    out.write(indent);
    out.write("#document:\n");
  }

  private static void formatElementNode (final Writer out, final Node rootNode, final String indent) throws IOException {
    out.write(indent);
    final String nsUri = rootNode.getNamespaceURI();
    if (nsUri != null) {
      out.write("{");
      out.write(nsUri);
      out.write("}");
      out.write(rootNode.getLocalName());
    } else {
      out.write(rootNode.getNodeName());
    }
    out.write(":");
    if (rootNode.getNodeType() == Node.ELEMENT_NODE) {
      final Element el = (Element) rootNode;
      final NamedNodeMap attrs = el.getAttributes();
      final int len = attrs.getLength();
      if (len > 0) {
        // Sort attributes:
        final TreeMap<String, Attr> sortedAttrs = new TreeMap<String, Attr>();
        for (int i = 0; i < len; i++) {
          final Attr attr = (Attr) attrs.item(i);
          sortedAttrs.put(attr.getName(), attr);
        }
        out.write(" [ ");
        String delim = "";
        for (final Attr attr : sortedAttrs.values()) {
          out.write(delim);
          out.write(attr.getName());
          out.write(": ");
          out.write(escapeValue(attr.getValue()));
          delim = ", ";
        }
        out.write(" ]");
      }
    }
    out.write("\n");
  }

  private static void formatTextNode (final Writer out, final Node rootNode, final String indent) throws IOException {
    out.write(indent);
    out.write(rootNode.getNodeName());
    out.write(": ");
    out.write(escapeValue(rootNode.getNodeValue()));
    out.write("\n");
  }

  private static String escapeValue (final String value) {
    if (value == null) {
      return "null";
    } else {
      String esc = value.replace("\\", "\\\\").replace("'", "\\'");
      esc = esc.replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
      return "'" + esc + "'";
    }
  }

  private static String loadResource (final String name) {
    final InputStream is = HtmlParseTest.class.getResourceAsStream(name);
    if (is == null) {
      throw new IllegalStateException("Cannot find resource '" + name + "' in package '"
              + HtmlParseTest.class.getPackage().getName() + "'.");
    }
    try {
      try {
        final StringBuilder sb = new StringBuilder();
        final char[] buf = new char[8 * 1024];
        final Reader r = new InputStreamReader(is, "UTF-8");
        for (int cnt = r.read(buf); cnt > 0; cnt = r.read(buf)) {
          sb.append(buf, 0, cnt);
        }
        return sb.toString().replace("\r\n", "\n").replace('\r', '\n');
      } finally {
        is.close();
      }
    } catch (final Exception exc) {
      throw new IllegalStateException("Failed to load resource '" + name + "' in package '"
              + HtmlParseTest.class.getPackage().getName() + "': " + exc, exc);
    }
  }

  private static void logDebug (final String msg) {
    if (DEBUG) {
      System.out.println(msg);
    }
  }
}
