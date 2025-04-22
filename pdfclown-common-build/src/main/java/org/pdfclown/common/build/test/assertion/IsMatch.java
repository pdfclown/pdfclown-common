/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (IsMatch.java) is part of pdfclown-common-build module in pdfClown Common project
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

    return expectedValue.getClass().isInstance(actualValue)
        && predicate.test((T) actualValue, expectedValue);
  }
}
