/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Strings.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.util;

import org.apache.commons.lang3.StringUtils;

/**
 * String utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Strings {
  // SourceFQN: org.pdfclown.common.util.Strings.INDEX__NOT_FOUND
  /**
   * Flag value used by methods like {@link String#indexOf(String)} to represent absence of searched
   * occurrence.
   */
  public static final int INDEX__NOT_FOUND = -1;

  // SourceFQN: org.pdfclown.common.util.Strings.BACKSLASH
  public static final char BACKSLASH = '\\';
  // SourceFQN: org.pdfclown.common.util.Strings.COMMA
  public static final char COMMA = ',';
  // SourceFQN: org.pdfclown.common.util.Strings.DOT
  public static final char DOT = '.';
  // SourceFQN: org.pdfclown.common.util.Strings.DQUOTE
  /**
   * Double quote (aka quotation mark).
   */
  public static final char DQUOTE = '\"';
  // SourceFQN: org.pdfclown.common.util.Strings.NBSP
  /**
   * <a href="https://en.wikipedia.org/wiki/Non-breaking_space">Non-breaking space</a> (aka hard
   * space) character preventing automatic line break at its position. In some formats, including
   * HTML, it also prevents consecutive whitespace characters from collapsing into a single space.
   */
  public static final char NBSP = 160;
  // SourceFQN: org.pdfclown.common.util.Strings.PIPE
  /**
   * Vertical bar.
   */
  public static final char PIPE = '|';
  // SourceFQN: org.pdfclown.common.util.Strings.ROUND_BRACKET_CLOSE
  public static final char ROUND_BRACKET_CLOSE = ')';
  // SourceFQN: org.pdfclown.common.util.Strings.ROUND_BRACKET_OPEN
  public static final char ROUND_BRACKET_OPEN = '(';
  // SourceFQN: org.pdfclown.common.util.Strings.SLASH
  public static final char SLASH = '/';
  // SourceFQN: org.pdfclown.common.util.Strings.SPACE
  public static final char SPACE = ' ';
  // SourceFQN: org.pdfclown.common.util.Strings.SQUOTE
  /**
   * Single quote (aka apostrophe).
   */
  public static final char SQUOTE = '\'';
  // SourceFQN: org.pdfclown.common.util.Strings.UNDERSCORE
  public static final char UNDERSCORE = '_';

  // SourceFQN: org.pdfclown.common.util.Strings.EMPTY
  /**
   * Empty string.
   */
  public static final String EMPTY = "";
  // SourceFQN: org.pdfclown.common.util.Strings.EOL
  /**
   * System-dependent line separator ({@link System#lineSeparator()} alias).
   */
  public static final String EOL = System.lineSeparator();
  // SourceFQN: org.pdfclown.common.util.Strings.S
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

  // SourceFQN: org.pdfclown.common.util.Strings.ELLIPSIS
  /**
   * <a href="https://en.wikipedia.org/wiki/Ellipsis">Common (aka Associated Press-style)
   * ellipsis</a>.
   */
  public static final String ELLIPSIS = S + DOT + DOT + DOT;
  // SourceFQN: org.pdfclown.common.util.Strings.ELLIPSIS__CHICAGO
  /**
   * <a href="https://en.wikipedia.org/wiki/Ellipsis">Chicago-style ellipsis</a>.
   */
  public static final String ELLIPSIS__CHICAGO = S + DOT + NBSP + DOT + NBSP + DOT;

  // SourceFQN: org.pdfclown.common.util.Strings.abbreviateMultiline(..)
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

  // SourceFQN: org.pdfclown.common.util.Strings.abbreviateMultiline(..)
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

  // SourceFQN: org.pdfclown.common.util.Strings.uncapitalizeGreedy(..)
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
