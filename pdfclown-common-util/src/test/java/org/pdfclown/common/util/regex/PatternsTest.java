/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (PatternsTest.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common> (this Program).

  This Program is free software: you can redistribute it and/or modify it under the terms of the GNU
  Lesser General Public License (LGPL) as published by the Free Software Foundation, either version
  3 of the License, or (at your option) any later version.

  This Program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this Program.
  If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>.
 */
package org.pdfclown.common.util.regex;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.pdfclown.common.build.test.Tests.argumentsStream;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.pdfclown.common.build.test.Tests.Argument;
import org.pdfclown.common.util.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
public class PatternsTest extends BaseTest {
  static class RegexArgument extends Argument<String> {
    /**
     * Samples matching {@linkplain #getValue() regex}.
     */
    List<String> matches;
    /**
     * Samples not matching {@linkplain #getValue() regex}.
     */
    List<String> mismatches;

    public RegexArgument(String value, List<String> matches, List<String> mismatches) {
      super(value, "");

      this.matches = matches;
      this.mismatches = mismatches;
    }
  }

  private static Stream<Arguments> _globToRegex() {
    return argumentsStream(
        // expected
        asList(
            "/.*/my[^/]*\\.[^/]*",
            "/home/[^/]*User/.*/foo.a./[^/]*\\.md"),
        // pattern
        asList(
            new RegexArgument("/**/my*.*",
                asList(
                    "/home/usr/Pictures/myRainbow.jpg",
                    "/home/usr/Documents/myFile.html",
                    "/home/usr/Projects/foobar/foobar-core/src/main/resources/html/myProject.html"),
                asList(
                    "myRainbow.jpg",
                    "/home/usr/Documents/file.html")),
            new RegexArgument("/home/*User/**/foo?a?/*.md",
                asList(
                    "/home/User/MyDocs/foobar/readme.md",
                    "/home/BlueUser/MyDocs/foocat/readme.md",
                    "/home/SuperUser/a/random/subdir/foocat/NOTE.md"),
                asList(
                    "/home/User/foobar/readme.md",
                    "/home/BlueUser/MyDocs/fooca/readme.md",
                    "/home/SuperUser/a/random/subdir/foocat/NOTEmd"))));
  }

  private static Stream<Arguments> _wildcardToRegex() {
    return argumentsStream(
        // expected
        asList(
            "Som. content\\. .* more \\(.*\\)\\?"),
        // pattern
        asList(
            new RegexArgument("Som? content. * more (*)\\?",
                asList("Some content. Whatever more (don't know)?"),
                asList("Som content. Whatever more (don't know)?"))));
  }

  @ParameterizedTest
  @MethodSource
  public void _globToRegex(String expected, RegexArgument glob) {
    var actual = (String) evalParameterized(
        () -> Patterns.globToRegex(glob.getValue()));

    /*
     * DO NOT remove (useful in case of arguments update)
     */
    //    generateExpected(actual,
    //        asList("glob"),
    //        asList(glob));

    assertParameterized(actual, expected);
    assertRegexMatches(actual, glob.matches, true);
    assertRegexMatches(actual, glob.mismatches, false);
  }

  @ParameterizedTest
  @MethodSource
  public void _wildcardToRegex(String expected, RegexArgument pattern) {
    var actual = (String) evalParameterized(
        () -> Patterns.wildcardToRegex(pattern.getValue()));

    /*
     * DO NOT remove (useful in case of arguments update)
     */
    //    generateExpected(actual,
    //        asList("pattern"),
    //        asList(pattern));

    assertParameterized(actual, expected);
    assertRegexMatches(actual, pattern.matches, true);
    assertRegexMatches(actual, pattern.mismatches, false);
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
