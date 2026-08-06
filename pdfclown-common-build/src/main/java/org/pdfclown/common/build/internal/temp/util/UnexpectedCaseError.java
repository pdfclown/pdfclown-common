/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (UnexpectedCaseException.java) is part of pdfclown-common-build module in pdfClown
  Common project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.temp.util;

import static org.apache.commons.lang3.StringUtils.stripToEmpty;
import static org.pdfclown.common.build.internal.temp.util.Objects.basicLiteral;
import static org.pdfclown.common.util.Chars.BACKTICK;
import static org.pdfclown.common.util.Chars.ROUND_BRACKET_CLOSE;
import static org.pdfclown.common.util.Chars.ROUND_BRACKET_OPEN;
import static org.pdfclown.common.util.Chars.SPACE;

import org.jspecify.annotations.Nullable;

/**
 * Thrown to indicate that a {@linkplain #getValue() case} (such as an enum constant) violates logic
 * assumptions and, therefore, cannot be managed.
 * <p>
 * Typically used in the default case of switches to ensure that unmanaged cases don't fall through.
 * </p>
 *
 * @author Stefano Chizzolini
 */
@SuppressWarnings("serial" /* serialization is currently not a concern */)
public class UnexpectedCaseError extends AssertionError {
  private static String buildMessage(String name, @Nullable Object value,
      @Nullable String message) {
    var b = new StringBuilder();
    if (!name.isEmpty()) {
      b.append(BACKTICK).append(name)
          .append(BACKTICK);
    } else {
      b.append("Value");
    }
    b.append(SPACE).append(ROUND_BRACKET_OPEN).append(basicLiteral(value))
        .append(ROUND_BRACKET_CLOSE).append(SPACE).append("UNEXPECTED");
    if (!(message = stripToEmpty(message)).isEmpty()) {
      b.append(SPACE).append(ROUND_BRACKET_OPEN).append(message)
          .append(ROUND_BRACKET_CLOSE);
    }
    return b.toString();
  }

  private final String name;
  private final @Nullable Object value;

  public UnexpectedCaseError(@Nullable String name, @Nullable Object value) {
    this(name, value, null, null);
  }

  public UnexpectedCaseError(@Nullable String name, @Nullable Object value,
      @Nullable String message) {
    this(name, value, message, null);
  }

  public UnexpectedCaseError(@Nullable String name, @Nullable Object value,
      @Nullable String message, @Nullable Throwable cause) {
    super(buildMessage(name = stripToEmpty(name), value, message), cause);

    this.name = name;
    this.value = value;
  }

  /**
   * Name of the parameter, variable, field, or expression {@link #getValue() value} was resolved
   * from.
   */
  public String getName() {
    return name;
  }

  /**
   * Unexpected value.
   */
  public @Nullable Object getValue() {
    return value;
  }
}
