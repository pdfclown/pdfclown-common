/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ObjectsTest.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import static java.util.Arrays.asList;
import static java.util.List.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.pdfclown.common.build.test.assertion.Assertions.ArgumentsStreamConfig.cartesian;
import static org.pdfclown.common.build.test.assertion.Assertions.ArgumentsStreamConfig.simple;
import static org.pdfclown.common.build.test.assertion.Assertions.argumentsStream;
import static org.pdfclown.common.build.test.assertion.Assertions.assertParameterizedOf;
import static org.pdfclown.common.util.Aggregations.entry;
import static org.pdfclown.common.util.Strings.EMPTY;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.pdfclown.common.build.test.assertion.Assertions.Expected;
import org.pdfclown.common.build.test.assertion.Assertions.ExpectedGeneration;
import org.pdfclown.common.util.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
class ObjectsTest extends BaseTest {
  /**
   * @implNote Generated as {@code expected} source code within {@link #objToLiteralString()}.
   */
  private static final List<String> LITERAL_STRINGS = asList(
      // [1] obj[0]: null
      "null",
      // [2] obj[1]: 1234
      "1234",
      // [3] obj[2]: 1.987
      "1.987",
      // [4] obj[3]: 1.5E-4
      "1.5E-4",
      // [5] obj[4]: true
      "true",
      // [6] obj[5]: '''
      "'\\''",
      // [7] obj[6]: '"'
      "'\"'",
      // [8] obj[7]: ""
      "\"\"",
      // [9] obj[8]: "Text with:\n- . . ."
      "\"Text with:\\n- \\\"quoted content\\\"\\n- newlines\"",
      // [10] obj[9]: "测试文本"
      "\"测试文本\"");

  static Stream<Arguments> objFromLiteralString() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // [1] s[0]: "null"
            null,
            // [2] s[1]: "1234"
            1234,
            // [3] s[2]: "1.987"
            1.987F,
            // [4] s[3]: "1.5E-4"
            1.5E-4F,
            // [5] s[4]: "true"
            true,
            // [6] s[5]: "'\\''"
            '\'',
            // [7] s[6]: "'\"'"
            '"',
            // [8] s[7]: "\"\""
            "",
            // [9] s[8]: "\"Text with:\\. . ."
            "Text with:\n- \"quoted content\"\n- newlines",
            // [10] s[9]: "\"测试文本\""
            "测试文本"),
        // s
        LITERAL_STRINGS);
  }

  static Stream<Arguments> objToLiteralString() {
    return argumentsStream(
        simple(),
        // expected
        LITERAL_STRINGS,
        // obj
        asList(
            null,
            1_234,
            1.987,
            1.5e-4,
            true,
            '\'',
            '"',
            EMPTY,
            "Text with:\n- \"quoted content\"\n- newlines",
            "测试文本"));
  }

  @Test
  void objDo() {
    var obj = new MutableObject<>();
    Objects.objDo(obj, $ -> $.setValue("RESULT"));

    assertThat(obj.getValue(), is("RESULT"));
  }

  @Test
  @SuppressWarnings("ConstantValue")
  void objElseGet() {
    List<Object> defaultResult = Collections.emptyList();

    List<Object> obj = of("test");
    Supplier<List<Object>> defaultSupplier = () -> defaultResult;
    assertThat(Objects.objElseGet(obj, defaultSupplier), is(obj));

    obj = null;
    assertThat(Objects.objElseGet(obj, defaultSupplier), is(defaultResult));
  }

  @ParameterizedTest
  @MethodSource
  void objFromLiteralString(Expected<@Nullable Object> expected, @Nullable String s) {
    assertParameterizedOf(
        () -> Objects.objFromLiteralString(s),
        expected,
        () -> new ExpectedGeneration(of(
            entry("s", s))));
  }

  @Test
  @SuppressWarnings("ConstantValue")
  void objToElse() {
    int defaultResult = 0;

    List<Object> obj = of("test");
    assertThat(Objects.objToElse(obj, List::size, defaultResult), is(1));

    obj = null;
    assertThat(Objects.objTo(obj, List::size), is(nullValue()));
    assertThat(Objects.objToElse(obj, List::size, defaultResult), is(defaultResult));
  }

  @Test
  @SuppressWarnings("ConstantValue")
  void objToElseGet() {
    int defaultResult = 0;

    List<Object> obj = of("test");
    Supplier<Integer> defaultSupplier = () -> defaultResult;
    assertThat(Objects.objToElseGet(obj, List::size, defaultSupplier), is(1));

    obj = null;
    assertThat(Objects.objToElseGet(obj, List::size, defaultSupplier), is(defaultResult));
  }

  @ParameterizedTest
  @MethodSource
  void objToLiteralString(Expected<Object> expected, @Nullable Object obj) {
    assertParameterizedOf(
        () -> Objects.objToLiteralString(obj),
        expected,
        () -> new ExpectedGeneration(of(
            entry("obj", obj))));
  }

  /**
   * NOTE: {@link Objects#quiet(FailableConsumer, Object)} can NEVER fail per-se; if the given
   * object fails on called operation, that's part of its regular execution.
   */
  @Test
  void quiet_FailableConsumer() {
    var ref = new MutableObject<>();
    var obj = new AutoCloseable() {
      @Override
      public void close() throws Exception {
        ref.setValue("DONE");
        throw new IllegalStateException("FAILED");
      }
    };
    var ret = Objects.quiet(AutoCloseable::close, obj);

    assertThat(ret, sameInstance(obj));
    assertThat(ref.getValue(), is("DONE"));
  }

  /**
   * NOTE: {@link Objects#quiet(FailableConsumer, Object, Consumer)} can NEVER fail per-se; if the
   * given object fails on called operation, that's part of its regular execution.
   */
  @Test
  void quiet_FailableConsumer_Consumer() {
    var obj = new AutoCloseable() {
      @Override
      public void close() throws Exception {
        throw new IllegalStateException("FAILED");
      }
    };
    var exceptionRef = new MutableObject<Throwable>();
    var ret = Objects.quiet(AutoCloseable::close, obj, exceptionRef::setValue);

    assertThat(ret, sameInstance(obj));
    assertThat(exceptionRef.getValue(), instanceOf(IllegalStateException.class));
    assertThat(exceptionRef.getValue().getMessage(), is("FAILED"));
  }

  /**
   * NOTE: {@link Objects#quiet(org.apache.commons.lang3.function.FailableRunnable)} can NEVER fail
   * per-se; if the given operation fails on call, that's part of its regular execution.
   */
  @Test
  void quiet_FailableRunnable() {
    var ref = new MutableObject<>();
    Objects.quiet(() -> {
      ref.setValue("DONE");
      throw new IllegalStateException("FAILED");
    });

    assertThat(ref.getValue(), is("DONE"));
  }

  /**
   * NOTE: {@link Objects#quiet(org.apache.commons.lang3.function.FailableRunnable)} can NEVER fail
   * per-se; if the given operation fails on call, that's part of its regular execution.
   */
  @Test
  void quiet_FailableRunnable_Consumer() {
    var exceptionRef = new MutableObject<Throwable>();
    Objects.quiet(() -> {
      throw new IllegalStateException("FAILED");
    }, exceptionRef::setValue);

    assertThat(exceptionRef.getValue(), instanceOf(IllegalStateException.class));
    assertThat(exceptionRef.getValue().getMessage(), is("FAILED"));
  }

  @Test
  void tryGetElse__fail() {
    var ret = Objects.tryGetElse(() -> {
      throw new IllegalStateException("FAILED");
    }, "ALT");

    assertThat(ret, is("ALT"));
  }

  @Test
  void tryGetElse__ok() {
    var ret = Objects.tryGetElse(() -> "RESULT", "ALT");

    assertThat(ret, is("RESULT"));
  }

  @Test
  void tryGet__fail() {
    var ret = Objects.tryGet(() -> {
      throw new IllegalStateException("FAILED");
    });

    assertThat(ret, is(nullValue()));
  }

  @Test
  void tryGet__ok() {
    var ret = Objects.tryGet(() -> "RESULT");

    assertThat(ret, is("RESULT"));
  }
}
