/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ContainsPattern.java) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion.match;

import static java.util.Objects.requireNonNull;

import java.util.regex.Pattern;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matches when the examined text contains the pattern.
 *
 * @author Stefano Chizzolini
 * @see org.hamcrest.text.MatchesPattern
 */
public class ContainsPattern extends TypeSafeMatcher<String> {
  private final Pattern pattern;

  /**
  */
  public ContainsPattern(Pattern pattern) {
    this.pattern = requireNonNull(pattern, "`pattern`");
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("a string containing the pattern ").appendValue(pattern);
  }

  @Override
  public boolean matchesSafely(String item) {
    return pattern.matcher(item).find();
  }
}
