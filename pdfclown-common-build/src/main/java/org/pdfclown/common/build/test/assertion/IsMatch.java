/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (IsMatch.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion;

import java.util.function.BiPredicate;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jspecify.annotations.Nullable;

/**
 * Generic predicate matcher.
 *
 * @param <T>
 * @author Stefano Chizzolini
 */
public class IsMatch<T> extends BaseMatcher<T> {
  public static <T> Matcher<T> matches(T expectedValue, BiPredicate<T, T> predicate) {
    return new IsMatch<>(expectedValue, predicate);
  }

  private final @Nullable T expectedValue;
  private final BiPredicate<T, T> predicate;

  public IsMatch(T expectedValue, BiPredicate<T, T> predicate) {
    this.expectedValue = expectedValue;
    this.predicate = predicate;
  }

  @Override
  public void describeTo(Description description) {
    description.appendValue(expectedValue);
  }

  @Override
  @SuppressWarnings({ "unchecked", "null" })
  public boolean matches(@Nullable Object actualValue) {
    if (actualValue == null)
      return expectedValue == null;

    return expectedValue != null
        && expectedValue.getClass().isInstance(actualValue)
        && predicate.test((T) actualValue, expectedValue);
  }
}
