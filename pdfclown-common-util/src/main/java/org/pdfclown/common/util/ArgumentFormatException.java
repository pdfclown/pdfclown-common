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

import static org.apache.commons.lang3.StringUtils.stripToNull;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.function.Functions.toElse;

import java.io.Serial;
import org.jspecify.annotations.Nullable;

/**
 * Argument format violation.
 *
 * @author Stefano Chizzolini
 */
public class ArgumentFormatException extends ArgumentException {
  @Serial
  private static final long serialVersionUID = 1L;

  private final int offset;

  public ArgumentFormatException(@Nullable String argName, @Nullable Object argValue, int offset) {
    this(argName, argValue, offset, null);
  }

  public ArgumentFormatException(@Nullable String argName, @Nullable Object argValue, int offset,
      @Nullable String message) {
    this(argName, argValue, offset, message, null);
  }

  public ArgumentFormatException(@Nullable String argName, @Nullable Object argValue, int offset,
      @Nullable String message, @Nullable Throwable cause) {
    super(argName, argValue, "at position %s%s".formatted(offset, toElse(stripToNull(message),
        $ -> " -- " + $, EMPTY)), cause);

    this.offset = offset;
  }

  /**
   * Position where the format violation was found.
   */
  public int getOffset() {
    return offset;
  }
}
