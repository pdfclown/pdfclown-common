/*
  SPDX-FileCopyrightText: © 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>
  SPDX-License-Identifier: LGPL-3.0-or-later

  Copyright 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (XtMap.java) is part of pdfclown-common-util module in pdfClown Common project (this
  Program).

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

import java.util.Iterator;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Extended map.
 *
 * @param <K>
 *          Key type.
 * @param <V>
 *          Value type.
 * @author Stefano Chizzolini
 */
public interface XtMap<K, V> extends Aggregation<Map.Entry<K, V>>, Map<K, V> {
  /**
   * Gets whether any of the given keys exists.
   *
   * @param keys
   *          Keys to evaluate.
   */
  @SuppressWarnings("unchecked")
  default boolean containsAnyKey(K... keys) {
    return getAny(keys) != null;
  }

  /**
   * Gets whether any of the given keys exists.
   *
   * @param key1
   *          First key to evaluate.
   * @param key2
   *          Second key to evaluate.
   */
  default boolean containsAnyKey(K key1, K key2) {
    return getAny(key1, key2) != null;
  }

  /**
   * Gets whether any of the given keys exists.
   *
   * @param key1
   *          First key to evaluate.
   * @param key2
   *          Second key to evaluate.
   * @param key3
   *          Third key to evaluate.
   */
  default boolean containsAnyKey(K key1, K key2, K key3) {
    return getAny(key1, key2, key3) != null;
  }

  /**
   * Gets the value corresponding to one of the given keys. Keys are evaluated sequentially until a
   * matching entry is found.
   *
   * @param keys
   *          Keys to evaluate.
   * @return {@code null}, if no match to {@code keys} was found.
   */
  @SuppressWarnings("unchecked")
  default @Nullable V getAny(K... keys) {
    for (K key : keys) {
      @Nullable
      V value = get(key);
      if (value != null)
        return value;
    }
    return null;
  }

  /**
   * Gets the value corresponding to one of the given keys. Keys are evaluated sequentially until a
   * matching entry is found.
   *
   * @param key1
   *          First key to evaluate.
   * @param key2
   *          Second key to evaluate.
   * @return {@code null}, if no match to the given keys was found.
   */
  default @Nullable V getAny(K key1, K key2) {
    @Nullable
    V value = get(key1);
    if (value != null)
      return value;

    return get(key2);
  }

  /**
   * Gets the value corresponding to one of the given keys. Keys are evaluated sequentially until a
   * matching entry is found.
   *
   * @param key1
   *          First key to evaluate.
   * @param key2
   *          Second key to evaluate.
   * @param key3
   *          Third key to evaluate.
   * @return {@code null}, if no match to the given keys was found.
   */
  default @Nullable V getAny(K key1, K key2, K key3) {
    @Nullable
    V value = get(key1);
    if (value != null)
      return value;

    value = get(key2);
    if (value != null)
      return value;

    return get(key3);
  }

  /**
   * Gets the key associated to the given value.
   *
   * @param value
   * @implNote The default implementation doesn't rely on bidirectional maps, to say that the only
   *           abstract way to retrieve a key from a value is to iterate the whole map (O(n)
   *           complexity). It is therefore recommended to override it with an optimized solution.
   */
  default @Nullable K getKey(@Nullable V value) {
    return Aggregations.getKey(this, value);
  }

  @Override
  default boolean isEmpty() {
    return Aggregation.super.isEmpty();
  }

  @Override
  default Iterator<Map.@NonNull Entry<K, V>> iterator() {
    return entrySet().iterator();
  }

  @Override
  default void putAll(Map<? extends K, ? extends V> m) {
    for (var e : m.entrySet()) {
      put(e.getKey(), e.getValue());
    }
  }

  /**
   * Fluent {@link #put(Object, Object) put}.
   *
   * @param key
   * @param value
   * @return This object.
   */
  default XtMap<K, V> with(K key, V value) {
    put(key, value);
    return this;
  }

  /**
   * Fluent {@link #remove(Object) remove}.
   *
   * @param key
   * @return This object.
   */
  default XtMap<K, V> without(K key) {
    remove(key);
    return this;
  }
}
