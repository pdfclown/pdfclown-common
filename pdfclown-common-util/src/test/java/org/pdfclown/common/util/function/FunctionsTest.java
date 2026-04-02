/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (FunctionsTest.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.function;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.pdfclown.common.build.test.assertion.Assertions.ArgumentsStreamStrategy.cartesian;
import static org.pdfclown.common.build.test.assertion.Assertions.argumentsStream;
import static org.pdfclown.common.build.test.assertion.Assertions.assertParameterizedOf;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.pdfclown.common.build.test.assertion.Assertions.Expected;
import org.pdfclown.common.build.test.assertion.Assertions.Failure;
import org.pdfclown.common.util.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
public class FunctionsTest extends BaseTest {
  /**
   * Consumer covering both success and failure (in this case, {@link ClassCastException}).
   */
  public static final Consumer<Object> CONSUMER = $ -> ((Integer) $).doubleValue();
  public static final List<Integer> DEFAULT_VALUES = asList(null, 8);
  /**
   * Mapper covering both success (non-null (if argument is {@link Integer}) and null) and failure
   * (in this case, {@link ClassCastException}).
   */
  public static final Function<Object, @Nullable Integer> MAPPER =
      $ -> $ != null ? ((Integer) $).intValue() /* Fails if type mismatch */
          : null /* null */;
  /**
   * Objects covering both null and non-null (valid and invalid) argument values.
   */
  public static final List<Object> OBJS = asList(
      null,
      "hello" /* Non-null, invalid */,
      42 /* Non-null, valid */);
  /**
   * Suppliers covering both non-null and null return values.
   */
  public static final List<Supplier<Integer>> SUPPLIERS = asList(() -> null, () -> 21);

  static Stream<Arguments> let() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // [1] obj[0]: null
            null,
            // [2] obj[1]: "hello"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // [3] obj[2]: 42
            42),
        // obj
        OBJS);
  }

  static Stream<Arguments> to() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // [1] obj[0]: null
            null,
            // [2] obj[1]: "hello"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // [3] obj[2]: 42
            42),
        // obj
        OBJS);
  }

  static Stream<Arguments> toElse() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // obj[0]: null
            // [1] defaultValue[0]: null
            new Failure("NullPointerException", "defaultObj"),
            // [2] defaultValue[1]: 8
            8,
            //
            // obj[1]: "hello"
            // [3] defaultValue[0]: null
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // [4] defaultValue[1]: 8
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            //
            // obj[2]: 42
            // [5] defaultValue[0]: null
            42,
            // [6] defaultValue[1]: 8
            42),
        // obj
        OBJS,
        // defaultValue
        DEFAULT_VALUES);
  }

  static Stream<Arguments> toElseGet() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // obj[0]: null
            // [1] supplier[0]: "org.pdfclown.common.util.function.FunctionsT. . ."
            null,
            // [2] supplier[1]: "org.pdfclown.common.util.function.FunctionsT. . ."
            21,
            //
            // obj[1]: "hello"
            // [3] supplier[0]: "org.pdfclown.common.util.function.FunctionsT. . ."
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // [4] supplier[1]: "org.pdfclown.common.util.function.FunctionsT. . ."
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            //
            // obj[2]: 42
            // [5] supplier[0]: "org.pdfclown.common.util.function.FunctionsT. . ."
            42,
            // [6] supplier[1]: "org.pdfclown.common.util.function.FunctionsT. . ."
            42),
        // obj
        OBJS,
        // supplier
        SUPPLIERS);
  }

  static Stream<Arguments> toElseGetNonNull() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // obj[0]: null
            // [1] supplier[0]: "org.pdfclown.common.util.function.FunctionsT. . ."
            new Failure("NullPointerException", ""),
            // [2] supplier[1]: "org.pdfclown.common.util.function.FunctionsT. . ."
            21,
            //
            // obj[1]: "hello"
            // [3] supplier[0]: "org.pdfclown.common.util.function.FunctionsT. . ."
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // [4] supplier[1]: "org.pdfclown.common.util.function.FunctionsT. . ."
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            //
            // obj[2]: 42
            // [5] supplier[0]: "org.pdfclown.common.util.function.FunctionsT. . ."
            42,
            // [6] supplier[1]: "org.pdfclown.common.util.function.FunctionsT. . ."
            42),
        // obj
        OBJS,
        // supplier
        SUPPLIERS);
  }

  static Stream<Arguments> toNonNull() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // [1] obj[0]: null
            new Failure("NullPointerException", ""),
            // [2] obj[1]: "hello"
            new Failure("ClassCastException",
                "class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')"),
            // [3] obj[2]: 42
            42),
        // obj
        OBJS);
  }

  static Stream<Arguments> tryLet() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // [1] obj[0]: null
            null,
            // [2] obj[1]: "hello"
            "hello",
            // [3] obj[2]: 42
            42),
        // obj
        OBJS);
  }

  static Stream<Arguments> tryTo() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // [1] obj[0]: null
            null,
            // [2] obj[1]: "hello"
            null,
            // [3] obj[2]: 42
            42),
        // obj
        OBJS);
  }

  static Stream<Arguments> tryToElse() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // obj[0]: null
            // [1] defaultValue[0]: null
            new Failure("NullPointerException", "defaultObj"),
            // [2] defaultValue[1]: 8
            8,
            //
            // obj[1]: "hello"
            // [3] defaultValue[0]: null
            new Failure("NullPointerException", "defaultObj"),
            // [4] defaultValue[1]: 8
            8,
            //
            // obj[2]: 42
            // [5] defaultValue[0]: null
            42,
            // [6] defaultValue[1]: 8
            42),
        // obj
        OBJS,
        // defaultValue
        DEFAULT_VALUES);
  }

  static Stream<Arguments> tryToElseGet() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // obj[0]: null
            // [1] supplier[0]: "org.pdfclown.common.util.function.FunctionsT. . ."
            null,
            // [2] supplier[1]: "org.pdfclown.common.util.function.FunctionsT. . ."
            21,
            //
            // obj[1]: "hello"
            // [3] supplier[0]: "org.pdfclown.common.util.function.FunctionsT. . ."
            null,
            // [4] supplier[1]: "org.pdfclown.common.util.function.FunctionsT. . ."
            21,
            //
            // obj[2]: 42
            // [5] supplier[0]: "org.pdfclown.common.util.function.FunctionsT. . ."
            42,
            // [6] supplier[1]: "org.pdfclown.common.util.function.FunctionsT. . ."
            42),
        // obj
        OBJS,
        // supplier
        SUPPLIERS);
  }

  static Stream<Arguments> tryToElseGetNonNull() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // obj[0]: null
            // [1] supplier[0]: "org.pdfclown.common.util.function.FunctionsT. . ."
            new Failure("NullPointerException", ""),
            // [2] supplier[1]: "org.pdfclown.common.util.function.FunctionsT. . ."
            21,
            //
            // obj[1]: "hello"
            // [3] supplier[0]: "org.pdfclown.common.util.function.FunctionsT. . ."
            new Failure("NullPointerException", ""),
            // [4] supplier[1]: "org.pdfclown.common.util.function.FunctionsT. . ."
            21,
            //
            // obj[2]: 42
            // [5] supplier[0]: "org.pdfclown.common.util.function.FunctionsT. . ."
            42,
            // [6] supplier[1]: "org.pdfclown.common.util.function.FunctionsT. . ."
            42),
        // obj
        OBJS,
        // supplier
        SUPPLIERS);
  }

  @ParameterizedTest
  @MethodSource
  void let(Expected<Object> expected, Object obj) {
    assertParameterizedOf(
        () -> Functions.let(obj, CONSUMER),
        expected,
        () -> expectedGeneration(obj));
  }

  /**
   * NOTE: {@link Functions#quietly(org.apache.commons.lang3.function.FailableRunnable)} can NEVER
   * fail per-se; if the operation fails on call, that's part of its regular execution.
   */
  @Test
  void quietly() {
    var ref = new MutableObject<>();
    Functions.quietly(() -> {
      ref.setValue("DONE");
      throw new IllegalStateException("FAILED");
    });

    assertThat(ref.get(), is("DONE"));
  }

  /**
   * NOTE: {@link Functions#quietly(org.apache.commons.lang3.function.FailableRunnable)} can NEVER
   * fail per-se; if the operation fails on call, that's part of its regular execution.
   */
  @Test
  void quietly_Consumer() {
    var exceptionRef = new MutableObject<Throwable>();
    Functions.quietly(() -> {
      throw new IllegalStateException("FAILED");
    }, exceptionRef::setValue);

    assertThat(exceptionRef.get(), instanceOf(IllegalStateException.class));
    assertThat(exceptionRef.get().getMessage(), is("FAILED"));
  }

  @ParameterizedTest
  @MethodSource
  void to(Expected<Integer> expected, Object obj) {
    assertParameterizedOf(
        () -> Functions.to(obj, MAPPER),
        expected,
        () -> expectedGeneration(obj));
  }

  @ParameterizedTest
  @MethodSource
  void toElse(Expected<Integer> expected, Object obj, Integer defaultValue) {
    assertParameterizedOf(
        () -> Functions.toElse(obj, MAPPER, defaultValue),
        expected,
        () -> expectedGeneration(obj, defaultValue));
  }

  @ParameterizedTest
  @MethodSource
  void toElseGet(Expected<Integer> expected, Object obj, Supplier<Integer> supplier) {
    assertParameterizedOf(
        () -> Functions.toElseGet(obj, MAPPER, supplier),
        expected,
        () -> expectedGeneration(obj, supplier));
  }

  @ParameterizedTest
  @MethodSource
  void toElseGetNonNull(Expected<Integer> expected, Object obj, Supplier<Integer> supplier) {
    assertParameterizedOf(
        () -> Functions.toElseGetNonNull(obj, MAPPER, supplier),
        expected,
        () -> expectedGeneration(obj, supplier));
  }

  @ParameterizedTest
  @MethodSource
  void toNonNull(Expected<Integer> expected, Object obj) {
    assertParameterizedOf(
        () -> Functions.toNonNull(obj, MAPPER),
        expected,
        () -> expectedGeneration(obj));
  }

  @Test
  void tryGetElse__fail() {
    var ret = Functions.tryGetElse(() -> {
      throw new IllegalStateException("FAILED");
    }, "ALT");

    assertThat(ret, is("ALT"));
  }

  @Test
  void tryGetElse__ok() {
    var ret = Functions.tryGetElse(() -> "RESULT", "ALT");

    assertThat(ret, is("RESULT"));
  }

  @Test
  void tryGet__fail() {
    var ret = Functions.tryGet(() -> {
      throw new IllegalStateException("FAILED");
    });

    assertThat(ret, is(nullValue()));
  }

  @Test
  void tryGet__ok() {
    var ret = Functions.tryGet(() -> "RESULT");

    assertThat(ret, is("RESULT"));
  }

  @ParameterizedTest
  @MethodSource
  void tryLet(Expected<Object> expected, Object obj) {
    assertParameterizedOf(
        () -> Functions.tryLet(obj, CONSUMER::accept),
        expected,
        () -> expectedGeneration(obj));
  }

  @ParameterizedTest
  @MethodSource
  void tryTo(Expected<Integer> expected, Object obj) {
    assertParameterizedOf(
        () -> Functions.tryTo(obj, MAPPER::apply),
        expected,
        () -> expectedGeneration(obj));
  }

  @ParameterizedTest
  @MethodSource
  void tryToElse(Expected<Integer> expected, Object obj, Integer defaultValue) {
    assertParameterizedOf(
        () -> Functions.tryToElse(obj, MAPPER::apply, defaultValue),
        expected,
        () -> expectedGeneration(obj, defaultValue));
  }

  @ParameterizedTest
  @MethodSource
  void tryToElseGet(Expected<Integer> expected, Object obj, Supplier<Integer> supplier) {
    assertParameterizedOf(
        () -> Functions.tryToElseGet(obj, MAPPER::apply, supplier),
        expected,
        () -> expectedGeneration(obj, supplier));
  }

  @ParameterizedTest
  @MethodSource
  void tryToElseGetNonNull(Expected<Integer> expected, Object obj, Supplier<Integer> supplier) {
    assertParameterizedOf(
        () -> Functions.tryToElseGetNonNull(obj, MAPPER::apply, supplier),
        expected,
        () -> expectedGeneration(obj, supplier));
  }
}