/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Bytes.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import static java.lang.System.arraycopy;
import static java.util.Arrays.mismatch;
import static org.pdfclown.common.util.Objects.INDEX__NOT_FOUND;
import static org.pdfclown.common.util.Objects.equalsAny;

/**
 * Byte utilities.
 *
 * @author Stefano Chizzolini
 */
public class Bytes {
  public static final byte[] BYTE_ARRAY__EMPTY = new byte[0];

  /**
   * Concatenates byte arrays.
   */
  public static byte[] concat(byte[]... aa) {
    byte[] ret;
    {
      int l = 0;
      for (var a : aa) {
        l += a.length;
      }
      ret = new byte[l];
    }
    int destPos = 0;
    for (var a : aa) {
      arraycopy(a, 0, ret, destPos, a.length);
      destPos += a.length;
    }
    return ret;
  }

  /**
   * Concatenates arrays.
   */
  public static byte[] concat(byte[] a1, byte[] a2) {
    return concat(a1, a2, BYTE_ARRAY__EMPTY);
  }

  /**
   * Concatenates arrays.
   */
  public static byte[] concat(byte[] a1, byte[] a2, byte[] a3) {
    var ret = new byte[a1.length + a2.length + a3.length];
    int destPos = 0;
    if (a1.length > 0) {
      arraycopy(a1, 0, ret, destPos, a1.length);
      destPos += a1.length;
    }
    if (a2.length > 0) {
      arraycopy(a2, 0, ret, destPos, a2.length);
      destPos += a2.length;
    }
    if (a3.length > 0) {
      arraycopy(a3, 0, ret, destPos, a3.length);
    }
    return ret;
  }

  /**
   * Gets whether a byte array contains the given prefix at the given position.
   *
   * @param offset
   *          {@code array} position from which to start the match.
   */
  public static boolean containsAt(byte[] array, int offset, byte[] prefix) {
    return equalsAny(mismatch(array, offset, array.length, prefix, 0, prefix.length),
        prefix.length /* `prefix` smaller than `array` over the specified range */,
        INDEX__NOT_FOUND /* `prefix` equal to `array` over the specified range */);
  }

  /**
   * Gets whether a byte array starts with the given prefix.
   *
   * @implNote This method is an analogue to {@link String#startsWith(String)}.
   */
  public static boolean startsWith(byte[] array, byte[] prefix) {
    return containsAt(array, 0, prefix);
  }
}
