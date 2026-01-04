/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (GraphicsAssertions.java) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Exceptions.unexpected;

import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.SynchronousQueue;

/**
 * Graphics assertion utilities.
 *
 * @author Stefano Chizzolini
 */
public final class GraphicsAssertions {
  /**
   * {@link PathIterator} evaluator.
   * <p>
   * Provides a convenient way to evaluate the segments defining a graphical path.
   * </p>
   *
   * @author Stefano Chizzolini
   */
  @FunctionalInterface
  public interface PathEvaluator {
    /**
     * Evaluates a graphical path.
     */
    static void eval(PathIterator itr, PathEvaluator evaluator) {
      var coords = new double[3 * 2];
      for (; !itr.isDone(); itr.next()) {
        final int segmentKind = itr.currentSegment(coords);
        final int coordsCount = switch (segmentKind) {
          case PathIterator.SEG_LINETO, PathIterator.SEG_MOVETO ->
              //noinspection PointlessArithmeticExpression -- informational purposes
              1 * 2;
          case PathIterator.SEG_QUADTO -> 2 * 2;
          case PathIterator.SEG_CUBICTO -> 3 * 2;
          case PathIterator.SEG_CLOSE ->
              //noinspection PointlessArithmeticExpression -- informational purposes
              0 * 2;
          default -> throw unexpected("segmentKind", segmentKind);
        };
        if (!evaluator.eval(segmentKind, coords, coordsCount)) {
          break;
        }
      }
    }

    /**
     * Evaluates a shape.
     */
    static void eval(Shape shape, PathEvaluator evaluator) {
      eval(shape.getPathIterator(null), evaluator);
    }

    /**
     * Evaluates a graphical path segment.
     *
     * @param segmentKind
     *          Segment type (see {@link PathIterator#currentSegment(double[])}).
     * @param coords
     *          Segment coordinates buffer (<span class="warning">WARNING: Reused across the
     *          iteration — if you need to retain its content, clone it</span>).
     * @param coordsCount
     *          Number of actual coordinates in {@code coords}.
     * @return Whether iteration should continue.
     */
    boolean eval(int segmentKind, double[] coords, int coordsCount);
  }

  /**
   * Asserts that {@code expected} and {@code actual} paths are equal within a non-negative
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
          () -> PathEvaluator.eval(expected, ($segmentKind, $coords, $coordsCount) -> {
            var assertMessagePrefix = "Segment " + segmentIndex[0]++;
            try {
              var actualSegment = actualSegmentQueue.take();
              assertEquals($segmentKind, actualSegment.kind, assertMessagePrefix + ", segmentKind");

              for (int i = 0; i < $coordsCount;) {
                var coordAssertMessagePrefix = "%s, point %s, ".formatted(assertMessagePrefix,
                    i % 2);
                assertEquals($coords[i], actualSegment.coords[i++], delta,
                    coordAssertMessagePrefix + "x");
                assertEquals($coords[i], actualSegment.coords[i++], delta,
                    coordAssertMessagePrefix + "y");
              }
              return true;
            } catch (InterruptedException ex) {
              return false;
            }
          }),
          () -> PathEvaluator.eval(actual, ($segmentKind, $coords, $coordsCount) -> {
            try {
              actualSegmentQueue.put(new Segment($segmentKind, $coords));
              return true;
            } catch (InterruptedException ex) {
              return false;
            }
          }));
      assertTrue(expected.isDone());
      assertTrue(actual.isDone());
    } catch (ExecutionException ex) {
      if (ex.getCause() instanceof AssertionError) {
        fail(ex.getCause());
      } else
        throw runtime(ex.getCause());
    } catch (InterruptedException ex) {
      throw runtime(ex);
    }
  }

  /**
   * Asserts that {@code expected} and {@code actual} are equal within a non-negative {@code delta}.
   */
  public static void assertShapeEquals(Shape expected, Shape actual, double delta) {
    assertPathEquals(expected.getPathIterator(null), actual.getPathIterator(null), delta);
  }

  private GraphicsAssertions() {
  }
}
