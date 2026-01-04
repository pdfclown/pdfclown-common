/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Matches.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion.match;

import java.util.function.BiPredicate;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jspecify.annotations.Nullable;

/**
 * Matches a value to another one based on an arbitrary condition.
 *
 * @param <T>
 *          Value type.
 * @author Stefano Chizzolini
 */
public class Matches<T> extends BaseMatcher<T> {
  private final @Nullable T expectedValue;
  private final BiPredicate<T, T> predicate;

  /**
  */
  public Matches(T expectedValue, BiPredicate<T, T> predicate) {
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
