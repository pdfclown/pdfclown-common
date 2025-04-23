/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (PatternMatcher.java) is part of pdfclown-common-build module in pdfClown Common project
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
/*
  SPDX-FileCopyrightText: © 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.common.build.test.assertion;

import static java.util.Objects.requireNonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/*
 * SourceFQN: nl.talsmasoftware.umldoclet.testing.PatternMatcher
 * Source: https://github.com/talsma-ict/umldoclet/blob/e9e7ce933f564da9a0dbbca476bd74a25d6f0663/src/test/java/nl/talsmasoftware/umldoclet/testing/PatternMatcher.java
 */
/**
 * Matches regular expression patterns.
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation to pdfClown)
 */
public class PatternMatcher extends TypeSafeMatcher<String> {
  public static PatternMatcher containsPattern(CharSequence pattern) {
    return containsPattern(pattern, 0);
  }

  public static PatternMatcher containsPattern(CharSequence pattern, int flags) {
    return containsPattern(Pattern.compile(requireNonNull(pattern, "`pattern`").toString(), flags));
  }

  public static PatternMatcher containsPattern(Pattern pattern) {
    return new PatternMatcher(pattern, false);
  }

  public static PatternMatcher matchesPattern(CharSequence pattern) {
    return matchesPattern(pattern, 0);
  }

  public static PatternMatcher matchesPattern(CharSequence pattern, int flags) {
    return matchesPattern(Pattern.compile(requireNonNull(pattern, "`pattern`").toString(), flags));
  }

  public static PatternMatcher matchesPattern(Pattern pattern) {
    return new PatternMatcher(pattern, true);
  }

  private final boolean fullMatch;
  private final Pattern pattern;

  protected PatternMatcher(Pattern pattern, boolean fullMatch) {
    this.pattern = requireNonNull(pattern, "`pattern`");
    this.fullMatch = fullMatch;
  }

  @Override
  public void describeTo(final Description description) {
    description.appendText("a string " + (fullMatch ? "matching" : "containing")
        + " pattern \"" + pattern.pattern() + '"');
  }

  @Override
  public boolean matchesSafely(final String string) {
    final Matcher matcher = pattern.matcher(string);
    return fullMatch ? matcher.matches() : matcher.find();
  }
}
