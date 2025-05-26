/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (PathEvaluator.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion;

import java.awt.Shape;
import java.awt.geom.PathIterator;

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
   * Evaluates the given graphical path.
   */
  static void eval(PathIterator itr, PathEvaluator evaluator) {
    var coords = new double[3 * 2];
    for (; !itr.isDone(); itr.next()) {
      final int segmentKind = itr.currentSegment(coords);
      final int coordsCount;
      switch (segmentKind) {
        case PathIterator.SEG_LINETO:
        case PathIterator.SEG_MOVETO:
          //noinspection PointlessArithmeticExpression: informational purposes.
          coordsCount = 1 * 2;
          break;
        case PathIterator.SEG_QUADTO:
          coordsCount = 2 * 2;
          break;
        case PathIterator.SEG_CUBICTO:
          coordsCount = 3 * 2;
          break;
        case PathIterator.SEG_CLOSE:
          //noinspection PointlessArithmeticExpression: informational purposes.
          coordsCount = 0 * 2;
          break;
        default:
          throw new UnsupportedOperationException("segmentKind UNEXPECTED: " + segmentKind);
      }
      if (!evaluator.eval(segmentKind, coords, coordsCount)) {
        break;
      }
    }
  }

  /**
   * Evaluates the given shape.
   */
  static void eval(Shape shape, PathEvaluator evaluator) {
    eval(shape.getPathIterator(null), evaluator);
  }

  /**
   * Evaluates the given graphical path segment.
   *
   * @param segmentKind
   *          Segment type (see {@link PathIterator#currentSegment(double[])}).
   * @param coords
   *          Segment coordinates buffer (<span class="warning">WARNING: Reused across the iteration
   *          — if you need to retain its content, clone it</span>).
   * @param coordsCount
   *          Number of actual coordinates in {@code coords}.
   * @return Whether iteration should continue.
   */
  boolean eval(int segmentKind, double[] coords, int coordsCount);
}
