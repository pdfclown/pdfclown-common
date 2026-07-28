/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ElementNotFoundException.java) is part of pdfclown-common-build module in pdfClown
  Common project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.temp.util;

import static org.pdfclown.common.build.internal.temp.util.Objects.textLiteral;
import static org.pdfclown.common.build.internal.temp.util.Strings.EMPTY;
import static org.pdfclown.common.util.Chars.ROUND_BRACKET_CLOSE;
import static org.pdfclown.common.util.Chars.ROUND_BRACKET_OPEN;
import static org.pdfclown.common.util.Chars.SPACE;

import org.jspecify.annotations.Nullable;

/**
 * Thrown if no element matched an associated reference.
 *
 * @author Stefano Chizzolini
 * @apiNote Useful for any kind of lookup.
 */
@SuppressWarnings("serial")
public class ElementNotFoundException extends RuntimeException {
  private final Object ref;

  public ElementNotFoundException(Object ref) {
    this(ref, null, null);
  }

  public ElementNotFoundException(Object ref, @Nullable String message) {
    this(ref, message, null);
  }

  public ElementNotFoundException(Object ref, @Nullable String message, @Nullable Throwable cause) {
    super("%s NOT FOUND%s".formatted(textLiteral(ref),
        message != null ? SPACE + ROUND_BRACKET_OPEN + message + ROUND_BRACKET_CLOSE : EMPTY),
        cause);

    this.ref = ref;
  }

  /**
   * Reference associated to the missing element (for example, its lookup identifier).
   */
  public Object getRef() {
    return ref;
  }
}
