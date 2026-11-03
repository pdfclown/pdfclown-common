/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Normalizations.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import static org.pdfclown.common.util.Conditions.requireNotBlank;
import static org.pdfclown.common.util.Strings.EMPTY;

import org.jspecify.annotations.Nullable;

/**
 * Normalization utilities.
 * <p>
 * Convenience methods providing common combinations of transformation and validation.
 * </p>
 *
 * @author Stefano Chizzolini
 */
public final class Normalizations {
  /**
   * Normalizes a string stripping its leading and trailing whitespace.
   *
   * @return Empty, if {@code s} is undefined.
   * @see #normalizeToNonEmpty(String)
   */
  public static String normalizeToEmpty(@Nullable String s) {
    return s != null ? s.strip() : EMPTY;
  }

  /**
   * Normalizes a string stripping its leading and trailing whitespace.
   *
   * @throws ArgumentException
   *           if {@code s} is blank or undefined.
   * @implNote {@code s} is nullable to allow uncertain values, such as those resolved from maps.
   * @see #normalizeToEmpty(String)
   */
  public static String normalizeToNonEmpty(@Nullable String s) {
    return normalizeToNonEmpty(s, null);
  }

  /**
   * Normalizes a string stripping its leading and trailing whitespace.
   *
   * @throws ArgumentException
   *           if {@code s} is blank or undefined.
   * @implNote {@code s} is nullable to allow uncertain values, such as those resolved from maps.
   * @see #normalizeToEmpty(String)
   */
  public static String normalizeToNonEmpty(@Nullable String s, @Nullable String name) {
    return requireNotBlank(s, name).strip();
  }

  private Normalizations() {
  }
}
