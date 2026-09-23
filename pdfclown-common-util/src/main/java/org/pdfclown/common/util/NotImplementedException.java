/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (NotImplementedException.java) is part of pdfclown-common-util module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import static org.apache.commons.lang3.StringUtils.stripToNull;
import static org.pdfclown.common.util.Chars.ROUND_BRACKET_CLOSE;
import static org.pdfclown.common.util.Chars.ROUND_BRACKET_OPEN;
import static org.pdfclown.common.util.Chars.SPACE;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.Strings.S;
import static org.pdfclown.common.util.function.Functions.toElse;

import java.io.Serial;
import org.jspecify.annotations.Nullable;

/**
 * Thrown to indicate that a block of code has not been implemented yet.
 * <p>
 * Differs from generic {@link UnsupportedOperationException} because the lack of support is
 * unintended and temporary rather than purposeful and permanent.
 * </p>
 *
 * @author Stefano Chizzolini
 */
public class NotImplementedException extends UnsupportedOperationException {
  @Serial
  private static final long serialVersionUID = 1L;

  public NotImplementedException() {
    this(null, null);
  }

  public NotImplementedException(@Nullable String message) {
    this(message, null);
  }

  public NotImplementedException(@Nullable String message, @Nullable Throwable cause) {
    super("There's work for you! You reached a code block that hasn't been implemented yet"
        + toElse(stripToNull(message),
            $ -> S + SPACE + ROUND_BRACKET_OPEN + $ + ROUND_BRACKET_CLOSE, EMPTY),
        cause);
  }
}
