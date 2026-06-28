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

import static org.pdfclown.common.util.Exceptions.wrongArg;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
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

  /**
   * Maps the lists to a stream of combinations, each made of an element from each list at a certain
   * position.
   *
   * @param mapper
   *          Maps an element combination.
   * @param <T>
   *          Combination type.
   * @throws org.pdfclown.common.util.ArgumentException
   *           if {@code lists} sizes differ one another.
   */
  public static <T> Stream<T> zip(Function<Object[], T> mapper, List<?>... lists) {
    int size = lists[0].size();
    for (int i = 1; i < lists.length; i++) {
      if (lists[i].size() != size)
        throw wrongArg("lists", null,
            "Size of list {} MISMATCH ({} instead of {} -- all lists must be the same size)",
            i, lists[i].size(), size);
    }
    return IntStream.range(0, size)
        .mapToObj(i -> mapper.apply(Arrays.stream(lists)
            .map($ -> $.get(i))
            .toArray()));
  }

  private Streams() {
  }
}
