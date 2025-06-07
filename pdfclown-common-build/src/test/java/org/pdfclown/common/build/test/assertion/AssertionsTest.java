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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.pdfclown.common.build.internal.util.Aggregations.entry;

import java.awt.Shape;
import java.awt.geom.Path2D;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.hamcrest.MatcherAssert;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.AssertionFailedError;
import org.pdfclown.common.build.__test.BaseTest;
import org.pdfclown.common.build.internal.util.io.XtPrintStream;

/**
 * @author Stefano Chizzolini
 */
class AssertionsTest extends BaseTest {
  private static final double DBL_DELTA = 1e-6;

  static Stream<Arguments> assertParameterized_cartesian() {
    return Assertions.cartesianArgumentsStream(
        // expected
        java.util.Arrays.asList(
            // value[0]: 'null'
            // -- length[0]: '50'
            new org.pdfclown.common.build.test.assertion.Assertions.ThrownExpected(
                "java.lang.NullPointerException", "`value`"),
            // -- length[1]: '20'
            new org.pdfclown.common.build.test.assertion.Assertions.ThrownExpected(
                "java.lang.NullPointerException", "`value`"),
            // -- length[2]: '5'
            new org.pdfclown.common.build.test.assertion.Assertions.ThrownExpected(
                "java.lang.NullPointerException", "`value`"),
            //
            // value[1]: ''
            // -- length[0]: '50'
            new org.pdfclown.common.build.test.assertion.Assertions.ThrownExpected(
                "java.lang.IllegalArgumentException",
                "`length` (50): INVALID (should be less than 0)"),
            // -- length[1]: '20'
            new org.pdfclown.common.build.test.assertion.Assertions.ThrownExpected(
                "java.lang.IllegalArgumentException",
                "`length` (20): INVALID (should be less than 0)"),
            // -- length[2]: '5'
            new org.pdfclown.common.build.test.assertion.Assertions.ThrownExpected(
                "java.lang.IllegalArgumentException",
                "`length` (5): INVALID (should be less than 0)"),
            //
            // value[2]: 'The quick brown. . .'
            // -- length[0]: '50'
            new org.pdfclown.common.build.test.assertion.Assertions.ThrownExpected(
                "java.lang.IllegalArgumentException",
                "`length` (50): INVALID (should be less than 43)"),
            // -- length[1]: '20'
            "The quick brown fox ",
            // -- length[2]: '5'
            "The q",
            //
            // value[3]: 'The lazy yellow. . .'
            // -- length[0]: '50'
            "The lazy yellow dog was caught by the slow red fox",
            // -- length[1]: '20'
            "The lazy yellow dog ",
            // -- length[2]: '5'
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

  static Stream<Arguments> assertParameterized_cartesian_generation() {
    return Assertions.cartesianArgumentsStream(
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

  static Stream<Arguments> assertParameterized_simple() {
    return Assertions.argumentsStream(
        // expected
        java.util.Arrays.asList(
            // value[0]: 'The quick brown. . .'; length[0]: '50'
            new org.pdfclown.common.build.test.assertion.Assertions.ThrownExpected(
                "java.lang.IllegalArgumentException",
                "`length` (50): INVALID (should be less than 43)"),
            // value[1]: 'The lazy yellow. . .'; length[1]: '20'
            "The lazy yellow dog "),
        // value
        asList(
            "The quick brown fox jumps over the lazy dog",
            "The lazy yellow dog was caught by the slow red fox as he lay sleeping in the sun"),
        // length
        asList(
            50,
            20));
  }

  static Stream<Arguments> assertParameterized_simple_generation() {
    return Assertions.argumentsStream(
        // expected
        null /* GENERATION MODE */,
        // value
        asList(
            "The quick brown fox jumps over the lazy dog",
            "The lazy yellow dog was caught by the slow red fox as he lay sleeping in the sun"),
        // length
        asList(
            50,
            20));
  }

  static Stream<Arguments> assertParameterized_simple_generation_invalidCardinality() {
    var exception = assertThrows(IllegalArgumentException.class, () -> Assertions.argumentsStream(
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
            5)));
    assertEquals("args[1].size (3): INVALID (should be 2)", exception.getMessage());

    return Stream.of(Arguments.of(null, "", 0));
  }

  private static Shape polygon(double[] coords) {
    var ret = new Path2D.Double();
    ret.moveTo(coords[0], coords[1]);
    for (int i = 1, l = coords.length - 1; i < l;) {
      ret.lineTo(coords[++i], coords[++i]);
    }
    ret.closePath();
    return ret;
  }

  private @Nullable XtPrintStream out;

  /**
   * Tests the regular execution of {@link Assertions#assertParameterized(Object, Object, Supplier)
   * assertParameterized(..)} along with {@link Assertions#cartesianArgumentsStream(List, List[])
   * cartesianArgumentsStream(..)}.
   */
  @ParameterizedTest
  @MethodSource
  void assertParameterized_cartesian(Object expected, String value, int length) {
    doAssertParameterized(expected, value, length);
  }

  /**
   * Tests the expected results generation of
   * {@link Assertions#assertParameterized(Object, Object, Supplier) assertParameterized(..)} along
   * with {@link Assertions#cartesianArgumentsStream(List, List[]) cartesianArgumentsStream(..)}.
   */
  @ParameterizedTest
  @MethodSource
  void assertParameterized_cartesian_generation(Object expected, String value, int length) {
    doAssertParameterized_generation(expected, value, length,
        "// expected\n"
            + "java.util.Arrays.asList(\n"
            + "  // value[0]: 'The quick brown. . .'\n"
            + "  // -- length[0]: '50'\n"
            + "  new org.pdfclown.common.build.test.assertion.Assertions.ThrownExpected(" +
            "\"java.lang.IllegalArgumentException\", " +
            "\"`length` (50): INVALID (should be less than 43)\"),\n"
            + "  // -- length[1]: '20'\n"
            + "  \"The quick brown fox \",\n"
            + "  // -- length[2]: '5'\n"
            + "  \"The q\",\n"
            + "  //\n"
            + "  // value[1]: 'The lazy yellow. . .'\n"
            + "  // -- length[0]: '50'\n"
            + "  \"The lazy yellow dog was caught by the slow red fox\",\n"
            + "  // -- length[1]: '20'\n"
            + "  \"The lazy yellow dog \",\n"
            + "  // -- length[2]: '5'\n"
            + "  \"The l\"),\n");
  }

  /**
   * Tests the regular execution of {@link Assertions#assertParameterized(Object, Object, Supplier)
   * assertParameterized(..)} along with {@link Assertions#argumentsStream(List, List[])
   * argumentsStream(..)}.
   */
  @ParameterizedTest
  @MethodSource
  void assertParameterized_simple(Object expected, String value, int length) {
    doAssertParameterized(expected, value, length);
  }

  /**
   * Tests the expected results generation of
   * {@link Assertions#assertParameterized(Object, Object, Supplier) assertParameterized(..)} along
   * with {@link Assertions#argumentsStream(List, List[]) argumentsStream(..)}.
   */
  @ParameterizedTest
  @MethodSource
  void assertParameterized_simple_generation(Object expected, String value, int length) {
    doAssertParameterized_generation(expected, value, length, "// expected\n"
        + "java.util.Arrays.asList(\n"
        + "  // value[0]: 'The quick brown. . .'; length[0]: '50'\n"
        + "  new org.pdfclown.common.build.test.assertion.Assertions.ThrownExpected(" +
        "\"java.lang.IllegalArgumentException\", " +
        "\"`length` (50): INVALID (should be less than 43)\"),\n"
        + "  // value[1]: 'The lazy yellow. . .'; length[1]: '20'\n"
        + "  \"The lazy yellow dog \"),\n");
  }

  /**
   * Tests {@link Assertions#argumentsStream(List, List[])} failure on expected results generation
   * (arguments cardinality should be the same, but it's not the case...).
   *
   * @see #assertParameterized_simple_generation_invalidCardinality()
   */
  @ParameterizedTest
  @MethodSource
  void assertParameterized_simple_generation_invalidCardinality(Object expected,
      String value, int length) {
  }

  @Test
  void assertShapeEquals() {
    assertDoesNotThrow(
        () -> Assertions.assertShapeEquals(
            polygon(new double[] { .1, .1, .8, .1, .9, .9, .2, .19 }),
            polygon(new double[] { .1, .1, .8, .1, .9, .9, .2, .19 }), DBL_DELTA));

    {
      var thrown = assertThrows(AssertionFailedError.class,
          () -> Assertions.assertShapeEquals(
              polygon(new double[] { .1, .1, .8, .1, .9, .9, .2, .19 }),
              polygon(new double[] { .1, .1, .8, .1, .9, 52, .2, .19 }), DBL_DELTA));
      assertEquals("Segment 2, point 0, y ==> expected: <0.9> but was: <52.0>",
          thrown.getCause().getMessage());
    }

    {
      var thrown = assertThrows(AssertionFailedError.class,
          () -> Assertions.assertShapeEquals(
              polygon(new double[] { .1, .1, .8, .1, .9, .9, .2, .19 }),
              polygon(new double[] { .1, .1, .8, .1, .9, .9 }), DBL_DELTA));
      assertEquals("Segment 3, segmentKind ==> expected: <1> but was: <4>",
          thrown.getCause().getMessage());
    }

    {
      var thrown = assertThrows(AssertionFailedError.class,
          () -> Assertions.assertShapeEquals(
              polygon(new double[] { .1, .1, .8, .1, .9, .9 }),
              polygon(new double[] { .1, .1, .8, .1, .9, .9, .2, .19 }), DBL_DELTA));
      assertEquals("Segment 3, segmentKind ==> expected: <4> but was: <1>",
          thrown.getCause().getMessage());
    }
  }

  /**
   * Test method for {@link Assertions#assertParameterized(Object, Object, Supplier)}-related tests.
   *
   * @throws IllegalArgumentException
   *           if {@code length} is less than 20.
   */
  private String assertParameterized_testMethod(String value, int length) {
    if (length > requireNonNull(value, "`value`").length())
      throw new IllegalArgumentException(String.format(
          "`length` (%s): INVALID (should be less than %s)", length, value.length()));

    return value.substring(0, length);
  }

  private void beginOut() {
    out = new XtPrintStream();
  }

  private void doAssertParameterized(Object expected, String value, int length) {
    Assertions.assertParameterizedOf(
        () -> assertParameterized_testMethod(value, length),
        expected,
        () -> new Assertions.ExpectedGeneration(
            List.of(
                entry("value", value),
                entry("length", length))));
  }

  private void doAssertParameterized_generation(Object expected, String value, int length,
      String expectedOutput) {
    if (out == null) {
      beginOut();
    }

    try {
      Assertions.assertParameterizedOf(
          () -> assertParameterized_testMethod(value, length),
          expected,
          () -> new Assertions.ExpectedGeneration(
              List.of(
                  entry("value", value),
                  entry("length", length)))
                      .setOut(out)
                      .setOutOverridable(false));
    } finally {
      if (!Assertions.isExpectedGenerationMode()) {
        String actualOutput = endOut();
        MatcherAssert.assertThat(actualOutput, is(expectedOutput));
      }
    }
  }

  private String endOut() {
    var ret = requireNonNull(requireNonNull(out).toDataString());
    out = null;
    return ret;
  }
}
