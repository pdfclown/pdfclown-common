/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ArgumentFormatException.java) is part of pdfclown-common-util module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import static java.util.Objects.requireNonNullElse;

import org.jspecify.annotations.Nullable;

/**
 * Argument format violation.
 *
 * @author Stefano Chizzolini
 */
public class ArgumentFormatException extends ArgumentException {
  private final int offset;

  public ArgumentFormatException(@Nullable String argName, @Nullable Object argValue,
      int offset) {
    this(argName, argValue, offset, null);
  }

  public ArgumentFormatException(@Nullable String argName, @Nullable Object argValue, int offset,
      @Nullable String message) {
    this(argName, argValue, offset, message, null);
  }

  /**
  */
  public ArgumentFormatException(@Nullable String argName, @Nullable Object argValue, int offset,
      @Nullable String message, @Nullable Throwable cause) {
    super(argName, argValue, requireNonNullElse(message, "INVALID at index " + offset), cause);

    this.offset = offset;
  }

  /**
   * Position where the format violation was found.
   */
  public int getOffset() {
    return offset;
  }
}
