/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  This file (JSONComparator.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: © 2012-2022 Skyscreamer

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.common.build.internal.jsonassert.comparator;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;
import org.pdfclown.common.build.internal.jsonassert.JSONCompareResult;

/**
 * Interface for comparison handler.
 *
 * @author <a href="mailto:aiveeen@gmail.com">Ivan Zaytsev</a> 2013-01-04
 */
public interface JSONComparator {
  /**
   * Compares two {@link JSONArray}s and returns the result of the comparison in a
   * {@link JSONCompareResult} object.
   *
   * @param expected
   *          the expected JSON array
   * @param actual
   *          the actual JSON array
   * @return the result of the comparison
   * @throws JSONException
   *           JSON parsing error
   */
  JSONCompareResult compareJSON(JSONArray expected, JSONArray actual) throws JSONException;

  /**
   * Compares two {@link JSONObject}s and returns the result of the comparison in a
   * {@link JSONCompareResult} object.
   *
   * @param expected
   *          the expected JSON object
   * @param actual
   *          the actual JSON object
   * @return the result of the comparison
   * @throws JSONException
   *           JSON parsing error
   */
  JSONCompareResult compareJSON(JSONObject expected, JSONObject actual) throws JSONException;

  /**
   * Compares two {@link JSONObject}s on the provided path represented by {@code prefix} and updates
   * the result of the comparison in the {@code result} {@link JSONCompareResult} object.
   *
   * @param prefix
   *          the path in the json where the comparison happens
   * @param expected
   *          the expected JSON object
   * @param actual
   *          the actual JSON object
   * @param result
   *          stores the actual state of the comparison result
   * @throws JSONException
   *           JSON parsing error
   */
  void compareJSON(String prefix, JSONObject expected, JSONObject actual, JSONCompareResult result)
      throws JSONException;

  /**
   * Compares two {@link JSONArray}s on the provided path represented by {@code prefix} and updates
   * the result of the comparison in the {@code result} {@link JSONCompareResult} object.
   *
   * @param prefix
   *          the path in the json where the comparison happens
   * @param expected
   *          the expected JSON array
   * @param actual
   *          the actual JSON array
   * @param result
   *          stores the actual state of the comparison result
   * @throws JSONException
   *           JSON parsing error
   */
  void compareJSONArray(String prefix, JSONArray expected, JSONArray actual,
      JSONCompareResult result) throws JSONException;

  /**
   * Compares two {@link Object}s on the provided path represented by {@code prefix} and updates the
   * result of the comparison in the {@code result} {@link JSONCompareResult} object.
   *
   * @param prefix
   *          the path in the json where the comparison happens
   * @param expectedValue
   *          the expected value
   * @param actualValue
   *          the actual value
   * @param result
   *          stores the actual state of the comparison result
   * @throws JSONException
   *           JSON parsing error
   */
  void compareValues(String prefix, Object expectedValue, Object actualValue,
      JSONCompareResult result) throws JSONException;
}
