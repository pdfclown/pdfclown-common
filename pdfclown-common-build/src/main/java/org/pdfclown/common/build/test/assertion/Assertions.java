/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  This file (Assertions.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import org.hamcrest.Matcher;
import org.pdfclown.common.build.util.Executions;
import org.pdfclown.common.build.util.PathEvaluator;

/**
 * Assertion utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Assertions {
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
   * Asserts that {@code expected} and {@code actual} are equal applying the given comparator.
   *
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
      assertTrue(comparator.compare(expectedElement, actualElement) == 0);
    }
    assertFalse(actualItr.hasNext());
  }

  /**
   * Asserts that {@code expected} and {@code actual} paths are equal within the given non-negative
   * {@code delta}.
   */
  public static void assertPathEquals(PathIterator expected, PathIterator actual, double delta) {
    class Segment {
      final int kind;
      final double[] coords;

      public Segment(int kind, double[] coords) {
        this.kind = kind;
        this.coords = coords.clone();
      }
    }

    var actualSegmentQueue = new SynchronousQueue<Segment>();
    var segmentIndex = new int[1];
    try {
      Executions.failFast(
          () -> {
            PathEvaluator.eval(expected, ($segmentKind, $coords, $coordsCount) -> {
              var assertMessagePrefix = "Segment " + segmentIndex[0]++;
              try {
                var actualSegment = actualSegmentQueue.take();
                assertEquals($segmentKind, actualSegment.kind,
                    assertMessagePrefix + ", segmentKind");

                for (int i = 0; i < $coordsCount;) {
                  var coordAssertMessagePrefix =
                      String.format("%s, point %s, ", assertMessagePrefix, i % 2);
                  assertEquals($coords[i], actualSegment.coords[i++], delta,
                      coordAssertMessagePrefix + "x");
                  assertEquals($coords[i], actualSegment.coords[i++], delta,
                      coordAssertMessagePrefix + "y");
                }
                return true;
              } catch (InterruptedException ex) {
                return false;
              }
            });
          },
          () -> {
            PathEvaluator.eval(actual, ($segmentKind, $coords, $coordsCount) -> {
              try {
                actualSegmentQueue.put(new Segment($segmentKind, $coords));
                return true;
              } catch (InterruptedException ex) {
                return false;
              }
            });
          });
      assertTrue(expected.isDone());
      assertTrue(actual.isDone());
    } catch (ExecutionException ex) {
      if (ex.getCause() instanceof AssertionError) {
        fail(ex.getCause());
      } else
        throw new RuntimeException(ex.getCause());
    } catch (InterruptedException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Asserts that {@code expected} and {@code actual} are equal within the given non-negative
   * {@code delta}.
   */
  public static void assertShapeEquals(Shape expected, Shape actual, double delta) {
    assertPathEquals(expected.getPathIterator(null), actual.getPathIterator(null), delta);
  }

  /**
   * Gets the absolute floating-point error tolerance with the minimum order of magnitude, enough to
   * pass the given assertion.
   * <p>
   * Useful to quickly tune assertions in a robust manner, wherever
   * {@linkplain Double#compare(double, double) exact} floating-point comparison is unsuitable.
   * </p>
   *
   * @param assertWrap
   *          Lambda wrapping the assertion to probe.
   * @apiNote To use it, wrap your assertion inside {@code assertWrap}, wiring its argument as the
   *          delta of your assertion; eg, if the assertion is:<pre>
   * assertEquals(myExpected, myActual, myDelta);</pre>
   *          <p>
   *          wrap it this way:
   *          </p>
   *          <pre>
   * Assertions.probeDelta($ -&gt; {
   *   assertEquals(myExpected, myActual, $);
   * });</pre>
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
