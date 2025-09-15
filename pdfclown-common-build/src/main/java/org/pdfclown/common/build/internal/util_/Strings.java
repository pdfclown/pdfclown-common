/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Strings.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.util_;

import static java.lang.Character.isDigit;
import static java.lang.Character.isLowerCase;
import static java.lang.Character.isUpperCase;
import static java.lang.Character.isWhitespace;
import static java.lang.Math.min;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
   * String str = S + HYPHEN;</pre>
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
   * Ensures the string doesn't exceed the limits; otherwise, replaces the exceeding substring with
   * a standard ellipsis.
   *
   * @param maxLineCount
   *          Maximum number of lines.
   * @param averageLineLength
   *          Average line length (used along with {@code maxLineCount} to calculate the overall
   *          maximum string length).
   * @see StringUtils#abbreviate(String, int)
   */
  public static String abbreviateMultiline(String s, int maxLineCount, int averageLineLength) {
    return abbreviateMultiline(s, maxLineCount, averageLineLength, ELLIPSIS);
  }

  /**
   * Ensures the string doesn't exceed the limits; otherwise, replaces the exceeding substring with
   * a marker.
   * <p>
   * The string is clipped by {@code maxLineCount}, then by overall string length.
   * </p>
   *
   * @param maxLineCount
   *          Maximum number of lines.
   * @param averageLineLength
   *          Average line length (used along with {@code maxLineCount} to calculate the overall
   *          maximum string length).
   * @param marker
   *          Replacement marker.
   * @see StringUtils#abbreviate(String, String, int)
   */
  public static String abbreviateMultiline(String s, int maxLineCount, int averageLineLength,
      String marker) {
    if (maxLineCount <= 0 && averageLineLength <= 0)
      return s;

    String ret = s;
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
    if (ret != s) {
      ret += marker;
    }
    return ret;
  }

  /**
   * Gets the number of substring occurrences in the string.
   */
  public static int count(String s, String sub) {
    int ret = 0;
    for (int i = 0;; i++) {
      i = s.indexOf(sub, i);
      if (i < 0) {
        break;
      }

      ret++;
    }
    return ret;
  }

  /**
   * Gets whether the index represents a match.
   *
   * @see String#indexOf(String)
   * @see String#lastIndexOf(String)
   */
  public static boolean found(int index) {
    return index != INDEX__NOT_FOUND;
  }

  /**
   * Gets the index of the first matching character.
   *
   * @return {@value INDEX__NOT_FOUND}, if no match was found.
   */
  public static int indexOf(String s, IntPredicate condition) {
    return indexOf(s, condition, 0);
  }

  /**
   * Gets the index of the first matching character.
   *
   * @return {@value INDEX__NOT_FOUND}, if no match was found.
   */
  public static int indexOf(String s, IntPredicate condition, int fromIndex) {
    for (int i = fromIndex, l = s.length(); i < l; i++) {
      if (condition.test(s.charAt(i)))
        return i;
    }
    return INDEX__NOT_FOUND;
  }

  /**
   * Gets whether the character is an end of line.
   */
  public static boolean isEOL(int c) {
    return c == LF || c == CR;
  }

  /**
   * Gets whether the string represents an integer number.
   * <p>
   * <b>Integer number</b> contains only Unicode digits, with optional leading sign, either positive
   * or negative.
   * </p>
   */
  public static boolean isInteger(@Nullable CharSequence s) {
    return isNumeric(s, true, true);
  }

  /**
   * Gets whether the string represents a generic number.
   * <p>
   * <b>Generic number</b> contains only Unicode digits, with optional leading sign, either positive
   * or negative, and decimal point.
   * </p>
   */
  public static boolean isNumeric(@Nullable CharSequence s) {
    return isNumeric(s, false, true);
  }

  // SPDX-SnippetBegin
  // SPDX-SnippetCopyrightText: © 2001-2025 The Apache Software Foundation
  // SPDX-License-Identifier: Apache-2.0
  //
  // Source: https://github.com/apache/commons-lang/blob/73f99230910010c1056bb6c04b36a04261da8b7d/src/main/java/org/apache/commons/lang3/StringUtils.java#L3682
  // SourceName: org.apache.commons.lang3.StringUtils.isNumeric(CharSequence)
  // Changes: see @implNote
  /**
   * Gets whether the string represents a generic number.
   * <p>
   * <b>Generic number</b> contains only Unicode digits, with optional leading sign, either positive
   * or negative, and decimal point.
   * </p>
   * <p>
   * NOTE: Even if a string passes this test, it may still generate an exception when parsed by
   * {@link Integer#parseInt(String)} or {@link Long#parseLong(String)} (for example, if the value
   * is outside the range for int or long, respectively).
   * </p>
   * <pre>
   * isNumeric(null,*,*)       = false
   * isNumeric("",*,*)         = false
   * isNumeric("  ",*,*)       = false
   * isNumeric("123",*,*)      = true
   * isNumeric("\u0967\u0968\u0969",*,*) = true
   * isNumeric("12 3",*,*)     = false
   * isNumeric("ab2c",*,*)     = false
   * isNumeric("12-3",*,*)     = false
   * isNumeric("12.3",false,*) = true
   * isNumeric("12.3",true,*)  = false
   * isNumeric("-123",*,true)  = true
   * isNumeric("-123",*,false) = false
   * isNumeric("+123",*,true)  = true
   * isNumeric("+123",*,false) = false</pre>
   *
   * @param integer
   *          Whether {@code s} should be integer (that is, without decimal point).
   * @param signable
   *          Whether {@code s} can contain a leading sign.
   * @implNote Contrary to the original implementation
   *           ({@code org.apache.commons.lang3.StringUtils.isNumeric(CharSequence)}), this method
   *           allows for a leading sign, either positive or negative, and a decimal point.
   */
  public static boolean isNumeric(@Nullable final CharSequence s, final boolean integer,
      final boolean signable) {
    if (isEmpty(s))
      return false;

    boolean decimal = false;
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (!isDigit(c)) {
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
   * Gets whether the string represents an unsigned integer number.
   * <p>
   * <b>Unsigned integer number</b> contains only Unicode digits, with no leading sign.
   * </p>
   */
  public static boolean isUInteger(@Nullable CharSequence s) {
    return isNumeric(s, true, false);
  }

  /**
   * Gets the index of the last matching character, searched backwards.
   *
   * @return {@value INDEX__NOT_FOUND}, if no match was found.
   */
  public static int lastIndexOf(String s, IntPredicate condition) {
    return lastIndexOf(s, condition, s.length());
  }

  /**
   * Gets the index of the last matching character, searched backwards.
   *
   * @return {@value INDEX__NOT_FOUND}, if no match was found.
   */
  public static int lastIndexOf(String s, IntPredicate condition, int fromIndex) {
    for (int i = min(fromIndex, s.length() - 1); i >= 0; i--) {
      if (condition.test(s.charAt(i)))
        return i;
    }
    return INDEX__NOT_FOUND;
  }

  /**
   * Gets the end of the line at the position.
   */
  public static int lineEnd(String s, int index) {
    var ret = indexOf(s, Strings::isEOL, index);
    return found(ret) ? ret : s.length();
  }

  /**
   * Gets the start of the line at the position.
   */
  public static int lineStart(String s, int index) {
    return lastIndexOf(s, Strings::isEOL, index) + 1;
  }

  /**
   * Converts the string to integer.
   *
   * @return {@code null}, if {@code s} is invalid.
   */
  public static @Nullable Integer strToInteger(String s) {
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  /**
   * Strips leading and trailing empty lines.
   * <p>
   * Contrary to {@link String#strip()}, only leading and trailing {@linkplain #isEOL(int)
   * end-of-line} characters are removed.
   * </p>
   */
  public static String stripEmptyLines(String s) {
    int begin = indexOf(s, $ -> !isEOL($));
    if (!found(begin))
      return EMPTY;

    int end = lastIndexOf(s, $ -> !isEOL($)) + 1;
    return begin > 0 || end < s.length() ? s.substring(begin, end) : s;
  }

  /**
   * Strips incidental leading and trailing whitespace from each line in the string.
   * <p>
   * <b>Incidental whitespace</b> is any other than internal indentation; the latter is what remains
   * after removing the same amount of leading whitespace from all the lines, until at least one
   * line has no more left.
   * </p>
   * <p>
   * This method mimics Java 15's <code><a href=
   * "https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/String.html#stripIndent()">String.stripIndent()</a></code>.
   * </p>
   */
  public static String stripIndent(String s) {
    if (s.isEmpty())
      return s;

    List<String> lines = s.lines().collect(Collectors.toCollection(ArrayList::new));

    // Detect incidental leading whitespace!
    int minLeadSpaceCount = Integer.MAX_VALUE;
    var changed = false;
    for (var it = lines.listIterator(); it.hasNext();) {
      var line = it.next();
      if (line.isEmpty()) {
        continue;
      }

      /*
       * Strip trailing whitespace!
       *
       * NOTE: This also prevents blank lines from interfering with the detection.
       */
      {
        var strippedLine = line.stripTrailing();
        if (!strippedLine.equals(line)) {
          it.set(line = strippedLine);
          changed = true;
          if (line.isEmpty()) {
            continue;
          }
        }
      }

      if (minLeadSpaceCount > 0) {
        minLeadSpaceCount = min(minLeadSpaceCount, indexOf(line, $ -> !isWhitespace($)));
      }
    }
    if (!changed && minLeadSpaceCount == 0)
      return s;

    // Strip incidental leading whitespace!
    Stream<String> stream = lines.stream();
    if (minLeadSpaceCount > 0) {
      int indexOfNonIncidental = minLeadSpaceCount;
      stream = stream
          .map($ -> $.isEmpty() ? $
              : $.substring(min(indexOf($, $$ -> !isWhitespace($$)), indexOfNonIncidental)));
    }
    return stream.collect(Collectors.joining(S + LF, EMPTY,
        isEOL(s.charAt(s.length() - 1)) ? S + LF /* Preserves EOL on last line */ : EMPTY));
  }

  /**
   * Ensures leading characters are lower-case.
   * <p>
   * Contrary to {@link StringUtils#uncapitalize(String)}, this method lowers all consecutive
   * leading upper-case characters except the last one if followed by a lower-case letter — for
   * example:
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
  public static String uncapitalizeGreedy(String s) {
    char[] cc = s.toCharArray();
    for (int i = 0, limit = cc.length - 1; i <= limit; i++) {
      /*-
       * Not upper-case letter?
       *
       * For example, "UTF8Test" --> "utf8Test"
       *                  ^
       */
      if (!isUpperCase(cc[i])) {
        // Unchanged?
        if (i == 0)
          return s;
        // Changed.
        else {
          break;
        }
      }
      /*-
       * Non-initial upper-case letter followed by lower-case letter?
       *
       * For example, "IOException" --> "ioException"
       *                 ^
       */
      else if (i > 0 && i < limit && isLowerCase(cc[i + 1])) {
        break;
      }

      cc[i] = Character.toLowerCase(cc[i]);
    }
    return new String(cc);
  }

  private Strings() {
  }
}
