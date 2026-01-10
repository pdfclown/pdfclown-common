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

import java.util.LinkedList;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Stream utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Streams {
  /**
   * Gets a stream from a byte array.
   */
  public static IntStream intStream(byte[] array) {
    return IntStream.range(0, array.length).map($ -> array[$]);
  }

  /**
   * Gets a stream whose elements are in reverse order.
   *
   * @param stream
   *          <span class="warning">WARNING: MUST NOT be an infinite stream (such as
   *          {@link Random#ints()}), as it would cause {@link OutOfMemoryError}.</span>
   * @see <a href="https://www.dontpanicblog.co.uk/2020/10/23/reverse-order-stream/">Reverse order a
   *      Stream in Java</a>
   */
  public static <T> Stream<T> reverse(Stream<T> stream) {
    var stack = new LinkedList<T>();
    stream.forEachOrdered(stack::push);
    return stack.stream();
  }

  /**
   * Gets a stream of unsigned values from a byte array.
   */
  public static IntStream uintStream(byte[] array) {
    return IntStream.range(0, array.length).map($ -> array[$] & 0xFF);
  }

  private Streams() {
  }
}
