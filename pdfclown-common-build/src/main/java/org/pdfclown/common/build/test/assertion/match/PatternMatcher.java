/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (PatternMatcher.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: © 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0

  -------------------------------------------------------------------------------------------------
  Source: https://github.com/talsma-ict/umldoclet/blob/e9e7ce933f564da9a0dbbca476bd74a25d6f0663/src/test/java/nl/talsmasoftware/umldoclet/testing/PatternMatcher.java
 */
package org.pdfclown.common.build.test.assertion.match;

import static java.util.Objects.requireNonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

// SourceFQN: nl.talsmasoftware.umldoclet.testing.PatternMatcher
/**
 * Matches regular expression patterns.
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation to pdfClown)
 */
public class PatternMatcher extends TypeSafeMatcher<String> {
  /**
   * Creates a matcher that matches when the examined text contains the given regular expression.
   */
  public static PatternMatcher containsPattern(CharSequence regex) {
    return containsPattern(regex, 0);
  }

  /**
   * Creates a matcher that matches when the examined text contains the given regular expression.
   *
   * @param flags
   *          (see {@link Pattern#compile( String, int)})
   */
  public static PatternMatcher containsPattern(CharSequence regex, int flags) {
    //noinspection MagicConstant
    return containsPattern(Pattern.compile(requireNonNull(regex, "`pattern`").toString(), flags));
  }

  /**
   * Creates a matcher that matches when the examined text contains the given pattern.
   */
  public static PatternMatcher containsPattern(Pattern pattern) {
    return new PatternMatcher(pattern, false);
  }

  /**
   * Creates a matcher that matches when the examined text matches the given regular expression.
   */
  public static PatternMatcher matchesPattern(CharSequence regex) {
    return matchesPattern(regex, 0);
  }

  /**
   * Creates a matcher that matches when the examined text matches the given regular expression.
   *
   * @param flags
   *          (see {@link Pattern#compile( String, int)})
   */
  public static PatternMatcher matchesPattern(CharSequence regex, int flags) {
    //noinspection MagicConstant
    return matchesPattern(Pattern.compile(requireNonNull(regex, "`pattern`").toString(), flags));
  }

  /**
   * Creates a matcher that matches when the examined text matches the given pattern.
   */
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
