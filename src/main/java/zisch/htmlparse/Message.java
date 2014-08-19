/*
 * Triggs u Gaeggs.
 */
package zisch.htmlparse;


import java.text.MessageFormat;
import java.util.Locale;


enum Message {
  /** Missing semicolon in named entity. */
  MISSING_SEMICOLON("Missing semicolon '';'' in entity ''{0}''."),

  /** Missing semicolon in numeric character reference. */
  MISSING_SEMICOLON_NCR("Missing semicolon '';'' in numeric character reference ''{0}''."),

  /** Unescaped ampersand or unknown entity. */
  UNKNOWN_ENTITY("Unescaped ampersand ''&'' or unknown entity ''{0}''."),

  /** Unescaped ampersand. */
  UNESCAPED_AMPERSAND("Unescaped ampersand ''&'' which should be written as ''&amp;''."),

  /** Named Entity {@code '&apos;'} is only defined in XML/XHTML. */
  APOS_UNDEFINED("Named Entity ''&apos;'' is only defined in XML/XHTML not in HTML."),

  /** Missing end tag. */
  MISSING_ENDTAG_FOR("Missing end tag </{0}>."),

  /** Missing end tag before <em>X</em>. */
  MISSING_ENDTAG_BEFORE("Missing end tag </{0}> before {1}."),

  /** Discarding unexpected <em>X</em>. */
  DISCARDING_UNEXPECTED("Discarding unexpected {0}."),

  /** Nested emphasis. */
  NESTED_EMPHASIS("Nested emphasis {0}."),

  /** Non-matching end tag. */
  NON_MATCHING_ENDTAG("Non-matching end tag; replacing unexpected {0} by </{1}>."),

  /** Tag not allowed in element content. */
  TAG_NOT_ALLOWED_IN("{0} isn''t allowed in <{1}> elements."),

  /** Missing start tag. */
  MISSING_STARTTAG("Missing start tag <{0}>."),

  /** Unexpected end tag. */
  UNEXPECTED_ENDTAG("Unexpected end tag </{0}>."),

  /** Using {@code <br>} in place of <em>X</em>. */
  USING_BR_INPLACE_OF("Using <br> in place of {0}."),

  /** Inserting implicit tag. */
  INSERTING_TAG("Inserting implicit <{0}>."),

  /** Missing quote for attribute value. */
  SUSPECTED_MISSING_QUOTE("Missing quote for attribute value."),

  /** Inserting missing {@code <title>} element. */
  MISSING_TITLE_ELEMENT("Inserting missing <title> element."),

  /** Repeated {@code <frameset>} element. */
  DUPLICATE_FRAMESET("Repeated <frameset> element."),

  /** <em>X</em> can't be nested. */
  CANT_BE_NESTED("{0} can''t be nested."),

  /** Replacing obsolete element. */
  OBSOLETE_ELEMENT("Replacing obsolete element {0} by {1}."),

  /** Element which is not approved by W3C. */
  PROPRIETARY_ELEMENT("{0} is not approved by W3C."),

  /** Element which is not recognized. */
  UNKNOWN_ELEMENT("{0} is not recognized."),

  /** Trimming empty element. */
  TRIM_EMPTY_ELEMENT("Trimming empty {0}."),

  /** Change start tag to end tag. */
  COERCE_TO_ENDTAG("<{0}> is probably intended as </{0}>."),

  /** Illegal nesting. */
  ILLEGAL_NESTING("{0} shouldn''t be nested."),

  /** <em>X</em> is not inside a {@code <noframes>} element. */
  NOFRAMES_CONTENT("{0} not inside <noframes> element."),

  /** Content occurs after end of {@code <body>}. */
  CONTENT_AFTER_BODY("Content occurs after end of <body>."),

  /** HTML doctype doesn't match content. */
  INCONSISTENT_VERSION("HTML doctype doesn''t match content."),

  /** Adjacent hyphens within comment. */
  MALFORMED_COMMENT("Adjacent hyphens within comment."),

  /** Expecting {@code '--'} or {@code '>'}. */
  BAD_COMMENT_CHARS("Expecting ''--'' or ''>''."),

  // /** Use MALFORMED_COMMENT! */
  // BAD_XML_COMMENT(""),

  /** {@code '</'} followed by letter not allowed here. */
  BAD_CDATA_CONTENT("''</'' followed by letter not allowed here."),

  /** HTML namespace doesn't match content. */
  INCONSISTENT_NAMESPACE("HTML namespace doesn't match content."),

  /** {@code <!DOCTYPE>} isn't allowed after the first element. */
  DOCTYPE_AFTER_TAGS("<!DOCTYPE> isn''t allowed after the first element."),

  /** Expected {@code 'html PUBLIC'} or {@code 'html SYSTEM'}. */
  MALFORMED_DOCTYPE("Expected ''html PUBLIC'' or ''html SYSTEM''."),

  /** End of file while parsing attributes. */
  UNEXPECTED_END_OF_FILE("End of file while parsing attributes {0}."),

  /** {@code 'SYSTEM'}, {@code 'PUBLIC'}, {@code 'W3C'}, {@code 'DTD'}, {@code 'EN'} must be upper case. */
  DTYPE_NOT_UPPER_CASE("''SYSTEM'', ''PUBLIC'', ''W3C'', ''DTD'', ''EN'' must be upper case."),

  /** Too many elements. */
  TOO_MANY_ELEMENTS("Too many {0} elements."),

  /** Unescaped element in {@code <pre>} content. */
  UNESCAPED_ELEMENT("Unescaped {0} in <pre> content."),

  /** Nested {@code <q>} elements, possible typo. */
  NESTED_QUOTATION("Nested <q> elements, possible typo."),

  /** Element not empty or not closed. */
  ELEMENT_NOT_EMPTY("{0} element not empty or not closed."),

  /** Specified input encoding does not match actual input encoding. */
  ENCODING_IO_CONFLICT("Specified input encoding ''{0}'' does not match actual input encoding ''{1}''."),

  /** FIXME: Missing resource property! */
  MIXED_CONTENT_IN_BLOCK("MIXED_CONTENT_IN_BLOCK"),

  /** Missing {@code <!DOCTYPE>} declaration. */
  MISSING_DOCTYPE("Missing <!DOCTYPE> declaration."),

  /** FIXME: Missing resource property! */
  SPACE_PRECEDING_XMLDECL("SPACE_PRECEDING_XMLDECL"),

  /** Too many elements in element. */
  TOO_MANY_ELEMENTS_IN("Too many {0} elements in <{1}>."),

  /** Unexpected end tag in element. */
  UNEXPECTED_ENDTAG_IN("Unexpected </{0}> in <{1}>."),

  /** Replacing element. */
  REPLACING_ELEMENT("Replacing element {0} with {1}."),

  /** FIXME: Missing resource property! */
  REPLACING_UNEX_ELEMENT("REPLACING_UNEX_ELEMENT"),

  /** FIXME: Missing resource property! */
  COERCE_TO_ENDTAG_WARN("COERCE_TO_ENDTAG_WARN"),

  /** Unknown attribute. */
  UNKNOWN_ATTRIBUTE("Unknown attribute ''{0}''."),

  /** Missing attribute. */
  MISSING_ATTRIBUTE("{0} lacks ''{1}'' attribute."),

  /** Missing attribute value. */
  MISSING_ATTR_VALUE("{0} attribute ''{1}'' lacks value."),

  /** Attribute has invalid value. */
  BAD_ATTRIBUTE_VALUE("{0} attribute ''{1}'' has invalid value ''{2}''."),

  /** Tag missing ending {@code '>'}. (FIXME: Methinks, this should be MISSING_GT instead of UNEXPECTED_GT!??) */
  UNEXPECTED_GT("{0} missing ''>'' for end of tag."),

  /** Proprietary attribute in element. */
  PROPRIETARY_ATTRIBUTE("{0} proprietary attribute ''{1}''."),

  /** Proprietary attribute value. */
  PROPRIETARY_ATTR_VALUE("{0} proprietary attribute value ''{1}''."),

  /** Dropping value for repeated attribute. */
  REPEATED_ATTRIBUTE("{0} dropping value ''{1}'' for repeated attribute ''{2}''."),

  /** Missing client-side image map. */
  MISSING_IMAGEMAP("{0} should use client-side image map."),

  /** Element has XML attribute. */
  XML_ATTRIBUTE_VALUE("{0} has XML attribute ''{1}''."),

  /** Attribute with missing trailing quote character. */
  MISSING_QUOTEMARK("''{0}'' attribute with missing trailing quote character."),

  /** Unexpected or duplicate quote character. */
  UNEXPECTED_QUOTEMARK("{0} unexpected or duplicate quote character."),

  /** Id and name attribute value mismatch. */
  ID_NAME_MISMATCH("{0} id and name attribute value mismatch."),

  /** URI reference contains a backslash. */
  BACKSLASH_IN_URI("{0} URI reference contains a backslash."),

  /** Converting backslash in URI to slash. */
  FIXED_BACKSLASH("{0} converting backslash in URI to slash."),

  /** Improperly escaped URI reference. */
  ILLEGAL_URI_REFERENCE("{0} improperly escaped URI reference."),

  /** Escaping malformed URI reference. */
  ESCAPED_ILLEGAL_URI("{0} escaping malformed URI reference."),

  /** Discarding newline in URI reference. */
  NEWLINE_IN_URI("{0} discarding newline in URI reference."),

  /** Anchor already defined. */
  ANCHOR_NOT_UNIQUE("{0} anchor ''{1}'' already defined."),

  /** No entities allowed in id attribute; discarding {@code '&'}. */
  ENTITY_IN_ID("No entities allowed in id attribute; discarding ''&''."),

  /** Joining values of repeated attribute. */
  JOINING_ATTRIBUTE("{0} joining values of repeated attribute ''{1}''."),

  /** Unexpected {@code '='}, expected attribute name. */
  UNEXPECTED_EQUALSIGN("{0} unexpected ''='', expected attribute name"),

  /** Attribute value must be lower case for XHTML. */
  ATTR_VALUE_NOT_LCASE("{0} attribute value ''{1}'' for ''{2}'' must be lower case for XHTML."),

  /** ID uses XML ID syntax. */
  XML_ID_SYNTAX("ID ''{0}'' uses XML ID syntax."),

  /** FIXME: Missing resource property! */
  INVALID_ATTRIBUTE("INVALID_ATTRIBUTE"),

  /** FIXME: Missing resource property! */
  BAD_ATTRIBUTE_VALUE_REPLACED("BAD_ATTRIBUTE_VALUE_REPLACED"),

  /** FIXME: Missing resource property! */
  INVALID_XML_ID("INVALID_XML_ID"),

  /** FIXME: Missing resource property! */
  UNEXPECTED_END_OF_FILE_ATTR("UNEXPECTED_END_OF_FILE_ATTR"),

  /**
   * It is unlikely that vendor-specific, system-dependent encodings work widely enough on the World Wide Web. You
   * should avoid using characters outside of US-ASCII in vendor specific encodings; instead you are recommended to use
   * named entities, e.g. {@code '&trade;}.
   */
  VENDOR_SPECIFIC_CHARS("Avoid using non-ASCII characters in the {0,choice,0#specified|1#Windows-1252|2#MacRoman} character encoding in favor of named entities, e.g. ''&trade;''."),

  /**
   * Character codes 128 to 159 ({@code U+0080} to {@code U+009F}) are not allowed in HTML. Even if they were, they
   * would likely be unprintable control characters. We assume you wanted to refer to a character with the same byte
   * value in the vendor encoding and replaced that reference with the Unicode equivalent.
   */
  INVALID_SGML_CHARS("Invalid control characters in the {0,choice,0#specified|1#Windows-1252|2#MacRoman} character encoding replaced with Unicode equivalent."),

  /** Replacing or discarding invalid UTF-8 bytes. */
  INVALID_UTF8("{0,choice,0#Replacing|1#Discarding} invalid UTF-8 bytes (char. code {1})."),

  /** Replacing or discarding invalid UTF-16 surrogate pair. */
  INVALID_UTF16("{0,choice,0#Replacing|1#Discarding} invalid UTF-16 surrogate pair (char. code {1})."),

  /** Specified input encoding does not match actual input encoding. */
  ENCODING_MISMATCH("Specified input encoding ''{0}'' does not match actual input encoding ''{1}''."),

  /**
   * URIs must be properly escaped, they must not contain unescaped characters below {@code U+0021} including the space
   * character and not above {@code U+007E}. Tidy escapes the URI for you as recommended by HTML 4.01 section B.2.1 and
   * XML 1.0 section 4.2.2. Some user agents use another algorithm to escape such URIs and some server-sided scripts
   * depend on that. If you want to depend on that, you must escape the URI by your own. For more information please
   * refer to <a href="http://www.w3.org/International/O-URL-and-ident.html">http://www.w3.org/International/O-URL
   * -and-ident.html</a>.
   */
  INVALID_URI("URI contains invalid characters."),

  /** Replacing or discarding invalid numeric character reference. */
  INVALID_NCR("{0,choice,0#Replacing|1#Discarding} invalid numeric character reference ''{1}''.");

  private// private final MessageType mType;
  private final String mDescription;

  private Message (/* final MessageType type, */final String description) {
    // assert type != null;
    assert !description.isEmpty();
    // mType = type;
    mDescription = description;
  }

  public String getDescription (final Object... params) {
    final MessageFormat mf = new MessageFormat(mDescription, Locale.US);
    return mf.format(params);
  }
}
