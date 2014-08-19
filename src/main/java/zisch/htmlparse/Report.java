/*
 *  Java HTML Tidy - JTidy
 *  HTML parser and pretty printer
 *
 *  Copyright (c) 1998-2000 World Wide Web Consortium (Massachusetts
 *  Institute of Technology, Institut National de Recherche en
 *  Informatique et en Automatique, Keio University). All Rights
 *  Reserved.
 *
 *  Contributing Author(s):
 *
 *     Dave Raggett <dsr@w3.org>
 *     Andy Quick <ac.quick@sympatico.ca> (translation to Java)
 *     Gary L Peskin <garyp@firstech.com> (Java development)
 *     Sami Lempinen <sami@lempinen.net> (release management)
 *     Fabrizio Giustina <fgiust at users.sourceforge.net>
 *
 *  The contributing author(s) would like to thank all those who
 *  helped with testing, bug fixes, and patience.  This wouldn't
 *  have been possible without all of you.
 *
 *  COPYRIGHT NOTICE:
 * 
 *  This software and documentation is provided "as is," and
 *  the copyright holders and contributing author(s) make no
 *  representations or warranties, express or implied, including
 *  but not limited to, warranties of merchantability or fitness
 *  for any particular purpose or that the use of the software or
 *  documentation will not infringe any third party patents,
 *  copyrights, trademarks or other rights. 
 *
 *  The copyright holders and contributing author(s) will not be
 *  liable for any direct, indirect, special or consequential damages
 *  arising out of any use of the software or documentation, even if
 *  advised of the possibility of such damage.
 *
 *  Permission is hereby granted to use, copy, modify, and distribute
 *  this source code, or portions hereof, documentation and executables,
 *  for any purpose, without fee, subject to the following restrictions:
 *
 *  1. The origin of this source code must not be misrepresented.
 *  2. Altered versions must be plainly marked as such and must
 *     not be misrepresented as being the original source.
 *  3. This Copyright notice may not be removed or altered from any
 *     source or altered source distribution.
 * 
 *  The copyright holders and contributing author(s) specifically
 *  permit, without fee, and encourage the use of this source code
 *  as a component for supporting the Hypertext Markup Language in
 *  commercial products. If you use this source code in a product,
 *  acknowledgment is not required but would be appreciated.
 *
 */
package zisch.htmlparse;


import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import zisch.htmlparse.TidyMessage.Level;


/**
 * Error/informational message reporter.
 * 
 * @author Zisch
 */
final class Report {

  // FIXME: move message enum(s) to separate classes!

  enum MessageType {
    ERROR, SUMMARY, ACCESSIBILITY_FLAW, PRESENTATION_FLAW, CHAR_ENCODING_ERROR, CHAR_MESSAGE
  }

  enum Summary {
    DOCTYPE_GIVEN_SUMMARY, REPORT_VERSION_SUMMARY, BADACCESS_SUMMARY, BADFORM_SUMMARY
  }

  enum AccessibilityFlaw {
    MISSING_IMAGE_ALT, MISSING_LINK_ALT, MISSING_SUMMARY, MISSING_IMAGE_MAP, USING_FRAMES, USING_NOFRAMES
  }

  enum PresentationFlaw {
    USING_SPACER, USING_LAYER, USING_NOBR, USING_FONT, USING_BODY
  }

  enum CharacterEncodingError {
    WINDOWS_CHARS, NON_ASCII, FOUND_UTF16
  }

  enum CharMessage {
    REPLACED_CHAR, DISCARDED_CHAR
  }

  /**
   * Resource bundle with messages.
   */
  private static final ResourceBundle res = ResourceBundle.getBundle(Report.class.getPackage().getName()
          + ".TidyMessages");

  /**
   * Printed in GNU Emacs messages.
   */
  private String currentFile;

  /**
   * message listener for error reporting.
   */
  private TidyMessageListener listener;

  private final ErrorHandler mErrorHandler;

  /**
   * Instantiated only in Tidy() constructor.
   */
  Report (final ErrorHandler errorHandler) {
    mErrorHandler = errorHandler;
  }

  /**
   * Generates a complete message for the warning/error. The message is composed by:
   * <ul>
   * <li>position in file</li>
   * <li>prefix for the error level (warning: | error:)</li>
   * <li>message read from ResourceBundle</li>
   * <li>optional parameters added to message using MessageFormat</li>
   * </ul>
   * 
   * @param errorCode tidy error code
   * @param lexer Lexer
   * @param message key for the ResourceBundle
   * @param params optional parameters added with MessageFormat
   * @param level message level. One of <code>TidyMessage.LEVEL_ERROR</code>, <code>TidyMessage.LEVEL_WARNING</code>,
   *          <code>TidyMessage.LEVEL_INFO</code>
   * @return formatted message
   * @throws MissingResourceException if <code>message</code> key is not available in jtidy resource bundle.
   * @see TidyMessage
   */
  protected String getMessage (final Message errorCode, final Locator locator, final String message,
          final Object[] params, final Level level) {
    final String resource = res.getString(message);

    String position;

    if (lexer != null && level != Level.SUMMARY) {
      position = getPosition(lexer);
    } else {
      position = "";
    }

    String prefix;

    if (level == Level.ERROR) {
      prefix = res.getString("error");
    } else if (level == Level.WARNING) {
      prefix = res.getString("warning");
    } else {
      prefix = "";
    }

    String messageString;

    if (params != null) {
      messageString = MessageFormat.format(resource, params);
    } else {
      messageString = resource;
    }

    if (listener != null) {
      TidyMessage msg = new TidyMessage(errorCode, (lexer != null) ? lexer.lines : 0, (lexer != null) ? lexer.columns
              : 0, level, messageString);
      listener.messageReceived(msg);
    }

    return position + prefix + messageString;
  }

  /**
   * Prints a message to lexer.errout after calling getMessage().
   * 
   * @param errorCode tidy error code
   * @param lexer Lexer
   * @param message key for the ResourceBundle
   * @param params optional parameters added with MessageFormat
   * @param level message level. One of <code>TidyMessage.LEVEL_ERROR</code>, <code>TidyMessage.LEVEL_WARNING</code>,
   *          <code>TidyMessage.LEVEL_INFO</code>
   * 
   * @throws SAXException if the error is fatal
   * 
   * @see TidyMessage
   */
  private void printMessage (final int errorCode, final Lexer lexer, final String message, final Object[] params,
          final Level level) throws SAXException {
    final String resource = getMessage(errorCode, lexer, message, params, level);
    handleMessage(level, resource, null);
  }

  /**
   * Prints a message to errout after calling getMessage(). Used when lexer is not yet defined.
   * 
   * @param errout PrintWriter
   * @param message key for the ResourceBundle
   * @param params optional parameters added with MessageFormat
   * @param level message level. One of <code>TidyMessage.LEVEL_ERROR</code>, <code>TidyMessage.LEVEL_WARNING</code>,
   *          <code>TidyMessage.LEVEL_INFO</code>
   * 
   * @throws SAXException if the error is fatal
   * 
   * @see TidyMessage
   */
  private void printMessage (final String message, final Object[] params, final Level level) throws SAXException {
    printMessage(-1, null, message, params, level);
  }

  /**
   * print version information.
   * 
   * @param p printWriter
   */
  public void showVersion (final PrintWriter p) {
    p.println(getMessage(-1, null, "version_summary", new Object[] { RELEASE_DATE_STRING }, Level.SUMMARY));
  }

  /**
   * Returns a formatted tag name handling start and ent tags, nulls, doctypes, and text.
   * 
   * @param tag Node
   * @return formatted tag name
   */
  private String getTagName (final Node tag) {
    if (tag != null) {
      if (tag.type == Node.START_TAG) {
        return "<" + tag.element + ">";
      } else if (tag.type == Node.END_TAG) {
        return "</" + tag.element + ">";
      } else if (tag.type == Node.DOCTYPE_TAG) {
        return "<!DOCTYPE>";
      } else if (tag.type == Node.TEXT_NODE) {
        return "plain text";
      } else {
        return tag.element;
      }
    }
    return "";
  }

  /**
   * Prints an "unknown option" error message. Lexer is not defined when this is called.
   * 
   * @param option unknown option name
   */
  public void unknownOption (final String option) {
    System.err.println(MessageFormat.format(res.getString("unknown_option"), new Object[] { option }));
  }

  /**
   * Prints a "bad argument" error message. Lexer is not defined when this is called.
   * 
   * @param key argument name
   * @param value bad argument value
   */
  public void badArgument (final String key, final String value) {
    System.err.println(MessageFormat.format(res.getString("bad_argument"), new Object[] { value, key }));
  }

  /**
   * Returns a formatted String describing the current position in file.
   * 
   * @param lexer Lexer
   * @return String position ("line:column")
   */
  private String getPosition (final Lexer lexer) {
    // Change formatting to be parsable by GNU Emacs
    if (lexer.configuration.emacs) {
      return MessageFormat.format(res.getString("emacs_format"), new Object[] { this.currentFile,
              new Integer(lexer.lines), new Integer(lexer.columns) })
              + " ";
    } else {
      // traditional format
      return MessageFormat.format(res.getString("line_column"), new Object[] { new Integer(lexer.lines),
              new Integer(lexer.columns) });
    }
  }

  /**
   * Prints encoding error messages.
   * 
   * @param lexer Lexer
   * @param code error code
   * @param c invalid char
   * 
   * @throws SAXException if the error is fatal
   */
  public void encodingError (final Lexer lexer, final int code, final int c) throws SAXException {
    lexer.warnings++;

    if (lexer.errors > lexer.configuration.showErrors) // keep quiet after <showErrors> errors
    {
      return;
    }

    if (lexer.configuration.showWarnings) {
      String buf = Integer.toHexString(c);

      // An encoding mismatch is currently treated as a non-fatal error
      if ((code & ~DISCARDED_CHAR) == ENCODING_MISMATCH) {
        // actual encoding passed in "c"
        lexer.badChars |= ENCODING_MISMATCH;
        printMessage(code, lexer, "encoding_mismatch", new Object[] { lexer.configuration.getInCharEncodingName(),
                ParsePropertyImpl.CHAR_ENCODING.getFriendlyName(null, new Integer(c), lexer.configuration) },
                Level.WARNING);
      } else if ((code & ~DISCARDED_CHAR) == VENDOR_SPECIFIC_CHARS) {
        lexer.badChars |= VENDOR_SPECIFIC_CHARS;
        printMessage(code, lexer, "invalid_char", new Object[] { new Integer(code & DISCARDED_CHAR), buf },
                Level.WARNING);
      } else if ((code & ~DISCARDED_CHAR) == INVALID_SGML_CHARS) {
        lexer.badChars |= INVALID_SGML_CHARS;
        printMessage(code, lexer, "invalid_char", new Object[] { new Integer(code & DISCARDED_CHAR), buf },
                Level.WARNING);
      } else if ((code & ~DISCARDED_CHAR) == INVALID_UTF8) {
        lexer.badChars |= INVALID_UTF8;
        printMessage(code, lexer, "invalid_utf8", new Object[] { new Integer(code & DISCARDED_CHAR), buf },
                Level.WARNING);
      }

      else if ((code & ~DISCARDED_CHAR) == INVALID_UTF16) {
        lexer.badChars |= INVALID_UTF16;
        printMessage(code, lexer, "invalid_utf16", new Object[] { new Integer(code & DISCARDED_CHAR), buf },
                Level.WARNING);

      }

      else if ((code & ~DISCARDED_CHAR) == INVALID_NCR) {
        lexer.badChars |= INVALID_NCR;
        printMessage(code, lexer, "invalid_ncr", new Object[] { new Integer(code & DISCARDED_CHAR), buf },
                Level.WARNING);
      }

    }
  }

  /**
   * Prints entity error messages.
   * 
   * @param lexer Lexer
   * @param code error code
   * @param entity invalid entity String
   * @param c invalid char
   * 
   * @throws SAXException if the error is fatal
   */
  public void entityError (Lexer lexer, short code, String entity, int c) throws SAXException {
    lexer.warnings++;

    if (lexer.errors > lexer.configuration.showErrors) // keep quiet after <showErrors> errors
    {
      return;
    }

    if (lexer.configuration.showWarnings) {
      switch (code) {
        case MISSING_SEMICOLON:
          printMessage(code, lexer, "missing_semicolon", new Object[] { entity }, Level.WARNING);
          break;
        case MISSING_SEMICOLON_NCR:
          printMessage(code, lexer, "missing_semicolon_ncr", new Object[] { entity }, Level.WARNING);
          break;
        case UNKNOWN_ENTITY:
          printMessage(code, lexer, "unknown_entity", new Object[] { entity }, Level.WARNING);
          break;
        case UNESCAPED_AMPERSAND:
          printMessage(code, lexer, "unescaped_ampersand", null, Level.WARNING);
          break;
        case APOS_UNDEFINED:
          printMessage(code, lexer, "apos_undefined", null, Level.WARNING);
          break;
        default:
          // should not reach here
          break;
      }
    }
  }

  /**
   * Prints error messages for attributes.
   * 
   * @param lexer Lexer
   * @param node current tag
   * @param attribute attribute
   * @param code error code
   * 
   * @throws SAXException if the error is fatal
   */
  public void attrError (Lexer lexer, Node node, AttVal attribute, short code) throws SAXException {
    if (code == UNEXPECTED_GT) {
      lexer.errors++;
    } else {
      lexer.warnings++;
    }

    if (lexer.errors > lexer.configuration.showErrors) // keep quiet after <showErrors> errors
    {
      return;
    }

    if (code == UNEXPECTED_GT) // error
    {
      printMessage(code, lexer, "unexpected_gt", new Object[] { getTagName(node) }, Level.ERROR);
    }

    if (!lexer.configuration.showWarnings) // warnings
    {
      return;
    }

    switch (code) {
      case UNKNOWN_ATTRIBUTE:
        printMessage(code, lexer, "unknown_attribute", new Object[] { attribute.attribute }, Level.WARNING);
        break;

      case MISSING_ATTRIBUTE:
        printMessage(code, lexer, "missing_attribute", new Object[] { getTagName(node), attribute.attribute },
                Level.WARNING);
        break;

      case MISSING_ATTR_VALUE:
        printMessage(code, lexer, "missing_attr_value", new Object[] { getTagName(node), attribute.attribute },
                Level.WARNING);
        break;

      case MISSING_IMAGEMAP:
        printMessage(code, lexer, "missing_imagemap", new Object[] { getTagName(node) }, Level.WARNING);
        lexer.badAccess |= MISSING_IMAGE_MAP;
        break;

      case BAD_ATTRIBUTE_VALUE:
        printMessage(code, lexer, "bad_attribute_value", new Object[] { getTagName(node), attribute.attribute,
                attribute.value }, Level.WARNING);
        break;

      case XML_ID_SYNTAX:
        printMessage(code, lexer, "xml_id_sintax", new Object[] { getTagName(node), attribute.attribute },
                Level.WARNING);
        break;

      case XML_ATTRIBUTE_VALUE:
        printMessage(code, lexer, "xml_attribute_value", new Object[] { getTagName(node), attribute.attribute },
                Level.WARNING);
        break;

      case UNEXPECTED_QUOTEMARK:
        printMessage(code, lexer, "unexpected_quotemark", new Object[] { getTagName(node) }, Level.WARNING);
        break;

      case MISSING_QUOTEMARK:
        printMessage(code, lexer, "missing_quotemark", new Object[] { getTagName(node) }, Level.WARNING);
        break;

      case REPEATED_ATTRIBUTE:
        printMessage(code, lexer, "repeated_attribute", new Object[] { getTagName(node), attribute.value,
                attribute.attribute }, Level.WARNING);
        break;

      case PROPRIETARY_ATTR_VALUE:
        printMessage(code, lexer, "proprietary_attr_value", new Object[] { getTagName(node), attribute.value },
                Level.WARNING);
        break;

      case PROPRIETARY_ATTRIBUTE:
        printMessage(code, lexer, "proprietary_attribute", new Object[] { getTagName(node), attribute.attribute },
                Level.WARNING);
        break;

      case UNEXPECTED_END_OF_FILE:
        // on end of file adjust reported position to end of input
        lexer.lines = lexer.in.getCurline();
        lexer.columns = lexer.in.getCurcol();
        printMessage(code, lexer, "unexpected_end_of_file", new Object[] { getTagName(node) }, Level.WARNING);
        break;

      case ID_NAME_MISMATCH:
        printMessage(code, lexer, "id_name_mismatch", new Object[] { getTagName(node) }, Level.WARNING);
        break;

      case BACKSLASH_IN_URI:
        printMessage(code, lexer, "backslash_in_uri", new Object[] { getTagName(node) }, Level.WARNING);
        break;

      case FIXED_BACKSLASH:
        printMessage(code, lexer, "fixed_backslash", new Object[] { getTagName(node) }, Level.WARNING);
        break;

      case ILLEGAL_URI_REFERENCE:
        printMessage(code, lexer, "illegal_uri_reference", new Object[] { getTagName(node) }, Level.WARNING);
        break;

      case ESCAPED_ILLEGAL_URI:
        printMessage(code, lexer, "escaped_illegal_uri", new Object[] { getTagName(node) }, Level.WARNING);
        break;

      case NEWLINE_IN_URI:
        printMessage(code, lexer, "newline_in_uri", new Object[] { getTagName(node) }, Level.WARNING);
        break;

      case ANCHOR_NOT_UNIQUE:
        printMessage(code, lexer, "anchor_not_unique", new Object[] { getTagName(node), attribute.value },
                Level.WARNING);
        break;

      case ENTITY_IN_ID:
        printMessage(code, lexer, "entity_in_id", null, Level.WARNING);
        break;

      case JOINING_ATTRIBUTE:
        printMessage(code, lexer, "joining_attribute", new Object[] { getTagName(node), attribute.attribute },
                Level.WARNING);
        break;

      case UNEXPECTED_EQUALSIGN:
        printMessage(code, lexer, "expected_equalsign", new Object[] { getTagName(node) }, Level.WARNING);
        break;

      case ATTR_VALUE_NOT_LCASE:
        printMessage(code, lexer, "attr_value_not_lcase", new Object[] { getTagName(node), attribute.value,
                attribute.attribute }, Level.WARNING);
        break;

      default:
        break;
    }
  }

  /**
   * Prints warnings.
   * 
   * @param lexer Lexer
   * @param element parent/missing tag
   * @param node current tag
   * @param code error code
   * 
   * @throws SAXException if the error is fatal
   */
  public void warning (Lexer lexer, Node element, Node node, short code) throws SAXException {

    TagTable tt = lexer.configuration.tt;
    if (!((code == DISCARDING_UNEXPECTED) && lexer.badForm != 0)) // lexer->errors++; already done in BadForm()
    {
      lexer.warnings++;
    }

    // keep quiet after <showErrors> errors
    if (lexer.errors > lexer.configuration.showErrors) {
      return;
    }

    if (lexer.configuration.showWarnings) {
      switch (code) {
        case MISSING_ENDTAG_FOR:
          printMessage(code, lexer, "missing_endtag_for", new Object[] { element.element }, Level.WARNING);
          break;

        case MISSING_ENDTAG_BEFORE:
          printMessage(code, lexer, "missing_endtag_before", new Object[] { element.element, getTagName(node) },
                  Level.WARNING);
          break;

        case DISCARDING_UNEXPECTED:
          if (lexer.badForm == 0) {
            // the case for when this is an error not a warning, is handled later
            printMessage(code, lexer, "discarding_unexpected", new Object[] { getTagName(node) }, Level.WARNING);
          }
          break;

        case NESTED_EMPHASIS:
          printMessage(code, lexer, "nested_emphasis", new Object[] { getTagName(node) }, Level.INFO);
          break;

        case COERCE_TO_ENDTAG:
          printMessage(code, lexer, "coerce_to_endtag", new Object[] { element.element }, Level.INFO);
          break;

        case NON_MATCHING_ENDTAG:
          printMessage(code, lexer, "non_matching_endtag", new Object[] { getTagName(node), element.element },
                  Level.WARNING);
          break;

        case TAG_NOT_ALLOWED_IN:
          printMessage(code, lexer, "tag_not_allowed_in", new Object[] { getTagName(node), element.element },
                  Level.WARNING);
          break;

        case DOCTYPE_AFTER_TAGS:
          printMessage(code, lexer, "doctype_after_tags", null, Level.WARNING);
          break;

        case MISSING_STARTTAG:
          printMessage(code, lexer, "missing_starttag", new Object[] { node.element }, Level.WARNING);
          break;

        case UNEXPECTED_ENDTAG:
          if (element != null) {
            printMessage(code, lexer, "unexpected_endtag_in", new Object[] { node.element, element.element },
                    Level.WARNING);
          } else {
            printMessage(code, lexer, "unexpected_endtag", new Object[] { node.element }, Level.WARNING);
          }
          break;

        case TOO_MANY_ELEMENTS:
          if (element != null) {
            printMessage(code, lexer, "too_many_elements_in", new Object[] { node.element, element.element },
                    Level.WARNING);
          } else {
            printMessage(code, lexer, "too_many_elements", new Object[] { node.element }, Level.WARNING);
          }
          break;

        case USING_BR_INPLACE_OF:
          printMessage(code, lexer, "using_br_inplace_of", new Object[] { getTagName(node) }, Level.WARNING);
          break;

        case INSERTING_TAG:
          printMessage(code, lexer, "inserting_tag", new Object[] { node.element }, Level.WARNING);
          break;

        case CANT_BE_NESTED:
          printMessage(code, lexer, "cant_be_nested", new Object[] { getTagName(node) }, Level.WARNING);
          break;

        case PROPRIETARY_ELEMENT:
          printMessage(code, lexer, "proprietary_element", new Object[] { getTagName(node) }, Level.WARNING);

          if (node.tag == tt.tagLayer) {
            lexer.badLayout |= USING_LAYER;
          } else if (node.tag == tt.tagSpacer) {
            lexer.badLayout |= USING_SPACER;
          } else if (node.tag == tt.tagNobr) {
            lexer.badLayout |= USING_NOBR;
          }
          break;

        case OBSOLETE_ELEMENT:
          if (element.tag != null && (element.tag.model & Dict.CM_OBSOLETE) != 0) {
            printMessage(code, lexer, "obsolete_element", new Object[] { getTagName(element), getTagName(node) },
                    Level.WARNING);
          } else {
            printMessage(code, lexer, "replacing_element", new Object[] { getTagName(element), getTagName(node) },
                    Level.WARNING);
          }
          break;

        case UNESCAPED_ELEMENT:
          printMessage(code, lexer, "unescaped_element", new Object[] { getTagName(element) }, Level.WARNING);
          break;

        case TRIM_EMPTY_ELEMENT:
          printMessage(code, lexer, "trim_empty_element", new Object[] { getTagName(element) }, Level.WARNING);
          break;

        case MISSING_TITLE_ELEMENT:
          printMessage(code, lexer, "missing_title_element", null, Level.WARNING);
          break;

        case ILLEGAL_NESTING:
          printMessage(code, lexer, "illegal_nesting", new Object[] { getTagName(element) }, Level.WARNING);
          break;

        case NOFRAMES_CONTENT:
          printMessage(code, lexer, "noframes_content", new Object[] { getTagName(node) }, Level.WARNING);
          break;

        case INCONSISTENT_VERSION:
          printMessage(code, lexer, "inconsistent_version", null, Level.WARNING);
          break;

        case MALFORMED_DOCTYPE:
          printMessage(code, lexer, "malformed_doctype", null, Level.WARNING);
          break;

        case CONTENT_AFTER_BODY:
          printMessage(code, lexer, "content_after_body", null, Level.WARNING);
          break;

        case MALFORMED_COMMENT:
          printMessage(code, lexer, "malformed_comment", null, Level.WARNING);
          break;

        case BAD_COMMENT_CHARS:
          printMessage(code, lexer, "bad_comment_chars", null, Level.WARNING);
          break;

        case BAD_XML_COMMENT:
          printMessage(code, lexer, "bad_xml_comment", null, Level.WARNING);
          break;

        case BAD_CDATA_CONTENT:
          printMessage(code, lexer, "bad_cdata_content", null, Level.WARNING);
          break;

        case INCONSISTENT_NAMESPACE:
          printMessage(code, lexer, "inconsistent_namespace", null, Level.WARNING);
          break;

        case DTYPE_NOT_UPPER_CASE:
          printMessage(code, lexer, "dtype_not_upper_case", null, Level.WARNING);
          break;

        case UNEXPECTED_END_OF_FILE:
          // on end of file adjust reported position to end of input
          lexer.lines = lexer.in.getCurline();
          lexer.columns = lexer.in.getCurcol();
          printMessage(code, lexer, "unexpected_end_of_file", new Object[] { getTagName(element) }, Level.WARNING);
          break;

        case NESTED_QUOTATION:
          printMessage(code, lexer, "nested_quotation", null, Level.WARNING);
          break;

        case ELEMENT_NOT_EMPTY:
          printMessage(code, lexer, "element_not_empty", new Object[] { getTagName(element) }, Level.WARNING);
          break;

        case MISSING_DOCTYPE:
          printMessage(code, lexer, "missing_doctype", null, Level.WARNING);
          break;

        default:
          break;
      }
    }

    if ((code == DISCARDING_UNEXPECTED) && lexer.badForm != 0) {
      // the case for when this is a warning not an error, is handled earlier
      printMessage(code, lexer, "discarding_unexpected", new Object[] { getTagName(node) }, Level.ERROR);
    }

  }

  /**
   * Prints errors.
   * 
   * @param lexer Lexer
   * @param element parent/missing tag
   * @param node current tag
   * @param code error code
   * 
   * @throws SAXException if the error is fatal
   */
  public void error (Lexer lexer, Node element, Node node, short code) throws SAXException {
    lexer.errors++;

    // keep quiet after <showErrors> errors
    if (lexer.errors > lexer.configuration.showErrors) {
      return;
    }

    if (code == SUSPECTED_MISSING_QUOTE) {
      printMessage(code, lexer, "suspected_missing_quote", null, Level.ERROR);
    } else if (code == DUPLICATE_FRAMESET) {
      printMessage(code, lexer, "duplicate_frameset", null, Level.ERROR);
    } else if (code == UNKNOWN_ELEMENT) {
      printMessage(code, lexer, "unknown_element", new Object[] { getTagName(node) }, Level.ERROR);
    } else if (code == UNEXPECTED_ENDTAG) {
      if (element != null) {
        printMessage(code, lexer, "unexpected_endtag_in", new Object[] { node.element, element.element }, Level.ERROR);
      } else {
        printMessage(code, lexer, "unexpected_endtag", new Object[] { node.element }, Level.ERROR);
      }
    }
  }

  /**
   * Prints error summary.
   * 
   * @param lexer Lexer
   * 
   * @throws SAXException if the error is fatal
   */
  public void errorSummary (final Lexer lexer) throws SAXException {
    // adjust badAccess to that its null if frames are ok
    if ((lexer.badAccess & (USING_FRAMES | USING_NOFRAMES)) != 0) {
      if (!(((lexer.badAccess & USING_FRAMES) != 0) && ((lexer.badAccess & USING_NOFRAMES) == 0))) {
        lexer.badAccess &= ~(USING_FRAMES | USING_NOFRAMES);
      }
    }
    if (lexer.badChars != 0) {
      if ((lexer.badChars & VENDOR_SPECIFIC_CHARS) != 0) {
        int encodingChoiche = 0;

        if ("Cp1252".equals(lexer.configuration.getInCharEncodingName())) {
          encodingChoiche = 1;
        } else if ("MacRoman".equals(lexer.configuration.getInCharEncodingName())) {
          encodingChoiche = 2;
        }

        printMessage(VENDOR_SPECIFIC_CHARS, lexer, "vendor_specific_chars_summary", new Object[] { new Integer(
                encodingChoiche) }, Level.SUMMARY);
      }

      if ((lexer.badChars & INVALID_SGML_CHARS) != 0 || (lexer.badChars & INVALID_NCR) != 0) {
        int encodingChoiche = 0;

        if ("Cp1252".equals(lexer.configuration.getInCharEncodingName())) {
          encodingChoiche = 1;
        } else if ("MacRoman".equals(lexer.configuration.getInCharEncodingName())) {
          encodingChoiche = 2;
        }

        printMessage(INVALID_SGML_CHARS, lexer, "invalid_sgml_chars_summary", new Object[] { new Integer(
                encodingChoiche) }, Level.SUMMARY);
      }

      if ((lexer.badChars & INVALID_UTF8) != 0) {
        printMessage(INVALID_UTF8, lexer, "invalid_utf8_summary", null, Level.SUMMARY);
      }

      if ((lexer.badChars & INVALID_UTF16) != 0) {
        printMessage(INVALID_UTF16, lexer, "invalid_utf16_summary", null, Level.SUMMARY);
      }

      if ((lexer.badChars & INVALID_URI) != 0) {
        printMessage(INVALID_URI, lexer, "invaliduri_summary", null, Level.SUMMARY);
      }
    }

    if (lexer.badForm != 0) {
      printMessage(BADFORM_SUMMARY, lexer, "badform_summary", null, Level.SUMMARY);
    }

    if (lexer.badAccess != 0) {
      if ((lexer.badAccess & MISSING_SUMMARY) != 0) {
        printMessage(MISSING_SUMMARY, lexer, "badaccess_missing_summary", null, Level.SUMMARY);
      }

      if ((lexer.badAccess & MISSING_IMAGE_ALT) != 0) {
        printMessage(MISSING_IMAGE_ALT, lexer, "badaccess_missing_image_alt", null, Level.SUMMARY);
      }

      if ((lexer.badAccess & MISSING_IMAGE_MAP) != 0) {
        printMessage(MISSING_IMAGE_MAP, lexer, "badaccess_missing_image_map", null, Level.SUMMARY);
      }

      if ((lexer.badAccess & MISSING_LINK_ALT) != 0) {
        printMessage(MISSING_LINK_ALT, lexer, "badaccess_missing_link_alt", null, Level.SUMMARY);
      }

      if (((lexer.badAccess & USING_FRAMES) != 0) && ((lexer.badAccess & USING_NOFRAMES) == 0)) {
        printMessage(USING_FRAMES, lexer, "badaccess_frames", null, Level.SUMMARY);
      }

      printMessage(BADACCESS_SUMMARY, lexer, "badaccess_summary", new Object[] { ACCESS_URL }, Level.SUMMARY);
    }

    if (lexer.badLayout != 0) {
      if ((lexer.badLayout & USING_LAYER) != 0) {
        printMessage(USING_LAYER, lexer, "badlayout_using_layer", null, Level.SUMMARY);
      }

      if ((lexer.badLayout & USING_SPACER) != 0) {
        printMessage(USING_SPACER, lexer, "badlayout_using_spacer", null, Level.SUMMARY);
      }

      if ((lexer.badLayout & USING_FONT) != 0) {
        printMessage(USING_FONT, lexer, "badlayout_using_font", null, Level.SUMMARY);
      }

      if ((lexer.badLayout & USING_NOBR) != 0) {
        printMessage(USING_NOBR, lexer, "badlayout_using_nobr", null, Level.SUMMARY);
      }

      if ((lexer.badLayout & USING_BODY) != 0) {
        printMessage(USING_BODY, lexer, "badlayout_using_body", null, Level.SUMMARY);
      }
    }
  }

  /**
   * Prints the "unknown option" message.
   * 
   * @param c invalid option char
   * 
   * @throws SAXException if the error is fatal
   */
  public void unknownOption (final char c) throws SAXException {
    printMessage("unrecognized_option", new Object[] { new String(new char[] { c }) }, Level.ERROR);
  }

  /**
   * Prints the "unknown file" message.
   * 
   * @param file invalid file name
   * 
   * @throws SAXException if the error is fatal
   */
  public void unknownFile (final String file) throws SAXException {
    printMessage("unknown_file", new Object[] { "Tidy", file }, Level.ERROR);
  }

  /**
   * Prints the "needs author intervention" message.
   * 
   * @throws SAXException if the error is fatal
   */
  public void needsAuthorIntervention () throws SAXException {
    printMessage("needs_author_intervention", null, Level.SUMMARY);
  }

  /**
   * Prints the "missing body" message.
   * 
   * @throws SAXException if the error is fatal
   */
  public void missingBody () throws SAXException {
    printMessage("missing_body", null, Level.ERROR);
  }

  /**
   * Prints the number of generated slides.
   * 
   * @param errout PrintWriter
   * @param count slides count
   * 
   * @throws SAXException if the error is fatal
   */
  public void reportNumberOfSlides (PrintWriter errout, int count) throws SAXException {
    printMessage("slides_found", new Object[] { new Integer(count) }, Level.SUMMARY);
  }

  /**
   * Prints tidy general info.
   * 
   * @throws SAXException if the error is fatal
   */
  public void generalInfo () throws SAXException {
    printMessage("general_info", null, Level.SUMMARY);
  }

  /**
   * Sets the current file name.
   * 
   * @param filename current file.
   */
  public void setFilename (final String filename) {
    this.currentFile = filename; // for use with Gnu Emacs
  }

  /**
   * Prints information for html version in input file.
   * 
   * @param lexer Lexer
   * @param filename file name
   * @param doctype doctype Node
   * 
   * @throws SAXException if the error is fatal
   */
  public void reportVersion (final Lexer lexer, final String filename, final Node doctype) throws SAXException {
    int i, c;
    int state = 0;
    String vers = lexer.htmlVersionName();
    int[] cc = new int[1];

    // adjust reported position to first line
    lexer.lines = 1;
    lexer.columns = 1;

    if (doctype != null) {

      StringBuffer doctypeBuffer = new StringBuffer();
      for (i = doctype.start; i < doctype.end; ++i) {
        c = doctype.textarray[i];

        // look for UTF-8 multibyte character
        if (c < 0) {
          i += PPrint.getUTF8(doctype.textarray, i, cc);
          c = cc[0];
        }

        if (c == '"') {
          ++state;
        } else if (state == 1) {
          doctypeBuffer.append((char) c);
        }
      }

      printMessage(DOCTYPE_GIVEN_SUMMARY, lexer, "doctype_given", new Object[] { filename, doctypeBuffer },
              Level.SUMMARY);
    }

    printMessage(REPORT_VERSION_SUMMARY, lexer, "report_version", new Object[] { filename,
            (vers != null ? vers : "HTML proprietary") }, Level.SUMMARY);
  }

  /**
   * Prints the number of error/warnings found.
   * 
   * @param lexer Lexer
   * 
   * @throws SAXException if the error is fatal
   */
  public void reportNumWarnings (final Lexer lexer) throws SAXException {
    if (lexer.warnings > 0 || lexer.errors > 0) {
      printMessage("num_warnings", new Object[] { new Integer(lexer.warnings), new Integer(lexer.errors) },
              Level.SUMMARY);
    } else {
      printMessage("no_warnings", null, Level.SUMMARY);
    }
  }

  /**
   * Prints tidy help.
   * 
   * @param out PrintWriter
   */
  public void helpText (PrintWriter out) {
    out.println(getMessage(-1, null, "help_text", new Object[] { "Tidy", RELEASE_DATE_STRING }, Level.SUMMARY));
  }

  /**
   * Prints the "bad tree" message.
   * 
   * @throws SAXException if the error is fatal
   */
  public void badTree () throws SAXException {
    printMessage("bad_tree", null, Level.ERROR);
  }

  /**
   * Adds a message listener.
   * 
   * @param l TidyMessageListener
   */
  public void addMessageListener (final TidyMessageListener l) {
    this.listener = l;
  }

  private void handleMessage (final Level l, final String msg, final Exception exc) throws SAXException {
    if (mErrorHandler == null) {
      switch (l == null ? Level.ERROR.getCode() : l.getCode()) {
        case 3:
          mErrorHandler.error(makeMessageException(msg, exc)); // or fatal error!?
          break;

        case 2:
          mErrorHandler.warning(makeMessageException(msg, exc));
          break;

        default:
          // we currently ignore everything which is neither an error nor a warning
      }
    }
  }

  private SAXParseException makeMessageException (final String msg, final Exception exc) {
    // TODO: should provide a locator!
    return new SAXParseException(msg, null, exc);
  }
}
