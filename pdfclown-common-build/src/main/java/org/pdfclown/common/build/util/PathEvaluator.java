/*
  SPDX-FileCopyrightText: © 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>
  SPDX-License-Identifier: LGPL-3.0-or-later

  Copyright 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (PathEvaluator.java) is part of pdfclown-common-build module in pdfClown Common project
  (this Program).

  This Program is free software: you can redistribute it and/or modify it under the terms of the GNU
  Lesser General Public License (LGPL) as published by the Free Software Foundation, either version
  3 of the License, or (at your option) any later version.

  This Program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this Program.
  If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>.
 */
package org.pdfclown.common.build.util;

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
   *
   * @param itr
   * @param evaluator
   */
  static void eval(PathIterator itr, PathEvaluator evaluator) {
    var coords = new double[3 * 2];
    for (; !itr.isDone(); itr.next()) {
      final int segmentKind = itr.currentSegment(coords);
      final int coordsCount;
      switch (segmentKind) {
        case PathIterator.SEG_LINETO:
        case PathIterator.SEG_MOVETO:
          coordsCount = 1 * 2;
          break;
        case PathIterator.SEG_QUADTO:
          coordsCount = 2 * 2;
          break;
        case PathIterator.SEG_CUBICTO:
          coordsCount = 3 * 2;
          break;
        case PathIterator.SEG_CLOSE:
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
   *
   * @param shape
   * @param evaluator
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
