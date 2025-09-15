/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Times.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import java.time.OffsetDateTime;
import java.util.GregorianCalendar;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.PolyNull;

/**
 * Time utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Times {
  /**
   * Converts the time.
   */
  public static @PolyNull @Nullable GregorianCalendar calendar(
      @PolyNull @Nullable OffsetDateTime value) {
    return value != null ? GregorianCalendar.from(value.toZonedDateTime()) : null;
  }

  /**
   * Converts the time.
   */
  public static @PolyNull @Nullable OffsetDateTime offsetDateTime(
      @PolyNull @Nullable GregorianCalendar value) {
    return value != null ? OffsetDateTime.from(value.toZonedDateTime()) : null;
  }

  private Times() {
  }
}
