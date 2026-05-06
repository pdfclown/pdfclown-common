/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Assertions.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.pdfclown.common.util.Conditions.requireNotBlank;
import static org.pdfclown.common.util.Objects.textLiteral;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.DoubleConsumer;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;

/**
 * Assertion utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Assertions {
  /**
   * Argument value wrapper to use within assertion-related objects like {@link Arguments}.
   *
   * @param <T>
   *          Argument value type.
   * @author Stefano Chizzolini
   */
  public static class Argument<T extends @Nullable Object> implements Named<T> {
    /**
     * Creates a named argument with qualified string representation, showing both its name and its
     * payload.
     *
     * @throws IllegalArgumentException
     *           if {@code name} is blank.
     * @see Named#named(String, Object)
     */
    public static <T extends @Nullable Object> Named<T> qnamed(String name, T payload) {
      return Named.of(toString(requireNotBlank(name, "name"), payload), payload);
    }

    private static String toString(String name, @Nullable Object payload) {
      return !name.isEmpty() ? "%s (%s)".formatted(textLiteral(payload), name)
          : textLiteral(payload);
    }

    private final String name;
    private final T payload;

    public Argument(String name, T payload) {
      this.name = name;
      this.payload = payload;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public T getPayload() {
      return payload;
    }

    @Override
    public String toString() {
      return toString(name, payload);
    }
  }

  private static final FileTreeAsserter ASSERTER__FILE_TREE = new FileTreeAsserter();
  private static final TextAsserter ASSERTER__TEXT = new TextAsserter();

  /**
   * Asserts that a file tree matches the expected one.
   *
   * @param expectedDirResourceName
   *          Resource name of the expected file tree.
   * @param actualDir
   *          Actual file tree.
   * @param test
   *          Current test unit.
   * @throws AssertionError
   *           if {@code actualDir} doesn't match the one at {@code expectedDirResourceName}.
   * @see Asserter#SYSTEM_PROPERTY__FILES_UPDATE
   */
  public static void assertFileTreeEquals(String expectedDirResourceName, Path actualDir,
      Test test) {
    ASSERTER__FILE_TREE.assertEquals(expectedDirResourceName, actualDir, new Asserter.Config(test));
  }

  /**
   * Asserts that {@code expected} and {@code actual} are equal applying a comparator.
   * <p>
   * This method supplements junit's
   * {@link org.junit.jupiter.api.Assertions#assertIterableEquals(Iterable, Iterable)
   * assertIterableEquals(..)} (which uses standard {@link Object#equals(Object)} for the same
   * purpose) to make the evaluation more flexible.
   * </p>
   */
  public static <T> void assertIterableEquals(Iterable<T> expected, Iterable<T> actual,
      Comparator<T> comparator) {
    if (expected == actual)
      return;

    Iterator<T> expectedItr = expected.iterator();
    Iterator<T> actualItr = actual.iterator();
    while (expectedItr.hasNext()) {
      assertTrue(actualItr.hasNext());

      T expectedElement = expectedItr.next();
      T actualElement = actualItr.next();
      //noinspection SimplifiableAssertion
      assertTrue(comparator.compare(expectedElement, actualElement) == 0);
    }
    assertFalse(actualItr.hasNext());
  }

  /**
   * Asserts that a file matches the expected one.
   *
   * @param expectedResourceName
   *          Resource name of the expected file.
   * @param actualFile
   *          Actual file.
   * @param test
   *          Current test unit.
   * @throws AssertionError
   *           if {@code actualFile} doesn't match the content of {@code expectedResourceName}.
   * @see Asserter#SYSTEM_PROPERTY__FILES_UPDATE
   */
  public static void assertTextEquals(String expectedResourceName, Path actualFile, Test test) {
    ASSERTER__TEXT.assertEquals(expectedResourceName, actualFile, new Asserter.Config(test));
  }

  /**
   * Asserts that a content matches the expected one.
   *
   * @param expectedResourceName
   *          Resource name of the expected content.
   * @param actualContent
   *          Actual content.
   * @param test
   *          Current test unit.
   * @throws AssertionError
   *           if {@code actualContent} doesn't match the content of {@code expectedResourceName}.
   * @see Asserter#SYSTEM_PROPERTY__FILES_UPDATE
   */
  public static void assertTextEquals(String expectedResourceName, String actualContent,
      Test test) {
    ASSERTER__TEXT.assertEquals(expectedResourceName, actualContent, new Asserter.Config(test));
  }

  /**
   * Gets the absolute floating-point error tolerance with the minimum order of magnitude, enough to
   * pass the assertion.
   * <p>
   * Useful to quickly tune assertions in a robust manner, wherever
   * {@linkplain Double#compare(double, double) exact} floating-point comparison is unsuitable.
   * </p>
   *
   * @param assertWrap
   *          Lambda wrapping the assertion to probe.
   * @apiNote To use it, wrap your assertion inside {@code assertWrap}, wiring its argument as the
   *          delta of your assertion; for example, if the assertion
   *          is:<pre class="lang-java"><code>
   * assertEquals(myExpected, myActual, myDelta);</code></pre>
   *          <p>
   *          wrap it this way:
   *          </p>
   *          <pre class="lang-java"><code>
   * Assertions.probeDelta($ {@code ->} {
   *   assertEquals(myExpected, myActual, $);
   * });</code></pre>
   *          <p>
   *          and run your test; probeDelta will iterate until success, printing to stdout the
   *          resulting delta:
   *          </p>
   *          <pre>
   * probeDelta -- result: 1.0E-7</pre>
   */
  public static double probeDelta(DoubleConsumer assertWrap) {
    var delta = 1e-16;
    while (true) {
      try {
        assertWrap.accept(delta);

        System.out.println("probeDelta -- result: " + delta);

        return delta;
      } catch (AssertionError ex) {
        delta *= 10 /* Inflates delta to the next order of magnitude */;
      }
    }
  }

  private Assertions() {
  }
}
