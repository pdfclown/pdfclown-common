/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Aggregations.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.util;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

/**
 * Aggregation (ie, collection or map) utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Aggregations {
  // SourceFQN: org.pdfclown.common.util.Aggregations.cartesianProduct(..)
  /**
   * Gets the Cartesian product of the given lists.
   *
   * @return Sequential stream.
   */
  public static Stream<List<Object>> cartesianProduct(List<List<?>> lists) {
    return cartesianProduct(lists, 0);
  }

  // SourceFQN: org.pdfclown.common.util.Aggregations.entry(..)
  /**
   * Creates a new unmodifiable entry.
   *
   * @see Map#entry(Object, Object)
   */
  public static <K, V> Map.Entry<K, V> entry(@Nullable K key, @Nullable V value) {
    return new AbstractMap.SimpleImmutableEntry<>(key, value);
  }

  // SourceFQN: org.pdfclown.common.util.Aggregations.getKey(..)
  /**
   * Gets the key associated to the given value.
   * <p>
   * NOTE: This implementation is the most simple and inefficient, iterating the whole map (O(n)
   * complexity). Use it for occasional calls only.
   * </p>
   */
  public static <K, V> @Nullable K getKey(Map<K, V> map, @Nullable V value) {
    for (Map.Entry<K, V> entry : map.entrySet()) {
      if (Objects.equals(entry.getValue(), value))
        return entry.getKey();
    }
    return null;
  }

  private static Stream<List<Object>> cartesianProduct(List<List<?>> lists, int index) {
    if (index == lists.size())
      return Stream.of(new ArrayList<>());

    return lists.get(index).stream()
        .flatMap($ -> cartesianProduct(lists, index + 1)
            .map($$ -> {
              var newList = new ArrayList<>($$);
              newList.add(0, $);
              return newList;
            }));
  }

  private Aggregations() {
  }
}
