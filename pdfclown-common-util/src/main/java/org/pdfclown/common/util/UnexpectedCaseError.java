/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (UnexpectedCaseException.java) is part of pdfclown-common-util module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import static org.pdfclown.common.util.Objects.toLiteralString;
import static org.pdfclown.common.util.Strings.strNorm;

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
public class UnexpectedCaseError extends AssertionError {
  private static final long serialVersionUID = 1L;

  private static String buildMessage(@Nullable Object value, @Nullable String message) {
    var b = new StringBuilder("Value (").append(toLiteralString(value)).append(") UNEXPECTED");
    if (!(message = strNorm(message)).isEmpty()) {
      b.append(" (").append(message).append(")");
    }
    return b.toString();
  }

  private final @Nullable Object value;

  public UnexpectedCaseError(@Nullable Object value) {
    this(value, null);
  }

  public UnexpectedCaseError(@Nullable Object value, @Nullable String message) {
    this(value, message, null);
  }

  /**
   */
  public UnexpectedCaseError(@Nullable Object value, @Nullable String message,
      @Nullable Throwable cause) {
    super(buildMessage(value, message), cause);

    this.value = value;
  }

  /**
   * Unexpected value.
   */
  public @Nullable Object getValue() {
    return value;
  }
}
