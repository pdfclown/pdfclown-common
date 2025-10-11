/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Booleans.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.util_;

import org.jspecify.annotations.Nullable;

/**
 * Boolean utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Booleans {
  /**
   * Converts the boolean to the corresponding integer.
   *
   * @return {@code 1}, if {@code value} is {@code true}; {@code 0}, otherwise.
   */
  public static int boolToInt(boolean value) {
    return value ? 1 : 0;
  }

  /**
   * Converts the string to the corresponding boolean (case-insensitive).
   *
   * @return {@code null}, if no match is found.
   */
  public static @Nullable Boolean strToBool(@Nullable String value) {
    if (value != null) {
      value = value.toLowerCase();
      if (value.equals("true"))
        return Boolean.TRUE;
      else if (value.equals("false"))
        return Boolean.FALSE;
    }
    return null;
  }

  private Booleans() {
  }
}
