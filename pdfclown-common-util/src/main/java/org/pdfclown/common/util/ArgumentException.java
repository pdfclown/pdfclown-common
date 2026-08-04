/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ArgumentException.java) is part of pdfclown-common-util module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import static java.util.Objects.requireNonNullElse;
import static org.apache.commons.lang3.StringUtils.stripToEmpty;
import static org.apache.commons.lang3.StringUtils.stripToNull;
import static org.pdfclown.common.util.Chars.BACKTICK;
import static org.pdfclown.common.util.Chars.COLON;
import static org.pdfclown.common.util.Chars.ROUND_BRACKET_CLOSE;
import static org.pdfclown.common.util.Chars.ROUND_BRACKET_OPEN;
import static org.pdfclown.common.util.Chars.SPACE;
import static org.pdfclown.common.util.Objects.basicLiteral;
import static org.pdfclown.common.util.Objects.nonNull;
import static org.pdfclown.common.util.collect.Tuple.tuple;
import static org.pdfclown.common.util.function.Functions.to;

import org.jspecify.annotations.Nullable;

/**
 * Enhanced {@link IllegalArgumentException}.
 * <p>
 * Anytime {@link #getArgValue() argValue} is omitted (for example, due to sensitive content),
 * {@link #ARG_VALUE__OMITTED} should be used as replacement; {@link #hasArgValue()} allows to check
 * whether the actual value is available.
 * </p>
 *
 * @author Stefano Chizzolini
 */
@SuppressWarnings("serial" /* serialization is currently not a concern */)
public class ArgumentException extends IllegalArgumentException {
  public static final Object ARG_VALUE__OMITTED = new Object() {
    @Override
    public String toString() {
      return "**OMITTED**";
    }
  };

  private final String argName;
  private final @Nullable Object argValue;

  /**
   * @param argValue
   *          ({@link #ARG_VALUE__OMITTED}, if not specified)
   */
  public ArgumentException(@Nullable String argName, @Nullable Object argValue) {
    this(argName, argValue, null);
  }

  /**
   * @param argValue
   *          ({@link #ARG_VALUE__OMITTED}, if not specified)
   */
  public ArgumentException(@Nullable String argName, @Nullable Object argValue,
      @Nullable String message) {
    this(argName, argValue, message, null);
  }

  /**
   * @param argValue
   *          ({@link #ARG_VALUE__OMITTED}, if not specified)
   */
  @SuppressWarnings({ "NullAway" /* false positive */, "ReferenceEquality" })
  public ArgumentException(@Nullable String argName, @Nullable Object argValue,
      @Nullable String message, @Nullable Throwable cause) {
    super(to(tuple(message, argName = stripToEmpty(argName), argValue), $ -> {
      var b = new StringBuilder();
      if (!nonNull($.getE2()).isEmpty()) {
        b.append(BACKTICK).append($.getE2()).append(BACKTICK);
      }
      if ($.getE3() != ARG_VALUE__OMITTED) {
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
   * @return {@link #ARG_VALUE__OMITTED}, if not {@linkplain #hasArgValue() specified}.
   */
  public @Nullable Object getArgValue() {
    return argValue;
  }

  /**
   * Whether {@link #getArgValue()} is specified.
   */
  @SuppressWarnings("ReferenceEquality")
  public boolean hasArgValue() {
    return argValue != ARG_VALUE__OMITTED;
  }
}
