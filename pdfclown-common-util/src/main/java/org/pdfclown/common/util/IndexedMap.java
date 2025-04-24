/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

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
 * Indexed map, ie a map whose entries are associated to a positional index which can be arbitrarily
 * manipulated by users.
 *
 * @param <K>
 *          Map key type.
 * @param <V>
 *          Map value type.
 * @author Stefano Chizzolini
 */
public interface IndexedMap<K, V> extends XtMap<K, V> {
  /**
   * Returns the index of the specified key in this map.
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
   * Returns the index of the specified value in this map.
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
   * Returns the key at the specified position.
   *
   * @param index
   * @throws IndexOutOfBoundsException
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
   * Moves the entry at the specified position to the specified destination (if the new position is
   * greater than the old one, it is decreased to offset the entry removal from its old position).
   *
   * @param index
   *          Current position.
   * @param targetIndex
   *          Destination.
   * @return Moved entry.
   * @throws IndexOutOfBoundsException
   */
  Map.Entry<K, V> move(int index, int targetIndex);

  /**
   * Associates the specified value with the specified key in this map, either at the current
   * position (if the map previously contained a mapping for the key) or to the end of this map.
   *
   * @param key
   * @param value
   * @return Previous value associated with {@code key}, or {@code null} if there was no mapping for
   *         {@code key} (a {@code null} return can also indicate that the map previously associated
   *         {@code null} with {@code key}, if the implementation supports {@code null} values).
   */
  @Override
  V put(K key, V value);

  /**
   * Associates the specified value with the specified key in this map at the specified position,
   * shifting the entry currently at that position (if any) and any subsequent entries to the right
   * (adds one to their indices). If the map previously contained a mapping for the key, the old
   * value is replaced by the specified one and the entry is moved to the new position (if the new
   * position is greater than the old one, it is decreased to offset the entry removal from its old
   * position).
   *
   * @param index
   * @param key
   * @param value
   * @return Previous value associated with {@code key}, or {@code null} if there was no mapping for
   *         {@code key} (a {@code null} return can also indicate that the map previously associated
   *         {@code null} with {@code key}, if the implementation supports {@code null} values).
   * @throws IndexOutOfBoundsException
   */
  @Nullable
  V put(@Nullable K key, @Nullable V value, int index);
}
