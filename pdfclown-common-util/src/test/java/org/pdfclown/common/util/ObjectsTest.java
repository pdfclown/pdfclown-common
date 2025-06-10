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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Named.named;
import static org.pdfclown.common.build.test.assertion.Assertions.argumentsStream;
import static org.pdfclown.common.build.test.assertion.Assertions.assertParameterizedOf;
import static org.pdfclown.common.build.test.assertion.Assertions.cartesianArgumentsStream;
import static org.pdfclown.common.util.Aggregations.entry;
import static org.pdfclown.common.util.Strings.EMPTY;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.pdfclown.common.build.test.assertion.Assertions.ExpectedGeneration;
import org.pdfclown.common.util.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
class ObjectsTest extends BaseTest {
  static Stream<Arguments> objToLiteralString() {
    return argumentsStream(
        // expected
        java.util.Arrays.asList(
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
            "'''",
            // [7] obj[6]: '"'
            "'\"'",
            // [8] obj[7]: ""
            "\"\"",
            // [9] obj[8]: "Text with:\n- . . ."
            "\"Text with:\\n- \\\"quoted content\\\"\\n- newlines\"",
            // [10] obj[9]: "测试文本"
            "\"测试文本\""),
        // obj
        asList(
            named("Null", null),
            named("Integer", 1_234),
            named("Double", 1.987),
            named("Double with exponential notation", 1.5e-4),
            named("Boolean", true),
            named("Single-quote character", '\''),
            named("Double-quote character", '"'),
            named("Empty string", EMPTY),
            named("String with control characters", "Text with:\n- \"quoted content\"\n- newlines"),
            named("String with Unicode characters", "测试文本")));
  }

  @Test
  @SuppressWarnings("ConstantValue")
  void objOrGet() {
    List<Object> defaultResult = Collections.emptyList();

    List<Object> obj = List.of("test");
    Supplier<List<Object>> defaultSupplier = () -> defaultResult;
    assertThat(Objects.objElseGet(obj, defaultSupplier), is(obj));

    obj = null;
    assertThat(Objects.objElseGet(obj, defaultSupplier), is(defaultResult));
  }

  @ParameterizedTest
  @MethodSource
  void objToLiteralString(Object expected, @Nullable Object obj) {
    assertParameterizedOf(
        () -> Objects.objToLiteralString(obj),
        expected,
        () -> new ExpectedGeneration(List.of(
            entry("obj", obj))));
  }

  @Test
  @SuppressWarnings("ConstantValue")
  void objToOrElse() {
    int defaultResult = 0;

    List<Object> obj = List.of("test");
    assertThat(Objects.objToElse(obj, List::size, defaultResult), is(1));

    obj = null;
    assertThat(Objects.objTo(obj, List::size), is(nullValue()));
    assertThat(Objects.objToElse(obj, List::size, defaultResult), is(defaultResult));
  }

  @Test
  @SuppressWarnings("ConstantValue")
  void objToOrGet() {
    int defaultResult = 0;

    List<Object> obj = List.of("test");
    Supplier<Integer> defaultSupplier = () -> defaultResult;
    assertThat(Objects.objToElseGet(obj, List::size, defaultSupplier), is(1));

    obj = null;
    assertThat(Objects.objToElseGet(obj, List::size, defaultSupplier), is(defaultResult));
  }
}
