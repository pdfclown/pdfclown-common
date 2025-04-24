/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  This file (RegularExpressionValueMatcherTest.java) is part of pdfclown-common-build module in
  pdfClown Common project <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: © 2012-2022 Skyscreamer

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.common.build.internal.jsonassert;

import com.github.openjson.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.pdfclown.common.build.internal.jsonassert.comparator.CustomComparator;

/**
 * Unit tests for RegularExpressionValueMatcher
 *
 * @author Duncan Mackinder
 *
 */
public class RegularExpressionValueMatcherTest {
  private static final String ARRAY_ELEMENT_PREFIX = "d.results[0].__metadata.uri";
  private static final String JSON_STRING_WITH_ARRAY =
      "{d:{results:[{__metadata:{uri:\"http://localhost:80/Person('1')\",type:Person},id:1}]}}";
  private static final String CONSTANT_URI_REGEX_EXPECTED_JSON =
      "{d:{results:[{__metadata:{uri:X}}]}}";

  @Test
  public void constantRegexMatchesStringAttributeInsideArray() throws JSONException {
    doTest(ARRAY_ELEMENT_PREFIX, "http://localhost:80/Person\\('\\d+'\\)",
        CONSTANT_URI_REGEX_EXPECTED_JSON, JSON_STRING_WITH_ARRAY);
  }

  @Test
  public void constantRegexWithSimplePathMatchsStringAttribute() throws JSONException {
    doTest("a", "v.", "{a:x}", "{a:v1}");
  }

  @Test
  public void constantRegexWithThreeLevelPathMatchsStringAttribute() throws JSONException {
    doTest("a.b.c", ".*Is.*", "{a:{b:{c:x}}}", "{a:{b:{c:thisIsAString}}}");
  }

  @Test
  public void dynamicRegexMatchesStringAttributeInsideArray() throws JSONException {
    doTest(ARRAY_ELEMENT_PREFIX, null,
        "{d:{results:[{__metadata:{uri:\"http://localhost:80/Person\\\\('\\\\d+'\\\\)\"}}]}}",
        JSON_STRING_WITH_ARRAY);
  }

  @Test
  public void dynamicRegexMatchesStringAttributeInsideArrayWithNoArgConstructor()
      throws JSONException {
    JSONAssert.assertEquals(
        "{d:{results:[{__metadata:{uri:\"http://localhost:80/Person\\\\('\\\\d+'\\\\)\"}}]}}",
        JSON_STRING_WITH_ARRAY, new CustomComparator(JSONCompareMode.STRICT_ORDER,
            new Customization(ARRAY_ELEMENT_PREFIX, new RegularExpressionValueMatcher<>())));
  }

  @Test
  public void dynamicRegexWithSimplePathMatchsStringAttribute() throws JSONException {
    doTest("a", null, "{a:\"v.\"}", "{a:v1}");
  }

  @Test
  public void dynamicRegexWithThreeLevelPathMatchsStringAttribute() throws JSONException {
    doTest("a.b.c", null, "{a:{b:{c:\".*Is.*\"}}}", "{a:{b:{c:thisIsAString}}}");
  }

  @Test
  public void failsWhenConstantRegexDoesNotMatchStringAttributeInsideArray() throws JSONException {
    try {
      doTest(ARRAY_ELEMENT_PREFIX, "http://localhost:80/Person\\\\('\\\\w+'\\\\)",
          CONSTANT_URI_REGEX_EXPECTED_JSON, JSON_STRING_WITH_ARRAY);
    } catch (AssertionError e) {
      Assert.assertTrue("Invalid exception message returned: " + e.getMessage(), e.getMessage()
          .startsWith(ARRAY_ELEMENT_PREFIX + ": Constant expected pattern did not match value"));
    }
  }

  @Test
  public void failsWhenConstantRegexInvalid() throws JSONException {
    try {
      doTest(ARRAY_ELEMENT_PREFIX, "http://localhost:80/Person\\\\['\\\\d+'\\\\)",
          CONSTANT_URI_REGEX_EXPECTED_JSON, JSON_STRING_WITH_ARRAY);
    } catch (IllegalArgumentException ex) {
      Assert.assertTrue("Invalid exception message returned: " + ex.getMessage(),
          ex.getMessage().startsWith("Constant expected pattern invalid: "));
    }
  }

  @Test
  public void failsWhenDynamicRegexDoesNotMatchStringAttributeInsideArray() throws JSONException {
    try {
      doTest(ARRAY_ELEMENT_PREFIX, null,
          "{d:{results:[{__metadata:{uri:\"http://localhost:80/Person\\\\('\\\\w+'\\\\)\"}}]}}",
          JSON_STRING_WITH_ARRAY);
    } catch (AssertionError e) {
      Assert.assertTrue("Invalid exception message returned: " + e.getMessage(), e.getMessage()
          .startsWith(ARRAY_ELEMENT_PREFIX + ": Dynamic expected pattern did not match value"));
    }
  }

  @Test
  public void failsWhenDynamicRegexInvalid() throws JSONException {
    try {
      doTest(ARRAY_ELEMENT_PREFIX, null,
          "{d:{results:[{__metadata:{uri:\"http://localhost:80/Person('\\\\d+'\\\\)\"}}]}}",
          JSON_STRING_WITH_ARRAY);
    } catch (AssertionError e) {
      Assert.assertTrue("Invalid exception message returned: " + e.getMessage(),
          e.getMessage().startsWith(ARRAY_ELEMENT_PREFIX + ": Dynamic expected pattern invalid: "));
    }
  }

  private void doTest(String jsonPath, String regex, String expectedJSON, String actualJSON)
      throws JSONException {
    JSONAssert.assertEquals(expectedJSON, actualJSON,
        new CustomComparator(JSONCompareMode.STRICT_ORDER,
            new Customization(jsonPath, new RegularExpressionValueMatcher<>(regex))));
  }
}
