/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ArgumentException.java) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.temp.util;

import static java.util.Objects.requireNonNullElse;
import static org.apache.commons.lang3.StringUtils.stripToEmpty;
import static org.apache.commons.lang3.StringUtils.stripToNull;
import static org.pdfclown.common.build.internal.temp.util.Objects.basicLiteral;
import static org.pdfclown.common.build.internal.temp.util.Objects.nonNull;
import static org.pdfclown.common.build.internal.temp.util.function.Functions.to;
import static org.pdfclown.common.build.util.Tuple.tuple;
import static org.pdfclown.common.util.Chars.BACKTICK;
import static org.pdfclown.common.util.Chars.COLON;
import static org.pdfclown.common.util.Chars.ROUND_BRACKET_CLOSE;
import static org.pdfclown.common.util.Chars.ROUND_BRACKET_OPEN;
import static org.pdfclown.common.util.Chars.SPACE;

import org.jspecify.annotations.Nullable;

/**
 * Enhanced {@link IllegalArgumentException}.
 *
 * @apiNote {@code null} {@link #getArgValue() argValue} means the argument was defined on caller
 *          side, but wasn't passed to this exception, for any reason (such as to avoid leaking
 *          sensitive information); conversely, in case of argument undefined on caller side, use
 *          {@link NullPointerException} (see {@link java.util.Objects#requireNonNull(Object)})
 *          instead.
 *          <p>
 *          <span class="important">IMPORTANT: DO NOT pass {@code argValue} unless in specific,
 *          non-sensitive cases.</span>
 *          </p>
 * @author Stefano Chizzolini
 */
@SuppressWarnings("serial" /* serialization is currently not a concern */)
public class ArgumentException extends IllegalArgumentException {
  private final String argName;
  private final @Nullable Object argValue;

  public ArgumentException(@Nullable String argName, @Nullable Object argValue) {
    this(argName, argValue, null);
  }

  public ArgumentException(@Nullable String argName, @Nullable Object argValue,
      @Nullable String message) {
    this(argName, argValue, message, null);
  }

  @SuppressWarnings("NullAway" /* false positive */)
  public ArgumentException(@Nullable String argName, @Nullable Object argValue,
      @Nullable String message, @Nullable Throwable cause) {
    super(to(tuple(message, argName = stripToEmpty(argName), argValue), $ -> {
      var b = new StringBuilder();
      if (!nonNull($.getE2()).isEmpty()) {
        b.append(BACKTICK).append($.getE2()).append(BACKTICK);
      }
      if ($.getE3() != null) {
        if (!b.isEmpty()) {
          b.append(SPACE);
        }
        b.append(ROUND_BRACKET_OPEN).append(basicLiteral($.getE3())).append(ROUND_BRACKET_CLOSE);
      }
      if (!b.isEmpty()) {
        b.append(COLON).append(SPACE);
      }
      return b.append(requireNonNullElse(stripToNull($.getE1()), "INVALID")).toString();
    }), cause);

    this.argName = argName;
    this.argValue = argValue;
  }

  /**
   * Argument name.
   */
  public String getArgName() {
    return argName;
  }

  /**
   * Argument value.
   *
   * @return {@code null}, if omitted.
   */
  public @Nullable Object getArgValue() {
    return argValue;
  }
}
