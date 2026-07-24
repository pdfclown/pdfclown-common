/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Patterns.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.regex;

import static org.pdfclown.common.util.Chars.BACKSLASH;
import static org.pdfclown.common.util.Chars.SLASH;
import static org.pdfclown.common.util.Exceptions.unexpected;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

  private static final String REGEX__FILE_SEPARATORS = "/\\\\";

  /**
   * Converts the (filesystem-oriented) glob to regex.
   *
   * @param glob
   *          Glob pattern interpreted according to the classic
   *          <a href="https://en.wikipedia.org/wiki/Glob_(programming)">glob</a> algorithm, which
   *          is filesystem-oriented. In particular, it supports the globstar ({@code "**"}).<br>
   *          NOTE: Character classes ({@code "["..."]"}) are NOT supported.
   * @implNote This method was spurred by the current lack of native support (see
   *           <a href="https://bugs.openjdk.org/browse/JDK-8241641">JDK-8241641</a>).
   */
  public static String globToRegex(String glob) {
    return globToRegex(glob, GLOB_MODE__FILESYSTEM);
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
   * Corresponds to {@link Pattern#split(CharSequence, int) p.split(input, -1)}.
   * </p>
   *
   * @apiNote This is a remedy to <a href="https://errorprone.info/bugpattern/StringSplitter">Error
   *          Prone — Bug Patterns — StringSplitter</a>.
   */
  public static String[] splitAll(Pattern p, CharSequence input) {
    return p.split(input, -1);
  }

  /**
   * Converts the wildcard pattern to regex.
   *
   * @param wildcard
   *          Wildcard pattern supporting
   *          <a href="https://en.wikipedia.org/wiki/Glob_(programming)">globbing</a> metacharacters
   *          ({@code '?'}, {@code '*'}).<br>
   *          NOTE: Character classes ({@code "["..."]"}) are NOT supported.
   * @implNote This method was spurred by the current lack of native support (see
   *           <a href="https://bugs.openjdk.org/browse/JDK-8241641">JDK-8241641</a>).
   */
  public static String wildcardToRegex(String wildcard) {
    return globToRegex(wildcard, GLOB_MODE__WILDCARD);
  }

  /**
   * @param glob
   *          Glob pattern.
   * @param globMode
   *          How to interpret {@code glob} ({@link #GLOB_MODE__FILESYSTEM},
   *          {@link #GLOB_MODE__WILDCARD}).
   */
  @SuppressWarnings("LabelledBreakTarget")
  private static String globToRegex(String glob, int globMode) {
    var b = new StringBuilder();
    int i = 0;
    while (i < glob.length()) {
      char c = glob.charAt(i);
      mainSwitch: switch (c) {
        // Reserved regex symbol.
        case //
            '.', //
            '(', //
            ')', //
            '[', //
            ']', //
            '{', //
            '}', //
            '^', //
            '$', //
            '+', //
            '|' ->
            // Escape reserved regex symbol!
            b.append(BACKSLASH).append(c);
        // Glob escape symbol.
        case BACKSLASH -> {
          int i1 = i + 1;
          if (glob.length() > i1) {
            char c1 = glob.charAt(i1);
            switch (c1) {
              // Reserved glob symbol.
              case '?', '*' -> {
                // Escape reserved regex symbol!
                b.append(BACKSLASH).append(c1);
                i = i1;
                break mainSwitch;
              }
              default -> {
                // NOP
              }
            }
          }

          // Literal backslash.
          switch (globMode) {
            case GLOB_MODE__WILDCARD -> b.append(BACKSLASH).append(c);
            case GLOB_MODE__FILESYSTEM -> b.append("[" + REGEX__FILE_SEPARATORS + "]");
            default -> throw unexpected("globMode", globMode);
          }
        }
        case SLASH -> {
          switch (globMode) {
            case GLOB_MODE__WILDCARD -> b.append(SLASH);
            case GLOB_MODE__FILESYSTEM -> b.append("[" + REGEX__FILE_SEPARATORS + "]");
            default -> throw unexpected("globMode", globMode);
          }
        }
        // `?` operator.
        case '?' -> b.append('.');
        // `*` operator.
        case '*' -> {
          switch (globMode) {
            case GLOB_MODE__WILDCARD -> b.append(".*");
            case GLOB_MODE__FILESYSTEM -> {
              int i1 = i + 1;
              if (glob.length() > i1 && glob.charAt(i1) == '*') {
                b.append(".*") /* Any (including level separators) */;
                i = i1;
              } else {
                b.append("[^" + REGEX__FILE_SEPARATORS + "]*") /* Any but level separators */;
              }
            }
            default -> throw unexpected("globMode", globMode);
          }
        }
        default -> b.append(c);
      }
      i++;
    }
    return b.toString();
  }

  private Patterns() {
  }
}
