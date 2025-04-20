/*
  SPDX-FileCopyrightText: © 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>
  SPDX-License-Identifier: LGPL-3.0-or-later

  Copyright 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (DefaultComparator.java) is part of pdfclown-common-build module in pdfClown Common
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

import static org.pdfclown.common.build.internal.jsonassert.comparator.JSONCompareUtil.allJSONObjects;
import static org.pdfclown.common.build.internal.jsonassert.comparator.JSONCompareUtil.allSimpleValues;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;
import org.pdfclown.common.build.internal.jsonassert.JSONCompareMode;
import org.pdfclown.common.build.internal.jsonassert.JSONCompareResult;

/**
 * This class is the default json comparator implementation. Comparison is performed according to
 * {@link JSONCompareMode} that is passed as constructor's argument.
 */
public class DefaultComparator extends AbstractComparator {
  JSONCompareMode mode;

  public DefaultComparator(JSONCompareMode mode) {
    this.mode = mode;
  }

  @Override
  public void compareJSON(String prefix, JSONObject expected, JSONObject actual,
      JSONCompareResult result) throws JSONException {
    // Check that actual contains all the expected values
    checkJsonObjectKeysExpectedInActual(prefix, expected, actual, result);

    // If strict, check for vice-versa
    if (!mode.isExtensible()) {
      checkJsonObjectKeysActualInExpected(prefix, expected, actual, result);
    }
  }

  @Override
  public void compareJSONArray(String prefix, JSONArray expected, JSONArray actual,
      JSONCompareResult result) throws JSONException {
    if (expected.length() != actual.length()) {
      result.fail(
          prefix + "[]: Expected " + expected.length() + " values but got " + actual.length());
      return;
    } else if (expected.length() == 0)
      return; // Nothing to compare

    if (mode.hasStrictOrder()) {
      compareJSONArrayWithStrictOrder(prefix, expected, actual, result);
    } else if (allSimpleValues(expected)) {
      compareJSONArrayOfSimpleValues(prefix, expected, actual, result);
    } else if (allJSONObjects(expected)) {
      compareJSONArrayOfJsonObjects(prefix, expected, actual, result);
    } else {
      // An expensive last resort
      recursivelyCompareJSONArray(prefix, expected, actual, result);
    }
  }

  @Override
  @SuppressWarnings("null")
  public void compareValues(String prefix, Object expectedValue, Object actualValue,
      JSONCompareResult result) throws JSONException {
    if (expectedValue == actualValue)
      return;
    if ((expectedValue == null && actualValue != null)
        || (expectedValue != null && actualValue == null)) {
      result.fail(prefix, expectedValue, actualValue);
    }
    if (areNumbers(expectedValue, actualValue)) {
      if (areNotSameDoubles(expectedValue, actualValue)) {
        result.fail(prefix, expectedValue, actualValue);
      }
    } else if (expectedValue.getClass().isAssignableFrom(actualValue.getClass())) {
      if (expectedValue instanceof JSONArray) {
        compareJSONArray(prefix, (JSONArray) expectedValue, (JSONArray) actualValue, result);
      } else if (expectedValue instanceof JSONObject) {
        compareJSON(prefix, (JSONObject) expectedValue, (JSONObject) actualValue, result);
      } else if (!expectedValue.equals(actualValue)) {
        result.fail(prefix, expectedValue, actualValue);
      }
    } else {
      result.fail(prefix, expectedValue, actualValue);
    }
  }

  protected boolean areNotSameDoubles(Object expectedValue, Object actualValue) {
    return ((Number) expectedValue).doubleValue() != ((Number) actualValue).doubleValue();
  }

  protected boolean areNumbers(Object expectedValue, Object actualValue) {
    return expectedValue instanceof Number && actualValue instanceof Number;
  }
}
