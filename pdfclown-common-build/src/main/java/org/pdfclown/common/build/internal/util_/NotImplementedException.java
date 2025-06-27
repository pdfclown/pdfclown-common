/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (NotImplementedException.java) is part of pdfclown-common-build module in pdfClown
  Common project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.util_;

import static org.pdfclown.common.build.internal.util_.Strings.isBlank;

import org.jspecify.annotations.Nullable;

/**
 * Thrown to indicate that a block of code has not been implemented yet.
 *
 * <p>
 * Differs from generic {@link UnsupportedOperationException} because the lack of support is
 * unintended and temporary rather than purposeful and permanent.
 * </p>
 *
 * @author Stefano Chizzolini
 */
public class NotImplementedException extends UnsupportedOperationException {
  private static final long serialVersionUID = 1L;

  public NotImplementedException() {
    this(null, null);
  }

  public NotImplementedException(@Nullable String message) {
    this(message, null);
  }

  /**
   */
  public NotImplementedException(@Nullable String message, @Nullable Throwable cause) {
    super(isBlank(message)
        ? "There's work for you! You reached a code block that hasn't been implemented yet."
        : message, cause);
  }
}
