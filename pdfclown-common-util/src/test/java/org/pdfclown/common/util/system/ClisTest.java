/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ClisTest.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.system;

import static java.util.Arrays.asList;
import static java.util.List.of;
import static java.util.stream.Collectors.joining;
import static org.hamcrest.Matchers.contains;
import static org.pdfclown.common.build.test.assertion.Assertions.ArgumentsStreamConfig.cartesian;
import static org.pdfclown.common.build.test.assertion.Assertions.argumentsStream;
import static org.pdfclown.common.build.test.assertion.Assertions.assertParameterizedOf;
import static org.pdfclown.common.util.Aggregations.entry;
import static org.pdfclown.common.util.Chars.COMMA;
import static org.pdfclown.common.util.Strings.S;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.pdfclown.common.build.test.assertion.Assertions.Expected;
import org.pdfclown.common.build.test.assertion.Assertions.ExpectedGeneration;
import org.pdfclown.common.util.Objects;

/**
 * @author Stefano Chizzolini
 */
class ClisTest {
  static Stream<Arguments> parseArgs() {
    return argumentsStream(
        cartesian(),
        // expected
        java.util.Arrays.asList(
            // [1] argsString[0]: "val1 val2 \"val3 (dquote)\" 'val4 (squote)'"
            asList("val1", "val2", "val3 (dquote)", "val4 (squote)"),
            // [2] argsString[1]: "val1 \"val2 (\\\"dquote\\\")\" 'val3 (\\'squ. . ."
            asList("val1", "val2 (\"dquote\")", "val3 ('squote')"),
            // [3] argsString[2]: "val1 \"val2 (\\\"dquote\\\") 'val3 (\\'squot. . ."
            asList("val1", "val2 (\"dquote\") 'val3 ('squote')'"),
            // [4] argsString[3]: "val1 \"val2 (\\\"dquote\\\")\"'val3 (\\'squo. . ."
            asList("val1", "val2 (\"dquote\")val3 ('squote')"),
            // [5] argsString[4]: "\"Multi-\nLine\nArgument\" \"val2\""
            asList("Multi-\nLine\nArgument", "val2"),
            // [6] argsString[5]: "val1 C:\\\\Some\\\\Random\\\\Path\\\\"
            asList("val1", "C:\\Some\\Random\\Path\\")),
        // argsString
        asList(
            // Quoted arguments embracing whitespace.
            "val1 val2 \"val3 (dquote)\" 'val4 (squote)'",
            // Quoted arguments with inner escaping.
            "val1 \"val2 (\\\"dquote\\\")\" 'val3 (\\'squote\\')'",
            // Quoted argument continuing on missing closing quote.
            "val1 \"val2 (\\\"dquote\\\") 'val3 (\\'squote\\')'",
            // Quoted argument continuing on contiguous quoted chunks without whitespace in between.
            "val1 \"val2 (\\\"dquote\\\")\"'val3 (\\'squote\\')'",
            // Multi-line arguments.
            "\"Multi-\nLine\nArgument\" \"val2\"",
            // Escaped backslashes.
            "val1 C:\\\\Some\\\\Random\\\\Path\\\\"));
  }

  @ParameterizedTest
  @MethodSource
  void parseArgs(Expected<List<String>> expected, String argsString) {
    assertParameterizedOf(
        () -> Clis.parseArgs(argsString),
        expected.match($ -> contains($.toArray(String[]::new))),
        () -> new ExpectedGeneration(of(
            entry("argsString", argsString)))
                .setExpectedSourceCodeGenerator(
                    $ -> String.format("asList(%s)", ((List<String>) $).stream()
                        .map(Objects::literal)
                        .collect(joining(S + COMMA)))));
  }
}