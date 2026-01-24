/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (XtCollection.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import java.util.Collection;
import java.util.Collections;

/**
 * Extended collection.
 *
 * @param <E>
 *          Element type.
 * @author Stefano Chizzolini
 */
public interface XtCollection<E> extends Aggregation<E>, Collection<E> {
  /**
   * Appends an array of elements.
   *
   * @param a
   *          Array of elements to append.
   * @return Whether this collection changed as a result of the call.
   */
  default boolean addAll(E[] a) {
    return Collections.addAll(this, a);
  }

  /**
   * Returns whether this collection contains any of the elements.
   */
  @SuppressWarnings("unchecked")
  default boolean containsAny(E... c) {
    for (E e : c) {
      if (contains(e))
        return true;
    }
    return false;
  }

  /**
   * Returns whether this collection contains any of the elements.
   */
  default boolean containsAny(E e1, E e2) {
    return contains(e1) || contains(e2);
  }

  /**
   * Returns whether this collection contains any of the elements.
   */
  default boolean containsAny(E e1, E e2, E e3) {
    return contains(e1) || contains(e2) || contains(e3);
  }

  @Override
  default boolean isEmpty() {
    return Aggregation.super.isEmpty();
  }

  @Override
  default boolean removeAll(Collection<?> c) {
    var ret = false;
    for (var o : c) {
      if (remove(o)) {
        ret = true;
      }
    }
    return ret;
  }

  /**
   * Fluent {@link Collection#add(Object) add(Object)}.
   *
   * @return This object.
   */
  default XtCollection<E> with(E e) {
    add(e);
    return this;
  }

  /**
   * Fluent {@link Collection#addAll(Collection) addAll(Collection)}.
   *
   * @return This object.
   */
  default XtCollection<E> withAll(Collection<? extends E> c) {
    addAll(c);
    return this;
  }

  /**
   * Fluent {@link Collection#remove(Object) remove(Object)}.
   *
   * @return This object.
   */
  default XtCollection<E> without(E e) {
    remove(e);
    return this;
  }

  /**
   * Fluent {@link Collection#removeAll(Collection) removeAll(Collection)}.
   *
   * @return This object.
   */
  default XtCollection<E> withoutAll(Collection<?> c) {
    removeAll(c);
    return this;
  }

  default XtCollection<E> withoutAny() {
    clear();
    return this;
  }
}
