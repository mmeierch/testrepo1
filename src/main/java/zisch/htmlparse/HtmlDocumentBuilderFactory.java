/*
 * Triggs u Gaeggs.
 */
package zisch.htmlparse;


import java.util.concurrent.atomic.AtomicReference;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


/**
 * TODO [javadoc]: type HtmlParserUtil
 * 
 * @author zisch
 */
public class HtmlDocumentBuilderFactory extends DocumentBuilderFactory {

  private static final String ATTR_NAME_BASE = "https://dals.ch/ns/ch.dals.lib.xml.html.HtmlDocumentBuilderFactory/";

  /**
   * The attribute name of the target namespace attribute.
   * <p>
   * The target namespace {@code String} attribute defines the namespace of the created DOM elements. When setting the
   * namespace any type of {@code Object} is accepted; objects other than {@code String}s will be converted using
   * {@code toString()}. The string value will also be {@linkplain String#trim() trimmed}. Empty strings will be
   * normalized to {@code null} and setting {@code null} will result in the omission of a namespace for the created
   * elements.
   * 
   * @see #getAttribute(String)
   * @see #setAttribute(String, Object)
   */
  public static final String ATTRIBUTE_TARGET_NAMESPACE = ATTR_NAME_BASE + "targetNamespace";

  /**
   * The XHTML namespace {@value} used as the default {@linkplain #ATTRIBUTE_TARGET_NAMESPACE target namespace}.
   */
  public static final String TARGET_NAMESPACE_DEFAULT_VALUE = "http://www.w3.org/1999/xhtml";

  private static final String ACCESS_EXTERNAL_DTD = "http://javax.xml.XMLConstants/property/accessExternalDTD";

  private static final String ACCESS_EXTERNAL_SCHEMA = "http://javax.xml.XMLConstants/property/accessExternalSchema";

  private AtomicReference<String> mTargetNamespace = new AtomicReference<String>(TARGET_NAMESPACE_DEFAULT_VALUE);

  /**
   * Default constructor.
   */
  public HtmlDocumentBuilderFactory () {
    super();
  }

  /**
   * The only attribute recognized and used by the {@code HtmlDocumentBuilderFactory} is the
   * {@link #ATTRIBUTE_TARGET_NAMESPACE} attribute. The Java 7 standard attributes {@code
   * "http://javax.xml.XMLConstants/property/accessExternalDTD"} and {@code
   * "http://javax.xml.XMLConstants/property/accessExternalSchema"} will always have the value {@code ""}. (Note that
   * {@link HtmlDocumentBuilder} will never access external DTDs or schemas.) All other attribute names will result in
   * an {@link IllegalArgumentException}.
   * 
   * @see #ATTRIBUTE_TARGET_NAMESPACE
   */
  @Override
  public Object getAttribute (final String name) {
    // FIXME: check name not null
    if (name.equals(ATTRIBUTE_TARGET_NAMESPACE)) {
      return mTargetNamespace.get();
    } else if (name.equals(ACCESS_EXTERNAL_DTD) || name.equals(ACCESS_EXTERNAL_SCHEMA)) {
      // TODO: this is currently ignored (AFAIK Tidy should never access any external resources, but this should be
      // checked!)
      return "";
    } else {
      throw new IllegalArgumentException("Unrecognized attribute name: '" + name + "'");
    }
  }

  /**
   * The only attribute recognized and used by the {@code HtmlDocumentBuilderFactory} is the
   * {@link #ATTRIBUTE_TARGET_NAMESPACE} attribute. The Java 7 standard attributes {@code
   * "http://javax.xml.XMLConstants/property/accessExternalDTD"} and {@code
   * "http://javax.xml.XMLConstants/property/accessExternalSchema"} are recognized but will always have the value
   * {@code ""}; values set using this method will simply be ignored. (Note that {@link HtmlDocumentBuilder} will never
   * access external DTDs or schemas.) All other attribute names will result in an {@link IllegalArgumentException}.
   * 
   * @see #ATTRIBUTE_TARGET_NAMESPACE
   */
  @Override
  public void setAttribute (final String name, final Object value) {
    // FIXME: check name not null
    if (name.equals(ATTRIBUTE_TARGET_NAMESPACE)) {
      final String tns = value == null ? null : value.toString().trim();
      mTargetNamespace.set(tns == null || tns.isEmpty() ? null : tns);
    } else if (name.equals(ACCESS_EXTERNAL_DTD) || name.equals(ACCESS_EXTERNAL_SCHEMA)) {
      // TODO: this is currently ignored (AFAIK Tidy should never access any external resources, but this should be
      // checked!)
    } else {
      throw new IllegalArgumentException("Unrecognized attribute name: '" + name + "'");
    }
  }

  /**
   * The only feature recognized (but currently ignored) by {@code HtmlDocumentBuilderFactory} is the
   * {@link XMLConstants#FEATURE_SECURE_PROCESSING}; it's value will always be {@code false}. All other feature names
   * will result in an {@link IllegalArgumentException}.
   */
  @Override
  public boolean getFeature (final String name) throws ParserConfigurationException {
    // FIXME: check name not null
    if (name.equals(XMLConstants.FEATURE_SECURE_PROCESSING)) {
      // TODO: This is currently ignored; check in what respect Tidy may or may not support secure processing!
      return false;
    } else {
      throw new ParserConfigurationException("Unrecognized feature name: '" + name + "'");
    }
  }

  /**
   * The only feature recognized (but currently ignored) by {@code HtmlDocumentBuilderFactory} is the
   * {@link XMLConstants#FEATURE_SECURE_PROCESSING}; it's value will always be {@code false} and values set using this
   * method will simply be ignored. All other feature names will result in an {@link IllegalArgumentException}.
   */
  @Override
  public void setFeature (String name, boolean value) throws ParserConfigurationException {
    // FIXME: check name not null
    if (name.equals(XMLConstants.FEATURE_SECURE_PROCESSING)) {
      // TODO: This is currently ignored; check in what respect Tidy may or may not support secure processing!
    } else {
      throw new ParserConfigurationException("Unrecognized feature name: '" + name + "'");
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public HtmlDocumentBuilder newDocumentBuilder () throws ParserConfigurationException {
    return new HtmlDocumentBuilder(mTargetNamespace.get());
  }
}
