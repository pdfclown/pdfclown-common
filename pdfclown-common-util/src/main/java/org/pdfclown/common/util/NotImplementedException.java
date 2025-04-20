/*
  SPDX-FileCopyrightText: © 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>
  SPDX-License-Identifier: LGPL-3.0-or-later

  Copyright 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (NotImplementedException.java) is part of pdfclown-common-util module in pdfClown Common
  project (this Program).

  This Program is free software: you can redistribute it and/or modify it under the terms of the GNU
  Lesser General Public License (LGPL) as published by the Free Software Foundation, either version
  3 of the License, or (at your option) any later version.

  This Program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this Program.
  If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>.
 */
package org.pdfclown.common.util;

import static org.pdfclown.common.util.Strings.isBlank;

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
   * @param message
   * @param cause
   */
  public NotImplementedException(@Nullable String message, @Nullable Throwable cause) {
    super(isBlank(message)
        ? "There's work for you! You reached a code block that hasn't been implemented yet."
        : message, cause);
  }
}
