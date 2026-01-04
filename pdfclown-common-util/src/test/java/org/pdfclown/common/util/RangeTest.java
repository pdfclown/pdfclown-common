/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (RangeTest.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.Range.Endpoint;
import org.pdfclown.common.util.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
@SuppressWarnings("Convert2MethodRef")
class RangeTest extends BaseTest {
  enum TestEnum {
    ONE,
    TWO,
    THREE,
    FOUR
  }

  @Test
  void _differentNumberTypes() {
    var ex = assertThrows(ArgumentException.class, () -> {
      Range.closed(0, 5.5);
    });
    assertThat(ex.getMessage(), is("`upper.class` (Double): MUST be Integer"));
  }

  @Test
  void atLeast() {
    Range<TestEnum> range = Range.atLeast(TestEnum.THREE);

    assertThat(range.getLower().isClosed(), is(true));
    assertThat(range.getLower().isBounded(), is(true));
    assertThat(range.getLower().getValue(), is(TestEnum.THREE));
    assertThat(range.getUpper().isClosed(), is(false));
    assertThat(range.getUpper().isBounded(), is(false));
    var ex = assertThrows(IllegalStateException.class, () -> {
      range.getUpper().getValue();
    });
    assertThat(ex.getMessage(), is("UNBOUNDED"));
    assertThat(range.toString(), is("[THREE,+∞)"));

    assertThat(range.contains(TestEnum.ONE), is(false));
    assertThat(range.contains(TestEnum.TWO), is(false));
    assertThat(range.contains(TestEnum.THREE), is(true));
    assertThat(range.contains(TestEnum.FOUR), is(true));
  }

  @Test
  void atMost() {
    Range<TestEnum> range = Range.atMost(TestEnum.THREE);

    assertThat(range.getLower().isClosed(), is(false));
    assertThat(range.getLower().isBounded(), is(false));
    var ex = assertThrows(IllegalStateException.class, () -> {
      range.getLower().getValue();
    });
    assertThat(ex.getMessage(), is("UNBOUNDED"));
    assertThat(range.getUpper().isClosed(), is(true));
    assertThat(range.getUpper().isBounded(), is(true));
    assertThat(range.getUpper().getValue(), is(TestEnum.THREE));
    assertThat(range.toString(), is("(-∞,THREE]"));

    assertThat(range.contains(TestEnum.ONE), is(true));
    assertThat(range.contains(TestEnum.TWO), is(true));
    assertThat(range.contains(TestEnum.THREE), is(true));
    assertThat(range.contains(TestEnum.FOUR), is(false));
  }

  @Test
  void closed() {
    Range<TestEnum> range = Range.closed(TestEnum.ONE, TestEnum.THREE);

    assertThat(range.getLower().isClosed(), is(true));
    assertThat(range.getLower().isBounded(), is(true));
    assertThat(range.getLower().getValue(), is(TestEnum.ONE));
    assertThat(range.getUpper().isClosed(), is(true));
    assertThat(range.getUpper().isBounded(), is(true));
    assertThat(range.getUpper().getValue(), is(TestEnum.THREE));
    assertThat(range.toString(), is("[ONE,THREE]"));

    assertThat(range.contains(TestEnum.ONE), is(true));
    assertThat(range.contains(TestEnum.TWO), is(true));
    assertThat(range.contains(TestEnum.THREE), is(true));
    assertThat(range.contains(TestEnum.FOUR), is(false));
  }

  @Test
  void greaterThan() {
    Range<TestEnum> range = Range.greaterThan(TestEnum.THREE);

    assertThat(range.getLower().isClosed(), is(false));
    assertThat(range.getLower().isBounded(), is(true));
    assertThat(range.getLower().getValue(), is(TestEnum.THREE));
    assertThat(range.getUpper().isClosed(), is(false));
    assertThat(range.getUpper().isBounded(), is(false));
    var ex = assertThrows(IllegalStateException.class, () -> {
      range.getUpper().getValue();
    });
    assertThat(ex.getMessage(), is("UNBOUNDED"));
    assertThat(range.toString(), is("(THREE,+∞)"));

    assertThat(range.contains(TestEnum.ONE), is(false));
    assertThat(range.contains(TestEnum.TWO), is(false));
    assertThat(range.contains(TestEnum.THREE), is(false));
    assertThat(range.contains(TestEnum.FOUR), is(true));
  }

  @Test
  void lessThan() {
    Range<TestEnum> range = Range.lessThan(TestEnum.THREE);

    assertThat(range.getLower().isClosed(), is(false));
    assertThat(range.getLower().isBounded(), is(false));
    var ex = assertThrows(IllegalStateException.class, () -> {
      range.getLower().getValue();
    });
    assertThat(ex.getMessage(), is("UNBOUNDED"));
    assertThat(range.getUpper().isClosed(), is(false));
    assertThat(range.getUpper().isBounded(), is(true));
    assertThat(range.getUpper().getValue(), is(TestEnum.THREE));
    assertThat(range.toString(), is("(-∞,THREE)"));

    assertThat(range.contains(TestEnum.ONE), is(true));
    assertThat(range.contains(TestEnum.TWO), is(true));
    assertThat(range.contains(TestEnum.THREE), is(false));
    assertThat(range.contains(TestEnum.FOUR), is(false));
  }

  @Test
  void normal() {
    {
      Range<Float> range = Range.normal(Float.class);

      /*
       * The normal range instance is reused whenever a range with the same characteristics is
       * requested.
       */
      assertThat(Range.closed(0f, 1f), sameInstance(range));
      assertThat(range.contains(-1f), is(false));
      assertThat(range.contains(0f), is(true));
      assertThat(range.contains(.5f), is(true));
      assertThat(range.contains(1f), is(true));
      assertThat(range.contains(2f), is(false));
    }

    {
      Range<Double> range = Range.normal(Double.class);

      /*
       * The normal range instance is reused whenever a range with the same characteristics is
       * requested.
       */
      assertThat(Range.closed(0d, 1d), sameInstance(range));
      assertThat(range.contains(-1d), is(false));
      assertThat(range.contains(0d), is(true));
      assertThat(range.contains(.5d), is(true));
      assertThat(range.contains(1d), is(true));
      assertThat(range.contains(2d), is(false));
    }

    {
      Range<Integer> range = Range.normal(Integer.class);

      /*
       * The normal range instance is reused whenever a range with the same characteristics is
       * requested.
       */
      assertThat(Range.closed(0, 1), sameInstance(range));
      assertThat(range.contains(-1), is(false));
      assertThat(range.contains(0), is(true));
      assertThat(range.contains(1), is(true));
      assertThat(range.contains(2), is(false));
    }
  }

  /**
   * Tests the behavior of numeric ranges, compared to regular ones.
   */
  @Test
  void numeric() {
    var regularRange = Range.closed(0f, 1f);
    var numericRange = Range.numeric(regularRange);

    assertThat(numericRange, not(regularRange));

    /*
     * Regular ranges fail to evaluate values of numeric types other than their own.
     */
    assertThat(regularRange.contains(0f), is(true));
    assertThrows(ClassCastException.class, () -> {
      regularRange.contains(0);
    });

    /*
     * Numeric ranges successfully evaluate values of any numeric type.
     */
    assertThat(numericRange.contains(0f), is(true));
    assertThat(numericRange.contains(0), is(true));
    assertThat(numericRange.contains(.1f), is(true));
    assertThat(numericRange.contains(.5), is(true));
    assertThat(numericRange.contains(1L), is(true));
    assertThat(numericRange.contains(-.5), is(false));
    assertThat(numericRange.contains(2.5), is(false));
  }

  @Test
  void of() {
    Endpoint<TestEnum> lower = Endpoint.of(TestEnum.ONE, false);
    Endpoint<TestEnum> upper = Endpoint.of(TestEnum.THREE, true);
    Range<TestEnum> range = Range.of(lower, upper);

    assertThat(range.getLower(), sameInstance(lower));
    assertThat(range.getUpper(), sameInstance(upper));

    assertThat(range.getLower().isClosed(), is(false));
    assertThat(range.getLower().isBounded(), is(true));
    assertThat(range.getLower().getValue(), is(TestEnum.ONE));
    assertThat(range.getUpper().isClosed(), is(true));
    assertThat(range.getUpper().isBounded(), is(true));
    assertThat(range.getUpper().getValue(), is(TestEnum.THREE));
    assertThat(range.toString(), is("(ONE,THREE]"));

    assertThat(range.contains(TestEnum.ONE), is(false));
    assertThat(range.contains(TestEnum.TWO), is(true));
    assertThat(range.contains(TestEnum.THREE), is(true));
    assertThat(range.contains(TestEnum.FOUR), is(false));
  }

  @Test
  void open() {
    Range<TestEnum> range = Range.open(TestEnum.ONE, TestEnum.THREE);

    assertThat(range.getLower().isClosed(), is(false));
    assertThat(range.getLower().isBounded(), is(true));
    assertThat(range.getLower().getValue(), is(TestEnum.ONE));
    assertThat(range.getUpper().isClosed(), is(false));
    assertThat(range.getUpper().isBounded(), is(true));
    assertThat(range.getUpper().getValue(), is(TestEnum.THREE));
    assertThat(range.toString(), is("(ONE,THREE)"));

    assertThat(range.contains(TestEnum.ONE), is(false));
    assertThat(range.contains(TestEnum.TWO), is(true));
    assertThat(range.contains(TestEnum.THREE), is(false));
    assertThat(range.contains(TestEnum.FOUR), is(false));
  }
}