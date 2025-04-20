/*
  SPDX-FileCopyrightText: © 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>
  SPDX-License-Identifier: LGPL-3.0-or-later

  Copyright 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (AbstractComparator.java) is part of pdfclown-common-build module in pdfClown Common
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

import static org.pdfclown.common.build.internal.jsonassert.comparator.JSONCompareUtil.arrayOfJsonObjectToMap;
import static org.pdfclown.common.build.internal.jsonassert.comparator.JSONCompareUtil.findUniqueKey;
import static org.pdfclown.common.build.internal.jsonassert.comparator.JSONCompareUtil.formatUniqueKey;
import static org.pdfclown.common.build.internal.jsonassert.comparator.JSONCompareUtil.getKeys;
import static org.pdfclown.common.build.internal.jsonassert.comparator.JSONCompareUtil.isUsableAsUniqueKey;
import static org.pdfclown.common.build.internal.jsonassert.comparator.JSONCompareUtil.jsonArrayToList;
import static org.pdfclown.common.build.internal.jsonassert.comparator.JSONCompareUtil.qualify;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.pdfclown.common.build.internal.jsonassert.JSONCompareResult;

/**
 * This class provides a skeletal implementation of the {@link JSONComparator} interface, to
 * minimize the effort required to implement this interface.
 */
public abstract class AbstractComparator implements JSONComparator {
  /**
   * Compares JSONArray provided to the expected JSONArray, and returns the results of the
   * comparison.
   *
   * @param expected
   *          Expected JSONArray
   * @param actual
   *          JSONArray to compare
   * @throws JSONException
   *           JSON parsing error
   */
  @Override
  public final JSONCompareResult compareJSON(JSONArray expected, JSONArray actual)
      throws JSONException {
    JSONCompareResult result = new JSONCompareResult();
    compareJSONArray("", expected, actual, result);
    return result;
  }

  /**
   * Compares JSONObject provided to the expected JSONObject, and returns the results of the
   * comparison.
   *
   * @param expected
   *          Expected JSONObject
   * @param actual
   *          JSONObject to compare
   * @throws JSONException
   *           JSON parsing error
   */
  @Override
  public final JSONCompareResult compareJSON(JSONObject expected, JSONObject actual)
      throws JSONException {
    JSONCompareResult result = new JSONCompareResult();
    compareJSON("", expected, actual, result);
    return result;
  }

  protected void checkJsonObjectKeysActualInExpected(String prefix, JSONObject expected,
      JSONObject actual, JSONCompareResult result) {
    Set<String> actualKeys = getKeys(actual);
    for (String key : actualKeys) {
      if (!expected.has(key)) {
        result.unexpected(prefix, key);
      }
    }
  }

  protected void checkJsonObjectKeysExpectedInActual(String prefix, JSONObject expected,
      JSONObject actual, JSONCompareResult result) throws JSONException {
    Set<String> expectedKeys = getKeys(expected);
    for (String key : expectedKeys) {
      Object expectedValue = expected.get(key);
      if (actual.has(key)) {
        Object actualValue = actual.get(key);
        compareValues(qualify(prefix, key), expectedValue, actualValue, result);
      } else {
        result.missing(prefix, key);
      }
    }
  }

  protected void compareJSONArrayOfJsonObjects(String key, JSONArray expected, JSONArray actual,
      JSONCompareResult result) throws JSONException {
    String uniqueKey = findUniqueKey(expected);
    if (uniqueKey == null || !isUsableAsUniqueKey(uniqueKey, actual)) {
      // An expensive last resort
      recursivelyCompareJSONArray(key, expected, actual, result);
      return;
    }
    Map<Object, JSONObject> expectedValueMap = arrayOfJsonObjectToMap(expected, uniqueKey);
    Map<Object, JSONObject> actualValueMap = arrayOfJsonObjectToMap(actual, uniqueKey);
    for (Object id : expectedValueMap.keySet()) {
      if (!actualValueMap.containsKey(id)) {
        result.missing(formatUniqueKey(key, uniqueKey, id), expectedValueMap.get(id));
        continue;
      }
      JSONObject expectedValue = expectedValueMap.get(id);
      JSONObject actualValue = actualValueMap.get(id);
      compareValues(formatUniqueKey(key, uniqueKey, id), expectedValue, actualValue, result);
    }
    for (Object id : actualValueMap.keySet()) {
      if (!expectedValueMap.containsKey(id)) {
        result.unexpected(formatUniqueKey(key, uniqueKey, id), actualValueMap.get(id));
      }
    }
  }

  @SuppressWarnings("null")
  protected void compareJSONArrayOfSimpleValues(String key, JSONArray expected, JSONArray actual,
      JSONCompareResult result) throws JSONException {
    Map<Object, Integer> expectedCount =
        JSONCompareUtil.getCardinalityMap(jsonArrayToList(expected));
    Map<Object, Integer> actualCount = JSONCompareUtil.getCardinalityMap(jsonArrayToList(actual));
    for (Object o : expectedCount.keySet()) {
      if (!actualCount.containsKey(o)) {
        result.missing(key + "[]", o);
      } else if (!actualCount.get(o).equals(expectedCount.get(o))) {
        result.fail(key + "[]: Expected " + expectedCount.get(o) + " occurrence(s) of " + o
            + " but got " + actualCount.get(o) + " occurrence(s)");
      }
    }
    for (Object o : actualCount.keySet()) {
      if (!expectedCount.containsKey(o)) {
        result.unexpected(key + "[]", o);
      }
    }
  }

  protected void compareJSONArrayWithStrictOrder(String key, JSONArray expected, JSONArray actual,
      JSONCompareResult result) throws JSONException {
    for (int i = 0; i < expected.length(); ++i) {
      Object expectedValue = JSONCompareUtil.getObjectOrNull(expected, i);
      Object actualValue = JSONCompareUtil.getObjectOrNull(actual, i);
      compareValues(key + "[" + i + "]", expectedValue, actualValue, result);
    }
  }

  // This is expensive (O(n^2) -- yuck), but may be the only resort for some cases with loose array ordering, and no
  // easy way to uniquely identify each element.
  // This is expensive (O(n^2) -- yuck), but may be the only resort for some cases with loose array ordering, and no
  // easy way to uniquely identify each element.
  @SuppressWarnings("null")
  protected void recursivelyCompareJSONArray(String key, JSONArray expected, JSONArray actual,
      JSONCompareResult result) throws JSONException {
    Set<Integer> matched = new HashSet<>();
    for (int i = 0; i < expected.length(); ++i) {
      Object expectedElement = JSONCompareUtil.getObjectOrNull(expected, i);
      boolean matchFound = false;
      for (int j = 0; j < actual.length(); ++j) {
        Object actualElement = JSONCompareUtil.getObjectOrNull(actual, j);
        if (expectedElement == actualElement) {
          matchFound = true;
          break;
        }
        if ((expectedElement == null && actualElement != null)
            || (expectedElement != null && actualElement == null)) {
          continue;
        }
        if (matched.contains(j) || !actualElement.getClass().equals(expectedElement.getClass())) {
          continue;
        }
        if (expectedElement instanceof JSONObject) {
          if (compareJSON((JSONObject) expectedElement, (JSONObject) actualElement).passed()) {
            matched.add(j);
            matchFound = true;
            break;
          }
        } else if (expectedElement instanceof JSONArray) {
          if (compareJSON((JSONArray) expectedElement, (JSONArray) actualElement).passed()) {
            matched.add(j);
            matchFound = true;
            break;
          }
        } else if (expectedElement.equals(actualElement)) {
          matched.add(j);
          matchFound = true;
          break;
        }
      }
      if (!matchFound) {
        result.fail(key + "[" + i + "] Could not find match for element " + expectedElement);
        return;
      }
    }
  }
}
