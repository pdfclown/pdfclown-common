/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (FlagSet.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import org.jspecify.annotations.NonNull;

/**
 * Flags.
 *
 * @param <E>
 *          Flag enum type.
 * @author Stefano Chizzolini
 */
public interface FlagSet<@NonNull E extends Flag> extends XtSet<E> {
  /**
   * Sets the given flag.
   *
   * @param flag
   *          Flag to set.
   * @param enabled
   *          Whether to enable the flag.
   */
  void set(E flag, boolean enabled);
}
