/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Clis.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.util.system;

import static org.pdfclown.common.util.Chars.COMMA;
import static org.pdfclown.common.util.Chars.DOT;
import static org.pdfclown.common.util.Chars.PIPE;
import static org.pdfclown.common.util.Chars.ROUND_BRACKET_CLOSE;
import static org.pdfclown.common.util.Chars.ROUND_BRACKET_OPEN;
import static org.pdfclown.common.util.Chars.SLASH;
import static org.pdfclown.common.util.Objects.textLiteral;
import static org.pdfclown.common.util.Strings.S;
import static org.pdfclown.common.util.regex.Patterns.globToRegex;
import static org.pdfclown.common.util.system.Systems.parsePropertyBoolean;

import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.system.Systems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command line utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Clis {
  private static final Logger log = LoggerFactory.getLogger(Clis.class);

  /**
   * Parses a resource name filter into a predicate.
   * <p>
   * The predicate is expected to evaluate fully-qualified resource names (for example,
   * {@code "/org/pdfclown/layout/LayoutIT_testPdfAConformance.pdf"}).
   * </p>
   *
   * @param filter
   *          ({@linkplain org.pdfclown.common.util.regex.Patterns#globToRegex(String) GLOB})
   *          Comma-separated resource names.
   * @return Ever-true if {@code filter} is true to {@link Systems#parsePropertyBoolean(String)};
   *         ever-false if {@code filter} is {@code null}.
   */
  public static Predicate<String> parseResourceNameFilter(@Nullable String filter) {
    return parseNameFilter(filter, $ -> !$.contains(S + SLASH) ? "/**" + $ : $);
  }

  /**
   * Parses a type name filter into a predicate.
   * <p>
   * The predicate is expected to evaluate fully-qualified type names (for example,
   * {@code "org.pdfclown.common.build.system.Clis"}).
   * </p>
   *
   * @param filter
   *          ({@linkplain org.pdfclown.common.util.regex.Patterns#globToRegex(String) GLOB})
   *          Comma-separated type names.
   * @return Ever-true if {@code filter} is true to {@link Systems#parsePropertyBoolean(String)};
   *         ever-false if {@code filter} is {@code null}.
   */
  public static Predicate<String> parseTypeNameFilter(@Nullable String filter) {
    return parseNameFilter(filter, $ -> !$.contains(S + DOT) ? "*." + $ : $);
  }

  /**
   * Parses a name filter into a predicate.
   * <p>
   * The predicate is expected to evaluate fully-qualified names.
   * </p>
   *
   * @param filter
   *          ({@linkplain org.pdfclown.common.util.regex.Patterns#globToRegex(String) GLOB})
   *          Comma-separated names.
   * @return Ever-true if {@code filter} is true to {@link Systems#parsePropertyBoolean(String)};
   *         ever-false if {@code filter} is {@code null}.
   */
  private static Predicate<String> parseNameFilter(@Nullable String filter,
      UnaryOperator<String> nameMapper) {
    String regex = null;
    boolean defaultResult = false;
    {
      if (filter != null) {
        // Any FQN?
        if (parsePropertyBoolean(filter)) {
          defaultResult = true;
        }
        // Specific FQNs.
        else {
          var b = new StringBuilder();
          String[] paramValueItems = filter.split(S + COMMA);
          for (int i = 0; i < paramValueItems.length; i++) {
            if (i > 0) {
              b.append(PIPE);
            }
            b.append(ROUND_BRACKET_OPEN).append(globToRegex(nameMapper.apply(
                paramValueItems[i]))).append(ROUND_BRACKET_CLOSE);
          }
          regex = b.toString();
        }
      }
    }

    log.debug("Filter {}: {}", textLiteral(filter),
        regex != null ? textLiteral(regex) + " (regex)" : defaultResult ? "ANY" : "NONE");

    if (regex != null)
      return Pattern.compile(regex).asMatchPredicate();
    else {
      final var result = defaultResult;
      return $ -> result;
    }
  }

  private Clis() {
  }
}
