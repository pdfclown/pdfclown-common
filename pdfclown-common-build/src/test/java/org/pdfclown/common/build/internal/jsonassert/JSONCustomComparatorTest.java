/*
  SPDX-FileCopyrightText: © 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>
  SPDX-License-Identifier: LGPL-3.0-or-later

  Copyright 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (JSONCustomComparatorTest.java) is part of pdfclown-common-build module in pdfClown
  Common project (this Program).

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.pdfclown.common.build.internal.jsonassert.JSONCompare.compareJSON;

import com.github.openjson.JSONException;
import org.junit.Test;
import org.pdfclown.common.build.internal.jsonassert.comparator.CustomComparator;
import org.pdfclown.common.build.internal.jsonassert.comparator.JSONComparator;

public class JSONCustomComparatorTest {
  String actual = "{\"first\":\"actual\", \"second\":1}";
  String expected = "{\"first\":\"expected\", \"second\":1}";

  String deepActual = "{\n"
      + "    \"outer\":\n"
      + "    {\n"
      + "        \"inner\":\n"
      + "        {\n"
      + "            \"value\": \"actual\",\n"
      + "            \"otherValue\": \"foo\"\n"
      + "        }\n"
      + "    }\n"
      + "}";
  String deepExpected = "{\n"
      + "    \"outer\":\n"
      + "    {\n"
      + "        \"inner\":\n"
      + "        {\n"
      + "            \"value\": \"expected\",\n"
      + "            \"otherValue\": \"foo\"\n"
      + "        }\n"
      + "    }\n"
      + "}";

  String simpleWildcardActual = "{\n"
      + "  \"foo\": {\n"
      + "    \"bar1\": {\n"
      + "      \"baz\": \"actual\"\n"
      + "    },\n"
      + "    \"bar2\": {\n"
      + "      \"baz\": \"actual\"\n"
      + "    }\n"
      + "  }\n"
      + "}";
  String simpleWildcardExpected = "{\n"
      + "  \"foo\": {\n"
      + "    \"bar1\": {\n"
      + "      \"baz\": \"expected\"\n"
      + "    },\n"
      + "    \"bar2\": {\n"
      + "      \"baz\": \"expected\"\n"
      + "    }\n"
      + "  }\n"
      + "}";

  String deepWildcardActual = "{\n"
      + "  \"root\": {\n"
      + "    \"baz\": \"actual\",\n"
      + "    \"foo\": {\n"
      + "      \"baz\": \"actual\",\n"
      + "      \"bar\": {\n"
      + "        \"baz\": \"actual\"\n"
      + "      }\n"
      + "    }\n"
      + "  }\n"
      + "}";
  String deepWildcardExpected = "{\n"
      + "  \"root\": {\n"
      + "    \"baz\": \"expected\",\n"
      + "    \"foo\": {\n"
      + "      \"baz\": \"expected\",\n"
      + "      \"bar\": {\n"
      + "        \"baz\": \"expected\"\n"
      + "      }\n"
      + "    }\n"
      + "  }\n"
      + "}";

  String rootDeepWildcardActual = "{\n"
      + "  \"baz\": \"actual\",\n"
      + "  \"root\": {\n"
      + "    \"baz\": \"actual\",\n"
      + "    \"foo\": {\n"
      + "      \"baz\": \"actual\",\n"
      + "      \"bar\": {\n"
      + "        \"baz\": \"actual\"\n"
      + "      }\n"
      + "    }\n"
      + "  }\n"
      + "}";
  String rootDeepWildcardExpected = "{\n"
      + "  \"baz\": \"expected\",\n"
      + "  \"root\": {\n"
      + "    \"baz\": \"expected\",\n"
      + "    \"foo\": {\n"
      + "      \"baz\": \"expected\",\n"
      + "      \"bar\": {\n"
      + "        \"baz\": \"expected\"\n"
      + "      }\n"
      + "    }\n"
      + "  }\n"
      + "}";

  int comparatorCallCount = 0;
  ValueMatcher<Object> comparator = new ValueMatcher<>() {
    @Override
    public boolean equal(Object o1, Object o2) {
      comparatorCallCount++;
      return o1.toString().equals("actual") && o2.toString().equals("expected");
    }
  };

  @Test
  public void whenDeepPathMatchesCallCustomMatcher() throws JSONException {
    JSONComparator jsonCmp = new CustomComparator(JSONCompareMode.STRICT,
        new Customization("outer.inner.value", comparator));
    JSONCompareResult result = compareJSON(deepExpected, deepActual, jsonCmp);
    assertTrue(result.getMessage(), result.passed());
    assertEquals(1, comparatorCallCount);
  }

  @Test
  public void whenDeepWildcardPathMatchesCallCustomMatcher() throws JSONException {
    JSONComparator jsonCmp =
        new CustomComparator(JSONCompareMode.STRICT, new Customization("root.**.baz", comparator));
    JSONCompareResult result = compareJSON(deepWildcardExpected, deepWildcardActual, jsonCmp);
    assertTrue(result.getMessage(), result.passed());
    assertEquals(3, comparatorCallCount);
  }

  @Test
  public void whenPathMatchesInCustomizationThenCallCustomMatcher() throws JSONException {
    JSONComparator jsonCmp =
        new CustomComparator(JSONCompareMode.STRICT, new Customization("first", comparator));
    JSONCompareResult result = compareJSON(expected, actual, jsonCmp);
    assertTrue(result.getMessage(), result.passed());
    assertEquals(1, comparatorCallCount);
  }

  @Test
  public void whenRootDeepWildcardPathMatchesCallCustomMatcher() throws JSONException {
    JSONComparator jsonCmp =
        new CustomComparator(JSONCompareMode.STRICT, new Customization("**.baz", comparator));
    JSONCompareResult result =
        compareJSON(rootDeepWildcardExpected, rootDeepWildcardActual, jsonCmp);
    assertTrue(result.getMessage(), result.passed());
    assertEquals(4, comparatorCallCount);
  }

  @Test
  public void whenSimpleWildcardPathMatchesCallCustomMatcher() throws JSONException {
    JSONComparator jsonCmp =
        new CustomComparator(JSONCompareMode.STRICT, new Customization("foo.*.baz", comparator));
    JSONCompareResult result = compareJSON(simpleWildcardExpected, simpleWildcardActual, jsonCmp);
    assertTrue(result.getMessage(), result.passed());
    assertEquals(2, comparatorCallCount);
  }
}
