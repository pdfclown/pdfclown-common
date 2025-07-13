/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (XtIllegalArgumentException.java) is part of pdfclown-common-util module in pdfClown
  Common project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static org.pdfclown.common.util.Objects.toLiteralString;
import static org.pdfclown.common.util.Strings.COLON;
import static org.pdfclown.common.util.Strings.ROUND_BRACKET_CLOSE;
import static org.pdfclown.common.util.Strings.ROUND_BRACKET_OPEN;
import static org.pdfclown.common.util.Strings.SPACE;
import static org.pdfclown.common.util.Strings.strNormToNull;

import org.jspecify.annotations.Nullable;

/**
 * Extended {@link IllegalArgumentException}.
 *
 * @author Stefano Chizzolini
 */
public class XtIllegalArgumentException extends IllegalArgumentException {
  private static ParamMessage format(String argName,
      @Nullable Object value, @Nullable String format, @Nullable Object... args) {
    requireNonNull(argName, "`argName`");

    var b = new StringBuilder();
    b.append(argName);
    if (value != null) {
      b.append(SPACE).append(ROUND_BRACKET_OPEN).append(toLiteralString(value))
          .append(ROUND_BRACKET_CLOSE);
    }
    if (b.length() > 0) {
      b.append(COLON).append(SPACE);
    }
    b.append(requireNonNullElse(format, "INVALID"));
    return ParamMessage.of(b.toString(), args);
  }

  private final String argName;

  private final @Nullable Object argValue;

  /**
  */
  public XtIllegalArgumentException(String argName, @Nullable Object argValue,
      @Nullable String format, @Nullable Object... args) {
    this(argName = requireNonNull(strNormToNull(argName), "`argName`"), argValue,
        format(argName, argValue, format, args));
  }

  private XtIllegalArgumentException(String argName, @Nullable Object argValue,
      ParamMessage paramMessage) {
    super(paramMessage.getDescription(), paramMessage.getCause());

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
   * @return {@code null}, if it was omitted.
   */
  public @Nullable Object getArgValue() {
    return argValue;
  }
}
