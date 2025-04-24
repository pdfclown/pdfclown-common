/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  This file (ArrayValueMatcherTest.java) is part of pdfclown-common-build module in pdfClown Common
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;
import java.text.MessageFormat;
import org.junit.Test;
import org.pdfclown.common.build.internal.jsonassert.comparator.ArraySizeComparator;
import org.pdfclown.common.build.internal.jsonassert.comparator.CustomComparator;
import org.pdfclown.common.build.internal.jsonassert.comparator.DefaultComparator;
import org.pdfclown.common.build.internal.jsonassert.comparator.JSONComparator;

/**
 * Unit tests for ArrayValueMatcher
 *
 * @author Duncan Mackinder
 *
 */
public class ArrayValueMatcherTest {
  // SourceFQN: ARRAY_OF_JSONARRAYS
  private static final String JSON_SAMPLE__ARRAY_ARRAY =
      "{a:[[6,7,8],[9,10,11],[12,13,14],[19,20,21,22]]}";
  // SourceFQN: ARRAY_OF_INTEGERS
  private static final String JSON_SAMPLE__INT_ARRAY = "{a:[1,2,3,4,5]}";
  // SourceFQN: ARRAY_OF_JSONOBJECTS
  private static final String JSON_SAMPLE__OBJECT_ARRAY =
      "{a:[{background:white,id:1,type:row},{background:grey,id:2,type:row},{background:white,id:3,type:row},{background:grey,id:4,type:row}]}";

  // SourceFQN: comparator
  private static final JSONComparator COMPARATOR = new DefaultComparator(JSONCompareMode.LENIENT);

  @Test
  public void failsWhenAppliedToNonArray() throws JSONException {
    try {
      doTest("a", new ArrayValueMatcher<>(COMPARATOR), "{a:[{background:white}]}",
          "{a:{attr1:value1,attr2:value2}}");
    } catch (IllegalArgumentException ex) {
      assertEquals("Exception message", "ArrayValueMatcher applied to non-array actual value",
          ex.getMessage());
      return;
    }
    fail("Did not throw IllegalArgumentException");
  }

  @Test
  public void failsWhenInnerArraySizeDoesNotMatch() throws JSONException {
    JSONComparator innerArraySizeComparator = new ArraySizeComparator(JSONCompareMode.STRICT_ORDER);
    doFailingMatchTest("a", new ArrayValueMatcher<>(innerArraySizeComparator), "{a:[[3]]}",
        JSON_SAMPLE__ARRAY_ARRAY,
        "a\\[3\\]\\[\\]\\s*Expected:\\s*array size of 3 elements\\s*got:\\s*4 elements\\s*");
  }

  @Test
  public void failsWhenInnerJSONObjectArrayElementDoesNotMatch() throws JSONException {
    ArrayValueMatcher<Object> innerArrayValueMatcher = new ArrayValueMatcher<>(COMPARATOR, 1);
    JSONComparator innerArrayComparator = new CustomComparator(JSONCompareMode.LENIENT,
        new Customization("a[2]", innerArrayValueMatcher));
    doFailingMatchTest("a", new ArrayValueMatcher<>(innerArrayComparator, 2), // tests inner array i.e. [12,13,14]
        "{a:[[99]]}", JSON_SAMPLE__ARRAY_ARRAY,
        "a\\[2\\]\\[1\\]\\s*Expected:\\s*99\\s*got:\\s*13\\s*");
  }

  @Test
  public void failsWhenNotEveryElementOfJSONObjectArrayMatches() throws JSONException {
    doFailingMatchTest("a", new ArrayValueMatcher<>(COMPARATOR), "{a:[{background:white}]}",
        JSON_SAMPLE__OBJECT_ARRAY,
        "a\\[1\\]\\.background\\s*Expected:\\s*white\\s*got:\\s*grey\\s*;\\s*a\\[3\\]\\.background\\s*Expected:\\s*white\\s*got:\\s*grey\\s*");
  }

  @Test
  public void failsWhenSecondElementOfJSONObjectArrayDoesNotMatch() throws JSONException {
    doFailingMatchTest("a", new ArrayValueMatcher<>(COMPARATOR, 1),
        "{a:[{background:DOES_NOT_MATCH,id:2,type:row}]}", JSON_SAMPLE__OBJECT_ARRAY,
        "a\\[1\\]\\.background\\s*Expected:\\s*DOES_NOT_MATCH\\s*got:\\s*grey\\s*");
  }

  @Test
  public void failsWhenThirdElementOfJSONObjectArrayDoesNotMatchInMultiplePlaces()
      throws JSONException {
    doFailingMatchTest("a", new ArrayValueMatcher<>(COMPARATOR, 2),
        "{a:[{background:DOES_NOT_MATCH,id:3,type:WRONG_TYPE}]}", JSON_SAMPLE__OBJECT_ARRAY,
        "a\\[2\\]\\.background\\s*Expected:\\s*DOES_NOT_MATCH\\s*got:\\s*white\\s*;\\s*a\\[2\\]\\.type\\s*Expected:\\s*WRONG_TYPE\\s*got:\\s*row\\s*");
  }

  @Test
  public void failsWhenTwoElementOfSimpleValueArrayDoNotMatch() throws JSONException {
    doFailingMatchTest("a", new ArrayValueMatcher<>(COMPARATOR, 3, 4), "{a:[3,4]}",
        JSON_SAMPLE__INT_ARRAY,
        "a\\[3\\]\\s*Expected:\\s3\\s*got:\\s*4\\s*;\\s*a\\[4\\]\\s*Expected:\\s*4\\s*got:\\s*5\\s*");
  }

  @Test
  public void failsWhenTwoElementsOfJSONObjectArrayDoNotMatch() throws JSONException {
    doFailingMatchTest("a", new ArrayValueMatcher<>(COMPARATOR, 1, 2),
        "{a:[{background:DOES_NOT_MATCH,id:2,type:row},{background:white,id:3,type:WRONG_TYPE}]}",
        JSON_SAMPLE__OBJECT_ARRAY,
        "a\\[1\\]\\.background\\s*Expected:\\s*DOES_NOT_MATCH\\s*got:\\s*grey\\s*;\\s*a\\[2\\]\\.type\\s*Expected:\\s*WRONG_TYPE\\s*got:\\s*row\\s*");
  }

  @Test
  public void jsonObjectMatchesSecondElementOfJSONObjectArray() throws JSONException {
    doTest("a", new ArrayValueMatcher<>(COMPARATOR, 1), "{a:{background:grey,id:2,type:row}}",
        JSON_SAMPLE__OBJECT_ARRAY);
  }

  @Test
  public void matchesElementPairsStartingFromElement0OfJSONObjectArrayWhenRangeTooLarge()
      throws JSONException {
    doTest("a", new ArrayValueMatcher<>(COMPARATOR), "{a:[{background:white},{background:grey}]}",
        JSON_SAMPLE__OBJECT_ARRAY);
  }

  @Test
  public void matchesElementPairsStartingFromElement1OfJSONObjectArrayWhenRangeTooLarge()
      throws JSONException {
    doTest("a", new ArrayValueMatcher<>(COMPARATOR, 1, 500),
        "{a:[{background:grey},{background:white}]}", JSON_SAMPLE__OBJECT_ARRAY);
  }

  @Test
  public void matchesEveryElementOfJSONObjectArray() throws JSONException {
    doTest("a", new ArrayValueMatcher<>(COMPARATOR), "{a:[{type:row}]}", JSON_SAMPLE__OBJECT_ARRAY);
  }

  @Test
  public void matchesEveryElementOfJSONObjectArrayWhenRangeTooLarge() throws JSONException {
    doTest("a", new ArrayValueMatcher<>(COMPARATOR, 0, 500), "{a:[{type:row}]}",
        JSON_SAMPLE__OBJECT_ARRAY);
  }

  @Test
  public void matchesFirstElementOfArrayOfJSONArrays() throws JSONException {
    doTest("a", new ArrayValueMatcher<>(COMPARATOR, 0), "{a:[[6,7,8]]}", JSON_SAMPLE__ARRAY_ARRAY);
  }

  @Test
  public void matchesSecondElementOfJSONObjectArray() throws JSONException {
    doTest("a", new ArrayValueMatcher<>(COMPARATOR, 1), "{a:[{background:grey,id:2,type:row}]}",
        JSON_SAMPLE__OBJECT_ARRAY);
  }

  @Test
  public void matchesSizeOfFirstThreeInnerArrays() throws JSONException {
    JSONComparator innerArraySizeComparator = new ArraySizeComparator(JSONCompareMode.STRICT_ORDER);
    doTest("a", new ArrayValueMatcher<>(innerArraySizeComparator, 0, 2), "{a:[[3]]}",
        JSON_SAMPLE__ARRAY_ARRAY);
  }

  @Test
  public void matchesThirdElementOfSimpleValueArray() throws JSONException {
    doTest("a", new ArrayValueMatcher<>(COMPARATOR, 2), "{a:[3]}", JSON_SAMPLE__INT_ARRAY);
  }

  @Test
  public void simpleValueMatchesSecondElementOfJSONObjectArray() throws JSONException {
    doTest("a", new ArrayValueMatcher<>(COMPARATOR, 3), "{a:4}", JSON_SAMPLE__INT_ARRAY);
  }

  /*
   * Following tests verify the ability to match an element containing either a simple value or a
   * JSON object against simple value or JSON object without requiring expected value to be wrapped
   * in an array reducing slightly the syntactic load on teh test author & reader.
   */

  @Test
  public void verifyBackgroundAttributesOfEveryArrayElementAlternateBetweenWhiteAndGrey()
      throws JSONException {
    JSONComparator comparator = new DefaultComparator(JSONCompareMode.LENIENT);
    Customization customization = new Customization("a", new ArrayValueMatcher<>(comparator));
    JSONAssert.assertEquals("{a:[{background:white},{background:grey}]}", JSON_SAMPLE__OBJECT_ARRAY,
        new CustomComparator(JSONCompareMode.LENIENT, customization));
  }

  @Test
  public void verifyEveryArrayElementWithCustomComparator() throws JSONException {
    // get length of array we will verify
    int aLength =
        ((JSONArray) ((JSONObject) JSONParser.parseJSON(JSON_SAMPLE__OBJECT_ARRAY)).get("a"))
            .length();
    // create array of customizations one for each array element
    RegularExpressionValueMatcher<Object> regExValueMatcher =
        new RegularExpressionValueMatcher<>("\\d+"); // matches one or more digits
    Customization[] customizations = new Customization[aLength];
    for (int i = 0; i < aLength; i++) {
      String contextPath = "a[" + i + "].id";
      customizations[i] = new Customization(contextPath, regExValueMatcher);
    }
    CustomComparator regExComparator =
        new CustomComparator(JSONCompareMode.STRICT_ORDER, customizations);

    ArrayValueMatcher<Object> regExArrayValueMatcher = new ArrayValueMatcher<>(regExComparator);
    Customization regExArrayValueCustomization = new Customization("a", regExArrayValueMatcher);
    CustomComparator regExCustomArrayValueComparator = new CustomComparator(
        JSONCompareMode.STRICT_ORDER, new Customization[] { regExArrayValueCustomization });
    JSONAssert.assertEquals("{a:[{id:X}]}", JSON_SAMPLE__OBJECT_ARRAY,
        regExCustomArrayValueComparator);
  }

  @Test
  public void verifyEveryElementOfArrayIsJSONArrayOfLength3() throws JSONException {
    JSONComparator comparator = new ArraySizeComparator(JSONCompareMode.STRICT_ORDER);
    Customization customization = new Customization("a", new ArrayValueMatcher<>(comparator, 0, 2));
    JSONAssert.assertEquals("{a:[[3]]}", JSON_SAMPLE__ARRAY_ARRAY,
        new CustomComparator(JSONCompareMode.LENIENT, customization));
  }

  /*
   * Following tests contain copies of code quoted in ArrayValueMatcher JavaDoc and are included to
   * verify that the exact code documented works as expected.
   */
  @Test
  public void verifyIdAttributeOfFirstArrayElementMatches() throws JSONException {
    JSONComparator comparator = new DefaultComparator(JSONCompareMode.LENIENT);
    Customization customization = new Customization("a", new ArrayValueMatcher<>(comparator, 0));
    JSONAssert.assertEquals("{a:[{id:1}]}", JSON_SAMPLE__OBJECT_ARRAY,
        new CustomComparator(JSONCompareMode.LENIENT, customization));
  }

  @Test
  public void verifyIdAttributeOfFirstArrayElementMatchesSimplifiedExpectedSyntax()
      throws JSONException {
    JSONComparator comparator = new DefaultComparator(JSONCompareMode.LENIENT);
    Customization customization = new Customization("a", new ArrayValueMatcher<>(comparator, 0));
    JSONAssert.assertEquals("{a:{id:1}}", JSON_SAMPLE__OBJECT_ARRAY,
        new CustomComparator(JSONCompareMode.LENIENT, customization));
  }

  @Test
  public void verifySecondElementOfArrayIsJSONArrayWhoseFirstElementIs9() throws JSONException {
    Customization innerCustomization =
        new Customization("a[1]", new ArrayValueMatcher<>(COMPARATOR, 0));
    JSONComparator comparator = new CustomComparator(JSONCompareMode.LENIENT, innerCustomization);
    Customization customization = new Customization("a", new ArrayValueMatcher<>(comparator, 1));
    JSONAssert.assertEquals("{a:[[9]]}", JSON_SAMPLE__ARRAY_ARRAY,
        new CustomComparator(JSONCompareMode.LENIENT, customization));
  }

  @Test
  public void verifySecondElementOfArrayIsJSONArrayWhoseFirstElementIs9WithEvenMoreSimpliedExpectedString()
      throws JSONException {
    Customization innerCustomization =
        new Customization("a[1]", new ArrayValueMatcher<>(COMPARATOR, 0));
    JSONComparator comparator = new CustomComparator(JSONCompareMode.LENIENT, innerCustomization);
    Customization customization = new Customization("a", new ArrayValueMatcher<>(comparator, 1));
    JSONAssert.assertEquals("{a:9}", JSON_SAMPLE__ARRAY_ARRAY,
        new CustomComparator(JSONCompareMode.LENIENT, customization));
  }

  @Test
  public void verifySecondElementOfArrayIsJSONArrayWhoseFirstElementIs9WithSimpliedExpectedString()
      throws JSONException {
    Customization innerCustomization =
        new Customization("a[1]", new ArrayValueMatcher<>(COMPARATOR, 0));
    JSONComparator comparator = new CustomComparator(JSONCompareMode.LENIENT, innerCustomization);
    Customization customization = new Customization("a", new ArrayValueMatcher<>(comparator, 1));
    JSONAssert.assertEquals("{a:[9]}", JSON_SAMPLE__ARRAY_ARRAY,
        new CustomComparator(JSONCompareMode.LENIENT, customization));
  }

  @Test
  public void verifyTypeAttributeOfEveryArrayElementMatchesRow() throws JSONException {
    JSONComparator comparator = new DefaultComparator(JSONCompareMode.LENIENT);
    Customization customization = new Customization("a", new ArrayValueMatcher<>(comparator));
    JSONAssert.assertEquals("{a:[{type:row}]}", JSON_SAMPLE__OBJECT_ARRAY,
        new CustomComparator(JSONCompareMode.LENIENT, customization));
  }

  @Test
  public void verifyTypeAttributeOfSecondAndThirdElementMatchesRow() throws JSONException {
    JSONComparator comparator = new DefaultComparator(JSONCompareMode.LENIENT);
    Customization customization = new Customization("a", new ArrayValueMatcher<>(comparator, 1, 2));
    JSONAssert.assertEquals("{a:[{type:row}]}", JSON_SAMPLE__OBJECT_ARRAY,
        new CustomComparator(JSONCompareMode.LENIENT, customization));
  }

  private void doFailingMatchTest(String jsonPath, ArrayValueMatcher<Object> arrayValueMatcher,
      String expectedJSON, String actualJSON, String expectedMessagePattern) throws JSONException {
    try {
      doTest(jsonPath, arrayValueMatcher, expectedJSON, actualJSON);
    } catch (AssertionError e) {
      String failureMessage =
          MessageFormat.format("Exception message ''{0}'', does not match expected pattern ''{1}''",
              e.getMessage(), expectedMessagePattern);
      assertTrue(failureMessage, e.getMessage().matches(expectedMessagePattern));
      return;
    }
    fail("AssertionError not thrown");
  }

  private void doTest(String jsonPath, ArrayValueMatcher<Object> arrayValueMatcher,
      String expectedJSON, String actualJSON) throws JSONException {
    Customization customization = new Customization(jsonPath, arrayValueMatcher);
    JSONAssert.assertEquals(expectedJSON, actualJSON,
        new CustomComparator(JSONCompareMode.LENIENT, customization));
  }
}
