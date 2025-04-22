/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (ArraySizeComparatorTest.java) is part of pdfclown-common-build module in pdfClown
  Common project <https://github.com/pdfclown/pdfclown-common> (this Program).

  This Program is free software: you can redistribute it and/or modify it under the terms of the GNU
  Lesser General Public License (LGPL) as published by the Free Software Foundation, either version
  3 of the License, or (at your option) any later version.

  This Program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this Program.
  If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>.
 */
/*
  SPDX-FileCopyrightText: © 2012-2022 Skyscreamer

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.common.build.internal.jsonassert.comparator;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.github.openjson.JSONException;
import java.text.MessageFormat;
import org.junit.Test;
import org.pdfclown.common.build.internal.jsonassert.JSONAssert;
import org.pdfclown.common.build.internal.jsonassert.JSONCompareMode;

/**
 * Unit tests for ArraySizeComparator
 *
 * @author Duncan Mackinder
 *
 */
public class ArraySizeComparatorTest {
  /* [upstream] twoElementArray */
  private static final String JSON_SAMPLE__ARRAY = "{a:[b,c]}";

  @Test
  public void failsWhenActualArrayLongerThanExpectedLength() throws JSONException {
    doFailingMatchTest("{a:[1]}", JSON_SAMPLE__ARRAY,
        "a\\[\\]\\s*Expected:\\s*array size of 1 elements\\s*got:\\s*2 elements\\s*");
  }

  @Test
  public void failsWhenActualArrayLongerThanMaxOfExpectedRange() throws JSONException {
    doFailingMatchTest("{a:[0,1]}", JSON_SAMPLE__ARRAY,
        "a\\[\\]\\s*Expected:\\s*array size of 0 to 1 elements\\s*got:\\s*2 elements\\s*");
  }

  @Test
  public void failsWhenActualArrayTooShort() throws JSONException {
    doFailingMatchTest("{a:[3]}", JSON_SAMPLE__ARRAY,
        "a\\[\\]\\s*Expected:\\s*array size of 3 elements\\s*got:\\s*2 elements\\s*");
  }

  @Test
  public void failsWhenExpectedArraySizeNotANumber() throws JSONException {
    doFailingMatchTest("{a:[X]}", JSON_SAMPLE__ARRAY,
        "a\\[\\]: invalid expectation: expected array size 'X' not a number");
  }

  @Test
  public void failsWhenExpectedArrayTooLong() throws JSONException {
    doFailingMatchTest("{a:[1,2,3]}", JSON_SAMPLE__ARRAY,
        "a\\[\\]: invalid expectation: expected array should contain either 1 or 2 elements but contains 3 elements");
  }

  @Test
  public void failsWhenExpectedArrayTooShort() throws JSONException {
    doFailingMatchTest("{a:[]}", JSON_SAMPLE__ARRAY,
        "a\\[\\]: invalid expectation: expected array should contain either 1 or 2 elements but contains 0 elements");
  }

  @Test
  public void failsWhenExpectedMaximumTooSmall() throws JSONException {
    doFailingMatchTest("{a:[8,6]}", JSON_SAMPLE__ARRAY,
        "a\\[\\]: invalid expectation: maximum expected array size '6' less than minimum expected array size '8'");
  }

  @Test
  public void failsWhenExpectedMinimumTooSmall() throws JSONException {
    doFailingMatchTest("{a:[-1,6]}", JSON_SAMPLE__ARRAY,
        "a\\[\\]: invalid expectation: minimum expected array size '-1' negative");
  }

  @Test
  public void failsWhenExpectedNotAllSimpleTypes() throws JSONException {
    doFailingMatchTest("{a:[{y:1},2]}", JSON_SAMPLE__ARRAY,
        "a\\[\\]: invalid expectation: minimum expected array size '\\{\"y\":1\\}' not a number");
  }

  @Test
  public void failsWhenFirstExpectedArrayElementNotANumber() throws JSONException {
    doFailingMatchTest("{a:[MIN,6]}", JSON_SAMPLE__ARRAY,
        "a\\[\\]: invalid expectation: minimum expected array size 'MIN' not a number");
  }

  @Test
  public void failsWhenSecondExpectedArrayElementNotANumber() throws JSONException {
    doFailingMatchTest("{a:[8,MAX]}", JSON_SAMPLE__ARRAY,
        "a\\[\\]: invalid expectation: maximum expected array size 'MAX' not a number");
  }

  @Test
  public void succeedsWhenActualArrayContainsBetween2And6Elements() throws JSONException {
    JSONAssert.assertEquals("{a:[2,6]}", "{a:[7, 8, 9]}",
        new ArraySizeComparator(JSONCompareMode.LENIENT));
  }

  @Test
  public void succeedsWhenActualArrayContainsExactly3Elements() throws JSONException {
    JSONAssert.assertEquals("{a:[3]}", "{a:[7, 8, 9]}",
        new ArraySizeComparator(JSONCompareMode.LENIENT));
  }

  @Test
  public void succeedsWhenExactSizeExpected() throws JSONException {
    doTest("{a:[2]}", JSON_SAMPLE__ARRAY);
  }

  @Test
  public void succeedsWhenSizeIsMaximumOfExpectedRange() throws JSONException {
    doTest("{a:[1,2]}", JSON_SAMPLE__ARRAY);
  }

  @Test
  public void succeedsWhenSizeIsMinimumOfExpectedRange() throws JSONException {
    doTest("{a:[2,4]}", JSON_SAMPLE__ARRAY);
  }

  @Test
  public void succeedsWhenSizeWithinExpectedRange() throws JSONException {
    doTest("{a:[1,3]}", JSON_SAMPLE__ARRAY);
  }

  /*
   * Following tests are copied from ArraySizeComparator JavaDoc and are include to ensure code as
   * documented work as expected.
   */

  private void doFailingMatchTest(String expectedJSON, String actualJSON,
      String expectedMessagePattern) throws JSONException {
    try {
      doTest(expectedJSON, actualJSON);
    } catch (AssertionError e) {
      String failureMessage =
          MessageFormat.format("Exception message ''{0}'', does not match expected pattern ''{1}''",
              e.getMessage(), expectedMessagePattern);
      assertTrue(failureMessage, e.getMessage().matches(expectedMessagePattern));
      return;
    }
    fail("AssertionError not thrown");
  }

  private void doTest(String expectedJSON, String actualJSON) throws JSONException {
    JSONAssert.assertEquals(expectedJSON, actualJSON,
        new ArraySizeComparator(JSONCompareMode.STRICT_ORDER));
  }

}
