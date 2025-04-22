/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (Strings.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common> (this Program).

  This Program is free software: you can redistribute it and/or modify it under the terms of the GNU
  Lesser General Public License (LGPL) as published by the Free Software Foundation, either version
  3 of the License, or (at your option) any later version.

  This Program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this Program.
  If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>.
 */
/*
 * NOTE: Apache License applies to the following methods:
 * - isNumeric
 * - strNormAll
 */
/*
  SPDX-FileCopyrightText: Copyright The Apache Software Foundation <https://www.apache.org>
  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.common.util;

import java.util.Random;
import java.util.function.IntPredicate;
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
    return abbreviateMultiline(value, maxLineCount, averageLineLength, "...");
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
   * Gets whether the given string is null or blank.
   */
  public static boolean isBlank(@Nullable String value) {
    return value == null || value.isBlank();
  }

  /**
   * Gets whether the given string is null or empty.
   */
  public static boolean isEmpty(@Nullable CharSequence value) {
    return value == null || value.length() == 0;
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

  /*-
   * Original FQN: org.apache.commons.lang3.StringUtils.isNumeric
   * Source: https://github.com/apache/commons-lang/blob/master/src/main/java/org/apache/commons/lang3/StringUtils.java
   * License: see file header.
   */
  /**
   * Gets whether the given string contains only Unicode digits, with optional leading sign, either
   * positive or negative, and decimal point.
   * <p>
   * NOTE: Contrary to the original org.apache.commons.lang3 implementation, this method allows for
   * a leading sign, either positive or negative, and a decimal point.
   * </p>
   * <p>
   * NOTE: If a string passes the numeric test, it may still generate an exception when parsed by
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
   */
  public static boolean isNumeric(@Nullable final CharSequence value, final boolean integer,
      final boolean signable) {
    if (isEmpty(value))
      return false;

    assert value != null;

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
   * Generates a random alphanumeric string.
   *
   * @param length
   *          String length.
   * @param alphabetic
   *          Whether (lower-case) alphabetic characters are included.
   * @param numeric
   *          Whether numeric characters are included.
   */
  public static String randomAlphanumeric(int length, boolean alphabetic, boolean numeric) {
    int lBound = 0;
    int uBound = 0;
    IntPredicate filter = null;
    if (numeric) {
      final int lb = lBound = '0';
      final int ub = uBound = '9';
      filter = $ -> $ >= lb && $ <= ub;
    }
    if (alphabetic) {
      final int lb = 'a';
      final int ub = uBound = 'z';
      IntPredicate f = $ -> $ >= lb && $ <= ub;
      if (filter != null) {
        filter = filter.or(f);
      } else {
        lBound = lb;
        filter = f;
      }
    }
    return new Random()
        .ints(lBound, uBound + 1)
        .filter(filter)
        .limit(length)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();
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

  /*-
   * Original FQN: org.apache.commons.lang3.StringUtils.normalizeSpace
   * Source: https://github.com/apache/commons-lang/blob/master/src/main/java/org/apache/commons/lang3/StringUtils.java
   * License: see file header.
   */
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
   */
  public static String strNormAll(@Nullable String value, String defaultValue, boolean trimmed) {
    /*
     * LANG-1020: Improved performance significantly by normalizing manually instead of using regex
     * See https://github.com/librucha/commons-lang-normalizespaces-benchmark for performance test
     */
    if (isEmpty(value))
      return defaultValue;

    assert value != null;

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
   * @param value
   * @return {@code null}, if {@code value} is invalid.
   */
  public static @Nullable Integer toInteger(String value) {
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
  public static String uncapitalizeMultichar(String value) {
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
