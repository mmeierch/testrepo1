/*
 * Triggs u Gaeggs.
 */
package zisch.htmlparse;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * TODO [javadoc]: type HtmlDocumentBuilder
 * 
 * @author zisch
 */
public class HtmlDocumentBuilder extends DocumentBuilder {

  private final String mTargetNamespace;

  private ErrorHandler mErrorHandler = null;

  /**
   * Default constructor using {@linkplain HtmlDocumentBuilderFactory#TARGET_NAMESPACE_DEFAULT_VALUE} the default XHTML
   * target namespace}.
   */
  public HtmlDocumentBuilder () {
    this(HtmlDocumentBuilderFactory.TARGET_NAMESPACE_DEFAULT_VALUE);
  }

  /**
   * Constructor.
   * 
   * @param targetNamespace the fixed namespace URI which is assigned to all elements of the HTML documents parsed by
   *          this builder; may be {@code null} or empty for no namespace
   */
  public HtmlDocumentBuilder (final String targetNamespace) {
    final String ts = targetNamespace == null ? null : targetNamespace.trim();
    mTargetNamespace = ts == null || ts.isEmpty() ? null : ts;
  }

  /**
   * Returns the fixed namespace URI which is assigned to all elements of the HTML documents parsed by this builder; may
   * be {@code null} or empty for no namespace.
   * 
   * @return the fixed namespace URI which is assigned to all elements of the HTML documents parsed by this builder; may
   *         be {@code null} or empty for no namespace
   * 
   * @see #isNamespaceAware()
   */
  public String getTargetNamespace () {
    return mTargetNamespace;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DOMImplementation getDOMImplementation () {
    return DomImpl.INSTANCE;
  }

  /**
   * An {@code HtmlDocumentBuilder} will ignore namespaces in the source documents (since they are not defined in HTML).
   * However, all elements created by a specific builder will have a {@linkplain #getTargetNamespace() fixed target
   * namespace} assigned. That target namespace is defined when the builder is constructed and defaults to the XHTML
   * namespace {@code http://www.w3.org/1999/xhtml} (see
   * {@link HtmlDocumentBuilderFactory#TARGET_NAMESPACE_DEFAULT_VALUE}).
   * 
   * @return always {@code false}
   * 
   * @see #getTargetNamespace()
   */
  @Override
  public boolean isNamespaceAware () {
    return false;
  }

  /**
   * An {@code HtmlDocumentBuilder} does never validate. (If necessary this can be done on the built DOM document.)
   */
  @Override
  public boolean isValidating () {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setEntityResolver (final EntityResolver er) {
    // ignored (we don't need to resolve external entities)
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setErrorHandler (final ErrorHandler errorHandler) {
    mErrorHandler = errorHandler;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Document newDocument () {
    return Tidy.createEmptyDocument(mTargetNamespace);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Document parse (final InputSource is) throws SAXException, IOException {
    // FIXME: check is not null!
    final Tidy t = tidy();
    if (is.getCharacterStream() != null) {
      return t.parseDOM(is.getCharacterStream(), mTargetNamespace);

    } else if (is.getByteStream() != null) {
      if (is.getEncoding() != null) {
        t.setInputEncoding(is.getEncoding());
      }
      return t.parseDOM(is.getByteStream(), mTargetNamespace);

    } else if (is.getSystemId() != null) {
      final URL url = new URL(is.getSystemId());
      final URLConnection conn = url.openConnection();
      conn.connect();
      final InputStream in = conn.getInputStream();
      try {
        final String enc = conn.getContentEncoding();
        if (enc != null) {
          t.setInputEncoding(enc);
        }
        return t.parseDOM(in, mTargetNamespace);
      } finally {
        in.close();
      }

    } else {
      throw new IllegalArgumentException(
              "Specified InputSource has neither character stream, byte stream nor system ID.");
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void reset () {
    // do nothing
  }

  private Tidy tidy () {
    return new Tidy(mErrorHandler);
  }

  private static final class DomImpl implements DOMImplementation {
    private static final DomImpl INSTANCE = new DomImpl();

    /**
     * {@inheritDoc}
     */
    @Override
    public Document createDocument (final String namespaceURI, final String qualifiedName, final DocumentType doctype)
            throws DOMException {
      // TODO Auto-generated method stub
      return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DocumentType createDocumentType (final String qualifiedName, final String publicId, final String systemId)
            throws DOMException {
      // TODO Auto-generated method stub
      return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getFeature (final String feature, final String version) {
      return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasFeature (final String feature, final String version) {
      return false;
    }
  }
}
