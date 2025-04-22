/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (JSONCompare.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common> (this Program).

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
package org.pdfclown.common.build.internal.jsonassert;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;
import com.github.openjson.JSONString;
import org.pdfclown.common.build.internal.jsonassert.comparator.DefaultComparator;
import org.pdfclown.common.build.internal.jsonassert.comparator.JSONComparator;

/**
 * Provides API to compare two JSON entities. This is the backend to {@link JSONAssert}, but it can
 * be programmed against directly to access the functionality. (eg, to make something that works
 * with a non-JUnit test framework)
 */
public final class JSONCompare {
  /**
   * Compares {@link JSONString} provided to the expected {@code JSONString}, checking that the
   * {@link org.json.JSONString#toJSONString()} are equal.
   *
   * @param expected
   *          Expected {@code JSONstring}
   * @param actual
   *          {@code JSONstring} to compare
   * @return result of the comparison
   */
  public static JSONCompareResult compareJson(final JSONString expected, final JSONString actual) {
    final JSONCompareResult result = new JSONCompareResult();
    final String expectedJson = expected.toJSONString();
    final String actualJson = actual.toJSONString();
    if (!expectedJson.equals(actualJson)) {
      result.fail("");
    }
    return result;
  }

  /**
   * Compares JSON object provided to the expected JSON object using provided comparator, and
   * returns the results of the comparison.
   *
   * @param expected
   *          expected json array
   * @param actual
   *          actual json array
   * @param comparator
   *          comparator to use
   * @return result of the comparison
   * @throws JSONException
   *           JSON parsing error
   */
  public static JSONCompareResult compareJSON(JSONArray expected, JSONArray actual,
      JSONComparator comparator) throws JSONException {
    return comparator.compareJSON(expected, actual);
  }

  /**
   * Compares JSONArray provided to the expected JSONArray, and returns the results of the
   * comparison.
   *
   * @param expected
   *          Expected JSONArray
   * @param actual
   *          JSONArray to compare
   * @param mode
   *          Defines comparison behavior
   * @return result of the comparison
   * @throws JSONException
   *           JSON parsing error
   */
  public static JSONCompareResult compareJSON(JSONArray expected, JSONArray actual,
      JSONCompareMode mode) throws JSONException {
    return compareJSON(expected, actual, getComparatorForMode(mode));
  }

  /**
   * Compares JSON object provided to the expected JSON object using provided comparator, and
   * returns the results of the comparison.
   *
   * @param expected
   *          expected json object
   * @param actual
   *          actual json object
   * @param comparator
   *          comparator to use
   * @return result of the comparison
   * @throws JSONException
   *           JSON parsing error
   */
  public static JSONCompareResult compareJSON(JSONObject expected, JSONObject actual,
      JSONComparator comparator) throws JSONException {
    return comparator.compareJSON(expected, actual);
  }

  /**
   * Compares JSONObject provided to the expected JSONObject, and returns the results of the
   * comparison.
   *
   * @param expected
   *          Expected JSONObject
   * @param actual
   *          JSONObject to compare
   * @param mode
   *          Defines comparison behavior
   * @return result of the comparison
   * @throws JSONException
   *           JSON parsing error
   */
  public static JSONCompareResult compareJSON(JSONObject expected, JSONObject actual,
      JSONCompareMode mode) throws JSONException {
    return compareJSON(expected, actual, getComparatorForMode(mode));
  }

  /**
   * Compares JSON string provided to the expected JSON string using provided comparator, and
   * returns the results of the comparison.
   *
   * @param expectedStr
   *          Expected JSON string
   * @param actualStr
   *          JSON string to compare
   * @param comparator
   *          Comparator to use
   * @return result of the comparison
   * @throws JSONException
   *           JSON parsing error
   * @throws IllegalArgumentException
   *           when type of expectedStr doesn't match the type of actualStr
   */
  public static JSONCompareResult compareJSON(String expectedStr, String actualStr,
      JSONComparator comparator) throws JSONException {
    Object expected = JSONParser.parseJSON(expectedStr);
    Object actual = JSONParser.parseJSON(actualStr);
    if ((expected instanceof JSONObject) && (actual instanceof JSONObject))
      return compareJSON((JSONObject) expected, (JSONObject) actual, comparator);
    else if ((expected instanceof JSONArray) && (actual instanceof JSONArray))
      return compareJSON((JSONArray) expected, (JSONArray) actual, comparator);
    else if (expected instanceof JSONString && actual instanceof JSONString)
      return compareJson((JSONString) expected, (JSONString) actual);
    else if (expected instanceof JSONObject)
      return new JSONCompareResult().fail("", expected, actual);
    else
      return new JSONCompareResult().fail("", expected, actual);
  }

  /**
   * Compares JSON string provided to the expected JSON string, and returns the results of the
   * comparison.
   *
   * @param expectedStr
   *          Expected JSON string
   * @param actualStr
   *          JSON string to compare
   * @param mode
   *          Defines comparison behavior
   * @return result of the comparison
   * @throws JSONException
   *           JSON parsing error
   */
  public static JSONCompareResult compareJSON(String expectedStr, String actualStr,
      JSONCompareMode mode) throws JSONException {
    return compareJSON(expectedStr, actualStr, getComparatorForMode(mode));
  }

  private static JSONComparator getComparatorForMode(JSONCompareMode mode) {
    return new DefaultComparator(mode);
  }

  private JSONCompare() {
  }
}
