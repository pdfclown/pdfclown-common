/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (StringsTest.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.pdfclown.common.build.test.assertion.Assertions.ArgumentsStreamStrategy.cartesian;
import static org.pdfclown.common.build.test.assertion.Assertions.argumentsStream;
import static org.pdfclown.common.build.test.assertion.Assertions.assertParameterizedOf;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.pdfclown.common.build.test.assertion.Assertions.Expected;
import org.pdfclown.common.build.test.assertion.Assertions.ExpectedGeneration;
import org.pdfclown.common.build.test.assertion.Assertions.Failure;
import org.pdfclown.common.util.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
@SuppressWarnings({ "ArraysAsListWithZeroOrOneArgument", "ConcatenationWithEmptyString" })
class StringsTest extends BaseTest {
  static Stream<Arguments> abbreviateMultiline() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // value[0]: "1:  A multi-line text to test whether String. . ."
            // -- maxLineCount[0]: 10
            // ---- averageLineLength[0]: 80
            // [1] -- marker[0]: "..."
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particularly long in order to stick out beyond average line length\n"
                + "4:  - Second: this element line will be a bit shorter\n"
                + "5:  - Third: this element line will be particularly long in order to stick out beyond average line length\n"
                + "6:  - Forth: this element line will be a bit shorter\n"
                + "7:  - Fifth: this element line will be particularly long in order to stick out beyond average line length\n"
                + "8:  - Sixth: this element line will be particularly long in order to stick out beyond average line length\n"
                + "9:  - Seventh: this element line will be a bit shorter\n"
                + "10: - Eighth: this element line will be a bit shorter",
            // [2] -- marker[1]: "[...]"
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particularly long in order to stick out beyond average line length\n"
                + "4:  - Second: this element line will be a bit shorter\n"
                + "5:  - Third: this element line will be particularly long in order to stick out beyond average line length\n"
                + "6:  - Forth: this element line will be a bit shorter\n"
                + "7:  - Fifth: this element line will be particularly long in order to stick out beyond average line length\n"
                + "8:  - Sixth: this element line will be particularly long in order to stick out beyond average line length\n"
                + "9:  - Seventh: this element line will be a bit shorter\n"
                + "10: - Eighth: this element line will be a bit shorter",
            // ---- averageLineLength[1]: 40
            // [3] -- marker[0]: "..."
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particularly long in order to stick out beyond average line length\n"
                + "4:  - Second: this element line will be a bit shorter\n"
                + "5:  - Third: this element line will be particularly long in order to stick out beyond av...",
            // [4] -- marker[1]: "[...]"
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particularly long in order to stick out beyond average line length\n"
                + "4:  - Second: this element line will be a bit shorter\n"
                + "5:  - Third: this element line will be particularly long in order to stick out beyond av[...]",
            // ---- averageLineLength[2]: 20
            // [5] -- marker[0]: "..."
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particula...",
            // [6] -- marker[1]: "[...]"
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particula[...]",
            // ---- averageLineLength[3]: 0
            // [7] -- marker[0]: "..."
            "...",
            // [8] -- marker[1]: "[...]"
            "[...]",
            // -- maxLineCount[1]: 6
            // ---- averageLineLength[0]: 80
            // [9] -- marker[0]: "..."
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particularly long in order to stick out beyond average line length\n"
                + "4:  - Second: this element line will be a bit shorter\n"
                + "5:  - Third: this element line will be particularly long in order to stick out beyond average line length\n"
                + "6:  - Forth: this element line will be a bit shorter...",
            // [10] - marker[1]: "[...]"
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particularly long in order to stick out beyond average line length\n"
                + "4:  - Second: this element line will be a bit shorter\n"
                + "5:  - Third: this element line will be particularly long in order to stick out beyond average line length\n"
                + "6:  - Forth: this element line will be a bit shorter[...]",
            // ---- averageLineLength[1]: 40
            // [11] - marker[0]: "..."
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particularly long in order to stick out beyond av...",
            // [12] - marker[1]: "[...]"
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particularly long in order to stick out beyond av[...]",
            // ---- averageLineLength[2]: 20
            // [13] - marker[0]: "..."
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list e...",
            // [14] - marker[1]: "[...]"
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list e[...]",
            // ---- averageLineLength[3]: 0
            // [15] - marker[0]: "..."
            "...",
            // [16] - marker[1]: "[...]"
            "[...]",
            // -- maxLineCount[2]: 3
            // ---- averageLineLength[0]: 80
            // [17] - marker[0]: "..."
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particularly long in order to stick out beyond av...",
            // [18] - marker[1]: "[...]"
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particularly long in order to stick out beyond av[...]",
            // ---- averageLineLength[1]: 40
            // [19] - marker[0]: "..."
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list e...",
            // [20] - marker[1]: "[...]"
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list e[...]",
            // ---- averageLineLength[2]: 20
            // [21] - marker[0]: "..."
            "1:  A multi-line text to test whether Strings.abbreviateMult...",
            // [22] - marker[1]: "[...]"
            "1:  A multi-line text to test whether Strings.abbreviateMult[...]",
            // ---- averageLineLength[3]: 0
            // [23] - marker[0]: "..."
            "...",
            // [24] - marker[1]: "[...]"
            "[...]",
            // -- maxLineCount[3]: 0
            // ---- averageLineLength[0]: 80
            // [25] - marker[0]: "..."
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particularly long in order to stick out beyond average line length\n"
                + "4:  - Second: this element line will be a bit shorter\n"
                + "5:  - Third: this element line will be particularly long in order to stick out beyond average line length\n"
                + "6:  - Forth: this element line will be a bit shorter\n"
                + "7:  - Fifth: this element line will be particularly long in order to stick out beyond average line length\n"
                + "8:  - Sixth: this element line will be particularly long in order to stick out beyond average line length\n"
                + "9:  - Seventh: this element line will be a bit shorter\n"
                + "10: - Eighth: this element line will be a bit shorter",
            // [26] - marker[1]: "[...]"
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particularly long in order to stick out beyond average line length\n"
                + "4:  - Second: this element line will be a bit shorter\n"
                + "5:  - Third: this element line will be particularly long in order to stick out beyond average line length\n"
                + "6:  - Forth: this element line will be a bit shorter\n"
                + "7:  - Fifth: this element line will be particularly long in order to stick out beyond average line length\n"
                + "8:  - Sixth: this element line will be particularly long in order to stick out beyond average line length\n"
                + "9:  - Seventh: this element line will be a bit shorter\n"
                + "10: - Eighth: this element line will be a bit shorter",
            // ---- averageLineLength[1]: 40
            // [27] - marker[0]: "..."
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particularly long in order to stick out beyond average line length\n"
                + "4:  - Second: this element line will be a bit shorter\n"
                + "5:  - Third: this element line will be particularly long in order to stick out beyond av...",
            // [28] - marker[1]: "[...]"
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particularly long in order to stick out beyond average line length\n"
                + "4:  - Second: this element line will be a bit shorter\n"
                + "5:  - Third: this element line will be particularly long in order to stick out beyond av[...]",
            // ---- averageLineLength[2]: 20
            // [29] - marker[0]: "..."
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particula...",
            // [30] - marker[1]: "[...]"
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particula[...]",
            // ---- averageLineLength[3]: 0
            // [31] - marker[0]: "..."
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particularly long in order to stick out beyond average line length\n"
                + "4:  - Second: this element line will be a bit shorter\n"
                + "5:  - Third: this element line will be particularly long in order to stick out beyond average line length\n"
                + "6:  - Forth: this element line will be a bit shorter\n"
                + "7:  - Fifth: this element line will be particularly long in order to stick out beyond average line length\n"
                + "8:  - Sixth: this element line will be particularly long in order to stick out beyond average line length\n"
                + "9:  - Seventh: this element line will be a bit shorter\n"
                + "10: - Eighth: this element line will be a bit shorter",
            // [32] - marker[1]: "[...]"
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particularly long in order to stick out beyond average line length\n"
                + "4:  - Second: this element line will be a bit shorter\n"
                + "5:  - Third: this element line will be particularly long in order to stick out beyond average line length\n"
                + "6:  - Forth: this element line will be a bit shorter\n"
                + "7:  - Fifth: this element line will be particularly long in order to stick out beyond average line length\n"
                + "8:  - Sixth: this element line will be particularly long in order to stick out beyond average line length\n"
                + "9:  - Seventh: this element line will be a bit shorter\n"
                + "10: - Eighth: this element line will be a bit shorter"),
        // value
        asList(
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave "
                + "correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particularly long in order to stick out "
                + "beyond average line length\n"
                + "4:  - Second: this element line will be a bit shorter\n"
                + "5:  - Third: this element line will be particularly long in order to stick out "
                + "beyond average line length\n"
                + "6:  - Forth: this element line will be a bit shorter\n"
                + "7:  - Fifth: this element line will be particularly long in order to stick out "
                + "beyond average line length\n"
                + "8:  - Sixth: this element line will be particularly long in order to stick out "
                + "beyond average line length\n"
                + "9:  - Seventh: this element line will be a bit shorter\n"
                + "10: - Eighth: this element line will be a bit shorter"),
        // maxLineCount
        asList(
            10,
            6,
            3,
            0),
        // averageLineLength
        asList(
            80,
            40,
            20,
            0),
        // marker
        asList(
            "...",
            "[...]"));
  }

  static Stream<Arguments> stripEmptyLines() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // [1] s[0]: null
            new Failure("NullPointerException", null),
            // [2] s[1]: ""
            "",
            // [3] s[2]: "\n"
            "",
            // [4] s[3]: "\n\n           \nFirst non-empty line\n     . . ."
            "           \n"
                + "First non-empty line\n"
                + "     Second line   \n"
                + "\n"
                + "Third line"),
        // s
        asList(
            null,
            "",
            "\n",
            "\n\n"
                + "           \n"
                + "First non-empty line\n"
                + "     Second line   \n"
                + "\n"
                + "Third line\n\n"));
  }

  static Stream<Arguments> uncapitalizeGreedy() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // [1] value[0]: "Capitalized"
            "capitalized",
            // [2] value[1]: "uncapitalized"
            "uncapitalized",
            // [3] value[2]: "EOF"
            "eof",
            // [4] value[3]: "XObject"
            "xObject",
            // [5] value[4]: "IOException"
            "ioException",
            // [6] value[5]: "UTF8Test"
            "utf8Test",
            // [7] value[6]: "UTF8TEST"
            "utf8TEST",
            // [8] value[7]: "UNDERSCORE_TEST"
            "underscore_TEST"),
        // value
        asList(
            "Capitalized",
            "uncapitalized",
            "EOF",
            "XObject",
            "IOException",
            "UTF8Test",
            "UTF8TEST",
            "UNDERSCORE_TEST"));
  }

  @ParameterizedTest
  @MethodSource
  void abbreviateMultiline(Expected<String> expected, String value, int maxLineCount,
      int averageLineLength, String marker) {
    assertParameterizedOf(
        () -> Strings.abbreviateMultiline(value, maxLineCount, averageLineLength, marker),
        expected,
        () -> new ExpectedGeneration(value, maxLineCount, averageLineLength, marker));

    // Check default ellipsis ("...") overload!
    if (marker.equals("...")) {
      assertParameterizedOf(
          () -> Strings.abbreviateMultiline(value, maxLineCount, averageLineLength),
          expected,
          null);
    }
  }

  @ParameterizedTest
  @MethodSource
  void stripEmptyLines(Expected<String> expected, String s) {
    assertParameterizedOf(
        () -> Strings.stripEmptyLines(s),
        expected,
        () -> new ExpectedGeneration(s));
  }

  @ParameterizedTest
  @MethodSource
  void uncapitalizeGreedy(Expected<String> expected, String value) {
    assertParameterizedOf(
        () -> Strings.uncapitalizeGreedy(value),
        expected,
        () -> new ExpectedGeneration(value));
  }

  /**
   * Tests that unchanged strings are returned without creating new instances of the same string.
   */
  @Test
  void uncapitalizeGreedy__sameInstance() {
    final var value = "notApplicable";

    assertThat(Strings.uncapitalizeGreedy(value), is(sameInstance(value)));
  }
}
