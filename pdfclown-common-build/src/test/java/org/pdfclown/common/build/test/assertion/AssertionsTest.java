/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (AssertionsTest.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.Matchers.is;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.hamcrest.MatcherAssert;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.pdfclown.common.build.__test.BaseTest;
import org.pdfclown.common.build.test.assertion.Assertions.ArgumentsStreamStrategy;
import org.pdfclown.common.build.test.assertion.Assertions.Expected;
import org.pdfclown.common.build.test.assertion.Assertions.Failure;

/**
 * @author Stefano Chizzolini
 */
@SuppressWarnings("ConcatenationWithEmptyString")
class AssertionsTest extends BaseTest {
  static Stream<Arguments> assertParameterized__cartesian() {
    return Assertions.argumentsStream(
        ArgumentsStreamStrategy.cartesian(),
        // expected
        asList(
            // value[0]: null
            // [1] length[0]: 50
            new Failure("NullPointerException", "`value`"),
            // [2] length[1]: 20
            new Failure("NullPointerException", "`value`"),
            // [3] length[2]: 5
            new Failure("NullPointerException", "`value`"),
            //
            // value[1]: ""
            // [4] length[0]: 50
            new Failure("IllegalArgumentException",
                "`length` (50): INVALID (should be less than 0)"),
            // [5] length[1]: 20
            new Failure("IllegalArgumentException",
                "`length` (20): INVALID (should be less than 0)"),
            // [6] length[2]: 5
            new Failure("IllegalArgumentException",
                "`length` (5): INVALID (should be less than 0)"),
            //
            // value[2]: "The quick brown fox jumps over the lazy dog"
            // [7] length[0]: 50
            new Failure("IllegalArgumentException",
                "`length` (50): INVALID (should be less than 43)"),
            // [8] length[1]: 20
            "The quick brown fox ",
            // [9] length[2]: 5
            "The q",
            //
            // value[3]: "The lazy yellow dog was caught by the slow r. . ."
            // [10] length[0]: 50
            "The lazy yellow dog was caught by the slow red fox",
            // [11] length[1]: 20
            "The lazy yellow dog ",
            // [12] length[2]: 5
            "The l"),
        // value
        asList(
            null,
            "",
            "The quick brown fox jumps over the lazy dog",
            "The lazy yellow dog was caught by the slow red fox as he lay sleeping in the sun"),
        // length
        asList(
            50,
            20,
            5));
  }

  static Stream<Arguments> assertParameterized__cartesian_generation() {
    return Assertions.argumentsStream(
        ArgumentsStreamStrategy.cartesian(),
        // expected
        null /* GENERATION MODE */,
        // value
        asList(
            "The quick brown fox jumps over the lazy dog",
            "The lazy yellow dog was caught by the slow red fox as he lay sleeping in the sun"),
        // length
        asList(
            50,
            20,
            5));
  }

  static Stream<Arguments> assertParameterized__simple() {
    return Assertions.argumentsStream(
        ArgumentsStreamStrategy.simple(),
        // expected
        asList(
            // [1] value[0]: "The quick brown fox jumps over the lazy dog"; length[0]: 50
            new Failure("IllegalArgumentException",
                "`length` (50): INVALID (should be less than 43)"),
            // [2] value[1]: "The lazy yellow dog was caught by the slow r. . ."; length[1]: 20
            "The lazy yellow dog "),
        // value, length
        List.of("The quick brown fox jumps over the lazy dog", 50),
        List.of(
            "The lazy yellow dog was caught by the slow red fox as he lay sleeping in the sun",
            20));
  }

  static Stream<Arguments> assertParameterized__simple_generation() {
    return Assertions.argumentsStream(
        ArgumentsStreamStrategy.simple(),
        // expected
        null /* GENERATION MODE */,
        // value, length
        List.of("The quick brown fox jumps over the lazy dog", 50),
        List.of(
            "The lazy yellow dog was caught by the slow red fox as he lay sleeping in the sun",
            20));
  }

  private @Nullable PrintStream out;
  private @Nullable ByteArrayOutputStream outBuffer;

  /**
   * Tests the regular execution of
   * {@link Assertions#assertParameterized(Object, Expected, Supplier) assertParameterized(..)}
   * along with {@link Assertions#argumentsStream(ArgumentsStreamStrategy, List, List[])
   * argumentsStream(..)}.
   */
  @ParameterizedTest
  @MethodSource
  void assertParameterized__cartesian(Expected<String> expected, @Nullable String value,
      int length) {
    doAssertParameterized(expected, value, length);
  }

  /**
   * Tests the expected results generation of
   * {@link Assertions#assertParameterized(Object, Expected, Supplier) assertParameterized(..)}
   * along with {@link Assertions#argumentsStream(ArgumentsStreamStrategy, List, List[])
   * argumentsStream(..)}.
   */
  @ParameterizedTest
  @MethodSource
  void assertParameterized__cartesian_generation(Expected<String> expected, String value,
      int length) {
    doAssertParameterized_generation(expected, value, length,
        """
            ,
                    // expected
                    java.util.Arrays.asList(
                        // value[0]: "The quick brown fox jumps over the lazy dog"
                        // [1] length[0]: 50
                        new org.pdfclown.common.build.test.assertion.Assertions.Failure("IllegalArgumentException", "`length` (50): INVALID (should be less than 43)"),
                        // [2] length[1]: 20
                        "The quick brown fox ",
                        // [3] length[2]: 5
                        "The q",
                        //
                        // value[1]: "The lazy yellow dog was caught by the slow r. . ."
                        // [4] length[0]: 50
                        "The lazy yellow dog was caught by the slow red fox",
                        // [5] length[1]: 20
                        "The lazy yellow dog ",
                        // [6] length[2]: 5
                        "The l")""");
  }

  /**
   * Tests the regular execution of
   * {@link Assertions#assertParameterized(Object, Expected, Supplier) assertParameterized(..)}
   * along with {@link Assertions#argumentsStream(ArgumentsStreamStrategy, List, List[])
   * argumentsStream(..)}.
   */
  @ParameterizedTest
  @MethodSource
  void assertParameterized__simple(Expected<String> expected, String value, int length) {
    doAssertParameterized(expected, value, length);
  }

  /**
   * Tests the expected results generation of
   * {@link Assertions#assertParameterized(Object, Expected, Supplier) assertParameterized(..)}
   * along with {@link Assertions#argumentsStream(ArgumentsStreamStrategy, List, List[])
   * argumentsStream(..)}.
   */
  @ParameterizedTest
  @MethodSource
  void assertParameterized__simple_generation(Expected<String> expected, String value, int length) {
    doAssertParameterized_generation(expected, value, length,
        """
            ,
                    // expected
                    java.util.Arrays.asList(
                        // [1] value[0]: "The quick brown fox jumps over the lazy dog"; length[0]: 50
                        new org.pdfclown.common.build.test.assertion.Assertions.Failure("IllegalArgumentException", "`length` (50): INVALID (should be less than 43)"),
                        // [2] value[1]: "The lazy yellow dog was caught by the slow r. . ."; length[1]: 20
                        "The lazy yellow dog ")""");
  }

  /**
   * Test method for {@link Assertions#assertParameterized(Object, Expected, Supplier)}-related
   * tests.
   *
   * @throws IllegalArgumentException
   *           if {@code length} is less than 20.
   */
  private String assertParameterizedTestMethod(String value, int length) {
    if (length > requireNonNull(value, "`value`").length())
      throw new IllegalArgumentException("`length` (%s): INVALID (should be less than %s)"
          .formatted(length, value.length()));

    return value.substring(0, length);
  }

  private void beginOut() {
    out = new PrintStream(outBuffer = new ByteArrayOutputStream());
  }

  private void doAssertParameterized(Expected<String> expected, @Nullable String value,
      int length) {
    //noinspection DataFlowIssue : null deliberated.
    Assertions.assertParameterizedOf(
        () -> assertParameterizedTestMethod(value, length),
        expected,
        () -> new Assertions.ExpectedGeneration(value, length));
  }

  private void doAssertParameterized_generation(Expected<String> expected, String value, int length,
      String expectedOutput) {
    if (out == null) {
      beginOut();
    }

    try {
      Assertions.assertParameterizedOf(
          () -> assertParameterizedTestMethod(value, length),
          expected,
          () -> new Assertions.ExpectedGeneration(value, length)
              .setOut(out));
    } finally {
      if (!Assertions.isExpectedGenerationMode()) {
        String actualOutput = endOut();
        MatcherAssert.assertThat(actualOutput, is(expectedOutput));
      }
    }
  }

  private String endOut() {
    var ret = requireNonNull(requireNonNull(outBuffer).toString());
    out = null;
    return ret;
  }
}
