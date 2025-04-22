/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (JSONCompareMode.java) is part of pdfclown-common-build module in pdfClown Common
  project <https://github.com/pdfclown/pdfclown-common> (this Program).

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

/**
 * <p>
 * These different modes define different behavior for the comparison of JSON for testing. Each mode
 * encapsulates two underlying behaviors: extensibility and strict ordering.
 * </p>
 *
 * <table border="1" summary="Behavior of JSONCompareMode">
 * <tr>
 * <th>&nbsp;</th>
 * <th>Extensible</th>
 * <th>Strict Ordering</th>
 * </tr>
 * <tr>
 * <th>STRICT</th>
 * <th>no</th>
 * <th>yes</th>
 * </tr>
 * <tr>
 * <th>LENIENT</th>
 * <th>yes</th>
 * <th>no</th>
 * </tr>
 * <tr>
 * <th>NON_EXTENSIBLE</th>
 * <th>no</th>
 * <th>no</th>
 * </tr>
 * <tr>
 * <th>STRICT_ORDER</th>
 * <th>yes</th>
 * <th>yes</th>
 * </tr>
 * </table>
 *
 * <p>
 * If extensibility not allowed, then all of the expected values must match in what's being tested,
 * but any additional fields will cause the test to fail. When extensibility is allowed, all values
 * must still match. For example, if you're expecting:
 * </p>
 *
 * <code>{id:1,name:"Carter"}</code>
 *
 * <p>
 * Then the following will pass when <i>extensible</i>, and will fail when not:
 * </p>
 *
 * <code>{id:1,name:"Carter",favoriteColor:"blue"}</code>
 *
 * <p>
 * If <i>strict ordering</i> is enabled, JSON arrays must be in strict sequence. For example, if
 * you're expecting:
 * </p>
 *
 * <code>{id:1,friends:[{id:<b>2</b>},{id:<b>3</b>}]}</code>
 *
 * <p>
 * Then the following will fail strict ordering, but will otherwise pass:
 * </p>
 *
 * <code>{id:1,friends:[{id:<b>3</b>},{id:<b>2</b>}]}</code>
 *
 */
public enum JSONCompareMode {
  /**
   * Strict checking. Not extensible, and strict array ordering.
   */
  STRICT(false, true),
  /**
   * Lenient checking. Extensible, and non-strict array ordering.
   */
  LENIENT(true, false),
  /**
   * Non-extensible checking. Not extensible, and non-strict array ordering.
   */
  NON_EXTENSIBLE(false, false),
  /**
   * Strict order checking. Extensible, and strict array ordering.
   */
  STRICT_ORDER(true, true);

  private final boolean _extensible;
  private final boolean _strictOrder;

  JSONCompareMode(boolean extensible, boolean strictOrder) {
    _extensible = extensible;
    _strictOrder = strictOrder;
  }

  /**
   * Strict order required
   *
   * @return True if results require strict array ordering, otherwise false.
   */
  public boolean hasStrictOrder() {
    return _strictOrder;
  }

  /**
   * Is extensible
   *
   * @return True if results can be extended from what's expected, otherwise false.
   */
  public boolean isExtensible() {
    return _extensible;
  }

  /**
   * Get the equivalent {@code JSONCompareMode} with or without extensibility.
   *
   * @param extensible
   *          if true, allows keys in actual that don't appear in expected
   * @return the equivalent {@code JSONCompareMode}
   */
  public JSONCompareMode withExtensible(boolean extensible) {
    if (extensible)
      return hasStrictOrder() ? STRICT_ORDER : LENIENT;
    else
      return hasStrictOrder() ? STRICT : NON_EXTENSIBLE;
  }

  /**
   * Get the equivalent {@code JSONCompareMode} with or without strict ordering.
   *
   * @param strictOrdering
   *          if true, requires strict ordering of array elements
   * @return the equivalent {@code JSONCompareMode}
   */
  public JSONCompareMode withStrictOrdering(boolean strictOrdering) {
    if (strictOrdering)
      return isExtensible() ? STRICT_ORDER : STRICT;
    else
      return isExtensible() ? LENIENT : NON_EXTENSIBLE;
  }
}
