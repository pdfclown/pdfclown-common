/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  This file (JSONArrayWithNullTest.java) is part of pdfclown-common-build module in pdfClown Common
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
import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;
import org.junit.Test;

public class JSONArrayWithNullTest {
  @Test
  public void testJSONArrayWithNullValue() throws JSONException {
    JSONArray jsonArray1 = getJSONArray1();
    JSONArray jsonArray2 = getJSONArray2();

    JSONAssert.assertEquals(jsonArray1, jsonArray2, true);
    JSONAssert.assertEquals(jsonArray1, jsonArray2, false);
  }

  @Test
  public void testJSONArrayWithNullValueAndJsonObject() throws JSONException {
    JSONArray jsonArray1 = getJSONArray1();
    JSONObject jsonObject1 = new JSONObject();
    jsonObject1.put("hey", "value");

    JSONArray jsonArray2 = getJSONArray2();
    JSONObject jsonObject2 = new JSONObject();
    jsonObject2.put("hey", "value");

    JSONAssert.assertEquals(jsonArray1, jsonArray2, true);
    JSONAssert.assertEquals(jsonArray1, jsonArray2, false);
  }

  private JSONArray getJSONArray1() {
    JSONArray jsonArray1 = new JSONArray();
    jsonArray1.put(1);
    jsonArray1.put(null);
    jsonArray1.put(3);
    jsonArray1.put(2);
    return jsonArray1;
  }

  private JSONArray getJSONArray2() {
    JSONArray jsonArray1 = new JSONArray();
    jsonArray1.put(1);
    jsonArray1.put(null);
    jsonArray1.put(3);
    jsonArray1.put(2);
    return jsonArray1;
  }
}
