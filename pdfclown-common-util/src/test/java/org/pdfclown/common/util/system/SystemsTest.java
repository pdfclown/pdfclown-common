/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (SystemsTest.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.system;

import static java.util.Arrays.asList;
import static org.pdfclown.common.build.test.assertion.Assertions.ArgumentsStreamStrategy.cartesian;
import static org.pdfclown.common.build.test.assertion.Assertions.argumentsStream;
import static org.pdfclown.common.build.test.assertion.Assertions.assertParameterizedOf;

import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.pdfclown.common.build.test.assertion.Assertions.Expected;
import org.pdfclown.common.build.test.assertion.Assertions.ExpectedGeneration;

/**
 * @author Stefano Chizzolini
 */
class SystemsTest {
  static Stream<Arguments> getBooleanProperty() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // [1] value[0]: null
            false,
            // [2] value[1]: "false"
            false,
            // [3] value[2]: "something else"
            false,
            // [4] value[3]: ""
            true,
            // [5] value[4]: "true"
            true,
            // [6] value[5]: "True"
            true),
        // value
        asList(
            null,
            "false",
            "something else",
            "",
            "true",
            "True"));
  }

  @ParameterizedTest
  @MethodSource
  void getBooleanProperty(Expected<Boolean> expected, @Nullable String value) {
    final var key = "myProperty";

    assertParameterizedOf(
        () -> {
          if (value != null) {
            System.setProperty(key, value);
          } else {
            System.clearProperty(key);
          }
          return Systems.getBooleanProperty(key);
        },
        expected,
        () -> new ExpectedGeneration<>(value));
  }
}