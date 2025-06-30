/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Checks.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import static java.util.Objects.requireNonNullElse;
import static org.pdfclown.common.util.Exceptions.differingArg;
import static org.pdfclown.common.util.Exceptions.wrongArg;
import static org.pdfclown.common.util.Exceptions.wrongArgOpt;
import static org.pdfclown.common.util.ParamMessage.ARG;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.PolyNull;

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
    public static <T> Predicate<@Nullable T> isAmong(@NonNull T... options) {
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
    public static <T> Predicate<@Nullable T> isEqual(@Nullable Object otherValue) {
      return $ -> Objects.equals($, otherValue);
    }

    /**
     */
    public static Predicate<@Nullable String> isNotBlank() {
      return StringUtils::isNotBlank;
    }

    /**
     */
    public static <@NonNull T extends Number> Predicate<T> isNotNegative() {
      return $ -> $.intValue() >= 0;
    }

    /**
     */
    public static <@NonNull T extends Number> Predicate<T> isPositive() {
      return $ -> $.doubleValue() > 0;
    }

    /**
     * Whether values are identical.
     */
    public static <T> Predicate<@Nullable T> isSame(@Nullable Object otherValue) {
      return $ -> $ == otherValue;
    }

    /**
     * Whether value is an instance of the given types or undefined.
     */
    public static <T> Predicate<@Nullable T> isType(Class<?>... types) {
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
   * Checks the given value through custom logic.
   * <p>
   * Useful wherever inline code is impossible and calling a full-fledged validation method is
   * inconvenient (e.g., constructors).
   * </p>
   *
   * @param <T>
   *          Value type.
   * @param value
   *          Value to check.
   * @param checker
   *          Checks the given value, throwing a {@link RuntimeException} if {@code value} fails the
   *          check.
   * @return {@code value}
   * @throws RuntimeException
   *           if the check failed.
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
   *          Returns {@code false} if {@code value} fails the check.
   * @param exceptionSupplier
   *          Supplies the exception to throw if the check failed.
   * @return {@code value}
   * @throws RuntimeException
   *           (supplied by {@code exceptionSupplier}) if the check failed.
   */
  public static <T> T check(T value, Predicate<T> condition,
      Function<T, RuntimeException> exceptionSupplier) {
    if (condition.test(value))
      return value;
    else
      throw exceptionSupplier.apply(value);
  }

  /**
   * Checks that the given value is among the given options, or undefined.
   *
   * @param <T>
   *          Value type.
   * @param value
   *          Value to check.
   * @param exceptionSupplier
   *          Supplies the exception to throw if the check failed.
   * @param options
   *          Any expected value which {@code value} may match.
   * @return {@code value}
   * @throws RuntimeException
   *           (supplied by {@code exceptionSupplier}) if the check failed.
   */
  @SafeVarargs
  public static <@Nullable T> @PolyNull T checkAmong(@PolyNull T value,
      Function<T, RuntimeException> exceptionSupplier,
      @NonNull T... options) {
    return check(value, Condition.isAmong(options), exceptionSupplier);
  }

  /**
   * Checks that the given value is among the given options, or undefined.
   *
   * @param <T>
   *          Value type.
   * @param value
   *          Value to check.
   * @param name
   *          Name of the parameter, variable, or expression {@code value} was resolved from.
   * @param message
   *          Exception message.
   * @param options
   *          Any expected value which {@code value} may match.
   * @return {@code value}
   * @throws RuntimeException
   *           if the check failed.
   */
  @SafeVarargs
  public static <@Nullable T> @PolyNull T checkAmong(@PolyNull T value, @Nullable String name,
      @Nullable String message,
      @NonNull T... options) {
    return checkAmong(value, $ -> wrongArgOpt(name, value, message, options), options);
  }

  /**
   * Checks that the given value is among the given options, or undefined.
   *
   * @param <T>
   *          Value type.
   * @param value
   *          Value to check.
   * @param name
   *          Name of the parameter, variable, or expression {@code value} was resolved from.
   * @param options
   *          Any expected value which {@code value} may match.
   * @return {@code value}
   * @throws RuntimeException
   *           if the check failed.
   */
  @SafeVarargs
  public static <@Nullable T> @PolyNull T checkAmong(@PolyNull T value, @Nullable String name,
      @NonNull T... options) {
    //noinspection RedundantCast
    return checkAmong(value, name, (String) null, options);
  }

  /**
   * Checks that the given value is among the given options, or undefined.
   *
   * @param <T>
   *          Value type.
   * @param value
   *          Value to check.
   * @param options
   *          Any expected value which {@code value} may match.
   * @return {@code value}
   * @throws RuntimeException
   *           if the check failed.
   */
  @SafeVarargs
  public static <@Nullable T> @PolyNull T checkAmong(@PolyNull T value, @NonNull T... options) {
    return checkAmong(value, (String) null, options);
  }

  /**
   * @param <T>
   *          Value type.
   * @param value
   *          Value to check.
   * @param min
   *          Lower bound.
   * @param max
   *          Higher bound.
   * @return {@code value}
   * @throws RuntimeException
   *           if the check failed.
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
   *          Lower bound.
   * @param max
   *          Higher bound.
   * @param exceptionSupplier
   *          Supplies the exception to throw if the check failed.
   * @return {@code value}
   * @throws RuntimeException
   *           (supplied by {@code exceptionSupplier}) if the check failed.
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
   *          Lower bound.
   * @param max
   *          Higher bound.
   * @param name
   *          Name of the parameter, variable, or expression {@code value} was resolved from.
   * @return {@code value}
   * @throws RuntimeException
   *           if the check failed.
   */
  public static <@NonNull T extends Number> T checkBetween(T value, T min, T max,
      @Nullable String name) {
    return checkBetween(value, min, max, name, null);
  }

  /**
   * @param <T>
   *          Value type.
   * @param value
   *          Value to check.
   * @param min
   *          Lower bound.
   * @param max
   *          Higher bound.
   * @param name
   *          Name of the parameter, variable, or expression {@code value} was resolved from.
   * @param message
   *          Exception message.
   * @return {@code value}
   * @throws RuntimeException
   *           if the check failed.
   */
  public static <@NonNull T extends Number> T checkBetween(T value, T min, T max,
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
   *          Value to compare to {@code value}.
   * @return {@code value}
   * @throws RuntimeException
   *           if the check failed.
   */
  public static <@Nullable T> @PolyNull T checkEqual(@PolyNull T value,
      @Nullable Object otherValue) {
    return checkEqual(value, otherValue, (String) null);
  }

  /**
   * @param <T>
   *          Value type.
   * @param value
   *          Value to check.
   * @param otherValue
   *          Value to compare to {@code value}.
   * @param exceptionSupplier
   *          Supplies the exception to throw if the check failed.
   * @return {@code value}
   * @throws RuntimeException
   *           (supplied by {@code exceptionSupplier}) if the check failed.
   */
  public static <@Nullable T> @PolyNull T checkEqual(@PolyNull T value, @Nullable Object otherValue,
      Function<T, RuntimeException> exceptionSupplier) {
    return check(value, Condition.isEqual(otherValue), exceptionSupplier);
  }

  /**
   * @param <T>
   *          Value type.
   * @param value
   *          Value to check.
   * @param otherValue
   *          Value to compare to {@code value}.
   * @param name
   *          Name of the parameter, variable, or expression {@code value} was resolved from.
   * @return {@code value}
   * @throws RuntimeException
   *           if the check failed.
   */
  public static <@Nullable T> @PolyNull T checkEqual(@PolyNull T value, @Nullable Object otherValue,
      @Nullable String name) {
    return checkEqual(value, otherValue, name, null);
  }

  /**
   * @param <T>
   *          Value type.
   * @param value
   *          Value to check.
   * @param otherValue
   *          Value to compare to {@code value}.
   * @param name
   *          Name of the parameter, variable, or expression {@code value} was resolved from.
   * @param message
   *          Exception message.
   * @return {@code value}
   * @throws RuntimeException
   *           if the check failed.
   */
  public static <@Nullable T> @PolyNull T checkEqual(@PolyNull T value, @Nullable Object otherValue,
      @Nullable String name, @Nullable String message) {
    return checkEqual(value, otherValue, $ -> differingArg(name, value, otherValue, message));
  }

  /**
   * Checks the given index is within the sequence range (0 to {@code length}, inclusive).
   *
   * @throws IndexOutOfBoundsException
   *           if the check failed.
   * @see Objects#checkIndex(int, int)
   */
  public static int checkIndexAdd(int index, int length) {
    if (index < 0 || index > length)
      throw new IndexOutOfBoundsException(index);

    return index;
  }

  /**
   * @param <T>
   *          Value type.
   * @param value
   *          Value to check.
   * @return {@code value}
   * @throws RuntimeException
   *           if the check failed.
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
   *          Name of the parameter, variable, or expression {@code value} was resolved from.
   * @return {@code value}
   * @throws RuntimeException
   *           if the check failed.
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
   * @throws RuntimeException
   *           if the check failed.
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
   *          Name of the parameter, variable, or expression {@code value} was resolved from.
   * @return {@code value}
   * @throws RuntimeException
   *           if the check failed.
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
   *          Any expected type which {@code value} may match as an instance.
   * @return {@code value}
   * @throws RuntimeException
   *           if the check failed.
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
   *          Type which {@code value} is expected to match as an instance.
   * @return {@code value}
   * @throws RuntimeException
   *           if the check failed.
   */
  @SuppressWarnings("unchecked")
  public static <@Nullable T, U extends T> U checkType(T value, Class<U> type) {
    return (U) checkType(value, new Class[] { type });
  }

  /**
   * Checks that the given value is an instance of the given types, or undefined.
   *
   * @param <T>
   *          Value type.
   * @param value
   *          Value to check.
   * @param exceptionSupplier
   *          Supplies the exception to throw if the check failed.
   * @param types
   *          Any expected type which {@code value} may match as an instance.
   * @return {@code value}
   * @throws RuntimeException
   *           (supplied by {@code exceptionSupplier}) if the check failed.
   */
  @SafeVarargs
  public static <@Nullable T> T checkType(T value, Function<T, RuntimeException> exceptionSupplier,
      Class<? extends T>... types) {
    return check(value, Condition.isType(types), exceptionSupplier);
  }

  /**
   * Checks that the given value is an instance of the given types, or undefined.
   *
   * @param <T>
   *          Value type.
   * @param value
   *          Value to check.
   * @param name
   *          Name of the parameter, variable, or expression {@code value} was resolved from.
   * @param types
   *          Any expected type which {@code value} may match as an instance.
   * @return {@code value}
   * @throws RuntimeException
   *           if the check failed.
   */
  @SafeVarargs
  public static <@Nullable T> T checkType(T value, @Nullable String name,
      Class<? extends T>... types) {
    //noinspection RedundantCast
    return checkType(value, name, (String) null, types);
  }

  /**
   * Checks that the given value is an instance of the given types, or undefined.
   *
   * @param <T>
   *          Value type.
   * @param value
   *          Value to check.
   * @param name
   *          Name of the parameter, variable, or expression {@code value} was resolved from.
   * @param message
   *          Exception message.
   * @param types
   *          Any expected type which {@code value} may match as an instance.
   * @return {@code value}
   * @throws RuntimeException
   *           if the check failed.
   */
  @SafeVarargs
  public static <@Nullable T> T checkType(T value, @Nullable String name, @Nullable String message,
      Class<? extends T>... types) {
    return checkType(value, $ -> wrongArgOpt(name, value, message, types), types);
  }

  private Checks() {
  }
}
