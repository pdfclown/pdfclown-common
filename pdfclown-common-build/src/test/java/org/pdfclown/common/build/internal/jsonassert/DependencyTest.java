/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (DependencyTest.java) is part of pdfclown-common-build module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common> (this Program).

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
