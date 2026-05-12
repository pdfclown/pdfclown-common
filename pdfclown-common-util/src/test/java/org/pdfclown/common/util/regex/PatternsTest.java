/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (PatternsTest.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.regex;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.pdfclown.common.build.test.assertion.Verifiers.VERIFIER__COMBINATION;
import static org.pdfclown.common.util.Objects.nonNull;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.build.test.assertion.Assertions.Argument;
import org.pdfclown.common.util.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
class PatternsTest extends BaseTest {
  static class PatternArgument extends Argument<String> {
    private final List<String> matches;
    private final List<String> mismatches;

    PatternArgument(String payload, List<String> matches, List<String> mismatches) {
      super("pattern", payload);

      this.matches = matches;
      this.mismatches = mismatches;
    }

    /**
     * Samples matching {@linkplain #getPayload() regex}.
     */
    public List<String> getMatches() {
      return matches;
    }

    /**
     * Samples not matching {@linkplain #getPayload() regex}.
     */
    public List<String> getMismatches() {
      return mismatches;
    }
  }

  /**
   * <a href=
   * "https://semver.org/#is-there-a-suggested-regular-expression-regex-to-check-a-semver-string">Official
   * Semantic Versioning 2.0 regular expression</a>.
   */
  private static final Pattern PATTERN__SEM_VER = Pattern.compile("""
      ^\
      (0|[1-9]\\d*)\\.\
      (0|[1-9]\\d*)\\.\
      (0|[1-9]\\d*)\
      (?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)\
      (?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?\
      (?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?\
      $""");

  @Test
  void globToRegex() {
    VERIFIER__COMBINATION.verify(
        (glob) -> {
          var actual = Patterns.globToRegex(nonNull(glob.getPayload()));

          assertRegexMatches(actual, glob.getMatches(), true);
          assertRegexMatches(actual, glob.getMismatches(), false);
          return actual;
        },
        List.of("glob"),
        // glob
        List.of(
            new PatternArgument("**/my*.*",
                List.of(
                    "/home/usr/Pictures/myFile.jpg",
                    "/home/usr/Documents/myFile.html",
                    "/home/usr/Projects/foobar/foobar-core/src/main/resources/html/myProject.html"),
                List.of(
                    "myFile.jpg",
                    "/home/usr/Documents/file.html")),
            /*
             * Same as previous case, to demonstrate transparent support to Windows despite Unix
             * separator.
             */
            new PatternArgument("**/my*.*",
                List.of(
                    "C:\\usr\\Pictures\\myFile.jpg",
                    "C:\\usr\\Documents\\myFile.html",
                    "C:\\usr\\Projects\\foobar\\foobar-core\\src\\main\\resources\\html\\myProject.html"),
                List.of(
                    "myFile.jpg",
                    "C:\\usr\\Documents\\file.html")),
            /*
             * Same as previous case, to demonstrate transparent support to Unix despite Windows
             * separator.
             */
            new PatternArgument("**\\my*.*",
                List.of(
                    "/home/usr/Pictures/myFile.jpg",
                    "/home/usr/Documents/myFile.html",
                    "/home/usr/Projects/foobar/foobar-core/src/main/resources/html/myProject.html"),
                List.of(
                    "myFile.jpg",
                    "/home/usr/Documents/file.html")),
            new PatternArgument("**User/**/foo?a?/*.md",
                List.of(
                    "/home/User/MyDocs/foobar/readme.md",
                    "/home/BlueUser/MyDocs/foocat/readme.md",
                    "/home/SuperUser/a/random/subdir/foocat/NOTE.md"),
                List.of(
                    "/home/User/foobar/readme.md",
                    "/home/BlueUser/MyDocs/fooca/readme.md",
                    "/home/SuperUser/a/random/subdir/foocat/NOTEmd"))));
  }

  @Test
  void indexOfMatchFailure() {
    VERIFIER__COMBINATION.verify(
        (input) -> {
          Matcher matcher = PATTERN__SEM_VER.matcher(input);
          //noinspection ResultOfMethodCallIgnored
          matcher.find();
          return Patterns.indexOfMatchFailure(matcher);
        },
        List.of("input"),
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

  @Test
  void wildcardToRegex() {
    VERIFIER__COMBINATION.verify(
        (wildcard) -> {
          var actual = Patterns.wildcardToRegex(nonNull(wildcard.getPayload()));

          assertRegexMatches(actual, wildcard.getMatches(), true);
          assertRegexMatches(actual, wildcard.getMismatches(), false);
          return actual;
        },
        List.of("wildcard"),
        // wildcard
        List.of(
            new PatternArgument("Som? content. * more (*)\\?",
                List.of("Some content. Whatever more (don't know)?"),
                List.of("Som content. Whatever more (don't know)?"))));
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
