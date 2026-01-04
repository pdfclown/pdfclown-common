/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (IndexedMap.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * Indexed map.
 * <p>
 * A map whose entries are associated to a positional index which can be arbitrarily manipulated by
 * users.
 * </p>
 *
 * @param <K>
 *          Map key type.
 * @param <V>
 *          Map value type.
 * @author Stefano Chizzolini
 */
public interface IndexedMap<K, V> extends XtMap<K, V> {
  /**
   * Gets the index of the key in this map.
   *
   * @return {@code -1} if this map does not contain the key.
   */
  default int indexOfKey(@Nullable K key) {
    int i = 0;
    for (K k : keySet()) {
      if (Objects.equals(k, key))
        return i;

      i++;
    }
    return -1;
  }

  /**
   * Gets the index of the value in this map.
   *
   * @return {@code -1} if this map does not contain the value.
   */
  default int indexOfValue(@Nullable V value) {
    int i = 0;
    for (K k : keySet()) {
      if (Objects.equals(get(k), value))
        return i;

      i++;
    }
    return -1;
  }

  /**
   * Gets the key at the position.
   *
   * @param index
   *          Entry position.
   * @throws IndexOutOfBoundsException
   *           if {@code index} is less, or equal, or greater than {@link #size() size}.
   */
  default @Nullable K keyOfIndex(int index) {
    int i = 0;
    for (K k : keySet()) {
      if (i++ == index)
        return k;
    }
    throw new IndexOutOfBoundsException(index);
  }

  /**
   * @implSpec Implementors must ensure that this method returns a set ordered according to the
   *           entry sequence.
   */
  @Override
  Set<K> keySet();

  /**
   * Moves the entry at the given position to the destination (if the new position is greater than
   * the old one, it is decreased to offset the entry removal from its old position).
   *
   * @param index
   *          Current position.
   * @param targetIndex
   *          Destination.
   * @return Moved entry.
   * @throws IndexOutOfBoundsException
   *           if {@code index} is less, or equal, or greater than {@link #size() size}.
   */
  Map.Entry<K, V> move(int index, int targetIndex);

  /**
   * Associates the value with the key in this map, either at the current position (if the map
   * previously contained a mapping for the key) or to the end of this map.
   *
   * @param key
   *          Entry key.
   * @param value
   *          Entry value.
   * @return Previous value associated with {@code key}, or {@code null} if there was no mapping for
   *         {@code key} (a {@code null} return can also indicate that the map previously associated
   *         {@code null} with {@code key}, if the implementation supports {@code null} values).
   */
  @Override
  V put(K key, V value);

  /**
   * Associates the value with the key in this map at the given position, shifting the entry
   * currently at that position (if any) and any subsequent entries to the right (adds one to their
   * indices). If the map previously contained a mapping for the key, the old value is replaced by
   * the specified one and the entry is moved to the new position (if the new position is greater
   * than the old one, it is decreased to offset the entry removal from its old position).
   *
   * @param index
   *          Entry position.
   * @param key
   *          Entry key.
   * @param value
   *          Entry value.
   * @return Previous value associated with {@code key}, or {@code null} if there was no mapping for
   *         {@code key} (a {@code null} return can also indicate that the map previously associated
   *         {@code null} with {@code key}, if the implementation supports {@code null} values).
   * @throws IndexOutOfBoundsException
   *           if {@code index} is less or greater than {@link #size() size}.
   */
  @Nullable
  V put(@Nullable K key, @Nullable V value, int index);
}
