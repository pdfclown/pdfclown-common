/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  This file (JSONCompareResult.java) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: © 2012-2022 Skyscreamer

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.common.build.internal.jsonassert;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Bean for holding results from JSONCompare.
 */
public class JSONCompareResult {
  private static String describe(Object value) {
    if (value instanceof JSONArray)
      return "a JSON array";
    else if (value instanceof JSONObject)
      return "a JSON object";
    else
      return value.toString();
  }

  private boolean _success;
  private StringBuilder _message;
  private String _field;
  private Object _expected;
  private Object _actual;

  private final List<FieldComparisonFailure> _fieldFailures = new ArrayList<>();
  private final List<FieldComparisonFailure> _fieldMissing = new ArrayList<>();
  private final List<FieldComparisonFailure> _fieldUnexpected = new ArrayList<>();

  /**
   * Default constructor.
   */
  public JSONCompareResult() {
    this(true, null);
  }

  private JSONCompareResult(boolean success, String message) {
    _success = success;
    _message = new StringBuilder(message == null ? "" : message);
  }

  public void fail(String message) {
    _success = false;
    if (_message.length() == 0) {
      _message.append(message);
    } else {
      _message.append(" ; ").append(message);
    }
  }

  /**
   * Identify that the comparison failed
   *
   * @param field
   *          Which field failed
   * @param expected
   *          Expected result
   * @param actual
   *          Actual result
   * @return result of comparision
   */
  public JSONCompareResult fail(String field, Object expected, Object actual) {
    _fieldFailures.add(new FieldComparisonFailure(field, expected, actual));
    this._field = field;
    this._expected = expected;
    this._actual = actual;
    fail(formatFailureMessage(field, expected, actual));
    return this;
  }

  /**
   * Identify that the comparison failed
   *
   * @param field
   *          Which field failed
   * @param exception
   *          exception containing details of match failure
   * @return result of comparision
   */
  public JSONCompareResult fail(String field, ValueMatcherException exception) {
    fail(field + ": " + exception.getMessage(), exception.getExpected(), exception.getActual());
    return this;
  }

  /**
   * Did the comparison fail?
   *
   * @return True if it failed
   */
  public boolean failed() {
    return !_success;
  }

  /**
   * Actual field value
   *
   * @return a {@code JSONObject}, {@code JSONArray} or other {@code Object} instance, or
   *         {@code null} if the comparison did not fail on a particular field
   * @deprecated Superseded by {@link #getFieldFailures()}
   */
  @Deprecated
  public Object getActual() {
    return _actual;
  }

  /**
   * Expected field value
   *
   * @return a {@code JSONObject}, {@code JSONArray} or other {@code Object} instance, or
   *         {@code null} if the comparison did not fail on a particular field
   * @deprecated Superseded by {@link #getFieldFailures()}
   */
  @Deprecated
  public Object getExpected() {
    return _expected;
  }

  /**
   * Dot-separated path the the field that failed comparison
   *
   * @return a {@code String} instance, or {@code null} if the comparison did not fail on a
   *         particular field
   * @deprecated Superseded by {@link #getFieldFailures()}
   */
  @Deprecated
  public String getField() {
    return _field;
  }

  /**
   * Get the list of failures on field comparisons
   *
   * @return list of comparsion failures
   */
  public List<FieldComparisonFailure> getFieldFailures() {
    return Collections.unmodifiableList(_fieldFailures);
  }

  /**
   * Get the list of missed on field comparisons
   *
   * @return list of comparsion failures
   */
  public List<FieldComparisonFailure> getFieldMissing() {
    return Collections.unmodifiableList(_fieldMissing);
  }

  /**
   * Get the list of failures on field comparisons
   *
   * @return list of comparsion failures
   */
  public List<FieldComparisonFailure> getFieldUnexpected() {
    return Collections.unmodifiableList(_fieldUnexpected);
  }

  /**
   * Result message
   *
   * @return String explaining why if the comparison failed
   */
  public String getMessage() {
    return _message.toString();
  }

  /**
   * Check if comparison failed on any particular fields
   *
   * @return true if there are field failures
   */
  public boolean isFailureOnField() {
    return !_fieldFailures.isEmpty();
  }

  /**
   * Check if comparison failed with missing on any particular fields
   *
   * @return true if an expected field is missing
   */
  public boolean isMissingOnField() {
    return !_fieldMissing.isEmpty();
  }

  /**
   * Check if comparison failed with unexpected on any particular fields
   *
   * @return true if an unexpected field is in the result
   */
  public boolean isUnexpectedOnField() {
    return !_fieldUnexpected.isEmpty();
  }

  /**
   * Identify the missing field
   *
   * @param field
   *          missing field
   * @param expected
   *          expected result
   * @return result of comparison
   */
  public JSONCompareResult missing(String field, Object expected) {
    _fieldMissing.add(new FieldComparisonFailure(field, expected, null));
    fail(formatMissing(field, expected));
    return this;
  }

  /**
   * Did the comparison pass?
   *
   * @return True if it passed
   */
  public boolean passed() {
    return _success;
  }

  @Override
  public String toString() {
    return _message.toString();
  }

  /**
   * Identify unexpected field
   *
   * @param field
   *          unexpected field
   * @param actual
   *          actual result
   * @return result of comparison
   */
  public JSONCompareResult unexpected(String field, Object actual) {
    _fieldUnexpected.add(new FieldComparisonFailure(field, null, actual));
    fail(formatUnexpected(field, actual));
    return this;
  }

  private String formatFailureMessage(String field, Object expected, Object actual) {
    return field + "\nExpected: " + describe(expected) + "\n     got: " + describe(actual) + "\n";
  }

  private String formatMissing(String field, Object expected) {
    return field + "\nExpected: " + describe(expected) + "\n     but none found\n";
  }

  private String formatUnexpected(String field, Object actual) {
    return field + "\nUnexpected: " + describe(actual) + "\n";
  }
}
