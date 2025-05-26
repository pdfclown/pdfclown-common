/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (CompositeMap.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Composite map, that is a map of homogeneous sub-maps.
 * <p>
 * NOTE: <i>Sub-map value types must belong to distinct inheritance lines</i> — since derived type
 * matching to corresponding entries is expected to be resolved traversing their ancestor graph, no
 * overlapping is acceptable (for further information, see {@link #get(Class)}).
 * </p>
 *
 * @param <K>
 *          Base key type, common to all sub-maps.
 * @param <V>
 *          Base value type, common to all sub-maps.
 * @param <M>
 *          Sub-map type.
 * @author Stefano Chizzolini
 */
public interface CompositeMap<K, V, M extends XtMap<? extends K, ? extends V>> {
  /**
   * Gets whether the given key has a match for the given type.
   *
   * @param type
   * @param key
   * @throws NullPointerException
   *           if {@code type} has no mapping.
   */
  @SuppressWarnings("null")
  default boolean containsKey(Class<? extends V> type, K key) {
    return get(type).containsKey(key);
  }

  /**
   * Gets the value map associated to the given type.
   *
   * @param type
   * @return {@code null}, if {@code type} has no mapping.
   * @implSpec Implementations are expected to support type resolution in case of missing entry: the
   *           ancestor graph (both concrete classes and interfaces) of the given type must be
   *           traversed until a match is found.
   */
  <T extends M> @Nullable T get(Class<? extends V> type);

  /**
   * Gets the value associated to the given key for the given type.
   *
   * @param <T>
   *          Value type.
   * @param type
   *          Value type.
   * @param key
   *          Key.
   * @throws NullPointerException
   *           if {@code type} has no mapping.
   */
  @SuppressWarnings({ "unchecked", "null" })
  default <T extends V> @Nullable T get(Class<T> type, K key) {
    return (T) get(type).get(key);
  }

  /**
   * Gets the key corresponding to the given value.
   *
   * @param value
   *          Value whose key is looked up.
   * @throws NullPointerException
   *           if {@code value} type has no mapping.
   */
  @SuppressWarnings({ "unchecked", "null" })
  default @Nullable K getKey(@NonNull V value) {
    return ((XtMap<K, V>) get((Class<V>) value.getClass())).getKey(value);
  }

  /**
   * Sets the value map associated to the given type.
   *
   * @param type
   * @param map
   * @throws NullPointerException
   *           if {@code type} has no mapping.
   */
  void put(Class<? extends V> type, M map);

  /**
   * Sets the value associated to the given key.
   *
   * @param key
   * @param value
   * @throws NullPointerException
   *           if {@code value} type has no mapping.
   */
  @SuppressWarnings({ "unchecked", "null" })
  default @Nullable V put(K key, @NonNull V value) {
    return ((XtMap<K, V>) get((Class<V>) value.getClass())).put(key, value);
  }
}
