/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ElementNotFoundException.java) is part of pdfclown-common-util module in pdfClown
  Common project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import static org.apache.commons.lang3.StringUtils.stripToNull;
import static org.pdfclown.common.util.Chars.ROUND_BRACKET_CLOSE;
import static org.pdfclown.common.util.Chars.ROUND_BRACKET_OPEN;
import static org.pdfclown.common.util.Chars.SPACE;
import static org.pdfclown.common.util.Objects.textLiteral;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.Strings.S;
import static org.pdfclown.common.util.function.Functions.toElse;

import org.jspecify.annotations.Nullable;

/**
 * Thrown if no element matched an associated reference.
 *
 * @author Stefano Chizzolini
 * @apiNote Useful for any kind of lookup.
 */
@SuppressWarnings("serial")
public class ElementNotFoundException extends RuntimeException {
  private final @Nullable Object ref;

  public ElementNotFoundException(@Nullable Object ref) {
    this(ref, null, null);
  }

  public ElementNotFoundException(@Nullable Object ref, @Nullable String message) {
    this(ref, message, null);
  }

  public ElementNotFoundException(@Nullable Object ref, @Nullable String message,
      @Nullable Throwable cause) {
    super("No element associated to %s%s".formatted(textLiteral(ref), toElse(stripToNull(message),
        $ -> S + SPACE + ROUND_BRACKET_OPEN + $ + ROUND_BRACKET_CLOSE, EMPTY)), cause);

    this.ref = ref;
  }

  /**
   * Reference associated to the missing element (for example, its lookup identifier).
   */
  public @Nullable Object getRef() {
    return ref;
  }
}
