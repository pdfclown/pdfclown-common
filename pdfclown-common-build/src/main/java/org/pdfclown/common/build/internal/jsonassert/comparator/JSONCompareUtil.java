/*
  SPDX-FileCopyrightText: © 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>
  SPDX-License-Identifier: LGPL-3.0-or-later

  Copyright 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (JSONCompareUtil.java) is part of pdfclown-common-build module in pdfClown Common
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
import com.github.openjson.JSONObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Utility class that contains Json manipulation methods.
 */
public final class JSONCompareUtil {
  private static Integer INTEGER_ONE = 1;

  /**
   * Returns whether all elements in {@code array} are {@link JSONArray} instances.
   *
   * @param array
   *          the array to inspect
   * @return true if all the elements in the given array are JSONArrays
   * @throws JSONException
   *           JSON parsing error
   */
  public static boolean allJSONArrays(JSONArray array) throws JSONException {
    for (int i = 0; i < array.length(); ++i) {
      if (!(array.get(i) instanceof JSONArray))
        return false;
    }
    return true;
  }

  /**
   * Returns whether all elements in {@code array} are {@link JSONObject} instances.
   *
   * @param array
   *          the array to inspect
   * @return true if all the elements in the given array are JSONObjects
   * @throws JSONException
   *           JSON parsing error
   */
  public static boolean allJSONObjects(JSONArray array) throws JSONException {
    for (int i = 0; i < array.length(); ++i) {
      if (!(array.get(i) instanceof JSONObject))
        return false;
    }
    return true;
  }

  /**
   * Returns whether all of the elements in the given array are simple values.
   *
   * @param array
   *          the JSON array to iterate through on
   * @return true if all the elements in {@code array} are simple values
   * @throws JSONException
   *           JSON parsing error
   * @see #isSimpleValue(Object)
   */
  public static boolean allSimpleValues(JSONArray array) throws JSONException {
    for (int i = 0; i < array.length(); ++i) {
      if (!array.isNull(i) && !isSimpleValue(array.get(i)))
        return false;
    }
    return true;
  }

  /**
   * Converts the provided {@link JSONArray} to a Map of {@link JSONObject}s where the key of each
   * object is the value at {@code uniqueKey} in each object.
   *
   * @param array
   *          the JSON array to convert
   * @param uniqueKey
   *          the key to map the JSON objects to
   * @return the map of {@link JSONObject}s from {@code array}
   * @throws JSONException
   *           JSON parsing error
   */
  public static Map<Object, JSONObject> arrayOfJsonObjectToMap(JSONArray array, String uniqueKey)
      throws JSONException {
    Map<Object, JSONObject> valueMap = new HashMap<>();
    for (int i = 0; i < array.length(); ++i) {
      JSONObject jsonObject = (JSONObject) array.get(i);
      Object id = jsonObject.get(uniqueKey);
      valueMap.put(id, jsonObject);
    }
    return valueMap;
  }

  /**
   * Searches for the unique key of the {@code expected} JSON array.
   *
   * @param expected
   *          the array to find the unique key of
   * @return the unique key if there's any, otherwise null
   * @throws JSONException
   *           JSON parsing error
   */
  public static String findUniqueKey(JSONArray expected) throws JSONException {
    // Find a unique key for the object (id, name, whatever)
    JSONObject o = (JSONObject) expected.get(0); // There's at least one at this point
    for (String candidate : getKeys(o)) {
      if (isUsableAsUniqueKey(candidate, expected))
        return candidate;
    }
    // No usable unique key :-(
    return null;
  }

  public static String formatUniqueKey(String key, String uniqueKey, Object value) {
    return key + "[" + uniqueKey + "=" + value + "]";
  }

  /**
   * Creates a cardinality map from {@code coll}.
   *
   * @param coll
   *          the collection of items to convert
   * @param <T>
   *          the type of elements in the input collection
   * @return the cardinality map
   */
  public static <T> Map<T, Integer> getCardinalityMap(final Collection<T> coll) {
    var count = new HashMap<T, Integer>();
    for (T item : coll) {
      Integer c = (count.get(item));
      if (c == null) {
        count.put(item, INTEGER_ONE);
      } else {
        count.put(item, c.intValue() + 1);
      }
    }
    return count;
  }

  /**
   * Collects all keys in {@code jsonObject}.
   *
   * @param jsonObject
   *          the {@link JSONObject} to get the keys of
   * @return the set of keys
   */
  public static Set<String> getKeys(JSONObject jsonObject) {
    Set<String> keys = new TreeSet<>();
    Iterator<?> iter = jsonObject.keys();
    while (iter.hasNext()) {
      keys.add((String) iter.next());
    }
    return keys;
  }

  /**
   * Returns the value present in the given index position. If null value is present, it will return
   * null
   *
   * @param jsonArray
   *          the JSON array to get value from
   * @param index
   *          index of object to retrieve
   * @return value at the given index position
   * @throws JSONException
   *           JSON parsing error
   */
  public static Object getObjectOrNull(JSONArray jsonArray, int index) throws JSONException {
    return jsonArray.isNull(index) ? null : jsonArray.get(index);
  }

  /**
   * Returns whether the given object is a simple value: not {@link JSONObject} and not
   * {@link JSONArray}.
   *
   * @param o
   *          the object to inspect
   * @return true if {@code o} is a simple value
   */
  public static boolean isSimpleValue(Object o) {
    return !(o instanceof JSONObject) && !(o instanceof JSONArray);
  }

  /**
   * <p>
   * Looks to see if candidate field is a possible unique key across a array of objects. Returns
   * true IFF:
   * </p>
   * <ol>
   * <li>array is an array of JSONObject
   * <li>candidate is a top-level field in each of of the objects in the array
   * <li>candidate is a simple value (not JSONObject or JSONArray)
   * <li>candidate is unique across all elements in the array
   * </ol>
   *
   * @param candidate
   *          is usable as a unique key if every element in the
   * @param array
   *          is a JSONObject having that key, and no two values are the same.
   * @return true if the candidate can work as a unique id across array
   * @throws JSONException
   *           JSON parsing error
   */
  public static boolean isUsableAsUniqueKey(String candidate, JSONArray array)
      throws JSONException {
    Set<Object> seenValues = new HashSet<>();
    for (int i = 0; i < array.length(); i++) {
      Object item = array.get(i);
      if (item instanceof JSONObject) {
        JSONObject o = (JSONObject) item;
        if (o.has(candidate)) {
          Object value = o.get(candidate);
          if (isSimpleValue(value) && !seenValues.contains(value)) {
            seenValues.add(value);
          } else
            return false;
        } else
          return false;
      } else
        return false;
    }
    return true;
  }

  /**
   * Converts the given {@link JSONArray} to a list of {@link Object}s.
   *
   * @param expected
   *          the JSON array to convert
   * @return the list of objects from the {@code expected} array
   * @throws JSONException
   *           JSON parsing error
   */
  public static List<Object> jsonArrayToList(JSONArray expected) throws JSONException {
    List<Object> jsonObjects = new ArrayList<>(expected.length());
    for (int i = 0; i < expected.length(); ++i) {
      jsonObjects.add(getObjectOrNull(expected, i));
    }
    return jsonObjects;
  }

  public static String qualify(String prefix, String key) {
    return "".equals(prefix) ? key : prefix + "." + key;
  }

  private JSONCompareUtil() {
  }
}
