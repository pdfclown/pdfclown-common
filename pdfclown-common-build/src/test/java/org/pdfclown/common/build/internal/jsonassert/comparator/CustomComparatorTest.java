/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  This file (CustomComparatorTest.java) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common>

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
import junit.framework.Assert;
import org.junit.Test;
import org.pdfclown.common.build.internal.jsonassert.JSONCompare;
import org.pdfclown.common.build.internal.jsonassert.JSONCompareMode;
import org.pdfclown.common.build.internal.jsonassert.JSONCompareResult;

/**
 * @author <a href="mailto:aiveeen@gmail.com">Ivan Zaytsev</a> 2013-01-04
 */
@SuppressWarnings("deprecation")
public class CustomComparatorTest {
  private static class ArrayOfJsonObjectsComparator extends DefaultComparator {
    public ArrayOfJsonObjectsComparator(JSONCompareMode mode) {
      super(mode);
    }

    @Override
    public void compareJSONArray(String prefix, JSONArray expected, JSONArray actual,
        JSONCompareResult result) throws JSONException {
      compareJSONArrayOfJsonObjects(prefix, expected, actual, result);
    }
  }

  @Test
  public void testFullArrayComparison() throws Exception {
    JSONCompareResult compareResult =
        JSONCompare.compareJSON("[{id:1}, {id:3}, {id:5}]", "[{id:1}, {id:3}, {id:6}, {id:7}]",
            new ArrayOfJsonObjectsComparator(JSONCompareMode.LENIENT));

    Assert.assertTrue(compareResult.failed());
    String message = compareResult.getMessage().replaceAll("\n", "");
    Assert.assertTrue(message,
        message.matches(".*id=5.*Expected.*id=6.*Unexpected.*id=7.*Unexpected.*"));
  }
}
