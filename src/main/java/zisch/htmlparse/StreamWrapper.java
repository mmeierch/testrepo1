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


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.xml.sax.SAXParseException;


/**
 * Input stream wrapper for the {@link Lexer}.
 * 
 * @author Zisch
 */
final class StreamWrapper {
  /** End of stream marker. */
  static final int END_OF_STREAM = -1;

  /** Number of characters kept in buffer. */
  private static final int CHARBUF_SIZE = 16;

  /** Character buffer. */
  private final int[] mCharbuf = new int[CHARBUF_SIZE];

  /** Actual position in buffer. */
  private int mBufpos = 0;

  private final String mPublicId;

  private final String mSystemId;

  /** Java input stream reader. */
  private final Reader mReader;

  /** Has end of stream been reached? */
  private boolean mEndOfStream = false;

  /** Has a char been pushed? */
  private boolean mPushed = false;

  /** Current column number. */
  private int mCurcol = 1;

  /** Last column. */
  private int mLastcol;

  /** Current line number. */
  private int mCurline = 1;

  /** Tab size in chars. */
  private final int mTabsize;

  private int mTabs;

  StreamWrapper (final String publicId, final String systemId, final InputStream stream, final String encoding,
          final int tabsize) throws UnsupportedEncodingException {
    // FIXME: check 'stream' and 'encoding' args!
    this(publicId, systemId, new InputStreamReader(stream, encoding), tabsize);
  }

  StreamWrapper (final String publicId, final String systemId, final Reader reader, final int tabsize) {
    // FIXME: check args!
    mPublicId = publicId;
    mSystemId = systemId;
    mReader = reader;
    mTabsize = tabsize;
  }

  int readCharFromStream () throws SAXParseException {
    try {
      final int c = mReader.read();
      if (c < 0) {
        mEndOfStream = true;
      }
      return c;
    } catch (final IOException exc) {
      throw createParseException("IOException while reading: " + exc, exc);
    }
  }

  public int readChar () throws SAXParseException {
    int c;

    if (mPushed) {
      c = mCharbuf[--(mBufpos)];
      if ((mBufpos) == 0) {
        mPushed = false;
      }

      if (c == '\n') {
        mCurcol = 1;
        mCurline++;
        return c;
      }

      mCurcol++;
      return c;
    }

    mLastcol = mCurcol;

    if (mTabs > 0) {
      mCurcol++;
      mTabs--;
      return ' ';
    }

    c = readCharFromStream();

    if (c < 0) {
      mEndOfStream = true;
      return END_OF_STREAM;
    }

    if (c == '\n') {
      mCurcol = 1;
      mCurline++;
      return c;
    } else if (c == '\r') // \r\n
    {
      c = readCharFromStream();
      if (c != '\n') {
        if (c != END_OF_STREAM) {
          ungetChar(c);
        }
        c = '\n';
      }
      mCurcol = 1;
      mCurline++;
      return c;
    }

    if (c == '\t') {
      mTabs = mTabsize - ((mCurcol - 1) % mTabsize) - 1;
      mCurcol++;
      c = ' ';
      return c;
    }

    mCurcol++;

    return c;
  }

  public void ungetChar (int c) {
    mPushed = true;
    if (mBufpos >= CHARBUF_SIZE) {
      // pop last element
      System.arraycopy(mCharbuf, 0, mCharbuf, 1, CHARBUF_SIZE - 1);
      mBufpos--;
    }
    mCharbuf[(mBufpos)++] = c;

    if (c == '\n') {
      --mCurline;
    }

    mCurcol = mLastcol;
  }

  public boolean isEndOfStream () {
    return mEndOfStream;
  }

  /**
   * Getter for <code>curcol</code>.
   * 
   * @return Returns the curcol.
   */
  public int getCurcol () {
    return mCurcol;
  }

  /**
   * Getter for <code>curline</code>.
   * 
   * @return Returns the curline.
   */
  public int getCurline () {
    return mCurline;
  }

  private SAXParseException createParseException (final String msg, final Exception cause) {
    return new SAXParseException(msg, mPublicId, mSystemId, mCurline, mCurcol, cause);
  }
}
