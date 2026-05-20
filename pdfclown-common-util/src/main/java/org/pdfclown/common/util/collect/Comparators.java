/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Comparators.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.collect;

import java.util.Comparator;
import java.util.Locale;

/**
 * Comparison utilities.
 *
 * @author Stefano Chizzolini
 */
public class Comparators {
  /**
   * Compares the {@linkplain Object#toString() string representation of objects} in alphabetic
   * order.
   */
  private static class AlphabeticOrderComparator implements Comparator<Object> {
    static final AlphabeticOrderComparator INSTANCE = new AlphabeticOrderComparator();

    @Override
    public int compare(Object c1, Object c2) {
      return c1.toString().toLowerCase(Locale.ROOT)
          .compareTo(c2.toString().toLowerCase(Locale.ROOT));
    }
  }

  /**
   * Gets a comparator to compare the {@linkplain Object#toString() string representation of
   * objects} in alphabetic order.
   */
  @SuppressWarnings("unchecked")
  public static <T> Comparator<T> alphabetic() {
    return (Comparator<T>) AlphabeticOrderComparator.INSTANCE;
  }
}
