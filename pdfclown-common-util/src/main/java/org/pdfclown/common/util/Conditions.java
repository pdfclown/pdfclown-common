/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Conditions.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import static java.util.Collections.singletonList;
import static org.pdfclown.common.util.Exceptions.wrongArg;
import static org.pdfclown.common.util.Exceptions.wrongArgOpt;
import static org.pdfclown.common.util.ParamMessage.ARG;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.PolyNull;

/**
 * Validation utilities.
 *
 * @author Stefano Chizzolini
 * @see Exceptions
 */
public final class Conditions {
  /**
   * Requires the value matches the given validator.
   * <p>
   * Useful wherever inline code is syntactically impossible and calling a full-fledged validation
   * method is inconvenient.
   * </p>
   *
   * @param <T>
   *          Value type.
   * @param value
   *          Value to validate.
   * @param validator
   *          Validates {@code value}, throwing a {@link RuntimeException} if invalid.
   * @return {@code value}
   * @throws RuntimeException
   *           if {@code value} is invalid.
   */
  public static <T> @PolyNull @Nullable T require(@PolyNull @Nullable T value,
      Consumer<T> validator) {
    return require(value, validator, null);
  }

  /**
   * Requires the value matches the given validator.
   * <p>
   * Useful wherever inline code is syntactically impossible and calling a full-fledged validation
   * method is inconvenient.
   * </p>
   *
   * @param <T>
   *          Value type.
   * @param value
   *          Value to validate.
   * @param validator
   *          Validates {@code value}, throwing a {@link RuntimeException} if invalid.
   * @return {@code value}
   * @throws RuntimeException
   *           if {@code value} is invalid.
   */
  public static <T> @PolyNull @Nullable T require(@PolyNull @Nullable T value,
      Consumer<T> validator, @Nullable String name) {
    try {
      validator.accept(value);
    } catch (RuntimeException ex) {
      throw name != null ? wrongArg(name, value, null, ex) : ex;
    }
    return value;
  }

  /**
   * Requires the value matches the given condition.
   *
   * @param <T>
   *          Value type.
   * @param value
   *          Value to validate.
   * @param condition
   *          Gets whether {@code value} is valid.
   * @param exceptionSupplier
   *          Supplies the exception to throw if {@code value} is invalid.
   * @return {@code value}
   * @throws RuntimeException
   *           (supplied by {@code exceptionSupplier}) if {@code value} is invalid.
   */
  public static <T> @PolyNull @Nullable T require(@PolyNull @Nullable T value,
      Predicate<T> condition, Function<T, RuntimeException> exceptionSupplier) {
    if (!condition.test(value))
      throw exceptionSupplier.apply(value);

    return value;
  }

  /**
   * Requires the insertion index is between 0 and {@code length}, inclusive.
   *
   * @param value
   *          Insertion index.
   * @param length
   *          Upper bound.
   * @throws IndexOutOfBoundsException
   *           if {@code value} is invalid.
   * @see Objects#checkIndex(int, int)
   */
  public static int requireAddIndexRange(int value, int length) {
    if (0 <= value && value <= length)
      return value;

    throw new IndexOutOfBoundsException(value);
  }

  /**
   * Requires the value is among the given options.
   *
   * @param value
   *          Value to validate.
   * @param options
   *          Valid values.
   * @return {@code value}
   * @throws XtIllegalArgumentException
   *           if {@code value} is invalid.
   */
  public static <T> @PolyNull @Nullable T requireAmong(@PolyNull @Nullable T value,
      Collection<T> options) {
    return requireAmong(value, options, null);
  }

  /**
   * Requires the value is among the given options.
   *
   * @param value
   *          Value to validate.
   * @param options
   *          Valid values.
   * @param name
   *          Name of the parameter, variable, or expression {@code value} was resolved from.
   * @return {@code value}
   * @throws XtIllegalArgumentException
   *           if {@code value} is invalid.
   */
  public static <T> @PolyNull @Nullable T requireAmong(@PolyNull @Nullable T value,
      Collection<T> options, @Nullable String name) {
    if (options.contains(value))
      return value;

    throw wrongArgOpt(name, value, null, options);
  }

  /**
   * Requires the value is at least the other one.
   *
   * @param value
   *          Value to validate.
   * @return {@code value}
   * @throws XtIllegalArgumentException
   *           if {@code value} is invalid.
   */
  public static <T extends Number> T requireAtLeast(T value, T otherValue) {
    return requireAtLeast(value, otherValue, null);
  }

  /**
   * Requires the value is at least the other one.
   *
   * @param value
   *          Value to validate.
   * @param name
   *          Name of the parameter, variable, or expression {@code value} was resolved from.
   * @return {@code value}
   * @throws XtIllegalArgumentException
   *           if {@code value} is invalid.
   */
  public static <T extends Number> T requireAtLeast(T value, T otherValue,
      @Nullable String name) {
    if (value.floatValue() >= otherValue.floatValue())
      return value;

    throw wrongArg(name, value, "MUST be at least " + ARG, otherValue);
  }

  /**
   * Requires the value is at most the other one.
   *
   * @param value
   *          Value to validate.
   * @return {@code value}
   * @throws XtIllegalArgumentException
   *           if {@code value} is invalid.
   */
  public static <T extends Number> T requireAtMost(T value, T otherValue) {
    return requireAtMost(value, otherValue, null);
  }

  /**
   * Requires the value is at most the other one.
   *
   * @param value
   *          Value to validate.
   * @param name
   *          Name of the parameter, variable, or expression {@code value} was resolved from.
   * @return {@code value}
   * @throws XtIllegalArgumentException
   *           if {@code value} is invalid.
   */
  public static <T extends Number> T requireAtMost(T value, T otherValue,
      @Nullable String name) {
    if (value.floatValue() <= otherValue.floatValue())
      return value;

    throw wrongArg(name, value, "MUST be at most " + ARG, otherValue);
  }

  /**
   * Requires the value is within unsigned-byte range, that is between 0 and 255 (inclusive).
   *
   * @param value
   *          Value to validate.
   * @return {@code value}
   * @throws XtIllegalArgumentException
   *           if {@code value} is invalid.
   */
  public static int requireByteRange(int value) {
    return requireByteRange(value, null);
  }

  /**
   * Requires the value is within unsigned-byte range, that is between 0 and 255 (inclusive).
   *
   * @param value
   *          Value to validate.
   * @param name
   *          Name of the parameter, variable, or expression {@code value} was resolved from.
   * @return {@code value}
   * @throws XtIllegalArgumentException
   *           if {@code value} is invalid.
   */
  public static int requireByteRange(int value, @Nullable String name) {
    return requireRange(value, 0, 255, name);
  }

  /**
   * Requires the value matches the other one.
   *
   * @param value
   *          Value to validate.
   * @param otherValue
   *          Valid value.
   * @return {@code value}
   * @throws XtIllegalArgumentException
   *           if {@code value} is invalid.
   */
  public static <T> @PolyNull @Nullable T requireEqual(@PolyNull @Nullable T value,
      @Nullable T otherValue) {
    return requireEqual(value, otherValue, null);
  }

  /**
   * Requires the value matches the other one.
   *
   * @param value
   *          Value to validate.
   * @param otherValue
   *          Valid value.
   * @param name
   *          Name of the parameter, variable, or expression {@code value} was resolved from.
   * @return {@code value}
   * @throws XtIllegalArgumentException
   *           if {@code value} is invalid.
   */
  public static <T> @PolyNull @Nullable T requireEqual(@PolyNull @Nullable T value,
      @Nullable T otherValue, @Nullable String name) {
    return requireAmong(value, singletonList(otherValue), name);
  }

  /**
   * Requires the value is greater than the other one.
   *
   * @param value
   *          Value to validate.
   * @return {@code value}
   * @throws XtIllegalArgumentException
   *           if {@code value} is invalid.
   */
  public static <T extends Number> T requireGreaterThan(T value, T otherValue) {
    return requireGreaterThan(value, otherValue, null);
  }

  /**
   * Requires the value is greater than the other one.
   *
   * @param value
   *          Value to validate.
   * @param name
   *          Name of the parameter, variable, or expression {@code value} was resolved from.
   * @return {@code value}
   * @throws XtIllegalArgumentException
   *           if {@code value} is invalid.
   */
  public static <T extends Number> T requireGreaterThan(T value, T otherValue,
      @Nullable String name) {
    if (value.floatValue() > otherValue.floatValue())
      return value;

    throw wrongArg(name, value, "MUST be greater than " + ARG, otherValue);
  }

  /**
   * Requires the value is less than the other one.
   *
   * @param value
   *          Value to validate.
   * @return {@code value}
   * @throws XtIllegalArgumentException
   *           if {@code value} is invalid.
   */
  public static <T extends Number> T requireLessThan(T value, T otherValue) {
    return requireLessThan(value, otherValue, null);
  }

  /**
   * Requires the value is less than the other one.
   *
   * @param value
   *          Value to validate.
   * @param name
   *          Name of the parameter, variable, or expression {@code value} was resolved from.
   * @return {@code value}
   * @throws XtIllegalArgumentException
   *           if {@code value} is invalid.
   */
  public static <T extends Number> T requireLessThan(T value, T otherValue,
      @Nullable String name) {
    if (value.floatValue() < otherValue.floatValue())
      return value;

    throw wrongArg(name, value, "MUST be less than " + ARG, otherValue);
  }

  /**
   * Requires the value is within normal range, that is between 0 and 1 (inclusive).
   *
   * @param <T>
   *          Value type.
   * @param value
   *          Value to validate.
   * @return {@code value}
   * @throws XtIllegalArgumentException
   *           if {@code value} is invalid.
   */
  public static <T extends Number> T requireNormalRange(T value) {
    return requireNormalRange(value, null);
  }

  /**
   * Requires the value is within normal range, that is between 0 and 1 (inclusive).
   *
   * @param <T>
   *          Value type.
   * @param value
   *          Value to validate.
   * @param name
   *          Name of the parameter, variable, or expression {@code value} was resolved from.
   * @return {@code value}
   * @throws XtIllegalArgumentException
   *           if {@code value} is invalid.
   */
  public static <T extends Number> T requireNormalRange(T value, @Nullable String name) {
    return requireRange(value, 0, 1, name);
  }

  /**
   * Requires the value is within the given range.
   *
   * @param value
   *          Value to validate.
   * @param range
   *          Range.
   * @return {@code value}
   * @throws XtIllegalArgumentException
   *           if {@code value} is invalid.
   */
  public static <T extends Number> T requireRange(T value, Range<T> range) {
    return requireRange(value, range, null);
  }

  /**
   * Requires the value is within the given range.
   *
   * @param value
   *          Value to validate.
   * @param range
   *          Range.
   * @param name
   *          Name of the parameter, variable, or expression {@code value} was resolved from.
   * @return {@code value}
   * @throws XtIllegalArgumentException
   *           if {@code value} is invalid.
   */
  public static <T extends Number> T requireRange(T value, Range<T> range, @Nullable String name) {
    if (range.contains(value))
      return value;

    throw wrongArg(name, value,
        "MUST be between " + ARG + " (" + ARG + ") and " + ARG + " (" + ARG + ")",
        range.getLow(), range.isLowInclusive() ? "inclusive" : "exclusive",
        range.getHigh(), range.isHighInclusive() ? "inclusive" : "exclusive");
  }

  /**
   * Requires the value is within the given range (inclusive).
   * <p>
   * For arbitrary bounds, use {@link #requireRange(Number, Range, String)} instead.
   * </p>
   *
   * @param value
   *          Value to validate.
   * @param min
   *          Lower bound.
   * @param max
   *          Higher bound.
   * @return {@code value}
   * @throws XtIllegalArgumentException
   *           if {@code value} is invalid.
   */
  public static <T extends Number> T requireRange(T value, int min, int max) {
    return requireRange(value, min, max, null);
  }

  /**
   * Requires the value is within the given range (inclusive).
   * <p>
   * For arbitrary bounds, use {@link #requireRange(Number, Range, String)} instead.
   * </p>
   *
   * @param value
   *          Value to validate.
   * @param min
   *          Lower bound.
   * @param max
   *          Higher bound.
   * @param name
   *          Name of the parameter, variable, or expression {@code value} was resolved from.
   * @return {@code value}
   * @throws XtIllegalArgumentException
   *           if {@code value} is invalid.
   */
  public static <T extends Number> T requireRange(T value, int min, int max,
      @Nullable String name) {
    if (value.floatValue() < min
        || value.floatValue() > max)
      throw wrongArg(name, value, "MUST be between " + ARG + " and " + ARG, min, max);

    return value;
  }

  /**
   * Requires the value is an instance of the given type (or undefined), and casts it accordingly.
   * <p>
   * Useful anytime an argument is required to be cast to a subtype in spite of its declaration,
   * such as in case of a specialized implementation of an interface.
   * </p>
   *
   * @param <T>
   *          Value type.
   * @param <U>
   *          Expected type.
   * @param value
   *          Value to validate.
   * @param type
   *          Type which {@code value} is expected to match as an instance.
   * @return {@code value}
   * @throws XtIllegalArgumentException
   *           if {@code value} is invalid.
   * @implNote Undefined {@code value} is allowed in accordance with the {@code instanceof}
   *           operator.
   */
  public static <T, U extends T> @PolyNull @Nullable U requireType(@PolyNull @Nullable T value,
      Class<U> type) {
    return requireType(value, type, null);
  }

  /**
   * Requires the value is an instance of the given type (or undefined), and casts it accordingly.
   * <p>
   * Useful anytime an argument is required to be cast to a subtype in spite of its declaration,
   * such as in case of a specialized implementation of an interface.
   * </p>
   *
   * @param <T>
   *          Value type.
   * @param <U>
   *          Expected type.
   * @param value
   *          Value to validate.
   * @param type
   *          Type which {@code value} is expected to match as an instance.
   * @param name
   *          Name of the parameter, variable, or expression {@code value} was resolved from.
   * @return {@code value}
   * @throws XtIllegalArgumentException
   *           if {@code value} is invalid.
   * @implNote Undefined {@code value} is allowed in accordance with the {@code instanceof}
   *           operator.
   */
  @SuppressWarnings("unchecked")
  public static <T, U extends T> @PolyNull @Nullable U requireType(@PolyNull @Nullable T value,
      Class<U> type, @Nullable String name) {
    return (U) requireType(value, List.of(type), name);
  }

  /**
   * Requires the value is an instance of the given types (or undefined).
   *
   * @param value
   *          Value to validate.
   * @param types
   *          Valid types.
   * @return {@code value}
   * @throws XtIllegalArgumentException
   *           if {@code value} is invalid.
   * @implNote Undefined {@code value} is allowed in accordance with the {@code instanceof}
   *           operator.
   */
  public static <T> @PolyNull @Nullable T requireType(@PolyNull @Nullable T value,
      Collection<Class<?>> types) {
    return requireType(value, types, null);
  }

  /**
   * Requires the value is an instance of the given types (or undefined).
   *
   * @param value
   *          Value to validate.
   * @param types
   *          Valid types.
   * @param name
   *          Name of the parameter, variable, or expression {@code value} was resolved from.
   * @return {@code value}
   * @throws XtIllegalArgumentException
   *           if {@code value} is invalid.
   * @implNote Undefined {@code value} is allowed in accordance with the {@code instanceof}
   *           operator.
   */
  public static <T> @PolyNull @Nullable T requireType(@PolyNull @Nullable T value,
      Collection<Class<?>> types, @Nullable String name) {
    if (value == null)
      return value;

    for (var type : types) {
      if (type.isInstance(value))
        return value;
    }

    throw wrongArgOpt(name, value, null, types);
  }

  private Conditions() {
  }
}
