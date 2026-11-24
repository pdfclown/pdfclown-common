/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  This file (Patterns.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block below this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.regex;

import static org.pdfclown.common.util.Chars.BACKSLASH;
import static org.pdfclown.common.util.Chars.CARET;
import static org.pdfclown.common.util.Chars.COMMA;
import static org.pdfclown.common.util.Chars.CURLY_BRACE_CLOSE;
import static org.pdfclown.common.util.Chars.CURLY_BRACE_OPEN;
import static org.pdfclown.common.util.Chars.DOT;
import static org.pdfclown.common.util.Chars.PIPE;
import static org.pdfclown.common.util.Chars.SLASH;
import static org.pdfclown.common.util.Chars.SQUARE_BRACKET_CLOSE;
import static org.pdfclown.common.util.Chars.SQUARE_BRACKET_OPEN;
import static org.pdfclown.common.util.Chars.STAR;
import static org.pdfclown.common.util.Exceptions.unexpected;
import static org.pdfclown.common.util.Objects.textLiteral;
import static org.pdfclown.common.util.Strings.S;

import java.util.ArrayDeque;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.text.ParseException;
import org.pdfclown.common.util.text.TextPosition;

/**
 * Regular-expression utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Patterns {
  /**
   * Glob expressions must be interpreted according to the classic
   * <a href="https://en.wikipedia.org/wiki/Glob_(programming)">glob</a> algorithm, which is
   * filesystem-oriented. In particular, it supports the globstar ({@code "**"}).
   */
  private static final int GLOB_MODE__FILESYSTEM = 0;
  /**
   * Glob expressions must be interpreted as wildcard pattern supporting
   * <a href="https://en.wikipedia.org/wiki/Glob_(programming)">globbing</a> metacharacters
   * ({@code '?'}, {@code '*'}).
   */
  private static final int GLOB_MODE__WILDCARD = 1;

  private static final String REGEX__DIR_SEP_CHARS =
      S + SLASH + BACKSLASH + BACKSLASH;

  /**
   * Regex for directory separator ({@code /} or {@code \}).
   */
  private static final String REGEX__CLASS__DIR_SEP =
      S + SQUARE_BRACKET_OPEN + REGEX__DIR_SEP_CHARS + SQUARE_BRACKET_CLOSE;

  /**
   * Regex for any character other than directory separator.
   */
  private static final String REGEX__CLASS__NOT_DIR_SEP =
      S + SQUARE_BRACKET_OPEN + CARET + REGEX__DIR_SEP_CHARS + SQUARE_BRACKET_CLOSE;

  /**
   * Escapes an opaque path fragment (plain text, no glob syntax) so that, embedded in a glob, it
   * matches exactly that text.
   * <ul>
   * <li>{@code /} is left as-is, since it represents the <b>directory separator</b> (that is,
   * structure, not content)</li>
   * <li>{@code \}, {@code *}, {@code ?}, {@code [}, {@code ]}, <code>{</code>, <code>}</code>,
   * {@code ,} are escaped prefixing them with {@code '\\'} (escape character)
   * <p>
   * NOTE: If a fragment holds Windows separators ({@code \}) that should behave as directory
   * separators, convert them to {@code /} before feeding it to this method.
   * </p>
   * </li>
   * <li>everything else is left as-is</li>
   * </ul>
   */
  public static String escapeGlob(String s) {
    return doEscapeGlob(s, GLOB_MODE__FILESYSTEM);
  }

  /**
   * Escapes an opaque fragment (plain text, no wildcard syntax) so that, embedded in a wildcard
   * pattern, it matches exactly that text.
   * <ul>
   * <li>{@code \}, {@code *}, {@code ?}, {@code [}, {@code ]}, <code>{</code>, <code>}</code>,
   * {@code ,} are escaped prefixing them with {@code '\\'} (escape character)</li>
   * <li>everything else is left as-is</li>
   * </ul>
   */
  public static String escapeWildcard(String s) {
    return doEscapeGlob(s, GLOB_MODE__WILDCARD);
  }

  /**
   * Converts a filesystem-oriented
   * <a href="https://en.wikipedia.org/wiki/Glob_(programming)">glob</a> to regex.
   * <p>
   * To ensure unambiguous mapping to regex, strict syntax is enforced, requiring special characters
   * to always be escaped (for the purpose, see {@link #escapeGlob(String)}) — this approach differs
   * from typical shell globbing implementations, which are lenient by design.
   * </p>
   * <p>
   * Supported syntax:
   * </p>
   * <dl>
   * <dt>{@code /}</dt>
   * <dd><b>directory separator</b> — matches either {@code /} (Unix separator) or {@code \}
   * (Windows separator), even mixed within one path. So, the glob {@code "src/conf/**"} matches
   * both {@code src/conf/a.conf} and {@code src\conf\a.conf}.</dd>
   * <dt>{@code \}</dt>
   * <dd><b>escape</b> — escapes the next character (such as {@code "\\*"}, {@code "\\?"},
   * {@code "\\["}, <code>"\\{"</code>, {@code "\\,"}).
   * <p>
   * NOTE:
   * </p>
   * <ul>
   * <li>{@code "\\"} is a literal backslash, matching only a backslash in the path</li>
   * <li>{@code "\/"} is rejected (a separator is written {@code "/"} only)</li>
   * </ul>
   * </dd>
   * <dt>*</dt>
   * <dd><b>any sequence of characters</b> within one path segment (that is, directory
   * separator-exclusive)</dd>
   * <dt>?</dt>
   * <dd><b>exactly one character</b> within a path segment (that is, directory
   * separator-exclusive)</dd>
   * <dt>**</dt>
   * <dd>its meaning is context-dependent:
   * <ul>
   * <li><b>globstar</b> — only when it is a whole path segment:
   * <ul>
   * <li><code>"**&#47;"</code> (leading or middle) — zero or more directories</li>
   * <li><code>"&#47;**"</code> (trailing) — everything below, at any depth</li>
   * <li><code>"**"</code> (the whole glob) — anything at all</li>
   * </ul>
   * </li>
   * <li><b>degrades to {@code "*"}</b> — anywhere else</li>
   * </ul>
   * </dd>
   * <dt>{@code [ab]}</dt>
   * <dd><b>character class</b> — any of these:
   * <ul>
   * <li>set (such as {@code "[abc]"})</li>
   * <li>range (such as {@code "[a-z]"})</li>
   * <li>negation (such as {@code "[!abc]"} or {@code "[^abc]"})</li>
   * </ul>
   * <p>
   * NOTE:
   * </p>
   * <ul>
   * <li>inside a class, {@code '\\'} escapes the next character (such as {@code "[\\]]"})</li>
   * <li>as in POSIX, a class cannot span a directory separator</li>
   * </ul>
   * </dd>
   * <dt><code>{a,b}</code></dt>
   * <dd><b>alternation</b> — may be nested (such as <code>"{src,{test,main}}"</code>)</dd>
   * </dl>
   *
   * @param glob
   *          Glob pattern interpreted according to the syntax above.
   * @implNote This method was spurred by the current lack of native filesystem-independent
   *           plain-string match support (see
   *           <a href="https://bugs.openjdk.org/browse/JDK-8241641">JDK-8241641</a>).
   */
  public static String globToRegex(String glob) {
    return doGlobToRegex(glob, GLOB_MODE__FILESYSTEM);
  }

  /**
   * Gets the position where the match failed.
   *
   * @param matcher
   *          Matcher whose {@link Matcher#find() find()} failed.
   * @apiNote Useful to identify the failing point of single-match, format-constrained text.
   */
  @SuppressWarnings("JdkObsolete")
  public static int indexOfMatchFailure(Matcher matcher) {
    int ret = 0;
    int low = 0;
    int high = matcher.regionEnd();
    while (low <= high) {
      int mid = (low + high) / 2;
      matcher.region(0, mid);
      if (matcher.matches() || matcher.hitEnd()) {
        ret = mid;
        low = mid + 1;
      } else {
        high = mid - 1;
      }
    }
    return ret;
  }

  /**
   * Tries to match the pattern.
   */
  public static Optional<Matcher> match(Pattern pattern, CharSequence input) {
    Matcher ret = pattern.matcher(input);
    return ret.find() ? Optional.of(ret) : Optional.empty();
  }

  /**
   * Tries to match the regular expression.
   */
  public static Optional<Matcher> match(String regex, CharSequence input) {
    return match(Pattern.compile(regex), input);
  }

  /**
   * Splits the input sequence around matches of this pattern, retaining any trailing empty string.
   * <p>
   * Corresponds to {@link Pattern#split(CharSequence, int) pattern.split(input, -1)}.
   * </p>
   *
   * @apiNote This is a remedy to <a href="https://errorprone.info/bugpattern/StringSplitter">Error
   *          Prone — Bug Patterns — StringSplitter</a>.
   */
  public static String[] splitAll(Pattern pattern, CharSequence input) {
    return pattern.split(input, -1);
  }

  /**
   * Converts a wildcard pattern to regex.
   * <p>
   * To ensure unambiguous mapping to regex, strict syntax is enforced, requiring special characters
   * to always be escaped (for the purpose, see {@link #escapeWildcard(String)}) — this approach
   * differs from typical shell globbing implementations, which are lenient by design.
   * </p>
   * <p>
   * Supported syntax:
   * </p>
   * <dl>
   * <dt>{@code \}</dt>
   * <dd><b>escape</b> — escapes the next character (such as {@code "\\*"}, {@code "\\?"},
   * {@code "\\["}, <code>"\\{"</code>, {@code "\\,"})</dd>
   * <dt>*</dt>
   * <dd><b>any sequence of characters</b>, including empty and {@code  /}</dd>
   * <dt>?</dt>
   * <dd><b>exactly one character</b>, including {@code  /}</dd>
   * <dt>{@code [ab]}</dt>
   * <dd><b>character class</b> — any of these:
   * <ul>
   * <li>set (such as {@code "[abc]"})</li>
   * <li>range (such as {@code "[a-z]"})</li>
   * <li>negation (such as {@code "[!abc]"} or {@code "[^abc]"})</li>
   * </ul>
   * <p>
   * NOTE: inside a class, {@code '\\'} escapes the next character (such as {@code "[\\]]"}).
   * </p>
   * </dd>
   * <dt><code>{a,b}</code></dt>
   * <dd><b>alternation</b> — may be nested (such as <code>"{src,{test,main}}"</code>)</dd>
   * </dl>
   *
   * @param wildcard
   *          Wildcard pattern interpreted according to the syntax above.
   * @implNote This method was spurred by the current lack of native generic string match support
   *           (see <a href="https://bugs.openjdk.org/browse/JDK-8241641">JDK-8241641</a>).
   */
  public static String wildcardToRegex(String wildcard) {
    return doGlobToRegex(wildcard, GLOB_MODE__WILDCARD);
  }

  /**
   * Appends to the given buffer the regex character class equivalent to the bracket expression
   * starting at {@code open}.
   *
   * @param open
   *          Position of the opening bracket ({@code '['}).
   * @return Position of the closing bracket ({@code ']'}).
   */
  private static int appendRegexClass(String glob, int globMode, int open, StringBuilder out)
      throws ParseException {
    out.append(SQUARE_BRACKET_OPEN);

    int n = glob.length();
    int j = open + 1;
    if (j < n && (glob.charAt(j) == '!' || glob.charAt(j) == '^')) {
      out.append(CARET);
      switch (globMode) {
        case GLOB_MODE__WILDCARD -> {
          // NOP
        }
        case GLOB_MODE__FILESYSTEM -> out.append(REGEX__DIR_SEP_CHARS);
        default -> throw unexpected("globMode", globMode);
      }
      j++;
    }

    var first = true;
    for (; j < n; j++, first = false) {
      char c = glob.charAt(j);
      /*
       * Bracket expression END?
       *
       * NOTE: ']' in first position is a literal element.
       */
      if (c == SQUARE_BRACKET_CLOSE && !first) {
        out.append(SQUARE_BRACKET_CLOSE);
        return j;
      }
      // Directory separator inside bracket expression?
      else if (c == SLASH && globMode == GLOB_MODE__FILESYSTEM)
        throw wrongFormat("Bracket expression cannot span a directory separator", glob, open, null);
      // Escape (next character is literal)?
      else if (c == BACKSLASH) {
        if (++j >= n) {
          break;
        }

        char e = glob.charAt(j);
        if (e == SLASH && globMode == GLOB_MODE__FILESYSTEM)
          throw wrongFormatEscapedSlash(glob, j - 1);
        /*
         * NOTE: A regex backslash before a letter or digit would form a shorthand (\d, \1, ...), so
         * only non-alphanumerics are backslash-escaped.
         */
        else if (!Character.isLetterOrDigit(e)) {
          out.append(BACKSLASH);
        }
        out.append(e);
      } else {
        // Java special character to escape?
        if (c == '[' || c == ']' || c == '&' || c == '^') {
          out.append(BACKSLASH);
        }
        out.append(c);
      }
    }
    throw wrongFormat("Unbalanced bracket expression delimiter", glob, open, SQUARE_BRACKET_OPEN);
  }

  /**
   * Appends to the given buffer the regex for a literal character outside classes. Never called
   * with '/', which is a separator.
   */
  private static void appendRegexLiteral(char c, StringBuilder out) {
    if ("\\.[]{}()*+-?^$|".indexOf(c) >= 0) {
      out.append(BACKSLASH);
    }
    out.append(c);
  }

  /**
   * @param globMode
   *          How to interpret {@code s} ({@link #GLOB_MODE__FILESYSTEM},
   *          {@link #GLOB_MODE__WILDCARD}).
   */
  private static String doEscapeGlob(String s,
      @SuppressWarnings({ "UnusedVariable", "unused" }) int globMode) {
    var b = new StringBuilder(s.length() + 8);
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if ("\\*?[]{},".indexOf(c) >= 0) {
        b.append(BACKSLASH).append(c);
      } else {
        b.append(c);
      }
    }
    return b.toString();
  }

  /**
   * @param glob
   *          Glob pattern.
   * @param globMode
   *          How to interpret {@code glob} ({@link #GLOB_MODE__FILESYSTEM},
   *          {@link #GLOB_MODE__WILDCARD}).
   */
  private static String doGlobToRegex(String glob, int globMode) throws ParseException {
    var b = new StringBuilder();
    var alternationStartIndexes = new ArrayDeque<Integer>();
    int n = glob.length();
    var atSegStart = true /*
                           * Whether at the start of a path segment (that is, start of the glob or
                           * right after a directory separator)
                           */;
    for (int i = 0; i < n; i++) {
      char c = glob.charAt(i);
      var nextAtSegStart = false;
      switch (c) {
        case SLASH -> {
          switch (globMode) {
            // Literal.
            case GLOB_MODE__WILDCARD -> b.append(SLASH);
            // Directory separator.
            case GLOB_MODE__FILESYSTEM -> {
              b.append(REGEX__CLASS__DIR_SEP);
              nextAtSegStart = true;
            }
            default -> throw unexpected("globMode", globMode);
          }
        }
        // Escape (next character is literal).
        case BACKSLASH -> {
          if (i + 1 >= n)
            throw wrongFormat("Dangling escape", glob, i, c);
          else if (glob.charAt(i + 1) == SLASH && globMode == GLOB_MODE__FILESYSTEM)
            throw wrongFormatEscapedSlash(glob, i);

          appendRegexLiteral(glob.charAt(++i), b);
        }
        // Any character operator.
        case STAR -> {
          int start = i;
          while (i + 1 < n && glob.charAt(i + 1) == STAR) {
            i++;
          }
          switch (globMode) {
            case GLOB_MODE__WILDCARD -> b.append(".*");
            case GLOB_MODE__FILESYSTEM -> {
              int count = i - start + 1;
              int after = i + 1;
              boolean sepNext = after < n && glob.charAt(after) == SLASH;
              boolean segEnd = after == n
                  || sepNext
                  || glob.charAt(after) == CURLY_BRACE_CLOSE
                  || (glob.charAt(after) == COMMA && !alternationStartIndexes.isEmpty());
              // Globstar?
              if (count >= 2 && atSegStart && segEnd) {
                // Non-trailing "**/" (zero or more directories)?
                if (sepNext) {
                  // NOTE: The separator in "**/" is consumed by the operator itself, so even
                  // top-level elements are matched (for example, "**/src" matches both "src" and
                  // "sub/src").
                  b.append("(?:.*").append(REGEX__CLASS__DIR_SEP).append(")?");
                  i++;
                  nextAtSegStart = true;
                }
                // Trailing "**" (everything below, or everything at all if alone).
                else {
                  b.append(".*");
                }
              }
              // Simple star (as-is, or degraded "**").
              else {
                b.append(REGEX__CLASS__NOT_DIR_SEP).append('*');
              }
            }
            default -> throw unexpected("globMode", globMode);
          }
        }
        // Single character operator.
        case '?' -> {
          switch (globMode) {
            case GLOB_MODE__WILDCARD -> b.append(DOT);
            case GLOB_MODE__FILESYSTEM -> b.append(REGEX__CLASS__NOT_DIR_SEP);
            default -> throw unexpected("globMode", globMode);
          }
        }
        // Character class BEGIN operator.
        case SQUARE_BRACKET_OPEN -> i = appendRegexClass(glob, globMode, i, b);
        // Alternation BEGIN operator.
        case CURLY_BRACE_OPEN -> {
          alternationStartIndexes.push(i);
          b.append("(?:");
          nextAtSegStart = true;
        }
        // Alternation END operator.
        case CURLY_BRACE_CLOSE -> {
          if (alternationStartIndexes.isEmpty())
            throw wrongFormat("Unbalanced alternation delimiter", glob, i, c);

          alternationStartIndexes.pop();
          b.append(')');
        }
        case COMMA -> {
          // Literal?
          if (alternationStartIndexes.isEmpty()) {
            b.append(COMMA);
          }
          // Alternation OR operator.
          else {
            b.append(PIPE);
            nextAtSegStart = true;
          }
        }
        default -> appendRegexLiteral(c, b);
      }
      atSegStart = nextAtSegStart;
    }
    if (!alternationStartIndexes.isEmpty())
      throw wrongFormat("Unbalanced alternation delimiter", glob, alternationStartIndexes.pop(),
          CURLY_BRACE_OPEN);

    return b.toString();
  }

  private static ParseException wrongFormat(String problem, String glob, int offset,
      @Nullable Object token) {
    return new ParseException("%s in %s".formatted(problem, textLiteral(glob)),
        TextPosition.of(offset, 0, 0), token, null, null);
  }

  private static ParseException wrongFormatEscapedSlash(String glob, int backslashIndex) {
    return wrongFormat("Escape not allowed for slash", glob, backslashIndex, "\\/");
  }

  private Patterns() {
  }
}
