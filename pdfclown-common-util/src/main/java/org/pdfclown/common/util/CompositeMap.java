/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (CompositeMap.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import static java.util.Objects.requireNonNull;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * A map of sub-maps whose entry values are {@linkplain Class#isInstance(Object) instances} of the
 * same type.
 * <p>
 * Consequently, <i>sub-map value types must belong to disjoint inheritance lines</i> — since
 * {@linkplain #getMap(Class) matching derived types are expected to be resolved traversing their
 * ancestor graph}, no overlapping is acceptable among sub-maps.
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
   * Gets whether the key has a match for the type.
   *
   * @param type
   *          Value type.
   * @param key
   *          Entry key.
   * @throws NullPointerException
   *           if {@code type} has no mapping.
   */
  default boolean containsKey(Class<? extends V> type, K key) {
    return requireNonNull((M) getMap(type), "`type`").containsKey(key);
  }

  /**
   * Gets the value associated to the key for the type.
   *
   * @param <T>
   *          Value type.
   * @param type
   *          Value type.
   * @param key
   *          Entry key.
   * @throws NullPointerException
   *           if {@code type} has no mapping.
   */
  @SuppressWarnings("unchecked")
  default <T extends V> @Nullable T get(Class<T> type, K key) {
    return (T) requireNonNull((M) getMap(type), "`type`").get(key);
  }

  /**
   * Gets the key corresponding to the value.
   *
   * @param value
   *          Value whose key is looked up.
   * @throws NullPointerException
   *           if {@code value} type has no mapping.
   */
  @SuppressWarnings("unchecked")
  default @Nullable K getKey(@NonNull V value) {
    return requireNonNull((XtMap<K, V>) getMap((Class<V>) value.getClass())).getKey(value);
  }

  /**
   * Gets the sub-map associated to the type.
   *
   * @param type
   *          Value type.
   * @return {@code null}, if {@code type} has no mapping.
   * @implSpec Implementations are expected to support type resolution in case of mismatch: the
   *           ancestor graph (both concrete classes and interfaces) of the type must be traversed
   *           until a match is found.
   */
  <R extends M> @Nullable R getMap(Class<? extends V> type);

  /**
   * Associates the value to the key.
   * <p>
   * The entry is placed in the sub-map corresponding to its value type.
   * </p>
   *
   * @param key
   *          Entry key.
   * @param value
   *          Entry value.
   * @throws NullPointerException
   *           if {@code value} type has no mapping.
   */
  @SuppressWarnings("unchecked")
  default @Nullable V put(K key, @NonNull V value) {
    return requireNonNull((XtMap<K, V>) getMap((Class<V>) value.getClass())).put(key, value);
  }

  /**
   * Associates the sub-map to the type.
   *
   * @param type
   *          Sub-map value type.
   * @param map
   *          Sub-map.
   * @throws NullPointerException
   *           if {@code type} has no mapping.
   */
  void putMap(Class<? extends V> type, M map);
}
