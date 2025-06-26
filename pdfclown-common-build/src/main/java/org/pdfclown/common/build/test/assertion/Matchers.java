/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (TextMatcher.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.hamcrest.Matcher;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.build.test.assertion.match.HasMatcher;
import org.pdfclown.common.build.test.assertion.match.PatternMatcher;
import org.pdfclown.common.build.test.assertion.match.TextFileMatcher;
import org.pdfclown.common.build.test.assertion.match.TextMatcher;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

/**
 * Assertion matchers.
 *
 * @author Stefano Chizzolini
 */
public final class Matchers {
  /**
   * Maps the given items to the corresponding matchers.
   */
  @SafeVarargs
  @SuppressWarnings("unchecked")
  public static <E, T extends Matcher<? super E>> List<Matcher<? super E>> asMatchers(
      Function<E, T> mapper, E... elements) {
    var ret = new ArrayList<T>();
    for (var element : elements) {
      ret.add(mapper.apply(element));
    }
    /*
     * NOTE: Cast is necessary because, due to type erasure, we are forced to declare
     * `List<Matcher<? super E>>` as return type instead of List<T>, since the latter would be
     * statically resolved as List<Object>, causing the linker to match wrong overloads (eg, see
     * org.hamcrest.Matchers.arrayContaining(..)).
     */
    return (List<Matcher<? super E>>) ret;
  }

  /**
   * Creates a matcher that matches when the examined text contains the given regular expression.
   */
  public static PatternMatcher containsPattern(CharSequence regex) {
    return PatternMatcher.containsPattern(regex);
  }

  /**
   * Creates a matcher that matches when the examined text contains the given regular expression.
   *
   * @param flags
   *          (see {@link Pattern#compile( String, int)})
   */
  public static PatternMatcher containsPattern(CharSequence regex, int flags) {
    return PatternMatcher.containsPattern(regex, flags);
  }

  /**
   * Creates a matcher that matches when the examined text contains the given pattern.
   */
  public static PatternMatcher containsPattern(Pattern pattern) {
    return PatternMatcher.containsPattern(pattern);
  }

  /**
   * Creates a matcher that matches when the transformation of the examined object satisfies the
   * given matcher.
   *
   * @param mappingDescription
   *          Description of the transformation (typically, should be expressed as the bean path
   *          corresponding to the transformation of an argument via {@code mapper}).
   * @param mapper
   *          Transforms an argument.
   * @param matcher
   *          Matches the result of {@code mapper}.
   * @param <T>
   *          Argument type.
   */
  public static <T> HasMatcher<T> has(String mappingDescription, Function<T, Object> mapper,
      Matcher<Object> matcher) {
    return HasMatcher.has(mappingDescription, mapper, matcher);
  }

  /**
   * Creates a matcher that matches when the examined event has the given attributes.
   */
  public static Matcher<LoggingEvent> matchesEvent(Level level, String message,
      @Nullable Class<Throwable> causeType) {
    return allOf(
        has("level", LoggingEvent::getLevel, is(level)),
        has("message", LoggingEvent::getMessage, is(message)),
        has("cause", LoggingEvent::getThrowable,
            is(causeType != null ? instanceOf(causeType) : nullValue())));
  }

  /**
   * Creates a matcher that matches when the examined text content equals the expected one.
   *
   * @param expectedContentPath
   *          Path of the file containing the expected text.
   */
  public static TextFileMatcher matchesFileContent(Path expectedContentPath) {
    return TextFileMatcher.matchesFileContent(expectedContentPath);
  }

  /**
   * Creates a matcher that matches when the examined text content equals the expected one, ignoring
   * case considerations.
   *
   * @param expectedContentPath
   *          Path of the file containing the expected text.
   */
  public static TextFileMatcher matchesFileContentIgnoreCase(Path expectedContentPath) {
    return TextFileMatcher.matchesFileContentIgnoreCase(expectedContentPath);
  }

  /**
   * Creates a matcher that matches when the examined text matches the given regular expression.
   */
  public static PatternMatcher matchesPattern(CharSequence regex) {
    return PatternMatcher.matchesPattern(regex);
  }

  /**
   * Creates a matcher that matches when the examined text matches the given regular expression.
   *
   * @param flags
   *          (see {@link Pattern#compile( String, int)})
   */
  public static PatternMatcher matchesPattern(CharSequence regex, int flags) {
    return PatternMatcher.matchesPattern(regex, flags);
  }

  /**
   * Creates a matcher that matches when the examined text matches the given pattern.
   */
  public static PatternMatcher matchesPattern(Pattern pattern) {
    return PatternMatcher.matchesPattern(pattern);
  }

  /**
   * Creates a matcher that matches when the examined text equals the expected one.
   *
   * @param expected
   *          Expected text.
   */
  public static TextMatcher matchesText(String expected) {
    return TextMatcher.matchesText(expected);
  }

  /**
   * Creates a matcher that matches when the examined text equals the expected one, ignoring case
   * considerations.
   *
   * @param expected
   *          Expected text.
   */
  public static TextMatcher matchesTextIgnoreCase(String expected) {
    return TextMatcher.matchesTextIgnoreCase(expected);
  }

  private Matchers() {
  }
}
