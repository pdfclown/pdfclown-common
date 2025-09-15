/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Has.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion.match;

import java.util.function.Function;
import org.hamcrest.Condition;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 * Generalized, strongly-typed alternative to {@link org.hamcrest.beans.HasPropertyWithValue},
 * asserting that a transformation of an argument meets the matcher.
 *
 * @param <T>
 *          Argument type.
 * @author Stefano Chizzolini
 */
public class Has<T> extends TypeSafeDiagnosingMatcher<T> {
  private final String mappingDescription;
  private final Function<T, Object> mapper;
  private final Matcher<Object> matcher;

  /**
  */
  public Has(String mappingDescription, Function<T, Object> mapper,
      Matcher<Object> matcher) {
    this.mappingDescription = mappingDescription;
    this.mapper = mapper;
    this.matcher = matcher;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("has ").appendValue(mappingDescription).appendText(" that ")
        .appendDescriptionOf(matcher);
  }

  @Override
  protected boolean matchesSafely(T item, Description mismatchDescription) {
    return Condition.matched(mapper.apply(item), mismatchDescription).matching(matcher, "instead ");
  }
}
