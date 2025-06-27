/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (UncheckedException.java) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.util_;

import org.jspecify.annotations.Nullable;

/**
 * Temporary unchecked wrapper of checked exception.
 * <p>
 * Useful to cross the boundary of lambda expressions, which by design don't support direct use of
 * checked exceptions; it is expected to be caught and unwrapped as soon as the boundary is crossed.
 * </p>
 *
 * @author Stefano Chizzolini
 */
public class UncheckedException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  UncheckedException(@Nullable Throwable cause) {
    super(cause);
  }
}
