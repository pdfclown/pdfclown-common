/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  This file (XtSet.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import java.util.Collection;
import java.util.Set;

/**
 * Extended set.
 *
 * @param <E>
 *          Element type.
 * @author Stefano Chizzolini
 */
public interface XtSet<E> extends Set<E>, XtCollection<E> {
  @Override
  default boolean isEmpty() {
    return XtCollection.super.isEmpty();
  }

  @Override
  default boolean removeAll(Collection<?> c) {
    return XtCollection.super.removeAll(c);
  }
}
