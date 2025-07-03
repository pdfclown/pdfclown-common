/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Patterns.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.regex;

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
   * Converts the given (filesystem-aware) glob to regex.
   *
   * @param glob
   *          Glob pattern interpreted according to the classic
   *          <a href="https://en.wikipedia.org/wiki/Glob_(programming)">glob</a> algorithm, which
   *          is filesystem-aware. In particular, it supports the globstar ({@code **}).<br>
   *          NOTE: Character classes ({@code [...]}) are NOT supported.
   * @implNote This method was spurred by the current lack of native support (see
   *           <a href="https://bugs.openjdk.org/browse/JDK-8241641">JDK-8241641</a>).
   */
  public static String globToRegex(String glob) {
    return globToRegex(glob, true);
  }

  /**
   * Tries to match the given pattern.
   */
  public static Optional<Matcher> match(Pattern pattern, CharSequence input) {
    Matcher ret = pattern.matcher(input);
    return ret.find() ? Optional.of(ret) : Optional.empty();
  }

  /**
   * Tries to match the given regular expression.
   */
  public static Optional<Matcher> match(String regex, CharSequence input) {
    return match(Pattern.compile(regex), input);
  }

  /**
   * Converts the given wildcard pattern to regex.
   *
   * @param pattern
   *          Wildcard pattern supporting
   *          <a href="https://en.wikipedia.org/wiki/Glob_(programming)">globbing</a> metacharacters
   *          ({@code ?}, {@code *}).<br>
   *          NOTE: Character classes ({@code [...]}) are NOT supported.
   * @implNote This method was spurred by the current lack of native support (see
   *           <a href="https://bugs.openjdk.org/browse/JDK-8241641">JDK-8241641</a>).
   */
  public static String wildcardToRegex(String pattern) {
    return globToRegex(pattern, false);
  }

  /**
   * @param glob
   *          Glob pattern.
   * @param fileSystemAware
   *          Whether {@code glob} must be interpreted according to the classic
   *          <a href="https://en.wikipedia.org/wiki/Glob_(programming)">glob</a> algorithm, which
   *          is filesystem-aware. In particular, it supports the globstar ({@code **}).
   */
  private static String globToRegex(String glob, boolean fileSystemAware) {
    var b = new StringBuilder();
    int i = 0;
    while (i < glob.length()) {
      char c = glob.charAt(i);
      mainSwitch: switch (c) {
        // Reserved regex symbol.
        case '.':
        case '(':
        case ')':
        case '[':
        case ']':
        case '{':
        case '}':
        case '^':
        case '$':
        case '+':
        case '|':
          // Escape reserved regex symbol!
          b.append('\\').append(c);
          break;
        // Glob escape symbol.
        case '\\': {
          int i1 = i + 1;
          if (glob.length() > i1) {
            char c1 = glob.charAt(i1);
            switch (c1) {
              // Escaped reserved glob symbol.
              case '?':
              case '*':
                // Escape reserved regex symbol!
                b.append('\\').append(c1);
                i = i1;
                break mainSwitch;
              default:
            }
          }
          // Literal backslash.
          b.append('\\').append(c);
          break;
        }
        // `?` operator.
        case '?':
          b.append('.');
          break;
        // `*` operator.
        case '*':
          if (fileSystemAware) {
            int i1 = i + 1;
            if (glob.length() > i1 && glob.charAt(i1) == '*') {
              b.append(".*") /* Any (including level separator) */;
              i = i1;
            } else {
              b.append("[^/]*") /* Any but level separator */;
            }
          } else {
            b.append(".*");
          }
          break;
        default:
          b.append(c);
          break;
      }
      i++;
    }
    return b.toString();
  }

  private Patterns() {
  }
}
