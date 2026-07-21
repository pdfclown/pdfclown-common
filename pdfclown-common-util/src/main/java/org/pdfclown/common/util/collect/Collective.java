/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Collective.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.collect;

import org.jspecify.annotations.Nullable;

/**
 * A plurality of elements.
 * <p>
 * Common interface bridging collections with maps.
 * </p>
 *
 * @param <E>
 *          Element type.
 * @author Stefano Chizzolini
 */
public interface Collective<E extends @Nullable Object> extends Iterable<E> {
  /**
   * Removes all the elements from this collective.
   *
   * @throws UnsupportedOperationException
   *           if this operation is not supported.
   */
  void clear();

  /**
   * Whether this collective contains no element.
   */
  default boolean isEmpty() {
    return size() == 0;
  }

  /**
   * Fluent {@link #clear() clear}.
   *
   * @return Self.
   * @throws UnsupportedOperationException
   *           if this operation is not supported.
   */
  default Collective<E> none() {
    clear();
    return this;
  }

  /**
   * Number of elements in this collective.
   */
  int size();
}
