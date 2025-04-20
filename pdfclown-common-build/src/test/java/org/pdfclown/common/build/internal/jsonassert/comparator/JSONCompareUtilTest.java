/*
  SPDX-FileCopyrightText: © 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>
  SPDX-License-Identifier: LGPL-3.0-or-later

  Copyright 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (JSONCompareUtilTest.java) is part of pdfclown-common-build module in pdfClown Common
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Test JSONCompareUtil
 *
 * @author Carter Page <carter@skyscreamer.org>
 */
@SuppressWarnings("deprecation")
public class JSONCompareUtilTest {
  @Test
  public void testGetCardinalityMap() {
    final int NUM_A = 76;
    final int NUM_B = 3;
    final int NUM_C = 0;
    final int NUM_D = 1;
    final int NUM_E = 2;

    List<String> listToTest = new ArrayList<>(NUM_A + NUM_B + NUM_C + NUM_D + NUM_E);
    for (int i = 0; i < NUM_A; ++i) {
      listToTest.add("A");
    }
    for (int i = 0; i < NUM_B; ++i) {
      listToTest.add("B");
    }
    for (int i = 0; i < NUM_C; ++i) {
      listToTest.add("C");
    }
    for (int i = 0; i < NUM_D; ++i) {
      listToTest.add("D");
    }
    for (int i = 0; i < NUM_E; ++i) {
      listToTest.add("E");
    }
    Collections.shuffle(listToTest);

    Map<String, Integer> cardinalityMap = JSONCompareUtil.getCardinalityMap(listToTest);
    Assert.assertEquals(NUM_A, cardinalityMap.get("A").intValue());
    Assert.assertEquals(NUM_B, cardinalityMap.get("B").intValue());
    Assert.assertNull(cardinalityMap.get("C"));
    Assert.assertEquals(NUM_D, cardinalityMap.get("D").intValue());
    Assert.assertEquals(NUM_E, cardinalityMap.get("E").intValue());
  }
}
