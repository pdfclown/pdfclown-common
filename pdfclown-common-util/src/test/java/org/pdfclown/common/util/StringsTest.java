/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (StringsTest.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import static java.util.Arrays.asList;
import static java.util.List.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.pdfclown.common.build.test.assertion.Assertions.ArgumentsStreamConfig.cartesian;
import static org.pdfclown.common.build.test.assertion.Assertions.argumentsStream;
import static org.pdfclown.common.build.test.assertion.Assertions.assertParameterizedOf;
import static org.pdfclown.common.util.Aggregations.entry;

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
            // value[0]: '1:  A multi-line text to test whether Strings. . .'
            // -- maxLineCount[0]: '10'
            // ---- averageLineLength[0]: '80'
            // ------ marker[0]: '...'
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
            // ------ marker[1]: '[...]'
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
            // ---- averageLineLength[1]: '40'
            // ------ marker[0]: '...'
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particularly long in order to stick out beyond average line length\n"
                + "4:  - Second: this element line will be a bit shorter\n"
                + "5:  - Third: this element line will be particularly long in order to stick out beyond av...",
            // ------ marker[1]: '[...]'
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particularly long in order to stick out beyond average line length\n"
                + "4:  - Second: this element line will be a bit shorter\n"
                + "5:  - Third: this element line will be particularly long in order to stick out beyond av[...]",
            // ---- averageLineLength[2]: '20'
            // ------ marker[0]: '...'
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particula...",
            // ------ marker[1]: '[...]'
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particula[...]",
            // ---- averageLineLength[3]: '0'
            // ------ marker[0]: '...'
            "...",
            // ------ marker[1]: '[...]'
            "[...]",
            // -- maxLineCount[1]: '6'
            // ---- averageLineLength[0]: '80'
            // ------ marker[0]: '...'
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particularly long in order to stick out beyond average line length\n"
                + "4:  - Second: this element line will be a bit shorter\n"
                + "5:  - Third: this element line will be particularly long in order to stick out beyond average line length\n"
                + "6:  - Forth: this element line will be a bit shorter...",
            // ------ marker[1]: '[...]'
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particularly long in order to stick out beyond average line length\n"
                + "4:  - Second: this element line will be a bit shorter\n"
                + "5:  - Third: this element line will be particularly long in order to stick out beyond average line length\n"
                + "6:  - Forth: this element line will be a bit shorter[...]",
            // ---- averageLineLength[1]: '40'
            // ------ marker[0]: '...'
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particularly long in order to stick out beyond av...",
            // ------ marker[1]: '[...]'
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particularly long in order to stick out beyond av[...]",
            // ---- averageLineLength[2]: '20'
            // ------ marker[0]: '...'
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list e...",
            // ------ marker[1]: '[...]'
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list e[...]",
            // ---- averageLineLength[3]: '0'
            // ------ marker[0]: '...'
            "...",
            // ------ marker[1]: '[...]'
            "[...]",
            // -- maxLineCount[2]: '3'
            // ---- averageLineLength[0]: '80'
            // ------ marker[0]: '...'
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particularly long in order to stick out beyond av...",
            // ------ marker[1]: '[...]'
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particularly long in order to stick out beyond av[...]",
            // ---- averageLineLength[1]: '40'
            // ------ marker[0]: '...'
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list e...",
            // ------ marker[1]: '[...]'
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list e[...]",
            // ---- averageLineLength[2]: '20'
            // ------ marker[0]: '...'
            "1:  A multi-line text to test whether Strings.abbreviateMult...",
            // ------ marker[1]: '[...]'
            "1:  A multi-line text to test whether Strings.abbreviateMult[...]",
            // ---- averageLineLength[3]: '0'
            // ------ marker[0]: '...'
            "...",
            // ------ marker[1]: '[...]'
            "[...]",
            // -- maxLineCount[3]: '0'
            // ---- averageLineLength[0]: '80'
            // ------ marker[0]: '...'
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
            // ------ marker[1]: '[...]'
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
            // ---- averageLineLength[1]: '40'
            // ------ marker[0]: '...'
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particularly long in order to stick out beyond average line length\n"
                + "4:  - Second: this element line will be a bit shorter\n"
                + "5:  - Third: this element line will be particularly long in order to stick out beyond av...",
            // ------ marker[1]: '[...]'
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particularly long in order to stick out beyond average line length\n"
                + "4:  - Second: this element line will be a bit shorter\n"
                + "5:  - Third: this element line will be particularly long in order to stick out beyond av[...]",
            // ---- averageLineLength[2]: '20'
            // ------ marker[0]: '...'
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particula...",
            // ------ marker[1]: '[...]'
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particula[...]",
            // ---- averageLineLength[3]: '0'
            // ------ marker[0]: '...'
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
            // ------ marker[1]: '[...]'
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
            "           \nFirst non-empty line\n     Second line   \n\nThird line"),
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

  static Stream<Arguments> stripIndent() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // [1] s[0]: "       \nSimple, without incidental whitespa. . ."
            "\nSimple, without incidental whitespace\n  among its non-blank lines,\n\n\n  just internal indentation.",
            // [2] s[1]: "       \n    Jagged, with incidental whitesp. . ."
            "\n  Jagged, with incidental whitespace\n        among its non-blank lines,\n\n\ntrailing whitespace, and ending newline.\n"),
        // s
        asList(
            ""
                + "       \n"
                + "Simple, without incidental whitespace\n"
                + "  among its non-blank lines,\n"
                + "   \n"
                + "\n"
                + "  just internal indentation.",
            ""
                + "       \n"
                + "    Jagged, with incidental whitespace  \n"
                + "          among its non-blank lines,     \n"
                + "   \n"
                + "\n"
                + "  trailing whitespace, and ending newline.\n"));
  }

  static Stream<Arguments> uncapitalizeGreedy() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // value[0]: 'Capitalized'
            "capitalized",
            // value[1]: 'uncapitalized'
            "uncapitalized",
            // value[2]: 'EOF'
            "eof",
            // value[3]: 'XObject'
            "xObject",
            // value[4]: 'IOException'
            "ioException",
            // value[5]: 'UTF8Test'
            "utf8Test",
            // value[6]: 'UTF8TEST'
            "utf8TEST",
            // value[7]: 'UNDERSCORE_TEST'
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
        () -> new ExpectedGeneration(of(
            entry("value", value),
            entry("maxLineCount", maxLineCount),
            entry("averageLineLength", averageLineLength),
            entry("marker", marker))));

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
        () -> new ExpectedGeneration(of(
            entry("s", s))));
  }

  @ParameterizedTest
  @MethodSource
  void stripIndent(Expected<String> expected, String s) {
    assertParameterizedOf(
        () -> Strings.stripIndent(s),
        expected,
        () -> new ExpectedGeneration(of(
            entry("s", s))));
  }

  @ParameterizedTest
  @MethodSource
  void uncapitalizeGreedy(Expected<String> expected, String value) {
    assertParameterizedOf(
        () -> Strings.uncapitalizeGreedy(value),
        expected,
        () -> new ExpectedGeneration(of(
            entry("value", value))));
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
