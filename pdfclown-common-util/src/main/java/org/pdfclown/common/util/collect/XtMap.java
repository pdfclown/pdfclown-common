/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (XtMap.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.collect;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.jspecify.annotations.Nullable;

/**
 * Extended map.
 *
 * @apiNote Values cannot be null; nonetheless, mutator methods accept {@code null} values as a
 *          shortcut to entry removal (for example, {@code myMap.put(myKey, null)} causes the entry
 *          associated to {@code myKey} to be removed from the map); this spares users redundant
 *          conditional branching.
 * @param <K>
 *          Key type.
 * @param <V>
 *          Value type.
 * @author Stefano Chizzolini
 * @see Collects#map()
 */
public interface XtMap<K extends @Nullable Object, V>
    extends Collective<Entry<K, V>>, Map<K, V> {
  /**
   * Fluent {@link #put(Object, Object) put}.
   *
   * @return Self.
   */
  default XtMap<K, V> and(K key, @Nullable V value) {
    put(key, value);
    return this;
  }

  /**
   * Fluent {@link #putAll(Map) putAll}.
   *
   * @return Self.
   */
  default XtMap<K, V> andAll(Map<? extends K, ? extends @Nullable V> m) {
    putAll(m);
    return this;
  }

  /**
   * Fluent {@link #remove(Object) remove}.
   *
   * @return Self.
   */
  default XtMap<K, V> but(K key) {
    remove(key);
    return this;
  }

  /**
   * Gets whether any of the keys exists.
   *
   * @param keys
   *          Keys to evaluate.
   */
  @SuppressWarnings("unchecked")
  default boolean containsAnyKey(K... keys) {
    return Collects.containsAnyKey(this, keys);
  }

  /**
   * Gets whether any of the keys exists.
   *
   * @param key1
   *          First key to evaluate.
   * @param key2
   *          Second key to evaluate.
   */
  default boolean containsAnyKey(K key1, K key2) {
    return Collects.containsAnyKey(this, key1, key2);
  }

  /**
   * Gets whether any of the keys exists.
   *
   * @param key1
   *          First key to evaluate.
   * @param key2
   *          Second key to evaluate.
   * @param key3
   *          Third key to evaluate.
   */
  default boolean containsAnyKey(K key1, K key2, K key3) {
    return Collects.containsAnyKey(this, key1, key2, key3);
  }

  /**
   * Gets the first non-null value associated to a key in the sequence.
   *
   * @return {@code null}, if no match was found.
   */
  @SuppressWarnings("unchecked")
  default @Nullable V getFirst(K... keys) {
    return Collects.getFirst(this, keys);
  }

  /**
   * Gets the first non-null value associated to a key in the sequence.
   *
   * @return {@code null}, if no match was found.
   */
  default @Nullable V getFirst(K key1, K key2) {
    return Collects.getFirst(this, key1, key2);
  }

  /**
   * Gets the first non-null value associated to a key in the sequence.
   *
   * @return {@code null}, if no match was found.
   */
  default @Nullable V getFirst(K key1, K key2, K key3) {
    return Collects.getFirst(this, key1, key2, key3);
  }

  /**
   * Gets the key associated to the value.
   *
   * @implNote The default implementation doesn't rely on bidirectional maps, to say that the only
   *           generic way to retrieve a key from a value is to iterate the whole map (O(n)
   *           complexity); it is therefore recommended to override it with an optimized solution.
   */
  @SuppressWarnings("NullAway" /*- TODO: false positive on `value` parameter which doesn't make
                                         sense, as the target `V` parameter of
                                         `Aggregations::getKey` is nullable too */)
  default @Nullable K getKey(@Nullable V value) {
    return Collects.getKey(this, value);
  }

  @Override
  default boolean isEmpty() {
    return Collective.super.isEmpty();
  }

  @Override
  default Iterator<Map.Entry<K, V>> iterator() {
    return entrySet().iterator();
  }

  @Override
  default XtMap<K, V> none() {
    return (XtMap<K, V>) Collective.super.none();
  }

  /**
   * @implNote {@code value} is purposely nullable (see {@linkplain XtMap API Note}).
   */
  @Override
  @Nullable
  V put(K key, @Nullable V value);

  @Override
  default void putAll(Map<? extends K, ? extends @Nullable V> m) {
    for (var e : m.entrySet()) {
      put(e.getKey(), e.getValue());
    }
  }
}
