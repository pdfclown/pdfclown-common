/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (XtSet.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.collect;

import java.util.Collection;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * Extended set.
 *
 * @param <E>
 *          Element type.
 * @author Stefano Chizzolini
 */
public interface XtSet<E extends @Nullable Object> extends Set<E>, XtCollection<E> {
  @Override
  default XtSet<E> and(E e) {
    return (XtSet<E>) XtCollection.super.and(e);
  }

  @Override
  default XtSet<E> andAll(Collection<? extends E> c) {
    return (XtSet<E>) XtCollection.super.andAll(c);
  }

  @Override
  default XtSet<E> but(E e) {
    return (XtSet<E>) XtCollection.super.but(e);
  }

  @Override
  default XtSet<E> butAll(Collection<?> c) {
    return (XtSet<E>) XtCollection.super.butAll(c);
  }

  @Override
  default boolean isEmpty() {
    return XtCollection.super.isEmpty();
  }

  @Override
  default XtSet<E> none() {
    return (XtSet<E>) XtCollection.super.none();
  }

  @Override
  default boolean removeAll(Collection<?> c) {
    return XtCollection.super.removeAll(c);
  }
}
