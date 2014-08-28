/*
 * Triggs u Gaeggs.
 */
package zisch.htmlparse;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.ext.LexicalHandler;


/**
 * TODO [javadoc]: type Sax2Dom
 * 
 * @author zisch
 */
public class Sax2DomHandler extends DefaultHandler2 implements ContentHandler, LexicalHandler {
  // private static final String XMLNS_PREFIX = "xmlns";
  // private static final String XMLNS_STRING = "xmlns:";
  // private static final String XMLNS_URI = "http://www.w3.org/2000/xmlns/";

  private final Document mDocument;

  // private final List<Node> mNodeStack = new ArrayList<Node>();

  // private final Map<String, String> mNamespaceDecls = new HashMap<String, String>();

  private final Node mRoot;

  private Node mCurrentNode = null;

  /**
   * Default constructor.
   * <p>
   * This is a shortcut for {@link #Sax2DomHandler(Node) Sax2Dom(null)}.
   * 
   * @throws ParserConfigurationException if the internally used {@link DocumentBuilder} cannot be created for some
   *           reason
   */
  public Sax2DomHandler () throws ParserConfigurationException {
    this(null);
  }

  /**
   * Constructor which uses the specified {@code root} node as root of the document built from the SAX events.
   * 
   * @param root the root node of the document to build; may be {@code null} to use a newly created, empty
   *          {@link Document} instance as root
   * 
   * @throws ParserConfigurationException if the internally used {@link DocumentBuilder} cannot be created for some
   *           reason
   */
  public Sax2DomHandler (final Node root) throws ParserConfigurationException {
    if (root == null) {
      final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      mDocument = factory.newDocumentBuilder().newDocument();
      mRoot = mDocument;
    } else {
      if (root instanceof Document) {
        mDocument = (Document) root;
      } else {
        mDocument = root.getOwnerDocument();
      }
      mRoot = root;
    }
  }

  /**
   * Returns the {@link Document} which holds the built DOM nodes. If no root node has been specified at
   * {@linkplain #Sax2DomHandler(Node) construction} or the specified root node was an instance of {@link Document} this
   * method will return the {@linkplain #getRoot() root node} itself, otherwise it will return the
   * {@linkplain Node#getOwnerDocument() owner document} of the specified root node.
   * 
   * @return the {@link Document} which holds the built DOM nodes
   * 
   * @see #getRoot()
   */
  public Document getDocument () {
    return mDocument;
  }

  /**
   * Returns the root node specified at {@linkplain #Sax2DomHandler(Node) construction} or, if none had been specified,
   * the {@link Document} instance which has been created as the root node. The returned node is the parent of the nodes
   * created from the SAX events.
   * 
   * @return the root node specified at construction
   * 
   * @see #getDocument()
   */
  public Node getRoot () {
    return mRoot;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void characters (final char[] ch, final int start, final int length) {
    final Node last = peekNode();

    // No text nodes can be children of the document node:
    if (last == mDocument) {
      // FIXME: Should throw an exception if the text contains any non-whitespace character!
      // FIXME: Shouldn't we only get ignorableWhitespace events outside of the document element!??
    } else {
      final String text = new String(ch, start, length);
      last.appendChild(mDocument.createTextNode(text));
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void startDocument () {
    pushNode(mRoot);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void endDocument () {
    popNode();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void startElement (final String namespace, final String localName, final String qName, final Attributes attrs) {
    final Element tmp = mDocument.createElementNS(namespace, qName);

    // FIXME: This is most probably wrong; AFAIK DOM nodes should not have any namespace declaration attributes!
    // Note: If I'm wrong and we need this, start/endPrefixMapping must be implemented to keep track of the current
    // namespaces!
    // // Add namespace declarations first
    // for (final Map.Entry<String, String> decl : mNamespaceDecls.entrySet()) {
    // if (decl.getKey() == null || decl.getKey().isEmpty()) {
    // tmp.setAttributeNS(XMLNS_URI, XMLNS_PREFIX, decl.getValue());
    // } else {
    // tmp.setAttributeNS(XMLNS_URI, XMLNS_STRING + decl.getKey(), decl.getValue());
    // }
    // }
    // mNamespaceDecls.clear();

    // Add attributes to element
    final int nattrs = attrs.getLength();
    for (int i = 0; i < nattrs; i++) {
      if (attrs.getLocalName(i) == null) {
        tmp.setAttribute(attrs.getQName(i), attrs.getValue(i));
      } else {
        tmp.setAttributeNS(attrs.getURI(i), attrs.getQName(i), attrs.getValue(i));
      }
    }

    // Append this new node to the current node and use it as new current node:
    appendNode(tmp);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void endElement (final String namespace, final String localName, final String qName) {
    popNode();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void startPrefixMapping (final String prefix, final String uri) {
    // do nothing
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void endPrefixMapping (final String prefix) {
    // do nothing
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void ignorableWhitespace (char[] ch, int start, int length) {
    // do nothing
    // FIXME: should we make text nodes out of this!??
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void processingInstruction (final String target, final String data) {
    final Node last = peekNode();
    final ProcessingInstruction pi = mDocument.createProcessingInstruction(target, data);
    if (pi != null) {
      last.appendChild(pi);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setDocumentLocator (final Locator locator) {
    // do nothing
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void skippedEntity (final String name) {
    // do nothing
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void comment (final char[] ch, final int start, final int length) {
    final Node last = peekNode();
    final Comment comment = mDocument.createComment(new String(ch, start, length));
    if (comment != null) {
      last.appendChild(comment);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void startCDATA () {
    // do nothing
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void endCDATA () {
    // do nothing
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void startEntity (final String name) {
    // do nothing
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void endEntity (final String name) {
    // do nothing
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void startDTD (final String name, final String publicId, final String systemId) throws SAXException {
    // TODO: We currently ignore any actual DTD content (if there is some)!
    final DocumentType dtd = mDocument.getImplementation().createDocumentType(name, publicId, systemId);
    appendNode(dtd);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void endDTD () {
    popNode();
  }

  private void appendNode (final Node node) {
    peekNode().appendChild(node);
    pushNode(node);
  }

  private void pushNode (final Node node) {
    assert node != null;
    if (node.getParentNode() != mCurrentNode) {
      throw new IllegalStateException("Can only push a node which is a child node of the current node!");
    }
    mCurrentNode = node;
  }

  private Node popNode () {
    if (mCurrentNode == null) {
      throw new IllegalStateException("No more nodes to pop!");
    }
    final Node n = mCurrentNode;
    if (mCurrentNode == mRoot) {
      mCurrentNode = null;
    } else {
      mCurrentNode = n.getParentNode();
    }
    return n;
  }

  private Node peekNode () {
    if (mCurrentNode == null) {
      throw new IllegalStateException("No nodes to peek!");
    }
    return mCurrentNode;
  }
}
