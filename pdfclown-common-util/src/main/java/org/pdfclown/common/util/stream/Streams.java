/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Streams.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.stream;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Stream utilities.
 *
 * @author Stefano Chizzolini
 * @see org.apache.commons.lang3.stream.Streams
 */
public final class Streams {
  /**
   * Gets a sequential stream of values from an array of unknown type.
   *
   * @implNote Reference arrays are delegated to {@link Arrays#stream( Object[])}, whilst primitive
   *           ones are mapped to boxed values.
   */
  public static Stream<?> fromArray(Object array) {
    return array instanceof Object[] a ? Arrays.stream(a)
        : IntStream.range(0, Array.getLength(array)).mapToObj($ -> Array.get(array, $));
  }

  /**
   * Gets a sequential stream from a byte array.
   */
  public static IntStream intStream(byte[] array) {
    return IntStream.range(0, array.length).map($ -> array[$]);
  }

  /**
   * Gets a sequential stream of unsigned values from a byte array.
   */
  public static IntStream uintStream(byte[] array) {
    return IntStream.range(0, array.length).map($ -> array[$] & 0xFF);
  }

  private Streams() {
  }
}
