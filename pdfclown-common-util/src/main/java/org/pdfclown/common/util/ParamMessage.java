/*
  SPDX-FileCopyrightText: © 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>
  SPDX-License-Identifier: LGPL-3.0-or-later

  Copyright 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (ParamMessage.java) is part of pdfclown-common-util module in pdfClown Common project
  (this Program).

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

import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.Strings.isEmpty;

import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parameterized message.
 * <p>
 * For convenience, emulates the behavior of slf4j-compliant logging libraries:
 * </p>
 * <ul>
 * <li>looks for {@value ParamMessage#ARG} placeholders for argument replacement in message
 * {@link #getDescription() description}</li>
 * <li>extracts {@link Throwable} from last message argument (if available) as message
 * {@link #getCause() cause} (see also
 * <a href="https://www.slf4j.org/faq.html#paramException">slf4j</a>)</li>
 * </ul>
 *
 * @author Stefano Chizzolini
 */
public class ParamMessage {
  /**
   * Message formatter.
   */
  public static class Formatter {
    private boolean quiet = true;

    /**
     * Formats the given parameterized string.
     * <p>
     * NOTE: If {@link #isQuiet() quiet}, cardinality mismatches between arguments and placeholders
     * are ignored and logged as warnings; otherwise, an exception is thrown.
     * </p>
     *
     * @param format
     *          Parameterized message (use {@value ParamMessage#ARG} as argument placeholder).
     * @param args
     *          Message arguments.
     */
    public String format(@Nullable String format, @Nullable Object[] args) {
      return format(format, args, args.length);
    }

    /**
     * Formats the given parameterized string.
     * <p>
     * NOTE: If {@link #isQuiet() quiet}, cardinality mismatches between arguments and placeholders
     * are ignored and logged as warnings; otherwise, an exception is thrown.
     * </p>
     *
     * @param format
     *          Parameterized message (use {@value ParamMessage#ARG} as argument placeholder).
     * @param args
     *          Message arguments.
     * @param argsCount
     *          Message arguments count.
     * @implNote Surrounding whitespace of {@code format} is preserved.
     */
    public String format(@Nullable String format, @Nullable Object[] args, int argsCount) {
      if (isEmpty(format))
        return EMPTY;

      assert format != null;

      var b = new StringBuilder();
      int index = 0;
      int oldIndex = index;
      for (int i = 0; i < argsCount; i++) {
        index = format.indexOf(ARG, oldIndex);
        if (index < 0) {
          warn("Placeholder '{}' missing for argument {} (format: '{}')", ARG, i, format);
          break;
        }

        b.append(format.substring(oldIndex, index)).append(formatArg(args[i]));
        oldIndex = index + ARG.length();
      }
      if (index >= 0 && format.indexOf(ARG, oldIndex) > 0) {
        warn("Argument {} missing for placeholder '{}' (format: '{}')", argsCount, ARG, format);
      }
      return b.append(format.substring(oldIndex)).toString();
    }

    /**
     * Whether formatting issues are logged instead of thrown.
     */
    public boolean isQuiet() {
      return quiet;
    }

    /**
     * Sets {@link #isQuiet() quiet}.
     */
    public void setQuiet(boolean value) {
      quiet = value;
    }

    /**
     * Formats the given argument.
     */
    protected String formatArg(@Nullable Object arg) {
      if (arg instanceof Class) {
        /*
         * NOTE: The names of classes belonging to common packages are simplified to reduce noise.
         */
        Class<?> type = (Class<?>) arg;
        arg = type.getPackageName().startsWith("java.lang")
            ? type.getSimpleName()
            : type.getName();
      }
      return Objects.toString(arg);
    }

    private void warn(String format, @Nullable Object... args) {
      if (quiet) {
        log.warn(format, args);
      } else
        throw runtime(format, args);
    }
  }

  private static final Logger log = LoggerFactory.getLogger(ParamMessage.class);

  /**
   * Argument placeholder.
   */
  public static final String ARG = "{}";

  /**
   * Default formatter.
   */
  private static final Formatter FORMATTER = new Formatter();

  /**
   * Formats the given parameterized string with the default formatter.
   * <p>
   * NOTE: For leniency, cardinality mismatches between arguments and placeholders are ignored and
   * logged as warnings.
   * </p>
   *
   * @param format
   *          Parameterized message (use {@value ParamMessage#ARG} as argument placeholder).
   * @param args
   *          Message arguments.
   */
  public static String format(@Nullable String format, @Nullable Object... args) {
    return format(FORMATTER, format, args, args.length);
  }

  /**
   * Resolves the given parameterized message with the given formatter.
   *
   * @param format
   *          Parameterized message (use {@value ParamMessage#ARG} as argument placeholder). It is
   *          assigned to {@link #getDescription() description}.
   * @param args
   *          Message arguments. In case last argument is {@link Throwable}, it is assigned to
   *          {@link #getCause() cause}.
   * @param formatter
   *          Applied formatter.
   */
  public static ParamMessage of(Formatter formatter, @Nullable String format,
      @Nullable Object... args) {
    int argsCount = args.length;
    var cause = argsCount > 0 && args[argsCount - 1] instanceof Throwable
        ? (Throwable) args[--argsCount]
        : null;
    return new ParamMessage(format(formatter, format, args, argsCount), cause);
  }

  /**
   * Resolves the given parameterized message with the default formatter.
   * <p>
   * NOTE: For leniency, cardinality mismatches between arguments and placeholders are ignored and
   * logged as warnings.
   * </p>
   *
   * @param format
   *          Parameterized message (use {@value ParamMessage#ARG} as argument placeholder). It is
   *          assigned to {@link #getDescription() description}.
   * @param args
   *          Message arguments. In case last argument is {@link Throwable}, it is assigned to
   *          {@link #getCause() cause}.
   */
  public static ParamMessage of(@Nullable String format, @Nullable Object... args) {
    return of(FORMATTER, format, args);
  }

  /**
   *
   * @param format
   *          Parameterized message (use {@value ParamMessage#ARG} as argument placeholder).
   * @param args
   *          Message arguments.
   * @param argsCount
   *          Message arguments count.
   * @param formatter
   *          Applied formatter.
   */
  private static String format(Formatter formatter, @Nullable String format,
      @Nullable Object[] args, int argsCount) {
    return formatter.format(format, args, argsCount);
  }

  private @Nullable Throwable cause;
  private String description;

  private ParamMessage(String description, @Nullable Throwable cause) {
    this.description = requireNonNull(description);
    this.cause = cause;
  }

  /**
   * Message cause.
   */
  public @Nullable Throwable getCause() {
    return cause;
  }

  /**
   * Message description.
   */
  public String getDescription() {
    return description;
  }
}
