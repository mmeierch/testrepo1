/*
 * Triggs u Gaeggs.
 */
package zisch.htmlparse;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * Unit tests for {@link HtmlDocumentBuilder}.
 * 
 * @author zisch
 */
public class HtmlDocumentBuilderUTest {
  private static final boolean DEBUG = true;

  /**
   * Tests {@link HtmlDocumentBuilder#parse(InputStream)} using a Javadoc HTML page to parse.
   * 
   * @throws ParserConfigurationException in case of errors
   * @throws SAXException in case of errors
   * @throws IOException in case of errors
   */
  @Test
  public void testParseJavadoc1 () throws ParserConfigurationException, SAXException, IOException {
    final String actual = parseResource("javadoc1.html");
    final String expected = loadResourceAsString("javadoc1-dom.txt");
    Assert.assertEquals(expected, actual);
  }

  private static String parseResource (final String rscName) throws SAXException, IOException,
          ParserConfigurationException {
    final HtmlDocumentBuilderFactory dbf = new HtmlDocumentBuilderFactory();
    final HtmlDocumentBuilder db = dbf.newDocumentBuilder();
    final InputStream is = openResource(rscName);
    final Document doc;
    try {
      doc = db.parse(is);
    } finally {
      is.close();
    }
    final String formatted = formatDocument(doc);
    if (DEBUG) {
      System.out.println("---- " + rscName + " ----");
      System.out.println(formatted);
      System.out.println("----");
      System.out.println();
    }
    return formatted;
  }

  private static String loadResourceAsString (final String rscName) {
    try {
      final StringBuilder sb = new StringBuilder();
      final InputStream is = openResource(rscName);
      try {
        final Reader r = new InputStreamReader(is, "UTF-8");
        final char[] buf = new char[8 * 1024];
        for (int cnt = r.read(buf); cnt > 0; cnt = r.read(buf)) {
          sb.append(buf, 0, cnt);
        }
      } finally {
        is.close();
      }
      return sb.toString().replace("\r\n", "\n").replace("\r", "\n");
    } catch (final Exception exc) {
      throw new IllegalStateException("Failed to load resource '" + rscName + "': " + exc, exc);
    }
  }

  private static InputStream openResource (final String rscName) {
    final InputStream rsc = HtmlDocumentBuilderUTest.class.getResourceAsStream(rscName);
    if (rsc == null) {
      throw new IllegalStateException("Cannot find resource file '" + rscName + "' in package '"
              + HtmlDocumentBuilderUTest.class.getPackage().getName() + "'.");
    }
    return rsc;
  }

  private static String formatDocument (final Document doc) {
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
    out.write("{");
    out.write(rootNode.getNamespaceURI());
    out.write("}");
    out.write(rootNode.getLocalName());
    out.write(":");
    if (rootNode.getNodeType() == Node.ELEMENT_NODE) {
      final Element el = (Element) rootNode;
      final NamedNodeMap attrs = el.getAttributes();
      final int len = attrs.getLength();
      if (len > 0) {
        out.write(" [ ");
        String delim = "";
        for (int i = 0; i < len; i++) {
          final Attr attr = (Attr) attrs.item(i);
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
}
