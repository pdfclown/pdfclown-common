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
import static org.pdfclown.common.build.test.assertion.Verifiers.COMBINATION;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
class StringsTest extends BaseTest {
  @Test
  void abbreviateMultiline() {
    var value = "1:  A multi-line text to test whether this method behave "
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
        + "10: - Eighth: this element line will be a bit shorter";

    COMBINATION.verify(
        (maxLineCount, maxLength, marker) -> Strings.abbreviateMultiline(value, maxLineCount,
            maxLength, marker),
        List.of("maxLineCount", "maxLength", "marker"),
        // maxLineCount
        asList(
            10,
            6,
            3,
            0),
        // maxLength
        asList(
            800,
            240,
            60,
            0),
        // marker
        asList(
            "...",
            "[...]"));
  }

  @Test
  void stripEmptyLines() {
    COMBINATION.verify(
        (s) -> Strings.stripEmptyLines(s),
        List.of("s"),
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

  @Test
  void uncapitalizeGreedy() {
    COMBINATION.verify(
        (value) -> Strings.uncapitalizeGreedy(value),
        List.of("value"),
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

  /**
   * Tests that unchanged strings are returned without creating new instances of the same string.
   */
  @Test
  void uncapitalizeGreedy__sameInstance() {
    final var value = "notApplicable";

    assertThat(Strings.uncapitalizeGreedy(value), is(sameInstance(value)));
  }
}
