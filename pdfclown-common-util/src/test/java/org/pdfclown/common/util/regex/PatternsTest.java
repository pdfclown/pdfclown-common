/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (PatternsTest.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.regex;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.pdfclown.common.build.test.assertion.Assertions.ArgumentsStreamConfig.cartesian;
import static org.pdfclown.common.build.test.assertion.Assertions.argumentsStream;
import static org.pdfclown.common.build.test.assertion.Assertions.assertParameterized;
import static org.pdfclown.common.build.test.assertion.Assertions.assertParameterizedOf;
import static org.pdfclown.common.build.test.assertion.Assertions.evalParameterized;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.pdfclown.common.build.test.assertion.Assertions.Argument;
import org.pdfclown.common.build.test.assertion.Assertions.Expected;
import org.pdfclown.common.build.test.assertion.Assertions.ExpectedGeneration;
import org.pdfclown.common.util.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
class PatternsTest extends BaseTest {
  static class PatternArgument extends Argument<String> {
    /**
     * Samples matching {@linkplain #getValue() regex}.
     */
    List<String> matches;
    /**
     * Samples not matching {@linkplain #getValue() regex}.
     */
    List<String> mismatches;

    PatternArgument(String payload, List<String> matches, List<String> mismatches) {
      super("pattern", payload);

      this.matches = matches;
      this.mismatches = mismatches;
    }
  }

  /**
   * <a href=
   * "https://semver.org/#is-there-a-suggested-regular-expression-regex-to-check-a-semver-string">Official
   * Semantic Versioning 2.0.0 regular expression</a>.
   */
  private static final Pattern PATTERN__SEM_VER = Pattern.compile("^"
      + "(?<major>0|[1-9]\\d*)\\."
      + "(?<minor>0|[1-9]\\d*)\\."
      + "(?<patch>0|[1-9]\\d*)"
      + "(?:-(?<prerelease>(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)"
      + "(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?"
      + "(?:\\+(?<build>[0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?"
      + "$");

  static Stream<Arguments> globToRegex() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // [1] glob[0]: "\"/**/my*.*\" (regex)"
            "/.*/my[^/]*\\.[^/]*",
            // [2] glob[1]: "\"/home/*User/**/foo?a?/*.md\" (regex)"
            "/home/[^/]*User/.*/foo.a./[^/]*\\.md"),
        // pattern
        asList(
            new PatternArgument("/**/my*.*",
                asList(
                    "/home/usr/Pictures/myRainbow.jpg",
                    "/home/usr/Documents/myFile.html",
                    "/home/usr/Projects/foobar/foobar-core/src/main/resources/html/myProject.html"),
                asList(
                    "myRainbow.jpg",
                    "/home/usr/Documents/file.html")),
            new PatternArgument("/home/*User/**/foo?a?/*.md",
                asList(
                    "/home/User/MyDocs/foobar/readme.md",
                    "/home/BlueUser/MyDocs/foocat/readme.md",
                    "/home/SuperUser/a/random/subdir/foocat/NOTE.md"),
                asList(
                    "/home/User/foobar/readme.md",
                    "/home/BlueUser/MyDocs/fooca/readme.md",
                    "/home/SuperUser/a/random/subdir/foocat/NOTEmd"))));
  }

  static Stream<Arguments> indexOfMatchFailure() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // [1] input[0]: "1.0.0"
            5,
            // [2] input[1]: "1.0.0-alpha"
            11,
            // [3] input[2]: "1.11.0.99"
            6,
            // [4] input[3]: "1.-11.0"
            2,
            // [5] input[4]: "1.01.0"
            3,
            // [6] input[5]: "1.0.0-00.3.7"
            8,
            // [7] input[6]: "1.0.0_alpha"
            5,
            // [8] input[7]: "1.0.0.5-alpha"
            5),
        // input
        List.of(
            // VALID
            "1.0.0",
            "1.0.0-alpha",
            // INVALID
            "1.11.0.99",
            "1.-11.0",
            "1.01.0",
            "1.0.0-00.3.7",
            "1.0.0_alpha",
            "1.0.0.5-alpha"));
  }

  static Stream<Arguments> wildcardToRegex() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // [1] pattern[0]: "\"Som? content. * more (*)\\\\?\" (regex)"
            "Som. content\\. .* more \\(.*\\)\\?"),
        // wildcard
        asList(
            new PatternArgument("Som? content. * more (*)\\?",
                asList("Some content. Whatever more (don't know)?"),
                asList("Som content. Whatever more (don't know)?"))));
  }

  @ParameterizedTest
  @MethodSource
  void globToRegex(Expected<String> expected, PatternArgument glob) {
    var actual = requireNonNull((String) evalParameterized(
        () -> Patterns.globToRegex(glob.getValue())));

    assertParameterized(actual, expected,
        () -> new ExpectedGeneration(glob));
    assertRegexMatches(actual, glob.matches, true);
    assertRegexMatches(actual, glob.mismatches, false);
  }

  @ParameterizedTest
  @MethodSource
  void indexOfMatchFailure(Expected<Integer> expected, String input) {
    assertParameterizedOf(
        () -> {
          Matcher matcher = PATTERN__SEM_VER.matcher(input);
          matcher.find();
          return Patterns.indexOfMatchFailure(matcher);
        },
        expected,
        () -> new ExpectedGeneration(input));
  }

  @ParameterizedTest
  @MethodSource
  void wildcardToRegex(Expected<String> expected, PatternArgument wildcard) {
    var actual = requireNonNull((String) evalParameterized(
        () -> Patterns.wildcardToRegex(wildcard.getValue())));

    assertParameterized(actual, expected,
        () -> new ExpectedGeneration(wildcard));
    assertRegexMatches(actual, wildcard.matches, true);
    assertRegexMatches(actual, wildcard.mismatches, false);
  }

  /**
   * @param inputs
   *          Strings to evaluate against {@code regex}.
   * @param expected
   *          Whether {@code inputs} are expected to match {@code regex}.
   */
  private void assertRegexMatches(String regex, List<String> inputs, boolean expected) {
    Predicate<String> matcher = Pattern.compile(regex).asMatchPredicate();
    for (var input : inputs) {
      assertThat(input, matcher.test(input), is(expected));
    }
  }
}
