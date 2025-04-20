/*
  SPDX-FileCopyrightText: © 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>
  SPDX-License-Identifier: LGPL-3.0-or-later

  Copyright 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (ArrayValueMatcher.java) is part of pdfclown-common-build module in pdfClown Common
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
package org.pdfclown.common.build.internal.jsonassert;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONException;
import java.text.MessageFormat;
import org.pdfclown.common.build.internal.jsonassert.comparator.JSONComparator;

/**
 * <p>
 * A value matcher for arrays. This operates like STRICT_ORDER array match, however if expected
 * array has less elements than actual array the matching process loops through the expected array
 * to get expected elements for the additional actual elements. In general the expected array will
 * contain a single element which is matched against each actual array element in turn. This allows
 * simple verification of constant array element components and coupled with
 * RegularExpressionValueMatcher can be used to match specific array element components against a
 * regular expression pattern. As a convenience to reduce syntactic complexity of expected string,
 * if the expected object is not an array, a one element expected array is created containing
 * whatever is provided as the expected value.
 * </p>
 *
 * <p>
 * Some examples of typical usage idioms listed below.
 * </p>
 *
 * <p>
 * Assuming JSON to be verified is held in String variable ARRAY_OF_JSONOBJECTS and contains:
 * </p>
 *
 * <pre>{@code
 * {a:[{background:white, id:1, type:row},
 *     {background:grey,  id:2, type:row},
 *     {background:white, id:3, type:row},
 *     {background:grey,  id:4, type:row}]}
 * }</pre>
 *
 * <p>
 * then:
 * </p>
 *
 * <p>
 * To verify that the 'id' attribute of first element of array 'a' is '1':
 * </p>
 *
 * <pre>{@code
 * JSONComparator comparator = new DefaultComparator(JSONCompareMode.LENIENT);
 * Customization customization = new Customization("a", new ArrayValueMatcher<Object>(comparator, 0));
 * JSONAssert.assertEquals("{a:[{id:1}]}", ARRAY_OF_JSONOBJECTS,
 *     new CustomComparator(JSONCompareMode.LENIENT, customization));
 * }</pre>
 *
 * <p>
 * To simplify complexity of expected JSON string, the value <code>"a:[{id:1}]}"</code> may be
 * replaced by <code>"a:{id:1}}"</code>
 * </p>
 *
 * <p>
 * To verify that the 'type' attribute of second and third elements of array 'a' is 'row':
 * </p>
 *
 * <pre>{@code
 * JSONComparator comparator = new DefaultComparator(JSONCompareMode.LENIENT);
 * Customization customization = new Customization("a", new ArrayValueMatcher<Object>(comparator, 1, 2));
 * JSONAssert.assertEquals("{a:[{type:row}]}", ARRAY_OF_JSONOBJECTS,
 *     new CustomComparator(JSONCompareMode.LENIENT, customization));
 * }</pre>
 *
 * <p>
 * To verify that the 'type' attribute of every element of array 'a' is 'row':
 * </p>
 *
 * <pre>{@code
 * JSONComparator comparator = new DefaultComparator(JSONCompareMode.LENIENT);
 * Customization customization = new Customization("a", new ArrayValueMatcher<Object>(comparator));
 * JSONAssert.assertEquals("{a:[{type:row}]}", ARRAY_OF_JSONOBJECTS,
 *     new CustomComparator(JSONCompareMode.LENIENT, customization));
 * }</pre>
 *
 * <p>
 * To verify that the 'id' attribute of every element of array 'a' matches regular expression '\d+'.
 * This requires a custom comparator to specify regular expression to be used to validate each array
 * element, hence the array of Customization instances:
 * </p>
 *
 * <pre>{@code
 * // get length of array we will verify
 * int aLength = ((JSONArray)((JSONObject)JSONParser.parseJSON(ARRAY_OF_JSONOBJECTS)).get("a")).length();
 * // create array of customizations one for each array element
 * RegularExpressionValueMatcher<Object> regExValueMatcher =
 *     new RegularExpressionValueMatcher<Object>("\\d+");  // matches one or more digits
 * Customization[] customizations = new Customization[aLength];
 * for (int i=0; i<aLength; i++) {
 *     String contextPath = "a["+i+"].id";
 *     customizations[i] = new Customization(contextPath, regExValueMatcher);
 * }
 * CustomComparator regExComparator = new CustomComparator(JSONCompareMode.STRICT_ORDER, customizations);
 * ArrayValueMatcher<Object> regExArrayValueMatcher = new ArrayValueMatcher<Object>(regExComparator);
 * Customization regExArrayValueCustomization = new Customization("a", regExArrayValueMatcher);
 * CustomComparator regExCustomArrayValueComparator =
 *     new CustomComparator(JSONCompareMode.STRICT_ORDER, new Customization[] { regExArrayValueCustomization });
 * JSONAssert.assertEquals("{a:[{id:X}]}", ARRAY_OF_JSONOBJECTS, regExCustomArrayValueComparator);
 * }</pre>
 *
 * <p>
 * To verify that the 'background' attribute of every element of array 'a' alternates between
 * 'white' and 'grey' starting with first element 'background' being 'white':
 * </p>
 *
 * <pre>{@code
 * JSONComparator comparator = new DefaultComparator(JSONCompareMode.LENIENT);
 * Customization customization = new Customization("a", new ArrayValueMatcher<Object>(comparator));
 * JSONAssert.assertEquals("{a:[{background:white},{background:grey}]}", ARRAY_OF_JSONOBJECTS,
 *     new CustomComparator(JSONCompareMode.LENIENT, customization));
 * }</pre>
 *
 * <p>
 * Assuming JSON to be verified is held in String variable ARRAY_OF_JSONARRAYS and contains:
 * </p>
 *
 * <code>{a:[[6,7,8], [9,10,11], [12,13,14], [19,20,21,22]]}</code>
 *
 * <p>
 * then:
 * </p>
 *
 * <p>
 * To verify that the first three elements of JSON array 'a' are JSON arrays of length 3:
 * </p>
 *
 * <pre>{@code
 * JSONComparator comparator = new ArraySizeComparator(JSONCompareMode.STRICT_ORDER);
 * Customization customization = new Customization("a", new ArrayValueMatcher<Object>(comparator, 0, 2));
 * JSONAssert.assertEquals("{a:[[3]]}", ARRAY_OF_JSONARRAYS, new CustomComparator(JSONCompareMode.LENIENT, customization));
 * }</pre>
 *
 * <p>
 * NOTE: simplified expected JSON strings are not possible in this case as ArraySizeComparator does
 * not support them.
 * </p>
 *
 * <p>
 * To verify that the second elements of JSON array 'a' is a JSON array whose first element has the
 * value 9:
 * </p>
 *
 * <pre>{@code
 * JSONComparator innerComparator = new DefaultComparator(JSONCompareMode.LENIENT);
 * Customization innerCustomization = new Customization("a[1]", new ArrayValueMatcher<Object>(innerComparator, 0));
 * JSONComparator comparator = new CustomComparator(JSONCompareMode.LENIENT, innerCustomization);
 * Customization customization = new Customization("a", new ArrayValueMatcher<Object>(comparator, 1));
 * JSONAssert.assertEquals("{a:[[9]]}", ARRAY_OF_JSONARRAYS, new CustomComparator(JSONCompareMode.LENIENT, customization));
 * }</pre>
 *
 * <p>
 * To simplify complexity of expected JSON string, the value <code>"{a:[[9]]}"</code> may be
 * replaced by <code>"{a:[9]}"</code> or <code>"{a:9}"</code>
 * </p>
 *
 * @author Duncan Mackinder
 *
 */
public class ArrayValueMatcher<T> implements LocationAwareValueMatcher<T> {
  private final JSONComparator comparator;
  private final int from;
  private final int to;

  /**
   * Create ArrayValueMatcher to match every element in actual array against elements taken in
   * sequence from expected array, repeating from start of expected array if necessary.
   *
   * @param comparator
   *          comparator to use to compare elements
   */
  public ArrayValueMatcher(JSONComparator comparator) {
    this(comparator, 0, Integer.MAX_VALUE);
  }

  /**
   * Create ArrayValueMatcher to match specified element in actual array against first element of
   * expected array.
   *
   * @param comparator
   *          comparator to use to compare elements
   * @param index
   *          index of the array element to be compared
   */
  public ArrayValueMatcher(JSONComparator comparator, int index) {
    this(comparator, index, index);
  }

  /**
   * Create ArrayValueMatcher to match every element in specified range (inclusive) from actual
   * array against elements taken in sequence from expected array, repeating from start of expected
   * array if necessary.
   *
   * @param comparator
   *          comparator to use to compare elements
   * @param from
   *          first element in actual array to compared
   * @param to
   *          last element in actual array to compared
   */
  public ArrayValueMatcher(JSONComparator comparator, int from, int to) {
    assert comparator != null : "comparator null";
    assert from >= 0 : MessageFormat.format("from({0}) < 0", from);
    assert to >= from : MessageFormat.format("to({0}) < from({1})", to, from);
    this.comparator = comparator;
    this.from = from;
    this.to = to;
  }

  @Override
  public boolean equal(String prefix, T actual, T expected, JSONCompareResult result) {
    if (!(actual instanceof JSONArray))
      throw new IllegalArgumentException("ArrayValueMatcher applied to non-array actual value");
    try {
      JSONArray actualArray = (JSONArray) actual;
      JSONArray expectedArray = expected instanceof JSONArray ? (JSONArray) expected
          : new JSONArray(new Object[] { expected });
      int first = Math.max(0, from);
      int last = Math.min(actualArray.length() - 1, to);
      int expectedLen = expectedArray.length();
      for (int i = first; i <= last; i++) {
        String elementPrefix = MessageFormat.format("{0}[{1}]", prefix, i);
        Object actualElement = actualArray.get(i);
        Object expectedElement = expectedArray.get((i - first) % expectedLen);
        comparator.compareValues(elementPrefix, expectedElement, actualElement, result);
      }
      // any failures have already been passed to result, so return true
      return true;
    } catch (JSONException ex) {
      return false;
    }
  }

  @Override
  /*
   * NOTE: method defined as required by ValueMatcher interface but will never be called so defined
   * simply to indicate match failure
   */
  public boolean equal(T o1, T o2) {
    return false;
  }
}
