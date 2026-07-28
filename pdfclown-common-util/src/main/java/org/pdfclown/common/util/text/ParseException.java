/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ParseException.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.text;

import static java.util.Objects.requireNonNullElse;
import static org.pdfclown.common.util.Chars.COLON;
import static org.pdfclown.common.util.Chars.SPACE;
import static org.pdfclown.common.util.Objects.textLiteral;
import static org.pdfclown.common.util.function.Functions.to;

import org.jspecify.annotations.Nullable;

/**
 * Thrown in case of unexpected parsing state.
 *
 * @author Stefano Chizzolini
 * @implNote This is an unchecked, more informative counterpart to {@link java.text.ParseException}.
 */
@SuppressWarnings("serial")
public class ParseException extends RuntimeException {
  private final TextPosition position;
  private final @Nullable Object token;
  private final @Nullable Object tokenType;

  public ParseException(@Nullable String message) {
    this(message, null, null, null, null);
  }

  public ParseException(@Nullable String message, @Nullable TextPosition position) {
    this(message, position, null, null, null);
  }

  public ParseException(@Nullable String message, @Nullable TextPosition position,
      @Nullable Object token, @Nullable Object tokenType, @Nullable Throwable cause) {
    super(to(requireNonNullElse(message, "Parsing FAILED"), $ -> {
      var b = new StringBuilder();
      if (token != null) {
        if (tokenType != null) {
          b.append(tokenType).append(SPACE);
        }
        b.append(textLiteral(token)).append(COLON).append(SPACE);
      }
      b.append($);
      if (position != null) {
        b.append(SPACE).append("at").append(SPACE).append(position);
      }
      return b.toString();
    }), cause);

    this.position = requireNonNullElse(position, TextPosition.absent());
    this.token = token;
    this.tokenType = tokenType;
  }

  public ParseException(@Nullable String message, @Nullable TextPosition position,
      @Nullable Throwable cause) {
    this(message, position, null, null, cause);
  }

  public ParseException(@Nullable String message, @Nullable Throwable cause) {
    this(message, null, null, null, cause);
  }

  public ParseException(Throwable cause) {
    this(null, cause);
  }

  /**
   * Position where the parsing failed.
   */
  public TextPosition getPosition() {
    return position;
  }

  /**
   * Token on which the parsing failed.
   */
  public @Nullable Object getToken() {
    return token;
  }

  /**
   * Type of the token on which the parsing failed.
   */
  public @Nullable Object getTokenType() {
    return tokenType;
  }
}
