/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Exceptions.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.internal.util_;

import static org.pdfclown.common.build.internal.util_.Objects.objTo;
import static org.pdfclown.common.build.internal.util_.ParamMessage.ARG;
import static org.pdfclown.common.build.internal.util_.Strings.COMMA;
import static org.pdfclown.common.build.internal.util_.Strings.CURLY_BRACE_CLOSE;
import static org.pdfclown.common.build.internal.util_.Strings.CURLY_BRACE_OPEN;
import static org.pdfclown.common.build.internal.util_.Strings.ROUND_BRACKET_CLOSE;
import static org.pdfclown.common.build.internal.util_.Strings.ROUND_BRACKET_OPEN;
import static org.pdfclown.common.build.internal.util_.Strings.SPACE;

import java.io.EOFException;
import java.util.Collection;
import java.util.NoSuchElementException;
import org.jspecify.annotations.Nullable;

/**
 * Exception utilities.
 *
 * @author Stefano Chizzolini
 * @see Conditions
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
   *          Parameterized message (use {@value ParamMessage#ARG} as argument placeholder).
   * @param args
   *          Message arguments. In case last argument is {@link Throwable}, it is assigned to
   *          {@link Throwable#getCause() cause}.
   */
  public static NotImplementedException TODO(@Nullable String format, @Nullable Object... args) {
    var message = ParamMessage.of(format, args);
    return new NotImplementedException(message.getDescription(), message.getCause());
  }

  public static NoSuchElementException missing() {
    return missing(null);
  }

  public static NoSuchElementException missing(@Nullable Object value) {
    return missing(value, null);
  }

  /**
   * @param value
   *          Mismatching value.
   * @param format
   *          Parameterized message (use {@value ParamMessage#ARG} as argument placeholder).
   * @param args
   *          Message arguments.
   */
  public static NoSuchElementException missing(@Nullable Object value, @Nullable String format,
      @Nullable Object... args) {
    String valueLiteral = objTo(value, Objects::toLiteralString);
    String message = objTo(format, $ -> ParamMessage.of($, args).getDescription());
    return new NoSuchElementException(
        valueLiteral == null ? message
            : message == null ? valueLiteral
            : String.format("%s (%s)", valueLiteral, message));
  }

  /**
   * @param format
   *          Parameterized message (use {@value ParamMessage#ARG} as argument placeholder).
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

  public static UnexpectedCaseError unexpected(@Nullable Object value) {
    return new UnexpectedCaseError(value);
  }

  /**
   * @param value
   *          Invalid value.
   * @param format
   *          Parameterized message (use {@value ParamMessage#ARG} as argument placeholder).
   * @param args
   *          Message arguments. In case last argument is {@link Throwable}, it is assigned to
   *          {@link Throwable#getCause() cause}.
   */
  public static UnexpectedCaseError unexpected(@Nullable Object value, @Nullable String format,
      @Nullable Object... args) {
    var message = ParamMessage.of(format, args);
    return new UnexpectedCaseError(value, message.getDescription(), message.getCause());
  }

  public static UnexpectedCaseError unexpected(@Nullable String name, @Nullable Object value) {
    return unexpected(value, name);
  }

  public static UnsupportedOperationException unsupported() {
    return new UnsupportedOperationException();
  }

  /**
   * @param format
   *          Parameterized message (use {@value ParamMessage#ARG} as argument placeholder).
   * @param args
   *          Message arguments. In case last argument is {@link Throwable}, it is assigned to
   *          {@link Throwable#getCause() cause}.
   */
  public static UnsupportedOperationException unsupported(@Nullable String format,
      @Nullable Object... args) {
    var message = ParamMessage.of(format, args);
    return new UnsupportedOperationException(message.getDescription(), message.getCause());
  }

  public static XtIllegalArgumentException wrongArg(@Nullable String name, @Nullable Object value) {
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
   *          Parameterized message (use {@value ParamMessage#ARG} as argument placeholder).
   * @param args
   *          Message arguments. In case last argument is {@link Throwable}, it is assigned to
   *          {@link Throwable#getCause() cause}.
   */
  public static XtIllegalArgumentException wrongArg(@Nullable String name,
      @Nullable Object value, @Nullable String format, @Nullable Object... args) {
    var message = ParamMessage.of(format, args);
    return new XtIllegalArgumentException(name, value, message.getDescription(),
        message.getCause());
  }

  public static <T> XtIllegalArgumentException wrongArgOpt(Collection<T> options) {
    return wrongArgOpt(null, null, null, options);
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
  public static <T> XtIllegalArgumentException wrongArgOpt(@Nullable String name,
      @Nullable Object value, @Nullable String description, Collection<T> options) {
    var b = new StringBuilder();
    if (description != null) {
      b.append(description).append(SPACE).append(ROUND_BRACKET_OPEN);
    } else {
      b.append("MUST be").append(SPACE);
    }
    if (options.size() > 1) {
      b.append("one of").append(SPACE).append(CURLY_BRACE_OPEN).append(SPACE);
    }
    /*
     * NOTE: In order to leverage `ParamMessage` formatting, the options are passed as arguments
     * rather than being appended as-is.
     */
    var args = new Object[options.size()];
    {
      int i = 0;
      for (var it = options.iterator(); it.hasNext(); i++) {
        if (i > 0) {
          b.append(COMMA).append(SPACE);
        }
        b.append(ARG);
        args[i] = it.next();
      }
    }
    if (options.size() > 1) {
      b.append(SPACE).append(CURLY_BRACE_CLOSE);
    }
    if (description != null) {
      b.append(ROUND_BRACKET_CLOSE);
    }
    return wrongArg(name, value, b.toString(), args);
  }

  /**
   * @param format
   *          Parameterized message (use {@value ParamMessage#ARG} as argument placeholder).
   * @param args
   *          Message arguments. In case last argument is {@link Throwable}, it is assigned to
   *          {@link Throwable#getCause() cause}.
   */
  public static IllegalStateException wrongState(@Nullable String format,
      @Nullable Object... args) {
    var message = ParamMessage.of(format, args);
    return new IllegalStateException(message.getDescription(), message.getCause());
  }

  public static IllegalStateException wrongState(Throwable cause) {
    return cause instanceof IllegalStateException ? (IllegalStateException) cause
        : new IllegalStateException(cause);
  }

  private Exceptions() {
  }
}
