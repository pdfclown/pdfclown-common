/*
  SPDX-FileCopyrightText: © 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>
  SPDX-License-Identifier: LGPL-3.0-or-later

  Copyright 2023-2025 Stefano Chizzolini and contributors -
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (JSONCompareModeTest.java) is part of pdfclown-common-build module in pdfClown Common
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
package org.pdfclown.common.build.internal.jsonassert;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.pdfclown.common.build.internal.jsonassert.JSONCompareMode.LENIENT;
import static org.pdfclown.common.build.internal.jsonassert.JSONCompareMode.NON_EXTENSIBLE;
import static org.pdfclown.common.build.internal.jsonassert.JSONCompareMode.STRICT;
import static org.pdfclown.common.build.internal.jsonassert.JSONCompareMode.STRICT_ORDER;

import org.junit.Test;

/**
 * Unit tests for {@link JSONCompareMode}
 */
@SuppressWarnings("deprecation")
public class JSONCompareModeTest {
  @Test
  public void testWithExtensibility() {
    assertTrue(NON_EXTENSIBLE.withExtensible(true).isExtensible());
    assertFalse(NON_EXTENSIBLE.withExtensible(true).hasStrictOrder());
    assertTrue(STRICT.withExtensible(true).isExtensible());
    assertTrue(STRICT.withExtensible(true).hasStrictOrder());

    assertEquals(LENIENT, LENIENT.withExtensible(true));
    assertEquals(STRICT_ORDER, STRICT_ORDER.withExtensible(true));
  }

  @Test
  public void testWithoutExtensibility() {
    assertFalse(STRICT_ORDER.withExtensible(false).isExtensible());
    assertTrue(STRICT_ORDER.withExtensible(false).hasStrictOrder());
    assertFalse(LENIENT.withExtensible(false).isExtensible());
    assertFalse(LENIENT.withExtensible(false).hasStrictOrder());

    assertEquals(STRICT, STRICT.withExtensible(false));
    assertEquals(NON_EXTENSIBLE, NON_EXTENSIBLE.withExtensible(false));
  }

  @Test
  public void testWithoutStrictOrdering() {
    assertFalse(STRICT_ORDER.withStrictOrdering(false).hasStrictOrder());
    assertTrue(STRICT_ORDER.withStrictOrdering(false).isExtensible());
    assertFalse(STRICT.withStrictOrdering(false).hasStrictOrder());
    assertFalse(STRICT.withStrictOrdering(false).isExtensible());

    assertEquals(LENIENT, LENIENT.withStrictOrdering(false));
    assertEquals(NON_EXTENSIBLE, NON_EXTENSIBLE.withStrictOrdering(false));
  }

  @Test
  public void testWithStrictOrdering() {
    assertTrue(LENIENT.withStrictOrdering(true).hasStrictOrder());
    assertTrue(LENIENT.withStrictOrdering(true).isExtensible());
    assertTrue(NON_EXTENSIBLE.withStrictOrdering(true).hasStrictOrder());
    assertFalse(NON_EXTENSIBLE.withStrictOrdering(true).isExtensible());

    assertEquals(STRICT, STRICT.withStrictOrdering(true));
    assertEquals(STRICT_ORDER, STRICT_ORDER.withStrictOrdering(true));
  }
}
