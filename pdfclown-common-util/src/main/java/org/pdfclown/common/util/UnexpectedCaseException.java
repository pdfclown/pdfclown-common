/*
  SPDX-FileCopyrightText: © 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>
  SPDX-License-Identifier: LGPL-3.0-or-later

  Copyright 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (UnexpectedCaseException.java) is part of pdfclown-common-util module in pdfClown Common
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

import static org.pdfclown.common.util.Objects.objToLiteralString;
import static org.pdfclown.common.util.Strings.strNorm;

import org.jspecify.annotations.Nullable;

/**
 * Thrown to indicate that a specific {@linkplain #getValue() case} (such as an enum constant) isn't
 * managed.
 * <p>
 * Typically used in the default case of switches to ensure that unmanaged cases don't fall through.
 * </p>
 *
 * @author Stefano Chizzolini
 */
public class UnexpectedCaseException extends AssertionError {
  private static final long serialVersionUID = 1L;

  private static String buildMessage(@Nullable Object value, @Nullable String message) {
    message = strNorm(message);

    var b = new StringBuilder("Unknown value: ").append(objToLiteralString(value));
    if (!message.isEmpty()) {
      b.append(" (").append(message).append(")");
    }
    return b.toString();
  }

  private final @Nullable Object value;

  public UnexpectedCaseException(@Nullable Object value) {
    this(value, null);
  }

  public UnexpectedCaseException(@Nullable Object value, @Nullable String message) {
    this(value, message, null);
  }

  /**
   * @param value
   * @param message
   * @param cause
   */
  public UnexpectedCaseException(@Nullable Object value, @Nullable String message,
      @Nullable Throwable cause) {
    super(buildMessage(value, message), cause);

    this.value = value;
  }

  public @Nullable Object getValue() {
    return value;
  }
}
