/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-or-later

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you repurpose (entirely or
  partially) this file, you MUST add your own copyright notice in a separate comment block above
  this file header, listing the main changes you applied to the original source.

  This file (LocationAwareValueMatcher.java) is part of pdfclown-common-build module in pdfClown
  Common project <https://github.com/pdfclown/pdfclown-common> (this Program).

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
 * A ValueMatcher extension that provides location in form of prefix to the equals method.
 *
 * @author Duncan Mackinder
 *
 */
public interface LocationAwareValueMatcher<T> extends ValueMatcher<T> {
  /**
   * Match actual value with expected value. If match fails any of the following may occur, return
   * false, pass failure details to specified JSONCompareResult and return true, or throw
   * ValueMatcherException containing failure details. Passing failure details to JSONCompareResult
   * or returning via ValueMatcherException enables more useful failure description for cases where
   * expected value depends entirely or in part on configuration of the ValueMatcher and therefore
   * expected value passed to this method will not give a useful indication of expected value.
   *
   * @param prefix
   *          JSON path of the JSON item being tested
   * @param actual
   *          JSON value being tested
   * @param expected
   *          expected JSON value
   * @param result
   *          JSONCompareResult to which match failure may be passed
   * @return true if expected and actual equal or any difference has already been passed to
   *         specified result instance, false otherwise.
   * @throws ValueMatcherException
   *           if expected and actual values not equal and ValueMatcher needs to override default
   *           comparison failure message that would be generated if this method returned false.
   */
  boolean equal(String prefix, T actual, T expected, JSONCompareResult result)
      throws ValueMatcherException;
}
