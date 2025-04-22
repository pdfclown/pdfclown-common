/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (Bytes.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common> (this Program).

  This Program is free software: you can redistribute it and/or modify it under the terms of the GNU
  Lesser General Public License (LGPL) as published by the Free Software Foundation, either version
  3 of the License, or (at your option) any later version.

  This Program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this Program.
  If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>.
 */
package org.pdfclown.common.util;

import static java.lang.System.arraycopy;

/**
 * Byte utilities.
 *
 * @author Stefano Chizzolini
 */
public class Bytes {
  public static final byte[] BYTE_ARRAY__EMPTY = new byte[0];

  /**
   * Concatenates the given arrays.
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
   * Concatenates the given arrays.
   */
  public static byte[] concat(byte[] a1, byte[] a2) {
    return concat(a1, a2, BYTE_ARRAY__EMPTY);
  }

  /**
   * Concatenates the given arrays.
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
}
