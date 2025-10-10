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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import org.apache.commons.lang3.exception.UncheckedException;
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
   * {@jada.reuseDoc ParamMessage.of(*):params}
   *
   * @param format
   *          Parameterized message (use {@value ParamMessage#ARG} as argument placeholder).
   * @param args
   *          Message arguments. In case last argument is {@link Throwable Throwable}, it is
   *          assigned to {@link org.pdfclown.common.build.internal.util_.ParamMessage#getCause()
   *          cause} (if {@link java.io.UncheckedIOException UncheckedIOException},
   *          {@link org.apache.commons.lang3.exception.UncheckedException UncheckedException}, or
   *          {@link java.lang.reflect.UndeclaredThrowableException UndeclaredThrowableException},
   *          it is unwrapped). {@jada.reuseDoc !end}
   */
  public static NotImplementedException TODO(@Nullable String format, @Nullable Object... args) {
    return throwable(NotImplementedException::new, format, args);
  }

  /**
   * {@jada.reuseDoc ParamMessage.of(*):params}
   *
   * @param format
   *          Parameterized message (use {@value ParamMessage#ARG} as argument placeholder).
   * @param args
   *          Message arguments. In case last argument is {@link Throwable Throwable}, it is
   *          assigned to {@link org.pdfclown.common.build.internal.util_.ParamMessage#getCause()
   *          cause} (if {@link java.io.UncheckedIOException UncheckedIOException},
   *          {@link org.apache.commons.lang3.exception.UncheckedException UncheckedException}, or
   *          {@link java.lang.reflect.UndeclaredThrowableException UndeclaredThrowableException},
   *          it is unwrapped). {@jada.reuseDoc !end}
   */
  public static IOException failedIO(@Nullable String format, @Nullable Object... args) {
    return throwable(IOException::new, format, args);
  }

  public static NoSuchElementException missing() {
    return missing(null);
  }

  public static NoSuchElementException missing(@Nullable Object value) {
    return missing(value, null);
  }

  /**
   * @param value
   *          Mismatching value. {@jada.reuseDoc ParamMessage.of(*):params}
   * @param format
   *          Parameterized message (use {@value ParamMessage#ARG} as argument placeholder).
   * @param args
   *          Message arguments. In case last argument is {@link Throwable Throwable}, it is
   *          assigned to {@link org.pdfclown.common.build.internal.util_.ParamMessage#getCause()
   *          cause} (if {@link java.io.UncheckedIOException UncheckedIOException},
   *          {@link org.apache.commons.lang3.exception.UncheckedException UncheckedException}, or
   *          {@link java.lang.reflect.UndeclaredThrowableException UndeclaredThrowableException},
   *          it is unwrapped). {@jada.reuseDoc !end}
   */
  public static NoSuchElementException missing(@Nullable Object value, @Nullable String format,
      @Nullable Object... args) {
    String valueLiteral = objTo(value, Objects::textLiteral);
    String message = objTo(format, $ -> ParamMessage.of($, args).getDescription());
    return new NoSuchElementException(
        valueLiteral == null ? message
            : message == null ? valueLiteral
            : String.format("%s (%s)", valueLiteral, message));
  }

  /**
   * @param path
   *          Missing path.
   */
  public static FileNotFoundException missingPath(Path path) {
    return new FileNotFoundException(ParamMessage.format(ARG + " MISSING", path));
  }

  /**
   * {@jada.reuseDoc ParamMessage.of(*):params}
   *
   * @param format
   *          Parameterized message (use {@value ParamMessage#ARG} as argument placeholder).
   * @param args
   *          Message arguments. In case last argument is {@link Throwable Throwable}, it is
   *          assigned to {@link org.pdfclown.common.build.internal.util_.ParamMessage#getCause()
   *          cause} (if {@link java.io.UncheckedIOException UncheckedIOException},
   *          {@link org.apache.commons.lang3.exception.UncheckedException UncheckedException}, or
   *          {@link java.lang.reflect.UndeclaredThrowableException UndeclaredThrowableException},
   *          it is unwrapped). {@jada.reuseDoc !end}
   */
  public static RuntimeException runtime(@Nullable String format, @Nullable Object... args) {
    return throwable(RuntimeException::new, format, args);
  }

  /**
   * Wraps the throwable into an unchecked exception.
   *
   * @param cause
   *          Throwable to wrap.
   * @return
   *         <ul>
   *         <li>{@code cause}, if it is an unchecked exception itself (pass-through)</li>
   *         <li>{@link UncheckedIOException}, if {@code cause} is {@link IOException}</li>
   *         <li>{@link UncheckedException}, if {@code cause} is any other {@linkplain Exception
   *         checked exception}</li>
   *         </ul>
   */
  public static RuntimeException runtime(Throwable cause) {
    return cause instanceof RuntimeException ? (RuntimeException) cause
        : cause instanceof IOException ? new UncheckedIOException((IOException) cause)
        : new UncheckedException(cause);
  }

  public static UnexpectedCaseError unexpected(@Nullable Object value) {
    return new UnexpectedCaseError(value);
  }

  /**
   * @param value
   *          Invalid value. {@jada.reuseDoc ParamMessage.of(*):params}
   * @param format
   *          Parameterized message (use {@value ParamMessage#ARG} as argument placeholder).
   * @param args
   *          Message arguments. In case last argument is {@link Throwable Throwable}, it is
   *          assigned to {@link org.pdfclown.common.build.internal.util_.ParamMessage#getCause()
   *          cause} (if {@link java.io.UncheckedIOException UncheckedIOException},
   *          {@link org.apache.commons.lang3.exception.UncheckedException UncheckedException}, or
   *          {@link java.lang.reflect.UndeclaredThrowableException UndeclaredThrowableException},
   *          it is unwrapped). {@jada.reuseDoc !end}
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
   * {@jada.reuseDoc ParamMessage.of(*):params}
   *
   * @param format
   *          Parameterized message (use {@value ParamMessage#ARG} as argument placeholder).
   * @param args
   *          Message arguments. In case last argument is {@link Throwable Throwable}, it is
   *          assigned to {@link org.pdfclown.common.build.internal.util_.ParamMessage#getCause()
   *          cause} (if {@link java.io.UncheckedIOException UncheckedIOException},
   *          {@link org.apache.commons.lang3.exception.UncheckedException UncheckedException}, or
   *          {@link java.lang.reflect.UndeclaredThrowableException UndeclaredThrowableException},
   *          it is unwrapped). {@jada.reuseDoc !end}
   */
  public static UnsupportedOperationException unsupported(@Nullable String format,
      @Nullable Object... args) {
    return throwable(UnsupportedOperationException::new, format, args);
  }

  public static XtIllegalArgumentException wrongArg(@Nullable String name, @Nullable Object value) {
    return wrongArg(name, value, null);
  }

  public static IllegalArgumentException wrongArg(@Nullable String format,
      @Nullable Object... args) {
    return throwable(IllegalArgumentException::new, format, args);
  }

  /**
   * @param name
   *          Name of the parameter, variable, or expression {@code value} was resolved from.
   * @param value
   *          Invalid value. {@jada.reuseDoc ParamMessage.of(*):params}
   * @param format
   *          Parameterized message (use {@value ParamMessage#ARG} as argument placeholder).
   * @param args
   *          Message arguments. In case last argument is {@link Throwable Throwable}, it is
   *          assigned to {@link org.pdfclown.common.build.internal.util_.ParamMessage#getCause()
   *          cause} (if {@link java.io.UncheckedIOException UncheckedIOException},
   *          {@link org.apache.commons.lang3.exception.UncheckedException UncheckedException}, or
   *          {@link java.lang.reflect.UndeclaredThrowableException UndeclaredThrowableException},
   *          it is unwrapped). {@jada.reuseDoc !end}
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
   * {@jada.reuseDoc ParamMessage.of(*):params}
   *
   * @param format
   *          Parameterized message (use {@value ParamMessage#ARG} as argument placeholder).
   * @param args
   *          Message arguments. In case last argument is {@link Throwable Throwable}, it is
   *          assigned to {@link org.pdfclown.common.build.internal.util_.ParamMessage#getCause()
   *          cause} (if {@link java.io.UncheckedIOException UncheckedIOException},
   *          {@link org.apache.commons.lang3.exception.UncheckedException UncheckedException}, or
   *          {@link java.lang.reflect.UndeclaredThrowableException UndeclaredThrowableException},
   *          it is unwrapped). {@jada.reuseDoc !end}
   */
  public static IllegalStateException wrongState(@Nullable String format,
      @Nullable Object... args) {
    return throwable(IllegalStateException::new, format, args);
  }

  public static IllegalStateException wrongState(Throwable cause) {
    return cause instanceof IllegalStateException ? (IllegalStateException) cause
        : new IllegalStateException(cause);
  }

  private static <T extends Throwable> T throwable(
      BiFunction<String, @Nullable Throwable, T> factory, @Nullable String format,
      @Nullable Object... args) {
    var message = ParamMessage.of(format, args);
    return factory.apply(message.getDescription(), message.getCause());
  }

  private Exceptions() {
  }
}
