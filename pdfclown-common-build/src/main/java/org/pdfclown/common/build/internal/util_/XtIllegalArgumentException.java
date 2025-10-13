/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (XtIllegalArgumentException.java) is part of pdfclown-common-build module in pdfClown
  Common project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.util_;

import static java.util.Objects.requireNonNullElse;
import static org.apache.commons.lang3.StringUtils.stripToNull;
import static org.pdfclown.common.build.internal.util_.Chars.BACKTICK;
import static org.pdfclown.common.build.internal.util_.Chars.COLON;
import static org.pdfclown.common.build.internal.util_.Chars.ROUND_BRACKET_CLOSE;
import static org.pdfclown.common.build.internal.util_.Chars.ROUND_BRACKET_OPEN;
import static org.pdfclown.common.build.internal.util_.Chars.SPACE;
import static org.pdfclown.common.build.internal.util_.Objects.basicLiteral;

import org.jspecify.annotations.Nullable;

/**
 * Extended {@link IllegalArgumentException}.
 * <p>
 * NOTE: {@code null} {@link #getArgValue() argValue} means the argument was defined on caller side,
 * but wasn't passed to this exception, for any reason (such as to avoid leaking of sensitive
 * information); conversely, in case of argument undefined on caller side, use
 * {@link NullPointerException} (see {@link java.util.Objects#requireNonNull(Object)}) instead.
 * </p>
 *
 * @author Stefano Chizzolini
 */
public class XtIllegalArgumentException extends IllegalArgumentException {
  private static String buildMessage(String argName,
      @Nullable Object argValue, @Nullable String message) {
    var b = new StringBuilder();
    b.append(BACKTICK).append(argName).append(BACKTICK);
    if (argValue != null) {
      b.append(SPACE).append(ROUND_BRACKET_OPEN).append(basicLiteral(argValue))
          .append(ROUND_BRACKET_CLOSE);
    }
    if (b.length() > 0) {
      b.append(COLON).append(SPACE);
    }
    return b.append(requireNonNullElse(stripToNull(message), "INVALID")).toString();
  }

  private final String argName;

  private final @Nullable Object argValue;

  public XtIllegalArgumentException(@Nullable String argName, @Nullable Object argValue) {
    this(argName, argValue, null);
  }

  public XtIllegalArgumentException(@Nullable String argName, @Nullable Object argValue,
      @Nullable String message) {
    this(argName, argValue, message, null);
  }

  /**
  */
  public XtIllegalArgumentException(@Nullable String argName, @Nullable Object argValue,
      @Nullable String message, @Nullable Throwable cause) {
    super(buildMessage(argName = requireNonNullElse(stripToNull(argName), "value"), argValue,
        message), cause);

    this.argName = argName;
    this.argValue = argValue;
  }

  /**
   * Argument name.
   */
  public String getArgName() {
    return argName;
  }

  /**
   * Argument value.
   *
   * @return {@code null}, if omitted.
   */
  public @Nullable Object getArgValue() {
    return argValue;
  }
}
