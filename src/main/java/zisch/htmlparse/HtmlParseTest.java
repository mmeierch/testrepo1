/*
 * Triggs u Gaeggs.
 */
package zisch.htmlparse;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import javax.xml.parsers.SAXParserFactory;

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
 * TODO [javadoc]: type HtmlParseTest
 * 
 * @author zisch
 */
public class HtmlParseTest {

  private static final String FACTORY_CLASS = "org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl";

  /**
   * TODO [javadoc]: method testParse
   * 
   * @throws Exception in case of errors
   */
  @Test
  public void testParse () throws Exception {
    final File srcFile = new File(HtmlParseTest.class.getResource("javadoc1.html").toURI());

    System.out.println("Create SAXParserFactory ...");
    final SAXParserFactory spf = SAXParserFactory.newInstance(FACTORY_CLASS, null);
    System.out.println("SAXParserFactory: " + spf + " (" + spf.getClass().getName() + ")");
    System.out.println("Parsing with SAX ...");
    spf.newSAXParser().parse(srcFile, new DefaultHandler());
    System.out.println("Done.");

    System.out.println("Parsing with DOM ...");
    final Sax2Dom s2d = new Sax2Dom();
    spf.newSAXParser().parse(srcFile, s2d);
    final Document doc = s2d.getDocument();
    System.out.println("Done.");
    System.out.println();
    printDocument(doc);
  }

  private static void printDocument (final Document doc) {
    try {
      final Writer sw = new PrintWriter(System.out);
      formatNode(sw, doc, "");
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
