/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Aggregation.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

/**
 * Element aggregation.
 * <p>
 * Common interface bridging collections with maps.
 * </p>
 *
 * @param <E>
 *          Element type.
 * @author Stefano Chizzolini
 */
public interface Aggregation<E> extends Fluent, Iterable<E> {
  default boolean isEmpty() {
    return size() == 0;
  }

  default boolean isFilled() {
    return !isEmpty();
  }

  int size();
}
