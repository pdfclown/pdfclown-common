/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Strings.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

/**
 * String utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Strings {
  /**
   * Flag value used by methods like {@link String#indexOf(String)} to represent absence of searched
   * occurrence.
   */
  public static final int INDEX__NOT_FOUND = -1;

  public static final char ANGLE_BRACKET_CLOSE = '>';
  public static final char ANGLE_BRACKET_OPEN = '<';
  public static final char APOSTROPHE = '\'';
  public static final char BACKSLASH = '\\';
  public static final char BACKTICK = '`';
  public static final char COLON = ':';
  public static final char COMMA = ',';
  /**
   * Carriage-return character.
   */
  public static final char CR = '\r';
  public static final char CURLY_BRACE_CLOSE = '}';
  public static final char CURLY_BRACE_OPEN = '{';
  public static final char DOT = '.';
  /**
   * Double quote (aka quotation mark).
   */
  public static final char DQUOTE = '\"';
  public static final char HYPHEN = '-';
  /**
   * Greater-than character.
   */
  public static final char GT = ANGLE_BRACKET_CLOSE;
  /**
   * Line-feed character.
   */
  public static final char LF = '\n';
  /**
   * Underscore character.
   */
  public static final char LOW_LINE = '_';
  /**
   * Less-than character.
   */
  public static final char LT = ANGLE_BRACKET_OPEN;
  public static final char MINUS = HYPHEN;
  /**
   * <a href="https://en.wikipedia.org/wiki/Non-breaking_space">Non-breaking space</a> (aka hard
   * space) character preventing automatic line break at its position. In some formats, including
   * HTML, it also prevents consecutive whitespace characters from collapsing into a single space.
   */
  public static final char NBSP = 160;
  public static final char PERCENT = '%';
  /**
   * Vertical bar.
   */
  public static final char PIPE = '|';
  public static final char PLUS = '+';
  public static final char ROUND_BRACKET_CLOSE = ')';
  public static final char ROUND_BRACKET_OPEN = '(';
  public static final char SEMICOLON = ';';
  public static final char SLASH = '/';
  public static final char SOFT_HYPHEN = '\u00ad';
  public static final char SPACE = ' ';
  public static final char SQUARE_BRACKET_CLOSE = ']';
  public static final char SQUARE_BRACKET_OPEN = '[';
  /**
   * Single quote (aka apostrophe).
   */
  public static final char SQUOTE = APOSTROPHE;
  public static final char STAR = '*';
  public static final char TAB = '\t';
  public static final char UNDERSCORE = LOW_LINE;

  /**
   * Empty string.
   */
  public static final String EMPTY = "";
  /**
   * System-dependent line separator ({@link System#lineSeparator()} alias).
   */
  public static final String EOL = System.lineSeparator();
  /**
   * String representation of literal {@code null}.
   */
  public static final String NULL = "null";
  /**
   * Empty string, used as a marker to conveniently force the compiler to treat the following
   * concatenated character as a string.
   * <p>
   * Example:
   * </p>
   * <pre>
   *String str = S + HYPHEN;</pre>
   */
  public static final String S = EMPTY;

  /**
   * <a href="https://en.wikipedia.org/wiki/Ellipsis">Common (aka Associated Press-style)
   * ellipsis</a>.
   */
  public static final String ELLIPSIS = S + DOT + DOT + DOT;
  /**
   * <a href="https://en.wikipedia.org/wiki/Ellipsis">Chicago-style ellipsis</a>.
   */
  public static final String ELLIPSIS__CHICAGO = S + DOT + NBSP + DOT + NBSP + DOT;

  /**
   * Empty string array.
   */
  public static final String[] STR_ARRAY__EMPTY = new String[0];

  /**
   * Ensures the given string doesn't exceed the given limits; otherwise, replaces the exceeding
   * substring with a standard ellipsis.
   *
   * @param value
   *          String to clip.
   * @param maxLineCount
   *          Maximum number of lines.
   * @param averageLineLength
   *          Average line length (used along with {@code maxLineCount} to calculate the overall
   *          maximum string length).
   * @see StringUtils#abbreviate(String, int)
   */
  public static String abbreviateMultiline(String value, int maxLineCount, int averageLineLength) {
    return abbreviateMultiline(value, maxLineCount, averageLineLength, ELLIPSIS);
  }

  /**
   * Ensures the given string doesn't exceed the given limits; otherwise, replaces the exceeding
   * substring with a marker.
   * <p>
   * The string is clipped by {@code maxLineCount}, then by overall string length.
   * </p>
   *
   * @param value
   *          String to clip.
   * @param maxLineCount
   *          Maximum number of lines.
   * @param averageLineLength
   *          Average line length (used along with {@code maxLineCount} to calculate the overall
   *          maximum string length).
   * @param marker
   *          Replacement marker.
   * @see StringUtils#abbreviate(String, String, int)
   */
  public static String abbreviateMultiline(String value, int maxLineCount, int averageLineLength,
      String marker) {
    if (maxLineCount <= 0 && averageLineLength <= 0)
      return value;

    String ret = value;
    {
      int pos = -1;
      int lineCount = 1;
      while ((pos = ret.indexOf('\n', pos + 1)) >= 0) {
        if (maxLineCount > 0 && lineCount == maxLineCount) {
          ret = ret.substring(0, pos);
          break;
        }

        lineCount++;
      }

      int maxLength = lineCount * averageLineLength;
      if (ret.length() > maxLength) {
        ret = ret.substring(0, maxLength);
      }
    }
    //noinspection StringEquality
    if (ret != value) {
      ret += marker;
    }
    return ret;
  }

  /**
   * Gets the number of substring occurrences in the given value.
   */
  public static int count(String value, String sub) {
    int ret = 0;
    for (int i = 0;; i++) {
      i = value.indexOf(sub, i);
      if (i < 0) {
        break;
      }

      ret++;
    }
    return ret;
  }

  /**
   * Gets whether the given index represents a matching.
   *
   * @see String#indexOf(String)
   * @see String#lastIndexOf(String)
   */
  public static boolean indexFound(int index) {
    return index != INDEX__NOT_FOUND;
  }

  /**
   * Gets whether the given string represents an integer number (ie, contains only Unicode digits,
   * with optional leading sign, either positive or negative).
   */
  public static boolean isInteger(@Nullable CharSequence value) {
    return isNumeric(value, true, true);
  }

  /**
   * Gets whether the given string represents a generic number (ie, contains only Unicode digits,
   * with optional leading sign, either positive or negative, and decimal point).
   */
  public static boolean isNumeric(@Nullable CharSequence value) {
    return isNumeric(value, false, true);
  }

  // SPDX-SnippetBegin
  // SPDX-SnippetCopyrightText: © 2001-2025 The Apache Software Foundation
  // SPDX-License-Identifier: Apache-2.0
  //
  // Source: https://github.com/apache/commons-lang/blob/73f99230910010c1056bb6c04b36a04261da8b7d/src/main/java/org/apache/commons/lang3/StringUtils.java#L3682
  // SourceFQN: org.apache.commons.lang3.StringUtils.isNumeric(CharSequence)
  // Changes: see @implNote
  /**
   * Gets whether the given string contains only Unicode digits, with optional leading sign, either
   * positive or negative, and decimal point.
   * <p>
   * NOTE: Even if a string passes this test, it may still generate an exception when parsed by
   * Integer.parseInt or Long.parseLong (eg, if the value is outside the range for int or long
   * respectively).
   * </p>
   *
   * <pre>
   *isNumeric(null,*,*)       = false
   *isNumeric("",*,*)         = false
   *isNumeric("  ",*,*)       = false
   *isNumeric("123",*,*)      = true
   *isNumeric("\u0967\u0968\u0969",*,*) = true
   *isNumeric("12 3",*,*)     = false
   *isNumeric("ab2c",*,*)     = false
   *isNumeric("12-3",*,*)     = false
   *isNumeric("12.3",false,*) = true
   *isNumeric("12.3",true,*)  = false
   *isNumeric("-123",*,true)  = true
   *isNumeric("-123",*,false) = false
   *isNumeric("+123",*,true)  = true
   *isNumeric("+123",*,false) = false</pre>
   *
   * @param value
   *          String to check.
   * @param integer
   *          Whether {@code value} should be integer (ie, without decimal point).
   * @param signable
   *          Whether {@code value} can contain a leading sign.
   * @implNote Contrary to the original implementation
   *           ({@code org.apache.commons.lang3.StringUtils.isNumeric(CharSequence)}), this method
   *           allows for a leading sign, either positive or negative, and a decimal point.
   */
  public static boolean isNumeric(@Nullable final CharSequence value, final boolean integer,
      final boolean signable) {
    if (isEmpty(value))
      return false;

    boolean decimal = false;
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (!Character.isDigit(c)) {
        switch (c) {
          case '.':
            if (!decimal && !integer) {
              decimal = true;
              break;
            }
            return false;
          case '+':
          case '-':
            if (i == 0 && signable) {
              break;
            }
            return false;
          default:
            return false;
        }
      }
    }
    return true;
  }
  // SPDX-SnippetEnd

  /**
   * Gets whether the given string represents an unsigned integer number (ie, contains only Unicode
   * digits, with no leading sign).
   */
  public static boolean isUInteger(@Nullable CharSequence value) {
    return isNumeric(value, true, false);
  }

  /**
   * Gets the end (exclusive) of the line at the given position.
   */
  public static int lineEnd(String text, int position) {
    position = text.indexOf('\n', position);
    return position < 0 ? text.length() : position;
  }

  /**
   * Gets the start (inclusive) of the line at the given position.
   */
  public static int lineStart(String text, int position) {
    return text.lastIndexOf('\n', position) + 1;
  }

  /**
   * Normalizes the given value, converting to {@code null} if empty.
   *
   * @param value
   *          String to normalize.
   * @return {@code null}, if {@code value} is empty.
   */
  public static @Nullable String strEmptyToNull(@Nullable String value) {
    return value != null && !value.isEmpty() ? value : null;
  }

  /**
   * Normalizes the given value, stripping and converting to empty if {@code null}.
   *
   * @param value
   *          String to normalize.
   * @return Empty, if {@code value} is {@code null}.
   */
  public static String strNorm(@Nullable String value) {
    return strNorm(value, EMPTY);
  }

  /**
   * Normalizes the given value, stripping and converting to default if needed.
   *
   * @param value
   *          String to normalize.
   * @param defaultValue
   *          String to return in case {@code value} is {@code null} or empty.
   * @return {@code defaultValue}, if {@code value} is {@code null} or normalized value is empty.
   * @see java.util.Objects#requireNonNullElse(Object, Object)
   */
  public static String strNorm(@Nullable String value, String defaultValue) {
    return value == null || (value = value.strip()).isEmpty() ? defaultValue : value;
  }

  /**
   * (see {@link #strNormAll(String, String, boolean)})
   *
   * @return Empty, if {@code value} is {@code null}.
   */
  public static String strNormAll(@Nullable String value) {
    return strNormAll(value, EMPTY, true);
  }

  // SPDX-SnippetBegin
  // SPDX-SnippetCopyrightText: © 2001-2025 The Apache Software Foundation
  // SPDX-License-Identifier: Apache-2.0
  //
  // Source: https://github.com/apache/commons-lang/blob/73f99230910010c1056bb6c04b36a04261da8b7d/src/main/java/org/apache/commons/lang3/StringUtils.java#L5364
  // SourceFQN: org.apache.commons.lang3.StringUtils.normalizeSpace(String)
  // Changes: see @implNote
  /**
   * Normalizes the given value replacing sequences of whitespace characters by a single space
   * (similar to <a href=
   * "https://www.w3.org/TR/xpath/#function-normalize-space">https://www.w3.org/TR/xpath/#function-normalize
   * -space</a>).
   * <p>
   * In XML whitespace characters are the same as those allowed by the
   * <a href="https://www.w3.org/TR/REC-xml/#NT-S">S</a> production
   * (<code>S::=(#x20|#x9|#xD|#xA)+</code>), while Java regex pattern <code>\s</code> defines
   * whitespace as <code>[ \t\n\x0B\f\r].</code>
   * </p>
   * <p>
   * For reference:
   * </p>
   * <pre>
   *\x0B = vertical tab
   *\f = #xC = form feed
   *#x20 = space
   *#x9 = \t
   *#xA = \n
   *#xD = \r</pre>
   * <p>
   * The difference is that Java's whitespace includes vertical tab and form feed, which this
   * function will also normalize.
   * </p>
   *
   * @see #strNorm(String)
   * @see <a href=
   *      "https://www.w3.org/TR/xpath/#function-normalize-space">https://www.w3.org/TR/xpath/#function-normalize-space</a>
   * @param value
   *          String to normalize.
   * @param defaultValue
   *          String to return in case {@code value} is {@code null} or empty.
   * @param trimmed
   *          Whether {@code value} must be trimmed.
   * @return {@code defaultValue}, if {@code value} is {@code null} or normalized value is empty.
   * @implNote Contrary to the original implementation
   *           ({@code org.apache.commons.lang3.StringUtils.normalizeSpace(String)}), this method
   *           allows to control trimming and default value.
   */
  public static String strNormAll(@Nullable String value, String defaultValue, boolean trimmed) {
    /*
     * LANG-1020: Improved performance significantly by normalizing manually instead of using regex
     * See https://github.com/librucha/commons-lang-normalizespaces-benchmark for performance test
     */
    if (isEmpty(value))
      return defaultValue;

    final int size = value.length();
    final var newChars = new char[size];
    int count = 0;
    int whitespacesCount = 0;
    boolean startWhitespaces = trimmed;
    for (int i = 0; i < size; i++) {
      final char actualChar = value.charAt(i);
      if (Character.isWhitespace(actualChar)) {
        if (whitespacesCount == 0 && !startWhitespaces) {
          newChars[count++] = SPACE;
        }
        whitespacesCount++;
      } else {
        startWhitespaces = false;
        newChars[count++] = actualChar == NBSP ? SPACE : actualChar;
        whitespacesCount = 0;
      }
    }
    return !startWhitespaces && (count -= (trimmed && whitespacesCount > 0 ? 1 : 0)) > 0
        ? new String(newChars, 0, count)
        : defaultValue;
  }
  // SPDX-SnippetEnd

  /**
   * (see {@link #strNormAll(String, String, boolean)})
   *
   * @return {@code null}, if empty after normalization.
   */
  public static @Nullable String strNormAllToNull(@Nullable String value) {
    return (value = strNormAll(value)).isEmpty() ? null : value;
  }

  /**
   * Normalizes the given value, stripping and converting to {@code null} if empty.
   *
   * @param value
   *          String to normalize.
   * @return {@code null}, if empty after stripping.
   */
  public static @Nullable String strNormToNull(@Nullable String value) {
    return (value = strNorm(value)).isEmpty() ? null : value;
  }

  /**
   * Normalizes the given value, converting to empty if {@code null}.
   *
   * @param value
   *          String to normalize.
   * @return Empty, if {@code value} is {@code null}.
   */
  public static String strNullToEmpty(@Nullable String value) {
    return value != null ? value : EMPTY;
  }

  /**
   * Converts the given string to integer.
   *
   * @return {@code null}, if {@code value} is invalid.
   */
  public static @Nullable Integer strToInteger(String value) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  /**
   * Ensures leading characters are lower-case.
   * <p>
   * Contrary to {@link StringUtils#uncapitalize(String)}, this method lowers all consecutive
   * leading upper-case characters except the last one if followed by a lower-case letter. Eg:
   * </p>
   * <ul>
   * <li>{@code "Capitalized"} to {@code "capitalized"}</li>
   * <li>{@code "EOF"} to {@code "eof"}</li>
   * <li>{@code "XObject"} to {@code "xObject"}</li>
   * <li>{@code "IOException"} to {@code "ioException"}</li>
   * <li>{@code "UTF8Test"} to {@code "utf8Test"}</li>
   * <li>{@code "UTF8TEST"} to {@code "utf8TEST"}</li>
   * <li>{@code "UNDERSCORE_TEST"} to {@code "underscore_TEST"}</li>
   * </ul>
   */
  public static String uncapitalizeGreedy(String value) {
    char[] valueChars = value.toCharArray();
    for (int i = 0, limit = valueChars.length - 1; i <= limit; i++) {
      /*-
       * Not upper-case letter?
       *
       * Eg, "UTF8Test" --> "utf8Test"
       *         ^
       */
      if (!Character.isUpperCase(valueChars[i])) {
        // Unchanged?
        if (i == 0)
          return value;
        // Changed.
        else {
          break;
        }
      }
      /*-
       * Non-initial upper-case letter followed by lower-case letter?
       *
       * Eg, "IOException" --> "ioException"
       *        ^
       */
      else if (i > 0 && i < limit && Character.isLowerCase(valueChars[i + 1])) {
        break;
      }

      valueChars[i] = Character.toLowerCase(valueChars[i]);
    }
    return new String(valueChars);
  }

  private Strings() {
  }
}
