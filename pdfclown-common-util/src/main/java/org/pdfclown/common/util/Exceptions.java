/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Exceptions.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import static java.util.Objects.requireNonNullElse;
import static org.pdfclown.common.util.Objects.objToLiteralString;
import static org.pdfclown.common.util.ParamMessage.ARG;
import static org.pdfclown.common.util.Strings.COLON;
import static org.pdfclown.common.util.Strings.COMMA;
import static org.pdfclown.common.util.Strings.CURLY_BRACE_CLOSE;
import static org.pdfclown.common.util.Strings.CURLY_BRACE_OPEN;
import static org.pdfclown.common.util.Strings.ROUND_BRACKET_CLOSE;
import static org.pdfclown.common.util.Strings.ROUND_BRACKET_OPEN;
import static org.pdfclown.common.util.Strings.SPACE;

import java.io.EOFException;
import java.util.NoSuchElementException;
import org.jspecify.annotations.Nullable;

/**
 * Exception utilities.
 *
 * @author Stefano Chizzolini
 * @see Checks
 */
public final class Exceptions {
  public static EOFException EOF() {
    return new EOFException();
  }

  public static NotImplementedException TODO() {
    return new NotImplementedException();
  }

  /**
   * @param format
   *          Parameterized message (use <code>{}</code> as argument placeholder).
   * @param args
   *          Message arguments. In case last argument is {@link Throwable}, it is assigned to
   *          {@link Throwable#getCause() cause}.
   */
  public static NotImplementedException TODO(@Nullable String format, @Nullable Object... args) {
    var message = ParamMessage.of(format, args);
    return new NotImplementedException(message.getDescription(), message.getCause());
  }

  public static IllegalArgumentException differingArg(@Nullable String name,
      @Nullable Object value, @Nullable Object otherValue, @Nullable String description) {
    return wrongArg(name, value, ARG + " (should be " + ARG + ")",
        requireNonNullElse(description, "INVALID"), objToLiteralString(otherValue));
  }

  public static NoSuchElementException missing() {
    return new NoSuchElementException();
  }

  public static IllegalArgumentException requiredArg() {
    return requiredArg(null);
  }

  public static IllegalArgumentException requiredArg(@Nullable String name) {
    return wrongArg(name, null, "REQUIRED");
  }

  /**
   * @param format
   *          Parameterized message (use <code>{}</code> as argument placeholder).
   * @param args
   *          Message arguments. In case last argument is {@link Throwable}, it is assigned to
   *          {@link Throwable#getCause() cause}.
   */
  public static RuntimeException runtime(@Nullable String format, @Nullable Object... args) {
    var message = ParamMessage.of(format, args);
    return new RuntimeException(message.getDescription(), message.getCause());
  }

  /**
   * Wraps the given throwable into an unchecked exception.
   *
   * @param cause
   *          Throwable to wrap.
   * @return {@code cause}, if itself is an unchecked exception (pass-through).
   */
  public static RuntimeException runtime(Throwable cause) {
    return cause instanceof RuntimeException ? (RuntimeException) cause
        : new RuntimeException(cause);
  }

  /**
   * Wraps the given throwable into a {@linkplain UncheckedException temporary unchecked exception}.
   *
   * @param cause
   *          Throwable to wrap.
   * @return {@code cause}, if itself is an unchecked exception (pass-through).
   */
  public static RuntimeException unchecked(Throwable cause) {
    return cause instanceof RuntimeException
        ? (RuntimeException) cause
        : new UncheckedException(cause);
  }

  public static UnexpectedCaseException unexpected(@Nullable Object value) {
    return new UnexpectedCaseException(value);
  }

  /**
   * @param value
   *          Invalid value.
   * @param format
   *          Parameterized message (use <code>{}</code> as argument placeholder).
   * @param args
   *          Message arguments. In case last argument is {@link Throwable}, it is assigned to
   *          {@link Throwable#getCause() cause}.
   */
  public static UnexpectedCaseException unexpected(@Nullable Object value, @Nullable String format,
      @Nullable Object... args) {
    var message = ParamMessage.of(format, args);
    return new UnexpectedCaseException(value, message.getDescription(), message.getCause());
  }

  public static UnexpectedCaseException unexpected(@Nullable String name, @Nullable Object value) {
    return unexpected(value, name);
  }

  public static UnsupportedOperationException unsupported() {
    return new UnsupportedOperationException();
  }

  /**
   * @param format
   *          Parameterized message (use <code>{}</code> as argument placeholder).
   * @param args
   *          Message arguments. In case last argument is {@link Throwable}, it is assigned to
   *          {@link Throwable#getCause() cause}.
   */
  public static UnsupportedOperationException unsupported(@Nullable String format,
      @Nullable Object... args) {
    var message = ParamMessage.of(format, args);
    return new UnsupportedOperationException(message.getDescription(), message.getCause());
  }

  public static IllegalArgumentException wrongArg(@Nullable String name, @Nullable Object value) {
    return wrongArg(name, value, null);
  }

  public static IllegalArgumentException wrongArg(@Nullable String format,
      @Nullable Object... args) {
    var message = ParamMessage.of(format, args);
    return new IllegalArgumentException(message.getDescription(), message.getCause());
  }

  /**
   * @param name
   *          Name of the parameter, variable, or expression {@code value} was resolved from.
   * @param value
   *          Invalid value.
   * @param format
   *          Parameterized message (use <code>{}</code> as argument placeholder).
   * @param args
   *          Message arguments. In case last argument is {@link Throwable}, it is assigned to
   *          {@link Throwable#getCause() cause}.
   */
  public static IllegalArgumentException wrongArg(@Nullable String name,
      @Nullable Object value, @Nullable String format, @Nullable Object... args) {
    if (name == null && value != null) {
      name = "value";
    }

    ParamMessage message;
    {
      var b = new StringBuilder();
      if (name != null) {
        b.append(name);
      }
      if (value != null) {
        b.append(SPACE).append(ROUND_BRACKET_OPEN).append(objToLiteralString(value))
            .append(ROUND_BRACKET_CLOSE);
      }
      if (b.length() > 0) {
        b.append(COLON).append(SPACE);
      }
      b.append(requireNonNullElse(format, "INVALID"));
      message = ParamMessage.of(b.toString(), args);
    }
    return new IllegalArgumentException(message.getDescription(), message.getCause());
  }

  /**
   * @param <T>
   *          Value type.
   * @param name
   *          Name of the parameter, variable, or expression {@code value} was resolved from.
   * @param value
   *          Invalid value.
   * @param description
   *          Exception description.
   * @param options
   *          Any expected value which {@code value} may have matched.
   */
  @SafeVarargs
  public static <T> IllegalArgumentException wrongArgOpt(@Nullable String name,
      @Nullable Object value, @Nullable String description, T... options) {
    var b = new StringBuilder();
    if (description != null) {
      b.append(description).append(SPACE).append(ROUND_BRACKET_OPEN);
    } else {
      b.append("MUST be").append(SPACE);
    }
    if (options.length > 1) {
      b.append("one of").append(SPACE).append(CURLY_BRACE_OPEN).append(SPACE);
    }
    var args = new @Nullable Object[options.length];
    for (int i = 0; i < options.length; i++) {
      if (i > 0) {
        b.append(COMMA).append(SPACE);
      }
      b.append(ARG);
      args[i] = options[i];
    }
    if (options.length > 1) {
      b.append(SPACE).append(CURLY_BRACE_CLOSE);
    }
    if (description != null) {
      b.append(ROUND_BRACKET_CLOSE);
    }
    return wrongArg(name, value, b.toString(), args);
  }

  @SafeVarargs
  public static <T> IllegalArgumentException wrongArgOpt(T... options) {
    return wrongArgOpt(null, null, null, options);
  }

  public static IllegalStateException wrongState(Exception cause) {
    return cause instanceof IllegalStateException ? (IllegalStateException) cause
        : new IllegalStateException(cause);
  }

  /**
   * @param format
   *          Parameterized message (use <code>{}</code> as argument placeholder).
   * @param args
   *          Message arguments. In case last argument is {@link Throwable}, it is assigned to
   *          {@link Throwable#getCause() cause}.
   */
  public static IllegalStateException wrongState(@Nullable String format,
      @Nullable Object... args) {
    var message = ParamMessage.of(format, args);
    return new IllegalStateException(message.getDescription(), message.getCause());
  }

  private Exceptions() {
  }
}
