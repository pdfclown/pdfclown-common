/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (Checks.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common> (this Program).

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

import static java.util.Objects.requireNonNullElse;
import static org.pdfclown.common.util.Exceptions.differingArg;
import static org.pdfclown.common.util.Exceptions.wrongArg;
import static org.pdfclown.common.util.Exceptions.wrongArgOpt;
import static org.pdfclown.common.util.ParamMessage.ARG;
import static org.pdfclown.common.util.Strings.strNormToNull;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Validation utilities.
 *
 * @author Stefano Chizzolini
 * @see Exceptions
 */
public final class Checks {
  /*
   * TODO: each condition should have an exception associated, so in case of violation should
   * automatically react instead of requiring a specific exception
   */
  /**
   * Validation conditions.
   */
  public static final class Condition {
    /**
     * Whether the input is among the given options or undefined.
     */
    @SafeVarargs
    public static <@Nullable T> Predicate<T> isAmong(@NonNull T... options) {
      return $ -> {
        if ($ == null)
          return true;

        for (T option : options) {
          if (option.equals($))
            return true;
        }
        return false;
      };
    }

    /**
     */
    public static <@NonNull T extends Number> Predicate<T> isBetween(T min, T max) {
      return $ -> $.doubleValue() >= min.doubleValue() && $.doubleValue() <= max.doubleValue();
    }

    /**
     * Whether values are equivalent.
     */
    public static <@Nullable T> Predicate<T> isEqual(@Nullable Object otherValue) {
      return $ -> Objects.equals($, otherValue);
    }

    /**
     */
    public static Predicate<@Nullable String> isNotBlank() {
      return $ -> !Strings.isBlank($);
    }

    /**
     */
    public static <@NonNull T extends Number> Predicate<T> isNotNegative() {
      return $ -> $.intValue() >= 0;
    }

    /**
     */
    public static <@Nullable T> Predicate<T> isNotNull() {
      return $ -> $ != null;
    }

    /**
     */
    public static <@NonNull T extends Number> Predicate<T> isPositive() {
      return $ -> $.doubleValue() > 0;
    }

    /**
     * Whether values are identical.
     */
    public static <@Nullable T> Predicate<T> isSame(@Nullable Object otherValue) {
      return $ -> $ == otherValue;
    }

    /**
     * Whether value is an instance of any of the given types or undefined.
     */
    public static <@Nullable T> Predicate<T> isType(Class<?>... types) {
      return $ -> {
        if ($ == null)
          return true;

        for (Class<?> type : types) {
          if (type.isInstance($))
            return true;
        }
        return false;
      };
    }

    /**
     */
    @SuppressWarnings("null")
    public static <T> Predicate<T> not(Predicate<T> p) {
      return p.negate();
    }

    private Condition() {
    }
  }

  /**
   * Checks the given value through an algorithm.
   *
   * @param <T>
   *          Value type.
   * @param value
   *          Value to check.
   * @param checker
   *          Checking algorithm. Expected to throw a {@link RuntimeException} if {@code value}
   *          doesn't pass its logic.
   * @return {@code value}
   * @apiNote Useful wherever inline code is impossible and calling a full-fledged validation method
   *          is inconvenient (eg, constructors).
   */
  public static <T> T check(T value, Consumer<T> checker) {
    checker.accept(value);
    return value;
  }

  /**
   * Checks the given value through a condition.
   *
   * @param <T>
   *          Value type.
   * @param value
   *          Value to check.
   * @param condition
   *          Check condition. Expected to return {@code false} if {@code value} doesn't pass its
   *          logic.
   * @param exceptionSupplier
   *          Exception to throw if {@code condition} is negative.
   * @return {@code value}
   * @throws RuntimeException
   *           (supplied by {@code exceptionSupplier}) if {@code condition} is negative.
   */
  public static <T> T check(T value, Predicate<T> condition,
      Function<T, RuntimeException> exceptionSupplier) {
    if (condition.test(value))
      return value;
    else
      throw exceptionSupplier.apply(value);
  }

  /**
   * Checks that the given value is among the given options (or null).
   *
   * @param <T>
   *          Value type.
   * @param value
   *          Value to check.
   * @param exceptionSupplier
   * @param options
   * @return {@code value}
   */
  @SafeVarargs
  public static <@Nullable T> T checkAmong(T value, Function<T, RuntimeException> exceptionSupplier,
      @NonNull T... options) {
    return check(value, Condition.isAmong(options), exceptionSupplier);
  }

  /**
   * Checks that the given value is among the given options (or null).
   *
   * @param <T>
   *          Value type.
   * @param value
   *          Value to check.
   * @param name
   * @param message
   * @param options
   * @return {@code value}
   */
  @SafeVarargs
  public static <@Nullable T> T checkAmong(T value, @Nullable String name, @Nullable String message,
      @NonNull T... options) {
    return checkAmong(value, $ -> wrongArgOpt(name, value, message, options), options);
  }

  /**
   * Checks that the given value is among the given options (or null).
   *
   * @param <T>
   *          Value type.
   * @param value
   *          Value to check.
   * @param name
   * @param options
   * @return {@code value}
   */
  @SafeVarargs
  public static <@Nullable T> T checkAmong(T value, @Nullable String name, @NonNull T... options) {
    return checkAmong(value, name, (String) null, options);
  }

  /**
   * Checks that the given value is among the given options (or null).
   *
   * @param <T>
   *          Value type.
   * @param value
   *          Value to check.
   * @param options
   * @return {@code value}
   */
  @SafeVarargs
  public static <@Nullable T> T checkAmong(T value, @NonNull T... options) {
    return checkAmong(value, (String) null, options);
  }

  /**
   * @param <T>
   *          Value type.
   * @param value
   *          Value to check.
   * @param min
   * @param max
   * @return {@code value}
   */
  public static <@NonNull T extends Number> T checkBetween(T value, T min, T max) {
    return checkBetween(value, min, max, (String) null);
  }

  /**
   * @param <T>
   *          Value type.
   * @param value
   *          Value to check.
   * @param min
   * @param max
   * @param exceptionSupplier
   * @return {@code value}
   */
  public static <@NonNull T extends Number> T checkBetween(T value, T min, T max,
      Function<T, RuntimeException> exceptionSupplier) {
    return check(value, Condition.isBetween(min, max), exceptionSupplier);
  }

  /**
   * @param <T>
   *          Value type.
   * @param value
   *          Value to check.
   * @param min
   * @param max
   * @param name
   * @return {@code value}
   */
  public static <@NonNull T extends Number> @NonNull T checkBetween(T value, T min, T max,
      @Nullable String name) {
    return checkBetween(value, min, max, name, null);
  }

  /**
   * @param <T>
   *          Value type.
   * @param value
   *          Value to check.
   * @param min
   * @param max
   * @param name
   * @param message
   * @return {@code value}
   */
  public static <@NonNull T extends Number> @NonNull T checkBetween(T value, T min, T max,
      @Nullable String name, @Nullable String message) {
    return checkBetween(value, min, max, $ -> wrongArg(name, value,
        ARG + " (should be between " + ARG + " and " + ARG + ")",
        requireNonNullElse(message, "INVALID"), min, max));
  }

  /**
   * @param <T>
   *          Value type.
   * @param value
   *          Value to check.
   * @param otherValue
   * @return {@code value}
   */
  public static <@Nullable T> T checkEqual(T value, @Nullable Object otherValue) {
    return checkEqual(value, otherValue, (String) null);
  }

  /**
   * @param <T>
   *          Value type.
   * @param value
   *          Value to check.
   * @param otherValue
   * @param exceptionSupplier
   * @return {@code value}
   */
  public static <@Nullable T> T checkEqual(T value, @Nullable Object otherValue,
      Function<T, RuntimeException> exceptionSupplier) {
    return check(value, Condition.isEqual(otherValue), exceptionSupplier);
  }

  /**
   * @param <T>
   *          Value type.
   * @param value
   *          Value to check.
   * @param otherValue
   * @param name
   * @return {@code value}
   */
  public static <@Nullable T> T checkEqual(T value, @Nullable Object otherValue,
      @Nullable String name) {
    return checkEqual(value, otherValue, name, null);
  }

  /**
   * @param <T>
   *          Value type.
   * @param value
   *          Value to check.
   * @param otherValue
   * @param name
   * @param message
   * @return {@code value}
   */
  public static <@Nullable T> T checkEqual(T value, @Nullable Object otherValue,
      @Nullable String name, @Nullable String message) {
    return checkEqual(value, otherValue, $ -> differingArg(name, value, otherValue, message));
  }

  /**
   * Checks the given index is within the sequence range (0 to {@code length}, inclusive).
   */
  public static int checkIndexAdd(int index, int length) {
    if (index < 0 || index > length)
      throw new IndexOutOfBoundsException(index);

    return index;
  }

  /**
   * @param value
   *          Value to check.
   * @return Normalized {@code value} (stripped of both leading and trailing
   *         {@linkplain Character#isWhitespace(int) white space}).
   */
  public static @Nullable String checkNotBlank(@Nullable String value) {
    return checkNotBlank(value, (String) null);
  }

  /**
   * @param value
   *          Value to check.
   * @param exceptionSupplier
   * @return Normalized {@code value} (stripped of both leading and trailing
   *         {@linkplain Character#isWhitespace(int) white space}).
   */
  public static @Nullable String checkNotBlank(@Nullable String value,
      Function<@Nullable String, RuntimeException> exceptionSupplier) {
    return check(strNormToNull(value), Condition.isNotNull(), exceptionSupplier);
  }

  /**
   * @param value
   *          Value to check.
   * @param name
   * @return Normalized {@code value} (stripped of both leading and trailing
   *         {@linkplain Character#isWhitespace(int) white space}).
   */
  public static @Nullable String checkNotBlank(@Nullable String value, @Nullable String name) {
    return checkNotBlank(value, name, null);
  }

  /**
   * @param value
   *          Value to check.
   * @param name
   * @param message
   * @return Normalized {@code value} (stripped of both leading and trailing
   *         {@linkplain Character#isWhitespace(int) white space}).
   */
  public static @Nullable String checkNotBlank(@Nullable String value, @Nullable String name,
      @Nullable String message) {
    return checkNotBlank(value,
        $ -> wrongArg(name, $, requireNonNullElse(message, "MUST be non-empty")));
  }

  /**
   * @param <T>
   *          Value type.
   * @param value
   *          Value to check.
   * @return {@code value}
   */
  public static <@NonNull T extends Number> T checkNotNegative(T value) {
    return checkNotNegative(value, null);
  }

  /**
   * @param <T>
   *          Value type.
   * @param value
   *          Value to check.
   * @param name
   * @return {@code value}
   */
  public static <@NonNull T extends Number> T checkNotNegative(T value, @Nullable String name) {
    return Checks.<@NonNull T>check(value, Condition.isNotNegative(),
        $ -> wrongArg(name, $, "MUST be non-negative"));
  }

  /**
   * @param <T>
   *          Value type.
   * @param value
   *          Value to check.
   * @return {@code value}
   */
  public static <@NonNull T extends Number> T checkPositive(T value) {
    return checkPositive(value, null);
  }

  /**
   * @param <T>
   *          Value type.
   * @param value
   *          Value to check.
   * @param name
   * @return {@code value}
   */
  public static <@NonNull T extends Number> T checkPositive(T value, @Nullable String name) {
    return Checks.<@NonNull T>check(value, Condition.isPositive(),
        $ -> wrongArg(name, $, "MUST be positive"));
  }

  /**
   * @param <T>
   *          Value type.
   * @param value
   *          Value to check.
   * @param types
   * @return {@code value}
   */
  @SafeVarargs
  public static <@Nullable T> T checkType(T value, Class<? extends T>... types) {
    return checkType(value, (String) null, types);
  }

  /**
   * @param <T>
   *          Value type.
   * @param <U>
   *          Expected type.
   * @param value
   *          Value to check.
   * @param type
   * @return {@code value}
   */
  @SuppressWarnings("unchecked")
  public static <@Nullable T, U extends T> U checkType(T value, Class<U> type) {
    return (U) checkType(value, new Class[] { type });
  }

  /**
   * Checks that the given value is an instance of any of the given types (or null).
   *
   * @param <T>
   *          Value type.
   * @param value
   *          Value to check.
   * @param exceptionSupplier
   * @param types
   * @return {@code value}
   */
  @SafeVarargs
  public static <@Nullable T> T checkType(T value, Function<T, RuntimeException> exceptionSupplier,
      Class<? extends T>... types) {
    return check(value, Condition.isType(types), exceptionSupplier);
  }

  /**
   * Checks that the given value is an instance of any of the given types (or null).
   *
   * @param <T>
   *          Value type.
   * @param value
   *          Value to check.
   * @param name
   * @param types
   * @return {@code value}
   */
  @SafeVarargs
  public static <@Nullable T> T checkType(T value, @Nullable String name,
      Class<? extends T>... types) {
    return checkType(value, name, (String) null, types);
  }

  /**
   * Checks that the given value is an instance of any of the given types (or null).
   *
   * @param <T>
   *          Value type.
   * @param value
   *          Value to check.
   * @param name
   * @param message
   * @param types
   * @return {@code value}
   */
  @SafeVarargs
  public static <@Nullable T> T checkType(T value, @Nullable String name, @Nullable String message,
      Class<? extends T>... types) {
    return checkType(value, $ -> wrongArgOpt(name, value, message, types), types);
  }

  private Checks() {
  }
}
