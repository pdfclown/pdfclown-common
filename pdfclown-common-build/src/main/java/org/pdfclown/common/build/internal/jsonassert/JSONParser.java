/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  This file (JSONParser.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

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
import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;
import com.github.openjson.JSONString;

/**
 * Simple JSON parsing utility.
 */
public class JSONParser {
  // regular expression to match a number in JSON format.  see http://www.json.org/fatfree.html.
  // "A number can be represented as integer, real, or floating point. JSON does not support octal or hex
  // ... [or] NaN or Infinity".
  private static final String NUMBER_REGEX = "-?(?:0|[1-9]\\d*)(?:\\.\\d+)?(?:[eE][+-]?\\d+)?";

  /**
   * Takes a JSON string and returns either a {@link org.json.JSONObject} or
   * {@link org.json.JSONArray}, depending on whether the string represents an object or an array.
   *
   * @param s
   *          Raw JSON string to be parsed
   * @return JSONObject or JSONArray
   * @throws JSONException
   *           JSON parsing error
   */
  public static Object parseJSON(final String s) throws JSONException {
    if (s.trim().startsWith("{"))
      return new JSONObject(s);
    else if (s.trim().startsWith("["))
      return new JSONArray(s);
    else if (s.trim().startsWith("\"") || s.trim().matches(NUMBER_REGEX))
      return new JSONString() {
        @Override
        public String toJSONString() {
          return s;
        }
      };
    throw new JSONException("Unparsable JSON string: " + s);
  }

  private JSONParser() {
  }
}
