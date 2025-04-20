/*
  SPDX-FileCopyrightText: © 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>
  SPDX-License-Identifier: LGPL-3.0-or-later

  Copyright 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (ArraySizeComparator.java) is part of pdfclown-common-build module in pdfClown Common
  project (this Program).

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
  SPDX-FileCopyrightText: © 2012-2022 Skyscreamer <https://github.com/skyscreamer>
  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.common.build.internal.jsonassert.comparator;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONException;
import java.text.MessageFormat;
import org.pdfclown.common.build.internal.jsonassert.JSONCompareMode;
import org.pdfclown.common.build.internal.jsonassert.JSONCompareResult;

/**
 * A JSONAssert array size comparator.
 *
 * <p>
 * Some typical usage idioms are listed below.
 * </p>
 *
 * <p>
 * Assuming JSON to be verified is held in String variable ARRAY_OF_JSONOBJECTS and contains:
 * </p>
 *
 * <code>{a:[7, 8, 9]}</code>
 *
 * <p>
 * then:
 * </p>
 *
 * <p>
 * To verify that array 'a' contains 3 elements:
 * </p>
 *
 * <code>
 * JSONAssert.assertEquals("{a:[3]}", ARRAY_OF_JSONOBJECTS, new ArraySizeComparator(JSONCompareMode.LENIENT));
 * </code>
 *
 * <p>
 * To verify that array 'a' contains between 2 and 6 elements:
 * </p>
 *
 * <code>
 * JSONAssert.assertEquals("{a:[2,6]}", ARRAY_OF_JSONOBJECTS, new ArraySizeComparator(JSONCompareMode.LENIENT));
 * </code>
 *
 * @author Duncan Mackinder
 *
 */
public class ArraySizeComparator extends DefaultComparator {
  /**
   * Create new ArraySizeComparator.
   *
   * @param mode
   *          comparison mode, has no impact on ArraySizeComparator but is used by instance of
   *          superclass DefaultComparator to control comparison of JSON items other than arrays.
   */
  public ArraySizeComparator(JSONCompareMode mode) {
    super(mode);
  }

  /**
   * Expected array should consist of either 1 or 2 integer values that define maximum and minimum
   * valid lengths of the actual array. If expected array contains a single integer value, then the
   * actual array must contain exactly that number of elements.
   */
  @Override
  public void compareJSONArray(String prefix, JSONArray expected, JSONArray actual,
      JSONCompareResult result) throws JSONException {
    String arrayPrefix = prefix + "[]";
    if (expected.length() < 1 || expected.length() > 2) {
      result.fail(MessageFormat.format(
          "{0}: invalid expectation: expected array should contain either 1 or 2 elements but contains {1} elements",
          arrayPrefix, expected.length()));
      return;
    }
    if (!(expected.get(0) instanceof Number)) {
      result.fail(MessageFormat.format(
          "{0}: invalid expectation: {1}expected array size ''{2}'' not a number", arrayPrefix,
          (expected.length() == 1 ? "" : "minimum "), expected.get(0)));
      return;
    }
    if ((expected.length() == 2 && !(expected.get(1) instanceof Number))) {
      result.fail(MessageFormat.format(
          "{0}: invalid expectation: maximum expected array size ''{1}'' not a number", arrayPrefix,
          expected.get(1)));
      return;
    }
    int minExpectedLength = expected.getInt(0);
    if (minExpectedLength < 0) {
      result.fail(MessageFormat.format(
          "{0}: invalid expectation: minimum expected array size ''{1}'' negative", arrayPrefix,
          minExpectedLength));
      return;
    }
    int maxExpectedLength = expected.length() == 2 ? expected.getInt(1) : minExpectedLength;
    if (maxExpectedLength < minExpectedLength) {
      result.fail(MessageFormat.format(
          "{0}: invalid expectation: maximum expected array size ''{1}'' less than minimum expected array size ''{2}''",
          arrayPrefix, maxExpectedLength, minExpectedLength));
      return;
    }
    if (actual.length() < minExpectedLength || actual.length() > maxExpectedLength) {
      result.fail(arrayPrefix,
          MessageFormat.format("array size of {0}{1} elements", minExpectedLength,
              (expected.length() == 2 ? (" to " + maxExpectedLength) : "")),
          MessageFormat.format("{0} elements", actual.length()));
    }
  }
}
