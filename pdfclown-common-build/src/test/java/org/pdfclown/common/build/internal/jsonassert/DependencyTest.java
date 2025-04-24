/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  This file (DependencyTest.java) is part of pdfclown-common-build module in pdfClown Common project
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

import com.github.openjson.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for our external/third-party dependencies.
 *
 * @author Carter Page <carter@skyscreamer.org>
 */
public class DependencyTest {
  @Test
  public void nop() {
    // Cloudbees doesn't like a unit test class with no tests
  }

  //@Test  // For https://github.com/skyscreamer/JSONassert/issues/25
  public void testJSonGetLong() throws Exception {
    Long target = -4611686018427386614L;
    String targetString = target.toString();

    JSONObject value = new JSONObject().put("id", target);
    Assert.assertEquals(target, (Long) value.getLong("id")); //Correct: when put as long getLong is correct

    value = new JSONObject().put("id", targetString);
    Assert.assertEquals(target, (Long) Long.parseLong(value.getString("id"))); //Correct: when put as String getString is correct
    Assert.assertEquals(target, (Long) value.getLong("id")); //Bug: Having json convert the string to long fails
  }
}
