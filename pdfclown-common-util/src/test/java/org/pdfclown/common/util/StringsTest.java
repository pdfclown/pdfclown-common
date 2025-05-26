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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.pdfclown.common.build.test.Tests.argumentsStream;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.pdfclown.common.util.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
public class StringsTest extends BaseTest {
  private static Stream<Arguments> _abbreviateMultiline() {
    return argumentsStream(
        // expected
        asList(
            // -- value[0]='1:  A multi-line text to test whether Strings.abbreviateMultiline(..) metho[...]'
            // ---- maxLineCount[0]='10'
            // ------ averageLineLength[0]='80'
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
            // ------ averageLineLength[1]='40'
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particularly long in order to stick out beyond average line length\n"
                + "4:  - Second: this element line will be a bit shorter\n"
                + "5:  - Third: this element line will be particularly long in order to stick out beyond av...",
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particularly long in order to stick out beyond average line length\n"
                + "4:  - Second: this element line will be a bit shorter\n"
                + "5:  - Third: this element line will be particularly long in order to stick out beyond av[...]",
            // ------ averageLineLength[2]='20'
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particula...",
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particula[...]",
            // ------ averageLineLength[3]='0'
            "...",
            "[...]",
            // ---- maxLineCount[1]='6'
            // ------ averageLineLength[0]='80'
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particularly long in order to stick out beyond average line length\n"
                + "4:  - Second: this element line will be a bit shorter\n"
                + "5:  - Third: this element line will be particularly long in order to stick out beyond average line length\n"
                + "6:  - Forth: this element line will be a bit shorter...",
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particularly long in order to stick out beyond average line length\n"
                + "4:  - Second: this element line will be a bit shorter\n"
                + "5:  - Third: this element line will be particularly long in order to stick out beyond average line length\n"
                + "6:  - Forth: this element line will be a bit shorter[...]",
            // ------ averageLineLength[1]='40'
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particularly long in order to stick out beyond av...",
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particularly long in order to stick out beyond av[...]",
            // ------ averageLineLength[2]='20'
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list e...",
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list e[...]",
            // ------ averageLineLength[3]='0'
            "...",
            "[...]",
            // ---- maxLineCount[2]='3'
            // ------ averageLineLength[0]='80'
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particularly long in order to stick out beyond av...",
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particularly long in order to stick out beyond av[...]",
            // ------ averageLineLength[1]='40'
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list e...",
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list e[...]",
            // ------ averageLineLength[2]='20'
            "1:  A multi-line text to test whether Strings.abbreviateMult...",
            "1:  A multi-line text to test whether Strings.abbreviateMult[...]",
            // ------ averageLineLength[3]='0'
            "...",
            "[...]",
            // ---- maxLineCount[3]='0'
            // ------ averageLineLength[0]='80'
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
            // ------ averageLineLength[1]='40'
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particularly long in order to stick out beyond average line length\n"
                + "4:  - Second: this element line will be a bit shorter\n"
                + "5:  - Third: this element line will be particularly long in order to stick out beyond av...",
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particularly long in order to stick out beyond average line length\n"
                + "4:  - Second: this element line will be a bit shorter\n"
                + "5:  - Third: this element line will be particularly long in order to stick out beyond av[...]",
            // ------ averageLineLength[2]='20'
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particula...",
            "1:  A multi-line text to test whether Strings.abbreviateMultiline(..) method behave correctly.\n"
                + "2:  Let's add some list elements to beef up this sample:\n"
                + "3:  - First: this element line will be particula[...]",
            // ------ averageLineLength[3]='0'
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

  private static Stream<Arguments> _uncapitalizeMultichar() {
    return argumentsStream(
        // expected
        asList(
            "capitalized",
            "uncapitalized",
            "eof",
            "xObject",
            "ioException",
            "utf8Test",
            "utf8TEST",
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
  public void _abbreviateMultiline(Object expected, String value, int maxLineCount,
      int averageLineLength, String marker) {
    var actual = evalParameterized(
        () -> Strings.abbreviateMultiline(value, maxLineCount, averageLineLength, marker));

    /*
     * DO NOT remove (useful in case of arguments update)
     */
    //    generateExpected(actual,
    //        asList("value", "maxLineCount", "averageLineLength", "marker"),
    //        asList(value, maxLineCount, averageLineLength, marker));

    assertParameterized(actual, expected);

    // Check default ellipsis ("...") overload!
    if (marker.equals("...")) {
      assertParameterized(evalParameterized(
          () -> Strings.abbreviateMultiline(value, maxLineCount, averageLineLength)), expected);
    }
  }

  @ParameterizedTest
  @MethodSource
  public void _uncapitalizeMultichar(String expected, String value) {
    var actual = evalParameterized(
        () -> Strings.uncapitalizeMultichar(value));

    /*
     * DO NOT remove (useful in case of arguments update)
     */
    //    generateExpected(actual,
    //        asList("value"),
    //        asList(value));

    assertParameterized(actual, expected);
  }

  /**
   * Tests that unchanged strings are returned without creating new instances of the same string.
   */
  @Test
  public void _uncapitalizeMultichar_sameInstance() {
    final var value = "notApplicable";

    assertThat(Strings.uncapitalizeMultichar(value), is(sameInstance(value)));
  }
}
