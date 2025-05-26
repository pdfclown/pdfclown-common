/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (AssertionsTest.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.build.test.assertion;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.Shape;
import java.awt.geom.Path2D;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.pdfclown.common.build.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
public class AssertionsTest extends BaseTest {
  private static final double DBL_DELTA = 1e-6;

  private static Shape polygon(double[] coords) {
    var ret = new Path2D.Double();
    ret.moveTo(coords[0], coords[1]);
    for (int i = 1, l = coords.length - 1; i < l;) {
      ret.lineTo(coords[++i], coords[++i]);
    }
    ret.closePath();
    return ret;
  }

  @Test
  public void _assertShapeEquals() {
    assertDoesNotThrow(
        () -> Assertions.assertShapeEquals(
            polygon(new double[] { .1, .1, .8, .1, .9, .9, .2, .19 }),
            polygon(new double[] { .1, .1, .8, .1, .9, .9, .2, .19 }), DBL_DELTA));

    {
      var thrown = assertThrows(AssertionFailedError.class,
          () -> Assertions.assertShapeEquals(
              polygon(new double[] { .1, .1, .8, .1, .9, .9, .2, .19 }),
              polygon(new double[] { .1, .1, .8, .1, .9, 52, .2, .19 }), DBL_DELTA));
      assertEquals("Segment 2, point 0, y ==> expected: <0.9> but was: <52.0>",
          thrown.getCause().getMessage());
    }

    {
      var thrown = assertThrows(AssertionFailedError.class,
          () -> Assertions.assertShapeEquals(
              polygon(new double[] { .1, .1, .8, .1, .9, .9, .2, .19 }),
              polygon(new double[] { .1, .1, .8, .1, .9, .9 }), DBL_DELTA));
      assertEquals("Segment 3, segmentKind ==> expected: <1> but was: <4>",
          thrown.getCause().getMessage());
    }

    {
      var thrown = assertThrows(AssertionFailedError.class,
          () -> Assertions.assertShapeEquals(
              polygon(new double[] { .1, .1, .8, .1, .9, .9 }),
              polygon(new double[] { .1, .1, .8, .1, .9, .9, .2, .19 }), DBL_DELTA));
      assertEquals("Segment 3, segmentKind ==> expected: <4> but was: <1>",
          thrown.getCause().getMessage());
    }
  }
}
