/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (XnumProvider.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.service;

import org.pdfclown.common.util.Xnum;
import org.pdfclown.common.util.lang.Immutable;

/**
 * Augmented enumeration provider.
 *
 * @author Stefano Chizzolini
 */
@Immutable
public interface XnumProvider extends ServiceProvider {
  /**
   * Tries to load the implementation of the given {@link Xnum}-derived interface.
   *
   * @param <T>
   * @param type
   * @return Whether loading succeeded.
   */
  <T extends Xnum<?>> boolean load(Class<T> type);
}
